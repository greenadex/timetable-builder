package utils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.*;
import com.google.api.services.calendar.model.Calendar;
import model.Class;
import model.FromDayToInteger;
import model.Timetable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Created on 26.09.2016
 */
public class TimetableBuilder {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME =
            "utils.TimetableBuilder";

    /**
     * Directory to store user credentials for this application.
     */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), ".credentials/timetable-builder");

    /**
     * Global instance of the {@link FileDataStoreFactory}.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;

    /**
     * Global instance of the Calendar service.
     */
    private static com.google.api.services.calendar.Calendar service;

    /**
     * Global instance of the scopes required by this application.
     * <p>
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/timetable-builder
     */
    private static final List<String> SCOPES =
            Collections.singletonList(CalendarScopes.CALENDAR);


    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
            service = getCalendarService();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    private static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
                TimetableBuilder.class.getResourceAsStream("/client.json");
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("online")
                        .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
//        System.out.println(
//                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Calendar client service.
     *
     * @return an authorized Calendar client service
     * @throws IOException
     */
    private static com.google.api.services.calendar.Calendar
    getCalendarService() throws IOException {
        Credential credential = authorize();
        return new com.google.api.services.calendar.Calendar.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }


    /**
     * Adds a new class (a new event) of the timetable to the calendar
     *
     * @param calendarId           - the id of the calendar where the new event will be added
     * @param newClass             - the Class object holding the information of the class to be added
     * @param noOfWeeks            - the number of weeks of the current semester
     * @param startingDateSemester - the starting date of the current semester
     * @param holidayWeek          - the starting holiday of the current semester
     * @param holidayLength        - the length of the holiday
     * @throws IOException
     */
    private static void addClass(String calendarId, Class newClass, int noOfWeeks, String startingDateSemester,
                                 int holidayWeek, int holidayLength) throws IOException {
        Event event = new Event();

        //Set summary, title, location and description
        String title = newClass.getNameOfClass();
        if (newClass.getTypeOfClass().equals("Laborator") && newClass.getGroup().contains("/"))
            title += " " + newClass.getGroup();

        event.setSummary(title);
        event.setLocation(newClass.getClassRoom());
        event.setDescription(newClass.getTypeOfClass() + ' ' + newClass.getNameOfClass() + " - "
                + newClass.getProfessor());


        //Set colors
        int colorId = 4; //light red
        //11 for dark red

        if (newClass.getTypeOfClass().equals("Seminar"))
            colorId = 10; //green

        if (newClass.getTypeOfClass().equals("Laborator"))
            colorId = 5; //yellow

        event.setColorId(Integer.toString(colorId));


        //Get real starting date of the event
        LocalDate localStartingDate = LocalDate.of(Integer.parseInt(startingDateSemester.substring(0, 4)),
                Integer.parseInt(startingDateSemester.substring(5, 7)),
                Integer.parseInt(startingDateSemester.substring(8)));

        localStartingDate = localStartingDate.plus(FromDayToInteger.getInteger(newClass.getDay()), ChronoUnit.DAYS);

        if (newClass.getFrequency().contains("2")) {
            localStartingDate = localStartingDate.plus(1, ChronoUnit.WEEKS);
        }

        String startingDate = localStartingDate.toString();


        //Frequency and format interval
        int frequency = noOfWeeks;
        if (!newClass.getFrequency().isEmpty())
            frequency /= 2;
        String formatInterval = (frequency == noOfWeeks ? "" : "INTERVAL=2;");


        //Recurrence rule with exception dates
        List<String> recurrenceSet = new ArrayList<>();

        //holiday week real = holidayWeek + 1
        int endException = holidayWeek + holidayLength;

        StringBuilder exDate = new StringBuilder("EXDATE;TZID=Europe/Bucharest:");

        int decrement = 1;
        if (holidayWeek == 12) { //checks whether we're in the first semester or not
            decrement = 0;
        }
        //Daylight savings time problem
        int hour = Integer.parseInt(newClass.getStartingHour().substring(0, 2)) - decrement;
        String hourFormat = Integer.toString(hour);
        if (hourFormat.length() == 1)
            hourFormat = "0" + hourFormat;
        String hourClass = "T" + hourFormat + newClass.getStartingHour().substring(3) + "00";

        boolean exDateExists = false;
        int desiredModulus = newClass.getFrequency().contains("2") ? 1 : 0;
        for (int i = holidayWeek; i < endException; i++) {
            if (frequency == noOfWeeks) {
                exDateExists = true;
                LocalDate localExceptionDate = localStartingDate.plus(i, ChronoUnit.WEEKS);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                String formattedDay = formatter.format(localExceptionDate);
                exDate.append(formattedDay).append(hourClass);
                if (endException - i > 1)
                    exDate.append(",");
            } else {
                if (i % 2 == desiredModulus) {
                    exDateExists = true;
                    LocalDate localExceptionDate = localStartingDate.plus(i - desiredModulus, ChronoUnit.WEEKS);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                    String formattedDay = formatter.format(localExceptionDate);
                    exDate.append(formattedDay).append(hourClass);
                }
            }
        }
        if (exDateExists) {
            recurrenceSet.add(exDate.toString());
        }

        recurrenceSet.add("RRULE:FREQ=WEEKLY;COUNT=" + frequency + ";" + formatInterval);

        //Set start and end for event; set recurrence
        DateTime start = DateTime.parseRfc3339(startingDate + "T" + newClass.getStartingHour() + ":00.000+03:00");
        DateTime end = DateTime.parseRfc3339(startingDate + "T" + newClass.getEndingHour() + ":00.000+03:00");

        event.setStart(new EventDateTime().setDateTime(start).setTimeZone("Europe/Bucharest"));
        event.setEnd(new EventDateTime().setDateTime(end).setTimeZone("Europe/Bucharest"));
        event.setRecurrence(recurrenceSet);

        System.out.println("Generating event for " + event.getSummary() + " " + newClass.getTypeOfClass());
        service.events().insert(calendarId, event).execute();
    }

    /**
     * Adds the timetable to the calendar
     *
     * @param calendarId - the id of the calendar where the timetable will be added
     * @param timetable  - the Timetable object which holds all the information of the calendar to be added
     * @throws IOException
     */
    public static void addTimetableInCalendar(String calendarId, Timetable timetable) throws IOException {
        List<Class> allClasses = timetable.getDays();
        for (Class nextClass : allClasses) {
            addClass(calendarId, nextClass, timetable.getCurrentNoOfWeeks(),
                    timetable.getCurrentStartingDate(), timetable.getCurrentHolidayWeek(),
                    timetable.getCurrentHolidayLength());
        }
    }

    /**
     * Creates a new calendar with an appropriate description for a timetable
     *
     * @param timetable - the timetable to be added in the newly created calendar
     * @return the id of the calendar created
     * @throws IOException
     */
    public static String createCalendar(Timetable timetable) throws IOException {
        Calendar newCalendar = new Calendar();

        newCalendar.setSummary("Timetable " + timetable.getGroup() + " Sem." + timetable.getSemester());
        newCalendar.setDescription("Here is the timetable for group " + timetable.getGroup() + " for the semester "
                + timetable.getSemester() + "\n\n\tRed - Course\n\tGreen - Seminar\n\tYellow - Laboratory");
        newCalendar.setTimeZone("Europe/Bucharest");

        return service.calendars().insert(newCalendar).execute().getId();
    }


}
