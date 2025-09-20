package controller;

import java.util.List;

import model.ICalendar;
import view.ICalendarView;

/**
 * This class extends the {@code Command} class.
 * A generic class for create command. This class represents a common class for
 * two functionalities which include create event and create calendar.
 * Whenever the keyword "calendar" is encountered this class delegates the functionality to
 * the {@code CreateCalendarCommand} class. When "event" is encountered it delegates it to
 * {@code CreateEventCommand} class.
 */
public class CreateCommand extends Command {

  private final CalendarData calendarData;

  CreateCommand(CalendarData calendarData) {
    this.calendarData = calendarData;
  }

  /**
   * This method delegates the functionality to either to {@code CreateEventCommand} or
   * {@code CreateCalendarCommand} class.
   * @param calendar     the calendar instance on which the command operates
   * @param calendarView the view component for displaying calendar-related information
   * @param tokens       a list of string tokens representing parsed command input
   */
  @Override
  void execute(ICalendar calendar, ICalendarView calendarView, List<String> tokens) {
    if (tokens.get(1).equals("calendar")) {
      Command createCalendarCommand =
              new CreateCalendarCommand(calendarData);
      createCalendarCommand.execute(calendar, calendarView, tokens);
    }
    else {
      Command createEventCommand =
              new CreateEventCommand();
      createEventCommand.execute(calendar, calendarView, tokens);
    }
  }
}
