# Melody Sync

<p align="center">
  <img alt="Licença" src="https://img.shields.io/github/license/jotatw/Melody-Sync">
  <img alt="CI" src="https://img.shields.io/github/actions/workflow/status/jotatw/Melody-Sync/ci.yml?branch=main">
  <img alt="Linguagem" src="https://img.shields.io/github/languages/top/jotatw/Melody-Sync">
  <img alt="Versão" src="https://img.shields.io/github/v/release/jotatw/Melody-Sync?include_prereleases&label=latest">
</p>

> Uma workstation desktop para curadoria de biblioteca musical pessoal — analise, corrija e mantenha organizada.

O Melody Sync é uma ferramenta pessoal, open-source, construída em torno de **arquivos locais** e **decisões explícitas**. Ele reporta problemas primeiro, sugere o que fazer, e só altera metadados ou arquivos quando você aprova.

**Reporta primeiro, aplica depois.** Nada é modificado silenciosamente: análises produzem planos e sugestões, e cada alteração em metadados ou arquivos é uma ação deliberada que você confirma.

---

## Sobre

O Melody Sync ajuda a trazer ordem a uma biblioteca musical pessoal. Ele escaneia seus arquivos, constrói um banco de dados pesquisável, detecta problemas reais (tags faltando, arquivos com duração zero, entradas órfãs, duplicatas) e oferece um fluxo de trabalho estruturado para corrigir e organizar — tudo com sua aprovação explícita a cada passo.

**Não é** um produto comercial, nem concorrente do Spotify, nem um serviço em nuvem. Seus arquivos ficam na sua máquina; seus dados ficam em um banco SQLite local.

---

## Objetivo

Criei o Melody Sync porque minha própria biblioteca era uma bagunça: `.mp3`, `.flac`, `.mp4`, `.png`, `.vtt`, `.txt` tudo misturado. Ferramentas existentes (MusicBrainz Picard, Kid3, LRCGET, Strawberry) resolvem cada uma uma peça do quebra-cabeça, mas nenhuma oferece um **fluxo de trabalho coeso de ponta a ponta** para limpar e organizar uma biblioteca do início ao fim.

O projeto também serve como campo de prática para engenharia de software: arquitetura antes da implementação, documentação como código, decisões guiadas por ADRs e evolução incremental.

---

## Funcionalidades

- **Biblioteca** — Navegue, busque, filtre, ordene e inspecione sua coleção.
- **Saúde** — Analise a biblioteca em busca de problemas reais: metadados ausentes, arquivos com duração zero, entradas órfãs no banco.
- **Metadados & Correção Rápida** — Revise problemas e aplique correções de tags sugeridas com sua aprovação.
- **Estatísticas** — Explore como a coleção é composta (formatos, artistas, álbuns, duração, tamanho).
- **Duplicatas** — Detecte grupos prováveis de duplicatas e revise antes de mover para lixeira.
- **Organizar** — Planeje uma estrutura de pastas Artista/Álbum como *dry-run*, depois aplique explicitamente.
- **CLI** — As mesmas capacidades para scripts e uso direto (`melody-sync scan`, `melody-sync fix`, etc.).
- **Atualizações** — Verifique e instale releases; atualização automática opcional na inicialização.

---

## O Fluxo de Trabalho

```text
Biblioteca Nova / Existente
            │
            ▼
         Revisão
            │
      ┌─────┴─────┐
      ▼           ▼
    Corrigir    Validar
      │           │
      └─────┬─────┘
            ▼
         Organizar
            │
            ▼
        Biblioteca
```

---

## Tecnologias

| Área | Stack |
|------|-------|
| **Linguagem** | Kotlin 2.4 (JVM 21) |
| **UI Desktop** | Compose Desktop 1.11 (Material 3) |
| **Framework CLI** | clikt 5.1 |
| **Banco de Dados** | SQLite via Exposed 0.61 (DAO + JDBC) + HikariCP |
| **Metadados de Áudio** | JAudioTagger 2.2.7 |
| **Logging** | SLF4J 2.0 + Logback 1.6 |
| **Concorrência** | Kotlinx Coroutines 1.10 |
| **Build** | Gradle 8 (Kotlin DSL + Version Catalog) |
| **Testes** | JUnit 5.12, Kotlinx Coroutines Test |
| **Cores no Terminal** | Mordant 3.0 |

