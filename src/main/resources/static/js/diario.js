'use strict';

const campoData = document.querySelector('#data');
const texto = document.querySelector('#texto');
const estado = document.querySelector('#estado');
const salvar = document.querySelector('#salvar');
const excluir = document.querySelector('#excluir');
const reabrir = document.querySelector('#reabrir');
const humor = document.querySelector('#humor');
const energia = document.querySelector('#energia');
let bemEstarOriginal = {humor:null, energia:null};
let diasComNota = new Set();
let numeroPedidoCalendario = 0;
const avisoSessao = document.querySelector('#sessao-aviso');
const usuario = document.querySelector('meta[name="usuario"]').content;
let token = document.querySelector('meta[name="csrf-token"]').content;
let cabecalho = document.querySelector('meta[name="csrf-header"]').content;
let dataAtual = null;
let original = '';
let versao = -1;
let ocupado = false;
let carregado = false;
let mesVisivel = new Date();
const folha = document.querySelector('.writing-area');
const movimentoReduzido = window.matchMedia('(prefers-reduced-motion: reduce)');
const telaPequena = window.matchMedia('(max-width: 760px)');
const calendarioDobrado = document.querySelector('.agenda-month');
calendarioDobrado.open = true;

function calendario() {
    const ano = mesVisivel.getFullYear(), mes = mesVisivel.getMonth();
    document.querySelector('#mes-agenda').textContent = new Intl.DateTimeFormat('pt-BR', {month:'long', year:'numeric'}).format(mesVisivel);
    const lista = document.querySelector('#calendario');
    lista.replaceChildren();
    const primeiro = (new Date(ano, mes, 1).getDay() + 6) % 7;
    for (let i = 0; i < primeiro; i++) {
        const espaco = document.createElement('span'); espaco.setAttribute('aria-hidden', 'true'); lista.append(espaco);
    }
    const dias = new Date(ano, mes + 1, 0).getDate();
    for (let numero = 1; numero <= dias; numero++) {
        const dia = new Date(ano, mes, numero, 12), iso = dataLocal(dia);
        const botao = document.createElement('button');
        botao.type = 'button'; botao.textContent = numero; botao.disabled = ocupado;
        botao.dataset.data = iso;
        botao.setAttribute('aria-label', new Intl.DateTimeFormat('pt-BR', {dateStyle:'full'}).format(dia));
        botao.setAttribute('aria-pressed', String(iso === dataAtual));
        if (iso === dataLocal()) botao.setAttribute('aria-current', 'date');
        botao.addEventListener('click', () => carregar(iso));
        lista.append(botao);
    }
    marcarDias();
    atualizarDiasComNota();
}

function marcarDias() {
    for (const botao of document.querySelectorAll('#calendario button[data-data]')) {
        const comNota = diasComNota.has(botao.dataset.data);
        botao.classList.toggle('has-note', comNota);
        const data = new Date(`${botao.dataset.data}T12:00:00`);
        botao.setAttribute('aria-label', new Intl.DateTimeFormat('pt-BR', {dateStyle:'full'}).format(data) + (comNota ? ', com nota salva' : ''));
    }
}
async function atualizarDiasComNota() {
    const pedido = ++numeroPedidoCalendario;
    const mes = `${mesVisivel.getFullYear()}-${String(mesVisivel.getMonth()+1).padStart(2,'0')}`;
    try {
        const datas = await requisitar(`/api/registros/datas?mes=${encodeURIComponent(mes)}`);
        if (pedido !== numeroPedidoCalendario) return;
        diasComNota = new Set(datas);
        marcarDias();
        document.querySelector('#calendario-estado').textContent = '';
    } catch (_) {
        if (pedido === numeroPedidoCalendario) document.querySelector('#calendario-estado').textContent = 'Não foi possível atualizar os dias com notas.';
    }
}

function bemEstarAtual() { return {humor:humor.value || null, energia:energia.value || null}; }
function preencherBemEstar(valor) {
    bemEstarOriginal = valor || {humor:null, energia:null};
    humor.value = bemEstarOriginal.humor || '';
    energia.value = bemEstarOriginal.energia || '';
}

function decorarData(dia) {
    document.querySelector('#carimbo-data').dateTime = dataAtual;
    document.querySelector('#numero-dia').textContent = String(dia.getDate()).padStart(2, '0');
    document.querySelector('#mes-pagina').textContent = new Intl.DateTimeFormat('pt-BR', {month:'short'}).format(dia);
    document.querySelector('#marcador-hoje').hidden = dataAtual !== dataLocal();
    document.querySelector('#data-hoje').textContent = `Hoje, ${new Intl.DateTimeFormat('pt-BR', {day:'numeric',month:'long'}).format(new Date())}`;
    mesVisivel = new Date(dia.getFullYear(), dia.getMonth(), 1);
    calendario();
}

