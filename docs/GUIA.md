# Guia de uso — Bíblia Dourada

Guia passo a passo de todas as telas e fluxos do aplicativo.

**Índice**

1. [Primeira execução](#1-primeira-execução)
2. [Navegação básica](#2-navegação-básica) — inclui o [tour de primeiro acesso](#tour-de-primeiro-acesso)
3. [Início](#3-início)
4. [Leitura](#4-leitura)
5. [Estudo do versículo](#5-estudo-do-versículo)
6. [Pesquisa](#6-pesquisa)
7. [Temas](#7-temas)
8. [Importar dados](#8-importar-dados)
9. [Privacidade](#9-privacidade)
10. [Solução de problemas](#10-solução-de-problemas)

---

## 1. Primeira execução

O aplicativo é distribuído **sem o conteúdo de estudo** — nem os versículos, nem
as anotações acompanham o APK. Por isso, na primeira vez que você abrir, verá:

<img src="prints/08-estado-vazio.png" width="300" alt="Tela inicial com o aviso de conteúdo não carregado" />

Toque em **Importar dados** e siga a [seção 8](#8-importar-dados).

> Se você compilou o app com um banco próprio (ver
> [BANCO_DE_DADOS.md](BANCO_DE_DADOS.md)), esta tela não aparece: o conteúdo
> já estará carregado.

---

## 2. Navegação básica

A barra inferior tem três abas, sempre visíveis:

| Aba | Para que serve |
|---|---|
| **Início** | Leitura do dia, retomar de onde parou e atalhos por livro |
| **Leitura** | O texto bíblico em si, com o estudo por versículo |
| **Pesquisa** | Busca em texto completo nos versículos e nas anotações |

No cabeçalho da aba **Início** há dois botões:

- **sol/lua** — alterna entre o tema claro (padrão) e o escuro;
- **livro** — abre o painel *Sobre o aplicativo*, com versão, autoria,
  contato e o acesso à importação de dados.

### Tour de primeiro acesso

Na **primeira vez** que o aplicativo é aberto, um tour escurece a tela, recorta
o elemento em foco e explica para que ele serve.

| Boas-vindas | Seção em foco |
|---|---|
| <img src="prints/09-tour-boas-vindas.png" width="240" alt="Tour, tela de boas-vindas" /> | <img src="prints/10-tour-secoes.png" width="240" alt="Tour destacando a aba Leitura" /> |

São 6 passos: uma boas-vindas e, em seguida, cada aba (**Início**, **Leitura**,
**Pesquisa**), o botão de **tema** e o botão **Sobre e dados**.

- **Avançar** vai para o próximo passo; **Pular tour** encerra de uma vez.
- Toque em qualquer lugar da área escurecida também avança.
- Enquanto o tour está ativo, o aplicativo não responde a toques — é só sair
  dele para continuar.
- Ele não volta a aparecer sozinho. Para rever, use **Sobre o aplicativo** →
  **Ver o tour novamente**.

---

## 3. Início

<img src="prints/01-inicio-escuro.png" width="300" alt="Tela inicial" />

**Leitura diária** — três capítulos sugeridos para a data de hoje. O plano é
lido do próprio banco de dados (tabela `bible_daily_reading`), então cada dia
traz um conjunto. Toque em **Ler** para abrir o capítulo.

**Continuar leitura** — o último capítulo aberto, guardado no aparelho. O botão
**Ler** leva direto para lá.

**Livros da Palavra** — atalhos para os livros, separados entre **Antigo** e
**Novo Testamento** pelos botões no canto direito da seção. Toque em um livro
para abrir o capítulo 1.

---

## 4. Leitura

<img src="prints/03-leitura.png" width="300" alt="Tela de leitura" />

**Trocar de capítulo**

- **Anterior** / **Próximo** na barra inferior;
- ou toque no título (por exemplo, `Gênesis 1`) no topo para abrir o seletor
  *Navegar na Bíblia*, que lista os livros e, em seguida, os capítulos.

**Tamanho da fonte** — os botões **A−** e **A+** no canto superior direito
ajustam o texto entre 14 e 32. A preferência fica salva.

**Rolagem** — ao abrir um capítulo vindo de um atalho ou da busca, a tela rola
automaticamente até o versículo de destino, que aparece destacado.

---

## 5. Estudo do versículo

Versículos que têm anotação exibem o selo **✦ Estudo** logo abaixo do texto.

<img src="prints/04-estudo.png" width="300" alt="Painel de estudo do versículo" />

Toque no versículo ou no selo para abrir o painel, que mostra:

1. a referência, por exemplo `Gênesis 1:1`;
2. o texto do versículo;
3. as anotações de estudo daquele versículo.

O painel traz **apenas** as anotações correlacionadas ao versículo tocado.
Anotações que valem para o capítulo inteiro não são repetidas em cada
versículo — para esses casos, use a **Pesquisa** pelo termo ou pelo nome do
livro.

Versículos sem selo não possuem anotação e não abrem o painel.

---

## 6. Pesquisa

<img src="prints/05-pesquisa.png" width="300" alt="Tela de pesquisa" />

Digite ao menos **2 letras**. A busca é feita em texto completo (SQLite FTS3) e
ignora acentos e pontuação — `coracao` encontra *coração*, e `espirito` encontra
*Espírito*.

Os filtros acima dos resultados restringem a busca:

- **Todos** — versículos e estudos;
- **Versículos** — apenas o texto bíblico;
- **Estudos** — apenas as anotações.

Cada resultado mostra a referência e um selo indicando se é **Versículo** ou
**Estudo**. Tocar em um resultado abre o capítulo na aba **Leitura**, já
posicionado no versículo.

---

## 7. Temas

<img src="prints/02-inicio-claro.png" width="300" alt="Tema claro" />

Toque no ícone **sol/lua** no cabeçalho da aba Início:

- **claro** (padrão) — a identidade dourada sobre fundo branco quente, com o
  dourado escurecido para manter o contraste de leitura;
- **escuro** — a paleta dourada sobre fundo quase preto.

A escolha é gravada no aparelho e mantida nas próximas aberturas. Barras de
status e navegação acompanham o tema.

---

## 8. Importar dados

Abra pelo botão **Importar dados** da tela inicial ou pelo painel *Sobre o
aplicativo* → **Importar dados**.

<img src="prints/07-importar.png" width="300" alt="Painel de dados do aplicativo" />

No topo aparece o que já está carregado: livros, versículos e anotações.

### Opção A — banco de dados completo (`.db`)

Use quando você tem um arquivo de banco com o texto bíblico (e, se quiser, as
anotações).

1. Toque em **Importar banco de dados (.db)**.
2. Escolha o arquivo no seletor do sistema.
3. Pronto: o conteúdo é **substituído** pelo do arquivo.

O arquivo é validado **antes** da troca. Se não for um banco reconhecido ou não
tiver versículos, a importação é cancelada e **nada é perdido**.

### Opção B — apenas anotações (`.json`)

Use quando os versículos já estão carregados e você quer acrescentar ou trazer
anotações próprias.

1. Toque em **Importar anotações (.json)**.
2. Escolha o arquivo.

As anotações são **mescladas** ao banco atual; nada é apagado. Entradas que não
correspondam a um livro, capítulo ou versículo existente são ignoradas e
informadas no resultado, sem interromper o restante.

O formato do JSON está descrito em
[BANCO_DE_DADOS.md](BANCO_DE_DADOS.md#formato-json-de-anotações), com um exemplo
pronto em [`exemplo-anotacoes.json`](exemplo-anotacoes.json).

### Solução de problemas na importação

| Mensagem | O que significa |
|---|---|
| *O arquivo não é um banco de dados reconhecido.* | O arquivo escolhido não é um SQLite com a tabela `verse`. Confira se selecionou o arquivo certo. |
| *O banco importado não contém versículos.* | O banco é válido, mas está vazio. |
| *O arquivo não é um JSON de anotações válido.* | O conteúdo não pôde ser lido como JSON. |
| *O JSON não contém o array "annotations".* | O JSON é válido, mas não tem a chave esperada. |
| *Importe primeiro um banco com os versículos (.db).* | A importação de anotações precisa que os versículos já existam. |
| *N anotações importadas. M entradas foram ignoradas…* | Parte das entradas não casou com livro/capítulo/versículo. Verifique a grafia do livro (`Lev`, `Levítico` ou o id). |

---

## 9. Privacidade

- Nenhum dado sai do aparelho. O aplicativo não faz requisições de rede.
- Não há coleta de dados, anúncios, login ou cobrança.
- As preferências (tema, tamanho da fonte, último capítulo) ficam no
  `SharedPreferences` local.
- Os arquivos importados são lidos apenas no momento da importação, a partir do
  arquivo que você escolher.

---

## 10. Solução de problemas

**A tela ficou vazia depois de importar.**
Confira os contadores no painel *Dados do aplicativo*. Se *Versículos* estiver
em 0, o arquivo importado não continha o texto bíblico.

**Toquei em um versículo e nada aconteceu.**
Esse versículo não tem anotação. Versículos com estudo exibem o selo
**✦ Estudo** embaixo do texto.

**Quero voltar ao estado inicial.**
Nos ajustes do Android, use *Armazenamento* → *Limpar dados* do aplicativo. O
banco volta a ficar vazio, o tema volta ao padrão (claro) e o tour de
boas-vindas aparece de novo na próxima abertura.

**O tour não aparece mais e eu queria revê-lo.**
Ele é mostrado uma única vez. Abra **Sobre o aplicativo** → **Ver o tour
novamente**.

**A busca não encontra uma palavra.**
Ela exige pelo menos 2 letras e procura por radicais — o resultado cobre flexões
da palavra. Se o conteúdo não estiver carregado, a busca não retorna nada.

---

Desenvolvido por **Jeiel Miranda** — [jeielmiranda.com.br](https://jeielmiranda.com.br) · JeielMirand@gmail.com
