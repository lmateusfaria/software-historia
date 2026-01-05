# Gestão Financeira Pessoal - Backend

## Visão Geral
Este projeto é o backend de uma aplicação de gestão financeira pessoal, desenvolvido em Java com o framework Spring Boot. Ele fornece APIs REST para gerenciar recursos financeiros, como contas, transações, usuários, entre outros. O backend utiliza PostgreSQL como banco de dados e implementa autenticação e autorização com JWT.

---

## Estrutura do Projeto
Abaixo está uma visão geral dos principais pacotes e suas responsabilidades:

### 1. **Pacote `config`**
- Contém classes de configuração para inicialização, segurança e integração com Swagger.
- **Principais classes:**
  - `DataInitializer`: Inicializa dados no banco para desenvolvimento.
  - `DevConfig`: Configurações específicas para o ambiente de desenvolvimento.
  - `OpenApiConfig`: Configurações para documentação da API com Swagger.
  - `SecurityConfig`: Configurações de segurança, como autenticação e autorização.

### 2. **Pacote `domains`**
- Define as entidades principais do sistema e suas estruturas auxiliares.
- **Principais classes:**
  - `Banco`, `CentroCusto`, `Conta`, `Transacao`, `Usuario`.
  - Subpacotes:
    - `dtos`: Objetos de transferência de dados.
    - `enums`: Enumerações para valores fixos.

### 3. **Pacote `repositories`**
- Contém interfaces para acesso ao banco de dados.
- **Principais classes:**
  - `BancoRepository`, `CentroCustoRepository`, `ContaRepository`, `TransacaoRepository`, `UsuarioRepository`.

### 4. **Pacote `resources`**
- Implementa controladores REST para expor endpoints da API.
- **Principais classes:**
  - `BancoResource`, `CentroCustoResource`, `ContaResource`, `TransacaoResource`, `UsuarioResource`.

### 5. **Pacote `security`**
- Gerencia autenticação e autorização.
- **Principais classes:**
  - `JWTAuthenticationFilter`, `JWTUtils`, `UserSS`.

### 6. **Pacote `services`**
- Contém a lógica de negócios do sistema.
- **Principais classes:**
  - `BancoService`, `UsuarioService`, `TransacaoService`, entre outros.

---

## Configuração do Ambiente

### 1. **Pré-requisitos**
- Java 17 ou superior.
- Maven 3.8 ou superior.
- PostgreSQL 14 ou superior.

### 2. **Configuração do Banco de Dados**
Certifique-se de que o PostgreSQL está instalado e configurado. Atualize as credenciais no arquivo `application-dev.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/gestaopessoalfinancas
spring.datasource.username=postgres
spring.datasource.password=postdba
```

O servidor estará disponível em `http://localhost:8080`.

---

## Endpoints Principais
Abaixo estão os principais endpoints disponíveis:

### **Usuários**
- `GET /usuarios`: Retorna todos os usuários cadastrados.
- `GET /usuarios/{id}`: Retorna os detalhes de um usuário específico pelo ID.
- `GET /usuarios/cpf/{cpf}`: Busca um usuário pelo CPF.
- `GET /usuarios/email/{email}`: Busca um usuário pelo email.
- `POST /usuarios`: Cria um novo usuário.
- `PUT /usuarios/{id}`: Atualiza os dados de um usuário existente pelo ID.
- `DELETE /usuarios/{id}`: Remove um usuário pelo ID.

### **Contas**
- `GET /contas`: Retorna todas as contas cadastradas.
- `GET /contas/{id}`: Retorna os detalhes de uma conta específica pelo ID.
- `POST /contas`: Cria uma nova conta.
- `PUT /contas/{id}`: Atualiza os dados de uma conta existente pelo ID.
- `DELETE /contas/{id}`: Remove uma conta pelo ID.

### **Transações**
- `POST /transacoes`: Registra uma nova transação.
- `GET /transacoes`: Lista todas as transações realizadas.

---

## Segurança
- **Autenticação:** Implementada com JWT.
- **Autorização:** Baseada em roles definidas para cada usuário(Por enquanto optamos apenas pelo 'ROLE_USER').

---

## Licença
Este projeto está licenciado sob a [MIT License](LICENSE).
