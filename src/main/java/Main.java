import model.Timetable;
import utils.ParseURL;
import utils.TimetableBuilder;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created on 27.09.2016.
 */
public class Main {
    /**
     * Starting point of the application.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.printf("Input the link to the timetable (tabelar):\n");
        String link = scanner.nextLine();

        System.out.printf("Input the group:\n");
        String groupNumber = scanner.nextLine();

        String semiGroupNumber;
        do {
            System.out.printf("Input the semi-group (1, 2 or * for both semi-groups):\n");
            semiGroupNumber = scanner.nextLine();
        } while (!semiGroupNumber.equals("*") && !semiGroupNumber.equals("1") && !semiGroupNumber.equals("2"));

        ParseURL parseURL = new ParseURL(link);
        Timetable timetable = new Timetable(parseURL, groupNumber, semiGroupNumber);
        System.out.println("Creating new calendar...");
        String calendarId = TimetableBuilder.createCalendar(timetable);

        TimetableBuilder.addTimetable(calendarId, timetable);
        System.out.println("All done!");
    }
}
