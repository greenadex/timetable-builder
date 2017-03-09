package model;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import utils.ParseURL;

/**
 * Created on 10.09.2016.
 */
public class Timetable {
    /**
     * The core information of the timetable
     */

    private final String group;
    private final String semiGroup;
    private final int semester;
    private List<Activity> allActivities;

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
        allActivities = new ArrayList<>();
        semester = website.getSemester();

        List<String> htmlCode = website.getLines();

        if (!tableExists(htmlCode, group)) {
            throw new InvalidParameterException("The given URL does not contain the timetable for group " + group);
        }

        List<String> tableOfGroup = getTableOfGroup(htmlCode, group);
        List<List<String>> rows = divideIntoRows(tableOfGroup);
        rows.forEach(row -> allActivities.add(processRow(row)));
        filterActivities();
    }

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

    private List<List<String>> divideIntoRows(List<String> table) {
        Pattern beginningOfRow = Pattern.compile("<tr .*>");
        Pattern endOfRow = Pattern.compile("</tr>");

        List<List<String>> rows = new ArrayList<>();
        List<String> currentRow = new ArrayList<>();

        boolean inRow = false;
        for (String line : table) {
            Matcher endOfRowMatcher = endOfRow.matcher(line);
            if (endOfRowMatcher.matches()) {
                rows.add(currentRow);
                currentRow = new ArrayList<>();
                inRow = false;
            }

            if (inRow) {
                currentRow.add(line);
            } else {
                Matcher beginningOfRowMatcher = beginningOfRow.matcher(line);
                if (beginningOfRowMatcher.matches()) {
                    inRow = true;
                }
            }
        }

        //The first row is the header of the table. Given that it does not contain any useful
        //information, it should be ignored
        return rows.subList(1, rows.size());
    }

    private boolean tableExists(List<String> htmlCode, String group) {
        Pattern groupPattern = Pattern.compile(".*Grupa " + group + ".*");
        return htmlCode.stream()
                .anyMatch(line -> groupPattern.matcher(line).matches());
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

    private void filterActivities() {
        allActivities = allActivities.stream()
                .filter(activity -> activity.isForSemiGroup(semiGroup))
                .collect(Collectors.toList());
    }

    public List<Activity> getAllActivities() {
        return allActivities;
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
        for (Activity activity : allActivities) {
            output.append(activity);
        }
        return output.toString();
    }
}
