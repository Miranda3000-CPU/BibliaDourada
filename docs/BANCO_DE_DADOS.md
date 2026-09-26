# Banco de dados e anotações

## Por que o banco não está no repositório

O arquivo `bible_v2.db` contém as **anotações de estudo** do Bispo Edir Macedo
(Igreja Universal do Reino de Deus), obtidas por engenharia reversa do
aplicativo oficial. Esse conteúdo é protegido por direitos autorais e **não é
redistribuído aqui**.

Por isso `bible_v2.db` e `app/src/main/assets/databases/` estão no
`.gitignore`. O aplicativo é distribuído **sem as anotações**.

## O que o app faz sem o banco

Ele compila e roda normalmente:

- na primeira execução, cria um banco **vazio** com o schema completo
  (tabelas criadas, nenhuma linha);
- a tela inicial e o leitor mostram um aviso "O aplicativo está vazio" com o
  botão **Importar dados**;
- o usuário traz o conteúdo a partir de um arquivo que já possua.

Nada é enviado para fora do aparelho: toda a importação é local.

## Como rodar localmente com os dados

Para desenvolver com o banco carregado, escolha uma das opções — nenhuma delas
versiona o arquivo:

1. **Asset local** (build):
   coloque o arquivo em `app/src/main/assets/databases/bible_v2.db`.
   Ele é copiado automaticamente na primeira execução.

2. **adb push** (mais rápido, não mexe no APK):
   ```bash
   adb push bible_v2.db /data/local/tmp/bible_v2.db
   ```
   O app procura nesse caminho antes de olhar os assets.

3. **Importação pelo próprio app**: use a ferramenta "Importar dados"
   (ícone da Bíblia no cabeçalho → "Importar dados", ou o botão do estado
   vazio). Selecione o arquivo `.db`.

## Esquema do banco

O schema é recriado vazio em `BibleDatabaseHelper.EMPTY_SCHEMA`. Para que uma
importação `.db` seja aceita, o arquivo precisa conter ao menos a tabela
`verse` com registros.

```sql
CREATE TABLE book (
  id INTEGER NOT NULL, testament INTEGER, number INTEGER, name TEXT,
  abbreviation TEXT, "language" TEXT, PRIMARY KEY (id AUTOINCREMENT)
);

CREATE TABLE chapter (
  id INTEGER NOT NULL, number INTEGER, text TEXT, book_id INTEGER,
  language TEXT, PRIMARY KEY (id AUTOINCREMENT)
);

CREATE TABLE verse (
  id INTEGER NOT NULL, text TEXT, number INTEGER, chapter_id INTEGER,
  language TEXT, PRIMARY KEY (id AUTOINCREMENT)
);

CREATE TABLE comment (
  id INTEGER NOT NULL, text TEXT, chapter_id INTEGER,
  start_verse_id INTEGER, end_verse_id INTEGER, language TEXT,
  PRIMARY KEY (id AUTOINCREMENT)
);

CREATE TABLE bible_daily_reading (
  id INTEGER NOT NULL, date TEXT NOT NULL, chapter_id INTEGER NOT NULL,
  language TEXT, PRIMARY KEY (id AUTOINCREMENT)
);

CREATE TABLE highlight_color (
  id INTEGER NOT NULL, name TEXT, hexadecimal TEXT, usage_amount INTEGER,
  language TEXT, PRIMARY KEY (id AUTOINCREMENT)
);

-- Índices de busca (FTS3) alimentados a partir de verse e comment
CREATE VIRTUAL TABLE bible_search USING fts3(
  rowid INTEGER, verse_id INTEGER, verse_number INTEGER, verse_text TEXT,
  verse_text_raw TEXT, chapter_id INTEGER, chapter_number INTEGER,
  book_id INTEGER, book_name TEXT, testament INTEGER, language TEXT
);

CREATE VIRTUAL TABLE comment_search USING fts3(
  rowid INTEGER, comment_id INTEGER, comment_text TEXT,
  comment_text_raw TEXT, verse_id INTEGER, verse_number INTEGER,
  chapter_id INTEGER, chapter_number INTEGER, book_id INTEGER,
  book_name TEXT, testament INTEGER, language TEXT
);
```

