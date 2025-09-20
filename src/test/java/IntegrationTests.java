import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


import controller.CalendarController;
import model.Calendar;
import view.CalendarView;

import static org.junit.Assert.assertEquals;

/**
 * Class that tests the application as a whole.
 */
public class IntegrationTests {
  Calendar calendar;
  CalendarController controller;
  CalendarView calendarView;
  StringBuffer out;
  String initCalendar;
  String initCalendar2;

  @Before
  public void setUp() {
    calendar = new Calendar();
    out = new StringBuffer();
    calendarView = new CalendarView(out);
    initCalendar = "create calendar --name Cal1 --timezone America/New_York\n" +
            "use calendar --name Cal1\n";
    initCalendar2 = "create calendar --name Cal2 --timezone Asia/Kolkata\n" +
            "create event Meeting1 from 2025-03-05T02:30 to 2025-03-05T03:30\n" +
            "create event Meeting2 from 2025-03-06T02:30 to 2025-03-06T03:30 " +
            "repeats MSF for 8 times\n" +
            "use calendar --name Cal2\n" +
            "create event Meeting21 from 2025-04-05T12:30 to 2025-04-05T13:30\n" +
            "use calendar --name Cal1\n";
  }

  @Test
  public void testForHeadlessModeCreateSuccessfulEvent() {

    //Set input
    Reader in = new StringReader(initCalendar + "create event --autoDecline \"Meeting One\" " +
            "from " +
            "2025-03-05T02:30 to 2025-03-05T03:30");

    controller = new CalendarController(in, "headless", calendar, calendarView);
    controller.listenInput();
    String actualOutput = out.toString();
    String[] output = actualOutput.split("\n");
    String expectedOutput = "Single Event created successfully";
    assertEquals(expectedOutput, output[1]);

  }

  @Test
  public void testForHeadlessModeEditEvent() {
    Reader in = new StringReader(initCalendar + "create event --autoDecline \"Meeting One\" from " +
            "2025-03-05T02:30 to 2025-03-05T03:30\n" + "edit event subject \"Meeting One\" " +
            "from 2025-03-05T11:00 to 2025-03-05T11:30  with Lecture2");
    controller = new CalendarController(in, "headless", calendar, calendarView);
    controller.listenInput();
    String actualOutput = out.toString();
    String[] output = actualOutput.split("\n");
    String expectedOutput1 = "Single Event created successfully";
    String expectedOutput2 = "Edited single event successfully";
    assertEquals(expectedOutput1, output[1]);
    assertEquals(expectedOutput2, output[2]);
  }

  @Test
  public void testForHeadlessModePrintEmptyEvent() {
    //Set input
    Reader in = new StringReader(initCalendar + "print events on 2024-05-07");
    controller = new CalendarController(in, "headless", calendar, calendarView);
    controller.listenInput();
    String actualOutput = out.toString();

    String expectedOutput = "Calendar added successfully with name: Cal1 and timezone: " +
            "America/New_York\n";
    assertEquals(expectedOutput, actualOutput);

  }

  @Test
  public void testForHeadlessModePrintSingleEvent() {

    Reader in = new StringReader(initCalendar + "create event --autoDecline \"Meeting One\" from " +
            "2025-03-05T02:30 to 2025-03-05T03:30\n" + "print events on 2025-03-05\n");
    controller = new CalendarController(in, "headless", calendar, calendarView);
    controller.listenInput();
    String actualOutput = out.toString();
    String[] output = actualOutput.split("\n");
    String expectedOutput1 = "Single Event created successfully";
    String expectedOutput2 = "•Subject:Meeting One from 2025-03-05 02:30 to 2025-03-05 03:30";
    assertEquals(expectedOutput1, output[1]);
    assertEquals(expectedOutput2, output[2]);
  }

