# MeNota

O MeNota é um protótipo acadêmico de agenda reflexiva para registrar acontecimentos, pensamentos e sentimentos e recuperar essas experiências por data. O diário privado permite reunir páginas em resumos por período para preparar o que o próprio usuário quer conversar em uma consulta. O módulo de acompanhamento oferece um fluxo separado em que o paciente pode autorizar um profissional a consultar as anotações feitas na agenda compartilhada. Use contas e relatos fictícios nas demonstrações.

O projeto atende principalmente a estudantes universitários em acompanhamento psicológico ou psiquiátrico. O problema investigado foi a dificuldade de lembrar acontecimentos e expressar sentimentos durante uma consulta com tempo limitado. Os relatos de validação, o recorte do MVP de acompanhamento e a declaração acadêmica da equipe estão nos [documentos do projeto](#documentação-acadêmica).

## Diário privado

### Criar conta, entrar e sair

- Em **Criar uma conta**, informe um pseudônimo único de 3 a 30 caracteres, usando letras minúsculas de `a` a `z`, números ou sublinhado. A senha precisa ter de 12 a 72 caracteres e ocupar no máximo 72 bytes em UTF-8. O cadastro dispensa nome e e-mail.
- Após o cadastro, entre com o pseudônimo e a senha. O login abre **Meu diário**. O sistema informa quando as credenciais são inválidas. Guarde as credenciais, pois o protótipo não oferece recuperação de senha.
- **Sair** encerra a sessão e retorna ao login. A sessão também expira após 30 minutos de inatividade.

### Escolher uma data, escrever e consultar

1. O diário abre na data local do navegador. Selecione outra data no calendário, use os botões de dia anterior e seguinte ou clique em **Hoje**.
2. Cada conta tem uma página por data. Uma data sem registro abre uma página vazia. Escreva até 20.000 caracteres e clique em **Salvar página**. O contador acompanha a digitação e o botão fica indisponível para texto em branco ou sem alterações. O servidor também valida o tamanho e rejeita texto vazio.
3. Para consultar ou editar, reabra a data. O texto salvo preenche o editor. Após editar, clique em **Salvar página** novamente. **Reabrir data** consulta a versão atual no servidor.
4. Para apagar uma página salva, clique em **Excluir página** e confirme. A exclusão é definitiva e a data volta a mostrar uma página vazia.

O editor indica carregamento, alterações pendentes, salvamento e falhas. Ele pede confirmação antes de descartar alterações ao trocar de data ou sair. Se houver falha de conexão, o texto digitado fica na aba, aguardando nova tentativa. Se a sessão expirar, entre com a mesma conta na nova aba indicada e use **Já entrei; retomar** na aba original. O rascunho só existe na memória da aba. Fechar a aba pode descartá-lo.

Cada página recebe uma versão. Quando uma segunda aba tenta gravar ou excluir usando uma versão antiga, o servidor bloqueia a operação e preserva a versão salva. Copie o texto pendente antes de reabrir a data se quiser reaproveitá-lo.

### Gerar um resumo

1. Salve as páginas que deseja incluir e abra **Meus resumos**. O formulário começa com os últimos sete dias, incluindo hoje, e aceita um intervalo personalizado de 1 a 31 dias corridos, com início e fim inclusivos.
2. Leia o aviso de privacidade e marque a confirmação de que o conteúdo é fictício e pode ser enviado ao Google. Clique em **Gerar resumo**. O servidor usa apenas as páginas salvas da conta no intervalo, por ordem de data. A seleção precisa conter ao menos 30 caracteres de texto, descontando os espaços no começo e no fim de cada página, e pode conter até 24.000 caracteres brutos. O sistema informa intervalos vazios e limites excedidos antes de chamar a API.
3. A tela mostra o texto gerado, o período, as datas das páginas usadas, o horário da geração e o modelo. Confira a síntese com os relatos originais. Ela foi projetada para descrever o que foi escrito e pode omitir detalhes ou cometer erros.
4. O histórico lista os 20 resumos mais recentes. Selecione um período da lista para abrir o conteúdo. **Atualizar histórico** consulta novamente o servidor, por exemplo após mudar uma página em outra aba.

O MeNota usa o Gemini 3.5 Flash-Lite (`gemini-3.5-flash-lite`). O servidor envia ao Google as datas e os textos selecionados, sem incluir o login ou identificadores das páginas. A integração pede uma síntese em português brasileiro, com saída de até 2.048 tokens e instruções para preservar dúvidas e contradições nos relatos e evitar diagnósticos e recomendações. A chave da API fica no servidor. No plano gratuito, o Google pode usar o conteúdo para melhorar seus produtos. A ausência da chave desabilita a geração e permite continuar escrevendo, consultando páginas e abrindo resumos que já existam.

Resumos ficam criptografados no banco. Repetir um intervalo com as mesmas páginas e versões recupera o resultado existente. Criar, editar ou excluir uma página invalida os resumos que abrangem aquela data. A aplicação admite uma geração em andamento por conta. Se as fontes mudarem durante a solicitação, o servidor descarta o resultado. Respostas bloqueadas, truncadas ou inválidas, erros de conexão e falta de cota deixam o histórico sem um novo resumo e exibem uma mensagem. O aplicativo não dispara novas tentativas automaticamente.

## Executar o diário privado

Use **JDK 21**, **PostgreSQL 18** e o **Maven Wrapper** incluído. A primeira compilação precisa de internet para baixar o Maven e as dependências. As opções abaixo partem da raiz do repositório.

### Demonstração no Windows

Com o Java 21 como `JAVA_HOME`, execute no PowerShell:

```powershell
.\mvnw.cmd -DskipTests package
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\iniciar-demo.ps1
```

Abra [http://127.0.0.1:8080](http://127.0.0.1:8080) e crie uma conta fictícia. O iniciador usa `target/menota.jar` e prepara um PostgreSQL local separado na porta **55438**. Se os executáveis não forem localizados, informe `-JavaExe` com o caminho de `java.exe` e `-PgBin` com a pasta `bin` do PostgreSQL. A pasta `.local` guarda o banco de demonstração, sua senha e a chave de criptografia. Preserve essa pasta para manter os dados entre execuções.

Para habilitar os resumos, gere a chave no [Google AI Studio](https://aistudio.google.com/apikey) e execute:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\configurar-gemini.ps1
```

O script pede a chave em um campo oculto e a guarda com proteção do Windows para o usuário atual. Reinicie a aplicação para carregar a configuração. Para encerrar, pressione **Ctrl+C** no terminal da aplicação e pare o banco de demonstração com:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\iniciar-demo.ps1 -PararBanco
```

### PostgreSQL próprio

Crie um banco vazio exclusivo para o diário. No `psql`, como administrador:

```sql
CREATE ROLE menota_diario_app LOGIN PASSWORD 'substitua-por-uma-senha-local';
CREATE DATABASE menota_diario OWNER menota_diario_app;
```

Configure as variáveis do processo antes de executar. O [arquivo de exemplo](.env.example) serve de referência.

| Variável | Valor |
| --- | --- |
| `DB_URL` | Padrão `jdbc:postgresql://localhost:5432/menota_diario`. |
| `DB_USER` | Padrão `menota_diario_app`. |
| `DB_PASSWORD` | Senha do usuário desse banco. |
| `DIARIO_KEY` | Chave obrigatória de 32 bytes em Base64. Gere uma vez e guarde-a em local privado. |
| `GEMINI_API_KEY` | Opcional. Habilita a geração de resumos. |
| `PORT` | Porta HTTP. Padrão `8080`. O servidor escuta em `127.0.0.1`. |

Exemplo de primeira execução no PowerShell:

```powershell
$env:DB_PASSWORD = 'a-senha-que-voce-definiu'
$bytes = New-Object byte[] 32
$rng = [Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$rng.Dispose()
$env:DIARIO_KEY = [Convert]::ToBase64String($bytes)
.\mvnw.cmd -DskipTests package
java -jar target/menota.jar
```

Guarde a chave gerada e restaure o mesmo valor em todas as execuções seguintes. Sem a chave original, os textos salvos ficam ilegíveis. Em Linux e macOS, use `./mvnw -DskipTests package` e defina as mesmas variáveis no ambiente do processo Java. Em uma IDE, importe o `pom.xml` da raiz com JDK 21 e passe as variáveis na configuração de execução da classe `br.edu.pooalab.diario.DiarioApplication`.

## Módulo de acompanhamento

O módulo em [`acompanhamento/`](acompanhamento/) tem login por e-mail, papéis de paciente e profissional e o banco `menota`. Cada módulo tem suas próprias contas e registros. Autorizar um profissional aqui compartilha apenas as notas da agenda de acompanhamento. As páginas e os resumos do diário privado permanecem acessíveis exclusivamente pela conta que os criou.

### Fluxo do paciente

- Em **Minha agenda**, escreva um relato e registre. A agenda rejeita relatos em branco, atribui a data do servidor e apresenta as notas mais recentes primeiro. Nesse módulo, notas salvas não têm edição ou exclusão.
- Em **Ver Resumo**, selecione **7** ou **30 dias**. O período termina hoje, inclusive. O servidor verifica se há notas e rejeita solicitações cujo conjunto de relatos formatados ultrapasse 40.000 caracteres. O resultado é apresentado na tela, sem histórico.
- Em **Permissões de acesso**, informe o e-mail de uma conta profissional cadastrada. A agenda lista os profissionais que já têm acesso e permite revogar cada autorização.

### Fluxo do profissional

- Depois do login, a página **Pacientes** lista somente os pacientes que concederam acesso. Selecione um paciente para ler suas notas e gerar uma síntese de 7 ou 30 dias.
- O sistema confere a autorização antes de consultar os registros. Na geração de resumo, confere novamente antes de exibir a resposta. A revogação impede novas consultas e bloqueia a entrega de um resumo se ocorrer durante a geração. O profissional perde acesso no sistema, e informações que já leu continuam conhecidas por ele.

### Executar o acompanhamento

Crie o banco `menota` com um usuário local `menota_app`, ou reutilize o banco já configurado para essa agenda. Passe `DB_PASSWORD` em um terminal separado. Use `DB_URL` e `DB_USERNAME` se precisar substituir `jdbc:postgresql://localhost:5432/menota` e `menota_app`. A geração requer `GEMINI_API_KEY` e aceita `GEMINI_MODEL`, cujo padrão é `gemini-3.5-flash-lite`.

```powershell
$env:DB_PASSWORD = 'a-senha-do-banco-de-acompanhamento'
.\mvnw.cmd -f acompanhamento/pom.xml spring-boot:run
```

Abra [http://127.0.0.1:8081](http://127.0.0.1:8081). O perfil `demo`, ativo por padrão, cria as tabelas e disponibiliza estas contas fictícias:

| Perfil | E-mail | Senha |
| --- | --- | --- |
| Paciente Teste | `paciente@menota.test` | `Paciente123!` |
| Profissional Teste | `profissional@menota.test` | `Profissional123!` |

## Arquitetura e proteção dos dados

O diário privado usa Java 21, Spring Boot 3.5.16, Spring MVC, Spring Security, Thymeleaf, JavaScript, Spring Data JPA e PostgreSQL. O módulo de acompanhamento usa Java 21, Spring Boot 4.2.0, Thymeleaf e JDBC, com permissões por perfil. A interface das duas aplicações é servida pelo próprio Spring.

- Os controllers do diário renderizam as páginas `/login`, `/cadastro`, `/` e `/resumos`. O JavaScript consulta `GET /api/registros?data=AAAA-MM-DD`, grava com `PUT /api/registros` e exclui com `DELETE /api/registros`. A renovação da sessão usa `GET /api/sessao`.
- A área de resumos usa `GET /api/resumos/configuracao` para saber se a geração está habilitada, `GET /api/resumos` para listar, `GET /api/resumos/{id}` para os detalhes e `POST /api/resumos` para gerar ou recuperar um resultado. O servidor identifica o dono pela sessão e verifica se a conta corresponde à da aba. As operações de escrita exigem token CSRF.
- No banco do diário, `usuarios` guarda o pseudônimo e o hash BCrypt da senha. A tabela `registros` guarda a data, a versão e o texto cifrado. A tabela `resumos` guarda o texto cifrado e metadados como intervalo, modelo e datas das fontes. O servidor cifra os textos com AES-256-GCM e os decifra para exibir ao dono ou enviar os relatos autorizados ao Gemini.
- No banco do acompanhamento, `usuarios`, `notas` e `autorizacoes` sustentam a agenda e os vínculos de acesso. As senhas são armazenadas como hashes e as notas ficam em texto no banco. Use dados fictícios nos dois módulos.

## Testes

Com o PostgreSQL ligado, use dois bancos descartáveis, um para cada suíte. Os testes do diário usam `TEST_DB_URL`, `TEST_DB_USER` e `TEST_DB_PASSWORD`. Os valores padrão dos dois primeiros são `jdbc:postgresql://127.0.0.1:5432/menota_diario_test` e `menota_diario_test`. Os testes de integração podem remover registros desses bancos. Para o acompanhamento, aponte `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` para o segundo banco.

```powershell
.\mvnw.cmd test
.\mvnw.cmd -f acompanhamento/pom.xml test
```

O diário testa cadastro e login, isolamento entre contas, CSRF, criação, edição e exclusão por data, controle de versão, criptografia e geração, reaproveitamento e invalidação de resumos. O acompanhamento testa a inicialização do Spring, o bloqueio de consultas sem autorização e a revogação durante a geração de resumos. As suítes usam simulações ou respostas HTTP locais para o Gemini.

## Documentação acadêmica

- [Problema e validação](docs/problema-validacao.md)
- [Definição do MVP de acompanhamento](docs/definicao-mvp.md)
- [Declaração acadêmica de IA e componentes do MVP de acompanhamento](docs/declaracao-ia-terceiros.md)

## Equipe

- Filipe Ribeiro Marques
- Luigi Stefano Vianna de Araújo Darzé
- Mikael Souza Barbosa
- Robert Wagner Lima Ramos Junior
- Victor Amadeu Sant Anna de Medeiros

**Turma** ADS-NOT-PIT-4S-T1 — 54942.
