package controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * The Features interface defines all the user-interaction callbacks
 * that the controller will handle, typically triggered by the View (GUI).
 */
public interface Features {

  /**
   * Creates a new calendar with the given name and timezone.
   * This method is triggered when user enters the name and timezone of calendar.
   * @param name the name of the calendar
   * @param timezone the timezone for the calendar
   */
  void createCalendar(String name, String timezone);

  /**
   * Retrieves a list of event summaries scheduled on a specific date.
   * This method is triggered when user clicks on a day in the month view of the calendar.
   * @param date the date to view events for
   * @return list of event summaries
   */
  List<String> viewEvents(LocalDate date);

  /**
   * Edits a specific property of an existing event.
   * This method is triggered when user clicks on an event in the month view of the calendar.
   * User cannot edit an event when the subject, startDate and startTime conflict with each other.
   * @param subject the subject/title of the event
   * @param startDate start date of the event
   * @param startTime start time of the event
   * @param endDate end date of the event
   * @param endTime end time of the event
   * @param property the property to be edited (e.g., "location", "description","subject",
   *                 "startDate","startTime","endDate","endTime")
   * @param newValue the new value for the specified property
   */
  void editEvent(String subject, LocalDate startDate, LocalTime startTime, LocalDate endDate,
                 LocalTime endTime, String property, String newValue);

  /**
   * Edits a specific property of multiple existing events. Either all the events
   * matching the provided event name/subject or events after a specified point of time are edited.
   * @param subject the subject/title of the multiple event
   * @param allEvents specifies if all events are to be edited or not
   * @param startDate specified date after which events are to be edited
   * @param startTime specified time on above date after which events are to be edited
   * @param property the event property to be edited
   * @param newValue the new value to be put for the specified event property
   */
  void editEvents(String subject, boolean allEvents, LocalDate startDate,
                  LocalTime startTime, String property, String newValue);

  /**
   * Creates a new event with specified parameters, including recurrence.
   * This method is triggered when user clicks "Add New Event" button
   * in the view event pane of the GUI.
   * @param subject the title of the event
   * @param startDate the start date
   * @param startTime the start time
   * @param endDate the end date
   * @param endTime the end time
   * @param description description of the event
   * @param location event location
   * @param isPrivate whether the event is private
   * @param isRecurring whether the event is recurring
   * @param daysOfWeek days the event recurs on (if applicable)
   * @param isOccurrences whether recurrence is based on number of occurrences
   * @param occurrences number of occurrences (if applicable)
   * @param specificEndDate the specific end date for recurrence (if applicable)
   */
  void createEvent(String subject, LocalDate startDate, LocalTime startTime, LocalDate endDate,
                   LocalTime endTime, String description, String location, Boolean isPrivate,
                   boolean isRecurring, String daysOfWeek, boolean isOccurrences, int occurrences,
                   LocalDate specificEndDate);

  /**
   * Switches to a specified calendar by name.
   * This method is triggered when user selects a particular calendar from the dropdown menu of GUI.
   * User need not create a calendar to create an event. The "Default" Calendar is already selected
   * for the user by the application.
   * @param calendar the name of the calendar to use
   */
  void useCalendar(String calendar);

  /**
   * Imports a calendar from the specified file.
   * This method accepts file in the csv format which adheres to Google Calendar norms.
   * This method is triggered when user enters the name of the file to be imported.
   * @param fileName the file name that we want to import.
   * @throws IllegalArgumentException   if provided name does not end with ".csv" extension.
   */
  void importCalendar(String fileName) throws IllegalArgumentException;

  /**
   * Exports events in the csv file format which adheres to Google Calendar norms.
   * This method is triggered when user enters the name of the file to be imported.
   * @param fileName the name or path of the file to import from
   * @throws IllegalArgumentException   if provided name does not end with ".csv" extension.
   */
  void exportCalendar(String fileName);
}
