package br.edu.pooalab.diario.registro;

// Autorrelato opcional, sem interpretação ou pontuação clínica.
public record BemEstar(Humor humor, Energia energia) {
    public enum Humor { MUITO_BAIXO, BAIXO, NEUTRO, BOM, MUITO_BOM }
    public enum Energia { BAIXA, MEDIA, ALTA }
    public static BemEstar vazio() { return new BemEstar(null, null); }
}
