# Skill: Stack Upgrade — Java, Spring Boot, Spring Cloud, Kafka & Cloud Dependencies

> Tipo: Procedural Skill
> Escopo: Atualização controlada de versões de toda a stack JVM/Spring
> Última revisão: 2026-03-31

---

## 1. Filosofia de Upgrade

### Princípios

1. **Nunca atualize tudo de uma vez** — isole camadas (runtime → framework → libs)
2. **Sempre leia o Release Notes + Migration Guide** antes de tocar no pom.xml
3. **Branch dedicada** — `upgrade/stack-<target-version>` com PR isolado
4. **Testes primeiro** — se não tem cobertura, escreva antes de atualizar
5. **Rollback plan** — tag antes do upgrade, branch de fallback

### Ordem de Upgrade (SEMPRE seguir)

```
1. Java (JDK runtime)
2. Spring Boot (parent BOM)
3. Spring Cloud (BOM release train)
4. Spring Security
5. Spring Data
6. Kafka (spring-kafka + broker)
7. Database drivers
8. Cloud dependencies (AWS SDK, GCP, Azure)
9. Utilitários (Lombok, MapStruct, Jackson, etc.)
10. Plugins de build (Maven/Gradle, JaCoCo, etc.)
```

> Lógica: o runtime sustenta o framework, o framework sustenta as libs.

---

## 2. Java Upgrade

### 2.1 Compatibility Matrix

| Java | Spring Boot | Spring Cloud | Kafka Client | Status      |
|------|-------------|--------------|-------------- |-------------|
| 17   | 3.0 – 3.5   | 2022.0 – 2024.0 | 3.4 – 3.7 | LTS stable  |
| 21   | 3.2 – 3.5+  | 2023.0+      | 3.6+         | LTS current |
| 23   | 3.5+        | 2024.0+      | 3.8+         | Latest      |

### 2.2 Checklist: Java 17 → 21

