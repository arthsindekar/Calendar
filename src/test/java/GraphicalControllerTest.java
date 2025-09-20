import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;

import controller.GraphicalController;
import model.Calendar;
import model.ICalendar;
import view.ICalendarView;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * Class that tests GUI functionality of features.
 */
public class GraphicalControllerTest {

  GraphicalController graphicalController;
  GraphicalController graphicalControllerWithMock;
  ICalendar calendar;
  ICalendarView calendarView;
  Appendable out;
  ICalendar mockCalendar;
  StringBuilder log;
  StringBuilder viewLog;
  File file;

  @Before
  public void setUp() {
    log = new StringBuilder();
    viewLog = new StringBuilder();
    out = new StringBuffer();
    calendarView = new MockCalendarView(out, viewLog);
    calendar = new Calendar();
    calendar.setName("Default");
    calendar.setTimeZone(TimeZone.getDefault());
    mockCalendar = new MockCalendar("Default", TimeZone.getDefault(), log, "abc");
    graphicalController = new GraphicalController(calendar, calendarView);
    graphicalControllerWithMock = new GraphicalController(mockCalendar, calendarView);
    assertEquals("[][abc]", viewLog.toString());
    file = new File("testImport.csv");
  }

  @After
  public void tearDown() {
    file.delete();
  }


  @Test
  public void testCreateCalendarSuccess() {
    graphicalController.createCalendar("Cal1", "Asia/Kolkata");
    graphicalController.useCalendar("Cal1");
    LocalDateTime startTime = LocalDateTime.of(2025, 3,
            5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3,
            5, 11, 30);
    graphicalController.createEvent("Lecture 1", startTime.toLocalDate(),
            startTime.toLocalTime(), endTime.toLocalDate(), endTime.toLocalTime(),
            null, null, null, false,
            null, false, 0, null);
    List<String> view = graphicalController.viewEvents(startTime.toLocalDate());
    String actual = view.get(0);
    String expected1 = "Calendar added successfully with name: Cal1 and timezone: Asia/Kolkata\n" +
            "Event created successfully";
    String expected2 = "\"Lecture 1\",\"2025-03-05\",\"11:00\",\"2025-03-05\",\"11:30\",\"\"," +
            "\"\",\"\",\"False\"";
    assertEquals(expected1, out.toString());
    assertEquals(expected2, actual);
  }

  @Test
  public void testCreateCalendarDuplicate() {
    graphicalController.createCalendar("Cal1", "Asia/Kolkata");
    String actual = "";
    try {
      graphicalController.createCalendar("Cal1", "Asia/Kolkata");
    } catch (Exception e) {
      actual = e.getMessage();
    }
    String expected = "Calendar added successfully with name: Cal1 and timezone: Asia/Kolkata\n";
    assertEquals("Calendar already exists\n", actual);
    assertEquals(expected, out.toString());
  }

  @Test
  public void testCreateCalendarInvalidTimezone() {
    String actual = "";
    try {
      graphicalController.createCalendar("Cal1", "Asia/Delhi");
    } catch (Exception e) {
      actual = e.getMessage();
    }
    assertEquals("Invalid Timezone", actual);
  }

  @Test
  public void testUseCalendarSuccess() {
    graphicalController.createCalendar("Cal1", "Asia/Kolkata");
    graphicalController.useCalendar("Cal1");
    LocalDateTime startTime = LocalDateTime.of(2025, 3,
            5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3,
            5, 11, 30);
    graphicalController.createEvent("Lecture 1", startTime.toLocalDate(),
            startTime.toLocalTime(), endTime.toLocalDate(), endTime.toLocalTime(),
            null, null, null, false,
            null, false, 0, null);
    graphicalController.useCalendar("Default"); //use default where no events found
    List<String> view = graphicalController.viewEvents(startTime.toLocalDate());
    assertEquals(0, view.size());
    graphicalController.useCalendar("Cal1");//then use cal1 where we have one event
    view = graphicalController.viewEvents(startTime.toLocalDate());
    assertEquals("\"Lecture 1\",\"2025-03-05\",\"11:00\",\"2025-03-05\",\"11:30\",\"\",\"\",\"\"," +
                    "\"False\""
            , view.get(0));
    String expected1 = "Calendar added successfully with name: Cal1 and timezone: Asia/Kolkata\n" +
            "Event created successfully";
    assertEquals(expected1, out.toString());
  }

