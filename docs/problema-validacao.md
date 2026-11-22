# Problema e Validação — MeNota

## Anexo A — Ficha do problema

**Solução:** MeNota  
**Equipe, turma e integrantes:** Turma ADS-NOT-PIT-4S-T1 — 54942
- Filipe Ribeiro Marques
- Luigi Stefano Vianna de Araújo Darzé
- Mikael Souza Barbosa
- Robert Wagner Lima Ramos Junior
- Victor Amadeu Sant Anna de Medeiros

### Formulação do problema

> **Para** estudantes universitários em acompanhamento psicológico ou psiquiátrico (com foco no contexto de serviços de apoio psicológico acadêmico, como o da UCSAL) e os profissionais que os atendem, **que enfrentam** dificuldade para lembrar, organizar e verbalizar acontecimentos e sentimentos vividos no intervalo entre consultas **durante** sessões com tempo limitado e frequentemente espaçadas, **a consequência é** a perda de informações relevantes, menor aproveitamento do tempo de consulta, dificuldade de autopercepção ao longo do tempo e maior esforço na retomada de contexto em cada encontro. **Hoje, esse público tenta resolver a situação por meio de** relatos feitos exclusivamente de memória na hora da consulta ou anotações esporádicas e desestruturadas em cadernos ou blocos de notas no celular, sem consolidação por período nem compartilhamento controlado.

| Campo | Preenchimento |
| :--- | :--- |
| **Público** | Estudantes universitários em acompanhamento psicológico ou psiquiátrico (como usuários de serviços de apoio psicológico acadêmico) e os psicólogos/psiquiatras que os atendem. |
| **Situação** | O problema é vivido no intervalo entre sessões (cotidiano acadêmico) e no momento das consultas, que possuem duração curta (tipicamente 50 minutos) e intervalos que com frequência chegam a 15 ou 30 dias. |
| **Problema** | O estudante tenta recordar e expressar o que viveu e sentiu nas últimas semanas, mas o esquecimento, o viés de memória, a timidez e a sobrecarga acadêmica dificultam um relato completo. O profissional, por sua vez, dispõe de pouco tempo para reconstruir o contexto do período antes de aprofundar o atendimento. |
| **Impacto** | Perda de vivências relevantes para o acompanhamento, menor aproveitamento do tempo de sessão, dificuldade de perceber recorrências e contrastes emocionais ao longo das semanas e retrabalho na retomada de contexto. |
| **Alternativa atual** | Relatos feitos apenas de memória durante a sessão ou anotações soltas e irregulares em cadernos físicos e aplicativos genéricos de notas, difíceis de revisar por período e sem controle de compartilhamento. |
| **Hipótese crítica** | Estudantes adotam uma agenda reflexiva simples para registrar relatos no dia a dia, consideram úteis os resumos descritivos por período (7 ou 30 dias, sem diagnósticos ou recomendações clínicas) para sua autopercepção, e aceitam compartilhar esse material com o profissional mediante autorização explícita e revogável. |
| **Evidência** | Combinação de levantamento bibliográfico em cinco estudos empíricos/clínicos (incluindo ACM CHI, *Australian Psychologist*, *Annual Review of Clinical Psychology*, *Research in Psychotherapy* e relato brasileiro em clínica-escola universitária) com relatos anonimizados coletados junto a estudantes em terapia e profissionais de saúde mental. |
| **Recorte** | MVP web local (`MeNota`) com contas e relatos estritamente fictícios, contemplando: registro diário de anotações com data automática, geração de resumos descritivos de 7 ou 30 dias via Gemini API (sem diagnósticos ou recomendações) e controle de autorização/revogação pelo paciente para consulta de notas e resumos pelo profissional. |

---

## Anexo B — Registro de validação

**Solução:** MeNota

### 1. Evidências documentais e literatura científica

