import org.junit.Before;
import org.junit.Test;

import java.io.Reader;
import java.io.StringReader;


import controller.CalendarController;
import controller.ICalendarController;


import model.ICalendar;
import view.ICalendarView;

import static org.junit.Assert.assertEquals;

/**
 * This class tests the edit calendar command.
 */
public class EditCalendarCommandTest {
  StringBuffer out;
  Reader in;
  ICalendarView calendarView;
  ICalendarController controller;
  ICalendar calendar;
  StringBuilder log;

  @Before
  public void setUp() {
    log = new StringBuilder();
    calendar  = new MockCalendar(log);
    out = new StringBuffer();
  }

  @Test
  public void testEditCalendarNameSuccess() {
    in = new StringReader("create calendar --name Cal1 --timezone Asia/Kolkata\n"
            +
            "edit calendar --name Cal1 --property name Cal2");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Calendar added successfully with name: Cal1 and timezone: Asia/Kolkata\n"
                    +
                    "Edited calendar 'Cal1' with name 'Cal2'\n"
            , out.toString());
  }

  @Test
  public void testEditCalendarNameAlreadyExists() {
    in = new StringReader("create calendar --name Cal1 --timezone Asia/Kolkata\n"
            +
            "edit calendar --name Cal1 --property name Cal1");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Calendar added successfully with name: Cal1 and timezone: Asia/Kolkata\n"
                    +
                    "Calendar already exists."
            , out.toString());


  }

  @Test
  public void testEditCalendarNameDoesNotExist() {
    in = new StringReader("create calendar --name Cal1 --timezone Asia/Kolkata\n"
            +
            "edit calendar --name Cal --property name Cal1");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Calendar added successfully with name: Cal1 and timezone: Asia/Kolkata\n"
                    +
                    "Calendar does not exist."
            , out.toString());

  }

  @Test
  public void testEditCalendarTimezoneDoesNotExist() {
    in = new StringReader("create calendar --name Cal1 --timezone Asia/Kolkata\n"
            +
            "edit calendar --property timezone Asia/Kolkata --name Cal ");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Calendar added successfully with name: Cal1 and timezone: Asia/Kolkata\n"
                    +
                    "Calendar does not exist."
            , out.toString());

  }

  @Test
  public void testEditCalendarTimeZoneSuccess() {
    in = new StringReader("create calendar --name Cal1 --timezone Asia/Kolkata\n"
            +
            "edit calendar --name Cal1 --property timezone UTC");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Calendar added successfully with name: Cal1 and timezone: Asia/Kolkata\n"
            + "Edited calendar 'Cal1' with timezone 'UTC'\n"
            , out.toString());

  }

  @Test
  public void testEditCalendarInvalidTimezone() {
    in = new StringReader("create calendar --name Cal1 --timezone Asia/Kolkata\n"
            +
            "edit calendar --name Cal1 --property timezone 1267");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Calendar added successfully with name: Cal1 and timezone:" +
            " Asia/Kolkata\n" +
            "Invalid Timezone",out.toString());


  }

  @Test
  public void testEditCalendarInvalidProperty() {
    in = new StringReader("create calendar --name Cal1 --timezone Asia/Kolkata\n"
            +
            "edit calendar --name Cal1 --property timezon Asia/Kolkata");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Calendar added successfully with name: Cal1 and timezone: " +
            "Asia/Kolkata\n" +
            "Invalid property. Expected 'name' or 'timezone'.",out.toString());
  }

  @Test
  public void testEditCalendarInvalidCommandExpectedProperty() {
    in = new StringReader("create calendar --name Cal1 --timezone Asia/Kolkata\n"
            +
            "edit calendar --name Cal1 --propert timezone Asia/Kolkata");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Calendar added successfully with name: Cal1 and timezone:" +
            " Asia/Kolkata\n" +
            "Invalid command. Expected '--property'",out.toString());
  }

  @Test
  public void testEditCalendarInvalidCommandExpectedName() {
    in = new StringReader("create calendar --name Cal1 --timezone Asia/Kolkata\n"
            +
            "edit calendar --nam Cal1 --property timezone Asia/Kolkata");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Calendar added successfully with name: Cal1 and timezone: " +
            "Asia/Kolkata\n" +
            "Invalid command. Expected '--name' or '--property'.",out.toString());
  }

  @Test
  public void testEditCalendarSuccessPropertyTimezoneFirst() {
    in = new StringReader("create calendar --name Cal1 --timezone Asia/Kolkata\n"
            +
            "edit calendar --property timezone UTC --name Cal1 ");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Calendar added successfully with name: Cal1 and timezone: " +
            "Asia/Kolkata\n" +
            "Edited calendar 'Cal1' with timezone 'UTC'",out.toString());

  }

  @Test
  public void testEditCalendarPropertyNameFirst() {
    in = new StringReader("create calendar --name Cal1 --timezone Asia/Kolkata\n"
            +
            "edit calendar --property name Cal2 --name Cal1 ");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Calendar added successfully with name: Cal1 and timezone: " +
            "Asia/Kolkata\n" +
            "Edited calendar 'Cal1' with name 'Cal2'",out.toString());


  }

  @Test
  public void testEditCalendarDuplicateName() {
    in = new StringReader("create calendar --name Cal1 --timezone Asia/Kolkata\n"
            +
            "edit calendar --property name Cal1 --name Cal1 ");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Calendar added successfully with name: Cal1 and timezone: " +
            "Asia/Kolkata\n" +
            "Calendar already exists.",out.toString());


  }

  @Test
  public void testEditCalendarNameNotExist() {
    in = new StringReader("create calendar --name Cal1 --timezone Asia/Kolkata\n"
            +
            "edit calendar --property name Cal1 --name Cal2 ");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Calendar added successfully with name: Cal1 and timezone: " +
            "Asia/Kolkata\n" +
            "Calendar does not exist.",out.toString());

  }

  @Test
  public void testEditCalendarInvalidTimezonePropertyFirst() {
    in = new StringReader("create calendar --name Cal1 --timezone Asia/Kolkata\n"
            +
            "edit calendar --property timezone 123 --name Cal1 ");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Calendar added successfully with name: Cal1 and timezone: " +
            "Asia/Kolkata\n" +
            "Invalid Timezone",out.toString());

  }

  @Test
  public void testEditCalendarInvalidPropertyName() {
    in = new StringReader("create calendar --name Cal1 --timezone Asia/Kolkata\n"
            +
            "edit calendar --property nam Cal2 --name Cal1 ");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Calendar added successfully with name: Cal1 and timezone: " +
            "Asia/Kolkata\n" +
            "Invalid property. Expected 'name' or 'timezone'.",out.toString());
  }

  @Test
  public void testEditCalendarInvalidPropertyTimezone() {
    in = new StringReader("create calendar --name Cal1 --timezone Asia/Kolkata\n"
            +
            "edit calendar --property timezon 123 --name Cal1 ");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Calendar added successfully with name: Cal1 and timezone: " +
            "Asia/Kolkata\n" +
            "Invalid property. Expected 'name' or 'timezone'.",out.toString());
  }

  @Test
  public void testEditCalendarInvalidCommandProperty() {
    in = new StringReader("create calendar --name Cal1 --timezone Asia/Kolkata\n"
            +
            "edit calendar --propert timezone 123 --name Cal1 ");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Calendar added successfully with name: Cal1 and timezone:" +
            " Asia/Kolkata\n" +
            "Invalid command. Expected '--name' or '--property'.",out.toString());
  }

  @Test
  public void testEditCalendarInvalidCommandName() {
    in = new StringReader("create calendar --name Cal1 --timezone Asia/Kolkata\n"
            +
            "edit calendar --property timezone 123 --nam Cal1 ");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Calendar added successfully with name: Cal1 and timezone: " +
            "Asia/Kolkata\n" +
            "Invalid command. Expected '--name'.",out.toString());
  }
}