  @Test
  public void testForHeadlessModeShowStatus() {

    Reader in = new StringReader(initCalendar + "create event --autoDecline \"Meeting One\" from " +
            "2025-03-05T02:30 to 2025-03-05T03:30\n" + "show status  on 2025-03-05T11:13\n");
    controller = new CalendarController(in, "headless", calendar, calendarView);
    controller.listenInput();
    String actualOutput = out.toString();
    String[] output = actualOutput.split("\n");
    String expectedOutput1 = "Single Event created successfully";
    String expectedOutput2 = "Available";
    assertEquals(expectedOutput1, output[1]);
    assertEquals(expectedOutput2, output[2]);
  }

  @Test
  public void testForHeadlessModeExport() {

    Reader in = new StringReader(initCalendar + "create event --autoDecline \"Meeting One\" from " +
            "2025-03-05T02:30 to 2025-03-05T03:30\n" + "export cal caltest.csv");
    controller = new CalendarController(in, "headless", calendar, calendarView);
    controller.listenInput();
    String actualOutput = out.toString();
    String[] output = actualOutput.split("\n");
    String expectedOutput1 = "Single Event created successfully";
    String expectedOutput2 = "Events exported successfully";
    assertEquals(expectedOutput1, output[1]);
    assertEquals(expectedOutput2, output[2]);
  }

  @Test
  public void testForHeadlessError() {
    //2nd command runs successfully
    Reader in1 = new StringReader(initCalendar + "export cal caltest.csv" );
    StringBuffer out2 = new StringBuffer();
    CalendarView view2 = new CalendarView(out2);
    CalendarController controller1 = new CalendarController(in1, "headless", calendar, view2);
    controller1.listenInput();
    String outString2 = out2.toString();
    String[] output2 = outString2.split("\n");
    assertEquals("No events found", output2[1]);

    //program stops after 1st command
    Reader in = new StringReader(initCalendar + "create even --autoDecline \"Meeting One\" from " +
            "2025-03-05T02:30 to 2025-03-05T03:30\n" + "export cal caltest.csv\n");
    controller = new CalendarController(in, "headless", calendar, calendarView);
    controller.listenInput();
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals("Invalid command argument: even",output[1]);

  }

  @Test
  public void testForInteractiveModeCreateSuccessfulEvent() {

    //Set input
    Reader in = new StringReader(initCalendar + "create event --autoDecline \"Meeting One\" from " +
            "2025-03-05T02:30 to 2025-03-05T03:30");

    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String actualOutput = out.toString();
    String[] output = actualOutput.split("\n");
    String expectedOutput = "Single Event created successfully";
    assertEquals(expectedOutput, output[1]);

  }

  @Test
  public void testForInteractiveModeEditEvent() {
    Reader in = new StringReader(initCalendar + "create event --autoDecline \"Meeting One\" from " +
            "2025-03-05T02:30 to 2025-03-05T03:30\n" + "edit event subject \"Meeting One\" " +
            "from 2025-03-05T11:00 to 2025-03-05T11:30  with Lecture2");
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String actualOutput = out.toString();
    String[] output = actualOutput.split("\n");
    String expectedOutput1 = "Single Event created successfully";
    String expectedOutput2 = "Edited single event successfully";
    assertEquals(expectedOutput1, output[1]);
    assertEquals(expectedOutput2, output[2]);
  }

  @Test
  public void testForInteractiveModePrintEmptyEvent() {
    //Set input
    Reader in = new StringReader(initCalendar + "print events on 2024-05-07");
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String actualOutput = out.toString();

    String expectedOutput = "Calendar added successfully with name: Cal1 and timezone: " +
            "America/New_York\n";
    assertEquals(expectedOutput, actualOutput);

  }

  @Test
  public void testForInteractiveModePrintSingleEvent() {

    Reader in = new StringReader(initCalendar + "create event --autoDecline \"Meeting One\" from " +
            "2025-03-05T02:30 to 2025-03-05T03:30\n" + "print events on 2025-03-05\n");
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String actualOutput = out.toString();
    String[] output = actualOutput.split("\n");
    String expectedOutput1 = "Single Event created successfully";
    String expectedOutput2 = "•Subject:Meeting One from 2025-03-05 02:30 to 2025-03-05 03:30";
    assertEquals(expectedOutput1, output[1]);
    assertEquals(expectedOutput2, output[2]);
  }

