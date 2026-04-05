# Skill: GC Troubleshooting — Configuracao e Otimizacao do Garbage Collector

> Tipo: Diagnostic & Optimization Skill
> Escopo: JVM Garbage Collection — identificacao de problemas, tuning e configuracao para producao
> Ultima revisao: 2026-03-31

---

## 1. Fundamentos Essenciais

### 1.1 Por Que GC Importa para BrekFood

BrekFood e um sistema de delivery em tempo real. Pausas de GC impactam diretamente:

| Componente         | Impacto de GC pause           | Tolerancia |
|--------------------|-------------------------------|-----------|
| Dispatch Algorithm | Driver assignment atrasado    | < 50ms    |
| Order API          | Latencia no checkout          | < 100ms   |
| Pricing Engine     | Calculo de fee atrasado       | < 30ms    |
| Earnings Engine    | Calculo em background         | < 500ms   |
| Kafka Consumer     | Rebalance se pause > session  | < 10s     |
| WebSocket/SSE      | Tracking de delivery congela  | < 200ms   |

### 1.2 Heap Memory Model (Geracional)

```
+--------------------------------------------------+
|                    JVM Heap                        |
|                                                    |
|  +-------------+  +---------------------------+   |
|  |   Young Gen  |  |        Old Gen            |   |
|  |              |  |                           |   |
|  | +---+ +----+ |  |                           |   |
|  | |Eden| |S0|S1| |  |                           |   |
|  | +---+ +----+ |  |                           |   |
|  +-------------+  +---------------------------+   |
|                                                    |
+--------------------------------------------------+
|              Metaspace (off-heap)                  |
+--------------------------------------------------+

Eden     -> Objetos novos sao alocados aqui
S0/S1    -> Survivors (objetos que sobreviveram a Minor GC)
Old Gen  -> Objetos de longa vida (promovidos do Young)
Metaspace -> Metadados de classes (off-heap, cresce dinamicamente)
```

### 1.3 Tipos de GC Collection

| Tipo       | O que coleta | Pausa          | Frequencia |
|------------|-------------|----------------|-----------|
| Minor GC   | Young Gen    | Curta (1-50ms) | Frequente  |
| Major GC   | Old Gen      | Longa (50ms-5s)| Rara       |
| Full GC    | Heap inteiro | Muito longa    | Evitar!    |
| Mixed GC   | Young + partes do Old (G1) | Media | Periodica |

---

## 2. Collectors Disponiveis (Java 17-23)

### 2.1 Matriz de Collectors

| Collector      | Flag                        | Foco                  | Pause Target | Heap Size    | Producao? |
|---------------|----------------------------|-----------------------|-------------|-------------|-----------|
| G1GC          | `-XX:+UseG1GC`             | Balanceado            | < 200ms     | 4-32 GB     | SIM (default) |
| ZGC           | `-XX:+UseZGC`              | Ultra-low latency     | < 1ms       | 8 GB - 16 TB| SIM       |
| Shenandoah    | `-XX:+UseShenandoahGC`     | Low latency           | < 10ms      | 4 GB - 4 TB | SIM       |
| Parallel GC   | `-XX:+UseParallelGC`       | Max throughput         | Variavel    | 4-64 GB     | Batch only|
| Serial GC     | `-XX:+UseSerialGC`         | Single-thread         | Alto        | < 256 MB    | NAO       |

### 2.2 Qual Escolher para BrekFood

```
BrekFood API (latency-sensitive):
  Java 21+ -> ZGC (recomendado)
  Java 17  -> G1GC com tuning

BrekFood Worker/Consumer (throughput):
  -> G1GC (default, bom equilibrio)

BrekFood Simulation Engine (batch):
  -> Parallel GC ou G1GC com heap grande
```

### 2.3 G1GC — O Default (Java 9+)

**Como funciona:**
- Divide heap em regioes (~2048 regioes de tamanho igual)
- Coleta primeiro as regioes com mais lixo ("Garbage First")
- Pause target configuravel

**Flags essenciais:**
```bash
-XX:+UseG1GC                          # Ativar (default desde Java 9)
-XX:MaxGCPauseMillis=200              # Target de pausa (default: 200ms)
-XX:G1HeapRegionSize=8m               # Tamanho da regiao (auto: heap/2048)
-XX:InitiatingHeapOccupancyPercent=45 # Iniciar marking quando Old > 45%
-XX:G1ReservePercent=10               # Reserva para promotion failures
-XX:ConcGCThreads=4                   # Threads para concurrent marking
-XX:ParallelGCThreads=8               # Threads para STW phases
```

