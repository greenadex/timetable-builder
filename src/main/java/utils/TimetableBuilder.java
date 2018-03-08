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

import model.Activity;
import model.Activity.Frequency;
import model.Timetable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class TimetableBuilder {
    private static final String APPLICATION_NAME = "TimetableBuilder";
    private static final File DATA_STORE_DIR = new File(System.getProperty("user.home"), ".credentials/timetable-builder");
    private static FileDataStoreFactory DATA_STORE_FACTORY;
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static HttpTransport HTTP_TRANSPORT;
    private static com.google.api.services.calendar.Calendar service;

    /**
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/timetable-builder
     */
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

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

    private static Credential authorize() throws IOException {
        InputStream in = TimetableBuilder.class.getResourceAsStream("/client.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("online")
                        .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    private static com.google.api.services.calendar.Calendar getCalendarService() throws IOException {
        Credential credential = authorize();
        return new com.google.api.services.calendar.Calendar.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private static LocalDate getStartingDateOfActivity(Activity activity, int semester) {
        String startingDate = SemesterInfo.getStartingDate(semester);
        LocalDate localStartingDate = LocalDate.of(Integer.parseInt(startingDate.substring(0, 4)),
                Integer.parseInt(startingDate.substring(5, 7)),
                Integer.parseInt(startingDate.substring(8)));

        localStartingDate = localStartingDate.plus(activity.getDayOfWeek().index, ChronoUnit.DAYS);
        return localStartingDate;
    }

    private static void setSummary(Event event, Activity activity) {
        String title = activity.getName();
        if (activity.getType() == Activity.Type.Laboratory && activity.getGroup().contains("/"))
            title += " " + activity.getGroup();
        event.setSummary(title);
    }

    private static void setLocation(Event event, Activity activity) {
        event.setLocation(activity.getRoom());
    }

    private static void setDescription(Event event, Activity activity) {
        event.setDescription(activity.getType().name() + ' ' + activity.getName() + " - "
                + activity.getProfessor());
    }

    private static void setColor(Event event, Activity activity) {
        final int colorId;

        switch (activity.getType()) {
            case Lecture:
                colorId = 4; // light red
                break;
            case Seminar:
                colorId = 10; // green
                break;
            case Laboratory:
                colorId = 5; // yellow
                break;
            default:
                throw new InvalidParameterException("No such activity type: " + activity.getType());
        }

        event.setColorId(Integer.toString(colorId));
    }

    private static void setStartAndEndDate(Event event, Activity activity, int semester) {
        String startingDate = getStartingDateOfActivity(activity, semester).toString(),
                timeZone = (semester == 1 ? ":00.000+03:00" : ":00.000+02:00");

        DateTime start = DateTime.parseRfc3339(startingDate + "T" + activity.getStartTime() + timeZone);
        DateTime end = DateTime.parseRfc3339(startingDate + "T" + activity.getEndTime() + timeZone);

        event.setStart(new EventDateTime().setDateTime(start).setTimeZone("Europe/Bucharest"));
        event.setEnd(new EventDateTime().setDateTime(end).setTimeZone("Europe/Bucharest"));
    }

    private static void setRecurrence(Event event, int semester) {
        String recurrence = "RRULE:FREQ=WEEKLY;COUNT=" + SemesterInfo.getNoOfWeeks(semester);
        event.setRecurrence(Collections.singletonList(recurrence));
    }

    private static void deleteExtraEvents(String calendarID, Activity activity, List<Event> items, int semester)
            throws IOException {
        int holidayLength = SemesterInfo.getHolidayLength(semester),
                startingWeekHoliday = SemesterInfo.getHolidayStartingWeek(semester);

        for (int week = 0; week < holidayLength; week++) {
            service.events().delete(calendarID, items.get(startingWeekHoliday + week).getId()).execute();
        }

        if (activity.getFrequency() == Activity.Frequency.Weekly) {
            return;
        }

        int activityParity = activity.getFrequency() == Frequency.EveryOddWeek ? 1 : 0;
        for (int week = activityParity; week < startingWeekHoliday; week += 2) {
            service.events().delete(calendarID, items.get(week).getId()).execute();
        }

        if (holidayLength % 2 != 0) {
            activityParity = 1 - activityParity;
        }

        for (int week = startingWeekHoliday + holidayLength + activityParity;
             week < SemesterInfo.getNoOfWeeks(semester); week += 2) {
            service.events().delete(calendarID, items.get(week).getId()).execute();
        }
    }

    private static void deleteExtraEvents(String calendarID, Activity activity, String eventID, int semester)
            throws IOException {
        String pageToken = null;
        do {
            Events events =
                    service.events().instances(calendarID, eventID).setPageToken(pageToken).execute();
            List<Event> items = events.getItems();
            deleteExtraEvents(calendarID, activity, items, semester);
            pageToken = events.getNextPageToken();
        } while (pageToken != null);
    }

    /**
     * Adds a new class (a new event) of the timetable to the calendar
     *
     * @param calendarId - the id of the calendar where the new event will be added
     * @param activity   - the Activity object holding the information of the class to be added
     * @param semester   - the activity's semester
     * @throws IOException
     */
    private static void addActivity(String calendarId, Activity activity, int semester) throws IOException {
        Event event = new Event();

        setSummary(event, activity);
        setLocation(event, activity);
        setDescription(event, activity);
        setColor(event, activity);
        setStartAndEndDate(event, activity, semester);
        setRecurrence(event, semester);

        System.out.println("Generating event for " + event.getSummary() + " " + activity.getType());
        event = service.events().insert(calendarId, event).execute();

        deleteExtraEvents(calendarId, activity, event.getId(), semester);
    }

    /**
     * Adds the timetable to the calendar
     *
     * @param calendarId - the id of the calendar where the timetable will be added
     * @param timetable  - the Timetable object which holds all the information of the calendar to be added
     * @throws IOException
     */
    public static void addTimetable(String calendarId, Timetable timetable) throws IOException {
        List<Activity> allActivities = timetable.getAllActivities();
        for (Activity nextActivity : allActivities) {
            addActivity(calendarId, nextActivity, timetable.getSemester());
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

        String summary = timetable.getSemiGroup().equals("*") ?
                timetable.getGroup() + " Sem." + timetable.getSemester() :
                timetable.getGroup() + "/" + timetable.getSemiGroup() + " Sem." + timetable.getSemester();
        newCalendar.setSummary(summary);

        String description = "Timetable for group " + timetable.getGroup() + " for the semester " +
                timetable.getSemester() + "\n\n\tRed - Course\n\tGreen - Seminar\n\tYellow - Laboratory";
        newCalendar.setDescription(description);

        newCalendar.setTimeZone("Europe/Bucharest");

        return service.calendars().insert(newCalendar).execute().getId();
    }

    public static void deleteCalendar(String calendarID) throws IOException {
        service.calendars().delete(calendarID).execute();
    }
}