- [ ] Verificar compatibilidade no [Spring Boot Wiki](https://github.com/spring-projects/spring-boot/wiki)
- [ ] Atualizar `<java.version>21</java.version>` no pom.xml
- [ ] Atualizar `JAVA_HOME` no CI/CD
- [ ] Atualizar `Dockerfile` base image: `eclipse-temurin:21-jre-alpine`
- [ ] Atualizar `mvnw` / `.mvn/wrapper/maven-wrapper.properties` se necessário
- [ ] Rodar build completo: `./mvnw clean verify`
- [ ] Verificar warnings de deprecated APIs removidas
- [ ] Revisar reflection-based code (impactado por strong encapsulation no JDK 17+)
- [ ] Testar com `--add-opens` se necessário para libs legacy

### 2.3 Checklist: Java 21 → 23

- [ ] Mesmos passos acima
- [ ] Verificar preview features usadas (virtual threads, pattern matching)
- [ ] Confirmar que Lombok suporta a versão (Lombok é sensível a mudanças do compiler)
- [ ] Confirmar que ByteBuddy/Mockito suporta (core do Spring test)

### 2.4 Features a Explorar por Versão

**Java 21 (LTS)**:
- Virtual Threads (Project Loom) — `spring.threads.virtual.enabled=true`
- Sequenced Collections
- Pattern Matching for switch (final)
- Record Patterns (final)
- String Templates (preview)

**Java 23+**:
- Structured Concurrency (preview)
- Scoped Values (preview)
- Unnamed Patterns
- Markdown Documentation Comments

### 2.5 Comandos de Diagnóstico

```bash
# Verificar versão compilada dos .class
javap -verbose MyClass.class | grep "major version"
# Java 17 = 61, Java 21 = 65, Java 23 = 67

# Verificar módulos necessários
jdeps --multi-release 21 -s target/*.jar

# Scan de APIs removidas
jdeprscan --release 21 target/*.jar
```

---

## 3. Spring Boot Upgrade

### 3.1 Release Train

| Spring Boot | Spring Framework | Mínimo Java | Mínimo Maven |
|-------------|-----------------|-------------|--------------|
| 3.2.x       | 6.1.x           | 17          | 3.6.3        |
| 3.3.x       | 6.1.x           | 17          | 3.6.3        |
| 3.4.x       | 6.2.x           | 17          | 3.6.3        |
| 3.5.x       | 6.2.x           | 17          | 3.9.x        |

### 3.2 Procedimento de Upgrade

```
1. Ler Migration Guide: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-X.Y-Release-Notes
2. Atualizar parent BOM
3. Remover version overrides que agora são gerenciados pelo BOM
4. Verificar properties renomeadas/removidas
5. Build + testes
6. Verificar deprecations nos logs de startup
```

### 3.3 Checklist Geral

- [ ] Atualizar `<version>` no `<parent>` spring-boot-starter-parent
- [ ] Rodar `./mvnw spring-boot:run` e verificar warnings de startup
- [ ] Verificar `application.properties` por keys renomeadas
- [ ] Executar `./mvnw dependency:tree` — buscar conflitos de versão
- [ ] Rodar suite de testes completa
- [ ] Verificar auto-configuration changes (novos starters, removidos)
- [ ] Verificar se `@Conditional*` mudaram de comportamento

### 3.4 Breaking Changes Comuns (3.x)

| Versão | Breaking Change                                       | Ação                                              |
|--------|-------------------------------------------------------|---------------------------------------------------|
| 3.0    | Jakarta EE 9+ (javax → jakarta)                      | Rename todos os imports                           |
| 3.0    | Spring Security 6 (authorize requests DSL)            | Reescrever SecurityFilterChain                    |
| 3.2    | RestClient como alternativa ao RestTemplate           | Avaliar migração                                  |
| 3.2    | Virtual Threads support                               | Habilitar se Java 21+                             |
| 3.3    | Docker Compose support nativo                         | Usar `spring-boot-docker-compose`                 |
| 3.4    | Structured logging                                    | Migrar para novo formato                          |
| 3.4    | MockMvcTester                                         | Avaliar migração de testes                        |
| 3.5    | Observability enhancements                            | Revisar Micrometer configs                        |

### 3.5 Comandos Úteis

```bash
# Dependency tree (detectar conflitos)
./mvnw dependency:tree -Dincludes=org.springframework

# Verificar versões gerenciadas pelo BOM
./mvnw help:effective-pom | grep -A2 "<artifactId>spring"

# Forçar resolução de dependências
./mvnw dependency:resolve -U

# Verificar properties disponíveis
./mvnw spring-boot:run --debug 2>&1 | grep "Auto-configuration"
```

---

## 4. Spring Cloud Upgrade

### 4.1 Release Train Matrix

| Spring Cloud   | Spring Boot | Codename     |
|---------------|-------------|--------------|
| 2022.0.x      | 3.0.x       | Kilburn      |
| 2023.0.x      | 3.2.x–3.3.x| Leyton       |
| 2024.0.x      | 3.4.x–3.5.x| Moorgate     |

### 4.2 Configuração do BOM

```xml
<properties>
    <spring-cloud.version>2024.0.x</spring-cloud.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 4.3 Componentes Principais

| Componente                 | Artifact                                    | Função                          |
|---------------------------|--------------------------------------------|---------------------------------|
| Config Server              | spring-cloud-config-server/client          | Configuração centralizada       |
| Service Discovery          | spring-cloud-starter-netflix-eureka-*      | Registro/descoberta de serviços |
| Load Balancer              | spring-cloud-starter-loadbalancer          | Client-side LB (substitui Ribbon) |
| Circuit Breaker            | spring-cloud-starter-circuitbreaker-resilience4j | Resilience patterns      |
| API Gateway                | spring-cloud-starter-gateway               | Gateway reativo (WebFlux)       |
| API Gateway MVC            | spring-cloud-starter-gateway-mvc           | Gateway servlet (Spring MVC)    |
| Distributed Tracing        | spring-cloud-starter-zipkin + micrometer-tracing | Tracing distribuído       |
| Stream (Kafka/RabbitMQ)    | spring-cloud-starter-stream-kafka          | Event-driven messaging          |
| OpenFeign                  | spring-cloud-starter-openfeign             | HTTP client declarativo         |

### 4.4 Checklist de Upgrade

- [ ] Verificar compatibilidade Boot ↔ Cloud no [compatibility matrix](https://spring.io/projects/spring-cloud)
- [ ] Atualizar `spring-cloud.version` no BOM
- [ ] Verificar se Ribbon/Hystrix/Zuul foram removidos (deprecated desde 2020.0)
- [ ] Migrar Hystrix → Resilience4j se ainda não migrado
- [ ] Migrar Zuul → Spring Cloud Gateway
- [ ] Verificar bootstrap.yml → application.yml (bootstrap context removido por padrão desde 2020.0)
- [ ] Se usa bootstrap: adicionar `spring-cloud-starter-bootstrap`
- [ ] Testar Service Discovery (Eureka health check mudou)
- [ ] Testar Config Server refresh (endpoint mudou em versões recentes)
- [ ] Verificar Feign interceptors (assinatura pode mudar)

### 4.5 Spring Cloud + BrekFood (Roadmap)

**Phase 1 (Monolith)**: Não precisa de Spring Cloud
**Phase 2 (Extração de serviços)**:
```
Adicionar:
- spring-cloud-starter-gateway-mvc (API Gateway)
- spring-cloud-starter-netflix-eureka-server (Discovery)
- spring-cloud-starter-netflix-eureka-client (nos serviços)
- spring-cloud-starter-circuitbreaker-resilience4j (resilience)
- spring-cloud-starter-openfeign (comunicação inter-serviço)
```
**Phase 3 (Event-Driven)**:
```
Adicionar:
- spring-cloud-starter-stream-kafka (messaging)
- spring-cloud-starter-zipkin (tracing)
```

---

## 5. Apache Kafka Upgrade

### 5.1 Versão Matrix

| spring-kafka | Kafka Broker | Kafka Client | Spring Boot |
|-------------|-------------|-------------|-------------|
| 3.1.x       | 3.6.x       | 3.6.x       | 3.2.x       |
| 3.2.x       | 3.7.x       | 3.7.x       | 3.3.x–3.4.x |
| 3.3.x       | 3.8.x       | 3.8.x       | 3.5.x       |

### 5.2 Dependência

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
<!-- Versão gerenciada pelo Spring Boot BOM — NÃO override manualmente -->

<!-- Para testes -->
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka-test</artifactId>
    <scope>test</scope>
</dependency>
```

### 5.3 Configuração Base (BrekFood)

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    consumer:
      group-id: brekfood-${spring.application.name}
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "com.br.wstech.brekfood.*"
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      retries: 3
    listener:
      ack-mode: MANUAL_IMMEDIATE
      concurrency: 3
```

### 5.4 Tópicos BrekFood

| Tópico                       | Produtor     | Consumidores               |
|-----------------------------|-------------|---------------------------|
| `brekfood.orders.created`    | Order        | Pricing, Restaurant        |
| `brekfood.orders.accepted`   | Restaurant   | Customer, Order            |
| `brekfood.orders.ready`      | Restaurant   | Delivery                   |
| `brekfood.delivery.assigned` | Delivery     | Customer, Driver, Order    |
| `brekfood.delivery.started`  | Delivery     | Customer, Order            |
| `brekfood.delivery.completed`| Delivery     | Payment, Earnings, Order   |
| `brekfood.payment.processed` | Payment      | Order, Earnings            |

### 5.5 Checklist de Upgrade Kafka

- [ ] Verificar compatibilidade spring-kafka ↔ Kafka broker
- [ ] Atualizar broker (Docker image): `confluentinc/cp-kafka:<version>` ou `apache/kafka:<version>`
- [ ] Verificar deprecated configs no broker (`server.properties`)
- [ ] Verificar deprecated configs no client (`ConsumerConfig`, `ProducerConfig`)
- [ ] Testar serialização/deserialização (JsonDeserializer muda entre versões)
- [ ] Verificar `listener.ack-mode` (comportamento pode mudar)
- [ ] Testar idempotência do producer (`enable.idempotence`)
- [ ] Testar transações se usadas (`transactional-id`)
- [ ] Rodar testes com `EmbeddedKafka` ou Testcontainers
- [ ] Verificar Kafka Streams se usado (API breaking changes frequentes)

### 5.6 Docker Compose Kafka (Dev)

```yaml
services:
  kafka:
    image: apache/kafka:3.8.0
    ports:
      - "9092:9092"
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@localhost:9093
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      CLUSTER_ID: "brekfood-dev-cluster-001"
```

### 5.7 Comandos de Diagnóstico

```bash
# Verificar versão do broker
kafka-broker-api-versions.sh --bootstrap-server localhost:9092

# Listar tópicos
kafka-topics.sh --bootstrap-server localhost:9092 --list

# Verificar consumer lag
kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group brekfood-order-service --describe

# Testar produção
kafka-console-producer.sh --bootstrap-server localhost:9092 \
  --topic brekfood.orders.created

# Testar consumo
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic brekfood.orders.created --from-beginning
```

---

## 6. Cloud Dependencies

### 6.1 AWS (SDK v2)

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>software.amazon.awssdk</groupId>
            <artifactId>bom</artifactId>
            <version>2.29.x</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<!-- Módulos conforme necessário -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>           <!-- Object storage -->
</dependency>
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>sqs</artifactId>           <!-- Message queue -->
</dependency>
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>sns</artifactId>           <!-- Notifications -->
</dependency>
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>secretsmanager</artifactId> <!-- Secrets -->
</dependency>
```

**Checklist AWS SDK Upgrade:**
- [ ] Atualizar BOM version
- [ ] Verificar breaking changes: [AWS SDK Changelog](https://github.com/aws/aws-sdk-java-v2/blob/master/CHANGELOG.md)
- [ ] Se migrando SDK v1 → v2: reescrever clients (APIs completamente diferentes)
- [ ] Verificar credenciais provider chain (comportamento muda entre versões)
- [ ] Testar com LocalStack para validação

### 6.2 Spring Cloud AWS

```xml
<dependency>
    <groupId>io.awspring.cloud</groupId>
    <artifactId>spring-cloud-aws-starter</artifactId>
</dependency>
<dependency>
    <groupId>io.awspring.cloud</groupId>
    <artifactId>spring-cloud-aws-starter-s3</artifactId>
</dependency>
<dependency>
    <groupId>io.awspring.cloud</groupId>
    <artifactId>spring-cloud-aws-starter-sqs</artifactId>
</dependency>
```

**BOM:**
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.awspring.cloud</groupId>
            <artifactId>spring-cloud-aws-dependencies</artifactId>
            <version>3.2.x</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 6.3 GCP (Spring Cloud GCP)

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.google.cloud</groupId>
            <artifactId>spring-cloud-gcp-dependencies</artifactId>
            <version>5.x.x</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>spring-cloud-gcp-starter</artifactId>
</dependency>
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>spring-cloud-gcp-starter-storage</artifactId>
</dependency>
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>spring-cloud-gcp-starter-pubsub</artifactId>
</dependency>
```

### 6.4 Azure (Spring Cloud Azure)

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.azure.spring</groupId>
            <artifactId>spring-cloud-azure-dependencies</artifactId>
            <version>5.x.x</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependency>
    <groupId>com.azure.spring</groupId>
    <artifactId>spring-cloud-azure-starter</artifactId>
</dependency>
```

### 6.5 Checklist Geral Cloud Dependencies

- [ ] Usar SEMPRE o BOM do provedor — nunca fixar versões individuais
- [ ] Verificar compatibilidade BOM do cloud provider ↔ Spring Boot version
- [ ] Testar com emuladores locais (LocalStack, GCP emulators, Azurite)
- [ ] Verificar IAM/credenciais (cada major version muda o provider chain)
- [ ] Revisar timeouts e retry policies (defaults mudam entre majors)
- [ ] Verificar se `auto-configuration` classes foram renomeadas/movidas

---

## 7. Utilitários & Plugins

### 7.1 Lombok

| Lombok   | Java 17 | Java 21 | Java 23 |
|----------|---------|---------|---------|
| 1.18.30+ | ✅       | ✅       | ❌       |
| 1.18.34+ | ✅       | ✅       | ✅       |

**Atenção**: Lombok depende de internals do `javac`. SEMPRE verifique compatibilidade antes de atualizar Java.

### 7.2 MapStruct

```xml
<properties>
    <mapstruct.version>1.6.x</mapstruct.version>
</properties>

<!-- No annotation processor do maven-compiler-plugin -->
<annotationProcessorPaths>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </path>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok-mapstruct-binding</artifactId>
        <version>0.2.0</version>
    </path>
    <path>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
        <version>${mapstruct.version}</version>
    </path>
</annotationProcessorPaths>
```

> **ORDEM IMPORTA**: Lombok ANTES de MapStruct no annotation processor path.

### 7.3 JaCoCo

- Verificar compatibilidade com versão Java (JaCoCo 0.8.12+ para Java 21)
- Atualizar `<version>` no plugin

### 7.4 Maven Plugins

```bash
# Verificar plugins desatualizados
./mvnw versions:display-plugin-updates

# Verificar dependências desatualizadas
./mvnw versions:display-dependency-updates

# Atualizar parent
./mvnw versions:update-parent
```

---

## 8. Procedimento Completo de Upgrade (Runbook)

### Pré-Upgrade
```
1. [ ] Criar branch: git checkout -b upgrade/stack-YYYY-MM
2. [ ] Tag de segurança: git tag pre-upgrade-YYYY-MM
3. [ ] Rodar build completo: ./mvnw clean verify
4. [ ] Salvar dependency tree: ./mvnw dependency:tree > deps-before.txt
5. [ ] Anotar cobertura atual do JaCoCo
```

### Execução
```
6.  [ ] Atualizar Java version (pom.xml + Dockerfile + CI)
7.  [ ] Build + test
8.  [ ] Atualizar Spring Boot parent
9.  [ ] Build + test
10. [ ] Atualizar Spring Cloud BOM (se usado)
11. [ ] Build + test
12. [ ] Atualizar Kafka (spring-kafka segue o Boot, broker separado)
13. [ ] Build + test
14. [ ] Atualizar Cloud provider BOMs
15. [ ] Build + test
16. [ ] Atualizar utilitários (Lombok, MapStruct, etc.)
17. [ ] Build + test
18. [ ] Atualizar plugins (JaCoCo, maven-compiler, etc.)
19. [ ] Build + test final
```

### Pós-Upgrade
```
20. [ ] Comparar dependency tree: ./mvnw dependency:tree > deps-after.txt && diff deps-before.txt deps-after.txt
21. [ ] Verificar tamanho do JAR (regressão de tamanho indica dependência duplicada)
22. [ ] Smoke test em ambiente de dev/staging
23. [ ] Performance baseline comparison
24. [ ] Merge PR + tag: post-upgrade-YYYY-MM
25. [ ] Atualizar memory.md com novas versões
```

---

## 9. Troubleshooting de Upgrade

### Problema: `NoSuchMethodError` / `NoClassDefFoundError`
```bash
# Encontrar de onde vem a classe
./mvnw dependency:tree -Dincludes=<groupId>:<artifactId>
# Forçar versão correta com <dependencyManagement> ou <exclusion>
```

### Problema: `BeanCreationException` após upgrade
```bash
# Rodar com debug para ver auto-configuration
./mvnw spring-boot:run -Ddebug
# Procurar "CONDITIONS EVALUATION REPORT" nos logs
```

### Problema: Testes falhando após upgrade
```bash
# Verificar se mocks estão compatíveis
./mvnw dependency:tree -Dincludes=org.mockito
./mvnw dependency:tree -Dincludes=net.bytebuddy
# ByteBuddy é sensível a versão de Java
```

### Problema: Lombok não compila
```bash
# Verificar versão do Lombok vs Java
# Limpar caches do IDE
# Verificar annotationProcessorPaths no maven-compiler-plugin
```

### Problema: Spring Security mudou comportamento
```
# Desde Spring Security 6.x:
# - requestMatchers() substitui antMatchers()
# - authorizeHttpRequests() substitui authorizeRequests()
# - SecurityFilterChain bean substitui WebSecurityConfigurerAdapter
```

---

## 10. Referências

| Recurso                                | URL                                                         |
|----------------------------------------|-------------------------------------------------------------|
| Spring Boot Release Notes              | https://github.com/spring-projects/spring-boot/wiki         |
| Spring Cloud Compatibility             | https://spring.io/projects/spring-cloud                     |
| Spring Kafka Compatibility             | https://spring.io/projects/spring-kafka                     |
| Java Almanac (API diff entre versões)  | https://javaalmanac.io/                                     |
| AWS SDK Java v2 Changelog              | https://github.com/aws/aws-sdk-java-v2/blob/master/CHANGELOG.md |
| Lombok Changelog                       | https://projectlombok.org/changelog                         |
| Maven Versions Plugin                  | https://www.mojohaus.org/versions/versions-maven-plugin/    |

