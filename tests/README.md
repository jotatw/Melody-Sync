# Melody Sync - Testes

# Testes no Melody Sync

Os testes garantem que o comportamento do Melody Sync permaneça correto conforme o projeto evolui.

Mais do que validar código, eles permitem refatorar módulos com segurança, detectar regressões e documentar o comportamento esperado de cada componente do sistema.

---

# Nossa filosofia

No Melody Sync, um módulo **não é considerado concluído apenas porque funciona**.

Um módulo somente é considerado finalizado quando passa pelas seguintes etapas:

```
Planejamento
    ↓
Implementação
    ↓
Revisão
    ↓
Testes
    ↓
Documentação
    ↓
Commit
```

Essa metodologia garante que cada Sprint produza software confiável e de fácil manutenção.

---

# O que testamos

Os testes validam exclusivamente o comportamento do código desenvolvido no Melody Sync.

Exemplos:

- Scanner
- Discovery
- Metadata
- Statistics
- Models
- UI
- CLI

O objetivo é garantir que as regras de negócio permaneçam corretas.

---

# O que NÃO testamos

Não escrevemos testes para bibliotecas externas ou funcionalidades já garantidas pelo Python.

Exemplos:

- pathlib
- Counter
- sum()
- sorted()
- Mutagen
- Typer
- Rich

Esses componentes já possuem seus próprios testes.

Nós apenas verificamos se o Melody Sync utiliza essas bibliotecas corretamente.

---

# Organização

A estrutura de testes acompanha a estrutura do código-fonte.

```
tests/
│
├── core/
├── models/
├── ui/
├── sample_library/
│
├── conftest.py
└── README.md
```

Essa organização facilita localizar rapidamente os testes de cada módulo.

---

# Ordem de desenvolvimento

Os testes seguem exatamente a mesma ordem utilizada durante o desenvolvimento do projeto.

```
Song
    ↓
Metadata
    ↓
Discovery
    ↓
Scanner
    ↓
Statistics
    ↓
CLI
```

Primeiro validamos os componentes mais simples.

Depois validamos a integração entre eles.

---

# Tipos de testes

## Testes Unitários

Validam apenas um módulo isoladamente.

Exemplos:

- Song
- LibraryStatistics
- Discovery

Esses testes são rápidos e independentes.

---

## Testes de Integração

Validam a comunicação entre módulos.

Exemplos:

```
Discovery
    ↓
Scanner
```

ou

```
Scanner
    ↓
Statistics
```

Esses testes garantem que o pipeline funcione corretamente.

---

## Testes Reais

Além dos testes automatizados, o Melody Sync utiliza uma biblioteca musical real para validar seu funcionamento.

Essa etapa confirma que o comportamento observado durante o desenvolvimento permanece consistente em um cenário próximo ao uso cotidiano.

---

# Biblioteca de testes

A pasta `sample_library` contém arquivos preparados para validar diferentes situações.

```
sample_library/

valid/
invalid/
duplicates/
missing_tags/
```

Sempre que um novo caso real for encontrado durante o desenvolvimento, ele poderá ser incorporado a essa biblioteca para evitar regressões futuras.

---

# Simplicidade

Os testes devem ser pequenos, objetivos e fáceis de entender.

Sempre que possível:

- um comportamento por teste;
- nomes descritivos;
- sem lógica complexa;
- sem duplicação.

Se um teste ficar difícil de entender, provavelmente o código também precisa ser simplificado.

---

# Nosso objetivo

O objetivo dos testes não é alcançar 100% de cobertura.

O objetivo é permitir que qualquer módulo possa ser refatorado com confiança.

Um teste deve responder apenas uma pergunta:

> "Posso modificar este código sem medo de quebrar o restante do sistema?"

Se a resposta for sim, então os testes cumpriram seu propósito.

---

# Lema

> O código mostra o que construímos.
>
> O conhecimento explica por que construímos.
>
> Os testes garantem que continuemos construindo com segurança.