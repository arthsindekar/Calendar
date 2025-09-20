package controller;

import java.util.List;

import model.ICalendar;
import view.ICalendarView;

/**
 * This class represents the create calendar command. This class extends the {@code Command} class
 * and implements the logic to create a calendar. This is the controller class which delegates the
 * execution of the command to the app.
 */
public class CreateCalendarCommand extends Command {


  CreateCalendarCommand(CalendarData calendarData) {
    super(calendarData);
  }

  /**
   * This method executes the create calendar command and creates the calendar and stores
   * it in a set of calendars.
   * @param calendar     the calendar instance on which the command operates.
   * @param calendarView the view component for displaying calendar-related information.
   * @param tokens       a list of string tokens representing parsed command input.
   * @throws IllegalArgumentException when 1.Properties are at incorrect position.
   *                                       2.When values of properties are empty.
   *                                       3.Timezone is invalid.
   *                                       4.Calendar name is duplicate.
   *
   */
  @Override
  void execute(ICalendar calendar, ICalendarView calendarView, List<String> tokens)
          throws IllegalArgumentException {
    String calendarName;
    String timezone ;
    if (tokens.size() == 6) {
      if (tokens.get(2).equals("--name")) {
        calendarName = tokens.get(3);
        if (tokens.get(4).equals("--timezone")) {
          timezone = tokens.get(5);
        } else {
          throw new IllegalArgumentException("Invalid property. Expected '--timezone'");
        }
      } else if (tokens.get(2).equals("--timezone")) {
        timezone = tokens.get(3);
        if (tokens.get(4).equals("--name")) {
          calendarName = tokens.get(5);
        } else {
          throw new IllegalArgumentException("Invalid property. Expected '--name'");
        }
      } else {
        throw new IllegalArgumentException("Invalid property. Expected '--name' or '--timezone'");
      }
    }
    else {
      throw new IllegalArgumentException("Invalid Arguments.");
    }
    createNewCalendar(calendarView, timezone, calendarName);
  }
}
