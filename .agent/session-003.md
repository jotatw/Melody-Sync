# Melody Sync — Agent Session Log

## Session 3 — 2026-07-31

### Contexto

**Início:** Core (55 testes) + CLI (4 testes) funcionando. README reescrito.
**Objetivo:** Milestone 3 — Database SQLite, integrar scanner com banco, atualizar documentação.

### O que foi feito

1. **Banco de dados (SQLite via Exposed):**
   - `database/SongsTable.kt` — tabela `songs` com 11 colunas (id autoincrement, path unique, metadados, info técnica)
   - `database/MusicDatabase.kt` — conexão SQLite + criação do schema
   - `database/MusicRepository.kt` — insert, insertAll, findAll, findByPath, exists, updateByPath, deleteByPath, deleteAll, count
   - 8 testes

2. **Integração scanner + banco:**
   - `service/LibrarySyncService.kt` — `syncDirectory()` e `syncSongs()` que escaneiam, inserem novos, atualizam existentes, removem os que sumiram
   - Adicionado `updateByPath`, `exists`, `deleteAll` ao repositório
   - 6 testes

3. **Documentação atualizada:**
   - README.md — Milestone 3 completo, 73 testes, nova seção database/sync
   - docs/INDEX.md — status v0.3.0-dev, Milestone 3 completo
   - ADR-0004 — versão final Exposed 0.61.0 + notas de implementação

### Decisões técnicas

- **Exposed 0.61.0 em vez de 1.3.1:** a 1.x reorganizou pacotes (`org.jetbrains.exposed.v1.*`) com documentação escassa. A 0.61 mantém a API clássica (`org.jetbrains.exposed.sql.*`) estável e bem documentada.
- **SQLite `:memory:` não funciona com Exposed:** cada conexão cria um banco separado; a tabela criada numa transação não é visível em outra. Testes usam banco em arquivo temporário.
- **Sincronização idempotente:** `syncDirectory` pode rodar múltiplas vezes sem duplicar dados (upsert por path).

### Testes

| Área | Testes |
|------|--------|
| Core (model + scanner) | 55 |
| Database (MusicRepository) | 8 |
| Service (LibrarySyncService) | 6 |
| CLI | 4 |
| **Total** | **73** |

### Pendências

- Integrar database ao CLI `scan` (usar sync em vez de escanear direto)
- YouTube API integration
- Library health check
- GUI Compose Desktop
- Remover código Python de referência (opcional)