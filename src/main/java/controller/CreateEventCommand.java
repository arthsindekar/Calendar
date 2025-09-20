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
 * A command that creates a new event in the calendar system if event is conflicting
 * with existing event it is not created regardless of the --autodecline argument value
 * This class extends {@code Command} and implements the logic for adding events
 * based on user input.
 * */
public class CreateEventCommand extends Command {

  private final Map<String, String> args;


  /**
  * Constructs a {@code CreateEventCommand} and initializes the argument keyword map.
  * This constructor sets up the necessary data structures for processing command input.
  */
  CreateEventCommand() {
    this.args = initializeKeywordMap();
  }

  /**
   * Executes the command to create a new event in the calendar system.
   * This method processes the input tokens, validates the event details,
   * and adds the new event to the specified {@code ICalendar} instance.
   * The {@code ICalendarView} is updated to reflect the new event.
   * @param calendar     the {@code ICalendar} instance where the new event will be added.
   * @param calendarView the {@code ICalendarView} instance to update the calendar view with
   *                     the new event.
   * @param tokens       a list of string tokens representing the parsed input for creating the
   *                     event.
   * @throws IllegalArgumentException if the input is invalid or if required event details are
   *                                  missing.
   */
  @Override
  void execute(ICalendar calendar, ICalendarView calendarView, List<String> tokens)
          throws IllegalArgumentException {
    checkNullCurrentCalendar(calendar);
    for (int i = 1; i < tokens.size(); i += 2) {
      String arg = tokens.get(i);
      if (args.containsKey(arg)) {
        if (arg.equals("--autoDecline")) {
          if (args.get("--autoDecline").equals("false")) {
            args.put("--autoDecline", "true");
            i--;
          }
          else {
            throw new IllegalArgumentException("Repeated command --autoDecline");
          }
        }
        else if (arg.equals("event")) {
          if (tokens.size() <= i + 1) {
            throw new IllegalArgumentException("Incomplete command: argument " + arg +
                    " values not mentioned");
          }
          if (tokens.get(i + 1).equals("--autoDecline")) {
            if (args.get("--autoDecline").equals("false")) {
              args.put("--autoDecline", "true");
            } else {
              throw new IllegalArgumentException("Repeated command --autoDecline");
            }
            if (args.get(arg) == null) {
              if (tokens.size() <= i + 2) {
                throw new IllegalArgumentException("Incomplete command: argument " + arg +
                        " values not mentioned");
              }
              args.put(arg, tokens.get(i + 2));
              i++;
            } else {
              throw new IllegalArgumentException("Repeated command " + arg);
            }
          }
          else {
            if (args.get(arg) == null) {
              args.put(arg, tokens.get(i + 1));
            } else {
              throw new IllegalArgumentException("Repeated command " + arg);
            }
          }
        }
        else {
          if (args.get(arg) == null) {
            if (arg.equals("for")) {
              if (tokens.size() <= i + 2) {
                throw new IllegalArgumentException("Incomplete command: argument " + arg +
                        " values not mentioned");
              }
              if (tokens.get(i + 2).equals("times")) {
                args.put(arg, tokens.get(i + 1));
                i++;
              }
              else {
                throw new IllegalArgumentException("times keyword missing after occurrences");
              }
            }
            else {
              if (tokens.size() <= i + 1) {
                throw new IllegalArgumentException("Incomplete command: argument " + arg +
                        " values not mentioned");
              }
              args.put(arg, tokens.get(i + 1));
            }
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

    if (args.get("from") == null && args.get("to") == null && args.get("on") != null) {
      try {
        startDateTime = LocalDateTime.parse(args.get("on"));
        startDateTime = LocalDateTime.of(startDateTime.toLocalDate(), LocalTime.MIN);
        endDateTime = LocalDateTime.of(startDateTime.toLocalDate(), LocalTime.MAX);
      } catch (Exception e) {
        try {
          startDateTime = LocalDateTime.of(LocalDate.parse(args.get("on")),
                  LocalTime.MIN);
          endDateTime = LocalDateTime.of(LocalDate.parse(args.get("on")),
                  LocalTime.MAX);
        } catch (Exception ex) {
          startDateTime = null;
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
        throw new IllegalArgumentException("Not able to parse endDateTime");
      }
    }
    else {
      throw new IllegalArgumentException("Invalid arguments: use either from and to or on keyword");

    }
    if (args.get("repeats") == null) {
      if (args.get("for") == null && args.get("until") == null ) {
        try {
          calendar.createEvent(args.get("event"), startDateTime, endDateTime, null,
                  null, null, true);
          calendarView.outputResponse("Single Event created successfully\n");
        } catch (Exception e) {
          throw new IllegalArgumentException(e.getMessage());
        }
      }
      else {
        throw new IllegalArgumentException("Invalid arguments for/until used without mentioning" +
                " weekdays");
      }
    }
    else {
      if (args.get("for") == null && args.get("until") != null) {
        try {
          LocalDateTime specificEndDateTime;
          try {
            specificEndDateTime = LocalDateTime.parse(args.get("until"));
          } catch (Exception e) {
            try {
              specificEndDateTime = LocalDateTime.of(LocalDate.parse(args.get("until")),
                      LocalTime.MAX);
            } catch (Exception ex) {
              specificEndDateTime = null;
            }
          }
          calendar.createRecurringEventWithSpecificEndDateTime(args.get("event"),
                  startDateTime, endDateTime, null, null, null,
                  args.get("repeats"), specificEndDateTime);
          calendarView.outputResponse("Recurring event created successfully\n");
        } catch (Exception e) {
          throw new IllegalArgumentException(e.getMessage());
        }
      }
      else if (args.get("for") != null && args.get("until") == null) {
        try {
          int occurrences;
          try {
            occurrences = Integer.parseInt(args.get("for"));
          } catch (NumberFormatException e) {
            occurrences = 0;
          }
          calendar.createRecurringEventWithOccurrences(args.get("event"),
                  startDateTime, endDateTime, null, null, null,
                  args.get("repeats"), occurrences);
          calendarView.outputResponse("Recurring Event created successfully\n");
        } catch (Exception e) {
          throw new IllegalArgumentException(e.getMessage());
        }
      }
      else {
        throw new IllegalArgumentException("Invalid arguments: mention either 'for' or 'until'");
      }
    }

  }

  private Map<String, String> initializeKeywordMap() {
    Map<String, String> argsMap = new HashMap<>();
    argsMap.put("event", null);
    argsMap.put("from", null);
    argsMap.put("to", null);
    argsMap.put("repeats", null);
    argsMap.put("for", null);
    argsMap.put("on", null);
    argsMap.put("until", null);
    argsMap.put("--autoDecline", "false");
    return argsMap;
  }
}