async function animarFolha(entrada, adiante) {
    if (movimentoReduzido.matches || !folha.animate) return;
    const angulo = adiante ? -55 : 35;
    const aberta = {transform:'rotateY(0deg)', opacity:1};
    const virada = {transform:`rotateY(${entrada ? -angulo : angulo}deg)`, opacity:0.15};
    const animacao = folha.animate(entrada ? [virada, aberta] : [aberta, virada], {
        duration: entrada ? 260 : 180, easing:'ease-in-out', fill:'forwards'
    });
    try { await animacao.finished; }
    finally { animacao.cancel(); }
}

function dataLocal(date = new Date()) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}
function alterado() { return texto.value !== original || JSON.stringify(bemEstarAtual()) !== JSON.stringify(bemEstarOriginal); }
function mensagem(conteudo, erro = false) {
    estado.textContent = conteudo;
    estado.classList.toggle('has-error', erro);
}
function controles() {
    texto.disabled = !carregado;
    texto.readOnly = ocupado;
    humor.disabled = ocupado || !carregado;
    energia.disabled = ocupado || !carregado;
    salvar.disabled = ocupado || !carregado || !alterado() || !texto.value.trim();
    excluir.disabled = ocupado || !carregado || versao === -1;
    reabrir.disabled = ocupado;
    for (const id of ['data', 'anterior', 'proximo', 'hoje']) {
        document.getElementById(id).disabled = ocupado;
    }
    for (const botao of document.querySelectorAll('#calendario button, #mes-anterior, #mes-proximo, #virar-proxima')) botao.disabled = ocupado;
    document.querySelector('#limite').textContent = `${texto.value.length.toLocaleString('pt-BR')} / 20.000`;
}

function confirmar(titulo, pergunta) {
    const dialog = document.querySelector('#confirmacao');
    document.querySelector('#confirmacao-titulo').textContent = titulo;
    document.querySelector('#confirmacao-texto').textContent = pergunta;
    dialog.returnValue = 'cancelar';
    return new Promise(resolve => {
        dialog.addEventListener('close', () => resolve(dialog.returnValue === 'confirmar'), {once: true});
        dialog.showModal();
        dialog.querySelector('button[value="cancelar"]').focus();
    });
}

async function requisitar(url, opcoes = {}) {
    let resposta;
    try {
        resposta = await fetch(url, {
            ...opcoes, credentials: 'same-origin', cache: 'no-store', signal: AbortSignal.timeout(15000),
            headers: {'Accept': 'application/json', 'X-Diario-Usuario': usuario,
                [cabecalho]: token, ...opcoes.headers}
        });
    } catch (_) {
        throw new Error('Não foi possível confirmar a operação. Mantenha esta aba aberta e tente novamente.');
    }
    if (resposta.status === 401 || resposta.status === 403) {
        avisoSessao.hidden = false;
        throw new Error('Entre novamente para continuar. Mantenha esta aba aberta.');
    }
    if (!resposta.ok) {
        const dados = await resposta.json().catch(() => ({}));
        throw new Error(dados.erro || 'Não foi possível concluir. Mantenha esta aba aberta e tente novamente.');
    }
    return resposta.status === 204 ? null : resposta.json();
}

async function carregar(data, perguntar = true) {
    if (ocupado) return;
    if (perguntar && alterado() && !await confirmar('Há alterações não salvas',
        'Deseja descartar as alterações e abrir esta data? Cancele para salvar primeiro.')) {
        campoData.value = dataAtual;
        return;
    }
    ocupado = true;
    controles();
    mensagem('Carregando página…');
    try {
        const pagina = await requisitar(`/api/registros?data=${encodeURIComponent(data)}`);
        const trocar = carregado && dataAtual !== pagina.data;
        const adiante = !dataAtual || pagina.data > dataAtual;
        if (trocar) await animarFolha(false, adiante);
        dataAtual = pagina.data;
        campoData.value = dataAtual;
        original = pagina.texto;
        texto.value = original;
        preencherBemEstar(pagina.bemEstar);
        versao = pagina.versao;
        carregado = true;
        const dia = new Date(`${dataAtual}T12:00:00`);
        document.querySelector('#titulo-data').textContent = new Intl.DateTimeFormat('pt-BR', {
            day: 'numeric', month: 'long', year: 'numeric'}).format(dia);
        document.querySelector('#dia-semana').textContent = new Intl.DateTimeFormat('pt-BR', {weekday: 'long'}).format(dia).toUpperCase();
        decorarData(dia);
        const escolhaData = document.querySelector('.agenda-dates');
        if (telaPequena.matches && escolhaData.open) {
            escolhaData.open = false;
            escolhaData.querySelector('summary').focus({preventScroll:true});
        }
        mensagem(versao === -1 ? 'Nenhum registro nesta data.' : 'Página carregada.');
        if (trocar) await animarFolha(true, adiante);
    } catch (erro) {
        campoData.value = dataAtual || data;
        mensagem(erro.message, true);
    } finally {
        ocupado = false;
        controles();
    }
}

