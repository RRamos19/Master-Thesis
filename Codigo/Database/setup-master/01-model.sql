CREATE TABLE teacher (
    id INT PRIMARY KEY,
    name VARCHAR(30) NOT NULL
);

CREATE TABLE course ( -- Por alterar
	id UUID PRIMARY KEY,
    name VARCHAR(10) NOT NULL UNIQUE
);

CREATE TABLE config (
	id UUID PRIMARY KEY,
	course_id UUID,
	name VARCHAR(10) NOT NULL UNIQUE,
	CONSTRAINT config_course_fk FOREIGN KEY (course_id) REFERENCES course(id)
);

CREATE TABLE subpart (
	id UUID PRIMARY KEY,
	config_id UUID,
	name VARCHAR(10) NOT NULL UNIQUE,
	CONSTRAINT subpart_config_fk FOREIGN KEY (config_id) REFERENCES config(id)
);

CREATE TABLE class_unit (
	id UUID PRIMARY KEY,
	subpart_id UUID,
    name VARCHAR(10) NOT NULL UNIQUE,
	parent_class_id UUID,
	CONSTRAINT class_class_parent_fk FOREIGN KEY (parent_class_id) REFERENCES class_unit(id),
	CONSTRAINT class_subpart_fk FOREIGN KEY (subpart_id) REFERENCES subpart(id)
);

CREATE TABLE timetable (
    id UUID PRIMARY KEY,
	program VARCHAR(10) NOT NULL,
	creation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE restriction ( -- Por alterar
	id SERIAL PRIMARY KEY,
	name VARCHAR(30) UNIQUE
);

CREATE TABLE class_restriction ( -- Por alterar
	id UUID,
	class_id UUID,
	restriction_id INT,
	penalty INT,
	required BOOL NOT NULL,
	CONSTRAINT class_restriction_class_fk FOREIGN KEY (class_id) REFERENCES class_unit(id),
	CONSTRAINT class_restriction_restriction_fk FOREIGN KEY (restriction_id) REFERENCES restriction(id),
	CONSTRAINT class_restriction_pk PRIMARY KEY (id, class_id)
);

CREATE TABLE room (
	id SERIAL PRIMARY KEY,
	name VARCHAR(6) NOT NULL UNIQUE
);

CREATE TABLE teacher_unavailability (
	id SERIAL,
	teacher_id INT NOT NULL,
	duration INT NOT NULL,
	start_slot INT NOT NULL,
	days VARCHAR(7) NOT NULL,
	weeks VARCHAR(16) NOT NULL,
	CONSTRAINT teacher_unavailability_teacher_fk FOREIGN KEY (teacher_id) REFERENCES teacher(id),
	CONSTRAINT teacher_unavailability_pk PRIMARY KEY (id, teacher_id)
);

CREATE TABLE teacher_class (
	teacher_id INT,
	class_id UUID,
	CONSTRAINT teacher_class_class_fk FOREIGN KEY (class_id) REFERENCES class_unit(id),
	CONSTRAINT teacher_class_teacher_fk FOREIGN KEY (teacher_id) REFERENCES teacher(id),
	CONSTRAINT teacher_class_pk PRIMARY KEY (teacher_id, class_id)
);

CREATE TABLE class_room (
	class_id UUID,
	room_id INT,
	penalty INT NOT NULL,
	CONSTRAINT class_room_class_fk FOREIGN KEY (class_id) REFERENCES class_unit(id),
	CONSTRAINT class_room_room_fk FOREIGN KEY (room_id) REFERENCES room(id),
	CONSTRAINT class_room_pk PRIMARY KEY (class_id, room_id)
);

CREATE TABLE room_distance (
	room_id_1 INT,
	room_id_2 INT,
	distance INT NOT NULL,
	CONSTRAINT room_distance_room1_fk FOREIGN KEY (room_id_1) REFERENCES room(id),
	CONSTRAINT room_distance_room2_fk FOREIGN KEY (room_id_2) REFERENCES room(id),
	CONSTRAINT room_distance_pk PRIMARY KEY (room_id_1, room_id_2)
);

CREATE TABLE room_unavailability (
	id SERIAL PRIMARY KEY,
	room_id INT NOT NULL,
	days VARCHAR(7) NOT NULL,
	weeks VARCHAR(16) NOT NULL,
	start_slot INT NOT NULL,
	duration INT NOT NULL,
	CONSTRAINT room_unavailability_room_fk FOREIGN KEY (room_id) REFERENCES room(id)
);

CREATE TABLE class_time (
	id UUID,
	class_id UUID,
	penalty INT NOT NULL,
	days VARCHAR(7) NOT NULL,
	start_slot INT NOT NULL,
	duration INT NOT NULL,
	weeks VARCHAR(16) NOT NULL,
	CONSTRAINT class_time_class_fk FOREIGN KEY (class_id) REFERENCES class_unit(id),
	CONSTRAINT class_time_pk PRIMARY KEY (id, class_id)
);

CREATE TABLE scheduled_lesson (
	id UUID,
	timetable_id UUID NOT NULL,
	class_id UUID NOT NULL,
	room_id INT,
	days VARCHAR(7) NOT NULL,
	weeks VARCHAR(16) NOT NULL,
	start_slot INT NOT NULL,
	duration INT NOT NULL,
	CONSTRAINT scheduled_lesson_class_fk FOREIGN KEY (class_id) REFERENCES class_unit(id),
	CONSTRAINT scheduled_lesson_room_fk FOREIGN KEY (room_id) REFERENCES room(id),
	CONSTRAINT scheduled_lesson_timetable_fk FOREIGN KEY (timetable_id) REFERENCES timetable(id),
	CONSTRAINT scheduled_lesson_pk PRIMARY KEY (id, timetable_id)
);

CREATE TABLE scheduled_lesson_teacher (
	scheduled_lesson_id UUID NOT NULL,
	scheduled_lesson_timetable_id UUID NOT NULL,
	teacher_id INT NOT NULL,
	CONSTRAINT scheduled_lesson_teacher_lesson_id_fk FOREIGN KEY (scheduled_lesson_id, scheduled_lesson_timetable_id) REFERENCES scheduled_lesson(id, timetable_id),
	CONSTRAINT scheduled_lesson_teacher_fk FOREIGN KEY (teacher_id) REFERENCES teacher(id),
	CONSTRAINT scheduled_lesson_teacher_pk PRIMARY KEY (scheduled_lesson_id, scheduled_lesson_timetable_id, teacher_id)
);

CREATE TABLE optimization_parameters (
	id SERIAL PRIMARY KEY,
	time_weight INT NOT NULL,
	room_weight INT NOT NULL,
	distribution_weight INT NOT NULL,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE configuration (
	id SERIAL PRIMARY KEY,
	number_days INT NOT NULL,
	number_weeks INT NOT NULL,
	slots_per_day INT NOT NULL,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);