**Quando G1 sofre:**
- Humongous allocations (objetos > 50% da regiao) — JSONs grandes, byte arrays
- Mixed GC nao acompanha promotion rate
- Fragmentation em heaps muito grandes (> 32 GB)

### 2.4 ZGC — Ultra-Low Latency (Java 15+, Production-Ready Java 21+)

**Como funciona:**
- Concurrent, region-based, compacting
- Quase todo trabalho e concurrent (nao para a aplicacao)
- Colored pointers + load barriers

**Flags essenciais:**
```bash
-XX:+UseZGC                           # Ativar
-XX:+ZGenerational                    # ZGC geracional (Java 21+, RECOMENDADO)
-XX:SoftMaxHeapSize=4g                # Soft limit (ZGC tenta ficar abaixo)
-Xmx8g                                # Hard max heap
-XX:ZCollectionInterval=0             # 0 = coleta sob demanda
-XX:ZFragmentationLimit=25            # Limite de fragmentacao (%)
```

**Vantagens para BrekFood:**
- Pausas < 1ms independente do heap size
- Perfeito para APIs real-time (dispatch, tracking)
- Sem tuning complexo — "just works"

**Trade-off:**
- ~3-5% mais CPU que G1
- ~10-15% mais memoria (overhead de colored pointers)

### 2.5 Shenandoah — Low Latency Alternativo

```bash
-XX:+UseShenandoahGC
-XX:ShenandoahGCHeuristics=adaptive   # adaptive | compact | aggressive
-XX:ShenandoahMinFreeThreshold=10     # % livre minimo antes de trigger
```

**Quando usar**: Se ZGC nao esta disponivel na JDK distribuicao (ex: Oracle JDK nao inclui Shenandoah)

---

## 3. Configuracao por Ambiente

### 3.1 Desenvolvimento Local

```bash
# Leve, rapido de iniciar
JAVA_OPTS="-Xms256m -Xmx512m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=100"
```

### 3.2 Testes / CI

```bash
# Rapido, com GC logging para diagnostico
JAVA_OPTS="-Xms512m -Xmx1g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=100 \
  -Xlog:gc*:file=gc-test.log:time,uptime,level,tags:filecount=3,filesize=10m"
```

### 3.3 Producao — API (latency-sensitive)

```bash
# Java 21+ com ZGC generacional
JAVA_OPTS="-Xms2g -Xmx4g \
  -XX:+UseZGC \
  -XX:+ZGenerational \
  -XX:SoftMaxHeapSize=3g \
  -XX:+AlwaysPreTouch \
  -XX:+UseTransparentHugePages \
  -XX:+UseNUMA \
  -Xlog:gc*:file=/var/log/brekfood/gc.log:time,uptime,level,tags:filecount=10,filesize=50m"
```

```bash
# Java 17 com G1GC tunado
JAVA_OPTS="-Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=100 \
  -XX:G1HeapRegionSize=16m \
  -XX:InitiatingHeapOccupancyPercent=35 \
  -XX:+AlwaysPreTouch \
  -XX:+UseStringDeduplication \
  -Xlog:gc*:file=/var/log/brekfood/gc.log:time,uptime,level,tags:filecount=10,filesize=50m"
```

### 3.4 Producao — Worker/Consumer Kafka

```bash
JAVA_OPTS="-Xms1g -Xmx2g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+UseStringDeduplication \
  -Xlog:gc*:file=/var/log/brekfood/gc-worker.log:time,uptime,level,tags:filecount=5,filesize=20m"
```

### 3.5 Dockerfile Integration

```dockerfile
FROM eclipse-temurin:21-jre-alpine

ENV JAVA_OPTS="-Xms2g -Xmx4g \
  -XX:+UseZGC \
  -XX:+ZGenerational \
  -XX:SoftMaxHeapSize=3g \
  -XX:+AlwaysPreTouch \
  -Xlog:gc*:file=/var/log/gc.log:time,uptime,level,tags:filecount=5,filesize=50m"

COPY target/*.jar app.jar

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]
```

