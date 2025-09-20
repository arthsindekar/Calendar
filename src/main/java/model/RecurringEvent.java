package model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;

import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *  Represents a recurring event that extends the functionality of {@code AbstractEvent}.
 *  This class is designed to handle events that occur repeatedly over a specified period.
 */
public class RecurringEvent extends AbstractEvent {

  private final Set<DayOfWeek> daysOfWeek;

  private final long occurrences;

  private final LocalDate specificEndDate;

  private final LocalTime specificEndTime;

  private final Set<LocalDateTime> recurringStartDateTime;

  private final Set<LocalDateTime> recurringEndDateTime;


  private RecurringEvent(RecurringEventBuilder builder) {
    super(builder.getSubject(), builder.getStartDate(), builder.getStartTime(),
            builder.getEndDate(), builder.getEndTime(),
            builder.getDescription(), builder.getLocation(), builder.getIsPrivate());
    daysOfWeek = builder.daysOfWeek;
    occurrences = builder.occurrences;
    specificEndDate = builder.specificEndDate;
    specificEndTime = builder.specificEndTime;
    recurringStartDateTime = builder.recurringStartDateTime;
    recurringEndDateTime = builder.recurringEndDateTime;
  }

  /**
   * A builder class for constructing instances of {@code RecurringEvent}.
   * This class extends {@code EventBuilder} to provide a fluent API for setting up
   * recurring event details, such as frequency and recurrence rules.
   */
  public static class RecurringEventBuilder extends EventBuilder<RecurringEventBuilder> {

    Set<DayOfWeek> daysOfWeek;

    long occurrences;

    LocalDate specificEndDate;

    LocalTime specificEndTime;

    Set<LocalDateTime> recurringStartDateTime;

    Set<LocalDateTime> recurringEndDateTime;

    /**
     * Constructs a {@code RecurringEventBuilder} with the specified subject,
     * start date, and start time.
     * This initializes the builder with essential details required to create a recurring event.
     *
     * @param subject   the name of the event
     * @param startDate the date when the recurring event starts
     * @param startTime the time when the recurring event starts
     */
    public RecurringEventBuilder(String subject, LocalDate startDate, LocalTime startTime,
                                 LocalDate endDate, LocalTime endTime) {
      super(subject, startDate, startTime, endDate, endTime);
    }

    @Override
    RecurringEventBuilder self() {
      return this;
    }

    RecurringEventBuilder setDaysOfWeek(Set<DayOfWeek> daysOfWeek) {
      this.daysOfWeek = daysOfWeek;
      return this;
    }

    RecurringEventBuilder setOccurrences(long occurrences) {
      this.occurrences = occurrences;
      return this;
    }

    RecurringEventBuilder setSpecificEndDate(LocalDate specificEndDate) {
      this.specificEndDate = specificEndDate;
      return this;
    }

    RecurringEventBuilder setSpecificEndTime(LocalTime specificEndTime) {
      this.specificEndTime = specificEndTime;
      return this;
    }

    RecurringEventBuilder setRecurringStartDateTime(Set<LocalDateTime>
                                                                           recurringStartDateTime) {
      this.recurringStartDateTime = recurringStartDateTime;
      return this;
    }

    RecurringEventBuilder setRecurringEndDateTime(Set<LocalDateTime>
                                                                         recurringEndDateTime) {
      this.recurringEndDateTime = recurringEndDateTime;
      return this;
    }

