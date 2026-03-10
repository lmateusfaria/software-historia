# Armazenamento de Imagens

## 1. Onde salvar os arquivos (Storage)

Em vez de salvar no banco, salve no sistema de arquivos do seu servidor Linux ou em um serviço de objetos:

- **MinIO (Recomendação Pro):** É um servidor de armazenamento de objetos *open-source* que você instala no seu Linux (via Docker ou binário). Ele simula a API do Amazon S3.
    - **Vantagem:** O Java tem bibliotecas excelentes para o MinIO. Você salva o arquivo lá e guarda apenas a **URL/Path** no PostgreSQL.
    

**2. PostgreSQL para Metadados e OCR**
Como você já usa Postgres, ele será o "cérebro" que relaciona tudo.
• **Tabela de Edições/Páginas:** Guarde o ID, Data do Jornal, Edição, Número da Página e o `file_path` (caminho da imagem).
• **Busca Textual (Full-Text Search):** O Postgres possui suporte nativo para busca textual (`tsvector`). Você pode salvar o texto extraído pelo OCR em uma coluna específica.
• **JSONB:** Se o seu OCR extrair coordenadas (ex: a palavra "Café" está na posição $x, y$ da imagem), você pode salvar esses dados estruturados em uma coluna do tipo `jsonb` para que o Angular possa desenhar um "quadrado" sobre a palavra buscada.

---

## **3. Fluxo de Processamento (Back-end Java)**

Para o OCR, como você está em um ambiente Java/Linux, o fluxo seria:

1. **Upload:** Angular envia a imagem para o Java.
2. **Armazenamento:** Java salva a imagem no MinIO ou Disco.
3. **Processamento:** O Java chama uma biblioteca de OCR.
    - **Tesseract OCR:** Você instala o binário no Linux (`sudo apt install tesseract-ocr`) e usa a biblioteca **Tess4J** no Java para processar a imagem.
4. **Persistência:** O Java salva o texto resultante no PostgreSQL vinculado ao ID da imagem.

## 4. Como relacionar Imagem e Texto no Front (Angular)

Para que a busca seja útil em jornais antigos, o usuário não quer apenas saber que a palavra existe, mas **onde** ela está.

- **Dica de Ouro:** Tente extrair o OCR no formato **hOCR** (um padrão HTML que o Tesseract gera). Ele contém o texto e as coordenadas.
- No Angular, você pode usar uma biblioteca de visualização de imagens (como `ngx-image-viewer` ou até um Canvas) para sobrepor os resultados da busca sobre a foto do jornal.

### Resumo da Stack sugerida:

- **Imagens:** MinIO (rodando no seu Linux).
- **Dados e OCR:** PostgreSQL (usando `TEXT` para o conteúdo e `JSONB` para coordenadas).
- **Motor de OCR:** Tesseract OCR (via Tess4J no Spring Boot).

O **Neo4j** (banco de dados de grafos) seria um "divisor de águas" para o seu projeto, mas **não para substituir o PostgreSQL**, e sim para trabalhar ao lado dele.

No seu cenário de jornais antigos, o Neo4j não serve para guardar o texto bruto (OCR), mas sim para **conectar as entidades** encontradas nesses textos. Enquanto o Postgres responde "Quais jornais mencionam a palavra Café?", o Neo4j responde "Quais pessoas mencionadas neste jornal de 1920 também aparecem em jornais de 1940 e qual a ligação entre elas?".

Aqui estão os ganhos reais de adicionar o Neo4j à sua stack (Java/Angular/Postgres):

---

### 1. Descoberta de Redes de Relacionamento

Jornais antigos são minas de ouro para genealogia e história política/social. Com o Neo4j, você pode criar nós para:

- **Pessoas:** Políticos, anunciantes, falecimentos.
- **Locais:** Ruas antigas, bairros, estabelecimentos comerciais.
- **Eventos:** Revoltas, inaugurações, festas.
- **Organizações:** Empresas, partidos, clubes.

**O diferencial:** Você pode descobrir que o "Doutor Silva" citado em uma nota de rodapé em 1910 é o mesmo que fundou uma fábrica citada em 1930. O Neo4j mapeia isso visualmente.

### 2. Enriquecimento do OCR com NER (Named Entity Recognition)

Ao processar o OCR com o **GPT-4.1 Nano**, você pode pedir para ele: *"Extraia o texto e liste todas as pessoas e locais mencionados"*.

- O **Texto completo** vai para o `TEXT` do **Postgres**.
- As **Entidades e suas conexões** vão para o **Neo4j**.

### 3. Sugestões de "Conteúdo Relacionado" no Angular

Sabe quando o YouTube sugere um vídeo baseado no que você viu? Com Neo4j, seu front-end Angular pode ter um widget de:

> *"Pessoas citadas nesta página também aparecem nestas outras edições..."*
> 
> 
> Isso é computacionalmente caro no Postgres (exige muitos `JOINs`), mas é instantâneo no Neo4j.
> 

---

### Como ficaria sua Arquitetura Híbrida

| **Camada** | **Tecnologia** | **Função** |
| --- | --- | --- |
| **Arquivos** | MinIO / Disco | Armazena as imagens (JPG/TIFF). |
| **Relacional** | **PostgreSQL** | Dados de usuários, logins, metadados básicos (data, edição) e busca textual por palavras isoladas. |
| **Grafos** | **Neo4j** | Conecta as informações: `(Pessoa)-[:CITADA_EM]->(Edição_Jornal)`. |
| **IA** | GPT-4.1 Nano | Faz o OCR e identifica quem são as pessoas/locais para o Neo4j. |

---

### Vale a pena o esforço extra?

- **SIM, se:** Seu objetivo é criar uma ferramenta de **pesquisa histórica profunda**, onde o usuário quer navegar por conexões entre fatos e pessoas ao longo das décadas.
- **NÃO, se:** O objetivo é apenas um "Google" de jornais antigos onde a pessoa digita um termo, lê a página e pronto.

### Exemplo de consulta no Neo4j (Cypher)

Se um pesquisador quisesse saber todos os jornais onde uma família específica é mencionada:

Cypher

`MATCH (p:Pessoa {sobrenome: "Almeida"})-[:MENCIONADA_EM]->(j:Jornal)
RETURN p, j`

**Você gostaria que eu mostrasse como seria o fluxo no seu Back-end Java para salvar os dados simultaneamente no Postgres e no Neo4j?**