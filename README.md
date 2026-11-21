# MeNota

O MeNota é uma agenda reflexiva para registrar acontecimentos, pensamentos e sentimentos e acompanhar as próprias experiências ao longo do tempo.
Os relatos podem ser reunidos em resumos por período e consultados por psicólogos e psiquiatras, mediante autorização do paciente.
O registro diário apoia a preparação para as consultas e a comunicação durante o acompanhamento.

## Problema, público e proposta de valor

O tempo da consulta, a dificuldade de lembrar acontecimentos e de expressar sentimentos podem deixar experiências importantes fora da conversa.
O MeNota oferece um espaço para registrá-las no dia a dia e retomá-las ao longo do acompanhamento.
As anotações e os resumos permitem observar recorrências e contrastes nos relatos. Com acesso autorizado, o profissional também pode consultar esse material antes do atendimento e se preparar com mais contexto.
O sistema é voltado a pessoas em acompanhamento psicológico ou psiquiátrico e aos profissionais que as atendem.

## Tecnologias

- **Java 21 e Spring Boot 4.2.0:** regras do sistema e servidor, com Spring Security para autenticação e permissões.
- **Thymeleaf, HTML e CSS:** páginas e interface.
- **PostgreSQL 18 e JDBC:** armazenamento e consultas.
- **Gemini API:** geração de resumos por período.
- **JUnit e Mockito:** testes automatizados.
- **Maven 3.10.1, via Wrapper:** dependências, compilação e execução.

## Instalação e execução

**Requisitos:** JDK 21, PostgreSQL 18, IntelliJ IDEA ou Eclipse com suporte a Maven e internet para baixar dependências e gerar resumos. O Maven 3.10.1 está definido no Wrapper do projeto.

1. Clonar ou baixar o [repositório](https://github.com/LuigiStefano/menota) (extrair os arquivos se utilizar o ZIP).
2. No pgAdmin, conectar ao PostgreSQL com uma conta administrativa. Em **Login/Group Roles → Create → Login/Group Role**, criar `menota_app`, definir uma senha em **Definition** e habilitar **Can login?** em **Privileges**.
3. Em **Databases → Create → Database**, criar o banco `menota`, com **Owner** igual a `menota_app`. A conexão padrão é `localhost:5432`.
4. Criar uma chave da Gemini API no [Google AI Studio](https://aistudio.google.com/api-keys).
5. Importar e executar o projeto em uma das IDEs abaixo.

### IntelliJ IDEA

1. Abrir o `pom.xml` como projeto Maven, selecionar o JDK 21 e aguardar o carregamento das dependências.
2. Em **Run → Edit Configurations**, criar ou editar a execução da classe `menota.MenotaApplication`.
3. Em **Environment variables** (disponível em **Modify options**), adicionar `DB_PASSWORD` com a senha de `menota_app` e `GEMINI_API_KEY` com a chave da API. Manter **Store as project file** desmarcado para não compartilhar as credenciais.
4. Executar a classe pelo botão **Run**.

### Eclipse

1. Em **File → Import → Maven → Existing Maven Projects**, selecionar a pasta que contém o `pom.xml` e concluir a importação.
2. Configurar o JDK 21 no projeto. Em **Run → Run Configurations → Java Application**, criar uma execução para `menota.MenotaApplication`.
3. Na aba **Environment**, adicionar `DB_PASSWORD` e `GEMINI_API_KEY` com os valores locais e executar em **Run**.

Acessar [http://localhost:8081](http://localhost:8081). As tabelas e contas de demonstração são criadas na inicialização. Para encerrar, utilizar **Stop** na IDE; as notas permanecem no banco.

Se a conexão com o banco for diferente da padrão, definir também `DB_URL` e `DB_USERNAME` na configuração de execução. O modelo pode ser alterado por `GEMINI_MODEL`; o padrão é `gemini-3.5-flash-lite`. Senhas e chaves devem permanecer nas configurações locais.

## Contas de demonstração

| Conta | E-mail | Senha |
|---|---|---|
| Paciente Teste | paciente@menota.test | Paciente123! |
| Profissional Teste | profissional@menota.test | Profissional123! |

As contas são fictícias e pertencem ao perfil de execução `demo`, ativo por padrão.

## Testes

O PostgreSQL deve estar ativo. Configurar o JDK 21 e a variável `DB_PASSWORD` também na execução dos testes; as variáveis da execução da aplicação não são transferidas automaticamente.

- **IntelliJ:** em **Run → Edit Configurations**, criar uma configuração **JUnit**, selecionar **All in package**, pacote `menota`, módulo `menota` e executar.
- **Eclipse:** clicar com o botão direito em `src/test/java` e selecionar **Run As → JUnit Test**. Em **Run Configurations → JUnit**, configurar `DB_PASSWORD` na aba **Environment**.

A suíte contém quatro testes unitários de acesso e revogação e um teste de inicialização do Spring com PostgreSQL. Esse último utiliza o banco configurado e inicializa tabelas e contas de demonstração. Os testes automatizados não fazem chamadas à Gemini API.

## Demonstração do fluxo principal

1. Entrar com a conta **Paciente Teste**.
2. Criar anotações fictícias e exibir os registros na agenda.
3. Abrir **Ver Resumo**, selecionar 7 ou 30 dias e gerar o resumo.
4. Apresentar o texto produzido a partir das anotações do período.

## Limitações

- Contas previamente cadastradas, sem cadastro público ou recuperação de senha.
- Notas com data automática do servidor, sem edição ou exclusão.
- Resumos de 7 ou 30 dias, sem histórico persistente.
- Geração dependente da disponibilidade e das cotas da API.
- Execução local. A revogação impede novas consultas, sem apagar informações anteriormente visualizadas.

## Próximos passos

- Cadastro de usuários e recuperação de senha.
- Edição e exclusão de anotações.
- Histórico de resumos e seleção de períodos personalizados.
- Hospedagem para acesso do paciente e do profissional em dispositivos diferentes.

## Fontes, licenças e uso de IA

A estrutura inicial foi gerada pelo [Spring Initializr](https://start.spring.io/). Componentes: [Spring Boot (Apache 2.0)](https://github.com/spring-projects/spring-boot/blob/main/LICENSE.txt), [Thymeleaf (Apache 2.0)](https://raw.githubusercontent.com/thymeleaf/thymeleaf/3.1-master/LICENSE.txt), [PostgreSQL (PostgreSQL License)](https://www.postgresql.org/about/licence/), [JUnit](https://junit.org/) e [Mockito (MIT)](https://github.com/mockito/mockito/blob/main/LICENSE). Os resumos utilizam a [Gemini API](https://aistudio.google.com/docs).

ChatGPT/Codex foi utilizado como apoio à elaboração e revisão de código, testes e documentação.

## Equipe

- Filipe Ribeiro Marques
- Luigi Stefano Vianna de Araújo Darzé
- Mikael Souza Barbosa
- Robert Wagner Lima Ramos Junior
- Victor Amadeu Sant Anna de Medeiros

**Turma:** ADS-NOT-PIT-4S-T1 — 54942.
