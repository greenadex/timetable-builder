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
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Calendar;

import model.Activity;
import model.GoogleCalendarEvent;
import model.Timetable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.util.*;

public class TimetableBuilder {
    private static final String APPLICATION_NAME = "TimetableBuilder";
    private static final File DATA_STORE_DIR =
            new File(System.getProperty("user.home"), ".credentials/timetable-builder");
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

    public static String createCalendar(Timetable timetable) throws IOException {
        Calendar newCalendar = new Calendar();

        String summary = timetable.getSemiGroup().equals("0") ?
                timetable.getGroup() + " Sem." + timetable.getSemester() :
                timetable.getGroup() + "/" + timetable.getSemiGroup() + " Sem." + timetable.getSemester();
        newCalendar.setSummary(summary);

        String description = "Timetable for group " + timetable.getGroup() + ", semester " +
                timetable.getSemester() + ".\n\n\tRed - Lecture\n\tGreen - Seminar\n\tYellow - Laboratory";
        newCalendar.setDescription(description);

        newCalendar.setTimeZone("Europe/Bucharest");

        return service.calendars().insert(newCalendar).execute().getId();
    }

    public static void addTimetable(String calendarId, Timetable timetable) throws IOException {
        List<Activity> allActivities = timetable.getAllActivities();
        for (Activity activity : allActivities) {
            GoogleCalendarEvent event = new GoogleCalendarEvent(activity, timetable.getSemester());
            System.out.println("Generating event for " + event.getSummary());
            event.insertInCalendar(service, calendarId);
        }
    }

    public static void deleteCalendar(String calendarID) throws IOException {
        service.calendars().delete(calendarID).execute();
    }
}
