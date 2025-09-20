package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents a single, non-recurring event that extends the functionality of {@code AbstractEvent}.
 * This class is used for events that occur only once at a specific date and time.
 */
public class SingleEvent extends AbstractEvent {

  private SingleEvent(SingleEventBuilder eventBuilder) {
    super(eventBuilder.getSubject(), eventBuilder.getStartDate(), eventBuilder.getStartTime(),
            eventBuilder.getEndDate(), eventBuilder.getEndTime(), eventBuilder.getDescription(),
            eventBuilder.getLocation(), eventBuilder.getIsPrivate());
  }

  /**
   * A builder class for constructing instances of {@code SingleEvent}.
   * This class extends {@code EventBuilder} to provide a fluent API for setting up
   * a single, non-recurring event with properties such as subject, date, and time.
   */
  public static class SingleEventBuilder extends EventBuilder<SingleEventBuilder> {

    /**
     * Constructs a {@code SingleEventBuilder} with the specified subject,
     * start date, and start time.
     * This initializes the builder with the essential details required to create a single event.
     *
     * @param subject   the name of the event
     * @param startDate the date when the event occurs
     * @param startTime the time when the event starts
     */
    public SingleEventBuilder(String subject, LocalDate startDate, LocalTime startTime,
                              LocalDate endDate, LocalTime endTime) {
      super(subject, startDate, startTime, endDate, endTime);
    }

    @Override
    SingleEventBuilder self() {
      return this;
    }

    @Override
    AbstractEvent build() {
      return new SingleEvent(this);
    }
  }

  private SingleEventBuilder getBuilder() {
    return new SingleEventBuilder(getSubject(), getStartDate(), getStartTime(),
            getEndDate(), getEndTime())
            .setDescription(getDescription())
            .setLocation(getLocation())
            .setPrivate(getIsPrivate());
  }


  @Override
  SingleEventBuilder getEventCopy() {
    return getBuilder();
  }

  @Override
  SingleEventBuilder getEventCopy(LocalDateTime dateTime, boolean after) {
    if (after && (dateTime.isBefore(LocalDateTime.of(getStartDate(), getStartTime()))
            || dateTime.isEqual(LocalDateTime.of(getStartDate(), getStartTime())))) {
      return getBuilder();
    }
    return null;
  }

  @Override
  EventBuilder getOneEvent(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    return getBuilder();
  }
}
