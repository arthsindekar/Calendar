package controller;

import java.util.List;

import model.ICalendar;
import view.ICalendarView;

/**
 * The {@code UseCalendarCommand} class represents a command to switch to a specified calendar
 * within a calendar management system. It validates user input, checks for calendar availability,
 * and updates the current calendar in the system.
 */
public class UseCalendarCommand extends Command {


  UseCalendarCommand(CalendarData calendarData) {
    super(calendarData);
  }

  /**
   * Executes the command to switch to a specific calendar.
   * The command expects a list of tokens following the format:
   * {@code use calendar --name <calendarName>}.
   *
   * @param calendar      The current calendar instance. This is not used in the implementation.
   * @param calendarView  The view component for displaying calendar-related data.
   * @param tokens        The list of command tokens provided by the user.
   * @throws IllegalArgumentException if the command format is incorrect or the calendar is
   *                                  unavailable.
   */
  @Override
  void execute(ICalendar calendar, ICalendarView calendarView, List<String> tokens)
          throws IllegalArgumentException {
    String calendarName;
    if (tokens.size() == 4) {
      if (tokens.get(1).equals("calendar")) {
        if (tokens.get(2).equals("--name")) {
          calendarName = tokens.get(3);
        }
        else {
          throw new IllegalArgumentException("Invalid command. Expected --name");
        }
      }
      else {
        throw new IllegalArgumentException("Invalid command. Expected calendar.");
      }
    }
    else {
      throw new IllegalArgumentException("Invalid Arguments.");
    }
    useCurrentCalendar(calendarName);
  }

}
