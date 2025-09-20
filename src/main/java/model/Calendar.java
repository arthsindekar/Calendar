package model;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;

import java.util.Comparator;

import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import java.util.Arrays;

/**
 * Defines the implementation of the calendar interface. This represents a calendar
 * which cannot accept recurring events to be created if they are conflicting with other events.
 */
public class Calendar implements ICalendar {
  private final Set<AbstractEvent> events;

  private String calendarName;

  private TimeZone timeZone;

  /**
   * Constructor initializes the events in a tree set.
   */
  public Calendar() {
    events = new TreeSet<>();
  }

  private Calendar(String calendarName, TimeZone timeZone) throws IllegalArgumentException {
    this.calendarName = calendarName;
    this.timeZone = timeZone;
    events = new TreeSet<>();
  }


  @Override
  public void createEvent(String subject, LocalDateTime startDateTime,
                             LocalDateTime endDateTime, String description,
                             String location, Boolean isPrivate, boolean autoDecline)
          throws IllegalArgumentException {
    AbstractEvent.EventBuilder newEventBuilder = createSingleEvent(subject, startDateTime,
            endDateTime, description, location, isPrivate);

    if (autoDecline && checkConflict(startDateTime,
            LocalDateTime.of(newEventBuilder.getEndDate(), newEventBuilder.getEndTime()))) {
      throw new IllegalArgumentException("Conflicting with existing event");
    }

    events.add(newEventBuilder.build());
  }

  @Override
  public void createRecurringEventWithOccurrences(String subject, LocalDateTime startDateTime,
                          LocalDateTime endDateTime, String description,
                          String location, Boolean isPrivate, String daysOfWeek, int occurrences)
          throws IllegalArgumentException {
    if (daysOfWeek == null || daysOfWeek.isEmpty()) {
      throw new IllegalArgumentException("Minimum 1 day of week should be specified");
    }
    if (occurrences <= 0) {
      throw new IllegalArgumentException("Minimum one occurrence should be specified");
    }
    if (Period.between(startDateTime.toLocalDate(), endDateTime.toLocalDate()).getDays() >= 1 ) {
      throw new IllegalArgumentException("Recurring event instance should start and end on" +
              " the same day");
    }
    Set<DayOfWeek> setOfDays = convertToSetOfDays(daysOfWeek);
    RecurringEvent.RecurringEventBuilder newRecurringEventBuilder = createRecurringEvent(subject,
            startDateTime, endDateTime, description, location, isPrivate, setOfDays)
            .setOccurrences(occurrences);
    RecurringEvent.RecurringEventBuilder builder = createRecurringEventOnSetOfDays(
            newRecurringEventBuilder, startDateTime, endDateTime, setOfDays, occurrences);
    if (checkConflict(builder.build())) {
      throw new IllegalArgumentException("Conflicting with existing event");
    }
    events.add(builder.build());
  }

  @Override
  public void createRecurringEventWithSpecificEndDateTime(String subject,
                                                          LocalDateTime startDateTime,
                                                          LocalDateTime endDateTime,
                                                          String description,
                                                          String location, Boolean isPrivate,
                                                          String daysOfWeek,
                                                          LocalDateTime specificEndDateTime)
          throws IllegalArgumentException {
    if (daysOfWeek == null || daysOfWeek.isEmpty()) {
      throw new IllegalArgumentException("Minimum 1 day of week should be specified");
    }
    if (specificEndDateTime == null) {
      throw new IllegalArgumentException("Specific end date should be specified");
    }
    if (specificEndDateTime.isBefore(endDateTime)) {
      throw new IllegalArgumentException("Specific end date time should be after end date time");
    }
    if (Period.between(startDateTime.toLocalDate(), endDateTime.toLocalDate()).getDays() >= 1 ) {
      throw new IllegalArgumentException("Recurring event instance should start and end on" +
              " the same day");
    }
    Set<DayOfWeek> setOfDays = convertToSetOfDays(daysOfWeek);
    long occurrences = getOccurrencesBetweenDates(startDateTime, specificEndDateTime,
            endDateTime.toLocalTime(), setOfDays);
    RecurringEvent.RecurringEventBuilder newRecurringEventBuilder = createRecurringEvent(subject,
            startDateTime, endDateTime, description, location, isPrivate, setOfDays)
            .setSpecificEndDate(specificEndDateTime.toLocalDate())
            .setSpecificEndTime(specificEndDateTime.toLocalTime());
    RecurringEvent.RecurringEventBuilder builder = createRecurringEventOnSetOfDays(
            newRecurringEventBuilder, startDateTime, endDateTime, setOfDays, occurrences);
    if (checkConflict(builder.build())) {
      throw new IllegalArgumentException("Conflicting with existing event");
    }
    events.add(builder.build());
  }

