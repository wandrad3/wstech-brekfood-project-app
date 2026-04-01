# Skill: CI Pipeline — Build, Sonar & Auto-PR

> Tipo: DevOps / CI Skill
> Escopo: GitHub Actions pipeline para build, análise de qualidade (SonarCloud) e abertura automática de PR
> Última revisão: 2026-04-01

---

## 1. Visão Geral

### 1.1 O que a Pipeline Faz

| Job           | Trigger                          | Ação                                                          |
|---------------|----------------------------------|---------------------------------------------------------------|
| **build**     | Push ou PR em qualquer branch    | Compila, roda testes, valida JaCoCo coverage gate             |
| **sonar**     | Após build OK (não forks)        | Análise SonarCloud: cobertura, bugs, code smells, CVEs        |
| **create-pr** | Push em feature/hotfix/bugfix    | Cria ou atualiza PR automaticamente para `develop`            |

### 1.2 Fluxo Visual

```
  developer pushes to feature/phase-1.2-auth-service
                    │
                    ▼
    ┌──────────────────────────────┐
    │  1. BUILD & TEST             │
    │  • ./mvnw clean verify       │
    │  • JaCoCo gate: 70/60/80%    │
    │  • Upload JaCoCo report      │
    └──────────────┬───────────────┘
                   │ ✅
                   ▼
    ┌──────────────────────────────┐
    │  2. SONARCLOUD ANALYSIS      │
    │  • Coverage report upload    │
    │  • Bug detection             │
    │  • Vulnerability scan (CVE)  │
    │  • Code smell detection      │
    │  • Quality Gate wait         │
    └──────────────┬───────────────┘
                   │ ✅
                   ▼
    ┌──────────────────────────────┐
    │  3. AUTO PR → develop        │
    │  • Creates/updates PR        │
    │  • Labels: ci/auto-pr        │
    │  • Body: CI summary          │
    └──────────────────────────────┘
```

---

## 2. Arquivos da Pipeline

| Arquivo                          | Função                                                |
|----------------------------------|-------------------------------------------------------|
| `.github/workflows/ci.yml`       | Pipeline principal (3 jobs)                           |
| `sonar-project.properties`       | Configuração do SonarCloud (sources, exclusions, etc) |

---

## 3. Configuração Necessária

### 3.1 Secrets (Settings → Secrets → Actions)

| Secret          | Onde obter                                | Obrigatório |
|-----------------|-------------------------------------------|-------------|
| `SONAR_TOKEN`   | https://sonarcloud.io → My Account → Security → Tokens | ✅ Sim |
| `GITHUB_TOKEN`  | Automático (gerado pelo GitHub Actions)   | ✅ Automático |

### 3.2 Variables (Settings → Variables → Actions)

| Variable              | Exemplo                         | Descrição                          |
|-----------------------|---------------------------------|------------------------------------|
| `SONAR_PROJECT_KEY`   | `wstech_brekfood-app`           | Project key no SonarCloud          |
| `SONAR_ORGANIZATION`  | `wstech`                        | Organization key no SonarCloud     |

### 3.3 SonarCloud Setup (primeira vez)

1. Acesse https://sonarcloud.io
2. Clique em **+** → **Analyze new project**
3. Selecione o repositório GitHub
4. Copie `Project Key` e `Organization` → configure nas Variables do GitHub
5. Gere um token em **My Account → Security** → configure como Secret `SONAR_TOKEN`
6. Defina o método de análise como **GitHub Actions** (não auto-scan)

### 3.4 Quality Gate Recomendado (SonarCloud)

Criar um Quality Gate customizado "BrekFood" com:

| Métrica                    | Condição     | Valor |
|----------------------------|-------------|-------|
| Coverage on new code        | ≥            | 70%   |
| Duplicated lines on new code| ≤            | 3%    |
| Maintainability rating      | ≤            | A     |
| Reliability rating          | ≤            | A     |
| Security rating             | ≤            | A     |
| Security hotspots reviewed  | =            | 100%  |

---

## 4. Branching Model

### 4.1 Branches Protegidas

| Branch    | Proteção                                      |
|-----------|-----------------------------------------------|
| `main`    | Requer PR + 1 aprovação + CI verde + Sonar OK |
| `develop` | Requer PR + CI verde + Sonar OK               |

### 4.2 Branches de Trabalho

| Padrão          | Uso                              | Exemplo                           |
|-----------------|----------------------------------|-----------------------------------|
| `feature/**`    | Novas funcionalidades            | `feature/phase-1.2-auth-service`  |
| `hotfix/**`     | Fix urgente em produção          | `hotfix/fix-jwt-expiration`       |
| `bugfix/**`     | Fix de bug em develop            | `bugfix/user-email-normalize`     |
| `chore/**`      | Manutenção (deps, CI, docs)      | `chore/upgrade-spring-boot-3.5.6` |
| `refactor/**`   | Refatoração sem mudar comportamento | `refactor/order-state-machine`  |

### 4.3 Fluxo Git

```
feature/phase-1.2-auth-service
         │
         │ push
         ▼
    CI Pipeline (build → sonar → auto-PR)
         │
         ▼
    PR → develop (review + merge)
         │
         ▼
    develop (acumula features)
         │
         │ release
         ▼
    PR → main (release tag)
```

