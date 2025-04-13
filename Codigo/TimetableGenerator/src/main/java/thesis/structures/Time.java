package thesis.structures;

import java.util.regex.Pattern;

public class Time {
    private static final Pattern VALID_CHARS_PATTERN = Pattern.compile("^[01]+$");

    private String days;
    private int start;
    private int length;
    private String weeks;

    public Time(String days, int start, int length, String weeks){
        // If the arguments days or weeks contain an invalid string an exception is thrown
        if(!validBinaryString(days) || !validBinaryString(weeks)){
            // TODO: Confirmar isto
            throw new RuntimeException();
        }

        this.days = days;
        this.start = start;
        this.length = length;
        this.weeks = weeks;
    }

    private Boolean validBinaryString(String str){
        return VALID_CHARS_PATTERN.matcher(str).matches();
    }
}
