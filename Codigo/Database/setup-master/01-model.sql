CREATE TABLE subject (
    id VARCHAR(10) PRIMARY KEY
);

CREATE TABLE teacher (
    id INT PRIMARY KEY,
    name VARCHAR(30) NOT NULL
);

CREATE TABLE course (
    id VARCHAR(10) PRIMARY KEY
);

CREATE TABLE timetable (
    id VARCHAR(128) PRIMARY KEY,
	creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	course_id VARCHAR(10) NOT NULL,
	CONSTRAINT timetable_course_fk FOREIGN KEY (course_id) REFERENCES course(id)
);

CREATE TABLE restriction (
	id SERIAL PRIMARY KEY NOT NULL,
	name VARCHAR(30) NOT NULL
);

CREATE TABLE room (
	id VARCHAR(6) PRIMARY KEY
);

CREATE TABLE restriction_subject (
	subject_id VARCHAR(10),
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
	days VARCHAR(7) NOT NULL,
	weeks VARCHAR(16) NOT NULL,
	teacher_id INT NOT NULL,
	CONSTRAINT teacher_unavailability_teacher_fk FOREIGN KEY (teacher_id) REFERENCES teacher(id)
);

CREATE TABLE class_subject (
	class_id VARCHAR(10) NOT NULL,
    subject_id VARCHAR(10) NOT NULL,
	parent_class VARCHAR(10),
	CONSTRAINT class_subject_subject_fk FOREIGN KEY (subject_id) REFERENCES subject(id),
	CONSTRAINT class_subject_class_id_unique UNIQUE (class_id),
	CONSTRAINT class_parent_class_fk FOREIGN KEY (parent_class) REFERENCES class_subject(class_id),
	CONSTRAINT class_subject_pk PRIMARY KEY (class_id, subject_id)
);

CREATE TABLE teacher_class (
	teacher_id INT,
	class_id VARCHAR(10) NOT NULL,
	subject_id VARCHAR(10) NOT NULL,
	CONSTRAINT teacher_class_class_subject_fk FOREIGN KEY (class_id, subject_id) REFERENCES class_subject(class_id, subject_id),
	CONSTRAINT teacher_class_teacher_fk FOREIGN KEY (teacher_id) REFERENCES teacher(id),
	CONSTRAINT teacher_class_pk PRIMARY KEY (teacher_id, class_id, subject_id)
);

CREATE TABLE config (
	id VARCHAR(10) PRIMARY KEY,
	course_id VARCHAR(10) NOT NULL,
	CONSTRAINT config_course_fk FOREIGN KEY (course_id) REFERENCES course(id)
);

CREATE TABLE subpart (
	id VARCHAR(10) PRIMARY KEY,
	config_id VARCHAR(10) NOT NULL,
	CONSTRAINT subpart_config_fk FOREIGN KEY (config_id) REFERENCES config(id)
);

CREATE TABLE subject_subpart (
	subpart_id VARCHAR(10) NOT NULL,
	subject_id VARCHAR(10) NOT NULL,
	CONSTRAINT subject_subpart_subpart_fk FOREIGN KEY (subpart_id) REFERENCES subpart(id),
	CONSTRAINT subject_subpart_subject_fk FOREIGN KEY (subject_id) REFERENCES subject(id),
	CONSTRAINT subject_subpart_pk PRIMARY KEY (subpart_id, subject_id)
);

CREATE TABLE subject_room (
	subject_id VARCHAR(10),
	room_id VARCHAR(6),
	penalty INT NOT NULL,
	CONSTRAINT subject_room_subject_fk FOREIGN KEY (subject_id) REFERENCES subject(id),
	CONSTRAINT subject_room_room_fk FOREIGN KEY (room_id) REFERENCES room(id),
	CONSTRAINT subject_room_pk PRIMARY KEY (subject_id, room_id)
);

CREATE TABLE room_distance (
	room_id_1 VARCHAR(6),
	room_id_2 VARCHAR(6),
	distance INT NOT NULL,
	CONSTRAINT room_distance_room1_fk FOREIGN KEY (room_id_1) REFERENCES room(id),
	CONSTRAINT room_distance_room2_fk FOREIGN KEY (room_id_2) REFERENCES room(id),
	CONSTRAINT room_distance_pk PRIMARY KEY (room_id_1, room_id_2)
);

CREATE TABLE room_unavailability (
	id SERIAL PRIMARY KEY,
	room_id VARCHAR(6) NOT NULL,
	days VARCHAR(7) NOT NULL,
	weeks VARCHAR(16) NOT NULL,
	start_slot INT NOT NULL,
	duration INT NOT NULL,
	CONSTRAINT room_unavailability_room_fk FOREIGN KEY (room_id) REFERENCES room(id)
);

CREATE TABLE subject_time (
	id SERIAL PRIMARY KEY,
	subject_id VARCHAR(10) NOT NULL,
	penalty INT NOT NULL,
	days VARCHAR(7) NOT NULL,
	start_slot INT NOT NULL,
	duration INT NOT NULL,
	weeks VARCHAR(16) NOT NULL,
	CONSTRAINT subject_time_subject_fk FOREIGN KEY (subject_id) REFERENCES subject(id)
);

CREATE TABLE scheduled_lesson (
	id VARCHAR(128) PRIMARY KEY,
    subject_id VARCHAR(10) NOT NULL,
    teacher_id INT NOT NULL,
	room_id VARCHAR(6) NOT NULL,
	timetable_id VARCHAR(128) NOT NULL,
	days VARCHAR(7) NOT NULL,
	weeks VARCHAR(16) NOT NULL,
	start_slot INT NOT NULL,
	duration INT NOT NULL,
	CONSTRAINT scheduled_lesson_subject_fk FOREIGN KEY (subject_id) REFERENCES subject(id),
	CONSTRAINT scheduled_lesson_teacher_fk FOREIGN KEY (teacher_id) REFERENCES teacher(id),
	CONSTRAINT scheduled_lesson_room_fk FOREIGN KEY (room_id) REFERENCES room(id),
	CONSTRAINT scheduled_lesson_timetable_fk FOREIGN KEY (timetable_id) REFERENCES timetable(id)
);

CREATE TABLE scheduled_lesson_teacher (
	scheduled_lesson_id VARCHAR(128) NOT NULL,
	teacher_id INT NOT NULL,
	CONSTRAINT scheduled_lesson_id_fk FOREIGN KEY (scheduled_lesson_id) REFERENCES scheduled_lesson(id),
	CONSTRAINT scheduled_lesson_teacher_fk FOREIGN KEY (teacher_id) REFERENCES teacher(id),
	CONSTRAINT scheduled_lesson_pk PRIMARY KEY (scheduled_lesson_id, teacher_id)
);

CREATE TABLE optimization_parameters (
	time_weight INT NOT NULL,
	room_weight INT NOT NULL,
	distribution_weight INT NOT NULL,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE timetable_configuration (
	number_days INT NOT NULL,
	number_weeks INT NOT NULL,
	slots_per_day INT NOT NULL,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);