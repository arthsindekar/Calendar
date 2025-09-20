import org.junit.Before;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;

import controller.CalendarController;
import controller.ICalendarController;
import model.Calendar;
import model.ICalendar;
import view.ICalendarView;

import static org.junit.Assert.assertEquals;

/**
 *  This class tests the create calendar command.
 */
public class CreateCalendarCommandTest {
  StringBuffer out;
  Reader in;
  ICalendarView calendarView;
  ICalendarController controller;
  ICalendar calendar;

  @Before
  public void setUp() {
    calendar  = new Calendar();
    out = new StringBuffer();
  }

  @Test
  public void testCreateCalendarSuccess() {
    in = new StringReader("create calendar --name Cal1 --timezone Asia/Kolkata");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Calendar added successfully with name: Cal1 and timezone: Asia/Kolkata\n"
            , out.toString());
  }

  @Test
  public void testCreateCalendarNamePropertyNotGiven() {
    in = new StringReader("create calendar Cal1 --timezone Asia/Kolkata");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Invalid Arguments.", out.toString());
  }

  @Test
  public void testCreateCalendarTimezonePropertyNotGiven() {
    in = new StringReader("create calendar --name Cal1 Asia/Kolkata");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Invalid Arguments.", out.toString());
  }

  @Test
  public void testCreateCalendarNamePropertyNotGiven2() {
    in = new StringReader("create calendar --timezone Asia/Kolkata Cal1 ");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Invalid Arguments.", out.toString());
  }

  @Test
  public void testCreateCalendarDuplicateCalendarName() {
    in = new StringReader("create calendar --name Cal1 --timezone Asia/Kolkata\n" +
            "create calendar --name Cal1 --timezone Asia/Kolkata");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Calendar added successfully with name: Cal1 and timezone: Asia/Kolkata\n"
            + "Calendar already exists\n", out.toString());
  }

  @Test
  public void testCreateCalendarInvalidTimezone() {
    in = new StringReader("create calendar --name Cal1 --timezone Asia/Delhi");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Invalid Timezone", out.toString());
  }

  @Test
  public void testCreateCalendarDifferentTimezone() {
    in = new StringReader("create calendar --timezone Asia/Kolkata --name Cal1\n" +
            "create calendar --name Cal2 --timezone Etc/GMT+1");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Calendar added successfully with name: Cal1 and timezone: Asia/Kolkata\n"
            + "Calendar added successfully with name: Cal2 and timezone: Etc/GMT+1\n",
            out.toString());
  }

  @Test
  public void testCreateCalendarTimezoneCommandIncorrect() {
    in = new StringReader("create calendar --name Cal1 --timezon Asia/Kolkata");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Invalid property. Expected '--timezone'", out.toString());
  }

  @Test
  public void testCreateCalendarNameCommandIncorrect() {
    in = new StringReader("create calendar --nam Cal1 --timezone Asia/Kolkata");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Invalid property. Expected '--name' or '--timezone'", out.toString());
  }
}