'use strict';
const contaAcompanhamento = document.querySelector('meta[name="usuario"]').content;
const tokenAcompanhamento = document.querySelector('meta[name="csrf-token"]').content;
const headerAcompanhamento = document.querySelector('meta[name="csrf-header"]').content;
async function acessoApi(caminho, opcoes = {}) {
  const resposta = await fetch(`/api/acompanhamento${caminho}`, {
    ...opcoes, credentials: 'same-origin', cache: 'no-store', signal: AbortSignal.timeout(15000),
    headers: {'Accept':'application/json', 'X-Diario-Usuario':contaAcompanhamento, [headerAcompanhamento]:tokenAcompanhamento, ...opcoes.headers}
  });
  if (resposta.status === 401 || resposta.status === 403) throw Error('Entre novamente para continuar.');
  if (resposta.status === 204) return null;
  const dados = await resposta.json().catch(() => ({}));
  if (!resposta.ok) throw Error(dados.erro || 'Não foi possível concluir. Tente novamente.');
  return dados;
}
function erroAcesso(erro) { return erro.name === 'TypeError' || erro.name === 'TimeoutError' ? 'Confira a conexão e tente novamente.' : erro.message; }
function dataAcompanhamento(data) { return new Intl.DateTimeFormat('pt-BR').format(new Date(`${data}T12:00:00`)); }

async function areaPaciente() {
  const estado = document.querySelector('#acesso-estado');
  const botao = document.querySelector('#autorizar');
  async function carregar() {
    const contas = await acessoApi('/acessos');
    const lista = document.querySelector('#acessos'); lista.replaceChildren();
    for (const conta of contas) {
      const item = document.createElement('li');
      const nome = document.createElement('p'); nome.textContent = conta.login;
      const remover = document.createElement('button'); remover.type = 'button'; remover.className = 'quiet danger'; remover.textContent = 'Remover acesso';
      remover.addEventListener('click', async () => {
        const dialogo = document.querySelector('#revogar-dialogo');
        document.querySelector('#revogar-texto').textContent = `${conta.login} deixará de consultar seus registros e análises.`;
        dialogo.returnValue = 'cancelar';
        const confirmado = new Promise(resolve => dialogo.addEventListener('close', () => resolve(dialogo.returnValue === 'remover'), {once:true}));
        dialogo.showModal(); dialogo.querySelector('[value="cancelar"]').focus();
        if (!await confirmado) return;
        remover.disabled = true;
        try { await acessoApi(`/acessos/${encodeURIComponent(conta.login)}`, {method:'DELETE'}); await carregar(); estado.textContent = 'Acesso removido.'; }
        catch (erro) { estado.textContent = erroAcesso(erro); remover.disabled = false; }
      });
      item.append(nome, remover); lista.append(item);
    }
    estado.textContent = contas.length ? 'Estas contas podem consultar seus registros.' : 'Você ainda não compartilhou seus registros com ninguém.';
    botao.disabled = false;
  }
  document.querySelector('#acesso-form').addEventListener('submit', async event => {
    event.preventDefault(); if (botao.disabled || !event.target.reportValidity()) return;
    botao.disabled = true;
    try {
      await acessoApi('/acessos', {method:'POST', headers:{'Content-Type':'application/json'}, body:JSON.stringify({profissional:document.querySelector('#profissional').value, autorizo:document.querySelector('#autorizo').checked})});
      event.target.reset(); await carregar(); estado.textContent = 'Acesso autorizado.';
    } catch (erro) { estado.textContent = erroAcesso(erro); }
    finally { botao.disabled = false; }
  });
  try { await carregar(); } catch (erro) { estado.textContent = erroAcesso(erro); }
}

async function areaProfissional() {
  const paciente = document.querySelector('#paciente');
  const estado = document.querySelector('#consulta-estado');
  const botao = document.querySelector('#consultar');
  const painel = document.querySelector('#painel-paciente');
  const hoje = new Date();
  const iso = d => `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;
  document.querySelector('#fim').value = iso(hoje); hoje.setDate(hoje.getDate()-6); document.querySelector('#inicio').value = iso(hoje);
  function limpar() { painel.hidden = true; document.querySelector('#registros-paciente').replaceChildren(); document.querySelector('#analises-paciente').replaceChildren(); }
  document.querySelector('#consulta-form').addEventListener('change', limpar);
  document.querySelector('#consulta-form').addEventListener('submit', async event => {
    event.preventDefault(); if (botao.disabled || !event.target.reportValidity()) return;
    limpar(); botao.disabled = true; paciente.disabled = true; estado.textContent = 'Consultando…';
    const inicio = document.querySelector('#inicio').value, fim = document.querySelector('#fim').value;
    document.querySelector('#inicio').disabled = true; document.querySelector('#fim').disabled = true;
    try {
      const dados = await acessoApi(`/pacientes/${encodeURIComponent(paciente.value)}?inicio=${inicio}&fim=${fim}`);
      const registros = document.querySelector('#registros-paciente');
      for (const pagina of dados.registros) {
        const detalhe = document.createElement('details');
        const titulo = document.createElement('summary'); titulo.textContent = dataAcompanhamento(pagina.data);
        const autorrelato = textoBemEstar(pagina.bemEstar);
        if (autorrelato) {
          const indicador = document.createElement('span'); indicador.className = 'record-wellbeing';
          indicador.textContent = autorrelato; indicador.title = 'Informado pelo paciente'; titulo.append(indicador);
        }
        const texto = document.createElement('p'); texto.className = 'record-text'; texto.textContent = pagina.texto;
        detalhe.append(titulo, texto); registros.append(detalhe);
      }
      if (!dados.registros.length) registros.textContent = 'Nenhum registro neste período.';
      const analises = document.querySelector('#analises-paciente');
      for (const analise of dados.analises) {
        const artigo = document.createElement('article'); artigo.className = 'summary-result';
        const titulo = document.createElement('h3'); titulo.textContent = `${dataAcompanhamento(analise.inicio)} a ${dataAcompanhamento(analise.fim)}`;
        const texto = document.createElement('p'); texto.className = 'summary-text'; texto.textContent = analise.texto;
        const emocoes = document.createElement('section'); emocoes.className = 'emotion-panel'; mostrarEmocoes(emocoes, analise);
        artigo.append(titulo, texto, emocoes); analises.append(artigo);
      }
      if (!dados.analises.length) analises.textContent = 'O paciente ainda não gerou análises dentro deste período. São exibidas até as 20 análises mais recentes.';
      painel.hidden = false; estado.textContent = `${dados.paciente}: ${dados.registros.length} registros no período.`;
    } catch (erro) { limpar(); estado.textContent = erroAcesso(erro); }
    finally { botao.disabled = false; paciente.disabled = false; document.querySelector('#inicio').disabled = false; document.querySelector('#fim').disabled = false; }
  });
  try {
    const contas = await acessoApi('/pacientes'); paciente.replaceChildren();
    const vazio = document.createElement('option'); vazio.value = ''; vazio.textContent = contas.length ? 'Selecione um paciente' : 'Nenhum paciente'; paciente.append(vazio);
    for (const conta of contas) { const opcao = document.createElement('option'); opcao.value = conta.login; opcao.textContent = conta.login; paciente.append(opcao); }
    paciente.disabled = !contas.length; botao.disabled = !contas.length;
    estado.textContent = contas.length ? 'Selecione um paciente e o período que deseja consultar.' : 'Nenhum paciente autorizou seu acesso ainda.';
  } catch (erro) { estado.textContent = erroAcesso(erro); }
}
if (document.body.dataset.area === 'paciente') areaPaciente(); else areaProfissional();