    @Override
    RecurringEventBuilder setEventProperty(String property, String value) {
      try {
        super.setEventProperty(property, value);
      } catch (IllegalArgumentException e) {
        if (!e.getMessage().contains("Invalid property:")) {
          throw e;
        }
      }
      long newOccurrences = occurrences;
      if (specificEndDate != null) {
        newOccurrences = Calendar.getOccurrencesBetweenDates(LocalDateTime.of(getStartDate(),
                getStartTime()), LocalDateTime.of(specificEndDate,
                specificEndTime), getEndTime(), daysOfWeek);
      }
      switch (property) {
        case "description":
        case "location":
        case "isPrivate":
        case "subject":
          break;
        case "startDate":
        case "endTime":
        case "startTime":
          Calendar.createRecurringEventOnSetOfDays(self(), LocalDateTime.of(getStartDate(),
                          getStartTime()), LocalDateTime.of(getEndDate(), getEndTime()),
                  daysOfWeek, newOccurrences);
          break;
        case "endDate":
          if (Period.between(getStartDate(), getEndDate()).getDays() >= 1 ) {
            throw new IllegalArgumentException("Recurring event instance should start and end on" +
                    " the same day");
          }
          break;
        case "daysOfWeek":
          Set<DayOfWeek> setOfDays = Calendar.convertToSetOfDays(value);
          daysOfWeek = setOfDays;
          Calendar.createRecurringEventOnSetOfDays(self(), LocalDateTime.of(getStartDate(),
                  getStartTime()), LocalDateTime.of(getEndDate(),
                  getEndTime()), setOfDays, newOccurrences);
          break;
        case "occurrences":
          newOccurrences = Long.parseLong(value);
          occurrences = newOccurrences;
          specificEndDate = null;
          specificEndTime = null;
          Calendar.createRecurringEventOnSetOfDays(self(), LocalDateTime.of(getStartDate(),
                  getStartTime()), LocalDateTime.of(getEndDate(),
                  getEndTime()), daysOfWeek, newOccurrences);
          break;
        case "specificEndDate":
          if (specificEndTime == null) {
            specificEndTime = LocalTime.MAX;
          }
          newOccurrences = Calendar.getOccurrencesBetweenDates(LocalDateTime.of(getStartDate(),
                  getStartTime()), LocalDateTime.of(LocalDate.parse(value),
                  specificEndTime), getEndTime(), daysOfWeek);
          specificEndDate = LocalDate.parse(value);
          occurrences = 0;
          Calendar.createRecurringEventOnSetOfDays(self(), LocalDateTime.of(getStartDate(),
                  getStartTime()), LocalDateTime.of(getEndDate(),
                  getEndTime()), daysOfWeek, newOccurrences);
          break;
        case "specificEndTime":
          if (specificEndDate == null) {
            throw new IllegalArgumentException("Specific end date must be" +
                    " specified before specific end time");
          }
          newOccurrences = Calendar.getOccurrencesBetweenDates(LocalDateTime.of(getStartDate(),
                  getStartTime()), LocalDateTime.of(specificEndDate,
                  LocalTime.parse(value)), getEndTime(), daysOfWeek);
          specificEndTime = LocalTime.parse(value);
          Calendar.createRecurringEventOnSetOfDays(self(), LocalDateTime.of(getStartDate(),
                  getStartTime()), LocalDateTime.of(getEndDate(),
                  getEndTime()), daysOfWeek, newOccurrences);
          break;
        default:
          throw new IllegalArgumentException("Invalid property: " + property);
      }
      return self();
    }

    @Override
    AbstractEvent build() {
      return new RecurringEvent(this);
    }
  }


  @Override
  List<AbstractEvent> checkForPrintWithinRange(LocalDate startDate, LocalTime startTime,
                                               LocalDate endDate, LocalTime endTime) {
    List<AbstractEvent> events = new ArrayList<>();
    List<LocalDateTime> recurringStartDateList = new ArrayList<>(recurringStartDateTime);
    List<LocalDateTime> recurringEndDateList = new ArrayList<>(recurringEndDateTime);
    int size = Math.max(recurringStartDateList.size(), recurringEndDateList.size());
    for (int i = 0; i < size; i++) {
      LocalDateTime eventStart = recurringStartDateList.get(i);
      LocalDateTime eventEnd = recurringEndDateList.get(i);

      // Check if the event's date falls within the given date range (inclusive)
      if (eventStart.toLocalDate().isBefore(startDate) || eventEnd.toLocalDate().isAfter(endDate)) {
        continue;
      }

      // If the event starts on the first day, ensure its start time is not before
      // the specified start time.
      if (eventStart.toLocalDate().equals(startDate) && startTime != null) {
        if (eventStart.toLocalTime().isBefore(startTime)) {
          continue;
        }
      }

      // If the event ends on the last day, ensure its end time is not after the specified end time.
      if (eventEnd.toLocalDate().equals(endDate) && endTime != null) {
        if (eventEnd.toLocalTime().isAfter(endTime)) {
          continue;
        }
      }

      // If we reach here, the event is within the desired range.
      EventBuilder singleEvent = getOneEvent(eventStart, eventEnd);
      events.add(singleEvent.build());
    }

    return events;
  }




  @Override
  boolean showStatusHelper(LocalDate date, LocalTime time) {
    List<LocalDateTime> recurringStartDateList = new ArrayList<>(recurringStartDateTime);
    List<LocalDateTime> recurringEndDateList = new ArrayList<>(recurringEndDateTime);
    for (int i = 0; i < recurringStartDateList.size(); i++) {
      LocalDateTime eventStart = recurringStartDateList.get(i);
      LocalDateTime eventEnd =  recurringEndDateList.get(i);

      if (date.isEqual(eventStart.toLocalDate())) { // Only check against the start date
        if ((time.isAfter(eventStart.toLocalTime()) && time.isBefore(eventEnd.toLocalTime()))
                || time.equals(eventStart.toLocalTime())) {
          return true; // User is busy
        }
      }
    }
    return false;
  }


