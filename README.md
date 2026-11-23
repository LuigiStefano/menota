# MeNota

O MeNota é uma agenda reflexiva para estudantes universitários.
Permite registrar experiências e rever páginas pelo calendário.
O paciente pode gerar análises por período e autorizar a consulta por um profissional.

## Problema, público e proposta de valor

O projeto é voltado a estudantes em acompanhamento psicológico ou psiquiátrico. Consultas com tempo limitado e a dificuldade de lembrar ou verbalizar experiências motivaram o projeto. A escrita livre ajuda a registrar o que aconteceu perto do momento em que ocorreu. A escolha de compartilhar fica com o paciente. Veja as hipóteses e as conversas anonimizadas na [validação](docs/problema-validacao.md).

## Instalar em outro computador

**Requisitos** JDK 21, PostgreSQL 18, navegador atualizado e internet para baixar as dependências na primeira compilação. O Maven Wrapper usa Maven 3.10.1.

**Tecnologias** Spring Boot 3.5.16, Spring Security, Spring Data JPA e Thymeleaf concentram a interface, as regras de acesso e a persistência no servidor Java. HTML, CSS e JavaScript fazem o calendário e as interações no navegador. O PostgreSQL preserva os dados entre execuções. O Gemini 3.5 Flash-Lite gera as análises, e JUnit e Mockito exercitam as regras sem depender da disponibilidade do Google.

### Windows com banco de demonstração

