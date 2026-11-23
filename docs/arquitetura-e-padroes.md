# Arquitetura, SOLID e padrões de projeto do MeNota

O aplicativo principal usa Java 21 e Spring Boot 3.5.16. Ele serve páginas Thymeleaf e uma API JSON da mesma origem. O navegador usa HTML, CSS e JavaScript para escolher datas, acompanhar salvamentos e exibir os resultados. O PostgreSQL guarda contas, vínculos, páginas e análises.

## Responsabilidades e utilidades

| Código | Responsabilidade e utilidade |
| --- | --- |
| [`usuario/`](../src/main/java/br/edu/pooalab/diario/usuario/) | `UsuarioService` valida e registra as contas com pseudônimo, perfil e senha protegida. `UsuarioRepository` consulta o login e bloqueia a linha da conta para coordenar escritas. |
| [`seguranca/`](../src/main/java/br/edu/pooalab/diario/seguranca/) | `SegurancaConfig` configura o login, perfis, sessão e CSRF. `AesGcmCriptografia` protege o conteúdo no banco por meio de `CriptografiaTexto`. |
| [`registro/`](../src/main/java/br/edu/pooalab/diario/registro/) | `RegistroService` consulta e grava uma página por conta e data, confere a versão, atualiza o calendário e invalida as análises afetadas. `RegistroRepository` guarda e consulta páginas. `BemEstar` representa as categorias opcionais e `BemEstarCriptografia` centraliza a serialização e a proteção desses valores. |
| [`resumo/`](../src/main/java/br/edu/pooalab/diario/resumo/) | `ResumoService` seleciona e valida fontes, reaproveita análises, controla concorrência e persiste o resultado. `Sumarizador` define entradas, saída e identidade da configuração. `GeminiSumarizador` implementa o protocolo HTTP e valida o JSON, as datas, as categorias e os trechos. |
| [`acompanhamento/`](../src/main/java/br/edu/pooalab/diario/acompanhamento/) | `AcompanhamentoService` aplica as regras de perfil, consentimento, consulta e revogação. `AcessoRepository` concentra o SQL parametrizado do vínculo. |
| [`web/`](../src/main/java/br/edu/pooalab/diario/web/) | Controllers renderizam páginas, recebem os dados e encaminham operações aos serviços. `ErrosApi` traduz falhas em respostas HTTP para a interface. |
| [`templates/`](../src/main/resources/templates/) e [`static/`](../src/main/resources/static/) | Views e recursos locais. O mesmo `analise.js` apresenta emoções no histórico do paciente e na consulta profissional. CSS responsivo e controles rotulados atendem telas estreitas e navegação por teclado. |

## SOLID aplicado

- **S — Responsabilidade única.** Um controller trata a entrada HTTP, um serviço reúne as regras de um caso de uso e o repositório concentra a persistência. Separar o SQL de vínculos do `AcompanhamentoService` permite alterar a consulta sem misturar a alteração à regra de autorização.
- **O — Aberto para extensão.** Outra implementação de `Sumarizador` pode ser configurada por injeção de dependência. O caso de uso reutiliza a mesma validação de fontes e persistência. O identificador de modelo e da versão da configuração participa da assinatura para que mudar o provedor ou as instruções produza um cache distinto.
- **L — Substituição de Liskov.** Uma implementação substituta precisa cumprir o contrato de `Sumarizador`: receber as entradas, informar sua configuração e retornar texto, modelo e emoções válidas ou indicar erro de geração. Os testes de integração substituem a chamada externa para exercer as mesmas regras de negócio. Isso demonstra essa substituição específica.
- **I — Segregação de interfaces.** `CriptografiaTexto` expõe apenas cifrar e decifrar. `Sumarizador` reúne somente geração e informação da configuração necessária ao caso de uso. Os serviços de domínio não precisam de métodos de clientes HTTP, repositórios externos ou gerenciamento de chaves.
- **D — Inversão de dependência.** Os serviços recebem `Sumarizador` e `CriptografiaTexto` pelo construtor. O resumo usa a configuração declarada pelo contrato. A implementação Gemini e a implementação AES são detalhes conectados pelo Spring.

## Padrões e fluxos

- **MVC.** O navegador interage com a View Thymeleaf e o JavaScript. Os controllers encaminham ações para os serviços. O modelo e os repositórios preservam as regras e os dados. Exemplo: `PUT /api/registros` → `RegistroController` → `RegistroService` → `RegistroRepository`.
- **Repository.** Spring Data JPA abstrai as consultas de usuários, registros e resumos. `AcessoRepository` aplica o mesmo padrão com JDBC. A decisão de conceder ou revogar acesso permanece no serviço.
- **Adapter (GoF).** `GeminiSumarizador` adapta requisições HTTP e respostas JSON ao contrato `Sumarizador`. `AesGcmCriptografia` adapta a Java Cryptography Architecture ao contrato `CriptografiaTexto`. O primeiro permite variar o provedor de análise e o segundo evita espalhar o uso de `Cipher` pelas regras de registro.
- **Injeção de dependência.** O Spring fornece as dependências dos serviços. Os testes usam uma implementação simulada de sumarização e um servidor HTTP local para observar separadamente a regra de negócio e o protocolo do Adapter.

## Dados, transações e segurança

O PostgreSQL guarda `usuarios` (pseudônimo, perfil e hash BCrypt), `registros` (data, versão, texto e autorrelato cifrados), `resumos` (intervalo, metadados das fontes, síntese e emoções cifradas) e `acessos_acompanhamento` (titular, profissional e data da autorização). As migrações do `schema.sql` acrescentam colunas e tabela sem apagar registros anteriores. Contas existentes recebem perfil de paciente por padrão.

Na escrita da agenda, o serviço trava a conta por uma transação curta, confere a versão e salva a página e a invalidação das análises afetadas. Em uma geração, captura registros e suas versões, libera o banco antes da chamada externa e confere as fontes novamente antes de gravar. Uma coleção concorrente em memória limita a uma geração ativa por conta nessa instância.

Na consulta profissional, `AcompanhamentoService` valida o perfil e a autorização antes de ler qualquer conteúdo. A consulta e a revogação usam o bloqueio da mesma conta. Apenas a conta de paciente usa as APIs privadas de páginas e resumo. A propriedade dos dados vem da sessão e a conta da aba é conferida com `X-Diario-Usuario`. As operações de escrita exigem CSRF. Os textos recebem AES-256-GCM no banco e são decifrados no servidor para o usuário autorizado ou para o envio consentido ao Gemini.

O sistema de compatibilidade na pasta `acompanhamento/` usa Spring Boot 4.2.0, MVC e Repository JDBC, com seus próprios perfis, banco e sessão. Seu `ResumoService` também recebe um `Sumarizador` no construtor e um Adapter traduz a API Gemini. Esse módulo atende à agenda por e-mail. A agenda e o painel profissional descritos nas seções anteriores pertencem ao aplicativo principal.