  @Test
  public void testForInteractiveModeShowStatus() {

    Reader in = new StringReader(initCalendar + "create event --autoDecline \"Meeting One\" from " +
            "2025-03-05T02:30 to 2025-03-05T03:30\n" + "show status  on 2025-03-05T11:13\n");
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String actualOutput = out.toString();
    String[] output = actualOutput.split("\n");
    String expectedOutput1 = "Single Event created successfully";
    String expectedOutput2 = "Available";
    assertEquals(expectedOutput1, output[1]);
    assertEquals(expectedOutput2, output[2]);
  }


  //export events
  @Test
  public void testForInteractiveExportCheckData() {
    String input = initCalendar + "create event --autoDecline \"Meeting One\" from " +
            "2025-03-05T02:30 to 2025-03-05T03:30\n" + "export cal caltest.csv";
    Reader in = new StringReader(input);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String actualOutput = out.toString();
    String[] output = actualOutput.split("\n");
    String expectedOutput1 = "Single Event created successfully";
    String expectedOutput2 = "Events exported successfully";
    String expected = "\"Subject\",\"Start Date\",\"Start Time\",\"End Date\",\"End Time\"," +
            "\"Description\",\"Location\",\"Private\",\"All Day Event\"\n" +
            "\"Meeting One\",\"2025-03-05\",\"02:30\",\"2025-03-05\",\"03:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expectedOutput1, output[1]);
    assertEquals(expectedOutput2, output[2]);
    Path filePath = Paths.get("caltest.csv");
    String actual = "";
    try {
      actual = new String(Files.readAllBytes(filePath));
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
    assertEquals(expected, actual);

  }

  @Test
  public void testForInteractiveExportRecurringCheckData() {
    String input = initCalendar + "create event --autoDecline Meeting1 from" +
            " 2025-03-05T02:30 to 2025-03-05T03:30 for 8 times repeats MSF\n" +
            "export cal caltest.csv";
    Reader in = new StringReader(input);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String actualOutput = out.toString();
    String[] output = actualOutput.split("\n");
    String expectedOutput1 = "Recurring Event created successfully";
    String expectedOutput2 = "Events exported successfully";
    String expected = "\"Subject\",\"Start Date\",\"Start Time\",\"End Date\",\"End Time\"," +
            "\"Description\",\"Location\",\"Private\",\"All Day Event\"\n" +
            "\"Meeting1\",\"2025-03-07\",\"02:30\",\"2025-03-07\",\"03:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Meeting1\",\"2025-03-08\",\"02:30\",\"2025-03-08\",\"03:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Meeting1\",\"2025-03-10\",\"02:30\",\"2025-03-10\",\"03:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Meeting1\",\"2025-03-14\",\"02:30\",\"2025-03-14\",\"03:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Meeting1\",\"2025-03-15\",\"02:30\",\"2025-03-15\",\"03:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Meeting1\",\"2025-03-17\",\"02:30\",\"2025-03-17\",\"03:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Meeting1\",\"2025-03-21\",\"02:30\",\"2025-03-21\",\"03:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Meeting1\",\"2025-03-22\",\"02:30\",\"2025-03-22\",\"03:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expectedOutput1, output[1]);
    assertEquals(expectedOutput2, output[2]);
    Path filePath = Paths.get("caltest.csv");
    String actual = "";
    try {
      actual = new String(Files.readAllBytes(filePath));
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
    assertEquals(expected, actual);


  }

  @Test
  public void testForInteractiveEmptyEvents() {
    String input = initCalendar + "export cal caltest.csv";
    Reader in = new StringReader(input);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String actualOutput = out.toString();
    String[] output = actualOutput.split("\n");
    String expectedOutput2 = "No events found";
    assertEquals(expectedOutput2, output[1]);
  }

  @Test
  public void testForInteractiveExportAllDayEvents() {
    String input = initCalendar + "create event --autoDecline Meeting1 on " +
            "2025-03-05T02:30\n" +
            "export cal caltest.csv";
    Reader in = new StringReader(input);
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String actualOutput = out.toString();
    String[] output = actualOutput.split("\n");
    String expectedOutput1 = "Single Event created successfully";
    String expectedOutput2 = "Events exported successfully";
    assertEquals(expectedOutput1, output[1]);
    assertEquals(expectedOutput2, output[2]);
    Path filePath = Paths.get("caltest.csv");
    String expected = "\"Subject\",\"Start Date\",\"Start Time\",\"End Date\",\"End Time\"," +
            "\"Description\",\"Location\",\"Private\",\"All Day Event\"\n" +
            "\"Meeting1\",\"2025-03-05\",\"00:00\",\"2025-03-05\",\"23:59\",\"\",\"\",\"\"," +
            "\"True\"\n";
    String actual = "";
    try {
      actual = new String(Files.readAllBytes(filePath));
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
    assertEquals(expected, actual);
  }

  //edit calendar
  @Test
  public void testForInteractiveModeEditCalendarTimezone() {
    String in = initCalendar + "create event --autoDecline \"Meeting One\" from " +
            "2025-03-05T02:30 to 2025-03-05T03:30\n" +
            "print events on 2025-03-05\n" +
            "edit calendar --name Cal1 --property timezone Asia/Kolkata\n"
            + "print events on 2025-03-05\n" +
            "edit calendar --name Cal1 --property name Cal2\n" +
            "edit calendar --name Cal2 --property timezone Europe/Berlin\n" +
            "print events on 2025-03-05";
    Reader input = new StringReader(in);
    controller = new CalendarController(input, "interactive", calendar, calendarView);
    controller.listenInput();
    String actualOutput = out.toString();
    assertEquals("Calendar added successfully with name: Cal1 and timezone:" +
            " America/New_York\n" +
            "Single Event created successfully\n" +
            "•Subject:Meeting One from 2025-03-05 02:30 to 2025-03-05 03:30\n" +
            "Edited calendar 'Cal1' with timezone 'Asia/Kolkata'\n•Subject:Meeting One from " +
            "2025-03-05 13:00 to 2025-03-05 14:00\n" +
            "Edited calendar 'Cal1' with name 'Cal2'\nEdited calendar 'Cal2' with timezone '" +
            "Europe/Berlin'\n" +
            "•Subject:Meeting One from 2025-03-05 08:30 to 2025-03-05 09:30\n",actualOutput);

  }

  @Test
  public void testForInteractiveModeEditCalendarName() {
    Reader in = new StringReader("create calendar --name Cal1 --timezone Asia/Kolkata\n" +
            "edit calendar --name Cal1 --property name Cal2\n" +
            "use calendar --name Cal2");
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String actualOutput = out.toString();
    assertEquals("Calendar added successfully with name: Cal1 and timezone: " +
            "Asia/Kolkata\n" +
            "Edited calendar 'Cal1' with name 'Cal2'\n", actualOutput);

  }

  @Test
  public void testForInteractiveModeUseCalendar() {
    Reader in = new StringReader("create calendar --name Cal1 --timezone Asia/Kolkata\n" +
            "use calendar --name Cal2");
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String actualOutput = out.toString();
    assertEquals("Calendar added successfully with name: Cal1 and timezone: " +
            "Asia/Kolkata\n" +
            "Calendar does not exist.", actualOutput);
  }

  @Test
  public void testForInteractiveModeUseCalendarSuccess() {
    Reader in = new StringReader("create calendar --name Cal1 --timezone Asia/Kolkata\n" +
            "use calendar --name Cal1");
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String actualOutput = out.toString();
    assertEquals("Calendar added successfully with name: Cal1 and timezone: " +
            "Asia/Kolkata\n"
            , actualOutput);
  }

  @Test
  public void testForInteractiveIncompleteArgumentsCreate() {
    Reader in = new StringReader("create calendar "
            );
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String actualOutput = out.toString();
    assertEquals("Invalid Arguments."
            , actualOutput);
  }

  @Test
  public void testForInteractiveIncompleteArgumentsEdit() {
    Reader in = new StringReader("edit calendar "
    );
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String actualOutput = out.toString();
    assertEquals("Invalid Arguments."
            , actualOutput);
  }

  @Test
  public void testForInteractiveIncompleteArgumentsUse() {
    Reader in = new StringReader("use calendar "
    );
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String actualOutput = out.toString();
    assertEquals("Invalid Arguments."
            , actualOutput);
  }

  @Test
  public void testForInteractiveCreateUseEdit() {
    Reader in = new StringReader("create calendar --name Cal1 --timezone Asia/Kolkata\n" +
            "create calendar --name Cal2 --timezone Asia/Kolkata\n" +
            "use calendar --name Cal1\n" +
            "create event --autoDecline \"Meeting One\"" +
            "from 2025-03-05T02:30 to 2025-03-05T03:30\n" +
            "use calendar --name Cal2\n" +
                    "print events on 2025-03-05\n" +
            "use calendar --name Cal1\n" +
            "print events on 2025-03-05\n");
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    assertEquals("Calendar added successfully with name: Cal1 and timezone: " +
            "Asia/Kolkata\n" +
            "Calendar added successfully with name: Cal2 and timezone: Asia/Kolkata\n" +
            "Single Event created successfully\n" +
            "•Subject:Meeting One from 2025-03-05 02:30 to 2025-03-05 03:30\n",
            out.toString());
  }

  @Test
  public void testCalendarControllerCopyEventToAStartDateTime() {
    Reader in = new StringReader(initCalendar + initCalendar2 +
            "copy event Meeting1 on 2025-03-05T02:30 --target Cal2 to 2025-03-07T02:30\n" +
            "use calendar --name Cal2\nprint events on 2025-03-07");
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Event(s) copied successfully" +
            "•Subject:Meeting1 from 2025-03-07 02:30 to 2025-03-07 03:30";
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[5] + output[6]);
  }

  @Test
  public void testCalendarControllerCopyEventsOnADateDifferentCalendar() {
    Reader in = new StringReader(initCalendar + initCalendar2 +
            "create event Meeting1 from 2025-03-05T15:30 to 2025-03-05T16:30\n" +
            "copy events on 2025-03-05 --target Cal2 to 2025-03-07\n" +
            "use calendar --name Cal2\nprint events from 2025-03-06T00:30 to 2025-03-08T23:30");
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Event(s) copied successfully" +
            "•Subject:Meeting1 from 2025-03-07 13:00 to 2025-03-07 14:00" +
            "•Subject:Meeting1 from 2025-03-08 02:00 to 2025-03-08 03:00";
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[6] + output[7] + output[8]);
  }

  @Test
  public void testCalendarControllerCopyEventsBetweenDatesDifferentCalendar() {
    Reader in = new StringReader(initCalendar + initCalendar2 + "copy events between 2025-03-06" +
            " and 2025-03-30 --target Cal2 to 2025-03-07\n" +
            "use calendar --name Cal2\nprint events from 2025-03-06T00:30 to 2025-03-30T23:30");
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Calendar added successfully with name: Cal1 and timezone: America/New_York\n"
            + "Calendar added successfully with name: Cal2 and timezone: Asia/Kolkata\n" +
            "Single Event created successfully\n" +
            "Recurring Event created successfully\n" +
            "Single Event created successfully\n" +
            "Event(s) copied successfully\n" +
            "•Subject:Meeting2 from 2025-03-08 13:00 to 2025-03-08 14:00\n" +
            "•Subject:Meeting2 from 2025-03-09 13:00 to 2025-03-09 13:00\n" +
            "•Subject:Meeting2 from 2025-03-11 12:00 to 2025-03-11 13:00\n" +
            "•Subject:Meeting2 from 2025-03-15 12:00 to 2025-03-15 13:00\n" +
            "•Subject:Meeting2 from 2025-03-16 12:00 to 2025-03-16 13:00\n" +
            "•Subject:Meeting2 from 2025-03-18 12:00 to 2025-03-18 13:00\n" +
            "•Subject:Meeting2 from 2025-03-22 12:00 to 2025-03-22 13:00\n" +
            "•Subject:Meeting2 from 2025-03-23 12:00 to 2025-03-23 13:00\n";
    assertEquals(expected, out.toString());
  }

  @Test
  public void testCalendarControllerCopyEventsBetweenDatesDifferentCalendar2() {
    Reader in = new StringReader(initCalendar + initCalendar2 + "copy events between 2025-03-04" +
            " and 2025-03-30 --target Cal2 to 2025-03-07\n" +
            "use calendar --name Cal2\nprint events from 2025-03-06T00:30 to 2025-03-30T23:30");
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Calendar added successfully with name: Cal1 and timezone: America/New_York\n"
            + "Calendar added successfully with name: Cal2 and timezone: Asia/Kolkata\n" +
            "Single Event created successfully\n" +
            "Recurring Event created successfully\n" +
            "Single Event created successfully\n" +
            "Event(s) copied successfully\n" +
            "•Subject:Meeting1 from 2025-03-08 13:00 to 2025-03-08 14:00\n" +
            "•Subject:Meeting2 from 2025-03-10 12:00 to 2025-03-10 13:00\n" +
            "•Subject:Meeting2 from 2025-03-11 12:00 to 2025-03-11 13:00\n" +
            "•Subject:Meeting2 from 2025-03-13 12:00 to 2025-03-13 13:00\n" +
            "•Subject:Meeting2 from 2025-03-17 12:00 to 2025-03-17 13:00\n" +
            "•Subject:Meeting2 from 2025-03-18 12:00 to 2025-03-18 13:00\n" +
            "•Subject:Meeting2 from 2025-03-20 12:00 to 2025-03-20 13:00\n" +
            "•Subject:Meeting2 from 2025-03-24 12:00 to 2025-03-24 13:00\n" +
            "•Subject:Meeting2 from 2025-03-25 12:00 to 2025-03-25 13:00\n";
    assertEquals(expected, out.toString());
  }

  @Test
  public void testCalendarControllerCopyEventsBetweenDatesDifferentCalendarConflictOneEvent() {
    Reader in = new StringReader(initCalendar + initCalendar2 + "copy events between 2025-03-06" +
            " and 2025-03-30 --target Cal2 to 2025-04-01\n" +
            "use calendar --name Cal2\nprint events from 2025-04-01T00:30 to 2025-04-30T23:30");
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Calendar added successfully with name: Cal1 and timezone: America/New_York\n"
            + "Calendar added successfully with name: Cal2 and timezone: Asia/Kolkata\n" +
            "Single Event created successfully\n" +
            "Recurring Event created successfully\n" +
            "Single Event created successfully\n" +
            "Event starting at: 2025-03-10T02:30 in source calendar" +
            " conflicts with existing event in target calendar\n" +
            "Event(s) copied successfully\n" +
            "•Subject:Meeting2 from 2025-04-02 12:00 to 2025-04-02 13:00\n" +
            "•Subject:Meeting2 from 2025-04-03 12:00 to 2025-04-03 13:00\n" +
            "•Subject:Meeting21 from 2025-04-05 12:30 to 2025-04-05 13:30\n" +
            "•Subject:Meeting2 from 2025-04-09 12:00 to 2025-04-09 13:00\n" +
            "•Subject:Meeting2 from 2025-04-10 12:00 to 2025-04-10 13:00\n" +
            "•Subject:Meeting2 from 2025-04-12 12:00 to 2025-04-12 13:00\n" +
            "•Subject:Meeting2 from 2025-04-16 12:00 to 2025-04-16 13:00\n" +
            "•Subject:Meeting2 from 2025-04-17 12:00 to 2025-04-17 13:00\n";
    assertEquals(expected, out.toString());
  }

  @Test
  public void testCalendarControllerCopyEventsBetweenDatesDifferentCalendarNoEvents() {
    Reader in = new StringReader(initCalendar + initCalendar2 + "copy events between 2025-02-06" +
            " and 2025-02-28 --target Cal2 to 2025-04-01\n" +
            "use calendar --name Cal2\nprint events from 2025-04-01T00:30 to 2025-04-30T23:30");
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Calendar added successfully with name: Cal1 and timezone: America/New_York\n"
            + "Calendar added successfully with name: Cal2 and timezone: Asia/Kolkata\n" +
            "Single Event created successfully\n" +
            "Recurring Event created successfully\n" +
            "Single Event created successfully\n" +
            "No events found to copy\n" +
            "•Subject:Meeting21 from 2025-04-05 12:30 to 2025-04-05 13:30\n";
    assertEquals(expected, out.toString());
  }

  @Test
  public void testCalendarControllerCopyEventsBetweenDatesSameCalendarNoEvents() {
    Reader in = new StringReader(initCalendar + "copy events between 2025-02-06" +
            " and 2025-02-28 --target Cal1 to 2025-04-01\n" +
            "print events from 2025-04-01T00:30 to 2025-04-30T23:30");
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Calendar added successfully with name: Cal1 and timezone: America/New_York\n"
            + "No events found to copy\n" ;
    assertEquals(expected, out.toString());
  }

  @Test
  public void testCalendarControllerCopyEventToAStartDateTimeSameCalendar() {
    Reader in = new StringReader(initCalendar + initCalendar2 +
            "copy event Meeting1 on 2025-03-05T02:30 --target Cal1 to 2025-04-07T02:30\n" +
            "print events on 2025-04-07");
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Event(s) copied successfully" +
            "•Subject:Meeting1 from 2025-04-07 02:30 to 2025-04-07 03:30";
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[5] + output[6]);
  }

