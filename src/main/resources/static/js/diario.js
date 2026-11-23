'use strict';

const campoData = document.querySelector('#data');
const texto = document.querySelector('#texto');
const estado = document.querySelector('#estado');
const salvar = document.querySelector('#salvar');
const excluir = document.querySelector('#excluir');
const reabrir = document.querySelector('#reabrir');
const avisoSessao = document.querySelector('#sessao-aviso');
const usuario = document.querySelector('meta[name="usuario"]').content;
let token = document.querySelector('meta[name="csrf-token"]').content;
let cabecalho = document.querySelector('meta[name="csrf-header"]').content;
let dataAtual = null;
let original = '';
let versao = -1;
let ocupado = false;
let carregado = false;

function dataLocal(date = new Date()) {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}
function alterado() { return texto.value !== original; }
function mensagem(conteudo, erro = false) {
    estado.textContent = conteudo;
    estado.classList.toggle('has-error', erro);
}
function controles() {
    texto.disabled = !carregado;
    texto.readOnly = ocupado;
    salvar.disabled = ocupado || !carregado || !alterado() || !texto.value.trim();
    excluir.disabled = ocupado || !carregado || versao === -1;
    reabrir.disabled = ocupado;
    for (const id of ['data', 'anterior', 'proximo', 'hoje']) {
        document.getElementById(id).disabled = ocupado;
    }
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
        throw new Error('Sem confirmação do servidor. Seu texto continua nesta aba. Confira a conexão e tente novamente.');
    }
    if (resposta.status === 401 || resposta.status === 403) {
        avisoSessao.hidden = false;
        throw new Error('Sessão expirada ou permissão inválida. Entre novamente; mantenha esta aba aberta.');
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
        'Deseja descartar o texto ainda não salvo e abrir esta data? Cancele para salvar primeiro.')) {
        campoData.value = dataAtual;
        return;
    }
    ocupado = true;
    controles();
    mensagem('Carregando página…');
    try {
        const pagina = await requisitar(`/api/registros?data=${encodeURIComponent(data)}`);
        dataAtual = pagina.data;
        campoData.value = dataAtual;
        original = pagina.texto;
        texto.value = original;
        versao = pagina.versao;
        carregado = true;
        const dia = new Date(`${dataAtual}T12:00:00`);
        document.querySelector('#titulo-data').textContent = new Intl.DateTimeFormat('pt-BR', {
            day: 'numeric', month: 'long', year: 'numeric'}).format(dia);
        document.querySelector('#dia-semana').textContent = new Intl.DateTimeFormat('pt-BR', {weekday: 'long'}).format(dia).toUpperCase();
        mensagem(versao === -1 ? 'Página vazia. Comece a escrever quando quiser.' : 'Página carregada.');
    } catch (erro) {
        campoData.value = dataAtual || data;
        mensagem(erro.message, true);
    } finally {
        ocupado = false;
        controles();
    }
}

texto.addEventListener('input', () => {
    mensagem(alterado() ? 'Alterações não salvas.' : 'Nenhuma alteração pendente.');
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
            body: JSON.stringify({data: dataAtual, texto: texto.value, versao})
        });
        original = pagina.texto;
        versao = pagina.versao;
        mensagem('Página salva.');
    } catch (erro) { mensagem(erro.message, true); }
    finally { ocupado = false; controles(); }
});
excluir.addEventListener('click', async () => {
    if (ocupado || versao === -1) return;
    if (!await confirmar('Excluir esta página?',
        'O registro desta data será excluído definitivamente. O texto aberto também será descartado.')) return;
    ocupado = true;
    controles();
    mensagem('Excluindo…');
    try {
        await requisitar(`/api/registros?data=${encodeURIComponent(dataAtual)}&versao=${versao}`, {method: 'DELETE'});
        original = '';
        texto.value = '';
        versao = -1;
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
    if (ocupado) { mensagem('Aguarde a confirmação da operação antes de sair.', true); return; }
    if (alterado() && !await confirmar('Sair sem salvar?', 'O texto ainda não salvo será descartado.')) return;
    original = texto.value;
    event.target.submit();
});
window.addEventListener('beforeunload', event => {
    if (alterado() || ocupado) { event.preventDefault(); event.returnValue = ''; }
});
campoData.value = dataLocal();
carregar(campoData.value, false);
