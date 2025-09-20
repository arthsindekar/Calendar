package controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import model.ICalendar;
import view.ICalendarView;


/**
 * A command that prints event details to the console or another output medium.
 * This class extends {@code Command} and is responsible for displaying events
 * from the calendar system based on the provided input tokens.
 */
public class PrintEventCommand extends Command {

  private final Map<String, String> args;

  /**
   * Constructs a {@code PrintEventCommand} and initializes the argument keyword map.
   * This constructor prepares the necessary data structures for processing the command input.
   */
  PrintEventCommand(CalendarData calendarData) {
    this.args = initializeKeywordMap();
  }

  /**
   * Executes the command to print event details based on the provided input.
   * This method processes the tokens, retrieves the necessary event details from the
   * {@code ICalendar} instance, and prints them to the specified output medium.
   * @param calendar     the {@code ICalendar} instance from which events will be printed.
   * @param calendarView the {@code ICalendarView} instance to update the user interface.
   * @param tokens       a list of string tokens representing the parsed input for printing options.
   * @throws IllegalArgumentException if the input is invalid or if the printing fails.
   */
  @Override
  void execute(ICalendar calendar, ICalendarView calendarView, List<String> tokens)
          throws IllegalArgumentException {
    checkNullCurrentCalendar(calendar);
    if (tokens.size() >= 2) {
      if (!tokens.get(1).equals("events")) {
        throw new IllegalArgumentException("Invalid command: expected events but got "
                + tokens.get(1));
      }
    }
    else {
      throw new IllegalArgumentException("Incomplete command: missing arguments");
    }

    for (int i = 2; i < tokens.size(); i += 2) {
      String arg = tokens.get(i);
      if (args.containsKey(arg)) {
        if (args.get(arg) == null) {
          if (tokens.size() <= i + 1) {
            throw new IllegalArgumentException("Incomplete command: argument " + arg +
                    " values not mentioned");
          }
          args.put(arg, tokens.get(i + 1));
        }
        else {
          throw new IllegalArgumentException("Repeated command " + arg);
        }
      }
      else {
        throw new IllegalArgumentException("Invalid command argument: " + arg);
      }
    }

    LocalDateTime startDateTime;
    LocalDateTime endDateTime;

    if (args.get("from") == null && args.get("to") == null && args.get("on") != null) {
      try {
        startDateTime = LocalDateTime.parse(args.get("on"));
      } catch (Exception e) {
        try {
          startDateTime = LocalDateTime.of(LocalDate.parse(args.get("on")),
                  LocalTime.MIN);
        } catch (Exception ex) {
          startDateTime = null;
        }
      }
      try {
        endDateTime = LocalDateTime.parse(args.get("on"));
      } catch (Exception e) {
        try {
          endDateTime = LocalDateTime.of(LocalDate.parse(args.get("on")),
                  LocalTime.MAX);
        } catch (Exception ex) {
          endDateTime = null;
        }
      }
    }
    else if (args.get("from") != null && args.get("to") != null && args.get("on") == null) {
      try {
        startDateTime = LocalDateTime.parse(args.get("from"));
      } catch (Exception e) {
        startDateTime = null;
      }
      try {
        endDateTime = LocalDateTime.parse(args.get("to"));
      }
      catch (Exception e) {
        endDateTime = null;
      }
    }
    else {
      throw new IllegalArgumentException("Invalid arguments: use either from and to or on keyword");
    }
    try {
      String events = calendar.printEvents(startDateTime, endDateTime);
      if (events.isEmpty()) {
        return;
      }
      List<String> lines = splitCSV(events);
      StringBuilder eventString = new StringBuilder();
      for (String line : lines) {
        String[] eventFields = parts(line);
        eventString.append("•").append("Subject:").append(eventFields[0]).append(" ").append("from")
                .append(" ").append(eventFields[1]).append(" ").append(eventFields[2]).append(" to")
                .append(" ").append(eventFields[3]).append(" ").append(eventFields[4]);
        if (!eventFields[5].isEmpty()) {
          eventString.append(" ").append("Description:").append(eventFields[5]);
        }
        if (!eventFields[6].isEmpty()) {
          eventString.append(" ").append("at").append(" ").append(eventFields[6]);
        }

        if (!eventFields[7].isEmpty()) {
          eventString.append(" ").append(eventFields[7]);
        }
        eventString.append("\n");
      }
      calendarView.outputResponse(eventString.toString());
    }
    catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  private Map<String, String> initializeKeywordMap() {
    Map<String, String> argsMap = new HashMap<>();
    argsMap.put("from", null);
    argsMap.put("to", null);
    argsMap.put("on", null);
    return argsMap;
  }
}
