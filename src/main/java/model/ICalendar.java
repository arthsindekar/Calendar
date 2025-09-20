package model;


import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;

/**
 * This interface represents the calendar which provides different operations
 * that can be performed on a calendar.
 */
public interface ICalendar {
  /**
   * Create an event based on the parameters that
   * are passed to it.The name, start date time and end date time of an event together uniquely
   * identify an event. These are mandatory fields.
   * @param subject describes the name of the event which cannot be null.
   *                Name of the event can be same for different events.
   * @param startDateTime describes the start date and start time of a particular event.
   *                      This field cannot be null since it describes when the event starts.
   * @param endDateTime describes the end date and end time of an event. This field cannot be null
   *                    since it defines when an event ends.
   * @param description provides the detailed description of an event. This field is can be null
   *                    which means user may choose to provide the description for an event.
   * @param location describes address of the event. This field is can be null
   *                 which means user may choose to provide the location for an event.
   * @param isPrivate  defines if the event is going to be public or private.
   *                   If true is passed then the event is categorized as private otherwise public.
   *                   This field can be null.
   * @param autoDecline defines whether the  event should be created or declined
   *                    based on conflicting events.
   *                    If true is passed then the event would not be created if it is conflicting
   *                    with another event,otherwise event will be created.
   *                    If false is passed then the event will be created even if conflicting
   *                    with other events.
   * @throws IllegalArgumentException When subject,end date time and start date time
   *                                  fields are null.
   *                                  When start date time is defined after end date time.
   */
  void createEvent(String subject, LocalDateTime startDateTime,
                      LocalDateTime endDateTime, String description,
                      String location, Boolean isPrivate, boolean autoDecline)
          throws IllegalArgumentException;

  /**
   * Create a recurring event that repeats for
   * specified amount of times. The subject,start time date and end date time, days of week,
   * number of occurrences are mandatory fields. This calendar does not allow recurring events
   * to span for multiple days.
   *
   * @param subject describes the name of the event which cannot be null.
                    Name of the event can be same for different events.
   * @param startDateTime describes the start date and start time of the recurring event series
   *                      in the series.This field cannot be null since it describes when the
   *                      event starts.
   * @param endDateTime describes the end date and end time of a particular event in the series.
   *                    This field cannot be null since it defines when an event ends.
   * @param description provides the detailed description of an event. This field is can be null
   *                    which means user may choose to provide the description for an event.
   * @param location describes where the event is going to be held.This field is can be null
   *                 which means user may choose to provide the location for an event.
   * @param isPrivate  defines if the event is going to be public or private.
   *                   If true is passed then the event is categorized as private otherwise public.
   *                   This field can be null.
   * @param daysOfWeek defines on which week day the event repeats. This field cannot be null.
   *                   The following letters define the week days from Monday to Sunday.
   *                   Monday - 'M' ,Tuesday 'T',Wednesday-'W',Thursday-'R',Friday -'F',
   *                   Saturday-'S',Sunday-'U'.
   * @param occurrences defines the number of times the event must be repeated.
   *                    This field cannot be null
   * @throws IllegalArgumentException if any of subject ,start date time ,end date time ,
   *                                  days of week for occurrences is null.
   *                                  When number of occurrences is less than or equal to 0.
   *                                  When start date time is after the end date time.
   */
  void createRecurringEventWithOccurrences(String subject, LocalDateTime startDateTime,
                                           LocalDateTime endDateTime, String description,
                                           String location, Boolean isPrivate, String daysOfWeek,
                                           int occurrences) throws IllegalArgumentException;