---

## 5. SonarCloud — O Que é Analisado

### 5.1 Coverage (Cobertura de Código)

- **Plugin:** JaCoCo 0.8.12
- **Relatório:** `target/site/jacoco/jacoco.xml`
- **Gate local (JaCoCo):** LINE ≥ 70%, BRANCH ≥ 60%, CLASS ≥ 80%
- **Gate Sonar:** Coverage on new code ≥ 70%
- **Exclusões:** Mesmo do `pom.xml` (skeleton packages, config classes)

### 5.2 Vulnerabilities (CVEs)

SonarCloud detecta automaticamente:

| Tipo                   | Exemplo                                  |
|------------------------|------------------------------------------|
| Known CVEs             | Dependência com vulnerabilidade conhecida |
| SQL Injection          | String concatenation em queries          |
| XSS                    | Dados não sanitizados em responses       |
| Path Traversal         | File access com input do usuário         |
| Insecure Deserialization | ObjectInputStream sem whitelist        |
| Weak Cryptography      | MD5/SHA1 para password hashing           |
| Hardcoded Credentials  | Senhas/tokens no código fonte            |

### 5.3 Code Smells & Bugs

- **Bugs:** NullPointerException potenciais, resource leaks, logic errors
- **Code Smells:** Complexidade ciclomática, métodos longos, nomes ruins
- **Duplicações:** Blocos de código duplicados (threshold: 3%)

---

## 6. Troubleshooting

### 6.1 Sonar Token Inválido

```
ERROR: Not authorized. Analyzing this project requires authentication.
```

**Solução:** Regenerar o token no SonarCloud e atualizar o secret `SONAR_TOKEN`.

### 6.2 Coverage Não Aparece no SonarCloud

```
WARN: No coverage report found
```

**Checklist:**
1. Verificar se `target/site/jacoco/jacoco.xml` existe (rodar `mvn verify` primeiro)
2. Verificar `sonar.coverage.jacoco.xmlReportPaths` no `sonar-project.properties`
3. Verificar se o build job gera o relatório antes do sonar job

### 6.3 Quality Gate Falha

```
ERROR: Quality Gate status: FAILED
```

**Ações:**
1. Acessar dashboard do SonarCloud para ver métricas que falharam
2. Se coverage insuficiente → escrever mais testes
3. Se vulnerabilidade → fix a issue antes de merge
4. Se code smell → refatorar

### 6.4 PR Não é Criado Automaticamente

**Checklist:**
1. Verificar se o branch segue o padrão `feature/**`, `hotfix/**` ou `bugfix/**`
2. Verificar permissions do `GITHUB_TOKEN` (precisa de `pull-requests: write`)
3. Verificar se já existe um PR aberto para o mesmo branch → será atualizado, não duplicado

### 6.5 Pipeline Roda Duas Vezes (push + PR)

Isso é esperado quando um push em feature branch cria um PR. A `concurrency` key cancela a run anterior automaticamente.

---

## 7. Evolução Planejada

### 7.1 Phase 2 (Deploy)

Quando houver estratégia de deploy:

```yaml
# Adicionar job de deploy após sonar
deploy-staging:
  needs: [build, sonar]
  if: github.ref == 'refs/heads/develop'
  # Deploy to staging environment

deploy-production:
  needs: [build, sonar]
  if: github.ref == 'refs/heads/main'
  # Deploy to production
```

### 7.2 Phase 3 (Avançado)

| Feature                | Quando adicionar                    |
|------------------------|-------------------------------------|
| Docker image build     | Quando tiver container registry     |
| Integration tests      | Quando Testcontainers no CI         |
| Dependency review       | Quando GitHub Advanced Security    |
| OWASP Dependency Check | Quando security compliance exigir   |
| Semantic versioning    | Quando tiver releases automáticos   |
| Slack/Teams notify     | Quando equipe crescer               |

---

## 8. Comandos Úteis (Local)

```bash
# Rodar a mesma verificação que a pipeline faz
./mvnw clean verify -Dspring.profiles.active=test -B

# Rodar análise Sonar local (requer token)
./mvnw clean verify sonar:sonar \
  -Dsonar.projectKey=wstech_brekfood-app \
  -Dsonar.organization=wstech \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.token=YOUR_TOKEN \
  -Dspring.profiles.active=test

# Ver relatório JaCoCo local
open target/site/jacoco/index.html

# Ver vulnerabilidades de dependência (OWASP, opcional)
./mvnw org.owasp:dependency-check-maven:check
```

---

## 9. Referências

| Recurso                               | URL                                                    |
|---------------------------------------|--------------------------------------------------------|
| SonarCloud                            | https://sonarcloud.io                                  |
| SonarCloud Maven Plugin               | https://docs.sonarsource.com/sonarcloud/advanced-setup/ci-based-analysis/sonarscanner-for-maven/ |
| GitHub Actions Docs                   | https://docs.github.com/en/actions                     |
| JaCoCo Maven Plugin                   | https://www.jacoco.org/jacoco/trunk/doc/maven.html     |
| peter-evans/create-pull-request       | https://github.com/peter-evans/create-pull-request     |
| SonarCloud Quality Gates              | https://docs.sonarsource.com/sonarcloud/standards/quality-gates/ |

