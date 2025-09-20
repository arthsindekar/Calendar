package model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

/**
 * This class defines the event which can either be a single event or a recurring event.
 * This class implements comparable because it implements compare to method which defines the
 * order in which events are stored.
 */
public abstract class AbstractEvent implements Comparable<AbstractEvent> {

  /**
   * Constructor initializes the common fields of single and recurring event.
   * @param subject describes the name of the event which cannot be null.
   *                Name of the event can be same for different events.
   * @param startDate describes the start date  of a particular event.
   *                      This field cannot be null since it describes when the event starts.
   * @param startTime describes start Time of a particular event. This field cannot be null.
   *
   * @param endDate describes the end date  of an event. This field cannot be null
   *                    since it defines when an event ends.
   * @param endTime describes the end time of an event. This field cannot be null
   * @param description provides the detailed description of an event. This field is can be null
   *                    which means user may choose to provide the description for an event.
   * @param location describes address of the event. This field is can be null
   *                 which means user may choose to provide the location for an event.
   * @param isPrivate  defines if the event is going to be public or private.
   *                   If true is passed then the event is categorized as private otherwise public.
   *                   This field can be null.
   */
  AbstractEvent(String subject, LocalDate startDate, LocalTime startTime, LocalDate endDate,
                LocalTime endTime,String description,
                String location,Boolean isPrivate) {
    this.subject = subject;
    this.startDate = startDate;
    this.startTime = startTime;
    this.endDate = endDate;
    this.endTime = endTime;
    this.description = description;
    this.location = location;
    this.isPrivate = isPrivate;
  }

  private final String subject;
  private final LocalDate startDate;
  private final LocalTime startTime;
  private final LocalDate endDate;
  private final LocalTime endTime;
  private final String description;
  private final String location;
  private final Boolean isPrivate;

  /**
   * Event Builder is an abstract event builder which helps to create ann  abstract event.
   * @param <T> Type of Event Builder class can be concrete  classes which extend event builder
   */
  public static abstract class EventBuilder<T extends EventBuilder<T>> {
    private String subject;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private String description;

    private String location;

    private Boolean isPrivate;

    EventBuilder(String subject, LocalDate startDate, LocalTime startTime, LocalDate endDate,
                 LocalTime endTime) {
      this.subject = subject;
      this.startDate = startDate;
      this.startTime = startTime;
      this.endDate = endDate;
      this.endTime = endTime;
    }

    String getSubject() {
      return this.subject;
    }

    LocalDate getStartDate() {
      return this.startDate;
    }

    LocalTime getStartTime() {
      return this.startTime;
    }

    LocalDate getEndDate() {
      return this.endDate;
    }

    LocalTime getEndTime() {
      return this.endTime;
    }

    String getDescription() {
      return this.description;
    }

    String getLocation() {
      return this.location;
    }

    Boolean getIsPrivate() {
      return this.isPrivate;
    }

    T setStartDate(LocalDate startDate) {
      this.startDate = startDate;
      return self();
    }

    T setEndDate(LocalDate endDate) {
      this.endDate = endDate;
      return self();
    }

    T setStartTime(LocalTime startTime) {
      this.startTime = startTime;
      return self();
    }

    T setEndTime(LocalTime endTime) {
      this.endTime = endTime;
      return self();
    }

    T setDescription(String description) {
      this.description = description;
      return self();
    }

    T setLocation(String location) {
      this.location = location;
      return self();
    }

    T setPrivate(Boolean isPrivate) {
      this.isPrivate = isPrivate;
      return self();
    }

    T setEventProperty(String property, String value) {
      switch (property) {
        case "description":
          this.description = value;
          break;
        case "location":
          this.location = value;
          break;
        case "isPrivate":
          if (value == null) {
            this.isPrivate = null;
          }
          else {
            this.isPrivate = Boolean.parseBoolean(value);
          }
          break;
        case "subject":
          if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Subject cannot be null or empty");
          }
          this.subject = value;
          break;
        case "startDate":
          LocalDate newStartDate = LocalDate.parse(value);
          Period period = Period.between(startDate, endDate);
          startDate = newStartDate;
          endDate = newStartDate.plus(period);
          break;
        case "endDate":
          if (LocalDate.parse(value).isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
          }
          endDate = LocalDate.parse(value);
          break;
        case "startTime":
          LocalTime newStartTime = LocalTime.parse(value);
          Duration duration = Duration.between(startTime, endTime);
          startTime = newStartTime;
          if (duration.compareTo(Duration.between(startTime, LocalTime.MAX)) >= 0) {
            endTime = LocalTime.of(23, 59, 59);
          }
          else {
            endTime = newStartTime.plus(duration);
          }
          break;
        case "endTime":
          if (LocalDateTime.of(startDate, startTime)
                  .isAfter(LocalDateTime.of(endDate, LocalTime.parse(value)))) {
            throw new IllegalArgumentException("End datetime cannot be before start datetime");
          }
          endTime = LocalTime.parse(value);
          break;
        default:
          throw new IllegalArgumentException("Invalid property: " + property);
      }
      return self();
    }

