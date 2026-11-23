'use strict';

function textoBemEstar(valor) {
  const humores = {MUITO_BAIXO:'Muito baixo', BAIXO:'Baixo', NEUTRO:'Neutro', BOM:'Bom', MUITO_BOM:'Muito bom'};
  const energias = {BAIXA:'Baixa', MEDIA:'Média', ALTA:'Alta'};
  const partes = [];
  if (humores[valor?.humor]) partes.push(`Humor: ${humores[valor.humor]}`);
  if (energias[valor?.energia]) partes.push(`Energia: ${energias[valor.energia]}`);
  return partes.join(' · ');
}

// Conteúdo dos relatos e da IA sempre é exibido como texto, nunca como HTML.
function mostrarEmocoes(container, resumo) {
  container.replaceChildren();
  const titulo = document.createElement('h3');
  titulo.textContent = 'Emoções nos relatos';
  container.append(titulo);
  const ajuda = document.createElement('p');
  ajuda.className = 'help-text';
  ajuda.textContent = 'Identificações da IA, com até duas emoções por dia. As contagens indicam dias com menções, não intensidade ou estado clínico.';
  container.append(ajuda);
  const emocoes = resumo.emocoes || [];
  if (!emocoes.length) {
    const vazio = document.createElement('p');
    vazio.textContent = !resumo.configuracao?.startsWith('diario-v1-') ? 'Não houve evidência suficiente para identificar emoções. Isso não significa ausência de emoções.' : 'Esta análise antiga não inclui emoções. Gere uma nova análise do período.';
    container.append(vazio);
    return;
  }
  const contagens = new Map();
  for (const emocao of emocoes) {
    if (!contagens.has(emocao.nome)) contagens.set(emocao.nome, new Set());
    contagens.get(emocao.nome).add(emocao.data);
  }
  const contadores = document.createElement('div');
  contadores.className = 'emotion-counts';
  for (const [nome, dias] of contagens) {
    const item = document.createElement('span');
    item.textContent = `${nome}: ${dias.size} ${dias.size === 1 ? 'dia' : 'dias'}`;
    contadores.append(item);
  }
  container.append(contadores);
  const lista = document.createElement('ul');
  lista.className = 'emotion-list';
  for (const emocao of [...emocoes].sort((a,b) => a.data.localeCompare(b.data))) {
    const item = document.createElement('li');
    const nome = document.createElement('span'); nome.className = 'emotion-name'; nome.textContent = emocao.nome;
    const data = document.createElement('span'); data.className = 'emotion-date';
    data.textContent = new Intl.DateTimeFormat('pt-BR').format(new Date(`${emocao.data}T12:00:00`));
    item.append(nome, data);
    if (emocao.incerta) {
      const incerta = document.createElement('p'); incerta.className = 'uncertain'; incerta.textContent = 'Interpretação incerta'; item.append(incerta);
    }
    const trecho = document.createElement('blockquote'); trecho.textContent = emocao.trecho; item.append(trecho);
    lista.append(item);
  }
  container.append(lista);
}
