#!/usr/bin/env python3
"""
Gera um banco de demonstração para o app Bíblia Dourada.

Motivo: o banco real (com as anotações de estudo de terceiros) não é
versionado — ver .gitignore e docs/BANCO_DE_DADOS.md. Este script produz um
banco pequeno, com estrutura idêntica, usando nomes de livros e textos de
domínio público, para que qualquer pessoa que clone o repositório consiga:

  * rodar o app com conteúdo;
  * reproduzir os prints do README.

Uso:
    python3 tools/gerar_banco_demo.py [saida.db]
"""

import os
import sqlite3
import sys
from datetime import date

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DEFAULT_OUT = os.path.join(ROOT, "build", "demo", "bible_v2.db")

SCHEMA = [
    """CREATE TABLE book (id INTEGER NOT NULL, testament INTEGER, number INTEGER,
        name TEXT, abbreviation TEXT, "language" TEXT, PRIMARY KEY (id AUTOINCREMENT))""",
    """CREATE TABLE chapter (id INTEGER NOT NULL, number INTEGER, text TEXT,
        book_id INTEGER, language TEXT, PRIMARY KEY (id AUTOINCREMENT))""",
    """CREATE TABLE verse (id INTEGER NOT NULL, text TEXT, number INTEGER,
        chapter_id INTEGER, language TEXT, PRIMARY KEY (id AUTOINCREMENT))""",
    """CREATE TABLE comment (id INTEGER NOT NULL, text TEXT, chapter_id INTEGER,
        start_verse_id INTEGER, end_verse_id INTEGER, language TEXT,
        PRIMARY KEY (id AUTOINCREMENT))""",
    """CREATE TABLE bible_daily_reading (id INTEGER NOT NULL, date TEXT NOT NULL,
        chapter_id INTEGER NOT NULL, language TEXT, PRIMARY KEY (id AUTOINCREMENT))""",
    """CREATE TABLE highlight_color (id INTEGER NOT NULL, name TEXT, hexadecimal TEXT,
        usage_amount INTEGER, language TEXT, PRIMARY KEY (id AUTOINCREMENT))""",
    """CREATE VIRTUAL TABLE bible_search USING fts3(
        rowid INTEGER, verse_id INTEGER, verse_number INTEGER, verse_text TEXT,
        verse_text_raw TEXT, chapter_id INTEGER, chapter_number INTEGER,
        book_id INTEGER, book_name TEXT, testament INTEGER, language TEXT)""",
    """CREATE VIRTUAL TABLE comment_search USING fts3(
        rowid INTEGER, comment_id INTEGER, comment_text TEXT, comment_text_raw TEXT,
        verse_id INTEGER, verse_number INTEGER, chapter_id INTEGER,
        chapter_number INTEGER, book_id INTEGER, book_name TEXT, testament INTEGER,
        language TEXT)""",
]

LANG = "pt_BR"

# (id, testamento, ordem, nome, abreviação, total de capítulos)
BOOKS = [
    (1, 0, 1, "Gênesis", "Gen", 50),
    (2, 0, 2, "Êxodo", "Exo", 40),
    (19, 0, 19, "Salmos", "Sal", 150),
    (20, 0, 20, "Provérbios", "Pv", 31),
    (23, 0, 23, "Isaías", "Is", 66),
    (40, 1, 40, "Mateus", "Mt", 28),
    (41, 1, 41, "Marcos", "Mc", 16),
    (42, 1, 42, "Lucas", "Lc", 24),
    (43, 1, 43, "João", "Jo", 21),
    (66, 1, 66, "Apocalipse", "Ap", 22),
]

# Texto de domínio público (tradução clássica de João Ferreira de Almeida).
VERSES = {
    ("Gen", 1): [
        "No princípio criou Deus os céus e a terra.",
        "E a terra era sem forma e vazia; e havia trevas sobre a face do abismo.",
        "E disse Deus: Haja luz; e houve luz.",
        "E viu Deus que era boa a luz; e fez Deus separação entre a luz e as trevas.",
        "E Deus chamou à luz Dia; e às trevas chamou Noite.",
    ],
    ("Sal", 23): [
        "O Senhor é o meu pastor; nada me faltará.",
        "Deitar-me faz em verdes pastos, guia-me mansamente a águas tranquilas.",
        "Refrigera a minha alma; guia-me pelas veredas da justiça por amor do seu nome.",
        "Ainda que eu andasse pelo vale da sombra da morte, não temeria mal algum.",
        "Preparas uma mesa perante mim na presença dos meus inimigos.",
        "Certamente que a bondade e a misericórdia me seguirão todos os dias da minha vida.",
    ],
    ("Jo", 3): [
        "E havia entre os fariseus um homem, chamado Nicodemos, príncipe dos judeus.",
        "Este foi ter de noite com Jesus, e disse-lhe: Rabi, sabemos que és Mestre vindo de Deus.",
        "Jesus respondeu, e disse-lhe: Na verdade, na verdade te digo que aquele que não nascer de novo não pode ver o reino de Deus.",
        "O vento assopra onde quer, e ouves a sua voz, mas não sabes de onde vem nem para onde vai.",
        "E como Moisés levantou a serpente no deserto, assim importa que o Filho do homem seja levantado.",
        "Porque Deus amou o mundo de tal maneira que deu o seu Filho unigênito.",
    ],
}

