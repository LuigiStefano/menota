# Anexo C — Definição do MVP do MeNota

**Usuário principal** Estudante universitário em acompanhamento psicológico ou psiquiátrico. O profissional participa quando o paciente autoriza a leitura.

**Resultado principal** Registrar experiências em uma agenda, recuperar páginas por data e organizar o que o paciente deseja retomar em consulta. O paciente pode gerar uma análise descritiva e autorizar uma conta profissional a lê-la junto dos registros do intervalo.

## Fluxo de ponta a ponta

1. O paciente cria conta com pseudônimo, senha e perfil. Após entrar, escolhe uma data e salva um relato. Humor e energia são opcionais.
2. A agenda marca a data salva. O paciente pode consultar, editar ou excluir a página, com validação de entrada e controle de versão.
3. Em **Acompanhamento**, o paciente seleciona até 31 dias e confirma o envio de dados fictícios. O servidor seleciona os registros da conta, usa o Gemini para gerar uma síntese e verifica as emoções e evidências retornadas. Armazena a análise cifrada e a disponibiliza no histórico.
4. Em **Compartilhar**, o paciente confirma acesso para o usuário exato de uma conta profissional. O profissional entra em sua conta, escolhe o paciente autorizado e consulta os registros e as análises disponíveis no período. A remoção do vínculo bloqueia novas consultas.

A interface de demonstração é a aplicação principal, em `127.0.0.1:8080`. O módulo separado em `8081` permanece disponível para instalações que utilizavam a agenda por e-mail.

## Critérios de pronto

- Outra pessoa consegue instalar e executar pelo README, criar os perfis e demonstrar o fluxo com dados fictícios.
- O diário aceita texto válido, rejeita entradas inválidas, protege o rascunho diante de falha recuperável e evita sobrescritas concorrentes.
- O servidor usa apenas fontes da conta e do intervalo selecionado. Se a resposta do Gemini estiver inválida ou as fontes mudarem antes de salvar, o histórico permanece íntegro.
- Sem autorização ativa, o profissional não consulta o paciente. Com autorização, consulta os dados do período em modo de leitura, inclusive a análise já gerada pelo paciente.
- Há roteiro curto de demonstração e instruções para conferir as suítes automatizadas, os limites e a contingência do Gemini.

## Riscos, teste e limitações

**Maior risco** O Gemini pode ficar indisponível, atingir a cota ou produzir uma interpretação imprecisa. Prepare uma análise fictícia já gerada e a contingência da apresentação. Confira o texto original ao demonstrar emoções e indique as incertezas.

**Teste principal** Com contas fictícias distintas, salve e retome uma página, gere ou consulte uma análise preparada, conceda acesso ao profissional e confirme que a revogação bloqueia a nova consulta. Testes de integração com PostgreSQL e Gemini simulado verificam limites, isolamento, descarte de resultados e revogação. As entrevistas anteriores avaliaram a proposta e o problema. A validação de uso com o público na interface final é um passo distinto.

**Fora do escopo** Recuperação de senha, verificação de identidade ou registro profissional, prontuário, agendamento, medição de risco clínico, prescrição e hospedagem pública. O cadastro profissional é autodeclarado. A autorização atual abrange todos os registros e análises, enquanto estiver ativa. O compartilhamento seletivo é uma hipótese futura, e as análises disponíveis dependem das fontes salvas.

[Instalação e roteiro](../README.md#roteiro-curto-de-demonstração) · [Arquitetura e padrões](arquitetura-e-padroes.md)
