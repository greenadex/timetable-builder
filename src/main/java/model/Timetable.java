package model;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.ParseURL;

/**
 * Created on 10.09.2016.
 */
public class Timetable {
    private static final int NO_OF_DAYS_IN_TIMETABLE = 7;

    /**
     * Hardcoded details of the first semester of 2016-2017 at UBB
     */
    private static final int NO_OF_WEEKS_FIRST_SEMESTER = 16;
    private static final String STARTING_DATE_FIRST_SEMESTER = "2016-10-03";
    private static final int CHRISTMAS_HOLIDAY = 12;
    private static final int CHRISTMAS_HOLIDAY_LENGTH = 2;

    /**
     * Hardcoded details of the second semester of 2016-2017 at UBB
     */
    private static final int NO_OF_WEEKS_SECOND_SEMESTER = 15;
    private static final String STARTING_DATE_SECOND_SEMESTER = "2017-02-27";
    private static final int EASTER_HOLIDAY = 7;
    private static final int EASTER_HOLIDAY_LENGTH = 1;

    /**
     * The core information of the timetable
     */
    private final Day[] days = new Day[NO_OF_DAYS_IN_TIMETABLE];
    private final String group;
    private final int semester;

    /**
     * Dynamically decided details of the current semester
     */
    private final int currentNoOfWeeks;
    private final String currentStartingDate;
    private final int currentHolidayWeek;
    private final int currentHolidayLength;


    /**
     * Constructor for the timetable. It parses all the lines, storing the necessary information for the given group.
     *
     * @param website - a ParseURL object containing the HTML source code lines
     * @param group   - the group number
     * @throws IOException
     */
    public Timetable(ParseURL website, final String group) throws IOException {
        this.group = group;
        semester = website.getSemester();

        //Determine the information of the current semester (holiday length, number of weeks in the semester etc.)
        currentNoOfWeeks = semester == 1 ? NO_OF_WEEKS_FIRST_SEMESTER : NO_OF_WEEKS_SECOND_SEMESTER;
        currentStartingDate = semester == 1 ? STARTING_DATE_FIRST_SEMESTER : STARTING_DATE_SECOND_SEMESTER;
        currentHolidayWeek = semester == 1 ? CHRISTMAS_HOLIDAY : EASTER_HOLIDAY;
        currentHolidayLength = semester == 1 ? CHRISTMAS_HOLIDAY_LENGTH : EASTER_HOLIDAY_LENGTH;

        for (int i = 0; i < NO_OF_DAYS_IN_TIMETABLE; i++) {
            days[i] = new Day();
        }

        List<String> urlLines = website.getLines();

        Pattern beginningOfTable = Pattern.compile("<table .*>");
        Pattern header1 = Pattern.compile("<h1>.*</h1>");

        int currentRow = -1;
        int currentColumn = -1;

        boolean inTheRightTable = false;

        Class nextClass = null;
        List<Class> allClasses = new ArrayList<>();

        String currentGroup = "";

        for (String line : urlLines) {

            //Gets the current group in case it encounters a header
            Matcher matcherHeader = header1.matcher(line);
            if (matcherHeader.matches()) {
                currentGroup = line.substring(4, line.length() - 5);
                continue;
            }

            //Keeps track of the tables encountered; (each table represents the timetable of a group)
            //Determines whether we have reached the timetable of the desired group
            Matcher matcherTable = beginningOfTable.matcher(line);
            if (matcherTable.matches()) {
                if (inTheRightTable) {
                    break;
                }

                if (currentGroup.equals("Grupa " + group)) {
                    inTheRightTable = true;
                }

                continue;
            }

            //The following code will be executed if and only if we are currently parsing the table of the desired group
            if (inTheRightTable) {

                //Keep track of the table's row
                if (line.substring(1, 3).equals("tr")) {
                    currentRow++;

                    //Having reached a new row, we need to reset the column count
                    currentColumn = -1;

                    //The first row does not contain any classes so we need to ignore it
                    if (currentRow >= 1) {
                        nextClass = new Class();
                    }

                    continue;
                }

                //Checks whether we have reached the end of a row
                if (line.substring(1, 4).equals("/tr") && currentRow >= 1) {
                    allClasses.add(nextClass);
                    continue;
                }

                //Determines whether we have encountered a new column
                if (line.substring(1, 3).equals("td")) {
                    currentColumn++;

                    //noOfElem represents the number of HTML elements before the actual information that we need
                    int noOfElem = 1;

                    //the columns 3, 6, 7 contain another tag besides <td>: <a href>
                    if (currentColumn == 3 || currentColumn == 6 || currentColumn == 7) {
                        noOfElem = 2;
                    }

                    //Parses the line to get to the information needed
                    StringBuilder information = new StringBuilder();
                    boolean isInformation = false;
                    for (int i = 0, current = 0; i < line.length(); i++) {
                        char character = line.charAt(i);

                        if (character == '<' && isInformation)
                            break;

                        if (isInformation) {
                            information.append(character);
                            continue;
                        }

                        if (character == '>')
                            current++;

                        if (current == noOfElem)
                            isInformation = true;
                    }

                    //Sets the information of the parsed class
                    nextClass.setInformation(currentColumn, information.toString());
                }
            }
        }

        //If no timetable for the group was found, throw an exception to mark the error
        if (!inTheRightTable) {
            throw new IllegalArgumentException("The timetable for group " + group + " was not found!");
        }

        //The timetable of the group was found, however it contained no classes
        if (allClasses.isEmpty()) {
            throw new IllegalArgumentException("No classes in timetable found!");
        }

        //Divide the classes by day
        String currentDay = allClasses.get(0).getDay();
        int indexOfCurrentDay = FromDayToInteger.getInteger(currentDay);

        for (Class currentClass : allClasses) {
            if (!currentClass.getDay().equals(currentDay)) {
                currentDay = currentClass.getDay();
                indexOfCurrentDay = FromDayToInteger.getInteger(currentDay);
            }

            days[indexOfCurrentDay].addClass(currentClass);
        }

    }

    public List<Class> getDays() {
        List<Class> allClasses = new ArrayList<>();
        for (Day day : days) {
            allClasses.addAll(day.getClasses());
        }
        return allClasses;
    }

    public List<Class> getDay(int index) {
        if (index < 0 || index > 6)
            throw new IllegalArgumentException("Invalid day");
        return days[index].getClasses();
    }

    public String getGroup() {
        return group;
    }

    public int getSemester() {
        return semester;
    }

    public int getCurrentNoOfWeeks() {
        return currentNoOfWeeks;
    }

    public String getCurrentStartingDate() {
        return currentStartingDate;
    }

    public int getCurrentHolidayWeek() {
        return currentHolidayWeek;
    }

    public int getCurrentHolidayLength() {
        return currentHolidayLength;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (Day day : days) {
            output.append(day);
        }
        return output.toString();
    }
}
