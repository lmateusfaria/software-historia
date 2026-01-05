# Estrutura do Projeto - Biblioteca Digital

## Equipe
- Diego Careno
- Luis Mateus
- Pedro Ximenes

## Visão Geral
A Biblioteca Digital é uma plataforma robusta e intuitiva para upload, organização e consulta de arquivos digitais, incluindo livros, documentos, PDFs e imagens. O sistema visa preservar e democratizar o acesso à informação de forma eficiente e segura.

## Backend
O backend é uma aplicação Java baseada no framework Spring Boot. Ele utiliza Maven como gerenciador de dependências e está configurado para rodar com Java 17.

### Principais Dependências
- **Spring Boot Starters**:
  - `spring-boot-starter-data-jpa`: Para persistência de dados.
  - `spring-boot-starter-security`: Para segurança e controle de acesso.
  - `spring-boot-starter-validation`: Para validação de dados.
  - `spring-boot-starter-web`: Para desenvolvimento de APIs REST.
- **Banco de Dados**:
  - `h2`: Banco de dados em memória para desenvolvimento.
  - `postgresql`: Banco de dados relacional para produção.
- **Swagger/OpenAPI**:
  - `springdoc-openapi-starter-webmvc-ui`: Para documentação da API.
- **JWT**:
  - `jjwt-api`, `jjwt-impl`, `jjwt-jackson`: Para autenticação baseada em tokens JWT.
- **Lombok**: Para reduzir boilerplate no código.
- **Testes**:
  - `spring-boot-starter-test`: Para testes unitários.

### Funcionalidades Backend
- Sistema de upload de múltiplos formatos (PDF, DOCX, TXT, JPG, PNG, etc.)
- API de busca avançada com filtros e ordenação
- Sistema de categorização e metadados
- Controle de acesso e perfis de usuário
- Gerenciamento de documentos e arquivos
- Armazenamento seguro (a definir com professora/coordenador)

### Configurações
- Arquivos de configuração:
  - `application.properties`: Configuração padrão.
  - `application-dev.properties`: Configuração para ambiente de desenvolvimento.
  - `application-prod.properties`: Configuração para ambiente de produção.

### Estrutura de Pastas (Backend)
- **src/main/java/com/bibliotecadigital**:
  - `config/`: Configurações gerais.
  - `domains/`: Modelos de domínio (Documento, Categoria, Usuário, etc.).
  - `repositories/`: Interfaces para acesso ao banco de dados.
  - `resources/`: Recursos adicionais.
  - `security/`: Configurações de segurança e controle de acesso.
  - `services/`: Lógica de negócios (upload, busca, categorização, etc.).

---

## Frontend
O frontend é uma aplicação Angular configurada para renderização do lado do servidor (SSR) com Express.

### Principais Dependências
- **Angular**:
  - `@angular/core`, `@angular/router`, `@angular/forms`: Componentes principais do Angular.
  - `@angular/ssr`: Para renderização do lado do servidor.
- **Express**: Para servir a aplicação SSR.
- **Framer Motion**: Para animações.
- **TailwindCSS**: Para estilização responsiva.

### Funcionalidades Frontend
- Interface de upload de arquivos (lote ou individual)
- Busca avançada com filtros por palavra-chave, título, autor, categoria e data
- Visualizador integrado de documentos (PDF, imagens, etc.)
- Sistema de categorização e organização
- Download de arquivos para uso offline
- Interface responsiva para acesso em qualquer dispositivo

### Configurações
- **Angular CLI**:
  - Configurações de build e serve estão definidas no `angular.json`.
  - Modos de produção e desenvolvimento configurados.
- **Prettier**:
  - Configurado para formatação de código consistente.
- **SSR**:
  - Arquivo de entrada: `src/server.ts`.

### Estrutura de Pastas (Frontend)
- **src/app**:
  - `core/`: Serviços e guardas de autenticação.
  - `features/`: Módulos de funcionalidades (ex.: `home`, `dashboard`, `usuarios`, `documentos`, `categorias`).
  - `shared/`: Componentes reutilizáveis (ex.: `button`, `card`, `upload`, `viewer`).

---

## Integrações
- **Backend e Frontend**:
  - O backend fornece APIs RESTful para o frontend consumir.
  - O frontend utiliza autenticação baseada em JWT, gerados pelo backend.
- **Funcionalidades de Usuários**:
  - **Login**: Implementado no frontend em `auth.service.ts` e `login.ts`, utilizando tokens JWT gerados pelo backend.
  - **Criação de Usuários**: Disponível no frontend em `user.service.ts` e `register.ts`, com integração direta ao backend.
  - **Controle de Acesso**: Sistema de perfis (administrador, contribuidor, visitante) para gerenciamento de permissões.
  - **Listagem de Usuários**: Implementado no backend e consumido pelo frontend.
- **Funcionalidades de Documentos**:
  - **Upload**: Sistema de envio de arquivos com validação de formato e tamanho.
  - **Busca**: API de busca avançada com filtros múltiplos.
  - **Categorização**: Sistema flexível de categorias e metadados.
  - **Visualização**: Visualizador integrado no navegador.
  - **Download**: API para download seguro de arquivos.
- **SSR**:
  - O frontend é renderizado no servidor utilizando Express, permitindo melhor performance e SEO.
  