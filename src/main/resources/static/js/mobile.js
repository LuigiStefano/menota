'use strict';
const celularMeNota = window.matchMedia('(max-width: 760px)');
function ajustarPaineisMobile() {
    for (const painel of document.querySelectorAll('[data-mobile-panel]')) painel.open = !celularMeNota.matches;
}
ajustarPaineisMobile();
celularMeNota.addEventListener('change', ajustarPaineisMobile);
for (const link of document.querySelectorAll('.skip-link')) {
    link.addEventListener('click', () => {
        const destino = document.querySelector(link.getAttribute('href'));
        const painel = destino?.closest('[data-mobile-panel]');
        if (painel) painel.open = true;
    });
}
// A navegação inferior cede espaço ao teclado virtual quando ele é detectável.
if (window.visualViewport) {
    const atualizarTeclado = () => {
        const campo = document.activeElement;
        const editando = campo?.matches('input:not([type=checkbox]), textarea, select');
        document.body.classList.toggle('keyboard-open', Boolean(celularMeNota.matches && editando && window.visualViewport.height < window.innerHeight - 140));
    };
    window.visualViewport.addEventListener('resize', atualizarTeclado);
    document.addEventListener('focusin', atualizarTeclado);
    document.addEventListener('focusout', () => document.body.classList.remove('keyboard-open'));
}
