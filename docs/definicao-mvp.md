# Definição do MVP — MeNota

## Anexo C — Definição do MVP

**Solução:** MeNota

**Usuário principal:**  
Estudante universitário (perfil `PACIENTE`) em acompanhamento psicológico ou psiquiátrico, com fluxo complementar de consulta pelo profissional de saúde mental (perfil `PROFISSIONAL`) mediante autorização explícita do paciente.

**Resultado principal:**  
O paciente consegue registrar acontecimentos e sentimentos do dia a dia, gerar um resumo descritivo dos últimos 7 ou 30 dias para apoiar sua autopercepção e conceder ou revogar o acesso de um profissional autorizado para consultar suas anotações e gerar o mesmo resumo antes ou durante o acompanhamento.

**Fluxo de ponta a ponta:**
1. **Registro e resumo pelo paciente:** Autenticação (`paciente@menota.test`) → escrita e envio de relato na agenda (`POST /paciente/notas`) → validação e persistência no PostgreSQL via JDBC (`NotaService` → `NotaRepository`) → seleção do período de 7 ou 30 dias (`POST /paciente/resumo`) → recuperação das notas do período e chamada à Gemini API (`ResumoService` → `Sumarizador` / `GeminiSumarizador`) → exibição do resumo descritivo na tela.
2. **Compartilhamento controlado com o profissional:** Paciente informa o e-mail do profissional em *Permissões de acesso* (`POST /paciente/autorizacoes`) → profissional entra na conta (`profissional@menota.test`), visualiza o paciente autorizado (`GET /profissional`), lê as notas (`GET /profissional/pacientes/{id}`) e gera o resumo de 7 ou 30 dias (`POST /profissional/pacientes/{id}/resumo`) → paciente pode revogar o acesso a qualquer momento (`POST /paciente/autorizacoes/{id}/revogar`), impedindo novas consultas.

**Critérios de pronto:**
- A aplicação compila e inicia localmente na porta `8081` com Java 21 e PostgreSQL 18, criando automaticamente o esquema (`schema.sql`) e as contas fictícias de demonstração (`DadosDemonstracao`).
- O paciente autenticado registra relatos válidos (rejeitando texto vazio) e visualiza a listagem cronológica decrescente.
- A geração de resumo para 7 ou 30 dias retorna síntese em português brasileiro sem diagnósticos, conselhos ou execução de comandos embutidos no texto, respeitando o limite de 40.000 caracteres e exibindo aviso claro quando não há notas no período.
- O controle de acesso (Spring Security + `ProfissionalService`) bloqueia acesso entre perfis (`403`) e impede leitura ou sumarização sem autorização ativa ou após revogação.
- A suíte automatizada (`MenotaApplicationTests` e `ProfissionalServiceTest`) executa sem falhas (5 testes aprovados) e o fluxo completo é reproduzível em máquina diferente do ambiente original seguindo apenas o `README.md`.

**Fora do escopo:**
- Cadastro público de novos usuários e recuperação de senha.
- Edição ou exclusão de anotações já registradas.
- Persistência de histórico de resumos gerados e seleção de intervalos personalizados de datas.
- Prontuário eletrônico, agendamento de consultas ou integração com sistemas externos.
- Hospedagem em produção na nuvem (o MVP opera localmente em `127.0.0.1:8081`).

**Maior risco:**
1. **Risco técnico/externo:** Indisponibilidade, lentidão ou esgotamento de cota da Gemini API durante a demonstração ao vivo.
2. **Risco ético e de privacidade:** Acesso indevido a relatos sensíveis ou extrapolação clínica pela IA (emissão de diagnósticos/conselhos). Mitigado no MVP pelo uso exclusivo de contas e relatos fictícios, checagem dupla de autorização em `ProfissionalService.gerarResumo` e instruções restritivas de sistema em `GeminiSumarizador`.

**Teste principal:**
- **Testes unitários orientados a risco (`ProfissionalServiceTest` — JUnit + Mockito):** validam o bloqueio de leitura de notas sem autorização, bloqueio de geração de resumo sem autorização, consulta bem-sucedida quando autorizado e descarte/bloqueio do resumo caso o acesso seja revogado *durante* o processamento.
- **Teste de integração/contexto (`MenotaApplicationTests`):** valida a inicialização completa do Spring Boot com o banco PostgreSQL configurado.
- **Teste funcional ponta a ponta e de reprodutibilidade:** execução do fluxo completo (paciente + resumo + autorização + profissional + revogação) em máquina limpa diferente da de desenvolvimento.

**Limitações conhecidas:**
- Contas previamente cadastradas apenas no perfil `demo`, sem cadastro aberto ou recuperação de senha.
- Notas registradas automaticamente na data do servidor, sem edição ou exclusão.
- Resumos restritos a janelas de 7 ou 30 dias e gerados sob demanda, sem armazenamento em histórico.
- A geração de resumos depende de conexão com a internet e da disponibilidade/cotas da Gemini API.
- Execução local (`localhost:8081`); a revogação impede novas consultas, mas não apaga informações que tenham sido visualizadas anteriormente.

---

## Relação com os conteúdos de POOA (visível no código)

A arquitetura do **MeNota** aplica conteúdos da disciplina de forma proporcional ao escopo do MVP, sem inserções artificiais:

| Conceito de POOA | Onde aparece no código | Justificativa técnica |
| :--- | :--- | :--- |
| **SOLID — Responsabilidade Única (SRP)** | Separação entre `controller/` (fluxo web/HTTP), `service/` (regras de negócio e autorização), `repository/` (acesso SQL) e `resumo/` (integração com IA). | Isola mudanças de interface, regras de acesso e persistência em camadas coesas e fáceis de testar. |
| **SOLID — Inversão de Dependência (DIP)** | `ResumoService` depende da interface `menota.resumo.Sumarizador`, recebida por injeção no construtor, e não da classe concreta `GeminiSumarizador`. | Permite trocar o provedor de IA ou simular o sumarizador em testes sem alterar a regra de negócio de `ResumoService`. |
| **Padrão GoF — Adapter** | `GeminiSumarizador` implementa `Sumarizador`. | Adapta o protocolo REST/JSON da Gemini API (`RestClient`, cabeçalho `x-goog-api-key`, `systemInstruction` e parsing de `JsonNode`) para o contrato simples `String resumir(String relatos)` esperado pelo domínio. |
| **Persistência relacional com JDBC** | `UsuarioRepository`, `NotaRepository` e `AutorizacaoRepository` com `JdbcTemplate` e `schema.sql`. | Mantém consultas SQL explícitas, parametrizadas (`?`) contra SQL Injection e mapeadas diretamente para `record`s imutáveis do Java 21 (`Nota`, `Usuario`, `Paciente`, `Profissional`, `Resumo`). |
| **Testes automatizados com isolamento de dependências** | `ProfissionalServiceTest` (JUnit 5 + Mockito). | Valida os cenários críticos de autorização e revogação sem depender da rede nem da Gemini API. |