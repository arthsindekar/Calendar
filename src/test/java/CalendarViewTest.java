import org.junit.Test;

import view.CalendarView;
import view.ICalendarView;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the {@code CalendarView} class.
 * This class contains test cases to verify the correct behavior of the {@code CalendarView}
 * implementation, specifically the correct display of calendar data and output functionality.
 */
public class CalendarViewTest {
  @Test
  public void testOutputResponse() {
    StringBuffer out = new StringBuffer();
    ICalendarView calendarView = new CalendarView(out);
    calendarView.outputResponse("abcdcba");
    assertEquals("abcdcba", out.toString());
  }

}