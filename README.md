# Sistema de Login com Spring Boot + MongoDB Atlas

Aplicação web de cadastro e autenticação de usuários com perfis de acesso. Os usuários ficam salvos no **MongoDB Atlas**, um banco de dados na nuvem.

## Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 17 |
| Spring Boot | 4.1.1 |
| Spring Security | autenticação por formulário, senhas com BCrypt |
| Spring Data MongoDB | acesso ao banco |
| Thymeleaf | páginas HTML |
| MongoDB Atlas | banco de dados na nuvem |
| Maven | build (o Maven Wrapper já vem incluso) |

## Funcionalidades

- Cadastro de usuário com perfil: `ROLE_USER`, `ROLE_MANAGER` ou `ROLE_ADMIN`
- Senhas criptografadas com BCrypt antes de serem salvas
- Bloqueio de nome de usuário duplicado
- Login, logout e página protegida (`/home`)
- Mensagens de erro para usuário ou senha inválidos

---

## 1. Pré-requisitos

- **JDK 17 ou superior.** Para conferir, rode `java -version`.
- **Uma conta no [MongoDB Atlas](https://www.mongodb.com/cloud/atlas).** O plano gratuito (M0) é suficiente.
- Você **não** precisa instalar o Maven, porque o projeto usa o wrapper `mvnw`.

## 2. Configurar o MongoDB Atlas

1. No Atlas, crie um **Cluster**. Pode ser o gratuito M0.
2. Em **Database Access**, crie um usuário do banco com usuário e senha.
3. Em **Network Access**, libere o seu IP. Para testes, você pode usar `0.0.0.0/0`, que libera qualquer IP.
4. No cluster, clique em **Connect → Drivers** e copie a *connection string*. Ela tem este formato:

   ```
   mongodb+srv://<usuario>:<senha>@<cluster>.mongodb.net/?retryWrites=true&w=majority
   ```

5. Coloque o nome do banco depois de `.mongodb.net/`. Este projeto usa `pfc_login`:

   ```
   mongodb+srv://<usuario>:<senha>@<cluster>.mongodb.net/pfc_login?retryWrites=true&w=majority
   ```

Você não precisa criar o banco nem a coleção. Os dois são criados automaticamente no primeiro cadastro.

## 3. Definir a variável de ambiente `MONGODB_URI`

Por segurança, a connection string **não fica no código**. A aplicação lê o valor da variável de ambiente `MONGODB_URI`.

**Windows (PowerShell):**
```powershell
$env:MONGODB_URI = "mongodb+srv://<usuario>:<senha>@<cluster>.mongodb.net/pfc_login?retryWrites=true&w=majority"
```

**Linux / macOS / Git Bash:**
```bash
export MONGODB_URI="mongodb+srv://<usuario>:<senha>@<cluster>.mongodb.net/pfc_login?retryWrites=true&w=majority"
```

> A variável só vale para o terminal em que foi definida. Se abrir outro terminal, defina de novo.
> Na IDE (IntelliJ, Eclipse, VS Code), adicione `MONGODB_URI` nas *Environment variables* da configuração de execução.

## 4. Executar

Na raiz do projeto, **no mesmo terminal em que você definiu a variável**:

**Windows:**
```powershell
.\mvnw.cmd spring-boot:run
```

**Linux / macOS:**
```bash
./mvnw spring-boot:run
```

Quando aparecer `Started DemoApplication`, acesse **http://localhost:8080**.

## 5. Como usar

1. Acesse `http://localhost:8080`. Como você ainda não está logado, o sistema redireciona para `/login`.
2. Clique em **"Cadastre-se"**, escolha usuário, senha e perfil, e salve.
3. Faça login com os dados cadastrados. Você será levado para `/home`.
4. Clique em **Sair** para encerrar a sessão.

| Rota | Acesso | Descrição |
|---|---|---|
| `/` | público | redireciona para `/home` |
| `/login` | público | tela de login |
| `/cadastro` | público | tela e envio do cadastro |
| `/home` | autenticado | página inicial do sistema |
| `/logout` | autenticado | encerra a sessão (POST) |

---

## Como funciona a integração com o MongoDB

```
Navegador ──> AuthController ──> UsuarioRepository ──> MongoDB Atlas
                                       ▲                (coleção "usuarios")
Spring Security ──> CustomUserDetailsService
```

- **Conexão:** o Spring Boot lê a propriedade `spring.mongodb.uri`, definida em [`application.properties`](src/main/resources/application.properties), que recebe o valor de `MONGODB_URI`. Com essa propriedade, o Spring Boot cria a conexão com o Atlas automaticamente.
- **Modelo:** [`Usuario`](src/main/java/com/example/demo/model/Usuario.java) é anotado com `@Document(collection = "usuarios")`. Cada usuário vira um documento nessa coleção:
  ```json
  {
    "_id": "ObjectId(...)",
    "username": "joao",
    "password": "$2a$10$...hash BCrypt...",
    "role": "ROLE_USER"
  }
  ```
- **Repositório:** [`UsuarioRepository`](src/main/java/com/example/demo/repository/UsuarioRepository.java) estende `MongoRepository`. O Spring Data gera as consultas a partir do nome do método. Por exemplo, `findByUsername` vira `{ username: ? }`.
- **Cadastro:** o [`AuthController`](src/main/java/com/example/demo/controller/AuthController.java) verifica se o usuário já existe, criptografa a senha com BCrypt e salva o documento no Atlas.
- **Login:** o Spring Security chama o [`CustomUserDetailsService`](src/main/java/com/example/demo/service/CustomUserDetailsService.java), que busca o usuário no MongoDB e compara a senha digitada com o hash salvo. O campo `role` define o perfil de acesso.

> **Atenção (Spring Boot 4):** a partir da versão 4.0, a propriedade antiga `spring.data.mongodb.uri` foi **substituída** por `spring.mongodb.uri`. Se você usar o nome antigo, a aplicação ignora o Atlas e tenta conectar em `localhost:27017`. Os sintomas são lentidão de cerca de 30 segundos e erro 500 no cadastro.

## Problemas comuns

| Sintoma | Causa provável | Solução |
|---|---|---|
| Erro `Could not resolve placeholder 'MONGODB_URI'` | A variável de ambiente não foi definida | Refaça o passo 3 no mesmo terminal |
| Cadastro demora cerca de 30s e dá erro 500 | Atlas inacessível ou IP não liberado | Libere seu IP em *Network Access* |
| `Authentication failed` no log | Usuário ou senha do banco incorretos | Confira o usuário criado em *Database Access* |
| `Port 8080 was already in use` | Outra instância do app já está rodando | Feche a instância anterior ou rode com `--server.port=8081` |
| Login sempre falha | Usuário cadastrado com senha diferente | Cadastre o usuário de novo ou confira os dados |

## Estrutura do projeto

```
src/main/java/com/example/demo/
├── DemoApplication.java              # classe principal
├── config/SecurityConfig.java        # regras de acesso, login e logout
├── controller/AuthController.java    # rotas /, /login, /cadastro, /home
├── model/Usuario.java                # documento da coleção "usuarios"
├── repository/UsuarioRepository.java # acesso ao MongoDB
└── service/CustomUserDetailsService.java # carrega o usuário para o login
src/main/resources/
├── application.properties            # configuração (lê MONGODB_URI)
└── templates/                        # login.html, cadastro.html, home.html
```
