Keycloak Features
- Single Sign-On (SSO) and Single Logout
- Identity Brokering and Social Login
- User Federation ( LDAP / Active Directory )
- Fine-Grained Authorization Services 
- Centralized Management and Admin Console
- Client Adapters ( Java / Javascript / Node JS )

Keycloak Advantages
- Open Source
- Versatility
- Scalability
- Security
- Customizability
- Ease of Use ( designed to be ease to use)

Keycloak Terms
- Realm
- Clients
- Client Scopes
- Users
- Groups

- 1 - Implementar a interface Authenticator
- 1.2 - Sobre o método authenticate
    - O método authenticate do seu CustomAuthenticator inicia o processo de autenticação ao criar e apresentar um formulário de login personalizado. O template verify-username.ftl define a aparência e o comportamento desse formulário, permitindo que os usuários insiram suas informações (neste caso, um e-mail) para prosseguir com a autenticação.
- 1.3 - Sobre o metodo action
  - O método action em conjunto com a classe UserByPostgresDataProvider implementa um fluxo de autenticação robusto que valida as credenciais do usuário, registra eventos importantes para auditoria e garante a segurança das senhas armazenadas. Cada parte do código é projetada para lidar com casos de sucesso e erro, proporcionando uma experiência segura e eficiente de autenticação para os usuários.
  - O método verifica a existência do usuário no banco de dados PostgreSQL por meio da chamada ao método findByEmailAndPassword, que utiliza uma consulta SQL parametrizada, garantindo segurança contra injeções. Se o usuário for autenticado com sucesso, uma nova sessão é criada no Keycloak, onde atributos como e-mail, nome e sobrenome são configurados, permitindo que o sistema reconheça o usuário em interações futuras.
  - Além disso, o método registra eventos de login, coletando informações relevantes para auditoria e monitoramento, como o clientId e o redirectUri. A segurança das credenciais é reforçada ao gerar um hash da senha antes de atualizá-la no Keycloak, e o usuário pode ser obrigado a atualizar sua senha como uma medida adicional. 
  - Por outro lado, se o usuário não for encontrado ou se ocorrer uma exceção, o método trata esses erros de forma apropriada, registrando falhas e retornando mensagens informativas ao cliente. Dessa forma, o action não apenas valida e autentica usuários, mas também mantém um registro detalhado das operações, assegurando um fluxo de autenticação robusto e seguro.

- 2 - 
  - 1.2 - CustomAuthenticationFactory
  - A classe CustomAuthenticationFactory é uma implementação do AuthenticatorFactory que fornece uma instância do CustomAuthenticator, permitindo que o Keycloak utilize a lógica de autenticação personalizada. Ela define as características do autenticador, como suas opções de requisito, tipo de exibição e propriedades de configuração.
  - Teoricamente, a classe segue princípios de design como o padrão Singleton para garantir uma única instância do autenticador e encapsula a lógica necessária para integrar um autenticador customizado ao Keycloak. A implementação de métodos como getRequirementChoices e getConfigProperties permite que a classe defina como o autenticador se comportará no contexto do Keycloak, incluindo a adição de propriedades que podem ser úteis para a configuração do sistema.
  - Além disso, a definição de métodos de ciclo de vida (init, postInit e close) permite um maior controle sobre a inicialização e a limpeza do recurso, mesmo que neste caso específico estejam vazios. Essa estrutura permite que os desenvolvedores personalizem o processo de autenticação de forma eficaz, garantindo que o Keycloak possa ser adaptado a diferentes necessidades de segurança e autenticação.
