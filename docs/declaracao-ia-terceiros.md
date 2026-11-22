# Anexo E — Declaração de uso de inteligência artificial e de componentes de terceiros

**Solução:** MeNota  
**Turma:** ADS-NOT-PIT-4S-T1 — 54942  
**Equipe:** Equipe MeNota  
**Integrantes:** Filipe Ribeiro Marques; Luigi Stefano Vianna de Araújo Darzé; Mikael Souza Barbosa; Robert Wagner Lima Ramos Junior; Victor Amadeu Sant Anna de Medeiros

---

## 1. Uso de inteligência artificial

**Houve uso de IA?** (X) Sim ( ) Não

| Ferramenta ou modelo | Finalidade ou tarefa | Parte afetada | Revisão e validação realizadas |
| :--- | :--- | :--- | :--- |
| **ChatGPT / Codex (OpenAI)** | Apoio à estruturação, escrita e revisão de código Java/Spring/HTML/CSS, testes unitários e `README.md`. | `src/main/`, `src/test/`, `pom.xml` e `README.md`. | Todo o código foi lido, compilado e executado pela equipe em mais de uma máquina (ambiente limpo com PostgreSQL 18 e JDK 21), com 5 testes automatizados JUnit/Mockito aprovados e verificação manual do fluxo completo. |
| **Google Gemini (assistente de texto)** | Apoio no levantamento inicial de referências acadêmicas, revisão de coerência entre código e documentação e estruturação dos anexos conforme o manual POOALAB. | `docs/problema-validacao.md`, `docs/definicao-mvp.md` e `docs/declaracao-ia-terceiros.md`. | Todos os cinco artigos científicos citados foram localizados, conferidos diretamente pela equipe (DOIs, autores, páginas e citações) e fichados antes da incorporação; os textos dos anexos foram revisados e ajustados pela equipe. |
| **Google Gemini API (`gemini-3.5-flash-lite`)** | Componente de execução do próprio MVP para sumarização descritiva das anotações do período selecionado (7 ou 30 dias). | `src/main/java/menota/resumo/GeminiSumarizador.java`. | As instruções de sistema foram revisadas para restringir a saída a resumo descritivo (sem diagnósticos ou recomendações). O recurso foi testado exclusivamente com relatos fictícios. |

**Observações sobre o uso de IA:**  
Nenhuma saída de IA foi apresentada como evidência de validação com usuários reais. Nenhum dado pessoal, prontuário real, senha ou chave de API foi enviado a ferramentas de IA ou versionado no repositório.

---

## 2. Componentes e recursos de terceiros

**Houve uso de componentes de terceiros?** (X) Sim ( ) Não

| Componente ou recurso | Origem ou link | Versão e licença | Função e alterações realizadas |
| :--- | :--- | :--- | :--- |
| **Spring Initializr** | https://start.spring.io/ | Serviço web / Apache-2.0 | Geração da estrutura inicial do projeto Maven/Spring Boot; configurado e preenchido pela equipe. |
| **Spring Boot (`jdbc`, `security`, `thymeleaf`, `validation`, `webmvc`)** | https://spring.io/projects/spring-boot | 4.2.0 — Apache-2.0 | Servidor web embutido, MVC, acesso JDBC, validação e autenticação/autorização por perfil; usado sem alteração no código da biblioteca. |
| **Thymeleaf e `thymeleaf-extras-springsecurity6`** | https://www.thymeleaf.org/ | Gerenciado pelo Spring Boot 4.2.0 — Apache-2.0 | Renderização server-side dos templates HTML (`src/main/resources/templates/`). |
| **PostgreSQL e Driver PostgreSQL JDBC** | https://www.postgresql.org/ / https://jdbc.postgresql.org/ | PostgreSQL 18 / Driver JDBC gerenciado pelo BOM — PostgreSQL License / BSD-2-Clause | Persistência relacional de usuários, notas e autorizações. |
| **JUnit e Mockito** | https://junit.org/ / https://site.mockito.org/ | Gerenciados pelo Spring Boot 4.2.0 — EPL-2.0 / MIT | Testes automatizados unitários e de inicialização do contexto Spring (`src/test/`). |
| **Apache Maven Wrapper** | https://maven.apache.org/wrapper/ | Wrapper 3.3.4 / Maven 3.10.1 — Apache-2.0 | Padronização do build e resolução de dependências (`mvnw`, `mvnw.cmd`). |
| **Google Gemini Developer API** | https://aistudio.google.com/docs | Modelo `gemini-3.5-flash-lite` — Termos de Serviço da Gemini API | Serviço externo consumido via HTTP (`RestClient`) para gerar resumos por período a partir de relatos fictícios. |
| **Fontes Caveat e Nunito** | https://github.com/googlefonts/caveat e https://github.com/googlefonts/nunito | Variáveis TTF — SIL Open Font License (OFL) 1.1 | Tipografia local da interface (`src/main/resources/static/fonts/`), mantendo os arquivos de licença OFL no repositório. |

---

## 3. Materiais ou código preexistentes

**Houve material produzido antes do PooaLAB?** ( ) Sim (X) Não

**Identificação e origem:**  
Não se aplica quanto a código, banco ou documentos. A temática geral dialoga com uma ideia inicial de estudo da equipe, mas todo o código-fonte, esquema SQL, testes e documentação do **MeNota** foram produzidos do zero durante o POOALAB 2026.2.

**Contribuição nova realizada pela equipe:**  
Modelagem do problema e levantamento de evidências, especificação do MVP, implementação completa da aplicação web `MeNota` (perfis paciente/profissional, agenda, sumarização via Gemini API e controle de autorização/revogação), testes automatizados, documentação e pitch.

---

## 4. Declaração da equipe

Declaramos que as informações acima são completas e verdadeiras; que revisamos e testamos as contribuições apoiadas por IA; que identificamos as fontes, licenças e partes preexistentes relevantes; que não apresentamos como autoria própria material de terceiros; e que não compartilhamos dados pessoais, credenciais ou informações restritas de forma indevida.

**Responsável pela submissão:** `[Inserir nome completo de quem enviará no Google Classroom]`

### Ciência dos integrantes

| Nome completo | Assinatura ou registro de ciência |
| :--- | :--- |
| Filipe Ribeiro Marques | `[Ciente — __/__/2026]` |
| Luigi Stefano Vianna de Araújo Darzé | `[Ciente — __/__/2026]` |
| Mikael Souza Barbosa | `[Ciente — __/__/2026]` |
| Robert Wagner Lima Ramos Junior | `Ciente — 21/11/2026` |
| Victor Amadeu Sant Anna de Medeiros | `Ciente — 21/11/2026` |

**Data:** 21/11/2026
