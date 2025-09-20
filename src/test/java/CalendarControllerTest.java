
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
 * Unit tests for the {@code CalendarController} class.
 * This class contains test cases to verify the correct behavior of the methods in
 * {@code ICalendarController}, including input handling, event management, and
 * calendar status checks.
 */
public class CalendarControllerTest {

  ICalendarController controller;
  ICalendar calendar;
  ICalendarView calendarView;
  String initCalendar;

  @Before
  public void setUp() {
    initCalendar = "create calendar --name Cal1 --timezone America/New_York\n" +
            "use calendar --name Cal1\n";
  }
  //Create Events

  @Test
  public void testCalendarControllerCreateEventWithoutUsingCalendar() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader("create event --autoDecline \"Meeting One\" from " +
            "2025-03-05T02:30 to 2025-03-05T03:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Please use a calendar first.", output[0]);
  }

  @Test
  public void testCalendarControllerCreateSingleEvent() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline \"Meeting One\" from " +
            "2025-03-05T02:30 to 2025-03-05T03:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Subject: Meeting One startDateTime: 2025-03-05T02:30 endDateTime: " +
            "2025-03-05T03:30 description: null location: null isPrivate: null autoDecline: true";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Single Event created successfully", output[1]);
  }

  @Test
  public void testCalendarControllerNoCommand() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "\n");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Calendar added successfully with name: Cal1 and" +
            " timezone: America/New_York\n";

    assertEquals("", log.toString());
    String outString = out.toString();
    assertEquals(expected, outString);
  }

  @Test
  public void testCalendarControllerExitCommand() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "exit");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Calendar added successfully with name: Cal1 and timezone: America/New_York";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[0]);
  }

  @Test
  public void testCalendarControllerInvalidKeyword() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "delete event Meeting1");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Unknown command: delete";
    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerCreateEventWrongCommandArgumentName() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline Meeting1 from " +
            "2025-03-05T02:30 to 2025-03-05T03:30 with description");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Invalid command argument: with";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerCreateEventNameNotMentioned() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create from 2025-03-05T02:30 to 2025-03-05T03:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Subject: null startDateTime: 2025-03-05T02:30 endDateTime: " +
            "2025-03-05T03:30 description: null location: null isPrivate: null autoDecline: true";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Subject cannot be null", output[1]);
  }

  @Test
  public void testCalendarControllerCreateEventRepeatedArgumentAutoDecline() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline Meeting1 from " +
            "2025-03-05T02:30 to 2025-03-05T03:30 --autoDecline");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Repeated command --autoDecline";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerCreateEventRepeatedAutoDecline2() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create --autoDecline event --autoDecline" +
            " Meeting1 from 2025-03-05T02:30 to 2025-03-05T03:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Repeated command --autoDecline";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerCreateEventAutoDeclineAtTheEnd() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event Meeting1 from " +
            "2025-03-05T02:30 to 2025-03-05T03:30 --autoDecline");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Subject: Meeting1 startDateTime: 2025-03-05T02:30 endDateTime: " +
            "2025-03-05T03:30 description: null location: null isPrivate: null autoDecline: true";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Single Event created successfully", output[1]);
  }

  @Test
  public void testCalendarControllerCreateEventAutoDeclineInBetween() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event Meeting1 from " +
            "2025-03-05T02:30 --autoDecline to 2025-03-05T03:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Subject: Meeting1 startDateTime: 2025-03-05T02:30 endDateTime: " +
            "2025-03-05T03:30 description: null location: null isPrivate: null autoDecline: true";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Single Event created successfully", output[1]);
  }

  @Test
  public void testCalendarControllerCreateEventAutoDeclineInPlaceOfArgumentValue() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event Meeting1 from --autoDecline" +
            "2025-03-05T02:30 to 2025-03-05T03:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Subject: Meeting1 startDateTime: null endDateTime: " +
            "2025-03-05T03:30 description: null location: null isPrivate: null autoDecline: true";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Single Event created successfully", output[1]);
  }

  @Test
  public void testCalendarControllerCreateEventDifferentArgumentOrder() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event Meeting1 to 2025-03-05T03:30 " +
            "from 2025-03-05T02:30 --autoDecline");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Subject: Meeting1 startDateTime: 2025-03-05T02:30 endDateTime: " +
            "2025-03-05T03:30 description: null location: null isPrivate: null autoDecline: true";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Single Event created successfully", output[1]);
  }

  @Test
  public void testCalendarControllerCreateEventIncompleteCommandNoArgumentsAfterEvent() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Incomplete command: argument event values not mentioned";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerCreateEventIncompleteCommandNoArgumentsAfterEvent2() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Incomplete command: argument event values not mentioned";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerCreateEventRepeatedEventArgumentName() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline Meeting1 event" +
            " Meeting2 from 2025-03-05T02:30 to 2025-03-05T03:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Repeated command event";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerCreateEventRepeatedEventArgumentName2() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event Meeting1 event --autoDecline" +
            " Meeting2 from 2025-03-05T02:30 to 2025-03-05T03:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Repeated command event";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerCreateEventRepeatedFromArgumentName() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline Meeting1 from " +
            "2025-03-05T02:30 from 2025-03-05T03:00 to 2025-03-05T03:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Repeated command from";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerCreateEventIncompleteNoArgumentValue() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline Meeting1 from " +
            "2025-03-05T02:30 to");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Incomplete command: argument to values not mentioned";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerCreateEventIncompleteNoForArgumentValue() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline Meeting1 from" +
            " 2025-03-05T02:30 to 2025-03-05T03:30 repeats MSF for");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Incomplete command: argument for values not mentioned";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerCreateEventIncompleteNoForTimesUsed() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline Meeting1 from " +
            "2025-03-05T02:30 to 2025-03-05T03:30 repeats MSF for 8");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Incomplete command: argument for values not mentioned";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerCreateEventMissingTimesKeywordInForArgument() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline Meeting1 from" +
            " 2025-03-05T02:30 to 2025-03-05T03:30 for 8 repeats MSF");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "times keyword missing after occurrences";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerCreateRecurringEventWithOccurrences() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline Meeting1 from" +
            " 2025-03-05T02:30 to 2025-03-05T03:30 for 8 times repeats MSF");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Subject: Meeting1 startDateTime: 2025-03-05T02:30" +
            " endDateTime: 2025-03-05T03:30 description: null location: null isPrivate: null" +
            " daysOfWeek: MSF occurrences: 8";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Recurring Event created successfully", output[1]);
  }

  @Test
  public void testCalendarControllerCreateRecurringEventWithStringOccurrences() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline Meeting1 from" +
            " 2025-03-05T02:30 to 2025-03-05T03:30 for nine times repeats MSF");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Subject: Meeting1 startDateTime: 2025-03-05T02:30" +
            " endDateTime: 2025-03-05T03:30 description: null location: null isPrivate: null" +
            " daysOfWeek: MSF occurrences: 0";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Recurring Event created successfully", output[1]);
  }

  @Test
  public void testCalendarControllerCreateEventFromToAndOnUsed() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline Meeting1 from" +
            " 2025-03-05T02:30 to 2025-03-05T03:30 on 2025-03-05T02:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Invalid arguments: use either from and to or on keyword";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerCreateSingleAllDayEvent() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline Meeting1" +
            " on 2025-03-05T02:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Subject: Meeting1 startDateTime: 2025-03-05T00:00" +
            " endDateTime: 2025-03-05T23:59:59.999999999 description: null" +
            " location: null isPrivate: null autoDecline: true";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Single Event created successfully", output[1]);
  }

  @Test
  public void testCalendarControllerCreateSingleAllDayEventOnDate() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline" +
            " Meeting1 on 2025-03-05");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Subject: Meeting1 startDateTime: 2025-03-05T00:00" +
            " endDateTime: 2025-03-05T23:59:59.999999999 description: null" +
            " location: null isPrivate: null autoDecline: true";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Single Event created successfully", output[1]);
  }

  @Test
  public void testCalendarControllerCreateEventWrongToArgument() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline" +
            " Meeting1 from 2025-03-05T02:30 to 2025-03-05T03-30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Not able to parse endDateTime";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerCreateEventWrongOnArgument() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline Meeting1" +
            " on 2025-03-05T02-30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Subject: Meeting1 startDateTime: null endDateTime: " +
            "null description: null location: null isPrivate: null autoDecline: true";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Single Event created successfully", output[1]);
  }

  @Test
  public void testCalendarControllerCreateEventWithoutRepeatsWeekdaysForMentioned() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline Meeting1 from" +
            " 2025-03-05T03:00 to 2025-03-05T03:30 for 8 times");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Invalid arguments for/until used without mentioning weekdays" +
            "";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerCreateEventWithoutRepeatsWeekdaysUntilMentioned() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline Meeting1 from" +
            " 2025-03-05T03:00 to 2025-03-05T03:30 until 2025-03-05");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Invalid arguments for/until used without mentioning weekdays" +
            "";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerCreateRecurringEventWithSpecificEndDateTime() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline Meeting1 from" +
            " 2025-03-05T03:00 to 2025-03-05T03:30 repeats MSF until 2025-03-05T03:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Subject: Meeting1 startDateTime: 2025-03-05T03:00 endDateTime:" +
            " 2025-03-05T03:30 description: null location: null isPrivate: null daysOfWeek: MSF" +
            " specificEndDateTime: 2025-03-05T03:30";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Recurring event created successfully", output[1]);
  }

  @Test
  public void testCalendarControllerCreateRecurringEventWithWrongSpecificEndDate() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline Meeting1 from" +
            " 2025-03-05T03:00 to 2025-03-05T03:30 repeats MSF until 2025-03-05T03-30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Subject: Meeting1 startDateTime: 2025-03-05T03:00 endDateTime:" +
            " 2025-03-05T03:30 description: null location: null isPrivate: null daysOfWeek: MSF" +
            " specificEndDateTime: null";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Recurring event created successfully", output[1]);
  }

  @Test
  public void testCalendarControllerCreateRecurringEventWithSpecificEndDate() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline Meeting1 from" +
            " 2025-03-05T03:00 to 2025-03-05T03:30 repeats MSF until 2025-03-05");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Subject: Meeting1 startDateTime: 2025-03-05T03:00 endDateTime:" +
            " 2025-03-05T03:30 description: null location: null isPrivate: null daysOfWeek: MSF" +
            " specificEndDateTime: 2025-03-05T23:59:59.999999999";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Recurring event created successfully", output[1]);
  }

  @Test
  public void testCalendarControllerCreateRecurringEventBothOccurrencesAndSpecificEndDateTime() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline Meeting1 from" +
            " 2025-03-05T03:00 to 2025-03-05T03:30 repeats MSF for 8 times until 2025-03-05");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Invalid arguments: mention either 'for' or 'until'";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerCreateRecurringEventNoSubjectWithOccurrences() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create from" +
            " 2025-03-05T03:00 to 2025-03-05T03:30 repeats MSF for 8 times");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Subject: null startDateTime: 2025-03-05T03:00 endDateTime: " +
            "2025-03-05T03:30 description: null location: null isPrivate: null" +
            " daysOfWeek: MSF occurrences: 8";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Subject cannot be null", output[1]);
  }

  @Test
  public void testCalendarControllerCreateRecurringEventNoSubjectWithSpecificEndDateTime() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create from" +
            " 2025-03-05T03:00 to 2025-03-05T03:30 repeats MSF until 2025-03-05");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Subject: null startDateTime: 2025-03-05T03:00 endDateTime: " +
            "2025-03-05T03:30 description: null location: null isPrivate: null" +
            " daysOfWeek: MSF specificEndDateTime: 2025-03-05T23:59:59.999999999";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Subject cannot be null", output[1]);
  }

  //Edit Events
  @Test
  public void testCalendarControllerEditEventWrongCommandArgumentName() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "edit events description Meeting1 from " +
            "2025-03-05T02:30 until 2025-03-08 with \"First Meeting\"");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Invalid command argument: until";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerEditEventAndEditEventsInSameCommand() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "edit events description Meeting1 " +
            "event description Meeting2 from " +
            "2025-03-05T02:30 with \"First Meeting\"");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Repeated command event";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerEditEventIncompleteArguments() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "edit events description");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Incomplete command: argument events values not mentioned";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerEditEventNoPropertyAndSubject() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "edit from 2025-03-05T02:30 to" +
            " 2025-03-05T03:30 with Meeting");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Invalid Command: provide either event or events" +
            " keyword with property and event name";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerEditEventIncompleteFromArgumentValue() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "edit events description Meeting1 from" +
            " 2025-03-05T02:30 with");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Incomplete command: argument with values not mentioned";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerEditEventRepeatedWithArgumentValue() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "edit events description Meeting1 with" +
            " \"First Meeting\" from 2025-03-05T02:30 with CS5010");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Repeated command with";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerEditEventWithArgumentMissing() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "edit events description Meeting1" +
            " from 2025-03-05T02:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Command argument with is missing";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerEditRecurringEventsToArgumentMentioned() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "edit events description Meeting1" +
            " from 2025-03-05T02:30 to 2025-03-05T03:30 with \"First Meeting\"");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Invalid command argument 'to'";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerEditEventsInvalidStartDateTime() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "edit events description Meeting1" +
            " from 2025-03-05T02-30 with \"First Meeting\"");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Subject: Meeting1 startDateTime: null property:" +
            " description newValue: First Meeting";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Edited series of events successfully", output[1]);
  }

  @Test
  public void testCalendarControllerEditEventInvalidStartDateTime() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "edit event description Meeting1" +
            " from 2025-03-05T02-30 to 2025-03-05T03:30 with \"First Meeting\"");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Subject: Meeting1 startDateTime: null endDateTime: 2025-03-05T03:30" +
            " property: description newValue: First Meeting";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("StartDateTime has invalid or null value", output[1]);
  }

  @Test
  public void testCalendarControllerEditEventWithoutUsingCalendar() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader("edit event description Meeting1" +
            " from 2025-03-05T02:30 to 2025-03-05T03:30 with \"First Meeting\"");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Please use a calendar first.", output[0]);
  }

  @Test
  public void testCalendarControllerEditSingleEvent() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "edit event description Meeting1" +
            " from 2025-03-05T02:30 to 2025-03-05T03:30 with \"First Meeting\"");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Subject: Meeting1 startDateTime: 2025-03-05T02:30 endDateTime:" +
            " 2025-03-05T03:30 property: description newValue: First Meeting";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Edited single event successfully", output[1]);
  }

  @Test
  public void testCalendarControllerEditRecurringEventAfterSpecificStartDateTime() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "edit events description Meeting1" +
            " from 2025-03-05T02:30 with \"First Meeting\"");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Subject: Meeting1 startDateTime: 2025-03-05T02:30 property: description" +
            " newValue: First Meeting";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Edited series of events successfully", output[1]);
  }

  @Test
  public void testCalendarControllerEditAllRecurringEvent() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "edit events description Meeting1" +
            " \"First Meeting\"");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Subject: Meeting1 startDateTime: null property: description" +
            " newValue: First Meeting";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
  }

  //Print Events
  @Test
  public void testCalendarControllerPrintEventWrongCommandArgumentName() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "print events until 2025-03-05T02:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Invalid command argument: until";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerPrintEventNoArguments() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "print");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Incomplete command: missing arguments";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerPrintEventNoArguments2() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "print events");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Invalid arguments: use either from and to or on keyword";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerPrintEventWrongSecondKeyword() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "print event from 2025-03-05T02:30" +
            " to 2025-03-06T02:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Invalid command: expected events but got event";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }


  @Test
  public void testCalendarControllerPrintEventsNoValueMentionedForArgument() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "print events from 2025-03-05T02:30 to");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Incomplete command: argument to values not mentioned";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerPrintEventRepeatedAArgument() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "print events from 2025-03-05T02:30 to" +
            " 2025-03-06T02:30 from 2025-03-04T02:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Repeated command from";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerPrintEventBothOnAndFromToUsed() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "print events from 2025-03-05T02:30 to" +
            " 2025-03-06T02:30 on 2025-03-05");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Invalid arguments: use either from and to or on keyword";

    assertEquals("", log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
  }

  @Test
  public void testCalendarControllerPrintEventWithoutUsingCalendar() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader("print events from 2025-03-05T02:30" +
            " to 2025-03-06T02:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log, "abcdcba");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Please use a calendar first.", output[0]);
  }

  @Test
  public void testCalendarControllerPrintEventTimeRange() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "print events from 2025-03-05T02:30" +
            " to 2025-03-06T02:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log, "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\"," +
            "\"11:30\",\"\",\"\",\"\",\"False\"\n");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "startDateTime: 2025-03-05T02:30 endDateTime: 2025-03-06T02:30";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("•Subject:Lecture 1 from 2025-03-06 11:00 to 2025-03-06 11:30", output[1]);
  }

  @Test
  public void testCalendarControllerPrintEventLineBreakInSubject() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "print events from 2025-03-05T02:30" +
            " to 2025-03-06T02:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log, "\"Lecture\n 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\"," +
            "\"11:30\",\"\",\"\",\"\",\"False\"\n");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "startDateTime: 2025-03-05T02:30 endDateTime: 2025-03-06T02:30";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("•Subject:Lecture 1 from 2025-03-06 11:00 to 2025-03-06 11:30",
            output[1] + output[2]);
  }

  @Test
  public void testCalendarControllerPrintEventLineBreakAtStart() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "print events from 2025-03-05T02:30" +
            " to 2025-03-06T02:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log, "\n\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\"," +
            "\"11:30\",\"\",\"\",\"\",\"False\"\n");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "startDateTime: 2025-03-05T02:30 endDateTime: 2025-03-06T02:30";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("•Subject:Lecture 1 from 2025-03-06 11:00 to 2025-03-06 11:30",
            output[1]);
  }

  @Test
  public void testCalendarControllerPrintEventTimeRangeWrongValues() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "print events from 2025-03-05T02-30" +
            " to 2025-03-06T02-30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log, "abcdcba");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "startDateTime: null endDateTime: null";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("startDateTime and endDateTime invalid or null",
            output[1]);
  }

  @Test
  public void testCalendarControllerPrintEventOnDate() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "print events on 2025-03-05");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log, "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\"," +
            "\"11:30\",\"\",\"\",\"\",\"False\"\n");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "startDateTime: 2025-03-05T00:00 endDateTime: 2025-03-05T23:59:59.999999999";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("•Subject:Lecture 1 from 2025-03-06 11:00 to 2025-03-06 11:30", output[1]);
  }

  @Test
  public void testCalendarControllerPrintEventOnWrongDate() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "print events on 2025-13-05");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log, "abcdcba");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "startDateTime: null endDateTime: null";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("startDateTime and endDateTime invalid or null",
            output[1]);
  }

  @Test
  public void testCalendarControllerPrintEventWrongRange() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "print events from 2025-03-08T02:30" +
            " to 2025-03-06T02:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log, "abcdcba");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "startDateTime: 2025-03-08T02:30 endDateTime: 2025-03-06T02:30";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("startDateTime must be after endDateTime",
            output[1]);
  }

  @Test
  public void testCalendarControllerPrintEventNoEventsFound() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "print events from 2025-04-10T02:30" +
            " to 2025-04-11T02:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log, "");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "startDateTime: 2025-04-10T02:30 endDateTime: 2025-04-11T02:30";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Calendar added successfully with name: Cal1 and timezone: America/New_York\n",
            outString);
  }


  //Export Events

  @Test
  public void testExportEventCommandWithoutUsingCalendar() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader("export cal cal.csv");
    StringBuilder log = new StringBuilder();
    calendarView = new MockCalendarView(out);
    calendar = new MockCalendar(log);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Please use a calendar first.";
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[0]);
    assertEquals("", log.toString());
  }

  @Test
  public void testExportEventCommandSuccess() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "export cal cal.csv");
    StringBuilder log = new StringBuilder();
    calendarView = new MockCalendarView(out);
    calendar = new MockCalendar(log);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Events exported successfully";
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
    assertEquals("eventInfoeventInfo", log.toString());
  }

  @Test
  public void testExportEventCommandFileNameNotEndWithCSV() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "export cal cal.cs");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "File name should end with .csv";
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
    assertEquals("", log.toString());

  }

  @Test
  public void testExportEventCommandExpectedKeywordCal() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "export cla cal.csv");
    StringBuilder log = new StringBuilder();
    boolean export = false;
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Expected keyword \"cal\" but found \"cla\"";
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
    assertEquals("", log.toString());

  }

  @Test
  public void testExportEventCommandIncompleteCommand() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "export");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Incomplete command ";
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
    assertEquals("", log.toString());
  }

  //Show Status

  @Test
  public void showStatusWithoutUsingCalendar() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader("show status on 2025-03-05T02:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals("", log.toString());
    assertEquals("Please use a calendar first.", output[0]);

  }

  @Test
  public void showStatusSuccess() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "show status on 2025-03-05T02:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals("2025-03-05T02:30", log.toString());
    assertEquals("Success", output[1]);

  }

  @Test
  public void showStatusFailIncompleteArgs() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "show");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Incomplete Arguments";
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
    assertEquals("", log.toString());
  }

  @Test
  public void showStatusFailStatusNotEntered() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "show stat on 2025-03-05T02:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Expected status, got : stat";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
    assertEquals("", log.toString());
  }

  @Test
  public void showStatusFailOnNotEntered() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "show status in 2025-03-05T02:30");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Expected argument on, got : in";
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
    assertEquals("", log.toString());
  }

  @Test
  public void showStatusFailDateNotEntered() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "show status on 2025-03-");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Text '2025-03-' could not be parsed at index 8 ";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
    assertEquals("", log.toString());
  }

  @Test
  public void showStatusFailTimeNotEntered() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "show status on 2025-03-05");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Text '2025-03-05' could not be parsed at index 10 ";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[1]);
    assertEquals("", log.toString());
  }

  @Test
  public void testCalendarControllerMultipleCommandsInteractive() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline \"Meeting One\" " +
            "from " +
            "2025-03-05T02:30 to 2025-03-05T03:30\nprint events from 2025-03-05T02:00" +
            " to 2025-03-06T02:00");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log, "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\"," +
            "\"11:30\",\"\",\"\",\"\",\"False\"\n");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Subject: Meeting One startDateTime: 2025-03-05T02:30 endDateTime:" +
            " 2025-03-05T03:30 description: null location: null isPrivate: null autoDecline: true" +
            "startDateTime: 2025-03-05T02:00 endDateTime: 2025-03-06T02:00";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Single Event created successfully", output[1]);
    assertEquals("•Subject:Lecture 1 from 2025-03-06 11:00 to 2025-03-06 11:30", output[2]);
  }

  @Test
  public void testCalendarControllerMultipleCommandsInteractiveFirstFails() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event \"Meeting One\" from " +
            "2025-03-05T02:30 to 2025-03-05T03:30 on 2025-03-05\nprint events from" +
            " 2025-03-05T02:00 to 2025-03-06T02:00");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log, "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\"," +
            "\"11:30\",\"\",\"\",\"\",\"False\"\n");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "startDateTime: 2025-03-05T02:00 endDateTime: 2025-03-06T02:00";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Invalid arguments: use either from and to or on keyword•Subject:Lecture 1 from " +
                    "2025-03-06 11:00 to 2025-03-06 11:30",
            output[1]);
  }

  @Test
  public void testCalendarControllerMultipleCommandsHeadless() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline \"Meeting One\" from " +
            "2025-03-05T02:30 to 2025-03-05T03:30\nprint events from 2025-03-05T02:00" +
            " to 2025-03-06T02:00");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log, "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\"," +
            "\"11:30\",\"\",\"\",\"\",\"False\"\n");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "headless", calendar, calendarView);
    controller.listenInput();
    String expected = "Subject: Meeting One startDateTime: 2025-03-05T02:30 endDateTime:" +
            " 2025-03-05T03:30 description: null location: null isPrivate: null autoDecline: true" +
            "startDateTime: 2025-03-05T02:00 endDateTime: 2025-03-06T02:00";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Single Event created successfully", output[1]);
    assertEquals("•Subject:Lecture 1 from 2025-03-06 11:00 to 2025-03-06 11:30", output[2]);
  }

  @Test
  public void testCalendarControllerMultipleCommandsHeadlessFirstFails() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "create event --autoDecline \"Meeting One\" " +
            "from " +
            "2025-03-05T02:30 to 2025-03-05T03:30 on 2025-03-05\nprint events from" +
            " 2025-03-05T02:00 to 2025-03-06T02:00");
    StringBuilder log = new StringBuilder();
    calendar = new MockCalendar(log, " abcdcba");
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "headless", calendar, calendarView);
    controller.listenInput();
    String expected = "";

    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, log.toString());
    assertEquals("Invalid arguments: use either from and to or on keyword",
            output[1]);
  }

}