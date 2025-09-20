package controller;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.ICalendar;
import view.ICalendarView;

/**
 * Command which provides operation to execute a copy operation of events
 * from current calendar to a provided target calendar to a target calendar date/time.
 */
public class CopyEventCommand extends Command {

  private final Map<String, String> args;

  /**
   * Assigns the calendar collection and calendar model
   * and initializes the argument map for copy command arguments.
   * @param calendarData calendar data which  contains calendar collection stored
   *                     in the controller and calendar model object
   */
  CopyEventCommand(CalendarData calendarData) {
    this.calendarCollection = calendarData.getCalendarCollection();
    this.args = initializeKeywordMap();
  }

  @Override
  void execute(ICalendar calendar, ICalendarView calendarView, List<String> tokens)
          throws IllegalArgumentException {
    checkNullCurrentCalendar(calendar);
    updateArgsMap(tokens);

    LocalDateTime startDateTime;
    LocalDateTime endDateTime;
    if (args.get("on") != null && args.get("between") == null && args.get("and") == null) {
      try {
        startDateTime = LocalDateTime.parse(args.get("on"));
        endDateTime = LocalDateTime.MAX;
        if (args.get("event") == null) {
          throw new IllegalArgumentException("Please mention only date for multiple event copy");
        }
      } catch (IllegalArgumentException e) {
        throw e;
      }
      catch (Exception e) {
        try {
          startDateTime = LocalDateTime.of(LocalDate.parse(args.get("on")), LocalTime.MIN);
          endDateTime = LocalDateTime.of(LocalDate.parse(args.get("on")), LocalTime.MAX);
          if (args.get("event") != null) {
            throw new IllegalArgumentException("Please mention dateTime string for on argument");
          }
        } catch (IllegalArgumentException e2) {
          throw e2;
        }
        catch (Exception e2) {
          throw new IllegalArgumentException("Invalid argument " + args.get("on"));
        }
      }
    }
    else if (args.get("on") == null && args.get("between") != null && args.get("and") != null
            && args.get("event") == null) {
      try {
        startDateTime = LocalDateTime.of(LocalDate.parse(args.get("between")), LocalTime.MIN);
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid argument " + args.get("between"));
      }
      try {
        endDateTime = LocalDateTime.of(LocalDate.parse(args.get("and")), LocalTime.MAX);
      }
      catch (Exception e) {
        throw new IllegalArgumentException("Invalid argument " + args.get("and"));
      }
    }
    else {
      throw new IllegalArgumentException("Invalid arguments: use either" +
              " 'between' and 'and' or 'on' keyword");
    }
    LocalDateTime targetStartDateTime = parseTargetStartDateTime();

    copyEvents(calendar, startDateTime, endDateTime, targetStartDateTime, calendarView);
  }

  private LocalDateTime parseTargetStartDateTime() {
    LocalDateTime targetStartDateTime;
    try {
      targetStartDateTime = LocalDateTime.parse(args.get("to"));
      if (args.get("event") == null) {
        throw new IllegalArgumentException("Please mention only target date for" +
                " multiple event copy");
      }
    } catch (IllegalArgumentException e) {
      throw e;
    }
    catch (Exception e) {
      if (args.get("event") != null) {
        throw new IllegalArgumentException("Invalid dateTime string provided " + args.get("to"));
      }
      try {
        targetStartDateTime = LocalDateTime.of(LocalDate.parse(args.get("to")), LocalTime.MIN);
      } catch (Exception e2) {
        throw new IllegalArgumentException("Invalid argument 'to' value " + args.get("to"));
      }
    }
    return targetStartDateTime;
  }

