DROP table if EXISTS disciplina;
DROP table if EXISTS professor;
DROP table if EXISTS curso;
DROP table if EXISTS horario;
DROP table if EXISTS disciplina_atribuida;

CREATE TABLE disciplina (
    id char(10) PRIMARY KEY NOT NULL,
    nome varchar(30) NOT NULL
);

CREATE TABLE professor (
    id char(10) PRIMARY KEY NOT NULL,
    nome varchar(30) NOT NULL
);

CREATE TABLE curso (
    id char(10) PRIMARY KEY NOT NULL,
    nome varchar(30) NOT NULL
);

CREATE TABLE horario (
    id char(10) PRIMARY KEY NOT NULL
);

CREATE TABLE disciplina_atribuida (
    id_disciplina char(10),
    id_professor char(10),
	id_curso char(10),
	CONSTRAINT disciplina_atribuida_disciplina_fk FOREIGN KEY (id_disciplina) REFERENCES disciplina(id),
	CONSTRAINT disciplina_atribuida_professor_fk FOREIGN KEY (id_professor) REFERENCES professor(id),
	CONSTRAINT disciplina_atribuida_curso_fk FOREIGN KEY (id_curso) REFERENCES curso(id)
);