# Melody Sync — Agent Session Log

## Session 4 — 2026-07-31

### Contexto

**Início:** Core + CLI + Database funcionando (74 testes). Commit da migração Kotlin feito.
**Objetivo:** Construir a GUI Desktop funcional (Compose Desktop) antes da integração YouTube, conforme decisão do usuário.
**Segunda parte:** Redesign da interface após feedback do usuário (tema não aplicou, interface limitada).

### Decisões do usuário

| Item | Decisão |
|------|---------|
| Plataforma-alvo | Linux Fedora 44 KDE Plasma (Wayland) |
| Ordem de prioridade | GUI funcional primeiro; YouTube depois |
| Seleção de pasta | Campo de texto + JFileChooser (testado OK no KDE) |
| Arquitetura da GUI | Simples (state holder próprio); evoluir depois |
| Escopo | Só scan + lista + busca + stats; organização/edição depois |
| Persistência | Sempre sincronizar com SQLite (banco = fonte de verdade) |
| Atualizações futuras | File watcher + rescan periódico (deixado para depois) |
| Navegação | Simples, sem sidebar agora |
| Colunas da lista | Título, Artista, Álbum, Duração (mais opções no futuro) |
| Stats | Números compactos no topo (detalhes no futuro) |
| Temas | Detecção automática + toggle manual; extensível para mais temas |

### O que foi feito

1. **`melody-sync-desktop` funcional:**
   - `desktop/Main.kt` — janela Material 3 (1100x700), tema escuro segue KDE (`~/.config/kdeglobals`) ou GNOME fallback
   - `state/AppState.kt` — state holder testável: diretório, status de scan, progresso, erro, músicas, stats, busca
   - `ui/LibraryScreen.kt` — seletor de diretório (campo de texto + JFileChooser), botão Scan com progresso, cards de estatísticas, campo de busca, LazyColumn de músicas

2. **Dependências:** `compose.material3`, `compose.foundation`, `compose.materialIconsExtended`; repositório `google()` adicionado (androidx)

3. **Build:** `./gradlew :melody-sync-desktop:run` abre a janela no KDE (validado)

### Problemas resolvidos

- **Tema escuro não detectava o Sweet theme:** agora usa luminância do `BackgroundNormal` no `kdeglobals` (limiar 128), independe do nome do tema
- **fillMaxSize não resolveu:** extension function precisa de import explícito (`androidx.compose.foundation.layout.fillMaxSize`)
- **AWT FileDialog não suporta diretório:** trocado por JFileChooser (Swing) com DIRECTORIES_ONLY
- **Clash de setters:** mutableStateOf gera setX() que colide com funções explícitas; renomeado para updateDirectory/updateQuery
- **GUI fechava após scan:** causado pelo shell matando o processo em background (SIGHUP); usar nohup resolve
- **androidx ausente:** adicionado repositório google()
- **config cache:** compilar com --no-configuration-cache

### Testes

| Módulo | Testes |
|--------|--------|
| Core | 69 |
| CLI | 5 |
| Desktop (tema) | 6 |
| **Total** | **80** |

### Pendências

- Ordenação na lista (clicar no cabeçalho)
- Empacotamento RPM
- Milestone 5: YouTube API, health check, file watcher

### Documentação

- README.md — v0.4.0-dev, Milestone 4 (GUI) completo, Quick Start com GUI
- docs/INDEX.md — status atualizado

### Pendências

- Fase 4: ordenação, empacotamento RPM (target já adicionado), testes do AppState
- Testar JFileChooser no Wayland (possível limitação)
- Milestone 5: YouTube API, health check, file watcher
- Remover código Python de referência (opcional)