  @Test
  public void testCalendarControllerCopyEventsOnADateSameCalendar() {
    Reader in = new StringReader(initCalendar + initCalendar2 +
            "create event Meeting1 from 2025-03-05T15:30 to 2025-03-05T16:30\n" +
            "copy events on 2025-03-05 --target Cal1 to 2025-04-07\n" +
            "print events from 2025-04-06T00:30 to 2025-04-08T23:30");
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Event(s) copied successfully" +
            "•Subject:Meeting1 from 2025-04-07 02:30 to 2025-04-07 03:30" +
            "•Subject:Meeting1 from 2025-04-07 15:30 to 2025-04-07 16:30";
    String outString = out.toString();
    String[] output = outString.split("\n");
    assertEquals(expected, output[6] + output[7] + output[8]);
  }

  @Test
  public void testCalendarControllerCopyEventsBetweenDatesSameCalendar() {
    Reader in = new StringReader(initCalendar + initCalendar2 + "copy events between 2025-03-06" +
            " and 2025-03-30 --target Cal1 to 2025-04-07\n" +
            "print events from 2025-04-06T00:30 to 2025-04-30T23:30");
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Calendar added successfully with name: Cal1 and timezone: America/New_York\n"
            + "Calendar added successfully with name: Cal2 and timezone: Asia/Kolkata\n" +
            "Single Event created successfully\n" +
            "Recurring Event created successfully\n" +
            "Single Event created successfully\n" +
            "Event(s) copied successfully\n" +
            "•Subject:Meeting2 from 2025-04-08 02:30 to 2025-04-08 03:30\n" +
            "•Subject:Meeting2 from 2025-04-09 02:30 to 2025-04-09 03:30\n" +
            "•Subject:Meeting2 from 2025-04-11 02:30 to 2025-04-11 03:30\n" +
            "•Subject:Meeting2 from 2025-04-15 02:30 to 2025-04-15 03:30\n" +
            "•Subject:Meeting2 from 2025-04-16 02:30 to 2025-04-16 03:30\n" +
            "•Subject:Meeting2 from 2025-04-18 02:30 to 2025-04-18 03:30\n" +
            "•Subject:Meeting2 from 2025-04-22 02:30 to 2025-04-22 03:30\n" +
            "•Subject:Meeting2 from 2025-04-23 02:30 to 2025-04-23 03:30\n";
    assertEquals(expected, out.toString());
  }

