package controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.ICalendar;
import view.ICalendarView;

/**
 * A command that edits an existing event in the calendar system.
 * This class extends {@code Command} and provides functionality to modify
 * event details such as title, date, time, or recurrence settings.
 */
public class EditEventCommand extends Command {

  private final Map<String, String> args;


  /**
  * Constructs an {@code EditEventCommand} and initializes the argument keyword map.
  * This constructor sets up the necessary data structures to process the user input
  * for editing an existing event.
  */
  EditEventCommand() {
    this.args = initializeKeywordMap();
  }

  /**
   * Executes the command to edit an existing event in the calendar system.
   * This method processes the input tokens, validates the event details, and
   * applies the changes to the specified {@code ICalendar} instance.
   * The {@code ICalendarView} is updated to reflect the modifications.
   *
   * @param calendar     the {@code ICalendar} instance where the event is stored and edited
   * @param calendarView the {@code ICalendarView} instance to update the calendar view
   * @param tokens       a list of string tokens representing the parsed input for editing the event
   * @throws IllegalArgumentException if the input is invalid or if the event to be edited
   *                                  cannot be found
   */
  @Override
  void execute(ICalendar calendar, ICalendarView calendarView, List<String> tokens)
          throws IllegalArgumentException {
    checkNullCurrentCalendar(calendar);
    String property = null;
    for (int i = 1; i < tokens.size(); i += 2) {
      String arg = tokens.get(i);
      if (args.containsKey(arg)) {
        if (arg.equals("event") || arg.equals("events")) {
          if (args.get("event") == null && args.get("events") == null) {
            if (tokens.size() <= i + 2) {
              throw new IllegalArgumentException("Incomplete command: argument " + arg +
                      " values not mentioned");
            }
            args.put(arg, tokens.get(i + 2));
            property = tokens.get(i + 1);
            if (arg.equals("events")) {
              if (tokens.size() == i + 4) {
                args.put("with", tokens.get(i + 3));
                i++;
              }
            }
            i++;
          }
          else {
            throw new IllegalArgumentException("Repeated command " + arg);
          }
        }
        else {
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
      }
      else {
        throw new IllegalArgumentException("Invalid command argument: " + arg);
      }
    }

    LocalDateTime startDateTime;
    LocalDateTime endDateTime;
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
    if (args.get("with") == null) {
      throw new IllegalArgumentException("Command argument with is missing");
    }
    if (args.get("event") == null && args.get("events") != null) {
      if (args.get("to") != null) {
        throw new IllegalArgumentException("Invalid command argument 'to'");
      }
      try {
        calendar.editEvents(args.get("events"), startDateTime, property, args.get("with"));
        calendarView.outputResponse("Edited series of events successfully\n");
      } catch (Exception e) {
        throw new IllegalArgumentException(e.getMessage());
      }
    }
    else if (args.get("event") != null && args.get("events") == null) {
      try {
        calendar.editEvent(args.get("event"), startDateTime, endDateTime, property,
                args.get("with"));
        calendarView.outputResponse("Edited single event successfully\n");
      } catch (Exception e) {
        throw new IllegalArgumentException(e.getMessage());
      }
    }
    else {
      throw new IllegalArgumentException("Invalid Command: provide either event or events" +
              " keyword with property and event name");
    }
  }

  private Map<String, String> initializeKeywordMap() {
    Map<String, String> argsMap = new HashMap<>();
    argsMap.put("event", null);
    argsMap.put("events", null);
    argsMap.put("from", null);
    argsMap.put("to", null);
    argsMap.put("with", null);
    return argsMap;
  }
}