### 3.6 Spring Boot application.properties (Container-Aware)

```properties
# Respeitar limites do container
# Java 17+ ja respeita cgroups v2 por padrao

# Se precisar forcar:
# -XX:+UseContainerSupport (default: true)
# -XX:MaxRAMPercentage=75.0   (usar 75% da RAM do container)
# -XX:InitialRAMPercentage=50.0
```

---

## 4. Diagnostico — Identificando Problemas de GC

### 4.1 Sintomas e Causas

| Sintoma                           | Causa Provavel                  | Investigar                    |
|-----------------------------------|--------------------------------|-------------------------------|
| Latencia p99 alta esporadicamente | Long GC pause (Full GC)       | GC logs, heap usage           |
| Throughput caindo gradualmente    | Memory leak (Old Gen crescendo)| Heap dump, allocation profile |
| OOM (OutOfMemoryError)           | Leak ou heap subdimensionado   | Heap dump, GC logs            |
| CPU alta sem carga               | GC thrashing (heap muito pequeno)| GC logs, CPU profiling      |
| Kafka consumer rebalance         | GC pause > session.timeout     | GC logs, Kafka metrics        |
| App nao inicia (lento)           | Heap muito grande + AlwaysPreTouch | Startup logs              |
| Metaspace OOM                    | Class loader leak (hot reload) | -XX:MaxMetaspaceSize, jmap    |

### 4.2 GC Logging (OBRIGATORIO em producao)

**Java 17+ (Unified Logging):**
```bash
# Log completo para arquivo
-Xlog:gc*:file=gc.log:time,uptime,level,tags:filecount=10,filesize=50m

# Log resumido para stdout (containers)
-Xlog:gc:stdout:time,level

# Log detalhado (debugging)
-Xlog:gc*,gc+phases=debug,gc+age=trace:file=gc-debug.log:time,uptime,level,tags
```

**O que cada tag mostra:**
```
gc          -> Eventos de GC basicos
gc+heap     -> Estado do heap antes/depois
gc+phases   -> Fases do GC com duracao
gc+age      -> Distribuicao de idade dos objetos
gc+alloc    -> Falhas de alocacao
gc+promotion-> Promocao Young -> Old
gc+humongous-> Alocacoes humongous (G1)
gc+ergo     -> Decisoes ergonomicas do GC
```

### 4.3 Comandos de Diagnostico em Runtime

```bash
# Listar processos Java
jps -lv

# GC stats resumido
jstat -gc <pid> 1000 10
# (coleta a cada 1s, 10 amostras)

# GC causa
jstat -gccause <pid> 1000

# Heap summary
jmap -heap <pid>

# Histogram de objetos (sem heap dump completo)
jmap -histo <pid> | head -30

# Heap dump (CUIDADO: pausa a app!)
jmap -dump:format=b,file=heap.hprof <pid>

# Heap dump live (so objetos alcancaveis, menor)
jmap -dump:live,format=b,file=heap-live.hprof <pid>

# Flight Recorder (zero-overhead profiling)
jcmd <pid> JFR.start name=brekfood duration=60s filename=recording.jfr

# GC info via jcmd
jcmd <pid> GC.heap_info
jcmd <pid> VM.flags
jcmd <pid> VM.info
```

### 4.4 Metricas Spring Boot Actuator

```properties
# application.properties
management.endpoints.web.exposure.include=health,metrics,prometheus
management.metrics.tags.application=brekfood
```

**Metricas JVM expostas automaticamente (Micrometer):**
```
jvm.gc.pause                  -> Duracao das pausas de GC
jvm.gc.pause.count            -> Quantidade de pausas
jvm.gc.memory.allocated       -> Bytes alocados
jvm.gc.memory.promoted        -> Bytes promovidos para Old
jvm.gc.live.data.size         -> Tamanho do Old Gen apos Full GC
jvm.gc.max.data.size          -> Max Old Gen
jvm.memory.used               -> Memoria usada por area
jvm.memory.committed          -> Memoria committed por area
jvm.buffer.memory.used        -> Direct/mapped buffers
```

