package view;


import controller.Features;

/**
 * Implements the {@code ICalendarView} interface, responsible for presenting the
 * calendar data to the user by outputting the response to a specified output medium.
 * This  handles how the calendar system's information is displayed or printed.
 */
public interface ICalendarView {

  /**
   * Outputs the given data to the specified output medium.
   * This method appends the data to the {@code Appendable} output stream.
   * If an {@code IOException} occurs, it is ignored and no further action is taken.
   *
   * @param data the data to be output to the user.
   */
  void outputResponse(String data);


  /**
   * Registers a {@link Features} implementation with the view to enable interaction
   * between the view and the controller.
   *
   * <p>This allows the view to call back into the controller in response to user actions
   * (e.g., button clicks, menu selections).
   *
   * @param features the set of controller callbacks the view can invoke
   */
  void addFeatures(Features features);
}