  @Test
  public void testCalendarControllerCopyEventsBetweenDatesSameCalendar2() {
    Reader in = new StringReader(initCalendar + initCalendar2 + "copy events between 2025-03-04" +
            " and 2025-03-30 --target Cal1 to 2025-04-07\n" +
            "print events from 2025-04-06T00:30 to 2025-04-30T23:30");
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Calendar added successfully with name: Cal1 and timezone: America/New_York\n"
            + "Calendar added successfully with name: Cal2 and timezone: Asia/Kolkata\n" +
            "Single Event created successfully\n" +
            "Recurring Event created successfully\n" +
            "Single Event created successfully\n" +
            "Event(s) copied successfully\n" +
            "•Subject:Meeting1 from 2025-04-08 02:30 to 2025-04-08 03:30\n" +
            "•Subject:Meeting2 from 2025-04-10 02:30 to 2025-04-10 03:30\n" +
            "•Subject:Meeting2 from 2025-04-11 02:30 to 2025-04-11 03:30\n" +
            "•Subject:Meeting2 from 2025-04-13 02:30 to 2025-04-13 03:30\n" +
            "•Subject:Meeting2 from 2025-04-17 02:30 to 2025-04-17 03:30\n" +
            "•Subject:Meeting2 from 2025-04-18 02:30 to 2025-04-18 03:30\n" +
            "•Subject:Meeting2 from 2025-04-20 02:30 to 2025-04-20 03:30\n" +
            "•Subject:Meeting2 from 2025-04-24 02:30 to 2025-04-24 03:30\n" +
            "•Subject:Meeting2 from 2025-04-25 02:30 to 2025-04-25 03:30\n";
    assertEquals(expected, out.toString());
  }