  private void checkMandatoryFields(String subject, LocalDateTime startDateTime,
                                    LocalDateTime endDateTime) throws IllegalArgumentException {
    if (subject == null || subject.isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    if (startDateTime == null || endDateTime == null) {
      throw new IllegalArgumentException("startDateTime and endDateTime cannot be null");
    }
  }

  private SingleEvent.SingleEventBuilder createSingleEvent(String subject,
                                                           LocalDateTime startDateTime,
                                                           LocalDateTime endDateTime,
                                                           String description, String location,
                                                           Boolean isPrivate)
          throws IllegalArgumentException {
    checkMandatoryFields(subject, startDateTime, endDateTime);
    if (startDateTime.isAfter(endDateTime)) {
      throw new IllegalArgumentException("Start date and time must be before end date and time");
    }
    return new SingleEvent.SingleEventBuilder(subject,
            startDateTime.toLocalDate(), startDateTime.toLocalTime(),
            endDateTime.toLocalDate(), endDateTime.toLocalTime())
            .setDescription(description)
            .setLocation(location)
            .setPrivate(isPrivate);
  }

  private RecurringEvent.RecurringEventBuilder createRecurringEvent(String subject,
                                                                    LocalDateTime startDateTime,
                                                                    LocalDateTime endDateTime,
                                                                    String description,
                                                                    String location,
                                                                    Boolean isPrivate,
                                                                    Set<DayOfWeek> setOfDays)
          throws IllegalArgumentException {
    checkMandatoryFields(subject, startDateTime, endDateTime);
    if (startDateTime.isAfter(endDateTime)) {
      throw new IllegalArgumentException("Start date and time must be before end date and time");
    }
    return new RecurringEvent.RecurringEventBuilder(subject,
            startDateTime.toLocalDate(), startDateTime.toLocalTime(),
            endDateTime.toLocalDate(), endDateTime.toLocalTime())
            .setDescription(description)
            .setLocation(location)
            .setPrivate(isPrivate)
            .setDaysOfWeek(setOfDays);
  }

  static RecurringEvent.RecurringEventBuilder createRecurringEventOnSetOfDays(
          RecurringEvent.RecurringEventBuilder eventBuilder,
          LocalDateTime startDateTime,
          LocalDateTime endDateTime,
          Set<DayOfWeek> setOfDays, long occurrences)
          throws IllegalArgumentException {
    List<DayOfWeek> dayList = new ArrayList<>(setOfDays);
    DayOfWeek startDay = getStartDay(startDateTime.toLocalDate(), setOfDays);
    Set<LocalDateTime> recurringStartDatetime = new TreeSet<>();
    Set<LocalDateTime> recurringEndDatetime = new TreeSet<>();
    int startIndex = dayList.indexOf(startDay);
    int i = 0;
    while (occurrences > 0) {
      int currentIndex = (startIndex + i) % dayList.size();
      LocalDate currentDate = startDateTime.toLocalDate()
              .with(TemporalAdjusters.nextOrSame(dayList.get(currentIndex)));
      if (recurringStartDatetime.contains(LocalDateTime.of(currentDate,
              startDateTime.toLocalTime()))) {
        currentDate = startDateTime.toLocalDate()
                .with(TemporalAdjusters.next(dayList.get(currentIndex)));
      }
      LocalDateTime currentStartDateTime = LocalDateTime.of(currentDate,
              startDateTime.toLocalTime());
      startDateTime = currentStartDateTime;
      LocalDateTime currentEndDateTime = LocalDateTime.of(currentDate, endDateTime.toLocalTime());

      recurringStartDatetime.add(currentStartDateTime);
      recurringEndDatetime.add(currentEndDateTime);
      occurrences--;
      i++;
    }
    return eventBuilder.setRecurringStartDateTime(recurringStartDatetime)
            .setRecurringEndDateTime(recurringEndDatetime);
  }

  private static DayOfWeek getDayOfWeek(char dayOfWeek) throws IllegalArgumentException {
    switch (dayOfWeek) {
      case 'M':
        return DayOfWeek.MONDAY;
      case 'T':
        return DayOfWeek.TUESDAY;
      case 'W':
        return DayOfWeek.WEDNESDAY;
      case 'R':
        return DayOfWeek.THURSDAY;
      case 'F':
        return DayOfWeek.FRIDAY;
      case 'S':
        return DayOfWeek.SATURDAY;
      case 'U':
        return DayOfWeek.SUNDAY;
      default:
        throw new IllegalArgumentException("Invalid day of week: " + dayOfWeek);
    }
  }

  static Set<DayOfWeek> convertToSetOfDays(String daysOfWeek) {
    Set<DayOfWeek> setOfDays = new TreeSet<>();
    for (char day : daysOfWeek.toCharArray()) {
      DayOfWeek dayOfWeek = getDayOfWeek(day);
      setOfDays.add(dayOfWeek);
    }
    return setOfDays;
  }

  private static DayOfWeek getStartDay(LocalDate startDate, Set<DayOfWeek> setOfDays)
          throws IllegalArgumentException {
    DayOfWeek startDateDay = startDate.getDayOfWeek();
    return setOfDays.stream()
            .min(Comparator.comparingInt(d -> daysDiff(startDateDay, d)))
            .get();
  }

  private static int daysDiff(DayOfWeek from, DayOfWeek to) {
    return (to.getValue() - from.getValue() + 7) % 7;
  }

  private boolean checkConflict(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    for (AbstractEvent event : events) {
      if (event.checkConflict(startDateTime, endDateTime)) {
        return true;
      }
    }
    return false;
  }

  private boolean checkConflict(AbstractEvent event) {
    List<AbstractEvent> allEvents = event.getAllEvents();
    for (AbstractEvent singleEvent : allEvents) {
      LocalDateTime startDateTime = LocalDateTime.of(singleEvent.getStartDate(),
              singleEvent.getStartTime());
      LocalDateTime endDateTime = LocalDateTime.of(singleEvent.getEndDate(),
              singleEvent.getEndTime());
      if (checkConflict(startDateTime, endDateTime)) {
        return true;
      }
    }
    return false;
  }

  static long getOccurrencesBetweenDates(LocalDateTime startDateTime,
                                        LocalDateTime specificEndDateTime, LocalTime endTime,
                                        Set<DayOfWeek> setOfDays) {
    LocalDate startDate = startDateTime.toLocalDate();
    LocalDate specificEndDate = specificEndDateTime.toLocalDate();
    if (specificEndDateTime.toLocalTime().isBefore(endTime)) {
      specificEndDate = specificEndDate.minusDays(1);
    }
    return startDate.datesUntil(specificEndDate.plusDays(1))
            .filter(date -> setOfDays.contains(date.getDayOfWeek()))
            .count();
  }

  @Override
  public void editEvents(String subject, LocalDateTime startDateTime,
                        String property, String newValue) throws IllegalArgumentException {
    if (subject == null || subject.isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    Set<AbstractEvent> sameSubjectEvents = searchEventsBySubject(subject);
    if (sameSubjectEvents.isEmpty()) {
      throw new IllegalArgumentException("No events found for subject: " + subject);
    }
    if (startDateTime == null) {
      for (AbstractEvent event : sameSubjectEvents) {
        AbstractEvent.EventBuilder eventBuilder = event.getEventCopy();
        changeEventProperty(property, newValue, event,null, eventBuilder);
      }
    }
    else {
      for (AbstractEvent event : sameSubjectEvents) {
        AbstractEvent.EventBuilder eventBuilder1 = event.getEventCopy(startDateTime, false);
        AbstractEvent.EventBuilder eventBuilder2 = event.getEventCopy(startDateTime, true);
        changeEventProperty(property, newValue, event,eventBuilder1, eventBuilder2);
      }
    }
  }

  private void changeEventProperty(String property, String newValue, AbstractEvent event,
                                   AbstractEvent.EventBuilder eventBuilder1,
                                   AbstractEvent.EventBuilder eventBuilder2)
          throws IllegalArgumentException {
    if (eventBuilder2 == null) {
      return;
    }
    AbstractEvent newEvent = eventBuilder2.setEventProperty(property, newValue).build();
    removeEvent(event);
    if (eventBuilder1 != null) {
      events.add(eventBuilder1.build());
    }
    if (checkConflict(newEvent)) {
      if (eventBuilder1 != null) {
        removeEvent(eventBuilder1.build());
        events.add(event);
      }
      throw new IllegalArgumentException("Conflicting with existing event");
    }
    events.add(newEvent);
  }

  private void removeEvent(AbstractEvent event) {
    events.remove(event);
  }

  private TreeSet<AbstractEvent> searchEventsBySubject(String subject) {
    TreeSet<AbstractEvent> recurringEvents = new TreeSet<>();
    for (AbstractEvent event : events) {
      if (event.subjectMatches(subject)) {
        recurringEvents.add(event);
      }
    }
    return recurringEvents;
  }

  @Override
  public void editEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                         String property, String newValue) throws IllegalArgumentException {
    if (subject == null || subject.isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty");
    }
    if (startDateTime == null) {
      throw new IllegalArgumentException("Start datetime cannot be null");
    }
    if (endDateTime == null) {
      throw new IllegalArgumentException("End datetime cannot be null");
    }
    if (startDateTime.isAfter(endDateTime)) {
      throw new IllegalArgumentException("Start datetime cannot be after end date time");
    }
    AbstractEvent.EventBuilder eventBuilder = searchEventBySubjectAndStartAndEndDateTime(subject,
            startDateTime, endDateTime);
    if (eventBuilder != null) {
      AbstractEvent newSingleEvent = eventBuilder.setEventProperty( property, newValue).build();
      removeEventBySubjectAndStartAndEndDateTime(subject, startDateTime, endDateTime);
      events.add(newSingleEvent);
    }
  }

  private AbstractEvent.EventBuilder searchEventBySubjectAndStartAndEndDateTime(String subject,
                                                                    LocalDateTime startDateTime,
                                                                    LocalDateTime endDateTime) {
    for (AbstractEvent event : events) {
      if (event.subjectMatches(subject) && event.startDateTimeMatches(startDateTime)
              && event.endDateTimeMatches(endDateTime)) {
        return event.getOneEvent(startDateTime, endDateTime);
      }
    }
    return null;
  }

  private void removeEventBySubjectAndStartAndEndDateTime(String subject,
                                                          LocalDateTime startDateTime,
                                                          LocalDateTime endDateTime) {
    Set<AbstractEvent> removalEvents = new TreeSet<>();
    Set<AbstractEvent> addEvents = new TreeSet<>();
    for (AbstractEvent event : events) {
      if (event.subjectMatches(subject) && event.startDateTimeMatches(startDateTime)
              && event.endDateTimeMatches(endDateTime)) {
        if (event instanceof RecurringEvent) {
          RecurringEvent recurringEvent = (RecurringEvent) event;
          TreeSet<LocalDateTime> startDateTimeSubset =  new TreeSet<>(recurringEvent
                  .getSubsetOfStartDateTime(date -> !date.isEqual(startDateTime)));
          TreeSet<LocalDateTime> endDateTimeSubset =  new TreeSet<>(recurringEvent
                  .getSubsetOfEndDateTime(date -> !date.isEqual(endDateTime)));
          AbstractEvent newEvent = recurringEvent.getEventCopy()
                  .setRecurringStartDateTime(startDateTimeSubset)
                  .setRecurringEndDateTime(endDateTimeSubset)
                  .setStartDate(startDateTimeSubset.first().toLocalDate())
                  .setEndDate(endDateTimeSubset.first().toLocalDate())
                  .build();
          removalEvents.add(recurringEvent);
          addEvents.add(newEvent);
        }
        else if (event instanceof SingleEvent) {
          removalEvents.add(event);
        }

      }
    }
    events.removeAll(removalEvents);
    events.addAll(addEvents);
  }

 

  /**
   * Defines a bulleted list of all events in the given interval including their start
   * and end times and location (if any).
   *
   * @param startDateTime defines the 'from' date time.
   * @param endDateTime defines the 'from' date time.
   * @return list of events that fall within the specified interval
   *         or no events found if there are no events in this calendar.
   */
  @Override
  public String printEvents(LocalDateTime startDateTime, LocalDateTime endDateTime)
          throws IllegalArgumentException {
    if (startDateTime == null || endDateTime == null) {
      throw new IllegalArgumentException("Both start and end date time must be provided");
    }
    LocalDate startDate = startDateTime.toLocalDate();
    LocalDate endDate = endDateTime.toLocalDate();
    LocalTime startTime = startDateTime.toLocalTime();
    LocalTime endTime = endDateTime.toLocalTime();
    if (endDate.isBefore(startDate)) {
      throw new IllegalArgumentException("End date cannot be before start date");
    }
    if (events.isEmpty()) {
      return "";
    }
    List<AbstractEvent> listOfEvents = new ArrayList<>();
    for (AbstractEvent event : events) {
      listOfEvents.addAll(event.checkForPrintWithinRange(startDate,startTime,endDate,endTime));
    }
    return printHelper(listOfEvents);
  }

  /**
   * Private helper method for printEvents to convert list of events to a desired string.
   *
   * @param listOfEvents defines the events that need to be converted into a string.
   * @return a bulleted list of events in string format.
   */
  private String printHelper(List<AbstractEvent> listOfEvents) {
    StringBuilder listOfEventsBuilder = new StringBuilder();
    for (AbstractEvent event : listOfEvents) {
      listOfEventsBuilder.append(event.printEachEvent());
    }
    return listOfEventsBuilder.toString();
  }


  @Override
  public List<List<String>> exportEvents() throws IllegalArgumentException {
    if (events.isEmpty()) {
      throw new IllegalArgumentException("No events found");
    }
    List<List<String>> data = new ArrayList<>();
    List<String> columnNames = Arrays.asList("Subject", "Start Date", "Start Time",
            "End Date", "End Time", "Description",
            "Location", "Private","All Day Event");
    data.add(columnNames);
    //put all single and recurring events in all events as list of single events.
    List<AbstractEvent> allEvents = new ArrayList<>();
    for (AbstractEvent event : events) {
      allEvents.addAll(event.getAllEvents());
    }

    //Event extraction
    for (AbstractEvent event : allEvents) {
      List<String> row = event.getStrings();
      data.add(row);
    }
    return data;
  }


  /**
   * Defines the status of the user as busy or available.
   * @param localDateTime defines the date and time at which we want to know the status of the user.
   * @return Busy if user is busy and Available otherwise.
   */
  @Override
  public String showStatus(LocalDateTime localDateTime) throws IllegalArgumentException {
    if (localDateTime == null) {
      throw new IllegalArgumentException("Date and time cannot be null");
    }
    LocalDate date = localDateTime.toLocalDate();
    LocalTime time = localDateTime.toLocalTime();

    for (AbstractEvent event : events) {
      if (event.showStatusHelper(date, time)) { // if returns true that means busy
        return "Busy"; //user busy
      }
    }
    return "Available"; //user not busy
  }

  @Override
  public TimeZone getTimeZone() {
    return this.timeZone;
  }

  @Override
  public void setTimeZone(TimeZone newTimeZone) {
    Set<AbstractEvent> setOfNewEvents = new TreeSet<>();
    for (AbstractEvent event : events) {
      AbstractEvent newEvent = event.convertTimeZone(timeZone,newTimeZone);
      setOfNewEvents.add(newEvent);
    }
    events.clear();
    events.addAll(setOfNewEvents);
    this.timeZone = newTimeZone;
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
    return new Calendar(this.calendarName, this.timeZone);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ICalendar)) {
      return false;
    }
    ICalendar otherCalendar = (ICalendar) o;
    return this.calendarName.equals(otherCalendar.getName());
  }

  @Override
  public int hashCode() {
    return calendarName.hashCode();
  }

}
