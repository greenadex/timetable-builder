package model;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GoogleCalendarService {
    private static final String APPLICATION_NAME = "TimetableBuilder";
    private static final File DATA_STORE_DIR =
            new File(System.getProperty("user.home"), ".credentials/timetable-builder");
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/timetable-builder
     */
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);

    private final FileDataStoreFactory dataStoreFactory;
    private final HttpTransport httpTransport;
    private final com.google.api.services.calendar.Calendar service;

    public GoogleCalendarService() throws IOException, GeneralSecurityException {
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);
        service = getCalendarService();
    }

    private com.google.api.services.calendar.Calendar getCalendarService() throws IOException {
        Credential credential = authorize();
        return new com.google.api.services.calendar.Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential authorize() throws IOException {
        InputStream in = GoogleCalendarService.class.getResourceAsStream("/client.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(dataStoreFactory)
                        .setAccessType("online")
                        .build();
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    public String insertCalendar(com.google.api.services.calendar.model.Calendar calendar) throws IOException {
        return service.calendars().insert(calendar).execute().getId();
    }

    public Event insertEventInCalendarWithId(String calendarId, Event event) throws IOException {
        return service.events().insert(calendarId, event).execute();
    }

    public List<Event> getRecurringEvents(String calendarId, String eventId) throws IOException {
        final List<Event> events = new ArrayList<>();
        String pageToken = null;
        do {
            Events additionalEvents = service.events().instances(calendarId, eventId).setPageToken(pageToken).execute();
            events.addAll(additionalEvents.getItems());
            pageToken = additionalEvents.getNextPageToken();
        } while (pageToken != null);
        return events;
    }

    public void deleteEventWithId(String calendarId, String eventId) throws IOException {
        service.events().delete(calendarId, eventId).execute();
    }

    public void deleteCalendar(String calendarId) throws IOException {
        service.calendars().delete(calendarId).execute();
    }
}
