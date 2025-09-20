package controller;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


import model.ICalendar;

import view.ICalendarView;

/**
  * The {@code CalendarController} class implements the {@code ICalendarController} interface
  * and serves as the controller in the Model-View-Controller (MVC) pattern for the calendar system.
  * It is responsible for processing user input, interacting with the {@code ICalendar} model to
  * manage events, and updating the {@code ICalendarView} to display the results.
  * */

public class CalendarController implements ICalendarController {

  private final ICalendarView calendarView;
  private final Readable in;
  private final String mode;

  private final Map<String, Function<CalendarData, Command>> commandMap = new HashMap<>();

  private CalendarData calendarData;

  /**
   * Constructs a {@code CalendarController} instance with the provided input source, mode,
   * calendar model, and calendar view.
   * @param in the {@code Readable} input source that provides user commands, such as
   *           a {@code Scanner} for console input or another source
   * @param mode the mode in which the calendar operates (e.g., interactive, batch), influencing
   *             how the controller processes input and interacts with the user
   * @param calendar the {@code ICalendar} model used to store, manage, and manipulate events
   * @param calendarView the {@code ICalendarView} used to output calendar-related information
   *                     to the user
   */
  public CalendarController(Readable in, String mode,
                            ICalendar calendar, ICalendarView calendarView) {
    this.in = in;
    this.mode = mode;
    this.calendarView = calendarView;
    this.calendarData = new CalendarData(new HashSet<>(), calendar);
    commandMap.put("create", CreateCommand::new);
    commandMap.put("edit", EditCommand::new);
    commandMap.put("show",ShowStatusCommand::new);
    commandMap.put("export", ExportEventCommand::new);
    commandMap.put("print", PrintEventCommand::new);
    commandMap.put("use",UseCalendarCommand::new);
    commandMap.put("copy", CopyEventCommand::new);

  }

  private void processInput(String input) throws IllegalArgumentException {
    String regex = "\"([^\"]*)\"|(\\S+)";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(input);

    List<String> tokens = new ArrayList<>();

    while (matcher.find()) {
      if (matcher.group(1) != null) {
        tokens.add(matcher.group(1));
      } else if (matcher.group(2) != null) {
        tokens.add(matcher.group(2));
      }
    }

    String keyword;
    if (!tokens.isEmpty()) {
      keyword = tokens.get(0);
    }
    else {
      return;
    }

    if (keyword.equals("exit")) {
      return;
    }

    Function<CalendarData, Command> commandConstructor = commandMap.get(keyword);
    if (commandConstructor != null) {
      Command command = commandConstructor.apply(calendarData);
      command.execute(calendarData.getCurrentCalendar(), calendarView, tokens);
    } else {
      throw new IllegalArgumentException("Unknown command: " + keyword);
    }

  }



  @Override
  public void listenInput() {
    Scanner scanner = new Scanner(this.in);
    String input = "";
    while (!input.equals("exit") && scanner.hasNextLine()) {
      input = scanner.nextLine();
      try {
        processInput(input);
      } catch (IllegalArgumentException e) {
        calendarView.outputResponse(e.getMessage());
        if (mode.equals("headless")) {
          break;
        }
      }
    }
  }

}
