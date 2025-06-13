-- Function to delete the optimization_parameters instances
CREATE FUNCTION delete_oldest_optimization_parameters()
RETURNS TRIGGER AS $$
BEGIN
  IF (SELECT COUNT(*) FROM optimization_parameters) > 1 THEN
    DELETE FROM optimization_parameters
    WHERE (time_weight, room_weight, distribution_weight) IN
	(
      SELECT time_weight, room_weight, distribution_weight
      FROM optimization_parameters
      ORDER BY created_at ASC
      LIMIT 1
    );
  END IF;
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Trigger that prevents the table optimization_parameters from having more than 1 row
CREATE TRIGGER trigger_delete_oldest_optimization_parameters
AFTER INSERT ON optimization_parameters
FOR EACH STATEMENT
EXECUTE FUNCTION delete_oldest_optimization_parameters();

-- Function to delete the timetable_configuration instances
CREATE FUNCTION delete_oldest_configuration()
RETURNS TRIGGER AS $$
BEGIN
  IF (SELECT COUNT(*) FROM configuration) > 1 THEN
    DELETE FROM configuration
    WHERE (number_days, number_weeks, slots_per_day) IN
	(
      SELECT number_days, number_weeks, slots_per_day
      FROM configuration
      ORDER BY created_at ASC
      LIMIT 1
    );
  END IF;
  RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Trigger that prevents the table timetable_configuration from having more than 1 row
CREATE TRIGGER trigger_delete_oldest_configuration
AFTER INSERT ON configuration
FOR EACH STATEMENT
EXECUTE FUNCTION delete_oldest_configuration();