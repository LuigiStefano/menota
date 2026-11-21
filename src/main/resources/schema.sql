CREATE TABLE IF NOT EXISTS usuarios (
                                        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                        nome VARCHAR(100) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE,
    senha VARCHAR(100) NOT NULL,
    perfil VARCHAR(20) NOT NULL,
    CONSTRAINT perfil_valido
    CHECK (perfil IN ('PACIENTE', 'PROFISSIONAL'))
    );

CREATE TABLE IF NOT EXISTS notas (
                                     id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                     paciente_id BIGINT NOT NULL REFERENCES usuarios(id),
    data DATE NOT NULL,
    texto TEXT NOT NULL
    );

CREATE TABLE IF NOT EXISTS autorizacoes (
                                            paciente_id BIGINT NOT NULL REFERENCES usuarios(id),
    profissional_id BIGINT NOT NULL REFERENCES usuarios(id),
    criada_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (paciente_id, profissional_id),
    CONSTRAINT pessoas_diferentes
    CHECK (paciente_id <> profissional_id)
    );