  @Test
  public void testCalendarControllerCopyEventsBetweenDatesSameCalendarConflictMultipleEvent() {
    Reader in = new StringReader(initCalendar + initCalendar2 + "copy events between 2025-03-06" +
            " and 2025-03-30 --target Cal1 to 2025-03-07\n" +
            "print events from 2025-03-01T00:30 to 2025-03-30T23:30");
    controller = new CalendarController(in, "interactive", calendar, calendarView);
    controller.listenInput();
    String expected = "Calendar added successfully with name: Cal1 and timezone: America/New_York\n"
            + "Calendar added successfully with name: Cal2 and timezone: Asia/Kolkata\n" +
            "Single Event created successfully\n" +
            "Recurring Event created successfully\n" +
            "Single Event created successfully\n" +
            "Event starting at: 2025-03-07T02:30 in source calendar" +
            " conflicts with existing event in target calendar\n" +
            "Event starting at: 2025-03-14T02:30 in source calendar" +
            " conflicts with existing event in target calendar\n" +
            "Event starting at: 2025-03-21T02:30 in source calendar" +
            " conflicts with existing event in target calendar\n" +
            "Event(s) copied successfully\n" +
            "•Subject:Meeting1 from 2025-03-05 02:30 to 2025-03-05 03:30\n" +
            "•Subject:Meeting2 from 2025-03-07 02:30 to 2025-03-07 03:30\n" +
            "•Subject:Meeting2 from 2025-03-08 02:30 to 2025-03-08 03:30\n" +
            "•Subject:Meeting2 from 2025-03-10 02:30 to 2025-03-10 03:30\n" +
            "•Subject:Meeting2 from 2025-03-14 02:30 to 2025-03-14 03:30\n" +
            "•Subject:Meeting2 from 2025-03-15 02:30 to 2025-03-15 03:30\n" +
            "•Subject:Meeting2 from 2025-03-17 02:30 to 2025-03-17 03:30\n" +
            "•Subject:Meeting2 from 2025-03-21 02:30 to 2025-03-21 03:30\n" +
            "•Subject:Meeting2 from 2025-03-22 02:30 to 2025-03-22 03:30\n" +
            "•Subject:Meeting2 from 2025-03-09 03:30 to 2025-03-09 03:30\n" +
            "•Subject:Meeting2 from 2025-03-11 02:30 to 2025-03-11 03:30\n" +
            "•Subject:Meeting2 from 2025-03-16 02:30 to 2025-03-16 03:30\n" +
            "•Subject:Meeting2 from 2025-03-18 02:30 to 2025-03-18 03:30\n" +
            "•Subject:Meeting2 from 2025-03-23 02:30 to 2025-03-23 03:30\n";
    assertEquals(expected, out.toString());
  }

}