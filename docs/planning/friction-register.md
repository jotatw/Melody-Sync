# Friction Register

> Living record of real-use frictions. Frictions are registered and classified — not automatically implemented. Only critical bugs, security issues, and regressions are fixed immediately.

## Document Information

| Item             | Value |
|------------------|-------|
| Document ID      | FRICTION-001 |
| Category         | Planning / Real Use |
| Audience         | Maintainer |
| Status           | Active |
| Project Version  | v0.13.0-dev |
| Last Updated     | 2026-08-22 |

---

## Purpose

Collect real friction from daily use of Melody Sync without turning every observation into an immediate implementation task. When enough evidence accumulates, the register feeds the expansion decision.

## Rules

- Register the friction; do not act on it immediately.
- Only critical bugs, security issues, and regressions bypass this rule.
- Review periodically (e.g. every 2 weeks of use) to group and prioritize.

---

## Registered Frictions

### F1 — Health does not surface album-only gaps or filename-fallback titles

- **Problema:** `hasMetadata` requires only title and artist. A file with real title + artist but missing album, or a file whose title is only the filename fallback but has an artist tag, is not flagged by Health.
- **Contexto:** Quick Fix's `diagnose` already tracks `MissingField.ALBUM`; Health's aggregate lags behind it.
- **Frequência:** every untagged or partially-tagged file.
- **Impacto:** baixo — o usuário ainda vê os itens no Review.
- **Reprodução:** conhecida (scan um arquivo sem album).
- **Possível solução:** alinhar Health aggregate com `QuickFixService.diagnose` (title/artist/album).
- **Status:** observado — aguardar mais uso real.

---

### F2 — Matcher local não resolve 4 padrões sistemáticos

- **Problema:** o `SongMatcher` não resolve: underscores-as-spaces (`JoJo_s Bizarre Adventure…`), `[ORIGINAL] X - Y` inversions, `"… by X"` phrasing, e nomes sem separador.
- **Contexto:** cobertura medida em 9/13 na biblioteca real; os 4 misses são sistemáticos, não one-offs.
- **Frequência:** ~31% dos MP3 sem tags da biblioteca.
- **Impacto:** médio — exige identificação manual ou YouTube.
- **Reprodução:** conhecida (ver `SongMatcherRealNamesTest`).
- **Possível solução:** Metadata Enrichment (YouTube identification) ou melhorias direcionadas no matcher (underscore disambiguation é o mais complexo).
- **Status:** observado — aguardar mais uso real para confirmar que é o gargalo principal.

---

### F3 — "No duplicates" empty state não probeado via CLI em biblioteca distinta

- **Problema:** o empty state de "no duplicates" não foi diretamente validado via CLI numa biblioteca com músicas distintas (os fixtures compartilham tags por design).
- **Contexto:** o UI empty state existe e está coberto por testes.
- **Frequência:** única (durante a validação).
- **Impacto:** muito baixo.
- **Reprodução:** trivial (biblioteca sem duplicatas).
- **Possível solução:** verificar wording via CLI se necessário.
- **Status:** observado — baixa prioridade.

---

### F4 — SongList filtered-empty message cita query mesmo quando o filtro é artist/album/format

- **Problema:** quando a lista fica vazia após filtrar, a mensagem sempre cita `"${state.query}"` mesmo se a causa foi um filtro de artist/album/format.
- **Contexto:** `SongList.kt` linha ~86.
- **Frequência:** sempre que um filtro não-query limpa a lista.
- **Impacto:** baixo — confusão momentânea.
- **Reprodução:** filtrar por artist inexistente.
- **Possível solução:** mensagem genérica ("No songs match the current filters") ou nomear o filtro ativo.
- **Status:** observado — baixa prioridade.

---

### F5 — DesignSystem §5.1 descreve apply instantâneo (implementação tem review dialog)

- **Problema:** o DesignSystem §5.1 (Quick-Fix HUD) descreve apply instantâneo + LED toast + thumbnail/duration no YouTube card. A implementação real insere um review dialog editável (melhor) e não mostra thumbnail.
- **Contexto:** o §5.1 foi escrito antes do review dialog ser implementado.
- **Frequência:** documentação apenas.
- **Impacto:** baixo — não afeta o usuário.
- **Reprodução:** ler §5.1 vs `QuickFixPanel.kt`.
- **Possível solução:** atualizar §5.1 para descrever o review dialog flow.
- **Status:** observado — atualizar documentação quando conveniente.

---

## Template (preencher durante o uso real)

Copie o bloco abaixo para cada nova fricção observada.

```text
### F[n] — [título curto]

- **Problema:**
- **Contexto:**
- **Frequência:** (sempre / às vezes / raro / desconhecido)
- **Impacto:** (bloqueante / irritante / cosmético)
- **Reprodução:** (passos para reproduzir)
- **Possível solução:**
- **Status:** (observado / priorizado / em correção / resolvido)
```

---

## Priorização periódica

A cada ~2 semanas de uso, revisar este registro e:

1. Agrupar fricções relacionadas.
2. Priorizar por impacto × frequência.
3. Decidir: correção imediata, próximo implementation block, ou aguardar mais evidência.

---

## Related Documents

- [Real-Use Checklist](real-use-checklist.md)
- [Product Validation Report](product-validation-report.md)
- [Product Hardening Report](product-hardening-report.md)
- [Filename Conventions](metadata-filename-conventions.md)
- [Product Roadmap](../project/product-roadmap.md)

---

This document follows the Melody Sync Documentation Standard.

**End of Document**