  private void updateArgsMap(List<String> tokens) throws IllegalArgumentException {
    int i;
    if (tokens.size() > 1) {
      if (tokens.get(1).equals("event")) {
        args.put("event", tokens.get(2));
        i = 3;
      }
      else if (tokens.get(1).equals("events")) {
        i = 2;
      }
      else {
        throw new IllegalArgumentException("Keyword event or events expected but got "
                + tokens.get(1));
      }
    }
    else {
      throw new IllegalArgumentException("Incomplete command no arguments specified");
    }
    for (; i < tokens.size(); i += 2) {
      String arg = tokens.get(i);
      if (args.containsKey(arg)) {
        if (args.get(arg) == null) {
          if (tokens.size() <= i + 1) {
            throw new IllegalArgumentException("Incomplete command: argument " + arg +
                    " values not mentioned");
          }
          args.put(arg, tokens.get(i + 1));
        } else {
          throw new IllegalArgumentException("Repeated command " + arg);
        }
      }
      else {
        throw new IllegalArgumentException("Invalid command argument: " + arg);
      }
    }
  }

  private void copyEvents(ICalendar calendar, LocalDateTime startDateTime,
                          LocalDateTime endDateTime, LocalDateTime targetStartDateTime,
                          ICalendarView calendarView)
          throws IllegalArgumentException {
    String eventsString = calendar.printEvents(startDateTime, endDateTime);
    if (eventsString.isEmpty()) {
      calendarView.outputResponse("No events found to copy\n");
      return;
    }
    List<String> events = splitCSV(eventsString);
    for (String event : events) {
      String[] eventFields = parts(event);
      String subject = eventFields[0];
      String newStartDateTime = eventFields[1] + "T" + eventFields[2];
      String newEndDateTime = eventFields[3] + "T" + eventFields[4];
      String description = eventFields[5];
      String location = eventFields[6];
      String isPrivateString = eventFields[7];
      if (description.isEmpty()) {
        description = null;
      }
      if (location.isEmpty()) {
        location = null;
      }
      Boolean isPrivate = null;
      if (!isPrivateString.isEmpty()) {
        isPrivate = Boolean.parseBoolean(isPrivateString);
      }

      LocalDateTime eventStart = LocalDateTime.parse(newStartDateTime);
      LocalDateTime eventEnd = LocalDateTime.parse(newEndDateTime);

      Duration durationBetweenDates = Duration.between(startDateTime, targetStartDateTime);
      Duration duration = Duration.between(eventStart, eventEnd);
      LocalDateTime newEventStartDateTime = eventStart.plus(durationBetweenDates);
      LocalDateTime newEventEndDateTime = newEventStartDateTime.plus(duration);


      String calendarName = args.get("--target");
      ICalendar targetCalendar = getCalendarUsingName(calendarName);
      if (targetCalendar == null) {
        throw new IllegalArgumentException("Target calendar not found");
      }

      if (args.get("event") != null) {
        if (args.get("event").equals(subject) && startDateTime.isEqual(eventStart)) {
          targetCalendar.createEvent(subject, newEventStartDateTime, newEventEndDateTime,
                  description, location, isPrivate, true);
          break;
        }
        else {
          continue;
        }
      }
      ZonedDateTime sourceZoneStart = newEventStartDateTime
              .atZone(calendar.getTimeZone().toZoneId());
      ZonedDateTime sourceZoneEnd = newEventEndDateTime
              .atZone(calendar.getTimeZone().toZoneId());

      ZonedDateTime targetZoneStart = sourceZoneStart
              .withZoneSameInstant(targetCalendar.getTimeZone().toZoneId());
      ZonedDateTime targetZoneEnd = sourceZoneEnd
              .withZoneSameInstant(targetCalendar.getTimeZone().toZoneId());
      LocalDateTime adjustedStartTime = targetZoneStart.toLocalDateTime();
      LocalDateTime adjustedEndTime = targetZoneEnd.toLocalDateTime();

      try {
        targetCalendar.createEvent(subject, adjustedStartTime, adjustedEndTime, description,
                location, isPrivate, true);
      } catch (IllegalArgumentException e) {
        calendarView.outputResponse("Event starting at: " + eventStart +
                " in source calendar conflicts with existing event in target calendar\n");
      }
    }
    calendarView.outputResponse("Event(s) copied successfully\n");
  }


  private Map<String, String> initializeKeywordMap() {
    Map<String, String> argsMap = new HashMap<>();
    argsMap.put("event", null);
    argsMap.put("on", null);
    argsMap.put("--target", null);
    argsMap.put("between", null);
    argsMap.put("and", null);
    argsMap.put("to", null);
    return argsMap;
  }
}
