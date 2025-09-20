package controller;

import java.util.List;

import model.ICalendar;
import view.ICalendarView;

/**
 * This class is a generic class to represent two functionalities ie edit event and edit calendar.
 * This class delegates the functionality to the {@code EditCalendarCommand} class if it encounters
 * the "calendar" keyword and {@code EditEventCommand} class if "event" keyword is encountered.
 */
public class EditCommand extends Command {

  private final CalendarData calendarData;

  EditCommand(CalendarData calendarData) {
    this.calendarData = calendarData;
  }

  /**
   * This method implements the logic to delegate functionalities to the respective classes.
   * @param calendar     the calendar instance on which the command operates
   * @param calendarView the view component for displaying calendar-related information
   * @param tokens       a list of string tokens representing parsed command input
   */
  @Override
  void execute(ICalendar calendar, ICalendarView calendarView, List<String> tokens) {
    if (tokens.get(1).equals("calendar")) {
      Command editCalendarCommand = new EditCalendarCommand(this.calendarData);
      editCalendarCommand.execute(calendar, calendarView, tokens);
    }
    else {
      Command editCalendarCommand = new EditEventCommand();
      editCalendarCommand.execute(calendar, calendarView, tokens);
    }
  }
}
