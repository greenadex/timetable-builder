import model.Timetable;
import utils.ParseURL;
import utils.TimetableBuilder;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by 27.09.2016.
 */
public class Main {
    /**
     * Starting point of the application
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.printf("Input the link to the timetable (tabelar): ");
        String link = scanner.nextLine();

        System.out.printf("Input the group: ");
        String groupNumber = scanner.nextLine();

        ParseURL parseURL;

        try {
            parseURL = new ParseURL(link);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Timetable timetable = new Timetable(parseURL, groupNumber);
        System.out.println("Reading has finished");

        String calendarId = TimetableBuilder.createCalendar(timetable);
        System.out.println("The new calendar was created!");

        TimetableBuilder.addTimetableInCalendar(calendarId, timetable);
        System.out.println("All done!");
    }
}