# Anotações de estudo — texto próprio, apenas para demonstração.
ANNOTATIONS = [
    ("Gen", 1, 1, 0, "1.1: O ponto de partida. O texto não apresenta argumentos: afirma. "
                     "Antes de qualquer coisa existir, Deus já era, e tudo o que existe "
                     "começa por iniciativa dele."),
    ("Gen", 1, 3, 0, "1.3: A palavra que cria. Deus não monta o mundo com material "
                     "prévio: ele fala e a realidade responde. É o mesmo padrão que "
                     "reaparece em toda a Escritura."),
    ("Sal", 23, 1, 0, "23.1: O pastor e a ovelha. A imagem não descreve um Deus "
                      "distante, mas alguém que caminha à frente e conhece cada "
                      "necessidade antes que ela seja pedida."),
    ("Jo", 3, 16, 0, "3.16: O amor como medida. A frase mais conhecida do evangelho "
                     "resume o movimento de Deus em direção ao mundo: ele dá o que "
                     "tem de mais precioso."),
    ("Gen", 0, 0, 0, "Estudo do capítulo: Gênesis 1 estabelece o ritmo de seis dias "
                     "de trabalho e um de descanso, a base da semana que organiza a "
                     "vida do povo de Israel."),
]


def normalize(text: str) -> str:
    """Mesma normalização usada pelo app para os índices FTS3."""
    import unicodedata

    decomposed = unicodedata.normalize("NFD", text)
    without_accents = "".join(c for c in decomposed if not unicodedata.combining(c))
    cleaned = "".join(c if (c.isalnum() or c.isspace()) else "" for c in without_accents)
    return " ".join(cleaned.upper().split())


def build(out_path: str) -> None:
    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    if os.path.exists(out_path):
        os.remove(out_path)

    db = sqlite3.connect(out_path)
    cur = db.cursor()
    for statement in SCHEMA:
        cur.execute(statement)

    cur.executemany(
        "INSERT INTO book (id, testament, number, name, abbreviation, language) "
        "VALUES (?,?,?,?,?,?)",
        [(b[0], b[1], b[2], b[3], b[4], LANG) for b in BOOKS],
    )

    # Capítulos de todos os livros (sem texto) + ids para os livros com versículos.
    by_abbrev = {b[4]: b for b in BOOKS}
    chapter_id = 0
    chapter_ids: dict[tuple[str, int], int] = {}
    for book in BOOKS:
        for number in range(1, book[5] + 1):
            chapter_id += 1
            cur.execute(
                "INSERT INTO chapter (id, number, text, book_id, language) VALUES (?,?,?,?,?)",
                (chapter_id, number, None, book[0], LANG),
            )
            chapter_ids[(book[4], number)] = chapter_id

    # Versículos + índices de busca.
    verse_id = 0
    verse_ids: dict[tuple[str, int, int], int] = {}
    for (abbrev, chapter_number), texts in VERSES.items():
        book = by_abbrev[abbrev]
        cid = chapter_ids[(abbrev, chapter_number)]
        for number, text in enumerate(texts, start=1):
            verse_id += 1
            verse_ids[(abbrev, chapter_number, number)] = verse_id
            cur.execute(
                "INSERT INTO verse (id, text, number, chapter_id, language) VALUES (?,?,?,?,?)",
                (verse_id, text, number, cid, LANG),
            )
            cur.execute(
                "INSERT INTO bible_search (rowid, verse_id, verse_number, verse_text, "
                "verse_text_raw, chapter_id, chapter_number, book_id, book_name, "
                "testament, language) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                (
                    verse_id, verse_id, number, text, normalize(text), cid,
                    chapter_number, book[0], book[3], book[1], LANG,
                ),
            )

    # Anotações.
    comment_id = 0
    for abbrev, chapter_number, verse_number, end_verse, text in ANNOTATIONS:
        book = by_abbrev[abbrev]
        cid = chapter_ids.get((abbrev, chapter_number)) if chapter_number else None
        if cid is None:
            # Anotação do livro inteiro: usa o primeiro capítulo.
            cid = chapter_ids[(abbrev, 1)]
        start_id = verse_ids.get((abbrev, chapter_number, verse_number), 0) if verse_number else 0
        end_id = verse_ids.get((abbrev, chapter_number, end_verse), 0) if end_verse else 0

        comment_id += 1
        cur.execute(
            "INSERT INTO comment (id, text, chapter_id, start_verse_id, end_verse_id, "
            "language) VALUES (?,?,?,?,?,?)",
            (comment_id, text, cid, start_id, end_id, LANG),
        )
        cur.execute(
            "INSERT INTO comment_search (rowid, comment_id, comment_text, comment_text_raw, "
            "verse_id, verse_number, chapter_id, chapter_number, book_id, book_name, "
            "testament, language) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
            (
                comment_id, comment_id, text, normalize(text), start_id, verse_number, cid,
                chapter_number or 1, book[0], book[3], book[1], LANG,
            ),
        )

    # Leituras diárias de hoje.
    today = date.today().strftime("%d/%m")
    daily = [("Gen", 1), ("Sal", 23), ("Jo", 3)]
    for index, (abbrev, chapter_number) in enumerate(daily, start=1):
        cur.execute(
            "INSERT INTO bible_daily_reading (id, date, chapter_id, language) "
            "VALUES (?,?,?,?)",
            (index, today, chapter_ids[(abbrev, chapter_number)], LANG),
        )

    db.commit()
    db.close()

    print(f"Banco de demonstração criado em: {out_path}")
    print(f"  livros: {len(BOOKS)}  capítulos: {chapter_id}  "
          f"versículos: {verse_id}  anotações: {comment_id}  "
          f"leituras de hoje ({today}): {len(daily)}")


if __name__ == "__main__":
    build(sys.argv[1] if len(sys.argv) > 1 else DEFAULT_OUT)
