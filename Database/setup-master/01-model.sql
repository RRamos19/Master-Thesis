CREATE TABLE tb_program (
	id SERIAL PRIMARY KEY,
	name VARCHAR(30) NOT NULL UNIQUE,
	time_weight SMALLINT NOT NULL,
	room_weight SMALLINT NOT NULL,
	distribution_weight SMALLINT NOT NULL,
	number_days SMALLINT NOT NULL,
	number_weeks INT NOT NULL,
	slots_per_day SMALLINT NOT NULL,
	last_updated_at TIMESTAMP NOT NULL
);

CREATE TABLE time_block (
	id SERIAL PRIMARY KEY,
	start_slot SMALLINT NOT NULL,
	duration SMALLINT NOT NULL,
	days SMALLINT NOT NULL,
	weeks INT NOT NULL,
	CONSTRAINT time_block_unique UNIQUE(start_slot, duration, days, weeks)
);

CREATE TABLE teacher (
    id INT PRIMARY KEY,
    name VARCHAR(30) NOT NULL
);

CREATE TABLE course (
	id SERIAL PRIMARY KEY,
	program_id INT NOT NULL,
    name VARCHAR(10) NOT NULL UNIQUE,
	CONSTRAINT course_program_fk FOREIGN KEY (program_id) REFERENCES tb_program(id)
);

CREATE TABLE config (
	id SERIAL PRIMARY KEY,
	course_id INT,
	name VARCHAR(10) NOT NULL UNIQUE,
	CONSTRAINT config_course_fk FOREIGN KEY (course_id) REFERENCES course(id)
);

CREATE TABLE subpart (
	id SERIAL PRIMARY KEY,
	config_id INT,
	name VARCHAR(10) NOT NULL UNIQUE,
	CONSTRAINT subpart_config_fk FOREIGN KEY (config_id) REFERENCES config(id)
);

CREATE TABLE class_unit (
	id SERIAL PRIMARY KEY,
	subpart_id INT,
    name VARCHAR(10) NOT NULL UNIQUE,
	parent_class_id INT,
	CONSTRAINT class_class_parent_fk FOREIGN KEY (parent_class_id) REFERENCES class_unit(id),
	CONSTRAINT class_subpart_fk FOREIGN KEY (subpart_id) REFERENCES subpart(id)
);

CREATE TABLE timetable (
    id SERIAL PRIMARY KEY,
	program_id INT NOT NULL,
	creation_date TIMESTAMP NOT NULL,
	CONSTRAINT timetable_program_fk FOREIGN KEY (program_id) REFERENCES tb_program(id)
);

CREATE TABLE constraint_type (
	id SERIAL PRIMARY KEY,
	name VARCHAR(30) UNIQUE
);

CREATE TABLE timetable_constraint (
	id SERIAL PRIMARY KEY,
	constraint_type_id INT NOT NULL,
	penalty INT,
	required BOOL,
	first_parameter INT,
	second_parameter INT,
	CONSTRAINT constraint_type_id_fk FOREIGN KEY (constraint_type_id) REFERENCES constraint_type(id)
);

CREATE TABLE class_constraint (
	class_id INT NOT NULL,
	constraint_id INT NOT NULL,
	CONSTRAINT class_constraint_class_fk FOREIGN KEY (class_id) REFERENCES class_unit(id),
	CONSTRAINT class_contraint_constraint_fk FOREIGN KEY (constraint_id) REFERENCES timetable_constraint(id),
	CONSTRAINT class_contraint_pk PRIMARY KEY (class_id, constraint_id)
);

CREATE TABLE room (
	id SERIAL PRIMARY KEY,
	name VARCHAR(6) NOT NULL UNIQUE
);

CREATE TABLE teacher_unavailability (
	id SERIAL,
	teacher_id INT NOT NULL,
	time_block_id INT NOT NULL,
	CONSTRAINT teacher_unavailability_time_block_fk FOREIGN KEY (time_block_id) REFERENCES time_block(id),
	CONSTRAINT teacher_unavailability_teacher_fk FOREIGN KEY (teacher_id) REFERENCES teacher(id),
	CONSTRAINT teacher_unavailability_pk PRIMARY KEY (id, teacher_id)
);

CREATE TABLE teacher_class (
	teacher_id INT,
	class_id INT,
	CONSTRAINT teacher_class_class_fk FOREIGN KEY (class_id) REFERENCES class_unit(id),
	CONSTRAINT teacher_class_teacher_fk FOREIGN KEY (teacher_id) REFERENCES teacher(id),
	CONSTRAINT teacher_class_pk PRIMARY KEY (teacher_id, class_id)
);

CREATE TABLE class_room (
	class_id INT,
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
	time_block_id INT NOT NULL,
	CONSTRAINT room_unavailability_time_block_fk FOREIGN KEY (time_block_id) REFERENCES time_block(id),
	CONSTRAINT room_unavailability_room_fk FOREIGN KEY (room_id) REFERENCES room(id)
);

CREATE TABLE class_time (
	class_id INT,
	time_block_id INT,
	penalty INT NOT NULL,
	CONSTRAINT class_time_time_block_fk FOREIGN KEY (time_block_id) REFERENCES time_block(id),
	CONSTRAINT class_time_class_fk FOREIGN KEY (class_id) REFERENCES class_unit(id),
	CONSTRAINT class_time_pk PRIMARY KEY (class_id, time_block_id)
);

CREATE TABLE scheduled_lesson (
	timetable_id INT NOT NULL,
	class_id INT NOT NULL,
	room_id INT,
	time_block_id INT NOT NULL,
	CONSTRAINT scheduled_lesson_time_block_fk FOREIGN KEY (time_block_id) REFERENCES time_block(id),
	CONSTRAINT scheduled_lesson_class_fk FOREIGN KEY (class_id) REFERENCES class_unit(id),
	CONSTRAINT scheduled_lesson_room_fk FOREIGN KEY (room_id) REFERENCES room(id),
	CONSTRAINT scheduled_lesson_timetable_fk FOREIGN KEY (timetable_id) REFERENCES timetable(id),
	CONSTRAINT scheduled_lesson_pk PRIMARY KEY (timetable_id, class_id, time_block_id)
);

CREATE TABLE scheduled_lesson_teacher (
	scheduled_lesson_timetable_id INT NOT NULL,
	scheduled_lesson_class_id INT NOT NULL,
	scheduled_lesson_time_block_id INT NOT NULL,
	teacher_id INT NOT NULL,
	CONSTRAINT scheduled_lesson_teacher_lesson_id_fk FOREIGN KEY (scheduled_lesson_timetable_id, scheduled_lesson_class_id, scheduled_lesson_time_block_id) REFERENCES scheduled_lesson(timetable_id, class_id, time_block_id),
	CONSTRAINT scheduled_lesson_teacher_fk FOREIGN KEY (teacher_id) REFERENCES teacher(id),
	CONSTRAINT scheduled_lesson_teacher_pk PRIMARY KEY (scheduled_lesson_timetable_id, scheduled_lesson_class_id, scheduled_lesson_time_block_id, teacher_id)
);