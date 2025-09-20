import org.junit.Before;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;

import controller.CalendarController;
import controller.ICalendarController;
import model.ICalendar;
import view.CalendarView;
import view.ICalendarView;

import static org.junit.Assert.assertEquals;

/**
 * Class that tests the use calendar command.
 */
public class UseCalendarCommandTest {
  StringBuffer out;
  Reader in;
  ICalendarView calendarView;
  ICalendarController controller;
  ICalendar calendar;
  StringBuilder log;

  @Before
  public void setUp() {
    log = new StringBuilder();
    calendar = new MockCalendar(log);
    out = new StringBuffer();
    calendarView = new CalendarView(out);
  }

  @Test
  public void testUseCalendarCommandSuccess() {
    in = new StringReader("create calendar --name Cal1 --timezone America/New_York\n" +
            "use calendar --name Cal1");
    controller = new CalendarController(in,"interactive",calendar,calendarView);
    controller.listenInput();
    assertEquals("Calendar added successfully with name: Cal1 and timezone: " +
            "America/New_York\n",out.toString());
  }

  @Test
  public void testUseCalendarCommandFailure() {
    in = new StringReader("create calendar --name Cal1 --timezone America/New_York\n" +
            "use calendar -- Cal1");
    controller = new CalendarController(in,"interactive",calendar,calendarView);
    controller.listenInput();
    assertEquals("Calendar added successfully with name: Cal1 and timezone:" +
            " America/New_York\n" +
            "Invalid command. Expected --name",out.toString());
  }

  @Test
  public void testUseCalendarCommandFailure2() {
    in = new StringReader("create calendar --name Cal1 --timezone America/New_York\n" +
            "use cal --name Cal1");
    controller = new CalendarController(in,"interactive",calendar,calendarView);
    controller.listenInput();
    assertEquals("Calendar added successfully with name: Cal1 and timezone: " +
            "America/New_York\n" +
            "Invalid command. Expected calendar.",out.toString());
  }

  @Test
  public void testUseCalendarCalendarNotExist() {
    in = new StringReader("use calendar --name Cal1");
    controller = new CalendarController(in,"interactive",calendar,calendarView);
    controller.listenInput();
    assertEquals("Calendar does not exist.",out.toString());
  }

}