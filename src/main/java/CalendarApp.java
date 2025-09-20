import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import controller.CalendarController;
import controller.GraphicalController;
import controller.ICalendarController;
import model.Calendar;
import model.ICalendar;
import view.CalendarView;
import view.GUICalendarView;
import view.ICalendarView;

/**
 * The {@code CalendarApp} class is the main entry point for the calendar application.
 * It sets up the necessary components for the calendar system, such as the calendar model,
 * view, and controller, and starts the interaction between the user and the system.
 */
public class CalendarApp {

  private static ICalendarController getController(String[] input) {

    List<String> tokens = new ArrayList<>(List.of(input));
    if (tokens.size() == 2 || tokens.size() == 3) {
      if (tokens.get(0).equals("--mode")) {
        if (tokens.get(1).equals("headless")) {
          if (tokens.size() == 3) {
            if (tokens.get(2).endsWith(".txt")) {
              try {
                Reader reader = new FileReader(tokens.get(2));
                ICalendar calendar = new Calendar();
                ICalendarView calendarView = new CalendarView(System.out);
                return new CalendarController(reader, tokens.get(1), calendar, calendarView);
              }
              catch (Exception e) {
                throw new RuntimeException(e.getMessage());
              }
            }
            else {
              throw new IllegalArgumentException("File name should end with '.txt'.");
            }
          }
          else {
            throw new IllegalArgumentException("Please enter file name");
          }
        }
        else if (tokens.get(1).equals("interactive")) {
          if (tokens.size() == 2) {
            Reader inputReader = new InputStreamReader(System.in);
            ICalendar calendar = new Calendar();
            ICalendarView calendarView = new CalendarView(System.out);
            return new CalendarController(inputReader, tokens.get(1), calendar, calendarView);
          }
          else {
            throw new IllegalArgumentException("Unwanted arguments after interactive");
          }
        }
        else {
          throw new IllegalArgumentException("Only two modes are" +
                  " allowed: interactive and headless");
        }
      }
      else {
        throw new IllegalArgumentException(input + " is not a recognized command");
      }
    }
    else {
      throw new IllegalArgumentException("Invalid input: Wrong number of arguments");
    }


  }

  /**
   * The entry point of the calendar application that listens for user input and processes commands.
   * This method reads a single line of input from the console, converts it to lowercase,
   * and then uses the input to determine the appropriate {@code ICalendarController}.
   * The controller's {@code listenInput} method is called to start processing the input.
   */
  public static void main(String[] args) {
    if (args.length == 0) {
      ICalendar calendar = new Calendar();
      calendar.setName("Default");
      calendar.setTimeZone(TimeZone.getDefault());
      ICalendarView guiCalendarView = new GUICalendarView();
      ICalendarController controller = new GraphicalController(calendar, guiCalendarView);
      controller.listenInput();
    } else {
      for (int i = 0; i < args.length; i++) {
        args[i] = args[i].toLowerCase();
      }
      ICalendarController controller = getController(args);
      controller.listenInput();
    }
  }
}
