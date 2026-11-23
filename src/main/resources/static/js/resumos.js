'use strict';
const inicio = document.querySelector('#inicio');
const fim = document.querySelector('#fim');
const gerar = document.querySelector('#gerar');
const estadoResumo = document.querySelector('#estado-resumo');
const usuarioResumo = document.querySelector('meta[name="usuario"]').content;
const csrfResumo = document.querySelector('meta[name="csrf-token"]').content;
const cabecalhoResumo = document.querySelector('meta[name="csrf-header"]').content;
let gerando = false;
let configurado = false;

function isoLocal(d) { return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`; }
function dataBr(data) { return new Intl.DateTimeFormat('pt-BR').format(new Date(`${data}T12:00:00`)); }
function periodo(item) { return `${dataBr(item.inicio)} a ${dataBr(item.fim)}`; }
function avisoResumo(texto, erro = false) { estadoResumo.textContent = texto; estadoResumo.classList.toggle('field-error',erro); }
async function apiResumo(url, opcoes = {}) {
  let resposta;
  try { resposta = await fetch(url, {...opcoes, credentials:'same-origin',cache:'no-store',signal:AbortSignal.timeout(60000),
    headers:{'Accept':'application/json','X-Diario-Usuario':usuarioResumo,[cabecalhoResumo]:csrfResumo,...opcoes.headers}}); }
  catch (_) { throw new Error('Não houve confirmação. Aguarde e atualize o histórico antes de tentar gerar novamente.'); }
  if (resposta.status===401 || resposta.status===403) {
    document.querySelector('#sessao-resumo').hidden=false;
    throw new Error('Entre novamente para continuar.');
  }
  const dados = await resposta.json().catch(()=>({}));
  if (!resposta.ok) throw new Error(dados.erro || 'Não foi possível concluir. Atualize o histórico e tente novamente.');
  return dados;
}
function mostrarResumo(resumo) {
  document.querySelector('#resultado-titulo').textContent=periodo(resumo);
  document.querySelector('#resultado-texto').textContent=resumo.texto;
  document.querySelector('#resultado-fontes').textContent=resumo.datasUtilizadas.map(dataBr).join(', ');
  document.querySelector('#resultado-data').textContent=new Intl.DateTimeFormat('pt-BR',{dateStyle:'short',timeStyle:'short'}).format(new Date(resumo.criadoEm));
  mostrarEmocoes(document.querySelector('#resultado-emocoes'), resumo);
  document.querySelector('#resultado').hidden=false;
  if (window.matchMedia('(max-width: 760px)').matches) {
    document.querySelector('.mobile-panel').open=false;
    document.querySelector('#resultado').scrollIntoView({block:'start'});
  }
}
async function carregarHistorico() {
  const itens = await apiResumo('/api/resumos');
  const lista = document.querySelector('#historico');
  lista.replaceChildren();
  for (const item of itens) {
    const li=document.createElement('li');
    const botao=document.createElement('button');
    botao.type='button'; botao.className='history-item'; botao.textContent=periodo(item);
    botao.addEventListener('click',async()=>{
      if (gerando) return;
      document.querySelector('#historico-estado').textContent='Abrindo resumo…';
      try {
        mostrarResumo(await apiResumo(`/api/resumos/${encodeURIComponent(item.id)}`));
        document.querySelector('#historico-estado').textContent='Resumo aberto.';
      } catch(erro) {
        document.querySelector('#resultado').hidden=true;
        document.querySelector('#historico-estado').textContent=erro.message;
      }
    });
    li.append(botao); lista.append(li);
  }
  document.querySelector('#historico-estado').textContent=itens.length ? 'Selecione um resumo para abrir.' : 'Você ainda não tem resumos.';
}
async function atualizarResumo() {
  if(gerando) return;
  document.querySelector('#resultado').hidden=true;
  try {
    const config=await apiResumo('/api/resumos/configuracao');
    configurado=config.configurado;
    gerar.disabled=!configurado;
    document.querySelector('#chave-aviso').hidden=configurado;
    avisoResumo(configurado ? 'Escolha o período que deseja analisar.' : 'Geração de resumos indisponível.');
    await carregarHistorico();
  } catch(erro) { avisoResumo(erro.message,true); document.querySelector('#historico-estado').textContent='Não foi possível carregar o histórico. Tente atualizar.'; }
}
document.querySelector('#gerar-form').addEventListener('submit',async event=>{
  event.preventDefault();
  if(gerando || !configurado || !event.target.reportValidity()) return;
  const diferenca=(Date.parse(fim.value)-Date.parse(inicio.value))/86400000;
  if(diferenca<0 || diferenca>30) { avisoResumo('Escolha um período de até 31 dias, com início anterior ou igual ao fim.',true); return; }
  gerando=true; gerar.disabled=true; inicio.disabled=true; fim.disabled=true;
  document.querySelector('#atualizar').disabled=true;
  document.querySelector('#resultado').hidden=true;
  avisoResumo('Analisando registros…');
  try {
    const resposta=await apiResumo('/api/resumos',{method:'POST',headers:{'Content-Type':'application/json'},
      body:JSON.stringify({inicio:inicio.value,fim:fim.value,confirmoEnvioFicticio:document.querySelector('#consentimento').checked})});
    mostrarResumo(resposta.resumo);
    avisoResumo(resposta.reutilizado ? 'Este período já tem um resumo. Ele foi aberto abaixo.' : 'Análise concluída e salva.');
    await carregarHistorico();
  } catch(erro) { avisoResumo(erro.message,true); }
  finally {gerando=false;gerar.disabled=!configurado;inicio.disabled=false;fim.disabled=false;document.querySelector('#atualizar').disabled=false;}
});
document.querySelector('#atualizar').addEventListener('click',atualizarResumo);
window.addEventListener('beforeunload',event=>{if(gerando){event.preventDefault();event.returnValue='';}});
const hojeResumo=new Date(); fim.value=isoLocal(hojeResumo); hojeResumo.setDate(hojeResumo.getDate()-6); inicio.value=isoLocal(hojeResumo);
atualizarResumo();
