import model.StudentInfo;
import model.Timetable;
import model.exception.InvalidParameterException;
import picocli.CommandLine;
import utils.TimetableURL;
import utils.TimetableBuilder;

import java.io.*;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        System.setProperty("picocli.usage.width", "150");
        System.out.println();

        Main application;
        try {
            application = CommandLine.populateCommand(new Main(), args);
        } catch (Exception err) {
            System.out.println(
                    err.getMessage() + ". Run the app with the '--help' flag in order to show the help message.");
            return;
        }

        if (application.usageHelpRequested) {
            CommandLine.usage(application, System.out);
            return;
        }

        application.run();
    }

    @CommandLine.Parameters(index = "0", description = "The link to the timetable, in 'tabelar' form." +
                    "\nExample: './run.sh [...] http://www.cs.ubbcluj.ro/files/orar/2017-2/tabelar/IE3.html <group>'\n")
    private String link;

    @CommandLine.Parameters(index = "1",
            description = "The group of the student.\nExample: './run.sh [...] <link> 931'\n")
    private int group;

    @CommandLine.Option(names = {"-semigroup"},
            description = "The semigroup (1 or 2).\nExample: './run.sh -semigroup=1 [...] <link> <group>'\n")
    private int semigroup;

    @CommandLine.Option(names = {"-f", "--filter"},
            description = "If toggled, the activities added will be only those present in 'filtered_activities.txt'.\n")
    private boolean filterActivities;

    @CommandLine.Option(names = { "-d", "--debug"},
            description = "Start the application in debug mode (the calendar will be deleted at the end).\n")
    private boolean debug;

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message.\n")
    private boolean usageHelpRequested;

    private void run() {
        validateArgs();
        try {
            buildTimetable();
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
    }

    private void validateArgs() {
        if (semigroup < 0 || semigroup > 2) {
            throw new InvalidParameterException("The semigroup given is not valid! The only accepted values are " +
                    "\'1\' and \'2\' (or 0 for both semigroups)");
        }
    }

    private void buildTimetable() throws IOException, GeneralSecurityException {
        StudentInfo student = new StudentInfo(link, group, semigroup);

        final TimetableURL timetableURL;
        try {
            timetableURL = new TimetableURL(student.timetableLink);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }

        final Timetable timetable;
        if (filterActivities) {
            List<String> filteredActivities = readFilteredActivities();
            timetable = new Timetable(timetableURL, "" + group, "" + semigroup, filteredActivities);
        } else {
            timetable = new Timetable(timetableURL, "" + group, "" + semigroup);
        }

        System.out.println("Creating new calendar...");
        TimetableBuilder builder = new TimetableBuilder(timetable);
        System.out.println("All done!");

        if (debug) {
            System.out.println("Press ENTER to delete the created calendar!");
            final int userSignal = System.in.read();
            builder.deleteCalendar();
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
