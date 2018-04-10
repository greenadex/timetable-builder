package model;

public class SemesterInfo {
    private static final int NO_OF_WEEKS_FIRST_SEMESTER = 16;
    private static final int CHRISTMAS_HOLIDAY = 12;
    private static final int CHRISTMAS_HOLIDAY_LENGTH = 2;

    private static final int NO_OF_WEEKS_SECOND_SEMESTER = 13;
    private static final int EASTER_HOLIDAY = 6;
    private static final int EASTER_HOLIDAY_LENGTH = 1;

    private static final String START_DATE_FIRST_SEMESTER = "2017-10-02";
    private static final String START_DATE_SECOND_SEMESTER = "2018-02-26";

    public static int getStartYear() {
        return Integer.parseInt(START_DATE_FIRST_SEMESTER.substring(0, 4));
    }

    public static int getEndYear() {
        return Integer.parseInt(START_DATE_SECOND_SEMESTER.substring(0, 4));
    }

    public static int getNoOfWeeks(int semester) {
        return (semester == 1 ? NO_OF_WEEKS_FIRST_SEMESTER : NO_OF_WEEKS_SECOND_SEMESTER);
    }

    public static int getHolidayStartWeek(int semester) {
        return (semester == 1 ? CHRISTMAS_HOLIDAY : EASTER_HOLIDAY);
    }

    public static int getHolidayLength(int semester) {
        return (semester == 1 ? CHRISTMAS_HOLIDAY_LENGTH : EASTER_HOLIDAY_LENGTH);
    }

    public static String getStartDate(int semester) {
        return (semester == 1 ? START_DATE_FIRST_SEMESTER : START_DATE_SECOND_SEMESTER);
    }
}
