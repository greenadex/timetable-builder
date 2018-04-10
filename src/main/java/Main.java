import model.StudentInfo;
import model.Timetable;
import model.exception.InvalidParameterException;
import picocli.CommandLine;
import utils.ParseURL;
import utils.TimetableBuilder;

import java.io.*;
import java.net.MalformedURLException;
import java.util.*;
import java.util.stream.Collectors;

public class Main implements Runnable {
    public static void main(String[] args) {
        System.setProperty("picocli.usage.width", "150");
        Main app = new Main();
        CommandLine commandLine = new CommandLine(app);
        commandLine.parse(args);
        if (commandLine.isUsageHelpRequested()) {
            commandLine.usage(System.out);
            return;
        }
        CommandLine.run(app, System.out, args);
    }

    @CommandLine.Parameters(index = "0", description = "The link to the timetable, in 'tabelar' form." +
                    "\nExample: './run.sh [...] http://www.cs.ubbcluj.ro/files/orar/2017-2/tabelar/IE3.html <group>'\n")
    private String link;

    @CommandLine.Parameters(index = "1",
            description = "The group of the student.\nExample: './run.sh [...] <link> 931'\n")
    private int group;

    @CommandLine.Option(names = "-semigroup",
            description = "The semigroup (1 or 2).\nExample: './run.sh -semigroup=1 [...] <link> <group>'\n")
    private int semigroup;

    @CommandLine.Option(names = "--help", usageHelp = true, description = "Display this help message.\n")
    private boolean usageHelpRequested;

    @CommandLine.Option(names = "-filter",
            description = "If toggled, the activities added will be only those present in 'filtered_activities.txt'.\n")
    private boolean filterActivities;

    @CommandLine.Option(names = "-debug",
            description = "Start the application in debug mode (the calendar will be deleted at the end).\n")
    private boolean debug;

    @Override
    public void run() {
        System.out.println(link);
        System.out.println(group);
        System.out.println(semigroup);
        System.out.println(usageHelpRequested);
        System.out.println(filterActivities);

        validateArgs();
        try {
            tryRunApp();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void validateArgs() {
        if (semigroup < 0 || semigroup > 2) {
            throw new InvalidParameterException("The semigroup given is not valid! The only accepted values are " +
                    "\'1\' and \'2\'");
        }
    }

    private void tryRunApp() throws IOException {
        StudentInfo student = new StudentInfo(link, group, semigroup );

        final ParseURL parseURL;
        try {
            parseURL = new ParseURL(student.timetableLink);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }

        final Timetable timetable;
        if (filterActivities) {
            List<String> filteredActivities = readFilteredActivities();
            timetable = new Timetable(parseURL, "" + group, "" + semigroup, filteredActivities);
        } else {
            timetable = new Timetable(parseURL, "" + group, "" + semigroup);
        }

        System.out.println("Creating new calendar...");
        String calendarId = TimetableBuilder.createCalendar(timetable);

        TimetableBuilder.addTimetable(calendarId, timetable);
        System.out.println("All done!");

        if (debug) {
            System.out.println("Press ENTER to delete the created calendar!");
            System.in.read();
            TimetableBuilder.deleteCalendar(calendarId);
            System.out.println("Calendar deleted!");
        }
    }

    private static List<String> readFilteredActivities() {
        try (BufferedReader reader = new BufferedReader(new FileReader("filtered_activities.txt"))) {
            return reader.lines()
                    .filter(line -> !line.isEmpty())
                    .map(String::trim)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