  @Override
  List<AbstractEvent> getAllEvents() {
    List<AbstractEvent> events = new ArrayList<>();
    List<LocalDateTime> recurringStartDateList = new ArrayList<>(recurringStartDateTime);
    List<LocalDateTime> recurringEndDateList = new ArrayList<>(recurringEndDateTime);
    for (int i = 0; i < recurringStartDateList.size(); i++) {
      LocalDateTime startDateOfEachEvent = recurringStartDateList.get(i);
      LocalDateTime endDateOfEachEvent = recurringEndDateList.get(i);
      EventBuilder singleEvent = getOneEvent(startDateOfEachEvent,endDateOfEachEvent);
      events.add(singleEvent.build());
    }
    return events;
  }

  @Override
  boolean checkConflict(LocalDateTime otherStartDateTime, LocalDateTime otherEndDateTime) {
    List<LocalDateTime> startDateTimeList = new ArrayList<>(this.recurringStartDateTime);
    List<LocalDateTime> endDateTimeList = new ArrayList<>(this.recurringEndDateTime);
    for (int i = 0; i < startDateTimeList.size(); i++) {
      if (otherStartDateTime.isBefore(startDateTimeList.get(i))
              && otherEndDateTime.isAfter(startDateTimeList.get(i))) {
        return true;
      }
      if (otherStartDateTime.isAfter(startDateTimeList.get(i))
              && otherStartDateTime.isBefore(endDateTimeList.get(i))) {
        return true;
      }
      if (otherStartDateTime.isEqual(startDateTimeList.get(i))) {
        return true;
      }
      if (otherEndDateTime.isEqual(endDateTimeList.get(i))) {
        return true;
      }
    }
    return false;
  }

  @Override
  AbstractEvent convertTimeZone(TimeZone timeZone, TimeZone newTimeZone) {
    RecurringEventBuilder recurringEventBuilder = (RecurringEventBuilder)
            getEventBuilderWithNewTimeZone(timeZone, newTimeZone);
    //convert specific end date and time to new time zone
    if (specificEndDate != null) {
      LocalDateTime newSpecificEndDateTime =
              convertTimeZoneHelper(timeZone,newTimeZone,
                      LocalDateTime.of(this.specificEndDate,this.specificEndTime));
      recurringEventBuilder.setSpecificEndDate(newSpecificEndDateTime.toLocalDate()).
              setSpecificEndTime(newSpecificEndDateTime.toLocalTime());
    }

    //create a set of recurringStartDate time
    Set<LocalDateTime> recurringStartDateTimeSet = new TreeSet<>();
    //create a set of recurringEndDateTime
    Set<LocalDateTime> recurringEndDateTimeSet = new TreeSet<>();
    //create a set of DaysOfWeek
    Set<DayOfWeek> daysOfWeekSet = new TreeSet<>();
    //create a list of StartDateTime
    List<LocalDateTime> startDateTimeList = new ArrayList<>(this.recurringStartDateTime);
    //create a list of EndDateTime
    List<LocalDateTime> endDateTimeList = new ArrayList<>(this.recurringEndDateTime);
    for (int i = 0; i < startDateTimeList.size(); i++) {
      //convert each StartDateTime to newer Time zone
      LocalDateTime newStartDateTime =
              convertTimeZoneHelper(timeZone,newTimeZone,startDateTimeList.get(i));
      //convert each EndDateTime to newer Time zone
      LocalDateTime newEndDateTime =
              convertTimeZoneHelper(timeZone,newTimeZone,endDateTimeList.get(i));
      //Add the days of week adhering to newer timezone to the set of days of week.
      daysOfWeekSet.add(newStartDateTime.getDayOfWeek());
      //Add new startDateTime to set of recurringStartDateTime
      recurringStartDateTimeSet.add(newStartDateTime);
      //Add new endDateTime to set of recurringStartDateTime
      recurringEndDateTimeSet.add(newEndDateTime);
    }
    //assign the new start date time and end date time to the recurring event.
    recurringEventBuilder.setRecurringStartDateTime(recurringStartDateTimeSet);
    recurringEventBuilder.setRecurringEndDateTime(recurringEndDateTimeSet);
    recurringEventBuilder.setDaysOfWeek(daysOfWeekSet);
    //build the recurring event.
    return recurringEventBuilder.build();
  }

