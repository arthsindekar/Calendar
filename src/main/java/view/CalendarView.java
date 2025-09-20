package view;


import java.io.IOException;

import controller.Features;

/**
 * Implements the {@code ICalendarView} interface, responsible for presenting the
 * calendar data to the user by outputting the response to a specified output medium.
 * This class handles how the calendar system's information is displayed or printed.
 */
public class CalendarView implements ICalendarView {
  private final Appendable out;

  /**
   * Constructs a {@code CalendarView} instance with the specified output medium.
   * This constructor allows you to specify where the calendar view data will be written to,
   * such as the console or a file.
   *
   * @param out the {@code Appendable} output stream to which the response will be written.
   */
  public CalendarView(Appendable out) {
    this.out = out;
  }


  /**
   * Outputs the given data to the specified output medium.
   * This method appends the data to the {@code Appendable} output stream.
   * If an {@code IOException} occurs, it is ignored and no further action is taken.
   *
   * @param data the data to be output to the user.
   */
  @Override
  public void outputResponse(String data) {
    try {
      this.out.append(data);
    } catch (IOException ignored) {
    }
  }

  @Override
  public void addFeatures(Features features) {
    //No features to be added for Command line view
  }
}