    abstract T self();

    abstract AbstractEvent build();
  }



  List<AbstractEvent> checkForPrintWithinRange(LocalDate startDate,LocalTime startTime,
                                             LocalDate endDate, LocalTime endTime) {
    List<AbstractEvent> events = new ArrayList<>();
    if (this.startDate.isBefore(startDate) || this.endDate.isAfter(endDate)) {
      return Collections.emptyList();
    }

    // If the event starts on the first day, ensure its start time is
    // not before the specified start time.
    if (this.startDate.equals(startDate) && startTime != null) {
      if (this.startTime.isBefore(startTime)) {
        return Collections.emptyList();
      }
    }

    // If the event ends on the last day, ensure its end time is not after the specified end time.
    if (this.endDate.equals(endDate) && endTime != null) {
      if (this.endTime.isAfter(endTime)) {
        return Collections.emptyList();
      }
    }
    events.add(this);
    return events;

  }

  List<String> getStrings() {
    List<String> row = new ArrayList<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    String startTime = formatter.format(this.startTime);
    String endTime = null;
    if (this.endTime != null) {
      endTime = formatter.format(this.endTime);
    }

    row.add(this.subject);
    row.add(this.startDate.toString());
    row.add(startTime);


    if (this.endDate != null) {
      row.add(this.endDate.toString());
    }
    else {
      row.add("");
    }
    if (this.endTime != null) {
      row.add(endTime);
    }
    else {
      row.add("");
    }
    if (this.description != null) {
      row.add(this.description);
    }
    else {
      row.add("");
    }
    if (this.location != null) {
      row.add(this.location);
    }
    else {
      row.add("");
    }
    if (this.isPrivate != null) {
      if (this.isPrivate) {
        row.add("True");
      }
      else {
        row.add("False");
      }
    }
    else {
      row.add("");
    }

    if (this.endDate != null && this.startDate.isEqual(this.endDate)
        && this.startTime == LocalTime.MIN && this.endTime == LocalTime.MAX)  { //All day Event
      row.add("True");
    }
    else { //Not an all day event
      row.add("False");
    }
    return row;
  }

  boolean showStatusHelper(LocalDate date, LocalTime time) {
    if (date.isEqual(this.startDate) || date.isEqual(this.endDate)) {
      if ((time.isAfter(this.startTime) && time.isBefore(this.endTime))
          || time.equals(this.startTime)) {
        return true; //user busy
      }
    }
    return date.isAfter(this.startDate) && date.isBefore(this.endDate);
  }

  StringBuilder printEachEvent() {
    List<String> eventFields = getStrings();
    eventFields.replaceAll(cell -> "\"" + cell + "\"");
    StringBuilder eventString = new StringBuilder();
    eventString.append(String.join(",", eventFields));
    eventString.append("\n");

    return eventString;
  }

  boolean checkConflict(LocalDateTime otherStartDateTime, LocalDateTime otherEndDateTime) {
    LocalDateTime thisStartDateTime = LocalDateTime.of(this.startDate, this.startTime);
    LocalDateTime thisEndDateTime = LocalDateTime.of(this.endDate, this.endTime);
    if (otherStartDateTime.isBefore(thisStartDateTime)
            && otherEndDateTime.isAfter(thisStartDateTime)) {
      return true;
    }
    if (otherStartDateTime.isAfter(thisStartDateTime)
            && otherStartDateTime.isBefore(thisEndDateTime)) {
      return true;
    }
    if (otherStartDateTime.isEqual(thisStartDateTime)) {
      return true;
    }
    return otherEndDateTime.isEqual(thisEndDateTime);
  }

  @Override
  public int compareTo(AbstractEvent o) {
    LocalDateTime startDateTime = LocalDateTime.of(this.startDate, this.startTime);
    LocalDateTime endDateTime = LocalDateTime.of(this.endDate, this.endTime);
    LocalDateTime otherStartDateTime = LocalDateTime.of(o.startDate, o.startTime);
    LocalDateTime otherEndDateTime = LocalDateTime.of(o.endDate, o.endTime);
    if (startDateTime.isAfter(otherStartDateTime)) {
      return 1;
    }
    if (startDateTime.isBefore(otherStartDateTime)) {
      return -1;
    }
    if (startDateTime.equals(otherStartDateTime)) {
      if (endDateTime.isAfter(otherEndDateTime)) {
        return 1;
      }
      if (endDateTime.isBefore(otherEndDateTime)) {
        return -1;
      }
      if (endDateTime.equals(otherEndDateTime)) {
        if (this.subject.compareToIgnoreCase(o.subject) > 0) {
          return 1;
        }
        if (this.subject.compareToIgnoreCase(o.subject) < 0) {
          return -1;
        }
      }
    }
    return 0;
  }

  boolean subjectMatches(String subject) {
    return this.subject.equals(subject);
  }

  boolean startDateTimeMatches(LocalDateTime startDateTime) {
    return LocalDateTime.of(this.startDate, this.startTime).isEqual(startDateTime);
  }

  boolean endDateTimeMatches(LocalDateTime endDateTime) {
    return LocalDateTime.of(this.endDate, this.endTime).isEqual(endDateTime);
  }

  abstract EventBuilder getOneEvent(LocalDateTime startDateTime,
                                                  LocalDateTime endDateTime);

  abstract EventBuilder getEventCopy();

  abstract EventBuilder getEventCopy(LocalDateTime dateTime, boolean after);

  List<AbstractEvent> getAllEvents() {
    List<AbstractEvent> events = new ArrayList<>();
    events.add(this);
    return events;
  }

  
  AbstractEvent convertTimeZone(TimeZone timeZone,TimeZone newTimeZone) {
    //get the zone of old time zone.
    final EventBuilder eventBuilder = getEventBuilderWithNewTimeZone(timeZone, newTimeZone);
    return eventBuilder.build();

  }

  LocalDateTime convertTimeZoneHelper(TimeZone timeZone,TimeZone newTimeZone,
                             LocalDateTime dateTime) {
    ZoneId originalZone = ZoneId.of(timeZone.getID());
    ZonedDateTime zonedOrgDateTime = dateTime.atZone(originalZone);

    ZoneId newZone = ZoneId.of(newTimeZone.getID());
    ZonedDateTime newZonedDateTime = zonedOrgDateTime.withZoneSameInstant(newZone);
    return newZonedDateTime.toLocalDateTime();

  }

  EventBuilder getEventBuilderWithNewTimeZone(TimeZone timeZone, TimeZone newTimeZone) {

    LocalDateTime originalStartDateTime = LocalDateTime.of(this.startDate, startTime);
    LocalDateTime originalEndDateTime = LocalDateTime.of(this.endDate, endTime);


    //convert old zone date and time to new zone date and time.
    LocalDateTime newStartDateTime = convertTimeZoneHelper(timeZone,
            newTimeZone, originalStartDateTime);
    LocalDateTime newEndDateTime = convertTimeZoneHelper(timeZone,
            newTimeZone, originalEndDateTime);

    //create an event which has new zones
    EventBuilder eventBuilder = getEventCopy();
    eventBuilder = eventBuilder.setStartDate(newStartDateTime.toLocalDate())
            .setEndDate(newEndDateTime.toLocalDate())
            .setStartTime(newStartDateTime.toLocalTime())
            .setEndTime(newEndDateTime.toLocalTime());
    return eventBuilder;
  }



  LocalDate getStartDate() {
    return startDate;
  }

  LocalTime getStartTime() {
    return startTime;
  }

  LocalDate getEndDate() {
    return endDate;
  }

  LocalTime getEndTime() {
    return endTime;
  }

  String getSubject() {
    return subject;
  }

  String getDescription() {
    return description;
  }

  String getLocation() {
    return location;
  }

  Boolean getIsPrivate() {
    return isPrivate;
  }

}

