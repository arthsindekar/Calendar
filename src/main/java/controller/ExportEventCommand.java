package controller;

import java.util.List;


import model.ICalendar;
import view.ICalendarView;

/**
 * A command that exports events from the calendar system.
 * This class extends {@code Command} and implements the logic for exporting
 * event details, typically to an external file or format such as CSV, JSON, etc.
 * The file writing and reading is done inside this controller class.
 */
public class ExportEventCommand extends Command {


  ExportEventCommand(CalendarData calendarData) {
    //No use of calendarData here
  }

  /**
   * Executes the command to export events from the calendar system.
   * This method processes the input tokens, retrieves the relevant events from the
   * {@code ICalendar} instance, and exports them in the specified format.
   * @param calendar     the {@code ICalendar} instance from which events will be exported.
   * @param calendarView the {@code ICalendarView} instance to update the user interface.
   * @param tokens       a list of string tokens representing the parsed input for export
   *                     options.
   * @throws IllegalArgumentException if the input is invalid or if the export fails.
   */
  @Override
  void execute(ICalendar calendar, ICalendarView calendarView, List<String> tokens)
          throws IllegalArgumentException {
    checkNullCurrentCalendar(calendar);
    if (tokens.size() == 3) {
      if (tokens.get(1).equals("cal")) {
        if (tokens.get(2).endsWith(".csv")) {
          try {
            if (calendar.exportEvents() != null) {
              List<List<String>> data = calendar.exportEvents();
              String fileName = tokens.get(2);
              writeFileWithEvents(fileName, data);
              calendarView.outputResponse("Events exported successfully\n");
            }
          } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
          }
        }
        else {
          throw new IllegalArgumentException("File name should end with .csv");
        }
      }
      else {
        throw new IllegalArgumentException("Expected keyword \"cal\" but found \"" +
                tokens.get(1) + "\"");
      }
    }
    else {
      throw new IllegalArgumentException("Incomplete command ");
    }
  }
}
