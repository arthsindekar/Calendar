import java.io.IOException;
import java.time.LocalDate;

import controller.Features;
import view.ICalendarView;

/**
 * A mock implementation of the {@code ICalendarView} interface used for testing purposes.
 * This class simulates the behavior of the {@code ICalendarView} by providing mock
 * implementations of the methods responsible for displaying calendar-related data to the user.
 */
public class MockCalendarView implements ICalendarView {

  private final Appendable out;

  private StringBuilder log;

  public MockCalendarView(Appendable out) {
    this.out = out;
  }

  public MockCalendarView(Appendable out, StringBuilder log) {
    this.out = out;
    this.log = log;
  }

  @Override
  public void outputResponse(String data) {
    try {

      out.append(data);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void addFeatures(Features features) {
    log.append(features.viewEvents(LocalDate.of(2024,12,12)));
  }
}
