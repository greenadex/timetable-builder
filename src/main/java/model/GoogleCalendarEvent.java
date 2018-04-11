package model;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import model.exception.InvalidParameterException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

public class GoogleCalendarEvent {
    private Event event;
    private final Activity activity;
    private final int semester;

    public GoogleCalendarEvent(Activity activity, int semester) {
        event = new Event();
        this.activity = activity;
        this.semester = semester;

        setSummary();
        setDescription();
        setStartAndEndDate();
        setRecurrence();
        setLocation();
        setColor();
    }

    private void setSummary() {
        String activityType = activity.getType().toString().substring(0, 3);
        String title = String.format("(%s) %s", activityType, activity.getName());

        if (activity.getType() == Activity.Type.Laboratory && activity.getGroup().contains("/"))
            title += " " + activity.getGroup();
        event.setSummary(title);
    }

    private void setDescription() {
        event.setDescription(activity.getType().name() + ' ' + activity.getName() + " - "
                + activity.getProfessor());
    }

    private void setStartAndEndDate() {
        String startingDate = getStartingDateOfActivity().toString(),
                timeZone = (semester == 1 ? ":00.000+03:00" : ":00.000+02:00");

        DateTime start = DateTime.parseRfc3339(startingDate + "T" + activity.getStartTime() + timeZone);
        DateTime end = DateTime.parseRfc3339(startingDate + "T" + activity.getEndTime() + timeZone);

        event.setStart(new EventDateTime().setDateTime(start).setTimeZone("Europe/Bucharest"));
        event.setEnd(new EventDateTime().setDateTime(end).setTimeZone("Europe/Bucharest"));
    }

    private LocalDate getStartingDateOfActivity() {
        String startingDate = SemesterInfo.getStartDate(semester);
        LocalDate localStartingDate = LocalDate.of(Integer.parseInt(startingDate.substring(0, 4)),
                Integer.parseInt(startingDate.substring(5, 7)),
                Integer.parseInt(startingDate.substring(8)));

        localStartingDate = localStartingDate.plus(activity.getDayOfWeek().index, ChronoUnit.DAYS);
        return localStartingDate;
    }

    private void setRecurrence() {
        String recurrence = "RRULE:FREQ=WEEKLY;COUNT=" + SemesterInfo.getNoOfWeeks(semester);
        event.setRecurrence(Collections.singletonList(recurrence));
    }

    private void setLocation() {
        event.setLocation(activity.getRoom());
    }

    private void setColor() {
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

    public void insertInCalendar(Calendar service, String calendarId) throws IOException {
        event = service.events().insert(calendarId, event).execute();
        deleteExtraEvents(service, calendarId);
    }

    private void deleteExtraEvents(Calendar service, String calendarID) throws IOException {
        String pageToken = null;
        do {
            Events events =
                    service.events().instances(calendarID, event.getId()).setPageToken(pageToken).execute();
            List<Event> items = events.getItems();
            deleteExtraEvents(service, calendarID, items);
            pageToken = events.getNextPageToken();
        } while (pageToken != null);
    }

    private void deleteExtraEvents(Calendar service, String calendarID, List<Event> items) throws IOException {
        int holidayLength = SemesterInfo.getHolidayLength(semester);
        int holidayStartWeek = SemesterInfo.getHolidayStartWeek(semester);

        for (int week = 0; week < holidayLength; week++) {
            service.events().delete(calendarID, items.get(holidayStartWeek + week).getId()).execute();
        }

        Activity.Frequency frequency = activity.getFrequency();
        if (frequency == Activity.Frequency.Weekly) {
            return;
        }

        for (int week = frequency.getSkipWeek(); week < holidayStartWeek; week += 2) {
            service.events().delete(calendarID, items.get(week).getId()).execute();
        }

        int nextWeekAfterHoliday = holidayStartWeek + holidayLength + frequency.getSkipWeek();
        for (int week = nextWeekAfterHoliday; week < SemesterInfo.getNoOfWeeks(semester); week += 2) {
            service.events().delete(calendarID, items.get(week).getId()).execute();
        }
    }

    public String getSummary() {
        return event.getSummary();
    }
}
