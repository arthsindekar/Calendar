package controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import model.ICalendar;
import view.ICalendarView;

/**
 * An abstract class representing a command that can be executed within a calendar system.
 * The subclasses  implement the {@code execute} method to define specific command behavior.
 */
public abstract class Command {

  Set<ICalendar> calendarCollection;

  private ICalendar calendarModel;

  private CalendarData calendarData;

  Command(CalendarData calendarData) {
    this.calendarData = calendarData;
    this.calendarCollection = calendarData.getCalendarCollection();
    this.calendarModel = calendarData.getCalendarModel();
  }

  /**
   * Initializes a empty calendar model set collection.
   */
  Command() {
    calendarCollection = new HashSet<>();
  }

  /**
   * Executes the command using the provided calendar, calendar view, and input tokens.
   * Implementations  handle command-specific logic, including validation and execution.
   *
   * @param calendar     the calendar instance on which the command operates
   * @param calendarView the view component for displaying calendar-related information
   * @param tokens       a list of string tokens representing parsed command input
   * @throws IllegalArgumentException if the command input is invalid or cannot be processed
   */
  abstract void execute(ICalendar calendar, ICalendarView calendarView, List<String> tokens)
          throws IllegalArgumentException;

  ICalendar getCalendarUsingName(String calendarName) {
    for (ICalendar calendar : calendarCollection) {
      if (calendar.getName().equals(calendarName)) {
        return calendar;//calendar exists.
      }
    }
    return null; //calendar does not exist.
  }

  TimeZone checkTimezone(String timezone) throws IllegalArgumentException {
    TimeZone tz = TimeZone.getTimeZone(timezone);
    if (tz.getID().equals(timezone)) {
      return tz;
    } else {
      throw new IllegalArgumentException("Invalid Timezone");
    }
  }

  void checkUnavailableCalendars(String calendarName)
          throws IllegalArgumentException {
    //check if calendar name does not exist.
    if (getCalendarUsingName(calendarName) == null) {
      throw new IllegalArgumentException("Calendar does not exist.");
    }
  }

  void checkNullCurrentCalendar(ICalendar calendar) throws IllegalArgumentException {
    if (calendar == null) {
      throw new IllegalArgumentException("Please use a calendar first.");
    }
  }

  void createNewCalendar(ICalendarView calendarView, String timezone, String calendarName)
          throws IllegalArgumentException {
    TimeZone tz = checkTimezone(timezone);
    ICalendar newCalendar = calendarModel.getCalendarCopy();
    newCalendar.setName(calendarName);
    newCalendar.setTimeZone(tz);
    Set<ICalendar> calendarCollection = this.calendarCollection;
    if (getCalendarUsingName(calendarName) == null) {  //calendar does not exist hence create new.
      calendarCollection.add(newCalendar);
      calendarView.outputResponse("Calendar added successfully with name: "
              + calendarName + " and timezone: " + tz.getID() + "\n");
    } else {
      throw new IllegalArgumentException("Calendar already exists\n");
    }
  }

  void useCurrentCalendar(String calendarName) {
    checkUnavailableCalendars(calendarName);
    calendarData.setCurrentCalendar(getCalendarUsingName(calendarName));
  }

  void writeFileWithEvents(String fileName, List<List<String>> data)
          throws IOException {
    File file = new File(fileName);
    try (FileWriter fileWriter = new FileWriter(file)) {
      for (List<String> row : data) {
        row.replaceAll(cell -> "\"" + cell + "\"");
        fileWriter.append(String.join(",", row));
        fileWriter.append("\n");
      }
    }
    catch (IOException e) {
      throw new IOException(e.getMessage());
    }
  }

  List<String> checkFileValid(String line, List<String> list,Map<String,Integer> fields) {
    try {
      String[] parts = parts(line);

      if (fields.get("Subject") == null) {
        throw new IllegalArgumentException("Subject field is required");
      }

      if (fields.get("Start Date") == null) {
        throw new IllegalArgumentException("Start Date field is required");
      }
      if (fields.get("Start Time") == null) {
        throw new IllegalArgumentException("Start Time field is required");
      }

      if (fields.get("End Date") == null) {
        throw new IllegalArgumentException("End Date field is required");
      }

      if (fields.get("End Time") == null) {
        throw new IllegalArgumentException("End Time field is required");
      }

      LocalDate.parse(parts[fields.get("Start Date")]);
      LocalTime.parse(parts[fields.get("Start Time")]);
      LocalDate.parse(parts[fields.get("End Date")]);
      LocalTime.parse(parts[fields.get("End Time")]);
      if (fields.get("Private") != null) {
        checkIsPrivate(parts[fields.get("Private")]);
      }

      if (fields.get("All Day Event") != null) {
        Boolean.parseBoolean(parts[fields.get("All Day Event")]);
      }

      list.add(line);
    } catch (Exception e) {
      throw new IllegalArgumentException("Parsing error: " + e.getMessage());
    }
    return list;
  }

  Boolean checkIsPrivate(String isPrivateString) {
    Boolean isPrivate = null;
    if (!isPrivateString.isEmpty()) {
      isPrivate = Boolean.parseBoolean(isPrivateString);
    }
    return isPrivate;
  }

  String[] parts(String line) {
    String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    for (int i = 0; i < parts.length; i++) {
      parts[i] = parts[i].replaceAll("^\"|\"$", "").trim();
    }
    return parts;
  }

  List<String> splitCSV(String input) {
    List<String> result = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    boolean inQuotes = false;

    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);

      if (c == '"') {
        inQuotes = !inQuotes;
      }

      if (c == '\n' && !inQuotes) {
        if (current.length() > 0) {
          result.add(current.toString());
          current.setLength(0);
        }
      } else {
        current.append(c);
      }
    }

    if (current.length() > 0) {
      result.add(current.toString());
    }

    return result;
  }
}
