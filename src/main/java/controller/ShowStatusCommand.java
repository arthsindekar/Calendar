package controller;


import java.time.LocalDateTime;

import java.util.List;


import model.ICalendar;
import view.ICalendarView;

/**
 * A command that shows the current status of the calendar system.
 * This class extends {@code Command} and is responsible for displaying
 * the status of the calendar, such as the number of events, active filters,
 * or any other relevant system state.
 * */

public class ShowStatusCommand extends Command {

  ShowStatusCommand(CalendarData calendarData) {
    //No use of calendar data here
  }

  /**
   * Executes the command to show the current status of the calendar system.
   * This method retrieves the necessary status information from the {@code ICalendar}
   * instance and displays it to the user through the {@code ICalendarView}.
   * @param calendar     the {@code ICalendar} instance from which the status is retrieved.
   * @param calendarView the {@code ICalendarView} instance to display the status to the user.
   * @param tokens       a list of string tokens representing optional filters or commands for
   *                     customizing the status view.
   */
  @Override
  void execute(ICalendar calendar, ICalendarView calendarView, List<String> tokens)
          throws IllegalArgumentException {
    checkNullCurrentCalendar(calendar);
    if (tokens.size() == 4) {
      if (tokens.get(1).equals("status")) {
        if (tokens.get(2).equals("on")) {
          LocalDateTime dateTime;
          try {
            dateTime = LocalDateTime.parse(tokens.get(3));
          } catch (Exception e) {
            dateTime = null;
            throw new IllegalArgumentException(e.getMessage() + " ");
          }
          String status = "";
          try {
            status = calendar.showStatus(dateTime) + "\n";
          } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
          }

          calendarView.outputResponse(status);
        }
        else {
          throw new IllegalArgumentException("Expected argument on, got : " + tokens.get(2));
        }
      }
      else {
        throw new IllegalArgumentException("Expected status, got : " + tokens.get(1));
      }
    }

    else {
      throw new IllegalArgumentException("Incomplete Arguments");
    }
  }
}