| Data e perfil | Hipótese | Evidência observada | Decisão |
| :--- | :--- | :--- | :--- |
| 21/11/2026 — Literatura científica: Oewel, Areán & Agapie (2024), ACM CHI '24 (entrevistas com 13 terapeutas e 14 pacientes) | O intervalo entre sessões envolve barreiras de tempo, vergonha e falta de estrutura; recomendações automáticas trazem risco. | O estudo identificou três barreiras principais entre sessões: desalinhamento de esforço, sobrecarga cotidiana/transições acadêmicas e desconforto/vergonha ao verbalizar sentimentos. Alertou ainda que recomendações automatizadas inadequadas podem piorar o bem-estar do paciente. | Manter escrita livre e rápida de baixo atrito; restringir o prompt da IA no `MeNota` a sumarização estritamente descritiva, proibindo diagnósticos, conselhos ou recomendações. |
| 21/11/2026 — Literatura científica: Keane & Smout (2025), *Australian Psychologist* (survey com 246 psicólogos e 16 entrevistas) | Custo, agenda e filas de espera aumentam o intervalo entre sessões, ampliando o esquecimento e a escassez de tempo clínico. | 86,6% dos clínicos apontaram custo e 37,8% restrições de tempo do cliente como barreiras; 82,1% dos atendimentos não graves ocorrem de forma quinzenal (52,8%) ou mensal (29,3%). Clínicas universitárias também enfrentam alta demanda e dificuldade para manter frequência semanal. | Definir os recortes de resumo do MVP em **7 e 30 dias** para cobrir desde acompanhamentos semanais até intervalos quinzenais e mensais, oferecendo contexto rápido ao profissional. |
| 21/11/2026 — Literatura científica: Shiffman, Stone & Hufford (2008), *Annual Review of Clinical Psychology* | Relatos feitos apenas de memória na consulta sofrem distorção e cadernos físicos têm baixa adesão pontual. | Revisão demonstra que relatos retrospectivos sofrem de viés de memória (*recall bias*) influenciado pelo humor do dia ("fotografia" vs. "filme"). Em estudos revisados, diários em papel tiveram apenas 11% de adesão real pontual (por preenchimento retroativo), contra 94% em diários digitais com marcação automática de data. | Adotar registro digital com **data automática do servidor** (`LocalDate.now()`), priorizando simplicidade de uso diário sobre formulários extensos. |
| 21/11/2026 — Literatura científica: Faccio, Turco & Iudici (2019), *Research in Psychotherapy* (estudo longitudinal de 2,5 anos) | O registro longitudinal apoia tanto a autopercepção do paciente quanto o compartilhamento com o terapeuta. | O uso contínuo do diário desenvolveu distanciamento reflexivo, reduziu o bloqueio por medo de julgamento e facilitou compartilhar pensamentos com o terapeuta. O artigo também alerta que escrever sem organizar/consolidar pode reforçar ruminação negativa. | Estruturar o fluxo com visão consolidada por período (resumo integrando acontecimentos, contrastes e recorrências) e área de consulta autorizada para o profissional. |
| 21/11/2026 — Literatura científica: Pereira & Sutil (2017), *Revista IGT na Rede* (estudo de caso em clínica-escola universitária brasileira) | Estudantes em clínicas-escola têm dificuldade de verbalização em sessões de 50 minutos e anotações esporádicas em caderno não bastam. | Em clínica-escola universitária (sessões de 50 min), a jovem paciente chegava em "modo automático" e relatava vergonha de escrever "não bonito". O diário com orientação simples facilitou o relato de eventos cotidianos e evidenciou conquistas, sem cobrança punitiva nas semanas de exaustão. | Adotar interface acolhedora, escrita livre sem exigência de formato e papel estritamente complementar ao acompanhamento humano. |

### 2. Evidências primárias — conversas e percepções (perfis anonimizados)

> *Nota ética:* Todos os relatos abaixo referem-se exclusivamente à percepção sobre a proposta e o fluxo de uso em contexto acadêmico, sem coleta de nomes reais, prontuários ou conteúdos clínicos pessoais.