  private RecurringEventBuilder getBuilder() {
    return new RecurringEventBuilder(getSubject(), getStartDate(), getStartTime(),
            getEndDate(), getEndTime())
            .setDescription(getDescription())
            .setLocation(getLocation())
            .setPrivate(getIsPrivate())
            .setDaysOfWeek(new TreeSet<>(daysOfWeek))
            .setOccurrences(occurrences)
            .setSpecificEndDate(specificEndDate)
            .setSpecificEndTime(specificEndTime)
            .setRecurringStartDateTime(new TreeSet<>(recurringStartDateTime))
            .setRecurringEndDateTime(new TreeSet<>(recurringEndDateTime));
  }

  @Override
  RecurringEventBuilder getEventCopy() {
    return getBuilder();
  }

  @Override
  RecurringEventBuilder getEventCopy(LocalDateTime dateTime, boolean after) {
    RecurringEventBuilder builder = getBuilder();
    if (after) {
      TreeSet<LocalDateTime> startDateAfterSubset = new TreeSet<>(getSubsetOfStartDateTime(
          date -> date.isAfter(dateTime) || date.isEqual(dateTime)));
      if (startDateAfterSubset.isEmpty()) {
        return null;
      }
      TreeSet<LocalDateTime> endDateAfterSubset = new TreeSet<>(getSubsetOfEndDateTime(
          date -> LocalDateTime.of(date.toLocalDate(),
                      builder.getStartTime()).isAfter(dateTime)
                      || LocalDateTime.of(date.toLocalDate(),
                      builder.getStartTime()).isEqual(dateTime)));
      builder.setRecurringStartDateTime(startDateAfterSubset)
              .setRecurringEndDateTime(endDateAfterSubset)
              .setStartDate(startDateAfterSubset.first().toLocalDate())
              .setEndDate(endDateAfterSubset.first().toLocalDate());
      if (builder.specificEndDate == null)  {
        builder.setOccurrences(startDateAfterSubset.size());
      }
    }
    else {
      TreeSet<LocalDateTime> startDateBeforeSubset = new TreeSet<>(getSubsetOfStartDateTime(
          date -> date.isBefore(dateTime)));
      if (startDateBeforeSubset.isEmpty()) {
        return null;
      }
      TreeSet<LocalDateTime> endDateBeforeSubset = new TreeSet<>(getSubsetOfEndDateTime(
          date -> LocalDateTime.of(date.toLocalDate(),
                      builder.getStartTime()).isBefore(dateTime)));
      builder.setRecurringStartDateTime(startDateBeforeSubset)
              .setRecurringEndDateTime(endDateBeforeSubset);
      if (builder.specificEndDate != null) {
        builder.setSpecificEndDate(endDateBeforeSubset.last().toLocalDate())
                .setSpecificEndTime(endDateBeforeSubset.last().toLocalTime());
      }
      else {
        builder.setOccurrences(startDateBeforeSubset.size());
      }
    }
    return builder;
  }

  @Override
  boolean startDateTimeMatches(LocalDateTime startDateTime) {
    for (LocalDateTime thisStartDateTime : this.recurringStartDateTime) {
      if (thisStartDateTime.isEqual(startDateTime)) {
        return true;
      }
    }
    return false;
  }

  @Override
  boolean endDateTimeMatches(LocalDateTime endDateTime) {
    for (LocalDateTime thisEndDateTime : this.recurringEndDateTime) {
      if (thisEndDateTime.isEqual(endDateTime)) {
        return true;
      }
    }
    return false;
  }



  @Override
  EventBuilder getOneEvent(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    return new SingleEvent.SingleEventBuilder(getSubject(), startDateTime.toLocalDate(),
            startDateTime.toLocalTime(), endDateTime.toLocalDate(), endDateTime.toLocalTime())
            .setDescription(getDescription())
            .setLocation(getLocation())
            .setPrivate(getIsPrivate());
  }

  Set<LocalDateTime> getSubsetOfStartDateTime(Predicate<LocalDateTime> condition) {
    return recurringStartDateTime.stream()
            .filter(condition)
            .collect(Collectors.toSet());
  }

  Set<LocalDateTime> getSubsetOfEndDateTime(Predicate<LocalDateTime> condition) {
    return recurringEndDateTime.stream()
            .filter(condition)
            .collect(Collectors.toSet());
  }

}