**Alertas recomendados (Prometheus/Grafana):**
```yaml
# Alerta: GC pause > 500ms
- alert: HighGCPause
  expr: max(jvm_gc_pause_seconds_max) > 0.5
  for: 5m
  labels:
    severity: warning

# Alerta: Heap > 85%
- alert: HighHeapUsage
  expr: jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} > 0.85
  for: 10m
  labels:
    severity: critical

# Alerta: GC overhead > 10% do tempo
- alert: GCOverhead
  expr: rate(jvm_gc_pause_seconds_sum[5m]) > 0.10
  for: 5m
  labels:
    severity: warning
```

---

## 5. Troubleshooting — Cenarios Comuns

### 5.1 Cenario: Full GC Frequente (G1GC)

**Sintomas:**
- Logs mostram `[Full GC (Allocation Failure)]`
- Old Gen constantemente > 80%
- Pausas de varios segundos

**Diagnostico:**
```bash
# Verificar distribuicao do heap
jstat -gc <pid> 1000

# Verificar se tem objetos humongous
grep -i "humongous" gc.log | tail -20

# Heap histogram (o que esta ocupando espaco?)
jmap -histo:live <pid> | head -30
```

**Solucoes (em ordem):**
```
1. Aumentar heap: -Xmx (se o container permite)
2. Reduzir IHOP: -XX:InitiatingHeapOccupancyPercent=30
   (inicia concurrent marking mais cedo)
3. Aumentar regioes: -XX:G1HeapRegionSize=16m ou 32m
   (reduz humongous allocations)
4. Verificar memory leak com heap dump
5. Considerar migrar para ZGC (Java 21+)
```

### 5.2 Cenario: Memory Leak (Heap Crescendo)

**Sintomas:**
- Old Gen cresce continuamente (nao estabiliza)
- Full GC libera cada vez menos memoria
- Eventual OOM

**Diagnostico:**
```bash
# Tirar 2 heap dumps com intervalo de 10-30min
jmap -dump:live,format=b,file=heap1.hprof <pid>
# (esperar 10-30 minutos)
jmap -dump:live,format=b,file=heap2.hprof <pid>

# Comparar no Eclipse MAT ou VisualVM:
# - Leak Suspects report
# - Dominator tree
# - Histogram diff
```

**Causas comuns em Spring Boot:**
```
1. @Scope("singleton") segurando referencias (ConcurrentHashMap crescendo)
2. Cache sem eviction (Caffeine/Ehcache mal configurado)
3. DataSource connection leak (conexao nao devolvida ao pool)
4. ThreadLocal nao limpo (especialmente com thread pools)
5. Listeners/observers nao desregistrados
6. EntityManager nao fechado (JPA/Hibernate)
7. StringBuilder/StringBuffer em loops sem clear
8. Static collections (Map, List) que so crescem
```

**Fix pattern:**
```java
// ERRADO: cache sem limite
private static final Map<String, Object> cache = new ConcurrentHashMap<>();

// CERTO: cache com eviction
private final Cache<String, Object> cache = Caffeine.newBuilder()
    .maximumSize(10_000)
    .expireAfterWrite(Duration.ofMinutes(10))
    .build();
```

### 5.3 Cenario: Latencia p99 Alta por GC (API)

**Sintomas:**
- p50 = 15ms, p99 = 800ms (outliers)
- Outliers correlacionam com GC pauses nos logs
- Afeta dispatch algorithm e pricing engine

**Diagnostico:**
```bash
# Correlacionar timestamps de GC com latencia
grep "pause" gc.log | awk '{print $1, $NF}' | sort -k2 -rn | head -20

# Verificar tipo de pausa
grep -E "Full GC|to-space|Allocation Failure" gc.log
```

**Solucoes:**
```
Opcao A: Tuning G1GC
  -XX:MaxGCPauseMillis=50        # Mais agressivo (default 200)
  -XX:G1NewSizePercent=40        # Mais espaco para Young Gen
  -XX:G1MaxNewSizePercent=60     # Limitar Young Gen

Opcao B: Migrar para ZGC (recomendado se Java 21+)
  -XX:+UseZGC -XX:+ZGenerational
  Resultado: pausas < 1ms, p99 estavel

Opcao C: Reduzir alocacao (codigo)
  - Usar primitivos ao inves de wrappers
  - Object pooling para objetos quentes (Location, Money)
  - Evitar String concatenation em hot paths
  - Usar record classes (menor footprint)
```

### 5.4 Cenario: Kafka Consumer Rebalance por GC

