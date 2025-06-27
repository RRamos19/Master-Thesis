INSERT INTO configuration (number_days, number_weeks, slots_per_day)
VALUES (7, 9, 288);

INSERT INTO optimization_parameters (time_weight, room_weight, distribution_weight)
VALUES (1, 1, 1);

INSERT INTO constraint_type (name)
VALUES
('SameStart'),
('SameTime'),
('DifferentTime'),
('SameDays'),
('DifferentDays'),
('SameWeeks'),
('DifferentWeeks'),
('Overlap'),
('NotOverlap'),
('SameRoom'),
('DifferentRoom'),
('SameAttendees'),
('Precedence'),
('WorkDay'),
('MinGap'),
('MaxDays'),
('MaxDayLoad'),
('MaxBreaks'),
('MaxBlock')