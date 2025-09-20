package controller;

import java.io.BufferedReader;
import java.io.FileReader;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import model.ICalendar;
import view.ICalendarView;

/**
 * Represent a controller that accepts input from a graphical user interface and displays
 * data on the graphical user interface. This controller also provides features
 * as callback functions to the GUI view to call back whn a action is triggerred by user.
 */
public class GraphicalController implements ICalendarController, Features {

  private final ICalendarView calendarView;

  private final CalendarData calendarData;

  private final Command command;

  /**
   * Starts the controller by taking the model and view objects.
   * Initializes the set of calendars with one default calendar
   * and provides this object to the view to add features.
   * @param calendar Calendar model which can be used to do event operations on the calendar
   * @param calendarView Calendar view is used to display the output to the user interface.
   */
  public GraphicalController(ICalendar calendar, ICalendarView calendarView) {
    Set<ICalendar> calendarCollection = new HashSet<>();
    calendarCollection.add(calendar);
    this.calendarView = calendarView;
    this.calendarData = new CalendarData(calendarCollection, calendar);
    calendarData.setCurrentCalendar(calendar);
    command = new CreateCalendarCommand(calendarData);
    calendarView.addFeatures(this);
  }

  @Override
  public void createCalendar(String name, String timezone) throws IllegalArgumentException {
    try {
      command.createNewCalendar(calendarView, timezone, name);
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  @Override
  public List<String> viewEvents(LocalDate date) {
    ICalendar calendar = calendarData.getCurrentCalendar();
    String eventsString = calendar.printEvents(LocalDateTime.of(date, LocalTime.MIN),
            LocalDateTime.of(date, LocalTime.MAX));
    if (eventsString.isEmpty()) {
      return Collections.emptyList();
    }
    return new ArrayList<>(command.splitCSV(eventsString));
  }

  @Override
  public void editEvent(String subject, LocalDate startDate, LocalTime startTime, LocalDate endDate,
                        LocalTime endTime, String property, String newValue) {
    ICalendar calendar = calendarData.getCurrentCalendar();
    calendar.editEvent(subject, LocalDateTime.of(startDate, startTime),
            LocalDateTime.of(endDate, endTime), property, newValue);
  }

  @Override
  public void editEvents(String subject, boolean allEvents, LocalDate startDate,
                         LocalTime startTime, String property, String newValue) {
    ICalendar calendar = calendarData.getCurrentCalendar();
    try {
      if (allEvents) {
        calendar.editEvents(subject, null, property, newValue);
      }
      else {
        calendar.editEvents(subject, LocalDateTime.of(startDate, startTime), property, newValue);
      }
      calendarView.outputResponse("Multiple events edited successfully");
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  @Override
  public void createEvent(String subject, LocalDate startDate, LocalTime startTime,
                          LocalDate endDate, LocalTime endTime, String description, String location,
                          Boolean isPrivate, boolean isRecurring, String daysOfWeek,
                          boolean isOccurrences, int occurrences, LocalDate specificEndDate) {
    ICalendar calendar = calendarData.getCurrentCalendar();
    try {
      if (!isRecurring) {
        calendar.createEvent(subject, LocalDateTime.of(startDate, startTime),
                LocalDateTime.of(endDate, endTime), description, location, isPrivate, true);
      }
      else {
        if (isOccurrences) {
          calendar.createRecurringEventWithOccurrences(subject, LocalDateTime.of(startDate,
                          startTime), LocalDateTime.of(endDate, endTime),
                  description, location, isPrivate, daysOfWeek, occurrences);
        }
        else {
          calendar.createRecurringEventWithSpecificEndDateTime(subject, LocalDateTime.of(startDate,
                  startTime), LocalDateTime.of(endDate, endTime), description, location, isPrivate,
                  daysOfWeek, LocalDateTime.of(specificEndDate, LocalTime.MAX));
        }
      }
      calendarView.outputResponse("Event created successfully");
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  @Override
  public void useCalendar(String calendar) throws IllegalArgumentException {
    command.useCurrentCalendar(calendar);
  }

  @Override
  public void importCalendar(String fileName) throws IllegalArgumentException {
    Map<String,Integer> fields = new HashMap<>();
    fields.put("Subject", null);
    fields.put("Start Date", null);
    fields.put("Start Time", null);
    fields.put("End Date", null);
    fields.put("End Time", null);
    fields.put("Description", null);
    fields.put("Location", null);
    fields.put("Private", null);
    fields.put("All Day Event", null);
    List<String> list = new ArrayList<>();
    if (!fileName.endsWith(".csv")) {
      throw new IllegalArgumentException("File Name must end with .csv");
    }
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
      String line;
      boolean firstLine = true;
      while ((line = reader.readLine()) != null) {
        if (firstLine) {
          String[] parts = command.parts(line);
          for (int i = 0; i < parts.length; i++) {
            if (fields.containsKey(parts[i])) {
              fields.put(parts[i],i);
            }
          }
          firstLine = false;
        }
        else {
          list = command.checkFileValid(line,list,fields);
        }
      }
      createEvents(list,fields);
      calendarView.outputResponse("File Imported successfully");
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  private void createEvents(List<String> list, Map<String,Integer> fields)
          throws IllegalArgumentException {
    for (String line : list) {
      String[] parts = command.parts(line);
      String subject = parts[fields.get("Subject")];
      LocalDate startDate = LocalDate.parse(parts[fields.get("Start Date")]);
      LocalTime startTime = LocalTime.parse(parts[fields.get("Start Time")]);
      LocalDate endDate = LocalDate.parse(parts[fields.get("End Date")]);
      LocalTime endTime = LocalTime.parse(parts[fields.get("End Time")]);
      String description = null;
      if (fields.get("Description") != null) {
        description = parts[fields.get("Description")];
        if (description.isEmpty()) {
          description = null;
        }
      }
      String location = null;
      if (fields.get("Location") != null) {
        location = parts[fields.get("Location")];
        if (location.isEmpty()) {
          location = null;
        }
      }
      Boolean isPrivate = null;
      if (fields.get("Private") != null) {
        isPrivate = command.checkIsPrivate(parts[fields.get("Private")]);
      }


      if (fields.get("All Day Event") != null) {
        boolean allDayEvent = Boolean.parseBoolean(parts[fields.get("All Day Event")]);
        if (allDayEvent) {
          endDate = startDate;
          startTime = LocalTime.of(0,0);
          endTime = LocalTime.of(23,59);
        }
      }
      
      LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
      LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);
      ICalendar calendar = calendarData.getCurrentCalendar();
      calendar.createEvent(subject,startDateTime,endDateTime,description,location,isPrivate,
              true);
    }
  }

  @Override
  public void exportCalendar(String fileName) throws IllegalArgumentException {
    if (!fileName.endsWith(".csv")) {
      throw new IllegalArgumentException("CSV file does not end with .csv");
    }
    ICalendar calendar = calendarData.getCurrentCalendar();
    List<List<String>> data = calendar.exportEvents();
    try {
      command.writeFileWithEvents(fileName,data);
      calendarView.outputResponse("Calendar Exported successfully");
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  @Override
  public void listenInput() {
    //GUI controller doesn't require a go method to start accepting input
  }
}