**Sintomas:**
- Consumer group rebalance frequente
- Logs Kafka: `Member X has failed heartbeat`
- GC pause > `session.timeout.ms`

**Diagnostico:**
```bash
# Verificar pausas longas
grep "Total" gc.log | awk -F'pause' '{print $2}' | sort -rn | head -10

# Verificar configs Kafka
grep -E "session.timeout|heartbeat" application.properties
```

**Solucoes:**
```properties
# Aumentar tolerancia do Kafka
spring.kafka.consumer.properties.session.timeout.ms=30000
spring.kafka.consumer.properties.heartbeat.interval.ms=10000
spring.kafka.consumer.properties.max.poll.interval.ms=600000
spring.kafka.consumer.max-poll-records=100

# E otimizar GC (ver opcoes acima)
```

### 5.5 Cenario: Metaspace OOM

**Sintomas:**
- `java.lang.OutOfMemoryError: Metaspace`
- Geralmente apos muitos redeploys (dev/staging)

**Diagnostico:**
```bash
# Verificar uso de Metaspace
jstat -gcmetacapacity <pid>

# Listar classloaders
jcmd <pid> VM.classloader_stats
```

**Solucoes:**
```bash
# Limitar Metaspace (prevenir consumo infinito)
-XX:MaxMetaspaceSize=512m
-XX:MetaspaceSize=256m

# Em dev com hot-reload: reiniciar periodicamente
# Em prod: investigar class loader leak (libs de proxy, reflection)
```

### 5.6 Cenario: Container OOM Kill (Docker/K8s)

**Sintomas:**
- Container morto com exit code 137 (SIGKILL)
- `dmesg` mostra OOM killer
- Heap + off-heap > container memory limit

**Diagnostico:**
```bash
# Verificar memoria nativa
jcmd <pid> VM.native_memory summary

# Verificar mapeamento de memoria
cat /proc/<pid>/status | grep -i vm
```

**Solucoes:**
```bash
# Regra: container_memory = heap + non-heap + overhead
# Heap = 75% da RAM do container
# Exemplo: container 4GB -> heap 3GB

-XX:MaxRAMPercentage=75.0
-XX:+UseContainerSupport          # default true no Java 17+
-XX:MaxDirectMemorySize=256m      # limitar NIO buffers

# OU valores fixos:
-Xmx3g                           # para container de 4GB
-XX:MaxMetaspaceSize=256m
-XX:ReservedCodeCacheSize=256m
-XX:MaxDirectMemorySize=256m
```

**Calculo de memoria total:**
```
Total = Heap (Xmx)
      + Metaspace (~150-300MB)
      + Code Cache (~100-250MB)
      + Thread Stacks (threads * Xss, default 1MB)
      + Direct Buffers (NIO)
      + Native Memory (JNI, libs)
      + GC overhead (~5-10% do heap)

Container limit = Total * 1.1 (margem 10%)
```

---

## 6. Tuning Avancado

### 6.1 Sizing do Heap

**Regras de ouro:**
```
1. Xms = Xmx (SEMPRE em producao)
   Motivo: evitar resize do heap em runtime (causa pausa)

2. Heap nao deve ultrapassar 75% da RAM do host/container
   Motivo: SO, Metaspace, threads, buffers precisam de espaco

3. Se Old Gen esta constantemente > 70%: aumente o heap
   Se Old Gen esta constantemente < 30%: diminua o heap (economize)

4. Young Gen = 30-50% do heap total (para APIs com muita alocacao)
```

### 6.2 Flags de Performance Universal

```bash
# Pre-touch: aloca paginas na inicializacao (evita page faults em runtime)
-XX:+AlwaysPreTouch

# Huge Pages: reduz TLB misses (requer config do SO)
-XX:+UseTransparentHugePages

# NUMA: otimiza acesso a memoria em servidores multi-socket
-XX:+UseNUMA

# String Deduplication (G1 only): reduz duplicatas de String no heap
-XX:+UseStringDeduplication

# Compressed Oops: referencia 32-bit em heaps < 32GB (default: auto)
-XX:+UseCompressedOops

# Disable biased locking (removido no Java 18+, desabilitar no 17)
-XX:-UseBiasedLocking
```

### 6.3 Tuning para Virtual Threads (Java 21+)

