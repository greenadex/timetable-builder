import model.Timetable;
import utils.ParseURL;
import utils.TimetableBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Created on 27.09.2016.
 */
public class Main {
    private static boolean debugMode = false;
    /**
     * Starting point of the application.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 0 && args[0].equals("debug")) {
            debugMode = true;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("Input the link to the timetable (tabelar):");
        String link = scanner.nextLine();

        System.out.println("Input the group:");
        String groupNumber = scanner.nextLine();

        String semiGroupNumber;
        do {
            System.out.println("Input the semi-group (1, 2 or * for both semi-groups):");
            semiGroupNumber = scanner.nextLine();
        } while (!semiGroupNumber.equals("*") && !semiGroupNumber.equals("1") && !semiGroupNumber.equals("2"));

        ParseURL parseURL = new ParseURL(link);
        Timetable timetable = new Timetable(parseURL, groupNumber, semiGroupNumber);
        System.out.println("Creating new calendar...");
        String calendarId = TimetableBuilder.createCalendar(timetable);

        TimetableBuilder.addTimetable(calendarId, timetable);
        System.out.println("All done!");

        if (debugMode) {
            System.out.println("Press ENTER to delete the created calendar!");
            System.in.read();
            TimetableBuilder.deleteCalendar(calendarId);
            System.out.println("Calendar deleted!");
        }
    }
}