| Data e perfil | Hipótese | Evidência observada | Decisão |
| :--- | :--- | :--- | :--- |
| `[DD/11/2026 — Profissional de psicologia, perfil anônimo]` | O resumo por período ajuda a ganhar contexto em sessões curtas sem substituir o atendimento. | `[Preencher com o que foi dito na conversa — incluir também ressalvas éticas ou críticas apontadas]` | `[Preencher decisão tomada pela equipe]` |
| `[DD/11/2026 — Profissional de psiquiatria/psicologia, perfil anônimo]` | O controle de autorização pelo paciente e a ausência de diagnósticos pela IA são condições essenciais de confiança. | `[Preencher com o que foi dito na conversa]` | `[Preencher decisão tomada pela equipe]` |
| `[DD/11/2026 — Estudante universitário(a) em acompanhamento terapêutico, perfil anônimo]` | O esquecimento e a dificuldade de verbalizar entre consultas ocorrem na prática e o diário com resumo ajudaria. | `[Preencher com o que foi dito na conversa]` | `[Preencher decisão tomada pela equipe]` |
| `[DD/11/2026 — Estudante universitário(a) em acompanhamento terapêutico, perfil anônimo]` | O paciente prefere controlar se e quando o profissional acessa os relatos. | `[Preencher com o que foi dito na conversa]` | `[Preencher decisão tomada pela equipe]` |

**Síntese dos aprendizados:**  
As evidências da literatura confirmaram que o problema central não é a falta de um aplicativo genérico, mas a lacuna entre sessões espaçadas (frequentemente quinzenais ou mensais por custo, agenda e filas em serviços universitários) somada ao viés de memória e à barreira emocional para verbalizar vivências em consultas curtas. Também ficou evidente que a IA não pode assumir papel prescritivo ou diagnóstico, devendo restringir-se à síntese fiel dos relatos, e que a privacidade com controle explícito do paciente é inegociável. `[Complementar em 2 linhas após inserir os relatos primários coletados pelo Robert.]`

**Mudanças realizadas no problema ou na solução:**
1. **Recorte de períodos (7 e 30 dias):** Definido diretamente em resposta aos intervalos semanais, quinzenais e mensais observados na prática clínica.
2. **Controle explícito de acesso (autorização e revogação):** Implementado para que o paciente escolha quem pode acompanhar seus relatos e possa revogar a permissão a qualquer instante, inclusive bloqueando resultados caso a revogação ocorra durante a geração de um resumo.
3. **Limites rígidos na sumarização por IA:** As instruções de sistema da Gemini API foram redigidas para impedir diagnósticos, recomendações clínicas, julgamentos ou execução de instruções injetadas nos relatos.
4. **Simplificação do escopo do MVP:** Recursos secundários (cadastro público, edição/exclusão de notas e histórico persistente de resumos) foram deixados fora do MVP para garantir estabilidade e clareza do fluxo principal até o prazo de entrega.

---

### Referências bibliográficas (ABNT)

- FACCIO, Elena; TURCO, Francesca; IUDICI, Antonio. Self-writing as a tool for change: the effectiveness of a psychotherapy using diary. *Research in Psychotherapy: Psychopathology, Process and Outcome*, v. 22, n. 2, p. 256–264, 2019. DOI: `10.4081/ripppo.2019.378`.
- KEANE, Imogen; SMOUT, Matthew Franklin. Session frequency in routine psychology practice and perspectives on session scheduling: a mixed-methods study of clinician attitudes and practices. *Australian Psychologist*, v. 60, n. 2, p. 99–111, 2025. DOI: `10.1080/00050067.2024.2415066`.
- OEWEL, Bruna; AREÁN, Patricia A.; AGAPIE, Elena. Approaches to Tailoring Between-Session Mental Health Therapy Activities. In: *CHI CONFERENCE ON HUMAN FACTORS IN COMPUTING SYSTEMS (CHI '24)*, 2024, Honolulu. Proceedings [...]. New York: ACM, 2024. p. 1–19. DOI: `10.1145/3613904.3642856`.
- PEREIRA, Fábio Nogueira; SUTIL, Marcio Vinicio do Nascimento. Um relato de experiência sobre o uso de diários como recurso terapêutico na abordagem gestáltica. *Revista IGT na Rede*, Rio de Janeiro, v. 14, n. 27, p. 291–314, 2017. ISSN 1807-2526.
- SHIFFMAN, Saul; STONE, Arthur A.; HUFFORD, Michael R. Ecological Momentary Assessment. *Annual Review of Clinical Psychology*, v. 4, p. 1–32, 2008. DOI: `10.1146/annurev.clinpsy.3.022806.091415`.