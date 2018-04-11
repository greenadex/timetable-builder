package utils;

import com.google.api.services.calendar.model.Calendar;

import model.Activity;
import model.GoogleCalendarEvent;
import model.GoogleCalendarService;
import model.Timetable;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

public class TimetableBuilder {
    private final GoogleCalendarService service;
    private final Timetable timetable;
    private final String calendarId;

    public TimetableBuilder(Timetable timetable) throws IOException, GeneralSecurityException {
        service = new GoogleCalendarService();
        this.timetable = timetable;
        calendarId = createCalendar();
        addActivities();
    }

    private String createCalendar() throws IOException {
        Calendar calendar = new Calendar();

        String summary = timetable.getSemiGroup().equals("0") ?
                timetable.getGroup() + " Sem." + timetable.getSemester() :
                timetable.getGroup() + "/" + timetable.getSemiGroup() + " Sem." + timetable.getSemester();
        calendar.setSummary(summary);

        String description = "Timetable for group " + timetable.getGroup() + ", semester " +
                timetable.getSemester() + ".\n\n\tRed - Lecture\n\tGreen - Seminar\n\tYellow - Laboratory";
        calendar.setDescription(description);

        calendar.setTimeZone("Europe/Bucharest");

        return service.insertCalendar(calendar);
    }

    private void addActivities() throws IOException {
        List<Activity> allActivities = timetable.getAllActivities();
        for (Activity activity : allActivities) {
            GoogleCalendarEvent event = new GoogleCalendarEvent(activity, timetable.getSemester());
            System.out.println("Generating event for " + event.getSummary());
            event.insertInCalendar(service, calendarId);
        }
    }

    public void deleteCalendar() throws IOException {
        service.deleteCalendar(calendarId);
    }
}
