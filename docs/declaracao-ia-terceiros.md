# Anexo E — Declaração de uso de IA e de componentes de terceiros

**Solução** MeNota

**Turma** ADS-NOT-PIT-4S-T1 — 54942

**Equipe** MeNota

**Integrantes** Filipe Ribeiro Marques, Luigi Stefano Vianna de Araújo Darzé, Mikael Souza Barbosa, Robert Wagner Lima Ramos Junior e Victor Amadeu Sant Anna de Medeiros.

## 1. Uso de inteligência artificial

**Houve uso de IA?** (X) Sim ( ) Não

| Ferramenta ou modelo | Finalidade | Parte afetada | Revisão e validação |
| --- | --- | --- | --- |
| ChatGPT / Codex (OpenAI) | Apoio à escrita e revisão de código, testes e documentação técnica | Java, HTML, CSS, JavaScript, scripts, suítes automatizadas, README e documentação | Compilação, testes com PostgreSQL, verificações no navegador e inspeção dos fluxos e limites. A equipe é responsável por revisar e explicar as decisões. |
| Gemini (assistente de texto) | Apoio ao levantamento inicial de referências e à revisão dos anexos | Problema, validação e documentação acadêmica | As referências da validação estão listadas no Anexo B. Conversas e aprendizados registrados nesse anexo são evidências humanas. |
| Gemini Developer API | Sumarização de páginas fictícias e identificação de emoções sustentadas por trechos dos registros | Adapter Gemini do aplicativo principal e sumarização da agenda de compatibilidade | Testes automatizados usam respostas simuladas e um servidor HTTP local. O código verifica formato, categorias, datas e trechos literais. |
| Ferramenta de geração de imagens | Apoio à criação da identidade visual | Logo do MeNota | Seleção visual e conferência de apresentação nas telas. |

Na demonstração, use apenas dados fictícios ao chamar a API. Os resultados gerados por IA são usados como saída do aplicativo, e as evidências sobre o público são registradas pelas pessoas que conduziram a validação.

## 2. Componentes e recursos de terceiros

**Houve uso de componentes de terceiros?** (X) Sim ( ) Não

| Componente | Versão ou licença | Função |
| --- | --- | --- |
| [Spring Boot e Spring Framework](https://spring.io/projects/spring-boot) | Boot 3.5.16 na aplicação principal e 4.2.0 na compatibilidade. Apache-2.0 | Servidor, MVC, segurança, validação e acesso a dados. |
| [Thymeleaf](https://www.thymeleaf.org/) | Gerenciado pelo Spring Boot. Apache-2.0 | Renderização dos templates HTML. |
| [PostgreSQL](https://www.postgresql.org/about/licence/) e [driver JDBC](https://jdbc.postgresql.org/about/license.html) | PostgreSQL 18. PostgreSQL License e BSD-2-Clause | Banco e conexão SQL. |
| [Hibernate ORM](https://hibernate.org/orm/) | Gerenciado pelo Spring Boot. LGPL-2.1-or-later | Mapeamento de entidades na aplicação principal com Spring Data JPA. |
| [Apache Maven Wrapper](https://maven.apache.org/wrapper/) | Wrapper 3.3.4, Maven 3.10.1. Apache-2.0 | Resolução de dependências, execução e empacotamento. |
| [JUnit](https://junit.org/) e [Mockito](https://site.mockito.org/) | Gerenciados pelo Spring Boot. EPL-2.0 e MIT | Testes unitários e de integração. |
| [Gemini Developer API](https://aistudio.google.com/docs/models/gemini-3.5-flash-lite) | Modelo `gemini-3.5-flash-lite`, sujeito aos termos do Google | Geração de análises e resumos. |
| [Caveat](https://github.com/google/fonts/tree/main/ofl/caveat) e [Nunito](https://github.com/google/fonts/tree/main/ofl/nunito) | SIL Open Font License 1.1 | Tipografia servida a partir dos arquivos locais, com licenças no repositório. |

Os arquivos `pom.xml` registram as dependências e as versões do Spring que as gerenciam. A base da agenda de compatibilidade foi configurada a partir do [Spring Initializr](https://start.spring.io/).

## 3. Materiais ou código preexistentes

A equipe declara que desenvolveu desde a base o código próprio da aplicação, com apoio de IA na escrita. O histórico autoral registra o trabalho da equipe no [diário e seu acompanhamento](https://github.com/LuigiStefano/diario-reflexivo/commit/f5ad40a) e no [MeNota](https://github.com/LuigiStefano/menota). As referências acima identificam bibliotecas, fontes e serviços externos utilizados.

**Material produzido antes do POOALAB** Segundo a equipe, o código próprio foi desenvolvido no projeto acadêmico. Para a versão final, a equipe deve confirmar esta informação junto com a ciência dos integrantes.

**Contribuição da equipe** Delimitação do problema, validação, agenda com calendário e autorrelato, armazenamento cifrado, análise por período, autorização e revogação, fluxo profissional, interface responsiva, testes e documentação.

## 4. Declaração e ciência

A equipe é responsável pela exatidão destas informações, pela revisão e pelos testes do material utilizado e pela sua explicação na avaliação. O responsável e a ciência da versão final devem ser confirmados pelos integrantes na submissão.

| Integrante | Ciência registrada para a declaração anterior |
| --- | --- |
| Filipe Ribeiro Marques | Pendente |
| Luigi Stefano Vianna de Araújo Darzé | 22/11/2026 |
| Mikael Souza Barbosa | Pendente |
| Robert Wagner Lima Ramos Junior | 21/11/2026 |
| Victor Amadeu Sant Anna de Medeiros | 21/11/2026 |

**Responsável pela submissão** A confirmar pela equipe.