  /**
   * Creates a recurring event when specific end date is provided.
   * @param subject describes the name of the event which cannot be null.
                    Name of the event can be same for different events.
   * @param startDateTime describes the start date and start time of the recurring event series
   *                      in the series.This field cannot be null since it describes when the
   *                      event starts.
   * @param endDateTime describes the end date and end time of a particular event in the series.
   *                    This field cannot be null since it defines when an event ends.
   * @param description provides the detailed description of an event. This field is can be null
   *                    which means user may choose to provide the description for an event.
   * @param location describes where the event is going to be held.This field is can be null
   *                 which means user may choose to provide the location for an event.
   * @param isPrivate  defines if the event is going to be public or private.
   *                   If true is passed then the event is categorized as private otherwise public.
   *                   This field can be null.
   * @param daysOfWeek defines on which week day the event repeats. This field cannot be null.
   *                   The following letters define the week days from Monday to Sunday.
   *                   Monday - 'M' ,Tuesday 'T',Wednesday-'W',Thursday-'R',Friday -'F',
   *                   Saturday-'S',Sunday-'U'.
   * @param specificEndDateTime defines the specific end date of the recurring event.
   *                            This field cannot be null.
   * @throws IllegalArgumentException    if any of subject ,start date time ,end date time ,
   *                                     days of week for occurrences and specific end date
   *                                     is null.
   *                                     When number of occurrences is less than or equal to 0.
   *                                     When start date time is after the end date time.
   */
  void createRecurringEventWithSpecificEndDateTime(String subject, LocalDateTime startDateTime,
                                           LocalDateTime endDateTime, String description,
                                           String location, Boolean isPrivate, String daysOfWeek,
                                           LocalDateTime specificEndDateTime)
          throws IllegalArgumentException;

  /**
   * Edits the recurring event based on provided subject,start date time.
   * Properties that can be changed :subject,startDate ,startTime,endDate ,endTime,
   * description,location,isPrivate,daysOfWeek,occurrences,specificEndDate,
   * specificEndTime.
   *
   * @param subject defines the name of the name of the event that needs to be changed.
   *                This field cannot be null.
   * @param startDateTime defines the start date of the event that needs to be changed.
   *                      This field cannot be null.
   * @param property defines the property that needs to be changed.
   * @param newValue defines the value to be assigned to the event.
   * @throws IllegalArgumentException when any of subject,start date time property is
   *                                  provided as null.
   *                                   If any other of the above property is given.
   */
  void editEvents(String subject, LocalDateTime startDateTime,
                 String property, String newValue)throws IllegalArgumentException;


  /**
   * Edits the single event based on provided subject,start date time,end date time.
   * Properties that can be changed :subject,startDate ,startTime,endDate ,endTime,
   * description,location,isPrivate.
   * @param subject defines the name of the name of the event that needs to be changed.
   *                This field cannot be null.
   * @param startDateTime defines the start date of the event that needs to be changed.
   *                      This field cannot be null.
   * @param endDateTime defines the end date time of the event.This field cannot be null.
   * @param property defines the property that needs to be changed.
   * @param newValue defines the value to be assigned to the event.
   * @throws IllegalArgumentException when any of subject,start date time,end date time
   *                                  property is provided as null.
   *                                  If any other of the above property is given.
   */
  void editEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime,
                 String property, String newValue) throws IllegalArgumentException;




  /** Provides the information of events within a particular
   * interval of time.
   * @param startDateTime defines the 'from' datetime.
   * @param endDateTime defines the 'to' date time.
   * @return list of events that fall within the specified interval.
   * @throws IllegalArgumentException if start date time is null.
   *                                  if start date time is after end date time.
   */
  String printEvents(LocalDateTime startDateTime, LocalDateTime endDateTime)
          throws IllegalArgumentException;

  /**
   * Exports the events in a list of strings
   * file format which adheres to the format specified by the
   * Google Calendar. The list of strings contain information about each event and its list contains
   * information about all events.
   * @return list of strings which contains information regarding events.
   */
  List<List<String>> exportEvents();

  /**  Defines the status of the user as busy or available.
   * @param localDateTime defines the date and time at which we want to know the status of the user.
   * @return Busy if user is busy and Available otherwise.
   * @throws NullPointerException if local date time is null.
   */
  String showStatus(LocalDateTime localDateTime) throws IllegalArgumentException;

  /**
   * Gets the timezone of the calendar, timezone in which the events are displayed.
   * @return timezone in which the events of calendar are displayed
   */
  TimeZone getTimeZone();

  /**
   * Sets the timezone of the calendar which adjusts the existing events to the new timezone.
   * @param timeZone New timezone to which the events in this calendar are to be adjusted to
   */
  void setTimeZone(TimeZone timeZone);

  /**
   * Gets the name of calendar to identify the calendar while using it.
   * @return the name of the calendar
   */
  String getName();

  /**
   * Sets the name of calendar to identify the calendar while using it.
   * @param name the name of the calendar
   */
  void setName(String name);

  /**
   * Gives the copy of this calendar instance with Name and Timezone copied not events.
   * @return copy of this calendar with Name and Timezone copied not events.
   */
  ICalendar getCalendarCopy();

}
