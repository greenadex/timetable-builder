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
    /**
     * The core information of the timetable
     */
    public static final int NO_OF_DAYS_IN_TIMETABLE = 7;

    private final Day[] days = new Day[NO_OF_DAYS_IN_TIMETABLE];
    private final String group;
    private final String semiGroup;
    private final int semester;

    private List<String> getTableOfGroup(List<String> htmlCode, final String group) {
        Pattern beginningOfTable = Pattern.compile("<table .*>");
        Pattern endOfTable = Pattern.compile("</table>");

        int wantedTable = group.charAt(group.length() - 1) - '0';
        int indexOfBeginning = -1, indexOfEnd = -1;

        for (int i = 0, foundTable = 0; i < htmlCode.size(); i++) {
            if (foundTable < wantedTable) {
                Matcher matcherTable = beginningOfTable.matcher(htmlCode.get(i));
                if (matcherTable.matches()) {
                    foundTable++;
                    if (wantedTable == foundTable) {
                        indexOfBeginning = i + 1;
                    }
                }
            }

            if (foundTable == wantedTable) {
                Matcher matcherEndTable = endOfTable.matcher(htmlCode.get(i));
                if (matcherEndTable.matches()) {
                    indexOfEnd = i;
                    break;
                }
            }
        }

        return htmlCode.subList(indexOfBeginning, indexOfEnd);
    }

    private Activity processRow(List<String> row) {
        Activity activity = new Activity();
        int currentColumn = 0;

        for (String column : row) {
            Pattern informationPattern = Pattern.compile(".*>([^<]+)<.*");
            Matcher matcher = informationPattern.matcher(column);
            if (matcher.find()) {
                String information = matcher.group(1);
                activity.setInformation(currentColumn, information);
            }
            currentColumn++;
        }

        return activity;
    }

    /**
     * Constructor for the timetable. It parses all the lines, storing the necessary information for the given group.
     *
     * @param website - a ParseURL object containing the HTML source code lines
     * @param group   - the group number
     * @throws IOException
     */
    public Timetable(ParseURL website, final String group, final String semiGroup) throws IOException {
        this.group = group;
        this.semiGroup = semiGroup;
        
        semester = website.getSemester();

        for (int i = 0; i < NO_OF_DAYS_IN_TIMETABLE; i++) {
            days[i] = new Day();
        }

        List<String> urlLines = website.getLines();

        Pattern beginningOfTable = Pattern.compile("<table .*>");
        Pattern header1 = Pattern.compile("<h1>.*</h1>");

        System.out.println(processRow(urlLines.subList(19, 28)));

        int currentRow = -1;
        int currentColumn = -1;

        boolean inTheRightTable = false, rowOutOfScope = false;
        Activity nextActivity = null;
        List<Activity> allActivities = new ArrayList<>();

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

                    //The first row does not contain any activities so we need to ignore it
                    if (currentRow >= 1) {
                        nextActivity = new Activity();
                    }

                    continue;
                }

                //Checks whether we have reached the end of a row
                if (line.substring(1, 4).equals("/tr") && currentRow >= 1) {
                    if (!rowOutOfScope || semiGroup.equals("*")) {
                        allActivities.add(nextActivity);
                    }
                    rowOutOfScope = false;
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

                    //Check if the current row belongs to another semigroup. If so,
                    //we mark it as out of scope
                    if (currentColumn == 4 && 
                        Pattern.compile(group + "/[^" + semiGroup + "]")
                        .matcher(information.toString())
                        .matches()) {
                        rowOutOfScope = true;
                    }

                    //Sets the information of the parsed activity
                    nextActivity.setInformation(currentColumn, information.toString());
                }
            }
        }

        //If no timetable for the group was found, throw an exception to mark the error
        if (!inTheRightTable) {
            throw new IllegalArgumentException("The timetable for group " + group + " was not found!");
        }

        //The timetable of the group was found, however it contained no activities
        if (allActivities.isEmpty()) {
            throw new IllegalArgumentException("No activities found");
        }

        //Divide the activities by day
        String currentDay = allActivities.get(0).getDay();
        int indexOfCurrentDay = FromDayToInteger.getInteger(currentDay);

        for (Activity currentActivity : allActivities) {
            if (!currentActivity.getDay().equals(currentDay)) {
                currentDay = currentActivity.getDay();
                indexOfCurrentDay = FromDayToInteger.getInteger(currentDay);
            }

            days[indexOfCurrentDay].addActivity(currentActivity);
        }

    }

    public List<Activity> getDays() {
        List<Activity> allActivities = new ArrayList<>();
        for (Day day : days) {
            allActivities.addAll(day.getActivities());
        }
        return allActivities;
    }

    public List<Activity> getDay(int index) {
        if (index < 0 || index > 6)
            throw new IllegalArgumentException("Invalid day");
        return days[index].getActivities();
    }

    public String getGroup() {
        return group;
    }

    public String getSemiGroup() {
        return semiGroup;
    }

    public int getSemester() {
        return semester;
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
