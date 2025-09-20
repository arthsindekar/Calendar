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
 * Covers all the test cases for copy event commands.
 */
public class CopyEventCommandTest {
  ICalendarController controller;
  ICalendar calendar;
  ICalendarView calendarView;
  String initCalendar;

  @Before
  public void setUp() {
    initCalendar = "create calendar --name Cal1 --timezone America/New_York\n" +
            "create calendar --name Cal2 --timezone America/New_York\n" +
            "create calendar --name Cal3 --timezone Asia/Kolkata\n" +
            "use calendar --name Cal1\n" +
            "create event Meeting1 from 2025-03-05T02:30 to 2025-03-05T03:30\n";
  }

  @Test
  public void testCalendarControllerCopyEventWrongCommandArgumentName() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "copy event Meeting1 from 2025-03-05T02:30" +
            " --target Cal3 to 2025-03-07T02:30");
    StringBuilder log = new StringBuilder();
    String uniqueCode = "•Subject:Lecture 1 from 2025-03-05 02:30 to 2025-03-05 03:30\n";
    calendar = new MockCalendar(log, uniqueCode);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Invalid command argument: from";
    String expectedLog = "Subject: Meeting1 startDateTime: 2025-03-05T02:30 endDateTime:" +
            " 2025-03-05T03:30 description: null location: null isPrivate: null autoDecline: true";
    assertEquals(expectedLog, log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[4]);
  }

  @Test
  public void testCalendarControllerCopyEventNoArguments() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "copy");
    StringBuilder log = new StringBuilder();
    String uniqueCode = "•Subject:Lecture 1 from 2025-03-05 02:30 to 2025-03-05 03:30\n";
    calendar = new MockCalendar(log, uniqueCode);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Incomplete command no arguments specified";
    String expectedLog = "Subject: Meeting1 startDateTime: 2025-03-05T02:30 endDateTime:" +
            " 2025-03-05T03:30 description: null location: null isPrivate: null autoDecline: true";
    assertEquals(expectedLog, log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[4]);
  }

  @Test
  public void testCalendarControllerCopyEventNameNotMentioned() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "copy on 2025-03-05T02:30" +
            " --target Cal3 to 2025-03-07T02:30");
    StringBuilder log = new StringBuilder();
    String uniqueCode = "•Subject:Lecture 1 from 2025-03-05 02:30 to 2025-03-05 03:30\n";
    calendar = new MockCalendar(log, uniqueCode);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Keyword event or events expected but got on";
    String expectedLog = "Subject: Meeting1 startDateTime: 2025-03-05T02:30 endDateTime:" +
            " 2025-03-05T03:30 description: null location: null isPrivate: null autoDecline: true";
    assertEquals(expectedLog, log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[4]);
  }

  @Test
  public void testCalendarControllerCopyEventIncompleteArgumentValue() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "copy event Meeting1 on 2025-03-05T02:30" +
            " --target Cal3 to");
    StringBuilder log = new StringBuilder();
    String uniqueCode = "•Subject:Lecture 1 from 2025-03-05 02:30 to 2025-03-05 03:30\n";
    calendar = new MockCalendar(log, uniqueCode);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Incomplete command: argument to values not mentioned";
    String expectedLog = "Subject: Meeting1 startDateTime: 2025-03-05T02:30 endDateTime:" +
            " 2025-03-05T03:30 description: null location: null isPrivate: null autoDecline: true";
    assertEquals(expectedLog, log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[4]);
  }

  @Test
  public void testCalendarControllerCopyEventRepeatedArgument() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "copy event Meeting1 on 2025-03-05T02:30" +
            " --target Cal3 to 2025-03-07T02:30 --target Cal2");
    StringBuilder log = new StringBuilder();
    String uniqueCode = "•Subject:Lecture 1 from 2025-03-05 02:30 to 2025-03-05 03:30\n";
    calendar = new MockCalendar(log, uniqueCode);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Repeated command --target";
    String expectedLog = "Subject: Meeting1 startDateTime: 2025-03-05T02:30 endDateTime:" +
            " 2025-03-05T03:30 description: null location: null isPrivate: null autoDecline: true";
    assertEquals(expectedLog, log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[4]);
  }

  @Test
  public void testCalendarControllerCopyEventOnlyDateMentionedForSingleEvent() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "copy event Meeting1 on 2025-03-05" +
            " --target Cal3 to 2025-03-07T02:30");
    StringBuilder log = new StringBuilder();
    String uniqueCode = "•Subject:Lecture 1 from 2025-03-05 02:30 to 2025-03-05 03:30\n";
    calendar = new MockCalendar(log, uniqueCode);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Please mention dateTime string for on argument";
    String expectedLog = "Subject: Meeting1 startDateTime: 2025-03-05T02:30 endDateTime:" +
            " 2025-03-05T03:30 description: null location: null isPrivate: null autoDecline: true";
    assertEquals(expectedLog, log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[4]);
  }

  @Test
  public void testCalendarControllerCopyEventOnBetweenAndUsed() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "copy event Meeting1 on 2025-03-05T02:30" +
            " between 2025-03-05 and 2025-03-06 --target Cal3 to 2025-03-07T02:30");
    StringBuilder log = new StringBuilder();
    String uniqueCode = "•Subject:Lecture 1 from 2025-03-05 02:30 to 2025-03-05 03:30\n";
    calendar = new MockCalendar(log, uniqueCode);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Invalid arguments: use either 'between' and 'and' or 'on' keyword";
    String expectedLog = "Subject: Meeting1 startDateTime: 2025-03-05T02:30 endDateTime:" +
            " 2025-03-05T03:30 description: null location: null isPrivate: null autoDecline: true";
    assertEquals(expectedLog, log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[4]);
  }

  @Test
  public void testCalendarControllerCopyEventsDateTimeMentioned() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "copy events on 2025-03-05T02:30" +
            " --target Cal3 to 2025-03-07T02:30");
    StringBuilder log = new StringBuilder();
    String uniqueCode = "•Subject:Lecture 1 from 2025-03-05 02:30 to 2025-03-05 03:30\n";
    calendar = new MockCalendar(log, uniqueCode);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Please mention only date for multiple event copy";
    String expectedLog = "Subject: Meeting1 startDateTime: 2025-03-05T02:30 endDateTime:" +
            " 2025-03-05T03:30 description: null location: null isPrivate: null autoDecline: true";
    assertEquals(expectedLog, log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[4]);
  }

  @Test
  public void testCalendarControllerCopyEventsWrongDateFormat() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "copy events on 2025March05" +
            " --target Cal3 to 2025-03-07T02:30");
    StringBuilder log = new StringBuilder();
    String uniqueCode = "•Subject:Lecture 1 from 2025-03-05 02:30 to 2025-03-05 03:30\n";
    calendar = new MockCalendar(log, uniqueCode);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Invalid argument 2025March05";
    String expectedLog = "Subject: Meeting1 startDateTime: 2025-03-05T02:30 endDateTime:" +
            " 2025-03-05T03:30 description: null location: null isPrivate: null autoDecline: true";
    assertEquals(expectedLog, log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[4]);
  }

  @Test
  public void testCalendarControllerCopyEventsWrongBetweenDateFormat() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "copy events between 2025March04" +
            " and 2025-03-06 --target Cal3 to 2025-03-07T02:30");
    StringBuilder log = new StringBuilder();
    String uniqueCode = "•Subject:Lecture 1 from 2025-03-05 02:30 to 2025-03-05 03:30\n";
    calendar = new MockCalendar(log, uniqueCode);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Invalid argument 2025March04";
    String expectedLog = "Subject: Meeting1 startDateTime: 2025-03-05T02:30 endDateTime:" +
            " 2025-03-05T03:30 description: null location: null isPrivate: null autoDecline: true";
    assertEquals(expectedLog, log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[4]);
  }

  @Test
  public void testCalendarControllerCopyEventsWrongAndDateFormat() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "copy events between 2025-03-04" +
            " and 2025March06 --target Cal3 to 2025-03-07T02:30");
    StringBuilder log = new StringBuilder();
    String uniqueCode = "•Subject:Lecture 1 from 2025-03-05 02:30 to 2025-03-05 03:30\n";
    calendar = new MockCalendar(log, uniqueCode);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Invalid argument 2025March06";
    String expectedLog = "Subject: Meeting1 startDateTime: 2025-03-05T02:30 endDateTime:" +
            " 2025-03-05T03:30 description: null location: null isPrivate: null autoDecline: true";
    assertEquals(expectedLog, log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[4]);
  }

  @Test
  public void testCalendarControllerCopyEventsTargetDateTimeProvided() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "copy events between 2025-03-04" +
            " and 2025-03-06 --target Cal3 to 2025-03-07T02:30");
    StringBuilder log = new StringBuilder();
    String uniqueCode = "•Subject:Lecture 1 from 2025-03-05 02:30 to 2025-03-05 03:30\n";
    calendar = new MockCalendar(log, uniqueCode);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Please mention only target date for multiple event copy";
    String expectedLog = "Subject: Meeting1 startDateTime: 2025-03-05T02:30 endDateTime:" +
            " 2025-03-05T03:30 description: null location: null isPrivate: null autoDecline: true";
    assertEquals(expectedLog, log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[4]);
  }

  @Test
  public void testCalendarControllerCopyEventsWrongTargetDateFormat() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "copy events between 2025-03-04" +
            " and 2025-03-06 --target Cal3 to 2025March07");
    StringBuilder log = new StringBuilder();
    String uniqueCode = "•Subject:Lecture 1 from 2025-03-05 02:30 to 2025-03-05 03:30\n";
    calendar = new MockCalendar(log, uniqueCode);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Invalid argument 'to' value 2025March07";
    String expectedLog = "Subject: Meeting1 startDateTime: 2025-03-05T02:30 endDateTime:" +
            " 2025-03-05T03:30 description: null location: null isPrivate: null autoDecline: true";
    assertEquals(expectedLog, log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[4]);
  }

  @Test
  public void testCalendarControllerCopyEventWrongTargetDateTimeFormat() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "copy event Meeting1 on 2025-03-05T02:30" +
            " --target Cal3 to 2025-03-07");
    StringBuilder log = new StringBuilder();
    String uniqueCode = "•Subject:Lecture 1 from 2025-03-05 02:30 to 2025-03-05 03:30\n";
    calendar = new MockCalendar(log, uniqueCode);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Invalid dateTime string provided 2025-03-07";
    String expectedLog = "Subject: Meeting1 startDateTime: 2025-03-05T02:30 endDateTime:" +
            " 2025-03-05T03:30 description: null location: null isPrivate: null autoDecline: true";
    assertEquals(expectedLog, log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[4]);
  }

  @Test
  public void testCalendarControllerCopyEventWrongTargetCalendarName() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "copy event Meeting1 on 2025-03-05T02:30" +
            " --target Cal4 to 2025-03-07T02:30");
    StringBuilder log = new StringBuilder();
    String uniqueCode = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\"," +
            "\"\",\"\",\"False\"\n";
    calendar = new MockCalendar(log, uniqueCode);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Target calendar not found";
    String expectedLog = "Subject: Meeting1 startDateTime: 2025-03-05T02:30" +
            " endDateTime: 2025-03-05T03:30 description: null location: null" +
            " isPrivate: null autoDecline: true" +
            "startDateTime: 2025-03-05T02:30 endDateTime: +999999999-12-31T23:59:59.999999999";
    assertEquals(expectedLog, log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[4]);
  }

  @Test
  public void testCalendarControllerCopyEventWithoutUsingCalendar() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader("copy event Meeting1 on 2025-03-05T02:30" +
            " --target Cal3 to 2025-03-07T02:30");
    StringBuilder log = new StringBuilder();
    String uniqueCode = "Subject:Meeting1 from 2025-03-05 02:30 to 2025-03-05 03:30\n" +
            "Subject:Meeting1 from 2025-03-05 03:30 to 2025-03-05 04:30\n" +
            "Subject:Meeting2 from 2025-03-05 01:30 to 2025-03-05 02:30\n";
    calendar = new MockCalendar(log, uniqueCode);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Please use a calendar first.";
    String expectedLog = "";
    assertEquals(expectedLog, log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[0]);
  }

  @Test
  public void testCalendarControllerCopyEventToAStartDateTime() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "copy event Meeting1 on 2025-03-05T02:30" +
            " --target Cal3 to 2025-03-07T02:30");
    StringBuilder log = new StringBuilder();
    String uniqueCode = "\"Meeting1\",\"2025-03-05\",\"02:30\",\"2025-03-05\",\"03:30\",\"\"," +
            "\"\",\"\",\"False\"\n" +
            "\"Meeting1\",\"2025-03-05\",\"03:30\",\"2025-03-05\",\"04:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Meeting2\",\"2025-03-05\",\"01:30\",\"2025-03-05\",\"02:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    calendar = new MockCalendar(log, uniqueCode);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Event(s) copied successfully";
    String expectedLog = "Subject: Meeting1 startDateTime: 2025-03-05T02:30" +
            " endDateTime: 2025-03-05T03:30 description: null location: null" +
            " isPrivate: null autoDecline: true" +
            "startDateTime: 2025-03-05T02:30 endDateTime: +999999999-12-31T23:59:59.999999999" +
            "Subject: Meeting1 startDateTime: 2025-03-07T02:30 endDateTime: 2025-03-07T03:30" +
            " description: null location: null isPrivate: null autoDecline: true";
    assertEquals(expectedLog, log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[4]);
  }

  @Test
  public void testCalendarControllerCopyEventToAStartDateTimePrivate() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "copy event Meeting1 on 2025-03-05T02:30" +
            " --target Cal3 to 2025-03-07T02:30");
    StringBuilder log = new StringBuilder();
    String uniqueCode = "\"Meeting1\",\"2025-03-05\",\"02:30\",\"2025-03-05\",\"03:30\",\"\"," +
            "\"\",\"True\",\"False\"\n" +
            "\"Meeting1\",\"2025-03-05\",\"03:30\",\"2025-03-05\",\"04:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Meeting2\",\"2025-03-05\",\"01:30\",\"2025-03-05\",\"02:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    calendar = new MockCalendar(log, uniqueCode);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Event(s) copied successfully";
    String expectedLog = "Subject: Meeting1 startDateTime: 2025-03-05T02:30" +
            " endDateTime: 2025-03-05T03:30 description: null location: null" +
            " isPrivate: null autoDecline: true" +
            "startDateTime: 2025-03-05T02:30 endDateTime: +999999999-12-31T23:59:59.999999999" +
            "Subject: Meeting1 startDateTime: 2025-03-07T02:30 endDateTime: 2025-03-07T03:30" +
            " description: null location: null isPrivate: true autoDecline: true";
    assertEquals(expectedLog, log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[4]);
  }

  @Test
  public void testCalendarControllerCopyEventToAStartDateTimePublic() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "copy event Meeting1 on 2025-03-05T02:30" +
            " --target Cal3 to 2025-03-07T02:30");
    StringBuilder log = new StringBuilder();
    String uniqueCode = "\"Meeting1\",\"2025-03-05\",\"02:30\",\"2025-03-05\",\"03:30\",\"\"," +
            "\"\",\"Public\",\"False\"\n" +
            "\"Meeting1\",\"2025-03-05\",\"03:30\",\"2025-03-05\",\"04:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Meeting2\",\"2025-03-05\",\"01:30\",\"2025-03-05\",\"02:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    calendar = new MockCalendar(log, uniqueCode);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Event(s) copied successfully";
    String expectedLog = "Subject: Meeting1 startDateTime: 2025-03-05T02:30" +
            " endDateTime: 2025-03-05T03:30 description: null location: null" +
            " isPrivate: null autoDecline: true" +
            "startDateTime: 2025-03-05T02:30 endDateTime: +999999999-12-31T23:59:59.999999999" +
            "Subject: Meeting1 startDateTime: 2025-03-07T02:30 endDateTime: 2025-03-07T03:30" +
            " description: null location: null isPrivate: false autoDecline: true";
    assertEquals(expectedLog, log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[4]);
  }

  @Test
  public void testCalendarControllerCopyEventsOnADateDifferentCalendar() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "copy events on 2025-03-05" +
            " --target Cal3 to 2025-03-07");
    StringBuilder log = new StringBuilder();
    String uniqueCode = "\"Meeting1\",\"2025-03-05\",\"02:30\",\"2025-03-05\",\"03:30\",\"\"," +
            "\"\",\"\",\"False\"\n" +
            "\"Meeting1\",\"2025-03-05\",\"03:30\",\"2025-03-05\",\"04:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Meeting2\",\"2025-03-05\",\"01:30\",\"2025-03-05\",\"02:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    calendar = new MockCalendar(log, uniqueCode);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Event(s) copied successfully";
    String expectedLog = "Subject: Meeting1 startDateTime: 2025-03-05T02:30" +
            " endDateTime: 2025-03-05T03:30 description: null location: null" +
            " isPrivate: null autoDecline: true" +
            "startDateTime: 2025-03-05T00:00 endDateTime: 2025-03-05T23:59:59.999999999" +
            "Subject: Meeting1 startDateTime: 2025-03-07T13:00 endDateTime: 2025-03-07T14:00" +
            " description: null location: null isPrivate: null autoDecline: true" +
            "Subject: Meeting1 startDateTime: 2025-03-07T14:00 endDateTime: 2025-03-07T15:00" +
            " description: null location: null isPrivate: null autoDecline: true" +
            "Subject: Meeting2 startDateTime: 2025-03-07T12:00 endDateTime: 2025-03-07T13:00" +
            " description: null location: null isPrivate: null autoDecline: true";
    assertEquals(expectedLog, log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[4]);
  }

  @Test
  public void testCalendarControllerCopyEventsBetweenDatesDifferentCalendar() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "copy events between 2025-03-04" +
            " and 2025-03-06 --target Cal3 to 2025-03-07");
    StringBuilder log = new StringBuilder();
    String uniqueCode = "\"Meeting1\",\"2025-03-05\",\"02:30\",\"2025-03-05\",\"03:30\",\"\"," +
            "\"\",\"\",\"False\"\n" +
            "\"Meeting1\",\"2025-03-05\",\"03:30\",\"2025-03-05\",\"04:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Meeting2\",\"2025-03-05\",\"01:30\",\"2025-03-05\",\"02:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    calendar = new MockCalendar(log, uniqueCode);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Event(s) copied successfully";
    String expectedLog = "Subject: Meeting1 startDateTime: 2025-03-05T02:30 endDateTime: " +
            "2025-03-05T03:30 description: null location: null isPrivate: null autoDecline: " +
            "truestartDateTime: 2025-03-04T00:00 endDateTime: 2025-03-06T23:59:59" +
            ".999999999Subject: Meeting1 startDateTime: 2025-03-08T13:00 endDateTime: " +
            "2025-03-08T14:00 description: null location: null isPrivate: null autoDecline: " +
            "trueSubject: Meeting1 startDateTime: 2025-03-08T14:00 endDateTime: 2025-03-08T15:00 " +
            "description: null location: null isPrivate: null autoDecline: trueSubject: Meeting2 " +
            "startDateTime: 2025-03-08T12:00 endDateTime: 2025-03-08T13:00 description: null " +
            "location: null isPrivate: null autoDecline: true";
    assertEquals(expectedLog, log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[4]);
  }

  @Test
  public void testCalendarControllerCopyEventToAStartDateTimeSameCalendar() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "copy event Meeting1 on 2025-03-05T02:30" +
            " --target Cal1 to 2025-03-07T02:30");
    StringBuilder log = new StringBuilder();
    String uniqueCode = "\"Meeting1\",\"2025-03-05\",\"02:30\",\"2025-03-05\",\"03:30\",\"\"," +
            "\"\",\"\",\"False\"\n" +
            "\"Meeting1\",\"2025-03-05\",\"03:30\",\"2025-03-05\",\"04:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Meeting2\",\"2025-03-05\",\"01:30\",\"2025-03-05\",\"02:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    calendar = new MockCalendar(log, uniqueCode);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Event(s) copied successfully";
    String expectedLog = "Subject: Meeting1 startDateTime: 2025-03-05T02:30" +
            " endDateTime: 2025-03-05T03:30 description: null location: null" +
            " isPrivate: null autoDecline: true" +
            "startDateTime: 2025-03-05T02:30 endDateTime: +999999999-12-31T23:59:59.999999999" +
            "Subject: Meeting1 startDateTime: 2025-03-07T02:30 endDateTime: 2025-03-07T03:30" +
            " description: null location: null isPrivate: null autoDecline: true";
    assertEquals(expectedLog, log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[4]);
  }

  @Test
  public void testCalendarControllerCopyEventsOnADateSameCalendar() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "copy events on 2025-03-05" +
            " --target Cal1 to 2025-03-07");
    StringBuilder log = new StringBuilder();
    String uniqueCode = "\"Meeting1\",\"2025-03-05\",\"02:30\",\"2025-03-05\",\"03:30\",\"\"," +
            "\"\",\"\",\"False\"\n" +
            "\"Meeting1\",\"2025-03-05\",\"03:30\",\"2025-03-05\",\"04:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Meeting2\",\"2025-03-05\",\"01:30\",\"2025-03-05\",\"02:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    calendar = new MockCalendar(log, uniqueCode);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Event(s) copied successfully";
    String expectedLog = "Subject: Meeting1 startDateTime: 2025-03-05T02:30" +
            " endDateTime: 2025-03-05T03:30 description: null location: null" +
            " isPrivate: null autoDecline: true" +
            "startDateTime: 2025-03-05T00:00 endDateTime: 2025-03-05T23:59:59.999999999" +
            "Subject: Meeting1 startDateTime: 2025-03-07T02:30 endDateTime: 2025-03-07T03:30" +
            " description: null location: null isPrivate: null autoDecline: true" +
            "Subject: Meeting1 startDateTime: 2025-03-07T03:30 endDateTime: 2025-03-07T04:30" +
            " description: null location: null isPrivate: null autoDecline: true" +
            "Subject: Meeting2 startDateTime: 2025-03-07T01:30 endDateTime: 2025-03-07T02:30" +
            " description: null location: null isPrivate: null autoDecline: true";
    assertEquals(expectedLog, log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[4]);
  }

  @Test
  public void testCalendarControllerCopyEventsBetweenDatesSameCalendar() {
    StringBuffer out = new StringBuffer();
    Reader in = new StringReader(initCalendar + "copy events between 2025-03-04" +
            " and 2025-03-06 --target Cal1 to 2025-03-07");
    StringBuilder log = new StringBuilder();
    String uniqueCode = "\"Meeting1\",\"2025-03-05\",\"02:30\",\"2025-03-05\",\"03:30\",\"\"," +
            "\"\",\"\",\"False\"\n" +
            "\"Meeting1\",\"2025-03-05\",\"03:30\",\"2025-03-05\",\"04:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Meeting2\",\"2025-03-05\",\"01:30\",\"2025-03-05\",\"02:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    calendar = new MockCalendar(log, uniqueCode);
    calendarView = new MockCalendarView(out);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Event(s) copied successfully";
    String expectedLog = "Subject: Meeting1 startDateTime: 2025-03-05T02:30" +
            " endDateTime: 2025-03-05T03:30 description: null location: null" +
            " isPrivate: null autoDecline: true" +
            "startDateTime: 2025-03-04T00:00 endDateTime: 2025-03-06T23:59:59.999999999" +
            "Subject: Meeting1 startDateTime: 2025-03-08T02:30 endDateTime: 2025-03-08T03:30" +
            " description: null location: null isPrivate: null autoDecline: true" +
            "Subject: Meeting1 startDateTime: 2025-03-08T03:30 endDateTime: 2025-03-08T04:30" +
            " description: null location: null isPrivate: null autoDecline: true" +
            "Subject: Meeting2 startDateTime: 2025-03-08T01:30 endDateTime: 2025-03-08T02:30" +
            " description: null location: null isPrivate: null autoDecline: true";
    assertEquals(expectedLog, log.toString());
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[4]);
  }

}