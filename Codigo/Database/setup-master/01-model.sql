CREATE TABLE subject (
    id CHAR(10) PRIMARY KEY
);

CREATE TABLE teacher (
    id CHAR(10) PRIMARY KEY,
    name VARCHAR(30) NOT NULL
);

CREATE TABLE course (
    id CHAR(10) PRIMARY KEY
);

CREATE TABLE timetable (
    id SERIAL PRIMARY KEY,
	creation_date DATE NOT NULL DEFAULT CURRENT_DATE,
	course_id CHAR(10) NOT NULL,
	CONSTRAINT timetable_course_fk FOREIGN KEY (course_id) REFERENCES course(id)
);

CREATE TABLE restriction (
	id SERIAL PRIMARY KEY NOT NULL,
	name VARCHAR(30) NOT NULL
);

CREATE TABLE room (
	id CHAR(6) PRIMARY KEY
);

CREATE TABLE restriction_subject (
	subject_id CHAR(10),
	restriction_id INT,
	penalty INT NOT NULL,
	hard BOOL NOT NULL,
	CONSTRAINT restriction_subject_subject_fk FOREIGN KEY (subject_id) REFERENCES subject(id),
	CONSTRAINT restriction_subject_restriction_fk FOREIGN KEY (restriction_id) REFERENCES restriction(id),
	CONSTRAINT restriction_subject_pk PRIMARY KEY (subject_id, restriction_id)
);

CREATE TABLE teacher_unavailability (
	id SERIAL PRIMARY KEY,
	duration INT NOT NULL,
	start_slot INT NOT NULL,
	weeks VARCHAR(15) NOT NULL,
	teacher_id CHAR(10) NOT NULL,
	CONSTRAINT teacher_unavailability_teacher_fk FOREIGN KEY (teacher_id) REFERENCES teacher(id)
);

CREATE TABLE class_subject (
	class_id CHAR(10) NOT NULL,
    subject_id CHAR(10) NOT NULL,
	CONSTRAINT class_subject_subject_fk FOREIGN KEY (subject_id) REFERENCES subject(id),
	CONSTRAINT class_subject_pk PRIMARY KEY (class_id, subject_id)
);

CREATE TABLE teacher_class (
	teacher_id CHAR(10),
	class_id CHAR(10) NOT NULL,
	subject_id char(10) NOT NULL,
	CONSTRAINT teacher_class_class_subject_fk FOREIGN KEY (class_id, subject_id) REFERENCES class_subject(class_id, subject_id),
	CONSTRAINT teacher_class_teacher_fk FOREIGN KEY (teacher_id) REFERENCES teacher(id),
	CONSTRAINT teacher_class_pk PRIMARY KEY (teacher_id, class_id, subject_id)
);

CREATE TABLE configuration (
	id CHAR(10) PRIMARY KEY,
	course_id CHAR(10) NOT NULL,
	CONSTRAINT configuration_course_fk FOREIGN KEY (course_id) REFERENCES course(id)
);

CREATE TABLE subpart (
	id CHAR(10) PRIMARY KEY,
	configuration_id CHAR(10) NOT NULL,
	CONSTRAINT subpart_configuration_fk FOREIGN KEY (configuration_id) REFERENCES configuration(id)
);

CREATE TABLE subject_subpart (
	subpart_id CHAR(10) NOT NULL,
	subject_id CHAR(10) NOT NULL,
	CONSTRAINT subject_subpart_subpart_fk FOREIGN KEY (subpart_id) REFERENCES subpart(id),
	CONSTRAINT subject_subpart_subject_fk FOREIGN KEY (subject_id) REFERENCES subject(id),
	CONSTRAINT subject_subpart_pk PRIMARY KEY (subpart_id, subject_id)
);

CREATE TABLE subject_room (
	subject_id CHAR(10),
	room_id CHAR(6),
	penalizacao INT NOT NULL,
	CONSTRAINT subject_room_subject_fk FOREIGN KEY (subject_id) REFERENCES subject(id),
	CONSTRAINT subject_room_room_fk FOREIGN KEY (room_id) REFERENCES room(id),
	CONSTRAINT subject_room_pk PRIMARY KEY (subject_id, room_id)
);

CREATE TABLE room_distance (
	room_id_1 CHAR(6),
	room_id_2 CHAR(6),
	distance INT NOT NULL,
	CONSTRAINT room_distance_room1_fk FOREIGN KEY (room_id_1) REFERENCES room(id),
	CONSTRAINT room_distance_room2_fk FOREIGN KEY (room_id_2) REFERENCES room(id),
	CONSTRAINT room_distance_pk PRIMARY KEY (room_id_1, room_id_2)
);

CREATE TABLE room_unavailability (
	id SERIAL PRIMARY KEY,
	room_id CHAR(6) NOT NULL,
	weeks VARCHAR(15) NOT NULL,
	start_slot INT NOT NULL,
	duration INT NOT NULL,
	CONSTRAINT room_unavailability_room_fk FOREIGN KEY (room_id) REFERENCES room(id)
);

CREATE TABLE subject_time (
	id SERIAL PRIMARY KEY,
	subject_id CHAR(10) NOT NULL,
	penalty INT NOT NULL,
	days VARCHAR(7) NOT NULL,
	start_slot INT NOT NULL,
	duration INT NOT NULL,
	weeks VARCHAR(15) NOT NULL,
	CONSTRAINT subject_time_subject_fk FOREIGN KEY (subject_id) REFERENCES subject(id)
);

CREATE TABLE scheduled_lesson (
	id SERIAL PRIMARY KEY,
    subject_id CHAR(10) NOT NULL,
    teacher_id CHAR(10) NOT NULL,
	room_id CHAR(6) NOT NULL,
	timetable_id INT NOT NULL,
	days VARCHAR(7) NOT NULL,
	weeks VARCHAR(15) NOT NULL,
	start_slot INT NOT NULL,
	CONSTRAINT scheduled_lesson_subject_fk FOREIGN KEY (subject_id) REFERENCES subject(id),
	CONSTRAINT scheduled_lesson_teacher_fk FOREIGN KEY (teacher_id) REFERENCES teacher(id),
	CONSTRAINT scheduled_lesson_room_fk FOREIGN KEY (room_id) REFERENCES room(id),
	CONSTRAINT scheduled_lesson_timetable_fk FOREIGN KEY (timetable_id) REFERENCES timetable(id)
);

CREATE TABLE optimization_parameters (
	time_weight INT NOT NULL,
	room_weight INT NOT NULL,
	distribution_weight INT NOT NULL
);

CREATE TABLE timetable_configuration (
	number_days INT NOT NULL,
	number_weeks INT NOT NULL,
	slots_per_day INT NOT NULL
);