```bash
# Virtual threads criam carrier threads = CPU cores
# Heap pressure muda: mais objetos short-lived, menos thread stacks

# Recomendacao:
-XX:+UseZGC -XX:+ZGenerational    # Melhor para muitos objetos short-lived
-Xmx4g                             # Heap suficiente para concurrency alta
# NAO precisa de -Xss (virtual threads usam heap, nao stack nativo)
```

### 6.4 GC Tuning Decision Tree

```
Inicio
  |
  +--> Latencia e critica? (API real-time, dispatch)
  |     |
  |     +--> Java 21+ disponivel?
  |     |     |
  |     |     +--> SIM: Use ZGC Generational
  |     |     |     -XX:+UseZGC -XX:+ZGenerational
  |     |     |
  |     |     +--> NAO: Use G1GC tunado
  |     |           -XX:MaxGCPauseMillis=50
  |     |           -XX:G1NewSizePercent=40
  |     |
  |     +--> Pausas ainda altas com G1?
  |           |
  |           +--> Verificar humongous allocations
  |           +--> Aumentar G1HeapRegionSize
  |           +--> Reduzir alocacao no codigo
  |
  +--> Throughput e prioridade? (batch, simulation, workers)
  |     |
  |     +--> Heap < 8GB: G1GC (default)
  |     +--> Heap > 8GB: G1GC ou Parallel GC
  |           -XX:+UseParallelGC (se latencia nao importa)
  |
  +--> Memoria limitada? (container pequeno, < 512MB)
        |
        +--> Use Serial GC: -XX:+UseSerialGC
        +--> Ou considere GraalVM Native Image
```

---

## 7. Ferramentas de Analise

### 7.1 GC Log Analyzers

| Ferramenta        | Tipo      | URL / Comando                           |
|-------------------|-----------|-----------------------------------------|
| GCEasy            | Online    | https://gceasy.io                       |
| GCViewer          | Desktop   | https://github.com/chewiebug/GCViewer   |
| Censum            | Desktop   | https://www.jclarity.com/censum/        |
| Eclipse MAT       | Desktop   | https://eclipse.dev/mat/                |
| VisualVM          | Desktop   | https://visualvm.github.io/             |
| JDK Mission Control | Desktop | Incluso no JDK                          |
| async-profiler    | CLI       | https://github.com/async-profiler/async-profiler |

### 7.2 Workflow de Analise

```
1. Coletar GC logs (ja deve estar habilitado em prod)
2. Upload no GCEasy -> relatorio automatico
3. Identificar:
   - Throughput (% do tempo que NAO e GC)
   - Pause distribution (p50, p95, p99)
   - Heap after GC trend (leak indicator)
   - Promotion rate (Young -> Old)
   - Allocation rate
4. Se leak suspected:
   - Heap dump -> Eclipse MAT -> Leak Suspects
5. Se allocation pressure:
   - async-profiler com -e alloc -> flame graph de alocacao
6. Aplicar fix -> coletar novos logs -> comparar
```

### 7.3 async-profiler (Allocation Profiling)

```bash
# Profiling de alocacao (identifica hot allocation sites)
./asprof -e alloc -d 30 -f alloc-flame.html <pid>

# Profiling de CPU
./asprof -e cpu -d 30 -f cpu-flame.html <pid>

# Profiling de lock contention
./asprof -e lock -d 30 -f lock-flame.html <pid>

# JFR output (para JDK Mission Control)
./asprof -e alloc -d 60 -o jfr -f recording.jfr <pid>
```

---

## 8. Checklist de Producao

### 8.1 Pre-Deploy Checklist

```
[ ] Xms = Xmx (sem resize em runtime)
[ ] GC logging habilitado com rotacao de arquivos
[ ] Collector escolhido conscientemente (nao usar default sem pensar)
[ ] Container memory limit = heap + 25-30% overhead
[ ] -XX:+AlwaysPreTouch habilitado
[ ] MaxMetaspaceSize definido (evitar crescimento infinito)
[ ] MaxDirectMemorySize definido (se usa NIO/Netty)
[ ] Actuator metrics expostos (jvm.gc.pause, jvm.memory.*)
[ ] Alertas configurados (heap > 85%, pause > 500ms, GC overhead > 10%)
[ ] Heap dump on OOM habilitado: -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp/
[ ] Exit on OOM (para container restart): -XX:+ExitOnOutOfMemoryError
```

