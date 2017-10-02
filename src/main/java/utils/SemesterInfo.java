package utils;

public class SemesterInfo {
    public static final int NO_OF_WEEKS_FIRST_SEMESTER = 16;
    public static final int CHRISTMAS_HOLIDAY = 12;
    public static final int CHRISTMAS_HOLIDAY_LENGTH = 2;

    public static final int NO_OF_WEEKS_SECOND_SEMESTER = 15;
    public static final int EASTER_HOLIDAY = 7;
    public static final int EASTER_HOLIDAY_LENGTH = 1;
    
    public static final String STARTING_DATE_FIRST_SEMESTER = "2017-10-02";
    public static final String STARTING_DATE_SECOND_SEMESTER = "2017-02-27";

    public static int getNoOfWeeks(int semester) {
        return (semester == 1 ? NO_OF_WEEKS_FIRST_SEMESTER : NO_OF_WEEKS_SECOND_SEMESTER);
    }

    public static int getHolidayStartingWeek(int semester) {
        return (semester == 1 ? CHRISTMAS_HOLIDAY : EASTER_HOLIDAY);
    }

    public static int getHolidayLength(int semester) {
        return (semester == 1 ? CHRISTMAS_HOLIDAY_LENGTH : EASTER_HOLIDAY_LENGTH);
    }

    public static String getStartingDate(int semester) {
        return (semester == 1 ? STARTING_DATE_FIRST_SEMESTER : STARTING_DATE_SECOND_SEMESTER);
    }
}