### Como as anotações se ligam aos versículos

Na tabela `comment`:

| Campo            | Significado                                                        |
| ---------------- | ------------------------------------------------------------------ |
| `start_verse_id` | `verse.id` onde o estudo começa; `0` = estudo do capítulo inteiro  |
| `end_verse_id`   | `verse.id` final do intervalo; `0` = versículo único               |
| `chapter_id`     | `chapter.id` (usado quando `start_verse_id = 0`)                   |

A busca de estudo por versículo usa apenas `start_verse_id` e
`start_verse_id..end_verse_id`. Estudos de capítulo inteiro (`start_verse_id = 0`)
**não** são exibidos ao tocar em um versículo, para não repetir o mesmo texto em
todos eles.

### Índices de busca

`verse_text_raw` e `comment_text_raw` guardam o texto normalizado para o FTS3:
sem acentos, em maiúsculas e sem pontuação.

```
"1.2a: Fala aos filhos de Israel"  ->  "12A FALA AOS FILHOS DE ISRAEL"
```

## Formato JSON de anotações

Quando o usuário já tem um banco com os versículos e quer apenas acrescentar
anotações (ou trazer as suas próprias), o formato aceito é:

```json
{
  "format": "biblia-dourada/anotacoes",
  "version": 1,
  "language": "pt_BR",
  "annotations": [
    { "book": "Lev", "chapter": 1, "verse": 2, "text": "1.2a: Fala aos filhos de Israel..." },
    { "book": "Lev", "chapter": 1, "verse": 2, "endVerse": 3, "text": "Estudo do intervalo 2-3." },
    { "book": "Gênesis", "chapter": 1, "verse": 0, "text": "Estudo do capítulo inteiro." }
  ]
}
```

| Campo      | Obrigatório | Descrição                                                                 |
| ---------- | ----------- | ------------------------------------------------------------------------- |
| `language` | não         | Idioma de referência. Padrão `pt_BR`.                                     |
| `book`     | sim         | Abreviação (`Lev`), nome (`Levítico`) ou `id` numérico do livro.          |
| `chapter`  | sim         | Número do capítulo.                                                       |
| `verse`    | não         | Número do versículo. `0` (ou ausente) = estudo do capítulo inteiro.       |
| `endVerse` | não         | Número final, quando o estudo cobre um intervalo.                         |
| `text`     | sim         | Texto da anotação.                                                        |

Cada entrada é resolvida para os ids internos. Entradas que não casarem com um
livro, capítulo ou versículo existente são **ignoradas** e contadas no retorno,
sem interromper a importação.

Um arquivo de exemplo está em [`docs/exemplo-anotacoes.json`](exemplo-anotacoes.json).

### Gerando o JSON a partir de um banco

Se você tem um banco compatível e quer extrair só as anotações:

```bash
sqlite3 bible_v2.db <<'SQL'
.mode json
SELECT json_object(
         'book', (SELECT abbreviation FROM book WHERE id = ch.book_id),
         'chapter', ch.number,
         'verse', (SELECT number FROM verse WHERE id = c.start_verse_id),
         'endVerse', (SELECT number FROM verse WHERE id = c.end_verse_id),
         'text', c.text
       ) AS annotation
FROM comment c
JOIN chapter ch ON ch.id = c.chapter_id
WHERE c.language = 'pt_BR';
SQL
```

Depois envolva as linhas no objeto `{ "annotations": [ ... ] }`.

## Como importar no app

1. Abra o ícone da Bíblia no cabeçalho (ou o botão do estado vazio).
2. Toque em **Importar dados**.
3. Escolha:
   - **Importar banco de dados (.db)** — substitui todo o conteúdo;
   - **Importar anotações (.json)** — acrescenta ao banco atual (exige que os
     versículos já estejam carregados).
4. O seletor de arquivos do sistema abre; o arquivo é lido localmente.

## Aviso

Este projeto não tem qualquer vínculo, patrocínio ou afiliação com a Igreja
Universal do Reino de Deus ou com o autor das anotações. A ferramenta de
importação existe para que cada usuário utilize, no seu próprio aparelho, um
conteúdo que já possua — sem redistribuição.