  @Test
  public void testUseCalendarFailure() {
    String actual = "";
    try {
      graphicalController.useCalendar("Cal1");
    } catch (Exception e) {
      actual = e.getMessage();
    }
    assertEquals("Calendar does not exist.", actual);
  }

  @Test
  public void testImportFail() {
    String actual = "";
    try {
      graphicalController.importCalendar("Cal1");
    } catch (Exception e) {
      actual = e.getMessage();
    }
    assertEquals("File Name must end with .csv", actual);
  }

  @Test
  public void testExportFail() {
    String actual = "";
    try {
      graphicalController.exportCalendar("Cal1");
    } catch (Exception e) {
      actual = e.getMessage();
    }
    assertEquals("CSV file does not end with .csv", actual);
  }

  @Test
  public void testImportSuccess() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3,
            5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3,
            5, 11, 30);
    graphicalController.createEvent("Lecture 1", startTime.toLocalDate(),
            startTime.toLocalTime(), endTime.toLocalDate(), endTime.toLocalTime(),
            null, null, false, false,
            null, false, 0, null);
    //first export the file
    graphicalController.exportCalendar("Default.csv");
    //correct output
    //then import
    graphicalControllerWithMock.importCalendar("Default.csv");

    assertEquals("startDateTime: 2024-12-12T00:00 endDateTime: " +
                    "2024-12-12T23:59:59.999999999Subject: Lecture 1 " +
                    "startDateTime: 2025-03-05T11:00 endDateTime: 2025-03-05T11:30" +
                    " description: null location: null isPrivate: false autoDecline: true",
            log.toString());
    String expectedOut = "Event created successfully" +
            "Calendar Exported successfully" +
            "File Imported successfully";
    assertEquals(expectedOut, out.toString());
    Path filePath = Paths.get("Default.csv");
    filePath.toFile().delete();
  }

  @Test
  public void testImportWithPrivateTrue() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3,
            5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3,
            5, 11, 30);
    graphicalController.createEvent("Lecture 1", startTime.toLocalDate(),
            startTime.toLocalTime(), endTime.toLocalDate(), endTime.toLocalTime(),
            null, null, true, false,
            null, false, 0, null);
    //first export the file
    graphicalController.exportCalendar("Default.csv");
    //correct output
    //then import
    graphicalControllerWithMock.importCalendar("Default.csv");

    assertEquals("startDateTime: 2024-12-12T00:00 endDateTime: " +
                    "2024-12-12T23:59:59.999999999Subject: Lecture 1 " +
                    "startDateTime: 2025-03-05T11:00 endDateTime: 2025-03-05T11:30 " +
                    "description: null location: null isPrivate: true autoDecline: true",
            log.toString());
    Path filePath = Paths.get("Default.csv");
    filePath.toFile().delete();
  }

  @Test
  public void testImportWithDescription() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3,
            5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3,
            5, 11, 30);
    graphicalController.createEvent("Lecture 1", startTime.toLocalDate(),
            startTime.toLocalTime(), endTime.toLocalDate(), endTime.toLocalTime(),
            "this is lec1", null, null, false,
            null, false, 0, null);
    //first export the file
    graphicalController.exportCalendar("Default.csv");
    //correct output
    //then import
    graphicalControllerWithMock.importCalendar("Default.csv");

    assertEquals("startDateTime: 2024-12-12T00:00 endDateTime: " +
                    "2024-12-12T23:59:59.999999999Subject: Lecture 1 " +
                    "startDateTime: 2025-03-05T11:00 endDateTime: 2025-03-05T11:30 " +
                    "description: this is lec1 location: null isPrivate: null autoDecline: true",
            log.toString());
    Path filePath = Paths.get("Default.csv");
    filePath.toFile().delete();
  }

  @Test
  public void testImportWithLocation() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3,
            5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3,
            5, 11, 30);
    graphicalController.createEvent("Lecture 1", startTime.toLocalDate(),
            startTime.toLocalTime(), endTime.toLocalDate(), endTime.toLocalTime(),
            null, "neu", null, false,
            null, false, 0, null);
    //first export the file
    graphicalController.exportCalendar("Default.csv");
    //correct output
    //then import
    graphicalControllerWithMock.importCalendar("Default.csv");

    assertEquals("startDateTime: 2024-12-12T00:00 endDateTime: " +
                    "2024-12-12T23:59:59.999999999Subject: Lecture 1 " +
                    "startDateTime: 2025-03-05T11:00 endDateTime: 2025-03-05T11:30 " +
                    "description: null location: neu isPrivate: null autoDecline: true",
            log.toString());
    Path filePath = Paths.get("Default.csv");
    filePath.toFile().delete();
  }

  @Test
  public void testImportWithoutAllDayEvent() {
    String fileName = "testImport.csv";

    String header = "\"Subject\",\"Start Date\",\"Start Time\",\"End Date\",\"End Time\"," +
            "\"Description\",\"Location\",\"Private\"";
    String data = "\"Meeting One\",\"2025-03-05\",\"02:30\",\"2025-03-05\"," +
            "\"03:30\",\"\",\"\",\"\"";

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
      writer.write(header);
      writer.newLine();
      writer.write(data);
    } catch (IOException e) {
      e.printStackTrace();
    }
    graphicalController.importCalendar("testImport.csv");

    assertEquals("\"Meeting One\",\"2025-03-05\",\"02:30\",\"2025-03-05\",\"03:30\",\"\",\"\"," +
                    "\"\",\"False\"",
            graphicalController.viewEvents(LocalDate.of(2025, 3, 5)).get(0));
  }

  @Test
  public void testImportWithoutPrivate() {
    String fileName = "testImport.csv";

    String header = "\"Subject\",\"Start Date\",\"Start Time\",\"End Date\",\"End Time\"," +
            "\"Description\",\"Location\",\"All Day Event\"";
    String data = "\"Meeting One\",\"2025-03-05\",\"02:30\",\"2025-03-05\",\"03:30\",\"\",\"\",," +
            "False";

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
      writer.write(header);
      writer.newLine();
      writer.write(data);
    } catch (IOException e) {
      e.printStackTrace();
    }
    graphicalController.importCalendar("testImport.csv");

    assertEquals("\"Meeting One\",\"2025-03-05\",\"02:30\",\"2025-03-05\",\"03:30\",\"\",\"\"," +
                    "\"\",\"False\"",
            graphicalController.viewEvents(LocalDate.of(2025, 3, 5)).get(0));
  }


  @Test
  public void testExportDataSuccess() {
    String actual = "";
    LocalDateTime startTime = LocalDateTime.of(2025, 3,
            5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3,
            5, 11, 30);
    graphicalController.createEvent("Lecture 1", startTime.toLocalDate(),
            startTime.toLocalTime(), endTime.toLocalDate(), endTime.toLocalTime(),
            null, null, false, false,
            null, false, 0, null);
    graphicalController.exportCalendar("Default.csv");
    Path filePath = Paths.get("Default.csv");
    try {
      actual = new String(Files.readAllBytes(filePath));
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
    assertEquals("\"Subject\",\"Start Date\",\"Start Time\",\"End Date\",\"End Time\"," +
            "\"Description\",\"Location\",\"Private\",\"All Day Event\"\n" +
            "\"Lecture 1\",\"2025-03-05\",\"11:00\",\"2025-03-05\",\"11:30\",\"\",\"\",\"False\"," +
            "\"False\"\n", actual);
    String expectedOut = "Event created successfully" +
            "Calendar Exported successfully";
    assertEquals(expectedOut, out.toString());
    filePath.toFile().delete();
  }

  @Test
  public void testCreateSingleEventFeature() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3,
            5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3,
            5, 11, 30);
    graphicalControllerWithMock.createEvent("Lecture 1", startTime.toLocalDate(),
            startTime.toLocalTime(), endTime.toLocalDate(), endTime.toLocalTime(),
            null, null, false, false,
            null, false, 0, null);
    String expected = "startDateTime: 2024-12-12T00:00 endDateTime: 2024-12-12T23:59:59.999999999" +
            "Subject: Lecture 1 startDateTime: 2025-03-05T11:00" +
            " endDateTime: 2025-03-05T11:30 description: null location: null" +
            " isPrivate: false autoDecline: true";
    assertEquals(expected, log.toString());
    assertEquals("Event created successfully", out.toString());
  }

  @Test
  public void testCreateRecurringEventWithOccurrencesFeature() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3,
            5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3,
            5, 11, 30);
    graphicalControllerWithMock.createEvent("Lecture 1", startTime.toLocalDate(),
            startTime.toLocalTime(), endTime.toLocalDate(), endTime.toLocalTime(),
            null, null, false, true,
            "MST", true, 4, null);
    String expected = "startDateTime: 2024-12-12T00:00 endDateTime: 2024-12-12T23:59:59.999999999" +
            "Subject: Lecture 1 startDateTime: 2025-03-05T11:00" +
            " endDateTime: 2025-03-05T11:30 description: null location: null" +
            " isPrivate: false daysOfWeek: MST occurrences: 4";
    assertEquals(expected, log.toString());
    assertEquals("Event created successfully", out.toString());
  }

  @Test
  public void testCreateRecurringEventWithSpecificEndDateFeature() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3,
            5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3,
            5, 11, 30);
    LocalDate specificEndDate = LocalDate.of(2025, 3, 15);
    graphicalControllerWithMock.createEvent("Lecture 1", startTime.toLocalDate(),
            startTime.toLocalTime(), endTime.toLocalDate(), endTime.toLocalTime(),
            null, null, false, true,
            "MST", false, 0, specificEndDate);
    String expected = "startDateTime: 2024-12-12T00:00 endDateTime: 2024-12-12T23:59:59.999999999" +
            "Subject: Lecture 1 startDateTime: 2025-03-05T11:00" +
            " endDateTime: 2025-03-05T11:30 description: null location: null" +
            " isPrivate: false daysOfWeek: MST specificEndDateTime: 2025-03-15T23:59:59.999999999";
    assertEquals(expected, log.toString());
    assertEquals("Event created successfully", out.toString());
  }

  @Test
  public void testCreateSingleEventFeatureSubjectIsNull() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3,
            5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3,
            5, 11, 30);
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class, () -> {
            graphicalControllerWithMock.createEvent(null, startTime.toLocalDate(),
                    startTime.toLocalTime(), endTime.toLocalDate(), endTime.toLocalTime(),
                    null, null, false, false,
                    null, false, 0, null);
        }
    );
    String expected = "startDateTime: 2024-12-12T00:00 endDateTime: 2024-12-12T23:59:59.999999999" +
            "Subject: null startDateTime: 2025-03-05T11:00" +
            " endDateTime: 2025-03-05T11:30 description: null location: null" +
            " isPrivate: false autoDecline: true";
    assertEquals(expected, log.toString());
    assertEquals("Subject cannot be null", exception.getMessage());
  }

  @Test
  public void testEditSingleEventFeature() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3,
            5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3,
            5, 11, 30);
    graphicalControllerWithMock.editEvent("Lecture 1", startTime.toLocalDate(),
            startTime.toLocalTime(), endTime.toLocalDate(), endTime.toLocalTime(),
            "description", "This is PDP lecture");
    String expected = "startDateTime: 2024-12-12T00:00 endDateTime: 2024-12-12T23:59:59.999999999" +
            "Subject: Lecture 1 startDateTime: 2025-03-05T11:00" +
            " endDateTime: 2025-03-05T11:30 property: description newValue: This is PDP lecture";
    assertEquals(expected, log.toString());
    assertEquals("", out.toString());
  }

  @Test
  public void testEditMultipleEventsAfterSpecifiedDateFeature() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3,
            5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3,
            5, 11, 30);
    graphicalControllerWithMock.editEvents("Lecture 1", false, startTime.toLocalDate(),
            startTime.toLocalTime(), "location", "Dodge Hall");
    String expected = "startDateTime: 2024-12-12T00:00 endDateTime: 2024-12-12T23:59:59.999999999" +
            "Subject: Lecture 1 startDateTime: 2025-03-05T11:00" +
            " property: location newValue: Dodge Hall";
    assertEquals(expected, log.toString());
    assertEquals("Multiple events edited successfully", out.toString());
  }

  @Test
  public void testEditMultipleEventsAllWithSubjectFeature() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3,
            5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3,
            5, 11, 30);
    graphicalControllerWithMock.editEvents("Lecture 1", true, startTime.toLocalDate(),
            startTime.toLocalTime(), "location", "Dodge Hall");
    String expected = "startDateTime: 2024-12-12T00:00 endDateTime: 2024-12-12T23:59:59.999999999" +
            "Subject: Lecture 1 startDateTime: null" +
            " property: location newValue: Dodge Hall";
    assertEquals(expected, log.toString());
    assertEquals("Multiple events edited successfully", out.toString());
  }

  @Test
  public void testEditMultipleEventsAllNoSubjectFeature() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3,
            5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3,
            5, 11, 30);
    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class, () -> {
            graphicalControllerWithMock.editEvents(null, true, startTime.toLocalDate(),
                    startTime.toLocalTime(), "location", "Dodge Hall");
        }
    );
    String expected = "startDateTime: 2024-12-12T00:00 endDateTime: 2024-12-12T23:59:59.999999999" +
            "Subject: null startDateTime: null property: location newValue: Dodge Hall";
    assertEquals(expected, log.toString());
    assertEquals("Subject cannot be null or empty", exception.getMessage());
  }

  @Test
  public void testCreateSingleEventFeature2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3,
            5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3,
            5, 11, 30);
    graphicalController.createEvent("Lecture 1", startTime.toLocalDate(),
            startTime.toLocalTime(), endTime.toLocalDate(), endTime.toLocalTime(),
            null, null, false, false,
            null, false, 0, null);
    String expected = "\"Lecture 1\",\"2025-03-05\",\"11:00\",\"2025-03-05\",\"11:30\",\"\"," +
            "\"\",\"False\",\"False\"";
    assertEquals(expected, graphicalController.viewEvents(startTime.toLocalDate()).get(0));
    assertEquals("Event created successfully", out.toString());
  }

  @Test
  public void testCreateRecurringEventWithOccurrencesFeature2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3,
            5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3,
            5, 11, 30);
    graphicalController.createEvent("Lecture 1", startTime.toLocalDate(),
            startTime.toLocalTime(), endTime.toLocalDate(), endTime.toLocalTime(),
            null, null, false, true,
            "MST", true, 4, null);
    String expected = "\"Lecture 1\",\"2025-03-08\",\"11:00\",\"2025-03-08\",\"11:30\",\"\",\"\"," +
            "\"False\",\"False\"";
    String expected2 = "\"Lecture 1\",\"2025-03-10\",\"11:00\",\"2025-03-10\",\"11:30\",\"\"," +
            "\"\",\"False\",\"False\"";
    assertEquals(expected,
            graphicalController.viewEvents(startTime.plusDays(3).toLocalDate()).get(0));
    assertEquals(expected2,
            graphicalController.viewEvents(startTime.plusDays(5).toLocalDate()).get(0));
    assertEquals("Event created successfully", out.toString());
  }

  @Test
  public void testCreateRecurringEventWithSpecificEndDateFeature2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3,
            5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3,
            5, 11, 30);
    LocalDate specificEndDate = LocalDate.of(2025, 3, 15);
    graphicalController.createEvent("Lecture 1", startTime.toLocalDate(),
            startTime.toLocalTime(), endTime.toLocalDate(), endTime.toLocalTime(),
            null, null, false, true,
            "MST", false, 0, specificEndDate);
    String expected = "\"Lecture 1\",\"2025-03-08\",\"11:00\",\"2025-03-08\",\"11:30\",\"\",\"\"," +
            "\"False\",\"False\"";
    String expected2 = "\"Lecture 1\",\"2025-03-10\",\"11:00\",\"2025-03-10\",\"11:30\",\"\"," +
            "\"\",\"False\",\"False\"";
    assertEquals(expected,
            graphicalController.viewEvents(startTime.plusDays(3).toLocalDate()).get(0));
    assertEquals(expected2,
            graphicalController.viewEvents(startTime.plusDays(5).toLocalDate()).get(0));
    assertEquals("Event created successfully", out.toString());
  }

  @Test
  public void testCreateSingleEventFeatureSubjectIsNull2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3,
            5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3,
            5, 11, 30);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      graphicalController.createEvent(null, startTime.toLocalDate(),
              startTime.toLocalTime(), endTime.toLocalDate(), endTime.toLocalTime(),
              null, null, false, false,
              null, false, 0, null);
    });
    assertEquals("Subject cannot be null or empty", exception.getMessage());
  }

  @Test
  public void testEditSingleEventFeature2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3,
            5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3,
            5, 11, 30);
    graphicalController.createEvent("Lecture 1", startTime.toLocalDate(),
            startTime.toLocalTime(), endTime.toLocalDate(), endTime.toLocalTime(),
            null, null, false, false,
            null, false, 0, null);
    graphicalController.editEvent("Lecture 1", startTime.toLocalDate(),
            startTime.toLocalTime(), endTime.toLocalDate(), endTime.toLocalTime(),
            "description", "This is PDP lecture");
    String expected = "\"Lecture 1\",\"2025-03-05\",\"11:00\",\"2025-03-05\",\"11:30\",\"This is " +
            "PDP lecture\",\"\",\"False\",\"False\"";
    assertEquals(expected, graphicalController.viewEvents(startTime.toLocalDate()).get(0));
    assertEquals("Event created successfully", out.toString());
  }

  @Test
  public void testEditMultipleEventsAfterSpecifiedDateFeature2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3,
            5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3,
            5, 11, 30);
    graphicalController.createEvent("Lecture 1", startTime.toLocalDate(),
            startTime.toLocalTime(), endTime.toLocalDate(), endTime.toLocalTime(),
            null, null, false, true,
            "MST", true, 4, null);
    graphicalController.editEvents("Lecture 1", false, startTime.toLocalDate().plusDays(4),
            startTime.toLocalTime(), "location", "Dodge Hall");
    String expected = "\"Lecture 1\",\"2025-03-08\",\"11:00\",\"2025-03-08\",\"11:30\",\"\",\"\"," +
            "\"False\",\"False\"";
    String expected2 = "\"Lecture 1\",\"2025-03-10\",\"11:00\",\"2025-03-10\",\"11:30\",\"\"," +
            "\"Dodge Hall\",\"False\",\"False\"";
    String expected3 = "\"Lecture 1\",\"2025-03-11\",\"11:00\",\"2025-03-11\",\"11:30\",\"\"," +
            "\"Dodge Hall\",\"False\",\"False\"";
    assertEquals(expected,
            graphicalController.viewEvents(startTime.toLocalDate().plusDays(3)).get(0));
    assertEquals(expected2,
            graphicalController.viewEvents(startTime.toLocalDate().plusDays(5)).get(0));
    assertEquals(expected3,
            graphicalController.viewEvents(startTime.toLocalDate().plusDays(6)).get(0));
    assertEquals("Event created successfullyMultiple events edited successfully", out.toString());
  }

  @Test
  public void testEditMultipleEventsAllWithSubjectFeature2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3,
            5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3,
            5, 11, 30);
    graphicalController.createEvent("Lecture 1", startTime.toLocalDate(),
            startTime.toLocalTime(), endTime.toLocalDate(), endTime.toLocalTime(),
            null, null, false, true,
            "MST", true, 4, null);
    graphicalController.editEvents("Lecture 1", true, startTime.toLocalDate(),
            startTime.toLocalTime(), "location", "Dodge Hall");
    String expected = "\"Lecture 1\",\"2025-03-08\",\"11:00\",\"2025-03-08\",\"11:30\",\"\"," +
            "\"Dodge Hall\"," +
            "\"False\",\"False\"";
    String expected2 = "\"Lecture 1\",\"2025-03-10\",\"11:00\",\"2025-03-10\",\"11:30\",\"\"," +
            "\"Dodge Hall\",\"False\",\"False\"";
    String expected3 = "\"Lecture 1\",\"2025-03-11\",\"11:00\",\"2025-03-11\",\"11:30\",\"\"," +
            "\"Dodge Hall\",\"False\",\"False\"";
    assertEquals(expected,
            graphicalController.viewEvents(startTime.toLocalDate().plusDays(3)).get(0));
    assertEquals(expected2,
            graphicalController.viewEvents(startTime.toLocalDate().plusDays(5)).get(0));
    assertEquals(expected3,
            graphicalController.viewEvents(startTime.toLocalDate().plusDays(6)).get(0));
    assertEquals("Event created successfullyMultiple events edited successfully", out.toString());
  }

  @Test
  public void testEditMultipleEventsAllNoSubjectFeature2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3,
            5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3,
            5, 11, 30);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      graphicalController.editEvents(null, true, startTime.toLocalDate(),
              startTime.toLocalTime(), "location", "Dodge Hall");
    });
    assertEquals("Subject cannot be null or empty", exception.getMessage());
  }


}