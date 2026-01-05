# Biblioteca Digital

## Proposta de Projeto

**Equipe:** Diego Careno, Luis Mateus e Pedro Ximenes.

---

## 1. Introdução e Justificativa

A preservação e o acesso à informação são pilares fundamentais para o desenvolvimento cultural e educacional de uma sociedade. Este projeto propõe o desenvolvimento de uma **Biblioteca Digital**, uma plataforma robusta e intuitiva para o upload, organização e consulta de um vasto acervo de arquivos digitais, incluindo livros, documentos, PDFs e imagens. 

A crescente digitalização de acervos e a necessidade de disponibilizar informações de forma ampla e acessível justificam a criação de um sistema que não apenas armazene, mas também facilite a pesquisa e a recuperação de informações de maneira eficiente e segura.

---

## 2. Objetivos

### 2.1. Objetivo Geral
Desenvolver uma solução de software completa para a implementação da Biblioteca Digital, garantindo uma plataforma centralizada para a gestão e o acesso a arquivos digitais de forma organizada, segura e escalável.

### 2.2. Objetivos Específicos
- Implementar um sistema de upload de arquivos que suporte múltiplos formatos.
- Desenvolver um mecanismo de busca avançada com filtros e ordenação.
- Criar um sistema de categorização flexível para organização do acervo.
- Garantir a visualização de diferentes tipos de arquivos diretamente na plataforma.
- Permitir o download dos arquivos para consulta offline.
- Assegurar a segurança e a integridade dos dados armazenados.

---

## 3. Funcionalidades Principais

O sistema contará com as seguintes funcionalidades essenciais:

- **Upload de Arquivos**: Interface para envio de arquivos em lote ou individualmente, com suporte para formatos como PDF, DOCX, TXT, JPG, PNG, entre outros.

- **Busca Avançada**: Ferramenta de pesquisa que permitirá aos usuários encontrar arquivos por palavra-chave, título, autor, categoria ou data de publicação.

- **Categorização e Metadados**: Sistema que permitirá a administradores e usuários autorizados a organização dos arquivos em categorias e a adição de metadados relevantes para facilitar a busca e a identificação.

- **Visualizador Integrado**: Exibição de documentos e imagens diretamente no navegador, sem a necessidade de download prévio.

- **Download de Arquivos**: Opção para baixar os arquivos para acesso e uso offline.

- **Controle de Acesso**: (Opcional) Sistema de perfis de usuário (administrador, contribuidor, visitante) para gerenciamento de permissões de upload, edição e acesso.

---

## 4. Requisitos Técnicos

A arquitetura do sistema será baseada em tecnologias modernas e de código aberto, visando a escalabilidade, a manutenibilidade e a segurança da plataforma.

| Componente       | Tecnologia Proposta                                    |
|------------------|-------------------------------------------------------|
| **Frontend**     | React.js ou Angular.js para uma interface rica e reativa |
| **Backend**      | Java, Spring Boot, Hibernate, JPA, API RESTful       |
| **Banco de Dados** | PostgreSQL                                           |
| **Armazenamento** | A DECIDIR JUNTO A PROFESSORA/COORDENADOR            |

---

## 5. Público-Alvo

A Biblioteca Digital destina-se a um público amplo e diversificado, incluindo:

- Pesquisadores e historiadores que necessitam de acesso a fontes primárias e documentos históricos.
- Estudantes e educadores em busca de material de apoio para atividades acadêmicas.
- Cidadãos interessados na história e na cultura local e regional.
- Servidores públicos que necessitam consultar documentos oficiais e processos arquivados.

---

## 6. Cronograma Resumido

O projeto será dividido em fases, com um cronograma estimado para garantir a entrega contínua de valor.

| Fase                           | Descrição                                                    |
|--------------------------------|--------------------------------------------------------------|
| 1. Planejamento e Análise      | Levantamento de requisitos e definição da arquitetura      |
| 2. Design da Interface (UI/UX) | Criação de wireframes e protótipos da interface             |
| 3. Desenvolvimento do Backend  | Implementação de APIs, autenticação e lógica de negócios   |
| 4. Desenvolvimento do Frontend | Implementação da interface e integração com backend         |
| 5. Testes e Homologação        | Testes funcionais, de segurança e performance               |
| 6. Implantação e Lançamento    | Deploy e disponibilização da plataforma                     |

---

## 7. Resultados Esperados

Ao final do projeto, espera-se entregar uma plataforma digital totalmente funcional e acessível, que:

- Centralize o acervo digital em um único local.
- Democratize o acesso à informação para um público mais amplo.
- Preserve a memória histórica e cultural para as futuras gerações.
- Otimize o processo de busca e consulta de documentos, economizando tempo e recursos.

---

## 8. Conclusão

A criação da Biblioteca Digital representa um passo significativo para a modernização da gestão de acervos e para a promoção do conhecimento. Com uma arquitetura robusta, funcionalidades intuitivas e um foco claro nas necessidades do usuário, este projeto tem o potencial de se tornar uma ferramenta indispensável para a comunidade e um modelo para outras iniciativas de preservação digital no país.

---

## Como Executar o Projeto

### Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Frontend
```bash
cd front
npm install
npm run dev
```

---

## Tecnologias Utilizadas

### Backend
- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Hibernate
- JWT (JSON Web Tokens)
- Swagger/OpenAPI

### Frontend
- Angular
- TypeScript
- TailwindCSS
- Angular SSR (Server-Side Rendering)
- RxJS

---

## Licença

Este projeto está sob a licença [MIT](LICENSE).

---

## Contato

Para mais informações, entre em contato com a equipe:
- Diego Careno
- Luis Mateus
- Pedro Ximenes