---

## Estrutura do Projeto

```
melody-sync/
├── melody-sync-core/      # Modelo de domínio, scanner, banco, saúde, correção, organização
├── melody-sync-desktop/   # Aplicação Compose Desktop (UI, telas, navegação)
├── melody-sync-cli/       # Pontos de entrada CLI (scan, fix, organize, stats, etc.)
├── docs/                  # Documentação técnica (INDEX.md, ADRs, handbook, testes)
├── scripts/               # Scripts de instalação e utilitários
├── .github/               # Workflows de CI/CD
├── gradle/                # Gradle wrapper
├── gradle/libs.versions.toml  # Catálogo de versões
├── settings.gradle.kts    # Configuração multi-módulo
└── README.md              # Este arquivo
```

**Módulos:**
- `melody-sync-core` — Kotlin puro, sem dependências de UI. Contém modelos de domínio, scanner de arquivos, repositório SQLite (Exposed), análise de saúde, engine de correção de metadados, planejador de organização, e serviços compatíveis com CLI.
- `melody-sync-desktop` — App Compose Desktop que depende do `core`. Implementa telas, navegação, temas (Material 3, dark-first), e responsividade a tamanhos de janela.
- `melody-sync-cli` — Camada CLI fina usando clikt, delega para serviços do `core`.

---

## Instalação

**Requisitos:** JDK 21+. Linux é a plataforma principal testada (Fedora, baseadas em Debian).

```bash
git clone https://github.com/jotatw/Melody-Sync.git
cd Melody-Sync

./gradlew build                        # compila + roda suíte completa de testes

./gradlew :melody-sync-desktop:run     # roda a aplicação desktop
./gradlew :melody-sync-cli:run --args="scan /caminho/para/musica"   # roda o CLI
```

**Instalador Fedora/Linux (cria .desktop, adiciona ao menu):**

```bash
./scripts/install.sh
```

---

## Documentação

A documentação detalhada vive em [`docs/INDEX.md`](docs/INDEX.md), que mapeia todas as áreas:

| Documento | Finalidade |
|-----------|------------|
| [`docs/INDEX.md`](docs/INDEX.md) | Hub de documentação e primeiros passos |
| [`docs/ROADMAP.md`](docs/ROADMAP.md) | Planejamento atual, prioridades e limites de escopo |
| [`docs/architecture/technology.md`](docs/architecture/technology.md) | Stack técnica, módulos, status de validação |
| [`docs/architecture/`](docs/architecture/) | Arquitetura, serviços centrais e decisões (ADRs) |
| [`docs/design/`](docs/design/) | UX, telas, navegação e sistema de design visual |
| [`docs/planning/`](docs/planning/) | Planos detalhados para mudanças maiores |
| [`docs/project/History.md`](docs/project/History.md) | Histórico do projeto e marcos |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | Diretrizes de contribuição |

---

## Status

**v0.13.0-dev** — Fase de refinamento estável. O fluxo principal está consolidado; o foco é confiabilidade e polimento de UX em vez de novas áreas de funcionalidade. Escrita de metadados verificada por re-leitura, a interface adapta-se a janelas compactas/médias/expandidas, e o feedback de status é semântico e consistente.

Veja [`docs/INDEX.md#-current-status`](docs/INDEX.md#-current-status) para a tabela de status ao vivo, [`docs/project/History.md`](docs/project/History.md) para marcos, e [`docs/project/SprintBoard.md`](docs/project/SprintBoard.md) para prioridades atuais.

---

## Princípios

- **Comece pequeno** — Prefira uma implementação pequena e coerente a expansão prematura.
- **Reporte primeiro** — Operações que podem alterar arquivos ou metadados apresentam o resultado antes de aplicar mudanças.
- **Aprovação explícita** — Nunca modifique metadados ou reorganize a biblioteca sem decisão do usuário.
- **Provedores externos são substituíveis** — YouTube e letras são fontes opcionais de apoio, nunca a base.
- **Desktop-first** — O fluxo principal é um ambiente desktop onde a curadoria é prática.
- **Documentação como código** — ADRs, diários de sprint e handbook vivem junto ao código que descrevem.

---

## Licença

O Melody Sync é lançado sob a [Licença MIT](LICENSE).

---

> **English version:** [README.md](README.md)