texto.addEventListener('input', () => {
    mensagem(alterado() ? 'Alterações não salvas.' : 'Nenhuma alteração.');
    controles();
});
for (const campo of [humor, energia]) campo.addEventListener('change', () => {
    mensagem(alterado() ? 'Alterações não salvas.' : 'Nenhuma alteração.');
    controles();
});
salvar.addEventListener('click', async () => {
    if (ocupado || !carregado) return;
    ocupado = true;
    controles();
    mensagem('Salvando…');
    try {
        const pagina = await requisitar('/api/registros', {
            method: 'PUT', headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({data: dataAtual, texto: texto.value, versao, bemEstar:bemEstarAtual()})
        });
        original = pagina.texto;
        versao = pagina.versao;
        preencherBemEstar(pagina.bemEstar);
        diasComNota.add(dataAtual);
        marcarDias();
        atualizarDiasComNota();
        mensagem('Página salva.');
    } catch (erro) { mensagem(erro.message, true); }
    finally { ocupado = false; controles(); }
});
excluir.addEventListener('click', async () => {
    if (ocupado || versao === -1) return;
    if (!await confirmar('Excluir esta página?',
        'A nota e os campos desta data serão excluídos definitivamente.')) return;
    ocupado = true;
    controles();
    mensagem('Excluindo…');
    try {
        await requisitar(`/api/registros?data=${encodeURIComponent(dataAtual)}&versao=${versao}`, {method: 'DELETE'});
        original = '';
        texto.value = '';
        versao = -1;
        preencherBemEstar(null);
        diasComNota.delete(dataAtual);
        marcarDias();
        atualizarDiasComNota();
        mensagem('Página excluída.');
    } catch (erro) { mensagem(erro.message, true); }
    finally { ocupado = false; controles(); }
});
campoData.addEventListener('change', () => {
    if (campoData.validity.valid && campoData.value) carregar(campoData.value);
    else campoData.value = dataAtual || dataLocal();
});
document.querySelector('#hoje').addEventListener('click', () => carregar(dataLocal()));
function mudarDia(delta) {
    const dia = new Date(`${dataAtual || dataLocal()}T12:00:00`);
    dia.setDate(dia.getDate() + delta);
    carregar(dataLocal(dia));
}
document.querySelector('#anterior').addEventListener('click', () => mudarDia(-1));
document.querySelector('#proximo').addEventListener('click', () => mudarDia(1));
document.querySelector('#virar-proxima').addEventListener('click', () => mudarDia(1));
function mudarMes(delta) {
    if (ocupado) return;
    mesVisivel = new Date(mesVisivel.getFullYear(), mesVisivel.getMonth() + delta, 1);
    calendario();
}
document.querySelector('#mes-anterior').addEventListener('click', () => mudarMes(-1));
document.querySelector('#mes-proximo').addEventListener('click', () => mudarMes(1));
reabrir.addEventListener('click', () => carregar(dataAtual || campoData.value || dataLocal()));

document.querySelector('#retomar').addEventListener('click', async () => {
    try {
        const sessao = await requisitar('/api/sessao');
        if (sessao.login !== usuario) throw new Error(`Entre com o usuário ${usuario} para retomar este texto.`);
        token = sessao.token;
        cabecalho = sessao.cabecalho;
        document.querySelector('#logout input[name="_csrf"]').value = token;
        avisoSessao.hidden = true;
        mensagem('Sessão renovada. Você já pode salvar seu texto.');
        if (!carregado) await carregar(campoData.value || dataLocal(), false);
    } catch (erro) { mensagem(erro.message, true); }
});
document.querySelector('#logout').addEventListener('submit', async event => {
    event.preventDefault();
    if (ocupado) { mensagem('Aguarde a operação terminar antes de sair.', true); return; }
    if (alterado() && !await confirmar('Sair sem salvar?', 'As alterações não salvas serão descartadas.')) return;
    original = texto.value;
    bemEstarOriginal = bemEstarAtual();
    event.target.submit();
});
window.addEventListener('beforeunload', event => {
    if (alterado() || ocupado) { event.preventDefault(); event.returnValue = ''; }
});
campoData.value = dataLocal();
carregar(campoData.value, false);
