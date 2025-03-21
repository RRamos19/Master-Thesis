CREATE TABLE disciplina (
    id CHAR(10) PRIMARY KEY
);

CREATE TABLE professor (
    id CHAR(10) PRIMARY KEY,
    nome VARCHAR(30) NOT NULL
);

CREATE TABLE curso (
    id CHAR(10) PRIMARY KEY
);

CREATE TABLE horario (
    id SERIAL PRIMARY KEY,
	data_criacao DATE NOT NULL DEFAULT CURRENT_DATE,
	id_curso CHAR(10) NOT NULL,
	CONSTRAINT horario_curso_fk FOREIGN KEY (id_curso) REFERENCES curso(id)
);

CREATE TABLE restricao (
	id SERIAL PRIMARY KEY NOT NULL,
	nome VARCHAR(30) NOT NULL
);

CREATE TABLE sala (
	id CHAR(6) PRIMARY KEY
);

CREATE TABLE restricao_disciplina (
	id_disciplina CHAR(10) NOT NULL,
	id_restricao INT NOT NULL,
	penalizacao INT NOT NULL,
	rigida BOOL NOT NULL,
	CONSTRAINT restricao_disciplina_disciplina_fk FOREIGN KEY (id_disciplina) REFERENCES disciplina(id),
	CONSTRAINT restricao_disciplina_restricao_fk FOREIGN KEY (id_restricao) REFERENCES restricao(id),
	CONSTRAINT restricao_disciplina_pk PRIMARY KEY (id_disciplina, id_restricao)
);

CREATE TABLE indisponibilidade_professor (
	id SERIAL PRIMARY KEY,
	slots_duracao INT NOT NULL,
	slots_inicio INT NOT NULL,
	semanas VARCHAR(15) NOT NULL,
	id_professor CHAR(10) NOT NULL,
	CONSTRAINT indisponibilidade_professor_professor_fk FOREIGN KEY (id_professor) REFERENCES professor(id)
);

CREATE TABLE professor_disciplina (
	id_disciplina CHAR(10) NOT NULL,
    id_professor CHAR(10) NOT NULL,
	CONSTRAINT disciplina_atribuida_disciplina_fk FOREIGN KEY (id_disciplina) REFERENCES disciplina(id),
	CONSTRAINT disciplina_atribuida_professor_fk FOREIGN KEY (id_professor) REFERENCES professor(id),
	CONSTRAINT professor_disciplina_pk PRIMARY KEY (id_disciplina, id_professor)
);

CREATE TABLE configuracao (
	id CHAR(10) PRIMARY KEY,
	id_curso CHAR(10) NOT NULL,
	CONSTRAINT configuracao_curso_fk FOREIGN KEY (id_curso) REFERENCES curso(id)
);

CREATE TABLE subparte (
	id CHAR(10) PRIMARY KEY,
	id_configuracao CHAR(10) NOT NULL,
	CONSTRAINT subparte_configuracao_fk FOREIGN KEY (id_configuracao) REFERENCES configuracao(id)
);

CREATE TABLE disciplina_subparte (
	id_subparte CHAR(10) NOT NULL,
	id_disciplina CHAR(10) NOT NULL,
	CONSTRAINT disciplina_subparte_subparte_fk FOREIGN KEY (id_subparte) REFERENCES subparte(id),
	CONSTRAINT disciplina_subparte_disciplina_fk FOREIGN KEY (id_disciplina) REFERENCES disciplina(id),
	CONSTRAINT disciplina_subparte_pk PRIMARY KEY (id_subparte, id_disciplina)
);

CREATE TABLE disciplina_sala (
	id_disciplina CHAR(10) NOT NULL,
	id_sala CHAR(6) NOT NULL,
	penalizacao INT NOT NULL,
	CONSTRAINT disciplina_sala_disciplina_fk FOREIGN KEY (id_disciplina) REFERENCES disciplina(id),
	CONSTRAINT disciplina_sala_sala_fk FOREIGN KEY (id_sala) REFERENCES sala(id),
	CONSTRAINT disciplina_sala_pk PRIMARY KEY (id_disciplina, id_sala)
);

CREATE TABLE distancia_sala (
	id_sala_1 CHAR(6) NOT NULL,
	id_sala_2 CHAR(6) NOT NULL,
	distancia INT NOT NULL,
	CONSTRAINT distancia_sala_sala1_fk FOREIGN KEY (id_sala_1) REFERENCES sala(id),
	CONSTRAINT distancia_sala_sala2_fk FOREIGN KEY (id_sala_2) REFERENCES sala(id),
	CONSTRAINT distancia_sala_pk PRIMARY KEY (id_sala_1, id_sala_2)
);

CREATE TABLE indisponibilidade_sala (
	id SERIAL PRIMARY KEY,
	id_sala CHAR(6) NOT NULL,
	semanas VARCHAR(15) NOT NULL,
	slots_inicio INT NOT NULL,
	slots_duracao INT NOT NULL,
	CONSTRAINT indisponibilidade_sala_sala_fk FOREIGN KEY (id_sala) REFERENCES sala(id)
);

CREATE TABLE disciplina_tempos (
	id SERIAL PRIMARY KEY,
	id_disciplina CHAR(10) NOT NULL,
	penalizacao INT NOT NULL,
	dias VARCHAR(7) NOT NULL,
	slot_inicio INT NOT NULL,
	slot_duracao INT NOT NULL,
	semanas VARCHAR(15) NOT NULL,
	CONSTRAINT disciplina_tempos_disciplina_fk FOREIGN KEY (id_disciplina) REFERENCES disciplina(id)
);

CREATE TABLE aula_agendada (
	id SERIAL PRIMARY KEY,
    id_disciplina CHAR(10) NOT NULL,
    id_professor CHAR(10) NOT NULL,
	id_sala CHAR(6) NOT NULL,
	id_horario INT NOT NULL,
	dias VARCHAR(7) NOT NULL,
	semanas VARCHAR(15) NOT NULL,
	slots_inicio INT NOT NULL,
	CONSTRAINT aula_agendada_disciplina_fk FOREIGN KEY (id_disciplina) REFERENCES disciplina(id),
	CONSTRAINT aula_agendada_professor_fk FOREIGN KEY (id_professor) REFERENCES professor(id),
	CONSTRAINT aula_agendada_sala_fk FOREIGN KEY (id_sala) REFERENCES sala(id),
	CONSTRAINT aula_agendada_horario_fk FOREIGN KEY (id_horario) REFERENCES horario(id)
);

CREATE TABLE param_otimizacao (
	peso_time INT NOT NULL,
	peso_room INT NOT NULL,
	peso_distribution INT NOT NULL
);

CREATE TABLE configuracao_horario (
	numero_dias INT NOT NULL,
	numero_semanas INT NOT NULL,
	slots_por_dia INT NOT NULL
);