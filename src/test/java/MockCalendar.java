import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import model.ICalendar;

/**
 * A mock implementation of the {@code ICalendar} interface used for testing purposes.
 * This class provides a simplified or controlled version of the calendar functionality,
 * allowing tests to simulate and verify interactions with a calendar without relying
 * on a fully implemented calendar system.
 * */
public class MockCalendar implements ICalendar {
  private final StringBuilder log;
  private String uniqueCode;
  private boolean export;

  private String calendarName;
  private TimeZone timeZone;

  MockCalendar(StringBuilder log) {
    this.log = log;
  }

  MockCalendar(String calendarName, TimeZone timeZone,StringBuilder log, String uniqueCode) {
    this.calendarName = calendarName;
    this.timeZone = timeZone;
    this.log = log;
    this.uniqueCode = uniqueCode;
  }

  private MockCalendar(StringBuilder log, String uniqueCode, boolean export,
                       String calendarName, TimeZone timeZone) {
    this.log = log;
    this.uniqueCode = uniqueCode;
    this.export = export;
    this.calendarName = calendarName;
    this.timeZone = timeZone;
  }


  MockCalendar(StringBuilder log,String uniqueCode) {
    this.uniqueCode = uniqueCode;
    this.log = log;
  }

  MockCalendar(StringBuilder log,String uniqueCode, boolean export) {
    this.uniqueCode = uniqueCode;
    this.log = log;
    this.export = export;
  }

  @Override
  public void createEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                          String description, String location,
                          Boolean isPrivate, boolean autoDecline)
          throws IllegalArgumentException {
    log.append("Subject: " + subject + " startDateTime: " + startDateTime + " endDateTime: "
            + endDateTime + " description: " + description + " location: " + location  +
            " isPrivate: " + isPrivate + " autoDecline: " + autoDecline);
    if (subject == null) {
      throw new IllegalArgumentException("Subject cannot be null");
    }
  }

  @Override
  public void createRecurringEventWithOccurrences(String subject, LocalDateTime startDateTime,
                                                  LocalDateTime endDateTime, String description,
                                                  String location, Boolean isPrivate,
                                                  String daysOfWeek, int occurrences)
          throws IllegalArgumentException {
    log.append("Subject: " + subject + " startDateTime: " + startDateTime + " endDateTime: "
            + endDateTime + " description: " + description + " location: " + location  +
            " isPrivate: " + isPrivate + " daysOfWeek: " + daysOfWeek +
            " occurrences: " + occurrences);
    if (subject == null) {
      throw new IllegalArgumentException("Subject cannot be null");
    }
  }

  @Override
  public void createRecurringEventWithSpecificEndDateTime(String subject,
                                                          LocalDateTime startDateTime,
                                                          LocalDateTime endDateTime,
                                                          String description, String location,
                                                          Boolean isPrivate, String daysOfWeek,
                                                          LocalDateTime specificEndDateTime)
          throws IllegalArgumentException {
    log.append("Subject: " + subject + " startDateTime: " + startDateTime + " endDateTime: "
            + endDateTime + " description: " + description + " location: " + location  +
            " isPrivate: " + isPrivate + " daysOfWeek: " + daysOfWeek +
            " specificEndDateTime: " + specificEndDateTime);
    if (subject == null) {
      throw new IllegalArgumentException("Subject cannot be null");
    }
  }

  @Override
  public void editEvents(String subject, LocalDateTime startDateTime, String property,
                         String newValue) throws IllegalArgumentException {
    log.append("Subject: " + subject + " startDateTime: " + startDateTime + " property: "
            + property + " newValue: " + newValue);
    if (subject == null) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
  }

  @Override
  public void editEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                        String property, String newValue) throws IllegalArgumentException {
    log.append("Subject: " + subject + " startDateTime: " + startDateTime + " endDateTime: " +
            endDateTime + " property: " + property + " newValue: " + newValue);
    if (startDateTime == null) {
      throw new IllegalArgumentException("StartDateTime has invalid or null value");
    }
  }

  @Override
  public String printEvents(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    log.append("startDateTime: " + startDateTime + " endDateTime: " + endDateTime);
    if (startDateTime == null && endDateTime == null) {
      throw new IllegalArgumentException("startDateTime and endDateTime invalid or null");
    }
    if (startDateTime.isAfter(endDateTime)) {
      throw new IllegalArgumentException("startDateTime must be after endDateTime");
    }
    return uniqueCode;
  }

  @Override
  public List<List<String>> exportEvents() {
    String eventInfo = "eventInfo";
    List<String> data = new ArrayList<>();
    List<List<String>> eventList = new ArrayList<>();
    data.add(eventInfo);
    eventList.add(data);
    for (List<String> event: eventList) {
      for (String datum : data) {
        log.append(datum);
      }
    }
    return eventList;
  }


  @Override
  public String showStatus(LocalDateTime localDateTime) {
    if (localDateTime == null) {
      throw new IllegalArgumentException("localDateTime cannot be null");
    }
    log.append(localDateTime);
    return "Success";
  }

  @Override
  public TimeZone getTimeZone() {
    return this.timeZone;
  }

  @Override
  public void setTimeZone(TimeZone timeZone) {
    this.timeZone = timeZone;
  }

  @Override
  public String getName() {
    return this.calendarName;
  }

  @Override
  public void setName(String name) {
    this.calendarName = name;
  }

  @Override
  public ICalendar getCalendarCopy() {
    return new MockCalendar(this.log,this.uniqueCode,this.export,this.calendarName,this.timeZone);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ICalendar)) {
      return false;
    }
    MockCalendar otherCalendar = (MockCalendar) o;
    return this.calendarName.equals(otherCalendar.calendarName);
  }

  @Override
  public int hashCode() {
    return calendarName.hashCode();
  }

}