1. [Baixe o ZIP](https://github.com/LuigiStefano/menota/archive/refs/heads/main.zip) e extraia ou clone o [repositório](https://github.com/LuigiStefano/menota).
2. Instale JDK 21 e PostgreSQL 18. Configure `JAVA_HOME` com a pasta do JDK e tenha o Java no `PATH`. Confira com `java -version`.
3. Abra o PowerShell na pasta que contém `pom.xml` e execute:

```powershell
.\mvnw.cmd -DskipTests package
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\iniciar-demo.ps1
```

4. Mantenha o terminal aberto. Nesse mesmo computador, acesse [http://127.0.0.1:8080](http://127.0.0.1:8080). O inicializador usa `target/menota.jar` e cria um banco de demonstração separado na porta `55438`. Se precisar indicar os executáveis, passe `-JavaExe 'C:\caminho\jdk-21\bin\java.exe'` e `-PgBin 'C:\Program Files\PostgreSQL\18\bin'`.

Para habilitar análises, gere uma chave no [Google AI Studio](https://aistudio.google.com/apikey). Em outro PowerShell na raiz do projeto, execute o comando abaixo, digite a chave quando solicitada e reinicie o MeNota. O configurador armazena a chave protegida para o usuário do Windows. Para servidores configurados manualmente, defina `GEMINI_API_KEY` no ambiente do processo.

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\configurar-gemini.ps1
```

Encerre a aplicação com **Ctrl+C** e desligue o banco de demonstração com `powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\iniciar-demo.ps1 -PararBanco`. Preserve a pasta privada `.local` para manter banco e chave de criptografia entre execuções. Uma instalação nova tem seu próprio banco e suas próprias contas.

### Acessar de outro dispositivo no mesmo Wi-Fi

No computador que hospeda o MeNota, encerre a execução anterior e inicie com `powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\iniciar-demo.ps1 -Celular`. No outro computador ou celular na mesma rede, abra o endereço `http://IP-DO-COMPUTADOR:8080` mostrado no terminal. As contas são as da instalação que está servindo a página. Esse modo usa HTTP e expõe o servidor à rede local. Use uma rede privada de confiança, dados fictícios e confira a permissão do firewall para a rede privada se o navegador não conectar. A execução padrão escuta apenas em `127.0.0.1`.

### Rodar com PostgreSQL próprio, Linux, macOS ou IDE

Crie um banco vazio para o aplicativo. No `psql`, execute com um usuário administrador:

```sql
CREATE ROLE menota_diario_app LOGIN PASSWORD 'substitua-por-uma-senha-local';
CREATE DATABASE menota_diario OWNER menota_diario_app;
```

| Variável | Valor |
| --- | --- |
| `DB_URL` | Padrão `jdbc:postgresql://localhost:5432/menota_diario`. |
| `DB_USER` | Padrão `menota_diario_app`. |
| `DB_PASSWORD` | Senha do usuário do banco. |
| `DIARIO_KEY` | Chave obrigatória de 32 bytes em Base64, gerada uma vez. Use sempre a mesma chave com esse banco. |
| `GEMINI_API_KEY` | Opcional. Habilita novas análises. |
| `PORT` e `SERVER_ADDRESS` | Padrões `8080` e `127.0.0.1`. |

Gere `DIARIO_KEY` com `openssl rand -base64 32` ou, no PowerShell, com `[Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))` no PowerShell 7. Guarde o valor fora do Git para restaurá-lo em futuras execuções. Configure essas variáveis no ambiente do processo, conforme o [exemplo](.env.example). Execute `./mvnw -DskipTests package` no Linux/macOS ou `.\mvnw.cmd -DskipTests package` no Windows e depois `java -jar target/menota.jar`. Na IDE, importe o `pom.xml` da raiz, selecione JDK 21 e configure as mesmas variáveis para a classe `br.edu.pooalab.diario.DiarioApplication`. O esquema é preparado no início e os dados ficam no PostgreSQL.

## Roteiro curto de demonstração

Na tela de entrada, clique em **Ver prévia com exemplos** para explorar três páginas fictícias, uma análise preenchida e um exemplo de compartilhamento. A prévia também abre diretamente em `/preview`.

1. Abra o MeNota, clique em **Criar conta** e cadastre `paciente_demo` como **Paciente**. Em uma janela anônima ou em outro perfil do navegador, cadastre `profissional_demo` como **Profissional de saúde**. Use senhas fictícias com pelo menos 12 caracteres.
2. Entre como paciente. Em **Minha agenda**, escolha uma data e escreva um relato fictício, por exemplo: “Fiquei feliz quando concluí a atividade em grupo, embora tenha sentido cansaço depois.” Marque humor ou energia se desejar e clique em **Salvar página**. Escolha outra data e retorne para mostrar o marcador e o texto salvo.
3. Abra **Acompanhamento** (**Emoções** no celular), escolha um período que inclua a página, confirme o envio de dados fictícios e clique em **Analisar período**. Com a chave Gemini configurada, mostre a síntese, os trechos que fundamentam as emoções e o histórico. Em caso de indisponibilidade do Google, abra uma análise fictícia já preparada ou use a gravação ou o roteiro visual da equipe.
4. Em **Compartilhar** (**Acesso** no celular), informe `profissional_demo`, leia o alcance e confirme a autorização. Entre na outra janela como profissional, selecione o paciente e consulte o mesmo intervalo.
5. Na conta do paciente, remova o acesso. Na conta profissional, refaça a consulta e confira que a autorização foi revogada.

## Como funciona

### Cadastro, login e sessão

Cada perfil usa um pseudônimo único de 3 a 30 caracteres, com letras minúsculas de `a` a `z`, números ou sublinhado. A senha tem de 12 a 72 caracteres e até 72 bytes UTF-8. O cadastro dispensa e-mail. Paciente abre a agenda. Profissional abre o painel de consulta. **Sair** encerra a sessão, que também expira após 30 minutos de inatividade. O perfil profissional é autodeclarado. O protótipo não oferece verificação de registro profissional ou recuperação de senha.

### Agenda e autorrelato

A agenda começa no dia local do navegador. Selecione qualquer data no calendário, navegue entre meses, volte a **Hoje** ou use as setas entre dias. Um ponto coral indica uma página salva. Cada conta de paciente tem uma página por data, com texto obrigatório de até 20.000 caracteres. Humor (de **Muito baixo** a **Muito bom**) e energia (**Baixa**, **Média**, **Alta**) são opcionais. São percepções que o paciente informa.

Salve explicitamente. Reabrir uma data carrega o registro para consulta ou edição. Em **Mais opções**, é possível recarregar ou excluir a página. A exclusão pede confirmação e remove o marcador do calendário. Alterações pendentes, falhas e sucesso aparecem na tela. Se a sessão expirar, entre novamente como o mesmo usuário em outra aba e clique em **Continuar** na aba original. Rascunhos ficam na memória da aba. Se outra aba gravar primeiro, o número de versão evita sobrescrita. Preserve o rascunho antes de reabrir uma página em conflito.

### Análise por período com Gemini

O formulário inicia nos últimos sete dias e aceita até 31 dias inclusivos. Após a confirmação, o servidor seleciona somente páginas salvas da conta no período. Exige pelo menos 30 caracteres úteis e limita o total a 24.000 caracteres. Envia ao Gemini 3.5 Flash-Lite datas, textos e autorrelatos opcionais, sem pseudônimo ou identificador interno. A resposta pede uma síntese em português, com linguagem natural e parágrafos curtos, mantendo contrastes, incertezas e o contexto original.

A análise pode apresentar até duas emoções por data, entre alegria, tristeza, raiva, medo, preocupação, tranquilidade, frustração e alívio. O servidor confere data, categoria, duplicação e presença do trecho literal na página. A IA marca interpretações incertas. Humor e energia aparecem como autorrelato, separados das emoções inferidas. As contagens indicam em quantos dias houve identificação, sem medir intensidade. Confira a análise com o texto original. A ferramenta não tem finalidade clínica. Use conteúdo fictício, pois o provedor processa os dados selecionados.

O histórico mostra até 20 análises recentes com período, datas utilizadas, horário, modelo e texto. Um pedido com as mesmas fontes e configuração reaproveita o resultado. Criar, editar ou excluir uma página, inclusive ao mudar seus autorrelatos, invalida as análises que incluíam aquela data. Se as páginas mudarem enquanto a IA responde, o resultado é descartado. Falhas, cota esgotada e respostas inválidas são informadas. Sem chave, há acesso à agenda e ao histórico já salvo.

### Compartilhamento e painel profissional

A autorização é dada pelo paciente a um usuário exato. Seu alcance inclui todas as páginas e análises presentes e futuras enquanto o vínculo existir. Após entrar, o profissional vê apenas pacientes que lhe deram acesso. Escolhe um período de até 31 dias e consulta páginas, humor, energia e análises já geradas pelo paciente. O painel considera as 20 análises mais recentes e mostra as integralmente contidas no intervalo. O profissional tem acesso de leitura, sem edição ou geração de análises pela conta dele. Revogar bloqueia novas consultas. Informações já visualizadas podem continuar conhecidas pela pessoa que as leu.

O módulo de compatibilidade em [`acompanhamento/`](acompanhamento/) usa suas próprias contas por e-mail, banco `menota` e porta `8081`. Para abrir dados dessa instalação, configure `DB_PASSWORD` e, se necessário, `DB_URL` e `DB_USERNAME` em outro terminal, e execute `.\mvnw.cmd -f acompanhamento/pom.xml spring-boot:run`. Os padrões são `jdbc:postgresql://localhost:5432/menota` e `menota_app`. No perfil de demonstração, as contas fictícias são `paciente@menota.test` / `Paciente123!` e `profissional@menota.test` / `Profissional123!`. O roteiro acima é do aplicativo principal em `8080`.

## Testes

Crie um banco descartável como no exemplo de PostgreSQL próprio, usando `menota_diario_test` como nome do banco e do usuário. Configure `TEST_DB_PASSWORD` com a senha desse usuário. Se usar outro nome ou servidor, ajuste `TEST_DB_URL` e `TEST_DB_USER`. O padrão é `jdbc:postgresql://127.0.0.1:5432/menota_diario_test`, com usuário `menota_diario_test`. Em um terminal separado, aponte `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` para outro banco descartável se executar a suíte de compatibilidade.

```powershell
.\mvnw.cmd test
.\mvnw.cmd -f acompanhamento/pom.xml test
```

As suítes verificam login, isolamento entre contas, CSRF, criptografia, calendário, autorrelato, autorização e revogação, além da geração, consistência e invalidação de análises. A API externa é simulada nos testes automatizados.

## Limitações e próximos passos

O protótipo foi feito para demonstração local com dados fictícios. A geração depende de chave, conexão e cota do Gemini. O cadastro profissional é autodeclarado, e cada autorização dá acesso ao conjunto de registros até a revogação. Próximos passos são observar o fluxo com usuários usando dados fictícios, avaliar a clareza do compartilhamento e estudar verificação profissional, compartilhamento seletivo e recuperação de conta.

## Documentação e créditos

O aplicativo usa Spring Boot e Thymeleaf (Apache-2.0), PostgreSQL (PostgreSQL License), Caveat e Nunito (SIL OFL 1.1) e a API Gemini. A equipe escreveu o código com apoio do ChatGPT/Codex. O Anexo E detalha as fontes, as licenças, as partes apoiadas por IA e as verificações.

- [Definição do MVP](docs/definicao-mvp.md)
- [Problema e registro de validação](docs/problema-validacao.md)
- [Arquitetura, SOLID e padrões de projeto](docs/arquitetura-e-padroes.md)
- [Declaração de IA, componentes, fontes e licenças](docs/declaracao-ia-terceiros.md)

## Equipe

- Filipe Ribeiro Marques
- Luigi Stefano Vianna de Araújo Darzé
- Mikael Souza Barbosa
- Robert Wagner Lima Ramos Junior
- Victor Amadeu Sant Anna de Medeiros

**Turma** ADS-NOT-PIT-4S-T1 — 54942.
