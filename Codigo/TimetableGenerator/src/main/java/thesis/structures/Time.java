package thesis.structures;

import java.util.regex.Pattern;

public class Time {
    private static final Pattern VALID_CHARS_PATTERN = Pattern.compile("^[01]+$");

    private final String days;
    private final int start;
    private final int duration;
    private final String weeks;

    public Time(String days, int start, int duration, String weeks){
        // If the arguments days or weeks contain an invalid string an exception is thrown
        if(!validBinaryString(days) || !validBinaryString(weeks)){
            throw new RuntimeException("The binary representation of days or weeks doesn't make sense");
        }

        this.days = days;
        this.start = start;
        this.duration = duration;
        this.weeks = weeks;
    }

    private Boolean validBinaryString(String str){
        return VALID_CHARS_PATTERN.matcher(str).matches();
    }
}