### 8.2 Configuracao Completa Recomendada (BrekFood API - Java 21)

```bash
JAVA_OPTS="\
  -Xms3g \
  -Xmx3g \
  -XX:+UseZGC \
  -XX:+ZGenerational \
  -XX:SoftMaxHeapSize=2500m \
  -XX:+AlwaysPreTouch \
  -XX:+UseTransparentHugePages \
  -XX:MaxMetaspaceSize=384m \
  -XX:MaxDirectMemorySize=256m \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/var/log/brekfood/heapdump.hprof \
  -XX:+ExitOnOutOfMemoryError \
  -Xlog:gc*:file=/var/log/brekfood/gc.log:time,uptime,level,tags:filecount=10,filesize=50m \
  -Djava.security.egd=file:/dev/./urandom"
```

### 8.3 Configuracao Completa Recomendada (BrekFood API - Java 17)

```bash
JAVA_OPTS="\
  -Xms3g \
  -Xmx3g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=100 \
  -XX:G1HeapRegionSize=16m \
  -XX:InitiatingHeapOccupancyPercent=35 \
  -XX:G1NewSizePercent=40 \
  -XX:G1MaxNewSizePercent=60 \
  -XX:+UseStringDeduplication \
  -XX:+AlwaysPreTouch \
  -XX:MaxMetaspaceSize=384m \
  -XX:MaxDirectMemorySize=256m \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/var/log/brekfood/heapdump.hprof \
  -XX:+ExitOnOutOfMemoryError \
  -Xlog:gc*:file=/var/log/brekfood/gc.log:time,uptime,level,tags:filecount=10,filesize=50m \
  -Djava.security.egd=file:/dev/./urandom"
```

---

## 9. Anti-Patterns (EVITAR)

| Anti-Pattern                              | Por que e ruim                                | O que fazer                          |
|-------------------------------------------|-----------------------------------------------|--------------------------------------|
| `Xms != Xmx` em producao                 | Resize causa pausa, fragmentacao              | Sempre Xms = Xmx                    |
| GC logging desabilitado em prod           | Voando cego — impossivel diagnosticar         | Sempre habilitar com rotacao         |
| `System.gc()` no codigo                   | Forca Full GC desnecessario                   | Remover; ou `-XX:+DisableExplicitGC` |
| Heap > 75% da RAM do container            | OOM Kill pelo SO                              | Calcular overhead corretamente       |
| Finalizers (`finalize()`)                 | Atrasa GC, pode causar leak                   | Usar `Cleaner` ou try-with-resources |
| Weak/SoftReferences como cache            | Comportamento imprevisivel sob GC pressure    | Usar Caffeine/Ehcache               |
| `-XX:+AggressiveOpts`                     | Removido/ignorado, flags experimentais        | Usar flags especificas documentadas  |
| Tuning cego (copiar flags do StackOverflow)| Cada app tem perfil diferente                | Medir, tunar, medir de novo         |

---

## 10. Referencia Rapida

### Flags mais usadas
```bash
# Heap
-Xms / -Xmx                     # Min/Max heap
-XX:MaxRAMPercentage=75.0        # Heap como % da RAM (containers)

# Collector
-XX:+UseG1GC                     # G1 (default)
-XX:+UseZGC -XX:+ZGenerational   # ZGC generacional (Java 21+)
-XX:+UseShenandoahGC             # Shenandoah

# G1 tuning
-XX:MaxGCPauseMillis=N           # Target de pausa
-XX:G1HeapRegionSize=Nm          # Tamanho da regiao
-XX:InitiatingHeapOccupancyPercent=N # IHOP

# Diagnostico
-XX:+HeapDumpOnOutOfMemoryError  # Dump automatico em OOM
-XX:HeapDumpPath=/path           # Destino do dump
-XX:+ExitOnOutOfMemoryError      # Exit para restart pelo orchestrator

# Logging
-Xlog:gc*:file=gc.log:time,uptime,level,tags:filecount=N,filesize=Nm

# Performance
-XX:+AlwaysPreTouch              # Pre-alocar paginas
-XX:+UseStringDeduplication      # Dedup de Strings (G1 only)
-XX:+UseTransparentHugePages     # THP (requer config SO)
```

