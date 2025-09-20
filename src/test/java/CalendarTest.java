import org.junit.Before;
import org.junit.Test;

import java.io.File;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.TimeZone;

import model.Calendar;
import model.ICalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;


/**
 * Unit tests for the {@code ICalendar} model.
 * This class contains test cases to verify the correct behavior of the {@code ICalendar} interface
 * and its implementations, such as event management, calendar state handling, and interaction with
 * other components.
 */
public class CalendarTest {
  private ICalendar cal;
  private ICalendar cal2;
  File file;

  @Before
  public void setUp() {
    cal = new Calendar();
    cal2 = new Calendar();
    cal2.setName("Cal2");
    cal2.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
    file = new File("data.csv");
    file.setWritable(true);
  }

  //Create Events
  @Test
  public void testCreateSingleEvent() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    cal.createEvent("Lecture 1", startTime, endTime,
            null, null, null, false);
    String expected = "\"Lecture 1\",\"2025-03-05\",\"11:00\",\"2025-03-05\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, endTime));
  }

  @Test
  public void testCreateEventNullSubject() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createEvent(null, startTime, endTime, null, null, null, false);
    });
    assertEquals("Subject cannot be null or empty", exception.getMessage());
  }

  @Test
  public void testCreateEventEmptySubject() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createEvent("", startTime, endTime, 
              null, null, null, false);
    });
    assertEquals("Subject cannot be null or empty", exception.getMessage());
  }

  @Test
  public void testCreateEventNullStartDateTime() {
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createEvent("Lecture 1", null, endTime,
              null, null, null, false);
    });
    assertEquals("startDateTime and endDateTime cannot be null", exception.getMessage());
  }

  @Test
  public void testCreateEventEndDateTimeBeforeStartDateTime() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 10, 30);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createEvent("Lecture 1", startTime, endTime,
              null, null, null, false);
    });
    assertEquals("Start date and time must be before end date and time",
            exception.getMessage());
  }

  @Test
  public void testCreateEventAutoDeclineTrueNoConflict() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    cal.createEvent("Lecture 1", startTime, endTime,
            null, null, null, false);
    LocalDateTime startTime2 = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime endTime2 = LocalDateTime.of(2025, 3, 5, 12, 30);
    cal.createEvent("Lecture 2", startTime2, endTime2,
            null, null, null, true);
    String expected = "\"Lecture 1\",\"2025-03-05\",\"11:00\",\"2025-03-05\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 2\",\"2025-03-05\",\"11:30\",\"2025-03-05\",\"12:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, endTime2));
  }

  @Test
  public void testCreateEventAutoDeclineTrueNoConflictAfterEvent() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    cal.createEvent("Lecture 1", startTime, endTime,
            null, null, null, false);
    LocalDateTime startTime2 = LocalDateTime.of(2025, 3, 5, 10, 30);
    LocalDateTime endTime2 = LocalDateTime.of(2025, 3, 5, 11, 0);
    cal.createEvent("Lecture 2", startTime2, endTime2,
            null, null, null, true);
    String expected = "\"Lecture 2\",\"2025-03-05\",\"10:30\",\"2025-03-05\",\"11:00\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-05\",\"11:00\",\"2025-03-05\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime2, endTime));
  }

  @Test
  public void testCreateEventAutoDeclineTrueConflictAtEnd() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    cal.createEvent("Lecture 1", startTime, endTime,
            null, null, null, false);
    LocalDateTime startTime2 = LocalDateTime.of(2025, 3, 5, 11, 29);
    LocalDateTime endTime2 = LocalDateTime.of(2025, 3, 5, 12, 30);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createEvent("Lecture 2", startTime2, endTime2,
              null, null, null, true);
    });
    String expected = "\"Lecture 1\",\"2025-03-05\",\"11:00\",\"2025-03-05\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, endTime));
    assertEquals("Conflicting with existing event", exception.getMessage());
  }

  @Test
  public void testCreateEventAutoDeclineTrueConflictAtStart() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    cal.createEvent("Lecture 1", startTime, endTime,
            null, null, null, false);
    LocalDateTime startTime2 = LocalDateTime.of(2025, 3, 5, 10, 30);
    LocalDateTime endTime2 = LocalDateTime.of(2025, 3, 5, 11, 1);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createEvent("Lecture 2", startTime2, endTime2,
              null, null, null, true);
    });
    String expected = "\"Lecture 1\",\"2025-03-05\",\"11:00\",\"2025-03-05\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime2, endTime));
    assertEquals("Conflicting with existing event", exception.getMessage());
  }

  @Test
  public void testCreateEventAutoDeclineTrueConflictEndTimeSame() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    cal.createEvent("Lecture 1", startTime, endTime,
            null, null, null, false);
    LocalDateTime startTime2 = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime endTime2 = LocalDateTime.of(2025, 3, 5, 11, 30);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createEvent("Lecture 2", startTime2, endTime2,
              null, null, null, true);
    });
    String expected = "\"Lecture 1\",\"2025-03-05\",\"11:00\",\"2025-03-05\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, endTime));
    assertEquals("Conflicting with existing event", exception.getMessage());
  }

  @Test
  public void testCreateRecurringEventWithOccurrencesZeroOccurrences() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createRecurringEventWithOccurrences("Lecture 1", startTime, endTime,
              null, null, null, "MRF", 0);
    });
    assertEquals("Minimum one occurrence should be specified",
            exception.getMessage());
  }

  @Test
  public void testCreateRecurringEventWithOccurrencesNegativeOccurrences() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createRecurringEventWithOccurrences("Lecture 1", startTime, endTime,
              null, null, null, "MRF", -10);
    });
    assertEquals("Minimum one occurrence should be specified",
            exception.getMessage());
  }

  @Test
  public void testCreateRecurringEventWithOccurrencesEmptyDaysOfWeek() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createRecurringEventWithOccurrences("Lecture 1", startTime, endTime,
              null, null, null, "", 4);
    });
    assertEquals("Minimum 1 day of week should be specified",
            exception.getMessage());
  }

  @Test
  public void testCreateRecurringEventWithOccurrencesNullDaysOfWeek() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createRecurringEventWithOccurrences("Lecture 1", startTime, endTime,
              null, null, null, null, 4);
    });
    assertEquals("Minimum 1 day of week should be specified",
            exception.getMessage());
  }

  @Test
  public void testCreateRecurringEventWithOccurrencesNullSubject() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createRecurringEventWithOccurrences(null, startTime, endTime,
              null, null, null, "MSF", 4);
    });
    assertEquals("Subject cannot be null or empty",
            exception.getMessage());
  }

  @Test
  public void testCreateRecurringEventWithOccurrencesMultiDayEvent() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 6, 11, 30);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createRecurringEventWithOccurrences("Lecture 1", startTime, endTime,
              null, null, null, "MSF", 4);
    });
    assertEquals("Recurring event instance should start and end on the same day",
            exception.getMessage());
  }

  @Test
  public void testCreateRecurringEventWithOccurrencesStartDateTimeAfterEndDateTime() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 12, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createRecurringEventWithOccurrences("Lecture 1", startTime, endTime,
              null, null, null, "MSF", 4);
    });
    assertEquals("Start date and time must be before end date and time",
            exception.getMessage());
  }

  @Test
  public void testCreateRecurringEventWithTwoOccurrencesThreeDaysOfWeek() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    cal.createRecurringEventWithOccurrences("Lecture 1", startTime, endTime,
            null, null, null, "TSM", 2);
    String expected = "\"Lecture 1\",\"2025-03-08\",\"11:00\",\"2025-03-08\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-10\",\"11:00\",\"2025-03-10\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, endTime.plusMonths(1)));
  }

  @Test
  public void testCreateRecurringEventWithMultipleOccurrencesThreeDaysOfWeek() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    cal.createRecurringEventWithOccurrences("Lecture 1", startTime, endTime,
            null, null, null, "TSM", 8);
    String expected = "\"Lecture 1\",\"2025-03-08\",\"11:00\",\"2025-03-08\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-10\",\"11:00\",\"2025-03-10\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-11\",\"11:00\",\"2025-03-11\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-15\",\"11:00\",\"2025-03-15\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-17\",\"11:00\",\"2025-03-17\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-18\",\"11:00\",\"2025-03-18\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-22\",\"11:00\",\"2025-03-22\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-24\",\"11:00\",\"2025-03-24\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, endTime.plusMonths(1)));
  }

  @Test
  public void testCreateRecurringEventWithMultipleOccurrencesConflictWithSingleEvent() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 24, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 24, 11, 30);
    cal.createEvent("Lecture 1", startTime, endTime,
            null, null, null, false);
    LocalDateTime startRecurringTime = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime endRecurringTime = LocalDateTime.of(2025, 3, 5, 11, 1);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createRecurringEventWithOccurrences("Lecture 1", startRecurringTime,
              endRecurringTime,
              null, null, null, "TSM", 8);
    });

    String expected = "\"Lecture 1\",\"2025-03-24\",\"11:00\",\"2025-03-24\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n";
    assertEquals(expected, cal.printEvents(startRecurringTime, endRecurringTime.plusMonths(1)));
    assertEquals("Conflicting with existing event", exception.getMessage());
  }

  @Test
  public void testCreateSingleEventWithConflictWithRecurringEvent() {
    LocalDateTime startRecurringTime = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime endRecurringTime = LocalDateTime.of(2025, 3, 5, 11, 1);
    cal.createRecurringEventWithOccurrences("Lecture 1", startRecurringTime, endRecurringTime,
            null, null, null, "TSM", 8);
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 24, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 24, 11, 30);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createEvent("Lecture 2", startTime, endTime,
              null, null, null, true);
    });
    String expected = "\"Lecture 1\",\"2025-03-08\",\"10:00\",\"2025-03-08\",\"11:01\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-10\",\"10:00\",\"2025-03-10\",\"11:01\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-11\",\"10:00\",\"2025-03-11\",\"11:01\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-15\",\"10:00\",\"2025-03-15\",\"11:01\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-17\",\"10:00\",\"2025-03-17\",\"11:01\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-18\",\"10:00\",\"2025-03-18\",\"11:01\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-22\",\"10:00\",\"2025-03-22\",\"11:01\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-24\",\"10:00\",\"2025-03-24\",\"11:01\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startRecurringTime, endRecurringTime.plusMonths(1)));
    assertEquals("Conflicting with existing event", exception.getMessage());
  }

  @Test
  public void testCreateSingleEventWithConflictWithRecurringEventAtStart() {
    LocalDateTime startRecurringTime = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime endRecurringTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    cal.createRecurringEventWithOccurrences("Lecture 1", startRecurringTime, endRecurringTime,
            null, null, null, "TSM", 8);
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 24, 9, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 24, 10, 1);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createEvent("Lecture 2", startTime, endTime,
              null, null, null, true);
    });
    String expected = "\"Lecture 1\",\"2025-03-08\",\"10:00\",\"2025-03-08\",\"11:00\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-10\",\"10:00\",\"2025-03-10\",\"11:00\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-11\",\"10:00\",\"2025-03-11\",\"11:00\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-15\",\"10:00\",\"2025-03-15\",\"11:00\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-17\",\"10:00\",\"2025-03-17\",\"11:00\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-18\",\"10:00\",\"2025-03-18\",\"11:00\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-22\",\"10:00\",\"2025-03-22\",\"11:00\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-24\",\"10:00\",\"2025-03-24\",\"11:00\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startRecurringTime, endRecurringTime.plusMonths(1)));
    assertEquals("Conflicting with existing event", exception.getMessage());
  }

  @Test
  public void testCreateSingleEventWithConflictWithRecurringEventEndTimeSame() {
    LocalDateTime startRecurringTime = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime endRecurringTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    cal.createRecurringEventWithOccurrences("Lecture 1", startRecurringTime, endRecurringTime,
            null, null, null, "TSM", 8);
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 24, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 24, 11, 0);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createEvent("Lecture 2", startTime, endTime,
              null, null, null, true);
    });
    String expected = "\"Lecture 1\",\"2025-03-08\",\"10:00\",\"2025-03-08\",\"11:00\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-10\",\"10:00\",\"2025-03-10\",\"11:00\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-11\",\"10:00\",\"2025-03-11\",\"11:00\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-15\",\"10:00\",\"2025-03-15\",\"11:00\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-17\",\"10:00\",\"2025-03-17\",\"11:00\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-18\",\"10:00\",\"2025-03-18\",\"11:00\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-22\",\"10:00\",\"2025-03-22\",\"11:00\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-24\",\"10:00\",\"2025-03-24\",\"11:00\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startRecurringTime, endRecurringTime.plusMonths(1)));
    assertEquals("Conflicting with existing event", exception.getMessage());
  }

  @Test
  public void testCreateRecurringEventWithConflictWithRecurringEvent() {
    LocalDateTime startRecurringTime = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime endRecurringTime = LocalDateTime.of(2025, 3, 5, 11, 1);
    cal.createRecurringEventWithOccurrences("Lecture 1", startRecurringTime, endRecurringTime,
            null, null, null, "TSM", 8);
    LocalDateTime startRecurringTime2 = LocalDateTime.of(2025, 3, 24, 11, 0);
    LocalDateTime endRecurringTime2 = LocalDateTime.of(2025, 3, 24, 11, 30);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createRecurringEventWithOccurrences("Lecture 2", startRecurringTime2,
              endRecurringTime2,
              null, null, null, "TSM", 8);
    });
    String expected = "\"Lecture 1\",\"2025-03-08\",\"10:00\",\"2025-03-08\",\"11:01\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-10\",\"10:00\",\"2025-03-10\",\"11:01\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-11\",\"10:00\",\"2025-03-11\",\"11:01\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-15\",\"10:00\",\"2025-03-15\",\"11:01\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-17\",\"10:00\",\"2025-03-17\",\"11:01\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-18\",\"10:00\",\"2025-03-18\",\"11:01\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-22\",\"10:00\",\"2025-03-22\",\"11:01\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-24\",\"10:00\",\"2025-03-24\",\"11:01\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startRecurringTime, endRecurringTime.plusMonths(2)));
    assertEquals("Conflicting with existing event", exception.getMessage());
  }

  @Test
  public void testCreateRecurringEventWithWithSpecificEndDateTimeEmptyDaysOfWeek() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 8, 12, 30);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
              null, null, null, "", specificEndTime);
    });
    assertEquals("Minimum 1 day of week should be specified",
            exception.getMessage());
  }

  @Test
  public void testCreateRecurringEventWithSpecificEndDateTimeNullDaysOfWeek() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 8, 12, 30);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
              null, null, null, null, specificEndTime);
    });
    assertEquals("Minimum 1 day of week should be specified",
            exception.getMessage());
  }

  @Test
  public void testCreateRecurringEventWithSpecificEndDateTimeEmptyDaysOfWeek() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 8, 12, 30);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
              null, null, null, "", specificEndTime);
    });
    assertEquals("Minimum 1 day of week should be specified",
            exception.getMessage());
  }

  @Test
  public void testCreateRecurringEventWithSpecificEndDateTimeNullSubject() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 8, 12, 30);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createRecurringEventWithSpecificEndDateTime(null, startTime, endTime,
              null, null, null, "MSF", specificEndTime);
    });
    assertEquals("Subject cannot be null or empty",
            exception.getMessage());
  }

  @Test
  public void testCreateRecurringEventWithSpecificEndDateTimeMultiDayEvent() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 6, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 8, 12, 30);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
              null, null, null, "MSF", specificEndTime);
    });
    assertEquals("Recurring event instance should start and end on the same day",
            exception.getMessage());
  }

  @Test
  public void testCreateRecurringEventWithSpecificEndDateTimeStartDateTimeAfterEndDateTime() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 12, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 8, 12, 30);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
              null, null, null, "MSF", specificEndTime);
    });
    assertEquals("Start date and time must be before end date and time",
            exception.getMessage());
  }

  @Test
  public void testCreateRecurringEventWithNullSpecificEndDateTime() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 8, 12, 30);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
              null, null, null, "WRFU", null);
    });
    assertEquals("Specific end date should be specified",
            exception.getMessage());
  }

  @Test
  public void testCreateRecurringEventWithSpecificEndDateTimeBeforeEndTime() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 5, 11, 29);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
              null, null, null, "WRFU", specificEndTime);
    });
    assertEquals("Specific end date time should be after end date time",
            exception.getMessage());
  }

  @Test
  public void testCreateRecurringEventWithSpecificEndDateTimeWrongDaysOfWeek() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 8, 11, 30);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
              null, null, null, "ABC", specificEndTime);
    });
    assertEquals("Invalid day of week: A", exception.getMessage());
  }

  @Test
  public void testCreateRecurringEventWithSpecificEndDate() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 8, 11, 30);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "WRFU", specificEndTime);
    String expected = "\"Lecture 1\",\"2025-03-05\",\"11:00\",\"2025-03-05\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime));
  }

  @Test
  public void testCreateRecurringEventWithSpecificEndDateOneDayOfWeek() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 30, 11, 30);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "W", specificEndTime);
    String expected = "\"Lecture 1\",\"2025-03-05\",\"11:00\",\"2025-03-05\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-12\",\"11:00\",\"2025-03-12\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-19\",\"11:00\",\"2025-03-19\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-26\",\"11:00\",\"2025-03-26\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime));
  }

  @Test
  public void testCreateRecurringEventWithSpecificEndDateTimeBeforeFirstOccurrence() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 5, 23, 30);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    String expected = "";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testCreateRecurringEventWithSpecificEndTimeBeforeEndTime() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 7, 11, 29);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  //Edit Events

  @Test
  public void testEditEventsSubjectIsNull() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.editEvents(null, startTime,
              "description", "Lecture Survey");
    });
    assertEquals("Subject cannot be null or empty", exception.getMessage());
  }

  @Test
  public void testEditEventsSubjectIsEmpty() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.editEvents("", startTime,
              "description", "Lecture Survey");
    });
    assertEquals("Subject cannot be null or empty", exception.getMessage());
  }

  @Test
  public void testEditEventsWrongPropertyName() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.editEvents("Lecture 1", startTime,
              "attendees", "Utkarsh");
    });
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals("Invalid property: attendees", exception.getMessage());
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsSubject() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);

    cal.editEvents("Lecture 1", startTime.plusDays(4),
            "subject", "PDP Lecture 1");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"PDP Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"PDP Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"PDP Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsTwice() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);

    cal.editEvents("Lecture 1", startTime.plusDays(4),
            "subject", "PDP Lecture 1");
    cal.editEvents("Lecture 1", null,
            "endTime", "11:31");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:31\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"PDP Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"PDP Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"PDP Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsStartDateToAPreviousDateFromStartDateTime() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    LocalDate changedStartDate = startTime.toLocalDate().minusDays(7);

    cal.editEvents("Lecture 1", startTime,
            "startDate", changedStartDate.toString());
    String expected = "\"Lecture 1\",\"2025-02-27\",\"11:00\",\"2025-02-27\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-02-28\",\"11:00\",\"2025-02-28\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-02\",\"11:00\",\"2025-03-02\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(LocalDateTime.of(changedStartDate, LocalTime.MIN),
            specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsStartDateToAPreviousDateFromSpecificStartDateTime() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    LocalDate changedStartDate = startTime.toLocalDate().minusDays(7);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.editEvents("Lecture 1", startTime.plusDays(4),
              "startDate", changedStartDate.toString());
    });
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(LocalDateTime.of(changedStartDate, LocalTime.MIN),
            specificEndTime.plusMonths(1)));
    assertEquals("Conflicting with existing event", exception.getMessage());
  }

  @Test
  public void testEditEventsStartTime() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    LocalTime changedStartTime = startTime.toLocalTime().plusHours(3);
    cal.editEvents("Lecture 1", startTime.plusDays(4),
            "startTime", changedStartTime.toString());
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"14:00\",\"2025-03-09\",\"14:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"14:00\",\"2025-03-13\",\"14:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"14:00\",\"2025-03-14\",\"14:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsEndDateToPreviousDate() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    LocalDate changedEndDate = startTime.toLocalDate().minusDays(7);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.editEvents("Lecture 1", startTime.plusDays(4),
              "endDate", changedEndDate.toString());
    });
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(LocalDateTime.of(changedEndDate, LocalTime.MIN),
            specificEndTime.plusMonths(1)));
    assertEquals("End date cannot be before start date", exception.getMessage());
  }

  @Test
  public void testEditEventsEndDateToFutureDate() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    LocalDate changedEndDate = startTime.toLocalDate().plusDays(7);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.editEvents("Lecture 1", startTime.plusDays(4),
              "endDate", changedEndDate.toString());
    });
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
    assertEquals("Recurring event instance should start and" +
            " end on the same day", exception.getMessage());
  }

  @Test
  public void testEditEventsEndDateToFutureDate3() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    LocalDate changedEndDate = startTime.toLocalDate().plusDays(5);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.editEvents("Lecture 1", startTime.plusDays(4),
              "endDate", changedEndDate.toString());
    });
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
    assertEquals("Recurring event instance should start and" +
            " end on the same day", exception.getMessage());
  }

  @Test
  public void testEditEventsEndTimeBeforeStartTime() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    LocalTime changedEndTime = endTime.toLocalTime().minusHours(3);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.editEvents("Lecture 1", startTime.plusDays(4),
              "endTime", changedEndTime.toString());
    });
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
    assertEquals("End datetime cannot be before start datetime", exception.getMessage());
  }

  @Test
  public void testEditEventsEndTimeToFutureTime() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    LocalTime changedEndTime = endTime.toLocalTime().plusHours(3);
    cal.editEvents("Lecture 1", startTime.plusDays(4),
            "endTime", changedEndTime.toString());
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"14:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"14:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"14:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsDescription() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);

    cal.editEvents("Lecture 1", startTime.plusDays(4),
            "description", "PDP Lecture 1");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"PDP Lecture 1\"," +
            "\"\",\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"PDP Lecture 1\"," +
            "\"\",\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"PDP Lecture 1\"," +
            "\"\",\"\",\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsDescriptionSingleAndRecurring() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    cal.createEvent("Lecture 1", startTime.plusDays(4).plusHours(4),
            endTime.plusDays(4).plusHours(4), null, null, null, false);
    cal.createEvent("Lecture 1", startTime.minusHours(4), endTime.minusHours(4),
            null, null, null, false);

    cal.editEvents("Lecture 1", startTime.plusDays(4),
            "description", "PDP Lecture 1");
    String expected = "\"Lecture 1\",\"2025-03-05\",\"07:00\",\"2025-03-05\",\"07:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"PDP Lecture 1\"," +
            "\"\",\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"PDP Lecture 1\"," +
            "\"\",\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"PDP Lecture 1\"," +
            "\"\",\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"15:00\",\"2025-03-09\",\"15:30\",\"PDP Lecture 1\"," +
            "\"\",\"\",\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime.minusDays(2), specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsLocation() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);

    cal.editEvents("Lecture 1", startTime.plusDays(4),
            "location", "Dodge Hall");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"Dodge Hall\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"Dodge Hall\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"Dodge Hall\"," +
            "\"\",\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsDaysOfWeek() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);

    cal.editEvents("Lecture 1", startTime.plusDays(4),
            "daysOfWeek", "FTM");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-10\",\"11:00\",\"2025-03-10\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-11\",\"11:00\",\"2025-03-11\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsOccurrences() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);

    cal.editEvents("Lecture 1", startTime.plusDays(4),
            "occurrences", "8");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-16\",\"11:00\",\"2025-03-16\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-20\",\"11:00\",\"2025-03-20\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-21\",\"11:00\",\"2025-03-21\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-23\",\"11:00\",\"2025-03-23\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-27\",\"11:00\",\"2025-03-27\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsSetSpecificEndDate() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);

    cal.editEvents("Lecture 1", startTime.plusDays(4),
            "specificEndDate", "2025-03-21");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-16\",\"11:00\",\"2025-03-16\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-20\",\"11:00\",\"2025-03-20\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-21\",\"11:00\",\"2025-03-21\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsSetSpecificEndTime() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 14, 11, 15);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    String expected1 = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\"," +
            "\"\",\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
             "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
             "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
             "\"False\"\n";
    assertEquals(expected1, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
    cal.editEvents("Lecture 1", startTime.plusDays(4),
            "specificEndTime", "12:00");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditIsPrivate() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);

    cal.editEvents("Lecture 1", startTime.plusDays(4),
            "isPrivate", "true");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"True\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"True\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"True\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsSubject2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithOccurrences("Lecture 1", startTime, endTime,
            null, null, null, "RFU", 5);

    cal.editEvents("Lecture 1", startTime.plusDays(4),
            "subject", "PDP Lecture 1");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"PDP Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"PDP Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"PDP Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsStartDateToAPreviousDateFromStartDateTime2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithOccurrences("Lecture 1", startTime, endTime,
            null, null, null, "RFU", 5);
    LocalDate changedStartDate = startTime.toLocalDate().minusDays(7);

    cal.editEvents("Lecture 1", startTime,
            "startDate", changedStartDate.toString());
    String expected = "\"Lecture 1\",\"2025-02-27\",\"11:00\",\"2025-02-27\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-02-28\",\"11:00\",\"2025-02-28\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-02\",\"11:00\",\"2025-03-02\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(LocalDateTime.of(changedStartDate, LocalTime.MIN),
            specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsStartDateToAPreviousDateFromSpecificStartDateTime2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithOccurrences("Lecture 1", startTime, endTime,
            null, null, null, "RFU", 5);
    LocalDate changedStartDate = startTime.toLocalDate().minusDays(7);
    cal.editEvents("Lecture 1", startTime.plusDays(4),
            "startDate", changedStartDate.toString());
    String expected = "\"Lecture 1\",\"2025-02-27\",\"11:00\",\"2025-02-27\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-02-28\",\"11:00\",\"2025-02-28\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-02\",\"11:00\",\"2025-03-02\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(LocalDateTime.of(changedStartDate, LocalTime.MIN),
            specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsStartTime2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithOccurrences("Lecture 1", startTime, endTime,
            null, null, null, "RFU", 5);
    LocalTime changedStartTime = startTime.toLocalTime().plusHours(3);
    cal.editEvents("Lecture 1", startTime.plusDays(4),
            "startTime", changedStartTime.toString());
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"14:00\",\"2025-03-09\",\"14:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"14:00\",\"2025-03-13\",\"14:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"14:00\",\"2025-03-14\",\"14:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsEndDateToPreviousDate2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithOccurrences("Lecture 1", startTime, endTime,
            null, null, null, "RFU", 5);
    LocalDate changedEndDate = startTime.toLocalDate().minusDays(7);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.editEvents("Lecture 1", startTime.plusDays(4),
              "endDate", changedEndDate.toString());
    });
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(LocalDateTime.of(changedEndDate, LocalTime.MIN),
            specificEndTime.plusMonths(1)));
    assertEquals("End date cannot be before start date", exception.getMessage());
  }

  @Test
  public void testEditEventsEndDateToFutureDate2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithOccurrences("Lecture 1", startTime, endTime,
            null, null, null, "RFU", 5);
    LocalDate changedEndDate = startTime.toLocalDate().plusDays(7);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.editEvents("Lecture 1", startTime.plusDays(4),
              "endDate", changedEndDate.toString());
    });
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
    assertEquals("Recurring event instance should start and" +
            " end on the same day", exception.getMessage());
  }

  @Test
  public void testEditEventsEndTimeBeforeStartTime2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithOccurrences("Lecture 1", startTime, endTime,
            null, null, null, "RFU", 5);
    LocalTime changedEndTime = endTime.toLocalTime().minusHours(3);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.editEvents("Lecture 1", startTime.plusDays(4),
              "endTime", changedEndTime.toString());
    });
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
    assertEquals("End datetime cannot be before start datetime", exception.getMessage());
  }

  @Test
  public void testEditEventsEndTimeToFutureTime2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithOccurrences("Lecture 1", startTime, endTime,
            null, null, null, "RFU", 5);
    LocalTime changedEndTime = endTime.toLocalTime().plusHours(3);
    cal.editEvents("Lecture 1", startTime.plusDays(4),
            "endTime", changedEndTime.toString());
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"14:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"14:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"14:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsDescription2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithOccurrences("Lecture 1", startTime, endTime,
            null, null, null, "RFU", 5);

    cal.editEvents("Lecture 1", startTime.plusDays(4),
            "description", "PDP Lecture 1");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"PDP Lecture 1\"," +
            "\"\",\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"PDP Lecture 1\"," +
            "\"\",\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"PDP Lecture 1\"," +
            "\"\",\"\",\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsLocation2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithOccurrences("Lecture 1", startTime, endTime,
            null, null, null, "RFU", 5);

    cal.editEvents("Lecture 1", startTime.plusDays(4),
            "location", "Dodge Hall");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"Dodge Hall\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"Dodge Hall\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"Dodge Hall\"," +
            "\"\",\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsDaysOfWeek2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithOccurrences("Lecture 1", startTime, endTime,
            null, null, null, "RFU", 5);

    cal.editEvents("Lecture 1", startTime.plusDays(4),
            "daysOfWeek", "FTM");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-10\",\"11:00\",\"2025-03-10\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-11\",\"11:00\",\"2025-03-11\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsOccurrences2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithOccurrences("Lecture 1", startTime, endTime,
            null, null, null, "RFU", 5);

    cal.editEvents("Lecture 1", startTime.plusDays(4),
            "occurrences", "8");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-16\",\"11:00\",\"2025-03-16\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-20\",\"11:00\",\"2025-03-20\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-21\",\"11:00\",\"2025-03-21\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-23\",\"11:00\",\"2025-03-23\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-27\",\"11:00\",\"2025-03-27\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsSetSpecificEndDate2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithOccurrences("Lecture 1", startTime, endTime,
            null, null, null, "RFU", 5);

    cal.editEvents("Lecture 1", startTime.plusDays(4),
            "specificEndDate", "2025-03-21");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-16\",\"11:00\",\"2025-03-16\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-20\",\"11:00\",\"2025-03-20\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-21\",\"11:00\",\"2025-03-21\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsSetSpecificEndTime2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 14, 11, 15);
    cal.createRecurringEventWithOccurrences("Lecture 1", startTime, endTime,
            null, null, null, "RFU", 5);
    String expected1 = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\"," +
            "\"\",\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected1, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.editEvents("Lecture 1", startTime.plusDays(4),
              "specificEndTime", "12:00");
    });
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
    assertEquals("Specific end date must be specified before specific end time",
            exception.getMessage());
  }

  @Test
  public void testEditIsPrivate2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithOccurrences("Lecture 1", startTime, endTime,
            null, null, null, "RFU", 5);

    cal.editEvents("Lecture 1", startTime.plusDays(4),
            "isPrivate", "true");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"True\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"True\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"True\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsFromStartSubject() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);

    cal.editEvents("Lecture 1", null,
            "subject", "PDP Lecture 1");
    String expected = "\"PDP Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\"," +
            "\"\",\"\",\"False\"\n" +
            "\"PDP Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"PDP Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"PDP Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"PDP Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsFromStartStartDateToAPreviousDateFromStartDateTime() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    LocalDate changedStartDate = startTime.toLocalDate().minusDays(7);

    cal.editEvents("Lecture 1", null,
            "startDate", changedStartDate.toString());
    String expected = "\"Lecture 1\",\"2025-02-27\",\"11:00\",\"2025-02-27\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-02-28\",\"11:00\",\"2025-02-28\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-02\",\"11:00\",\"2025-03-02\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(LocalDateTime.of(changedStartDate, LocalTime.MIN),
            specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsFromStartStartDateToAPreviousDateFromSpecificStartDateTime() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    LocalDate changedStartDate = startTime.toLocalDate().minusDays(7);
    cal.editEvents("Lecture 1", null,
            "startDate", changedStartDate.toString());
    String expected = "\"Lecture 1\",\"2025-02-27\",\"11:00\",\"2025-02-27\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-02-28\",\"11:00\",\"2025-02-28\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-02\",\"11:00\",\"2025-03-02\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(LocalDateTime.of(changedStartDate, LocalTime.MIN),
            specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsFromStartStartTime() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    LocalTime changedStartTime = startTime.toLocalTime().plusHours(3);
    cal.editEvents("Lecture 1", null,
            "startTime", changedStartTime.toString());
    String expected = "\"Lecture 1\",\"2025-03-06\",\"14:00\",\"2025-03-06\",\"14:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"14:00\",\"2025-03-07\",\"14:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"14:00\",\"2025-03-09\",\"14:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"14:00\",\"2025-03-13\",\"14:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"14:00\",\"2025-03-14\",\"14:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsFromStartEndDateToPreviousDate() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    LocalDate changedEndDate = startTime.toLocalDate().minusDays(7);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.editEvents("Lecture 1", null,
              "endDate", changedEndDate.toString());
    });
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(LocalDateTime.of(changedEndDate, LocalTime.MIN),
            specificEndTime.plusMonths(1)));
    assertEquals("End date cannot be before start date", exception.getMessage());
  }

  @Test
  public void testEditEventsFromStartEndDateToFutureDate() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    LocalDate changedEndDate = startTime.toLocalDate().plusDays(7);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.editEvents("Lecture 1", null,
              "endDate", changedEndDate.toString());
    });
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
    assertEquals("Recurring event instance should start and" +
            " end on the same day", exception.getMessage());
  }

  @Test
  public void testEditEventsFromStartEndTimeBeforeStartTime() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    LocalTime changedEndTime = endTime.toLocalTime().minusHours(3);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.editEvents("Lecture 1", null,
              "endTime", changedEndTime.toString());
    });
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
    assertEquals("End datetime cannot be before start datetime", exception.getMessage());
  }

  @Test
  public void testEditEventsFromStartEndTimeToFutureTime() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    LocalTime changedEndTime = endTime.toLocalTime().plusHours(3);
    cal.editEvents("Lecture 1", null,
            "endTime", changedEndTime.toString());
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"14:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"14:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"14:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"14:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"14:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsFromStartDescription() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);

    cal.editEvents("Lecture 1", null,
            "description", "PDP Lecture 1");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"PDP " +
            "Lecture 1\",\"\",\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"PDP Lecture 1\"," +
            "\"\",\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"PDP Lecture 1\"," +
            "\"\",\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"PDP Lecture 1\"," +
            "\"\",\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"PDP Lecture 1\"," +
            "\"\",\"\",\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsFromStartLocation() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);

    cal.editEvents("Lecture 1", null,
            "location", "Dodge Hall");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\"," +
            "\"Dodge Hall\",\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"Dodge Hall\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"Dodge Hall\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"Dodge Hall\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"Dodge Hall\"," +
            "\"\",\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsFromStartDaysOfWeek() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);

    cal.editEvents("Lecture 1", null,
            "daysOfWeek", "FTM");
    String expected = "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-10\",\"11:00\",\"2025-03-10\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-11\",\"11:00\",\"2025-03-11\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-17\",\"11:00\",\"2025-03-17\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsFromStartOccurrences() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);

    cal.editEvents("Lecture 1", null,
            "occurrences", "8");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-16\",\"11:00\",\"2025-03-16\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-20\",\"11:00\",\"2025-03-20\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-21\",\"11:00\",\"2025-03-21\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsFromStartSetSpecificEndDate() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);

    cal.editEvents("Lecture 1", null,
            "specificEndDate", "2025-03-21");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-16\",\"11:00\",\"2025-03-16\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-20\",\"11:00\",\"2025-03-20\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-21\",\"11:00\",\"2025-03-21\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsFromStartSetSpecificEndTime() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 14, 11, 15);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    String expected1 = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\"," +
            "\"\",\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected1, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
    cal.editEvents("Lecture 1", null,
            "specificEndTime", "12:00");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventsFromStartIsPrivate() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);

    cal.editEvents("Lecture 1", null,
            "isPrivate", "true");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"True\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"True\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"True\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"True\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"True\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventSubject() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);

    cal.editEvent("Lecture 1", startTime.plusDays(4), endTime.plusDays(4),
            "subject", "PDP Lecture 1");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"PDP Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventStartDateToAPreviousDateFromStartDateTime() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);

    LocalDate changedStartDate = startTime.toLocalDate().minusDays(7);

    cal.editEvent("Lecture 1", startTime.plusDays(4), endTime.plusDays(4),
            "startDate", changedStartDate.toString());
    String expected = "\"Lecture 1\",\"2025-02-26\",\"11:00\",\"2025-02-26\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(LocalDateTime.of(changedStartDate, LocalTime.MIN),
            specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventStartDateToAPreviousDateFromSpecificStartDateTime() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    LocalDate changedStartDate = startTime.toLocalDate().minusDays(7);
    cal.editEvent("Lecture 1", startTime.plusDays(4), endTime.plusDays(4),
            "startDate", changedStartDate.toString());
    String expected = "\"Lecture 1\",\"2025-02-26\",\"11:00\",\"2025-02-26\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(LocalDateTime.of(changedStartDate, LocalTime.MIN),
            specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventStartTime() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    LocalTime changedStartTime = startTime.toLocalTime().plusHours(3);
    cal.editEvent("Lecture 1", startTime.plusDays(4), endTime.plusDays(4),
            "startTime", changedStartTime.toString());
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"14:00\",\"2025-03-09\",\"14:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventStartTimeToEndOfDay() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    LocalTime changedStartTime = startTime.toLocalTime().plusHours(12).plusMinutes(30);
    cal.editEvent("Lecture 1", startTime.plusDays(4), endTime.plusDays(4),
            "startTime", changedStartTime.toString());
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"23:30\",\"2025-03-09\",\"23:59\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventStartTimeToEndOfDay2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    LocalTime changedStartTime = LocalTime.MAX.minusMinutes(30);
    cal.editEvent("Lecture 1", startTime.plusDays(4), endTime.plusDays(4),
            "startTime", changedStartTime.toString());
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"23:29\",\"2025-03-09\",\"23:59\",\"\",\"\",\"\"," +
            "\"False\"\n";
    String isBusy = cal.showStatus(LocalDateTime.of(2025, 3, 9, 23,
            59, 59, 9));
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
    assertEquals("Available", isBusy);
  }

  @Test
  public void testEditEventEndDateToPreviousDate() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    LocalDate changedEndDate = startTime.toLocalDate().minusDays(7);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.editEvent("Lecture 1", startTime.plusDays(4), endTime.plusDays(4),
              "endDate", changedEndDate.toString());
    });
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(LocalDateTime.of(changedEndDate, LocalTime.MIN),
            specificEndTime.plusMonths(1)));
    assertEquals("End date cannot be before start date", exception.getMessage());
  }

  @Test
  public void testEditEventEndDateToFutureDate() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    LocalDate changedEndDate = startTime.toLocalDate().plusDays(7);
    cal.editEvent("Lecture 1", startTime.plusDays(4), endTime.plusDays(4),
            "endDate", changedEndDate.toString());
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-12\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventEndTimeBeforeStartTime() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    LocalTime changedEndTime = endTime.toLocalTime().minusHours(3);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.editEvent("Lecture 1", startTime.plusDays(4), endTime.plusDays(4),
              "endTime", changedEndTime.toString());
    });
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
    assertEquals("End datetime cannot be before start datetime", exception.getMessage());
  }

  @Test
  public void testEditEventEndTimeToFutureTime() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    LocalTime changedEndTime = endTime.toLocalTime().plusHours(3);
    cal.editEvent("Lecture 1", startTime.plusDays(4), endTime.plusDays(4),
            "endTime", changedEndTime.toString());
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"14:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventDescription() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);

    cal.editEvent("Lecture 1", startTime.plusDays(4), endTime.plusDays(4),
            "description", "PDP Lecture 1");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"PDP Lecture 1\"," +
            "\"\",\"\",\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventLocation() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);

    cal.editEvent("Lecture 1", startTime.plusDays(4), endTime.plusDays(4),
            "location", "Dodge Hall");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"Dodge Hall\"," +
            "\"\",\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditEventIsPrivate() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime,
            null, null, null, "RFU", specificEndTime);

    cal.editEvent("Lecture 1", startTime.plusDays(4), endTime.plusDays(4),
            "isPrivate", "true");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"True\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, specificEndTime.plusMonths(1)));
  }

  @Test
  public void testEditSingleEventSubject() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime startTime2 = LocalDateTime.of(2025, 3, 5, 12, 0);
    LocalDateTime endTime2 = LocalDateTime.of(2025, 3, 5, 12, 30);
    cal.createEvent("Lecture 1", startTime, endTime,
            null, null, null, false);
    cal.createEvent("Lecture 2", startTime2, endTime2,
            null, null, null, false);
    String expected1 = "\"Lecture 1\",\"2025-03-05\",\"11:00\",\"2025-03-05\",\"11:30\",\"\"," +
            "\"\",\"\",\"False\"\n" +
            "\"Lecture 2\",\"2025-03-05\",\"12:00\",\"2025-03-05\",\"12:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected1, cal.printEvents(startTime, endTime.plusMonths(1)));
    cal.editEvent("Lecture 1", startTime, endTime,
            "subject", "PDP Lecture 1");
    String expected = "\"PDP Lecture 1\",\"2025-03-05\",\"11:00\",\"2025-03-05\",\"11:30\",\"\"," +
            "\"\",\"\",\"False\"\n" +
            "\"Lecture 2\",\"2025-03-05\",\"12:00\",\"2025-03-05\",\"12:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, endTime.plusMonths(1)));
  }

  @Test
  public void testEditSingleEventNotAvailableSubject() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime startTime2 = LocalDateTime.of(2025, 3, 5, 12, 0);
    LocalDateTime endTime2 = LocalDateTime.of(2025, 3, 5, 12, 30);
    cal.createEvent("Lecture 2", startTime, endTime,
            null, null, null, false);
    cal.editEvent("Lecture 1", startTime, endTime,
            "subject", "PDP Lecture 1");
    String expected = "\"Lecture 2\",\"2025-03-05\",\"11:00\",\"2025-03-05\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, endTime.plusMonths(1)));
  }

  @Test
  public void testEditSingleEventNotAvailableSubject2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime startTime2 = LocalDateTime.of(2025, 3, 5, 12, 0);
    LocalDateTime endTime2 = LocalDateTime.of(2025, 3, 5, 12, 30);
    cal.createEvent("Lecture 1", startTime, endTime2,
            null, null, null, false);
    cal.editEvent("Lecture 1", startTime, endTime,
            "subject", "PDP Lecture 1");
    String expected = "\"Lecture 1\",\"2025-03-05\",\"11:00\",\"2025-03-05\",\"12:30\",\"\",\"\"," +
            "\"\",\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, endTime.plusMonths(1)));
  }

  @Test
  public void testEditSingleEventNotAvailableSubject3() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime startTime2 = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime endTime2 = LocalDateTime.of(2025, 3, 5, 12, 30);
    cal.createEvent("Lecture 1", startTime2, endTime,
            null, null, null, false);
    cal.editEvent("Lecture 1", startTime, endTime,
            "subject", "PDP Lecture 1");
    String expected = "\"Lecture 1\",\"2025-03-05\",\"10:00\",\"2025-03-05\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime2, endTime.plusMonths(1)));
  }

  @Test
  public void testEditRecurringEventNotAvailableSubject() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime startTime2 = LocalDateTime.of(2025, 3, 5, 12, 0);
    LocalDateTime endTime2 = LocalDateTime.of(2025, 3, 5, 12, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 2", startTime, endTime,
            null, null, null, "RFU", specificEndTime);
    cal.editEvent("Lecture 1", startTime.plusDays(4), endTime.plusDays(4),
            "subject", "PDP Lecture 1");
    String expected = "\"Lecture 2\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 2\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 2\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 2\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 2\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, endTime.plusMonths(1)));
  }

  @Test
  public void testEditRecurringEventNotAvailableSubject2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime startTime2 = LocalDateTime.of(2025, 3, 5, 12, 0);
    LocalDateTime endTime2 = LocalDateTime.of(2025, 3, 5, 12, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime, endTime2,
            null, null, null, "RFU", specificEndTime);
    cal.editEvent("Lecture 1", startTime.plusDays(4), endTime.plusDays(4),
            "subject", "PDP Lecture 1");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"11:00\",\"2025-03-06\",\"12:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"11:00\",\"2025-03-07\",\"12:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"11:00\",\"2025-03-09\",\"12:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"11:00\",\"2025-03-13\",\"12:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"11:00\",\"2025-03-14\",\"12:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime, endTime.plusMonths(1)));
  }

  @Test
  public void testEditRecurringEventNotAvailableSubject3() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    LocalDateTime startTime2 = LocalDateTime.of(2025, 3, 5, 10, 0);
    LocalDateTime endTime2 = LocalDateTime.of(2025, 3, 5, 12, 30);
    LocalDateTime specificEndTime = LocalDateTime.of(2025, 3, 15, 12, 0);
    cal.createRecurringEventWithSpecificEndDateTime("Lecture 1", startTime2, endTime,
            null, null, null, "RFU", specificEndTime);
    cal.editEvent("Lecture 1", startTime.plusDays(4), endTime.plusDays(4),
            "subject", "PDP Lecture 1");
    String expected = "\"Lecture 1\",\"2025-03-06\",\"10:00\",\"2025-03-06\",\"11:30\",\"\",\"\"," +
            "\"\",\"False\"\n" +
            "\"Lecture 1\",\"2025-03-07\",\"10:00\",\"2025-03-07\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-09\",\"10:00\",\"2025-03-09\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-13\",\"10:00\",\"2025-03-13\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-14\",\"10:00\",\"2025-03-14\",\"11:30\",\"\",\"\",\"\"," +
            "\"False\"\n";
    assertEquals(expected, cal.printEvents(startTime2, endTime.plusMonths(1)));
  }

  @Test
  public void testCreateTwoSingleEventsConflicts() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    cal.createEvent("Lecture 1", startTime, endTime,
            null, null, null, false);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createEvent("Lecture 1", startTime, endTime,
              null, null, null, true);
    });

    assertEquals("Conflicting with existing event", exception.getMessage());
  }

  @Test
  public void testCreateTwoSingleEventsConflicts2() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5, 11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5, 11, 30);
    cal.createEvent("Lecture 1", startTime, endTime,
            null, null, null, false);
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
      cal.createEvent("Lecture 1", startTime.minusHours(1), endTime,
              null, null, null, true);
    });

    assertEquals("Conflicting with existing event", exception.getMessage());
  }


  //Print Events
  @Test
  public void printEvents2() {
    LocalDate date = LocalDate.of(2025, 3, 5);//5march
    LocalDate date2 = LocalDate.of(2025, 3, 8);//8march
    LocalTime startTime = LocalTime.of(12, 30);//12:30am
    LocalTime endTime = LocalTime.of(12, 0);//12am

    LocalDateTime startDateTime = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime endDateTime = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am
    LocalDateTime startDateTime2 = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime endDateTime2 = LocalDateTime.of(2025, 3,
            7, 12, 30);//7am 12.30
    LocalDateTime startDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime endDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am
    cal.createEvent("Lecture 1", startDateTime, endDateTime,
            "DBMS", null, null, false);
    cal.createEvent("Lecture 2", startDateTime2, endDateTime2, "PDP",
            "NEU", null, false);
    cal.createEvent("Lecture 3", startDateTime3, endDateTime3, "AI",
            "NEU", null, false);
    String expected = "\"Lecture 2\",\"2025-03-07\",\"12:00\",\"2025-03-07\",\"12:30\",\"PDP\"," +
            "\"NEU\",\"\",\"False\"\n";
    assertEquals(expected,
            cal.printEvents(LocalDateTime.of(date, startTime), LocalDateTime.of(date2, endTime)));
  }

  @Test
  public void printEvents3() {
    LocalDate date = LocalDate.of(2025, 3, 7);//7march
    LocalDate date2 = LocalDate.of(2025, 3, 8);//8march
    LocalTime startTime = LocalTime.of(12, 0);//12:00am
    LocalTime endTime = LocalTime.of(12, 30);//12.15am

    LocalDateTime startDateTime = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime endDateTime = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am
    LocalDateTime startDateTime2 = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime endDateTime2 = LocalDateTime.of(2025, 3,
            7, 12, 30);//7am 12.30
    LocalDateTime startDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime endDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    //8 march 12.15am to 8 march 12.30am
    LocalDateTime startDateTime4 = LocalDateTime.of(2025, 3, 8,
            12, 15);//8 march 12am
    LocalDateTime endDateTime4 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    cal.createEvent("Lecture 1", startDateTime, endDateTime,
            "DBMS", null, null, false);
    cal.createEvent("Lecture 2", startDateTime2, endDateTime2, "PDP",
            "NEU", null, false);
    cal.createEvent("Lecture 3", startDateTime3, endDateTime3, "AI",
            "NEU", null, false);
    cal.createEvent("Lecture 4", startDateTime4, endDateTime4, "CS",
            "NEU", null, false);
    String expected = "\"Lecture 2\",\"2025-03-07\",\"12:00\",\"2025-03-07\",\"12:30\",\"PDP\"," +
            "\"NEU\",\"\",\"False\"\n" +
            "\"Lecture 3\",\"2025-03-08\",\"12:00\",\"2025-03-08\",\"12:30\",\"AI\",\"NEU\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 4\",\"2025-03-08\",\"12:15\",\"2025-03-08\",\"12:30\",\"CS\",\"NEU\",\"\"," +
            "\"False\"\n";
    System.out.println(cal.printEvents(LocalDateTime.of(date, startTime),
            LocalDateTime.of(date2, endTime)));
    assertEquals(expected,
            cal.printEvents(LocalDateTime.of(date, startTime), LocalDateTime.of(date2, endTime)));
  }

  @Test
  public void printEvents4() {
    LocalDate date = LocalDate.of(2025, 3, 7);//7march
    LocalDate date2 = LocalDate.of(2025, 3, 8);//8march
    LocalTime startTime = LocalTime.of(12, 0);//12:00am
    LocalTime endTime = LocalTime.of(12, 30);//12.30am

    LocalDateTime startDateTime = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime endDateTime = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am
    LocalDateTime startDateTime2 = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime endDateTime2 = LocalDateTime.of(2025, 3,
            7, 12, 30);//7am 12.30
    LocalDateTime startDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime endDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am


    LocalDateTime endDateTime4 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    cal.createEvent("Lecture 1", startDateTime, endDateTime,
            "DBMS", null, null, false);
    cal.createEvent("Lecture 2", startDateTime2, endDateTime2, "PDP",
            "NEU", null, false);
    cal.createEvent("Lecture 3", startDateTime3, endDateTime3, "AI",
            "NEU", null, false);

    //5march 11 am to 8 march12.30 event spanning for several days
    cal.createEvent("Lecture 4", startDateTime, endDateTime4, "CS",
            "NEU", null, false);
    String expected = "\"Lecture 2\",\"2025-03-07\",\"12:00\",\"2025-03-07\",\"12:30\",\"PDP\"," +
            "\"NEU\",\"\",\"False\"\n" +
            "\"Lecture 3\",\"2025-03-08\",\"12:00\",\"2025-03-08\",\"12:30\",\"AI\",\"NEU\",\"\"," +
            "\"False\"\n";

    //time frame--> 7march12am to 8march 12.30am
    System.out.println(cal.printEvents(LocalDateTime.of(date, startTime),
            LocalDateTime.of(date2, endTime)));
    assertEquals(expected,
            cal.printEvents(LocalDateTime.of(date, startTime), LocalDateTime.of(date2, endTime)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void printStartDateBeforeEndDate() { //start date cannot start before end date.
    LocalDate date = LocalDate.of(2025, 3, 7);//7march
    LocalDate date2 = LocalDate.of(2025, 3, 5);//5march
    LocalTime startTime = LocalTime.of(12, 0);//12:00am
    LocalTime endTime = LocalTime.of(12, 30);//12.15am

    LocalDateTime startDateTime = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime endDateTime = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am
    LocalDateTime startDateTime2 = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime endDateTime2 = LocalDateTime.of(2025, 3,
            7, 12, 30);//7am 12.30
    LocalDateTime startDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime endDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am


    LocalDateTime endDateTime4 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    cal.createEvent("Lecture 1", startDateTime, endDateTime,
            "DBMS", null, null, false);
    cal.createEvent("Lecture 2", startDateTime2, endDateTime2, "PDP",
            "NEU", null, false);
    cal.createEvent("Lecture 3", startDateTime3, endDateTime3, "AI",
            "NEU", null, false);

    //5march 11 am to 8 march12.30
    cal.createEvent("Lecture 4", startDateTime, endDateTime4, "CS",
            "NEU", null, false);
    String expected = "•Subject:Lecture 2 from 2025-03-07 12:00 to 2025-03-07 12:30" +
            " Description:PDP at NEU\n" +
            "•Subject:Lecture 3 from 2025-03-08 12:00 to 2025-03-08 12:30 Description:AI at NEU\n";

    //time frame--> 7march12am to 5march 12.15am
    System.out.println(cal.printEvents(LocalDateTime.of(date, startTime),
            LocalDateTime.of(date2, endTime)));
    assertEquals(expected,
            cal.printEvents(LocalDateTime.of(date, startTime), LocalDateTime.of(date2, endTime)));
  }

  @Test
  public void printEvents6() {
    LocalDate date = LocalDate.of(2025, 3, 5);//5march
    LocalDate date2 = LocalDate.of(2025, 3, 7);//7march
    LocalTime startTime = LocalTime.of(12, 0);//12:00am
    LocalTime endTime = LocalTime.of(12, 30);//12.15am

    LocalDateTime startDateTime = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime endDateTime = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am
    LocalDateTime startDateTime2 = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime endDateTime2 = LocalDateTime.of(2025, 3,
            7, 12, 30);//7am 12.30
    LocalDateTime startDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime endDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am


    LocalDateTime endDateTime4 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am


    cal.createEvent("Lecture 1", startDateTime, endDateTime,
            "DBMS", null, null, false);
    cal.createEvent("Lecture 2", startDateTime2, endDateTime2, "PDP",
            "NEU", null, false);
    cal.createEvent("Lecture 3", startDateTime3, endDateTime3, "AI",
            "NEU", null, false);

    //5march 11 am to 8 march12.30
    cal.createEvent("Lecture 4", startDateTime, endDateTime4, "CS",
            "NEU", null, false);
    String expected = "\"Lecture 2\",\"2025-03-07\",\"12:00\",\"2025-03-07\",\"12:30\",\"PDP\"," +
            "\"NEU\",\"\",\"False\"\n";


    //time frame--> 7march12am to 5march 12.15am
    System.out.println(cal.printEvents(LocalDateTime.of(date, startTime),
            LocalDateTime.of(date2, endTime)));
    assertEquals(expected,
            cal.printEvents(LocalDateTime.of(date, startTime), LocalDateTime.of(date2, endTime)));
  }

  @Test
  public void printEventsWhenEmptyCalendar() {

    assertEquals("", cal.printEvents(LocalDateTime.of(12, 3,
                    5, 12, 0, 0),
            LocalDateTime.of(12, 4, 6, 12, 1, 1)));
  }

  @Test
  public void printSingleRecurringEventsWithinRange() {

    LocalDateTime startDateTime = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime endDateTime = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am
    LocalDateTime startDateTime2 = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime endDateTime2 = LocalDateTime.of(2025, 3,
            7, 12, 30);//7am 12.30
    LocalDateTime startDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime endDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime endDateTime4 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime recurDateTime5 = LocalDateTime.of(2025, 3,
            10, 12, 30);//10 march 12.30pm

    LocalDateTime endDateTime5 = LocalDateTime.of(2025, 3,
            10, 13, 0);//10 march 1pm

    LocalDateTime specificEndDateTime6 = LocalDateTime.of(2025, 3,
            12, 13, 0);//12march1pm

    // 5march 11am to 5march 11.30
    cal.createEvent("Lecture 1", startDateTime, endDateTime,
            "DBMS", null, null, false);
    //7march 12am to 7march 12.30
    cal.createEvent("Lecture 2", startDateTime2, endDateTime2, "PDP",
            "NEU", null, false);
    //8march 12am to 8march 12.30
    cal.createEvent("Lecture 3", startDateTime3, endDateTime3, "AI",
            "NEU", null, false);

    //5march 11 am to 8march 12.30 --> event span for more than 1 day
    cal.createEvent("Lecture 4", startDateTime, endDateTime4, "CS",
            "NEU", null, false);

    //create recurring event 10 march 12am to 12.30am to 12march 1am
    cal.createRecurringEventWithSpecificEndDateTime("Recurring Event", recurDateTime5,
            endDateTime5, "Lectures", "NEU", true, "MTW",
            specificEndDateTime6);

    String expected = "\"Recurring Event\",\"2025-03-10\",\"12:30\",\"2025-03-10\",\"13:00\"," +
            "\"Lectures\",\"NEU\",\"True\",\"False\"\n" +
            "\"Recurring Event\",\"2025-03-11\",\"12:30\",\"2025-03-11\",\"13:00\",\"Lectures\"," +
            "\"NEU\",\"True\",\"False\"\n" +
            "\"Recurring Event\",\"2025-03-12\",\"12:30\",\"2025-03-12\",\"13:00\",\"Lectures\"," +
            "\"NEU\",\"True\",\"False\"\n";

    //Time frame 10march 12.30 to  12march 1pm
    assertEquals(expected, cal.printEvents(recurDateTime5, specificEndDateTime6));
  }

  @Test
  public void printSingleRecurringEventsWithinRange2() {
    LocalDate date = LocalDate.of(2025, 3, 5);//5march
    LocalDate date2 = LocalDate.of(2025, 3, 7);//7march
    LocalTime startTime = LocalTime.of(12, 0);//12:00am
    LocalTime endTime = LocalTime.of(12, 30);//12.15am

    LocalDateTime startDateTime = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime endDateTime = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am
    LocalDateTime startDateTime2 = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime endDateTime2 = LocalDateTime.of(2025, 3,
            7, 12, 30);//7am 12.30
    LocalDateTime startDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime endDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime endDateTime4 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime recurDateTime5 = LocalDateTime.of(2025, 3,
            10, 12, 30);//10 march 12.30pm

    LocalDateTime endDateTime5 = LocalDateTime.of(2025, 3,
            10, 13, 0);//10 march 1pm

    LocalDateTime specificEndDateTime6 = LocalDateTime.of(2025, 3,
            12, 13, 0);//12march1pm

    LocalDateTime elevenMarch = LocalDateTime.of(2025, 3,
            11, 13, 0);

    // 5march 11am to 5march 11.30
    cal.createEvent("Lecture 1", startDateTime, endDateTime,
            "DBMS", null, null, false);
    //7march 12am to 7march 12.30
    cal.createEvent("Lecture 2", startDateTime2, endDateTime2, "PDP",
            "NEU", null, false);
    //8march 12am to 8march 12.30
    cal.createEvent("Lecture 3", startDateTime3, endDateTime3, "AI",
            "NEU", null, false);

    //5march 11 am to 8march 12.30 --> event span for more than 1 day
    cal.createEvent("Lecture 4", startDateTime, endDateTime4, "CS",
            "NEU", null, false);

    //create recurring event 10 march 12am to 12.30am to 12march 1pm
    cal.createRecurringEventWithSpecificEndDateTime("Recurring Event",
            recurDateTime5, endDateTime5, "Lectures", "NEU",
            true, "MTW", specificEndDateTime6);

    String expected = "\"Lecture 2\",\"2025-03-07\",\"12:00\",\"2025-03-07\",\"12:30\",\"PDP\"," +
            "\"NEU\",\"\",\"False\"\n" +
            "\"Lecture 3\",\"2025-03-08\",\"12:00\",\"2025-03-08\",\"12:30\",\"AI\",\"NEU\",\"\"," +
            "\"False\"\n" +
            "\"Recurring Event\",\"2025-03-10\",\"12:30\",\"2025-03-10\",\"13:00\",\"Lectures\"," +
            "\"NEU\",\"True\",\"False\"\n" +
            "\"Recurring Event\",\"2025-03-11\",\"12:30\",\"2025-03-11\",\"13:00\",\"Lectures\"," +
            "\"NEU\",\"True\",\"False\"\n";
    //events from 7march 12pm to 11march 1 pm
    assertEquals(expected, cal.printEvents(startDateTime2, elevenMarch));
  }

  @Test(expected = IllegalArgumentException.class)
  public void printSingleRecurringEventsWithinRange3() {
    LocalDate date = LocalDate.of(2025, 3, 5);//5march
    LocalDate date2 = LocalDate.of(2025, 3, 7);//7march
    LocalTime startTime = LocalTime.of(12, 0);//12:00am
    LocalTime endTime = LocalTime.of(12, 30);//12.15am

    LocalDateTime startDateTime = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime endDateTime = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am
    LocalDateTime startDateTime2 = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime endDateTime2 = LocalDateTime.of(2025, 3,
            7, 12, 30);//7am 12.30
    LocalDateTime startDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime endDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime endDateTime4 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime recurDateTime5 = LocalDateTime.of(2025, 3,
            10, 12, 30);//10 march 12.30pm

    LocalDateTime endDateTime5 = LocalDateTime.of(2025, 3,
            10, 13, 0);//10 march 1pm

    LocalDateTime specificEndDateTime6 = LocalDateTime.of(2025, 3,
            12, 13, 0);//12march1pm

    LocalDateTime elevenMarch = LocalDateTime.of(2025, 3,
            11, 13, 0);

    // 5march 11am to 5march 11.30
    cal.createEvent("Lecture 1", startDateTime, endDateTime,
            "DBMS", null, null, false);
    //7march 12am to 7march 12.30
    cal.createEvent("Lecture 2", startDateTime2, endDateTime2, "PDP",
            "NEU", null, false);
    //8march 12am to 8march 12.30
    cal.createEvent("Lecture 3", startDateTime3, endDateTime3, "AI",
            "NEU", null, false);

    //5march 11 am to 8march 12.30 --> event span for more than 1 day
    cal.createEvent("Lecture 4", startDateTime, endDateTime4, "CS",
            "NEU", null, false);

    //create recurring event 10 march 12am to 12.30am to 12march 1pm
    cal.createRecurringEventWithSpecificEndDateTime("Recurring Event",
            recurDateTime5, endDateTime5, "Lectures", "NEU",
            true, "MTW", specificEndDateTime6);

    //8march to 10 march
    cal.printEvents(null, null);
  }

  @Test
  public void printSingleRecurringEventsWithinRange4() {

    LocalDateTime startDateTime = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime endDateTime = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am
    LocalDateTime startDateTime2 = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime endDateTime2 = LocalDateTime.of(2025, 3,
            7, 12, 30);//7am 12.30
    LocalDateTime startDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime endDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime endDateTime4 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime recurDateTime5 = LocalDateTime.of(2025, 3,
            10, 12, 30);//10 march 12.30pm

    LocalDateTime endDateTime5 = LocalDateTime.of(2025, 3,
            10, 13, 0);//10 march 1pm

    LocalDateTime specificEndDateTime6 = LocalDateTime.of(2025, 3,
            12, 13, 0);//12march1pm

    // 5march 11am to 5march 11.30
    cal.createEvent("Lecture 1", startDateTime, endDateTime,
            "DBMS", null, null, false);
    //7march 12am to 7march 12.30
    cal.createEvent("Lecture 2", startDateTime2, endDateTime2, "PDP",
            "NEU", null, false);
    //8march 12am to 8march 12.30
    cal.createEvent("Lecture 3", startDateTime3, endDateTime3, "AI",
            "NEU", null, false);

    //5march 11 am to 8march 12.30 --> event span for more than 1 day
    cal.createEvent("Lecture 4", startDateTime, endDateTime4, "CS",
            "NEU", null, false);

    //create recurring event 10 march 12am to 12.30am to 12march 1am
    cal.createRecurringEventWithSpecificEndDateTime("Recurring Event", recurDateTime5,
            endDateTime5, "Lectures", "NEU", true, "MTW",
            specificEndDateTime6);


    System.out.println(cal.printEvents(startDateTime3, recurDateTime5));
    String expected = "\"Lecture 3\",\"2025-03-08\",\"12:00\",\"2025-03-08\",\"12:30\",\"AI\"," +
            "\"NEU\",\"\",\"False\"\n";
    //Time frame 8march 12.30 to  10march 12.30pm
    assertEquals(expected, cal.printEvents(startDateTime3, recurDateTime5));
  }

  @Test
  public void testsPrintEventForRecurringEventWithinRange5() {
    LocalDate tenMarch = LocalDate.of(2025, 3, 10);
    LocalTime twelve35am = LocalTime.of(12, 35);
    LocalDateTime fiveMarch11am = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime fiveMarch1130am = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am
    LocalDateTime sevenMarch12am = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime sevenMarch1230am = LocalDateTime.of(2025, 3,
            7, 12, 30);//7am 12.30
    LocalDateTime eightMarch12am = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime eightMarch1230am = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am


    LocalDateTime tenMarch1230am = LocalDateTime.of(2025, 3,
            10, 12, 30);//10 march 12.30pm

    LocalDateTime tenMarch1300 = LocalDateTime.of(2025, 3,
            10, 13, 0);//10 march 1pm

    LocalDateTime twelveMarch1300 = LocalDateTime.of(2025, 3,
            12, 13, 0);//12march1pm

    LocalDateTime elevenMarch = LocalDateTime.of(2025, 3,
            11, 12, 30);

    // 5march 11am to 5march 11.30
    cal.createEvent("Lecture 1", fiveMarch11am, fiveMarch1130am,
            "DBMS", null, null, false);
    //7march 12am to 7march 12.30
    cal.createEvent("Lecture 2", sevenMarch12am, sevenMarch1230am, "PDP",
            "NEU", null, false);
    //8march 12am to 8march 12.30
    cal.createEvent("Lecture 3", eightMarch12am, eightMarch1230am, "AI",
            "NEU", null, false);

    //create recurring event 10 march  (12.30am to 1 pm) to 12march 1pm
    cal.createRecurringEventWithSpecificEndDateTime("Recurring Event",
            tenMarch1230am, tenMarch1300, "Lectures", "NEU", true,
            "MTW", twelveMarch1300);


    assertEquals("", cal.printEvents(LocalDateTime.of(tenMarch, twelve35am), tenMarch1300));
  }

  @Test
  public void testPrintEventsAllDayEventNotInRange() {
    LocalDateTime startDateTime = LocalDateTime.of(2025, 3, 5, 0,
            0);//5march 11am
    LocalDateTime endDateTime = LocalDateTime.of(2025, 3, 5,
            23, 59);//5march 11.30am
    LocalDateTime startDateTime2 = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime endDateTime2 = LocalDateTime.of(2025, 3,
            7, 12, 30);//7am 12.30
    LocalDateTime startDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime endDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime endDateTime4 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime recurDateTime5 = LocalDateTime.of(2025, 3,
            10, 12, 30);//10 march 12.30pm

    LocalDateTime endDateTime5 = LocalDateTime.of(2025, 3,
            10, 13, 0);//10 march 1pm

    LocalDateTime specificEndDateTime6 = LocalDateTime.of(2025, 3,
            12, 13, 0);//12march1pm

    // 5 march all day event start from 00:00
    cal.createEvent("Lecture 1", startDateTime, endDateTime,
            "DBMS", null, null, false);
    //7march 12am to 7march 12.30
    cal.createEvent("Lecture 2", startDateTime2, endDateTime2, null,
            "NEU", null, false);
    //8march 12am to 8march 12.30
    cal.createEvent("Lecture 3", startDateTime3, endDateTime3, "AI",
            null, true, false);

    //5march 11 pm to 8march 12.30 --> event span for more than 1 day
    cal.createEvent("Lecture 4", startDateTime, endDateTime4, "CS",
            "NEU", null, false);

    String expected = "\"Lecture 1\",\"2025-03-05\",\"00:00\",\"2025-03-05\",\"23:59\",\"DBMS\"," +
            "\"\",\"\",\"False\"\n" +
            "\"Lecture 4\",\"2025-03-05\",\"00:00\",\"2025-03-08\",\"12:30\",\"CS\",\"NEU\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 2\",\"2025-03-07\",\"12:00\",\"2025-03-07\",\"12:30\",\"\",\"NEU\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 3\",\"2025-03-08\",\"12:00\",\"2025-03-08\",\"12:30\",\"AI\",\"\"," +
            "\"True\",\"False\"\n";
    assertEquals(expected, cal.printEvents(startDateTime, endDateTime4));
  }

  @Test
  public void testPrintEventsSameStartDateTime() {
    LocalDateTime startDateTime = LocalDateTime.of(2025, 3, 5, 2,
            0);//5march 11am
    LocalDateTime endDateTime = LocalDateTime.of(2025, 3, 5,
            3, 0);//5march 11.30am
    LocalDateTime startDateTime2 = LocalDateTime.of(2025, 3, 5,
            2, 0);//7march 12am
    LocalDateTime endDateTime2 = LocalDateTime.of(2025, 3,
            5, 2, 30);//7am 12.30
    LocalDateTime startDateTime3 = LocalDateTime.of(2025, 3, 5,
            2, 0);//8 march 12am
    LocalDateTime endDateTime3 = LocalDateTime.of(2025, 3, 5,
            3, 0);//8 march 12.30am

    LocalDateTime endDateTime4 = LocalDateTime.of(2025, 3, 5,
            3, 0);//8 march 12.30am

    LocalDateTime recurDateTime5 = LocalDateTime.of(2025, 3,
            10, 12, 30);//10 march 12.30pm

    LocalDateTime endDateTime5 = LocalDateTime.of(2025, 3,
            10, 13, 0);//10 march 1pm

    LocalDateTime specificEndDateTime6 = LocalDateTime.of(2025, 3,
            12, 13, 0);//12march1pm

    // 5 march all day event start from 00:00
    cal.createEvent("Lecture 1", startDateTime, endDateTime,
            "DBMS", null, null, false);
    //7march 12am to 7march 12.30
    cal.createEvent("Lecture 2", startDateTime2, endDateTime2, null,
            "NEU", null, false);
    //8march 12am to 8march 12.30
    cal.createEvent("Lecture 3", startDateTime3, endDateTime3, "AI",
            null, true, false);

    //5march 11 pm to 8march 12.30 --> event span for more than 1 day
    cal.createEvent("Lecture 0", startDateTime, endDateTime4, "CS",
            "NEU", null, false);

    String expected = "\"Lecture 2\",\"2025-03-05\",\"02:00\",\"2025-03-05\",\"02:30\",\"\"," +
            "\"NEU\",\"\",\"False\"\n" +
            "\"Lecture 0\",\"2025-03-05\",\"02:00\",\"2025-03-05\",\"03:00\",\"CS\",\"NEU\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 1\",\"2025-03-05\",\"02:00\",\"2025-03-05\",\"03:00\",\"DBMS\",\"\",\"\"," +
            "\"False\"\n" +
            "\"Lecture 3\",\"2025-03-05\",\"02:00\",\"2025-03-05\",\"03:00\",\"AI\",\"\"," +
            "\"True\",\"False\"\n";
    assertEquals(expected, cal.printEvents(startDateTime, endDateTime4));
  }

  @Test
  public void showStatusForEmptyEvent() {
    assertEquals("Available", cal.showStatus(LocalDateTime.of(2025,
            3, 5, 12, 30, 45)));
  }

  @Test
  public void showStatus1() {
    LocalDate date = LocalDate.of(2025, 3, 5);//5march
    LocalTime time = LocalTime.of(11, 0);//11am

    LocalDateTime startDateTime = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime endDateTime = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am

    LocalDateTime startDateTime2 = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime endDateTime2 = LocalDateTime.of(2025, 3,
            7, 12, 30);//7march 12.30

    LocalDateTime startDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime endDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime endDateTime4 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    //Lecture1 5march 11 am to 5march11.30
    cal.createEvent("Lecture 1", startDateTime, endDateTime,
            "DBMS", null, null, false);

    //Lecture2 7march 12am to 7 match 12.30
    cal.createEvent("Lecture 2", startDateTime2, endDateTime2, "PDP",
            "NEU", null, false);

    //Lecture3 8march 12am to 8march 12.30 Lecture3
    cal.createEvent("Lecture 3", startDateTime3, endDateTime3, "AI",
            "NEU", null, false);

    //Lecture 4 5march 11 am to 8 march12.30 Lecture4
    cal.createEvent("Lecture 4", startDateTime, endDateTime4, "CS",
            "NEU", null, false);
    assertEquals("Busy", cal.showStatus(LocalDateTime.of(date, time)));//5march 11am

  }

  @Test
  public void showStatus2() {

    LocalDate date3 = LocalDate.of(2025, 3, 8);//8march
    LocalTime endTime2 = LocalTime.of(12, 30);//12.30am
    LocalDateTime startDateTime = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime endDateTime = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am

    LocalDateTime startDateTime2 = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime endDateTime2 = LocalDateTime.of(2025, 3,
            7, 12, 30);//7march 12.30

    LocalDateTime startDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime endDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime endDateTime4 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    //Lecture1 5march 11 am to 5march11.30
    cal.createEvent("Lecture 1", startDateTime, endDateTime,
            "DBMS", null, null, false);

    //Lecture2 7march 12am to 7 match 12.30
    cal.createEvent("Lecture 2", startDateTime2, endDateTime2, "PDP",
            "NEU", null, false);

    //Lecture3 8march 12am to 8march 12.30 Lecture3
    cal.createEvent("Lecture 3", startDateTime3, endDateTime3, "AI",
            "NEU", null, false);

    //Lecture 4 5march 11 am to 8 march12.30 Lecture4
    cal.createEvent("Lecture 4", startDateTime, endDateTime4, "CS",
            "NEU", null, false);

    assertEquals("Available", cal.showStatus(LocalDateTime.of(date3, endTime2)));
    //8march 12.30am

  }

  @Test
  public void showStatusInRecurringEvent() {

    LocalDateTime startDateTime = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime endDateTime = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am
    LocalDateTime startDateTime2 = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime endDateTime2 = LocalDateTime.of(2025, 3,
            7, 12, 30);//7am 12.30
    LocalDateTime startDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime endDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime endDateTime4 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime recurDateTime5 = LocalDateTime.of(2025, 3,
            10, 12, 30);//10 march 12.30pm

    LocalDateTime endDateTime5 = LocalDateTime.of(2025, 3,
            10, 13, 0);//10 march 1pm

    LocalDateTime specificEndDateTime6 = LocalDateTime.of(2025, 3,
            12, 13, 0);//12march1pm

    LocalDateTime elevenMarch = LocalDateTime.of(2025, 3,
            11, 12, 59);

    // 5march 11am to 5march 11.30
    cal.createEvent("Lecture 1", startDateTime, endDateTime,
            "DBMS", null, null, false);
    //7march 12am to 7march 12.30
    cal.createEvent("Lecture 2", startDateTime2, endDateTime2, "PDP",
            "NEU", null, false);
    //8march 12am to 8march 12.30
    cal.createEvent("Lecture 3", startDateTime3, endDateTime3, "AI",
            "NEU", null, false);

    //5march 11 am to 8march 12.30 --> event span for more than 1 day
    cal.createEvent("Lecture 4", startDateTime, endDateTime4, "CS",
            "NEU", null, false);

    //create recurring event 10 march 12am to 12.30am to 12march 1pm
    cal.createRecurringEventWithSpecificEndDateTime("Recurring Event", recurDateTime5,
            endDateTime5, "Lectures", "NEU", true, "MTW",
            specificEndDateTime6);

    //11 march 12:59
    assertEquals("Busy", cal.showStatus(LocalDateTime.of(elevenMarch.toLocalDate(),
            elevenMarch.toLocalTime())));
  }

  @Test
  public void showStatusInRecurringEvent2() {

    LocalDateTime startDateTime = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime endDateTime = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am
    LocalDateTime startDateTime2 = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime endDateTime2 = LocalDateTime.of(2025, 3,
            7, 12, 30);//7am 12.30
    LocalDateTime startDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime endDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime endDateTime4 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime recurDateTime5 = LocalDateTime.of(2025, 3,
            10, 12, 30);//10 march 12.30pm

    LocalDateTime endDateTime5 = LocalDateTime.of(2025, 3,
            10, 13, 0);//10 march 1pm

    LocalDateTime specificEndDateTime6 = LocalDateTime.of(2025, 3,
            12, 13, 0);//12march1pm

    LocalDateTime elevenMarch = LocalDateTime.of(2025, 3,
            11, 13, 0);

    // 5march 11am to 5march 11.30
    cal.createEvent("Lecture 1", startDateTime, endDateTime,
            "DBMS", null, null, false);
    //7march 12am to 7march 12.30
    cal.createEvent("Lecture 2", startDateTime2, endDateTime2, "PDP",
            "NEU", null, false);
    //8march 12am to 8march 12.30
    cal.createEvent("Lecture 3", startDateTime3, endDateTime3, "AI",
            "NEU", null, false);

    //5march 11 am to 8march 12.30 --> event span for more than 1 day
    cal.createEvent("Lecture 4", startDateTime, endDateTime4, "CS",
            "NEU", null, false);

    //create recurring event 10 march 12am to 13.00 to 12march 1pm
    cal.createRecurringEventWithSpecificEndDateTime("Recurring Event",
            recurDateTime5, endDateTime5, "Lectures", "NEU", true,
            "MTW", specificEndDateTime6);

    //11 march 13:00
    assertEquals("Available", cal.showStatus(LocalDateTime.of(elevenMarch.toLocalDate(),
            elevenMarch.toLocalTime())));
  }

  @Test
  public void showStatusRecurringEvent3() {
    LocalDateTime startDateTime = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime endDateTime = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am
    LocalDateTime startDateTime2 = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime endDateTime2 = LocalDateTime.of(2025, 3,
            7, 12, 30);//7am 12.30
    LocalDateTime startDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime endDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime endDateTime4 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime recurDateTime5 = LocalDateTime.of(2025, 3,
            10, 12, 30);//10 march 12.30pm

    LocalDateTime endDateTime5 = LocalDateTime.of(2025, 3,
            10, 13, 0);//10 march 1pm

    LocalDateTime specificEndDateTime6 = LocalDateTime.of(2025, 3,
            12, 13, 0);//12march1pm

    LocalDateTime elevenMarch = LocalDateTime.of(2025, 3,
            11, 12, 30);

    // 5march 11am to 5march 11.30
    cal.createEvent("Lecture 1", startDateTime, endDateTime,
            "DBMS", null, null, false);
    //7march 12am to 7march 12.30
    cal.createEvent("Lecture 2", startDateTime2, endDateTime2, "PDP",
            "NEU", null, false);
    //8march 12am to 8march 12.30
    cal.createEvent("Lecture 3", startDateTime3, endDateTime3, "AI",
            "NEU", null, false);

    //5march 11 am to 8march 12.30 --> event span for more than 1 day
    cal.createEvent("Lecture 4", startDateTime, endDateTime4, "CS",
            "NEU", null, false);

    //create recurring event 10 march  (12.30am to 1 pm) to 12march 1pm
    cal.createRecurringEventWithSpecificEndDateTime("Recurring Event", recurDateTime5,
            endDateTime5, "Lectures", "NEU", true, "MTW",
            specificEndDateTime6);

    //11 march 12:30
    assertEquals("Busy", cal.showStatus(LocalDateTime.of(elevenMarch.toLocalDate(),
            elevenMarch.toLocalTime())));
  }

  @Test(expected = IllegalArgumentException.class)
  public void showStatusForNullDate() {

    LocalDateTime startDateTime = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime endDateTime = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am
    LocalDateTime startDateTime2 = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime endDateTime2 = LocalDateTime.of(2025, 3,
            7, 12, 30);//7am 12.30
    LocalDateTime startDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime endDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime endDateTime4 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime recurDateTime5 = LocalDateTime.of(2025, 3,
            10, 12, 30);//10 march 12.30pm

    LocalDateTime endDateTime5 = LocalDateTime.of(2025, 3,
            10, 13, 0);//10 march 1pm

    LocalDateTime specificEndDateTime6 = LocalDateTime.of(2025, 3,
            12, 13, 0);//12march1pm

    LocalDateTime elevenMarch = LocalDateTime.of(2025, 3,
            11, 13, 0);

    // 5march 11am to 5march 11.30
    cal.createEvent("Lecture 1", startDateTime, endDateTime,
            "DBMS", null, null, false);
    //7march 12am to 7march 12.30
    cal.createEvent("Lecture 2", startDateTime2, endDateTime2, "PDP",
            "NEU", null, false);
    //8march 12am to 8march 12.30
    cal.createEvent("Lecture 3", startDateTime3, endDateTime3, "AI",
            "NEU", null, false);

    //5march 11 am to 8march 12.30 --> event span for more than 1 day
    cal.createEvent("Lecture 4", startDateTime, endDateTime4, "CS",
            "NEU", null, false);

    //create recurring event 10 march 12am to 12.30am to 12march 1pm
    cal.createRecurringEventWithSpecificEndDateTime("Recurring Event", recurDateTime5,
            endDateTime5, "Lectures", "NEU", true, "MTW",
            specificEndDateTime6);


    cal.showStatus(null);
  }

  @Test
  public void showStatusRecurringEvent5() {
    LocalDateTime startDateTime = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime endDateTime = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am
    LocalDateTime startDateTime2 = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime endDateTime2 = LocalDateTime.of(2025, 3,
            7, 12, 30);//7am 12.30
    LocalDateTime startDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime endDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime endDateTime4 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime recurDateTime5 = LocalDateTime.of(2025, 3,
            10, 12, 30);//10 march 12.30pm

    LocalDateTime endDateTime5 = LocalDateTime.of(2025, 3,
            10, 13, 0);//10 march 1pm

    LocalDateTime specificEndDateTime6 = LocalDateTime.of(2025, 3,
            12, 13, 0);//12march1pm

    LocalDateTime elevenMarch = LocalDateTime.of(2025, 3,
            11, 12, 30);

    // 5march 11am to 5march 11.30
    cal.createEvent("Lecture 1", startDateTime, endDateTime,
            "DBMS", null, null, false);
    //7march 12am to 7march 12.30
    cal.createEvent("Lecture 2", startDateTime2, endDateTime2, "PDP",
            "NEU", null, false);
    //8march 12am to 8march 12.30
    cal.createEvent("Lecture 3", startDateTime3, endDateTime3, "AI",
            "NEU", null, false);

    //5march 11 am to 8march 12.30 --> event span for more than 1 day
    cal.createEvent("Lecture 4", startDateTime, endDateTime4, "CS",
            "NEU", null, false);

    //create recurring event 10 march  (12.30am to 1 pm) to 12march 1pm
    cal.createRecurringEventWithSpecificEndDateTime("Recurring Event", recurDateTime5,
            endDateTime5, "Lectures", "NEU", true, "MTW",
            specificEndDateTime6);

    //10 march 12:30
    assertEquals("Busy", cal.showStatus(LocalDateTime.of(recurDateTime5.toLocalDate(),
            recurDateTime5.toLocalTime())));
  }

  @Test
  public void showStatusRecurringEvent6() {
    LocalDateTime startDateTime = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime endDateTime = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am
    LocalDateTime startDateTime2 = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime endDateTime2 = LocalDateTime.of(2025, 3,
            7, 12, 30);//7am 12.30
    LocalDateTime startDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime endDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime endDateTime4 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime recurDateTime5 = LocalDateTime.of(2025, 3,
            10, 12, 30);//10 march 12.30pm

    LocalDateTime endDateTime5 = LocalDateTime.of(2025, 3,
            10, 13, 0);//10 march 1pm

    LocalDateTime specificEndDateTime6 = LocalDateTime.of(2025, 3,
            12, 13, 0);//12march1pm

    LocalDateTime elevenMarch = LocalDateTime.of(2025, 3,
            11, 12, 30);

    // 5march 11am to 5march 11.30
    cal.createEvent("Lecture 1", startDateTime, endDateTime,
            "DBMS", null, null, false);
    //7march 12am to 7march 12.30
    cal.createEvent("Lecture 2", startDateTime2, endDateTime2, "PDP",
            "NEU", null, false);
    //8march 12am to 8march 12.30
    cal.createEvent("Lecture 3", startDateTime3, endDateTime3, "AI",
            "NEU", null, false);

    //5march 11 am to 8march 12.30 --> event span for more than 1 day
    cal.createEvent("Lecture 4", startDateTime, endDateTime4, "CS",
            "NEU", null, false);

    //create recurring event 10 march  (12.30am to 1 pm) to 12march 1pm
    cal.createRecurringEventWithSpecificEndDateTime("Recurring Event", recurDateTime5,
            endDateTime5, "Lectures", "NEU", true, "MTW",
            specificEndDateTime6);

    //12 march 12:30
    assertEquals("Busy", cal.showStatus(LocalDateTime.of(specificEndDateTime6.toLocalDate(),
            recurDateTime5.toLocalTime())));
  }

  @Test
  public void showStatusRecurringEvent7() {
    LocalDateTime startDateTime = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime endDateTime = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am
    LocalDateTime startDateTime2 = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime endDateTime2 = LocalDateTime.of(2025, 3,
            7, 12, 30);//7am 12.30
    LocalDateTime startDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime endDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime endDateTime4 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime recurDateTime5 = LocalDateTime.of(2025, 3,
            10, 12, 30);//10 march 12.30pm

    LocalDateTime endDateTime5 = LocalDateTime.of(2025, 3,
            10, 13, 0);//10 march 1pm

    LocalDateTime specificEndDateTime6 = LocalDateTime.of(2025, 3,
            12, 13, 0);//12march1pm

    LocalDateTime elevenMarch = LocalDateTime.of(2025, 3,
            11, 12, 30);

    // 5march 11am to 5march 11.30
    cal.createEvent("Lecture 1", startDateTime, endDateTime,
            "DBMS", null, null, false);
    //7march 12am to 7march 12.30
    cal.createEvent("Lecture 2", startDateTime2, endDateTime2, "PDP",
            "NEU", null, false);
    //8march 12am to 8march 12.30
    cal.createEvent("Lecture 3", startDateTime3, endDateTime3, "AI",
            "NEU", null, false);

    //5march 11 am to 8march 12.30 --> event span for more than 1 day
    cal.createEvent("Lecture 4", startDateTime, endDateTime4, "CS",
            "NEU", null, false);

    //create recurring event 10 march  (12.30am to 1 pm) to 12march 1pm
    cal.createRecurringEventWithSpecificEndDateTime("Recurring Event", recurDateTime5,
            endDateTime5, "Lectures", "NEU", true, "MTW",
            specificEndDateTime6);

    //12 march 13:00
    assertEquals("Available", cal.showStatus(LocalDateTime.of(specificEndDateTime6.toLocalDate(),
            specificEndDateTime6.toLocalTime())));
  }

  @Test
  public void showStatusRecurringEvent8() {

    LocalDate fourMarch = LocalDate.of(2025, 3, 4);
    LocalTime eleven30am = LocalTime.of(11, 30);
    LocalDateTime fiveMarch11am = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime fiveMarch1130am = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am
    LocalDateTime sevenMarch12am = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime sevenMarch1230am = LocalDateTime.of(2025, 3,
            7, 12, 30);//7am 12.30
    LocalDateTime eightMarch12am = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime eightMarch1230am = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am


    LocalDateTime tenMarch1230am = LocalDateTime.of(2025, 3,
            10, 12, 30);//10 march 12.30pm

    LocalDateTime tenMarch1300 = LocalDateTime.of(2025, 3,
            10, 13, 0);//10 march 1pm

    LocalDateTime twelveMarch1300 = LocalDateTime.of(2025, 3,
            12, 13, 0);//12march1pm

    LocalDateTime elevenMarch = LocalDateTime.of(2025, 3,
            11, 12, 30);

    // 5march 11am to 5march 11.30
    cal.createEvent("Lecture 1", fiveMarch11am, fiveMarch1130am,
            "DBMS", null, null, false);
    //7march 12am to 7march 12.30
    cal.createEvent("Lecture 2", sevenMarch12am, sevenMarch1230am, "PDP",
            "NEU", null, false);
    //8march 12am to 8march 12.30
    cal.createEvent("Lecture 3", eightMarch12am, eightMarch1230am, "AI",
            "NEU", null, false);

    //5march 11 am to 8march 12.30 --> event span for more than 1 day
    cal.createEvent("Lecture 4", fiveMarch11am, eightMarch1230am, "CS",
            "NEU", null, false);

    //create recurring event 10 march  (12.30am to 1 pm) to 12march 1pm
    cal.createRecurringEventWithSpecificEndDateTime("Recurring Event",
            tenMarch1230am, tenMarch1300, "Lectures", "NEU", true,
            "MTW", twelveMarch1300);

    assertEquals("Available", cal.showStatus(LocalDateTime.of(fourMarch, eleven30am)));
    //startDateTime equal

  }

  @Test
  public void showStatusRecurringEvent9() {
    LocalDate tenMarch = LocalDate.of(2025, 3, 10);
    LocalTime twelve35am = LocalTime.of(12, 35);
    LocalDateTime fiveMarch11am = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime fiveMarch1130am = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am
    LocalDateTime sevenMarch12am = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime sevenMarch1230am = LocalDateTime.of(2025, 3,
            7, 12, 30);//7am 12.30
    LocalDateTime eightMarch12am = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime eightMarch1230am = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am


    LocalDateTime tenMarch1230am = LocalDateTime.of(2025, 3,
            10, 12, 30);//10 march 12.30pm

    LocalDateTime tenMarch1300 = LocalDateTime.of(2025, 3,
            10, 13, 0);//10 march 1pm

    LocalDateTime twelveMarch1300 = LocalDateTime.of(2025, 3,
            12, 13, 0);//12march1pm

    LocalDateTime elevenMarch = LocalDateTime.of(2025, 3,
            11, 12, 30);

    // 5march 11am to 5march 11.30
    cal.createEvent("Lecture 1", fiveMarch11am, fiveMarch1130am,
            "DBMS", null, null, false);
    //7march 12am to 7march 12.30
    cal.createEvent("Lecture 2", sevenMarch12am, sevenMarch1230am, "PDP",
            "NEU", null, false);
    //8march 12am to 8march 12.30
    cal.createEvent("Lecture 3", eightMarch12am, eightMarch1230am, "AI",
            "NEU", null, false);

    //5march 11 am to 8march 12.30 --> event span for more than 1 day
    cal.createEvent("Lecture 4", fiveMarch11am, eightMarch1230am, "CS",
            "NEU", null, false);

    //create recurring event 10 march  (12.30am to 1 pm) to 12march 1pm
    cal.createRecurringEventWithSpecificEndDateTime("Recurring Event", tenMarch1230am,
            tenMarch1300, "Lectures", "NEU", true, "MTW",
            twelveMarch1300);

    assertEquals("Busy", cal.showStatus(LocalDateTime.of(tenMarch, twelve35am)));
  }

  @Test
  public void showStatusRecurringEvent10() {
    LocalDate tenMarch = LocalDate.of(2025, 3, 10);
    LocalTime twelve29am = LocalTime.of(12, 29);
    LocalDateTime fiveMarch11am = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime fiveMarch1130am = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am
    LocalDateTime sevenMarch12am = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime sevenMarch1230am = LocalDateTime.of(2025, 3,
            7, 12, 30);//7am 12.30
    LocalDateTime eightMarch12am = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime eightMarch1230am = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am


    LocalDateTime tenMarch1230am = LocalDateTime.of(2025, 3,
            10, 12, 30);//10 march 12.30pm

    LocalDateTime tenMarch1300 = LocalDateTime.of(2025, 3,
            10, 13, 0);//10 march 1pm

    LocalDateTime twelveMarch1300 = LocalDateTime.of(2025, 3,
            12, 13, 0);//12march1pm

    LocalDateTime elevenMarch = LocalDateTime.of(2025, 3,
            11, 12, 30);

    // 5march 11am to 5march 11.30
    cal.createEvent("Lecture 1", fiveMarch11am, fiveMarch1130am,
            "DBMS", null, null, false);
    //7march 12am to 7march 12.30
    cal.createEvent("Lecture 2", sevenMarch12am, sevenMarch1230am, "PDP",
            "NEU", null, false);
    //8march 12am to 8march 12.30
    cal.createEvent("Lecture 3", eightMarch12am, eightMarch1230am, "AI",
            "NEU", null, false);

    //5march 11 am to 8march 12.30 --> event span for more than 1 day
    cal.createEvent("Lecture 4", fiveMarch11am, eightMarch1230am, "CS",
            "NEU", null, false);

    //create recurring event 10 march  (12.30am to 1 pm) to 12march 1pm
    cal.createRecurringEventWithSpecificEndDateTime("Recurring Event", tenMarch1230am,
            tenMarch1300, "Lectures", "NEU", true, "MTW",
            twelveMarch1300);

    assertEquals("Available", cal.showStatus(LocalDateTime.of(tenMarch, twelve29am)));
  }

  @Test
  public void showStatusRecurringEvent11() {
    LocalDate nineMarch = LocalDate.of(2025, 3, 9);
    LocalTime twelve35am = LocalTime.of(12, 35);
    LocalDateTime fiveMarch11am = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime fiveMarch1130am = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am
    LocalDateTime sevenMarch12am = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime sevenMarch1230am = LocalDateTime.of(2025, 3,
            7, 12, 30);//7am 12.30
    LocalDateTime eightMarch12am = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime eightMarch1230am = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am


    LocalDateTime tenMarch1230am = LocalDateTime.of(2025, 3,
            10, 12, 30);//10 march 12.30pm

    LocalDateTime tenMarch1300 = LocalDateTime.of(2025, 3,
            10, 13, 0);//10 march 1pm

    LocalDateTime twelveMarch1300 = LocalDateTime.of(2025, 3,
            12, 13, 0);//12march1pm

    LocalDateTime elevenMarch = LocalDateTime.of(2025, 3,
            11, 12, 30);

    // 5march 11am to 5march 11.30
    cal.createEvent("Lecture 1", fiveMarch11am, fiveMarch1130am,
            "DBMS", null, null, false);
    //7march 12am to 7march 12.30
    cal.createEvent("Lecture 2", sevenMarch12am, sevenMarch1230am, "PDP",
            "NEU", null, false);
    //8march 12am to 8march 12.30
    cal.createEvent("Lecture 3", eightMarch12am, eightMarch1230am, "AI",
            "NEU", null, false);

    //5march 11 am to 8march 12.30 --> event span for more than 1 day
    cal.createEvent("Lecture 4", fiveMarch11am, eightMarch1230am, "CS",
            "NEU", null, false);

    //create recurring event 10 march  (12.30am to 1 pm) to 12march 1pm
    cal.createRecurringEventWithSpecificEndDateTime("Recurring Event", tenMarch1230am,
            tenMarch1300, "Lectures", "NEU", true, "MTW",
            twelveMarch1300);

    assertEquals("Available", cal.showStatus(LocalDateTime.of(nineMarch, twelve35am)));
  }

  @Test
  public void showStatusForSingleEvent() {
    LocalDate eightMarch = LocalDate.of(2025, 3, 8);
    LocalTime twelve10am = LocalTime.of(12, 10);
    LocalDateTime fiveMarch11am = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime fiveMarch1130am = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am
    LocalDateTime sevenMarch12am = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime sevenMarch1230am = LocalDateTime.of(2025, 3,
            7, 12, 30);//7am 12.30
    LocalDateTime eightMarch12am = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime eightMarch1230am = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am


    LocalDateTime tenMarch1230am = LocalDateTime.of(2025, 3,
            10, 12, 30);//10 march 12.30pm

    LocalDateTime tenMarch1300 = LocalDateTime.of(2025, 3,
            10, 13, 0);//10 march 1pm

    LocalDateTime twelveMarch1300 = LocalDateTime.of(2025, 3,
            12, 13, 0);//12march1pm

    LocalDateTime elevenMarch = LocalDateTime.of(2025, 3,
            11, 12, 30);

    // 5march 11am to 5march 11.30
    cal.createEvent("Lecture 1", fiveMarch11am, fiveMarch1130am,
            "DBMS", null, null, false);
    //7march 12am to 7march 12.30
    cal.createEvent("Lecture 2", sevenMarch12am, sevenMarch1230am, "PDP",
            "NEU", null, false);
    //8march 12am to 8march 12.30
    cal.createEvent("Lecture 3", eightMarch12am, eightMarch1230am, "AI",
            "NEU", null, false);

    //create recurring event 10 march  (12.30am to 1 pm) to 12march 1pm
    cal.createRecurringEventWithSpecificEndDateTime("Recurring Event", tenMarch1230am,
            tenMarch1300, "Lectures", "NEU", true, "MTW",
            twelveMarch1300);

    assertEquals("Busy", cal.showStatus(LocalDateTime.of(eightMarch, twelve10am)));
  }


  @Test
  public void testExportEvents() {

    LocalDateTime startDateTime = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime endDateTime = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am

    LocalDateTime startDateTime2 = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime endDateTime2 = LocalDateTime.of(2025, 3,
            7, 12, 30);//7march 12.30

    LocalDateTime startDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime endDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime endDateTime4 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    //Lecture1 5march 11 am to 5march11.30
    cal.createEvent("Lecture 1", startDateTime, endDateTime,
            "DBMS", null, null, false);

    //Lecture2 7march 12am to 7 match 12.30
    cal.createEvent("Lecture 2", startDateTime2, endDateTime2, "PDP",
            "NEU", null, false);

    //Lecture3 8march 12am to 8march 12.30 Lecture3
    cal.createEvent("Lecture 3", startDateTime3, endDateTime3, "AI",
            "NEU", null, false);

    //Lecture 4 5march 11 am to 8 march12.30 Lecture4
    cal.createEvent("Lecture 4", startDateTime, endDateTime4, "CS",
            "NEU", null, false);
    List<List<String>> data = cal.exportEvents();
    String events = "";
    for (List<String> datum : data) {
      for (String s : datum) {
        events += s;
      }
    }
    assertEquals("SubjectStart DateStart TimeEnd DateEnd " +
            "TimeDescriptionLocationPrivateAll" +
            " Day EventLecture 12025-03-0511:002025-03-0511:30DBMSFalseLecture " +
            "42025-03-0511:002025-03-0812:30CSNEUFalseLecture " +
            "22025-03-0712:002025-03-0712:30PDPNEUFalseLecture " +
            "32025-03-0812:002025-03-0812:30AINEUFalse", events);


  }


  @Test
  public void testExportRecurringEvents() {

    LocalDateTime startDateTime = LocalDateTime.of(2025, 3, 5, 11,
            0);//5march 11am
    LocalDateTime endDateTime = LocalDateTime.of(2025, 3, 5,
            11, 30);//5march 11.30am
    LocalDateTime startDateTime2 = LocalDateTime.of(2025, 3, 7,
            12, 0);//7march 12am
    LocalDateTime endDateTime2 = LocalDateTime.of(2025, 3,
            7, 12, 30);//7am 12.30
    LocalDateTime startDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 0);//8 march 12am
    LocalDateTime endDateTime3 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime endDateTime4 = LocalDateTime.of(2025, 3, 8,
            12, 30);//8 march 12.30am

    LocalDateTime recurDateTime5 = LocalDateTime.of(2025, 3,
            10, 12, 30);//10 march 12.30pm

    LocalDateTime endDateTime5 = LocalDateTime.of(2025, 3,
            10, 13, 0);//10 march 1pm

    LocalDateTime specificEndDateTime6 = LocalDateTime.of(2025, 3,
            12, 13, 0);//12march1pm

    LocalDateTime elevenMarch = LocalDateTime.of(2025, 3,
            11, 13, 0);

    // 5march 11am to 5march 11.30
    cal.createEvent("Lecture 1", startDateTime, endDateTime,
            "DBMS", null, null, false);
    //7march 12am to 7march 12.30
    cal.createEvent("Lecture 2", startDateTime2, endDateTime2, "PDP",
            "NEU", null, false);
    //8march 12am to 8march 12.30
    cal.createEvent("Lecture 3", startDateTime3, endDateTime3, "AI",
            "NEU", null, false);

    //5march 11 am to 8march 12.30 --> event span for more than 1 day
    cal.createEvent("Lecture 4", startDateTime, endDateTime4, "CS",
            "NEU", null, false);

    //create recurring event 10 march 12am to 12.30am to 12march 1pm
    cal.createRecurringEventWithSpecificEndDateTime("Recurring Event", recurDateTime5,
            endDateTime5, "Lectures", "NEU", true, "MTW",
            specificEndDateTime6);

    List<List<String>> data = cal.exportEvents();
    String events = "";
    for (List<String> datum : data) {
      for (String s : datum) {
        events += s;
      }
    }
    assertEquals("SubjectStart DateStart TimeEnd DateEnd " +
            "TimeDescriptionLocationPrivateAll Day EventLecture 12025-03-0511:002025-03-0511:30" +
            "DBMSFalseLecture 42025-03-0511:002025-03-0812:30CSNEUFalseLecture " +
            "22025-03-0712:002025-03-0712:30PDPNEUFalseLecture " +
            "32025-03-0812:002025-03-0812:30AINEU" +
            "FalseRecurring Event2025-03-1012:302025-03-1013:00LecturesNEUTrueFalseRecurring " +
            "Event2025-03-1112:302025-03-1113:00LecturesNEUTrueFalseRecurring " +
            "Event2025-03-1212:302025-03-1213:00LecturesNEUTrueFalse", events);
  }

  @Test
  public void exportEmptyEvents() {
    String output = "";
    try {
      List<List<String>> data = cal.exportEvents();
      String events = "";
      for (List<String> datum : data) {
        for (String s : datum) {
          events += s;
        }
      }
    } catch (Exception e) {
      output = e.getMessage();
    }

    assertEquals("No events found", output);
  }


  @Test
  public void testEditSetTimeZoneSuccess() {
    LocalDate date = LocalDate.of(2025, 3, 5);
    LocalTime time = LocalTime.MIN;
    LocalTime endTime = (LocalTime.MAX);
    LocalDateTime dateTime = LocalDateTime.of(date, time);
    cal2.createEvent("abc", LocalDateTime.of(date, time), LocalDateTime.of(date, endTime),
            "DBMS", null, null, false);

    TimeZone tz = TimeZone.getTimeZone("Asia/Kolkata");
    cal2.setTimeZone(tz);
    assertEquals("\"abc\",\"2025-03-05\",\"04:30\",\"2025-03-06\",\"04:29\",\"DBMS\",\"\",\"\"," +
            "\"False\"\n",
            cal2.printEvents(dateTime.minusDays(3), dateTime.plusDays(3)));
    assertEquals("Asia/Kolkata", cal2.getTimeZone().getID());

  }

  @Test
  public void testEditSetTimeZoneRecurringEventSuccessSpecificEndDateTime() {
    LocalDate date = LocalDate.of(2025, 3, 5);
    LocalTime time = LocalTime.MIN;
    LocalTime endTime = (LocalTime.MAX);
    LocalDateTime dateTime = LocalDateTime.of(date, time);
    LocalDateTime recurDateTime5 = LocalDateTime.of(2025, 3,
            10, 12, 30);//10 march 12.30pm

    LocalDateTime endDateTime5 = LocalDateTime.of(2025, 3,
            10, 13, 0);//10 march 1pm

    LocalDateTime specificEndDateTime6 = LocalDateTime.of(2025, 3,
            12, 13, 0);//12march1pm
    cal2.createEvent("abc", LocalDateTime.of(date, time), LocalDateTime.of(date, endTime),
            "DBMS", null, null, false);
    cal2.createRecurringEventWithSpecificEndDateTime("Recurring Event", recurDateTime5,
            endDateTime5, "Lectures", "NEU", true, "MTW",
            specificEndDateTime6);
    TimeZone tz = TimeZone.getTimeZone("Asia/Kolkata");
    cal2.setTimeZone(tz);
    assertEquals("\"abc\",\"2025-03-05\",\"04:30\",\"2025-03-06\",\"04:29\",\"DBMS\",\"\",\"\"," +
                    "\"False\"\n" +
                    "\"Recurring Event\",\"2025-03-10\",\"17:00\",\"2025-03-10\",\"17:30\"," +
                    "\"Lectures\",\"NEU\",\"True\",\"False\"\n" +
                    "\"Recurring Event\",\"2025-03-11\",\"17:00\",\"2025-03-11\",\"17:30\"," +
                    "\"Lectures\",\"NEU\",\"True\",\"False\"\n" +
                    "\"Recurring Event\",\"2025-03-12\",\"17:00\",\"2025-03-12\",\"17:30\"," +
                    "\"Lectures\",\"NEU\",\"True\",\"False\"\n",
            cal2.printEvents(dateTime.minusDays(10), dateTime.plusDays(10)));
    assertEquals("Asia/Kolkata", cal2.getTimeZone().getID());

  }

  @Test
  public void testEditSetTimeZoneRecurringEventWithOccurrences() {
    LocalDateTime startTime = LocalDateTime.of(2025, 3, 5,
            11, 0);
    LocalDateTime endTime = LocalDateTime.of(2025, 3, 5,
            11, 30);
    cal2.createRecurringEventWithOccurrences("Lecture 1", startTime, endTime,
            null, null, null, "TSM", 2);
    TimeZone tz = TimeZone.getTimeZone("Asia/Kolkata");
    cal2.setTimeZone(tz);
    assertEquals("\"Lecture 1\",\"2025-03-08\",\"15:30\",\"2025-03-08\",\"16:00\",\"\",\"\",\"\"," +
                    "\"False\"\n" +
                    "\"Lecture 1\",\"2025-03-10\",\"15:30\",\"2025-03-10\",\"16:00\",\"\",\"\"," +
                    "\"\",\"False\"\n",
            cal2.printEvents(startTime.minusDays(10), endTime.plusDays(10)));
    assertEquals("Asia/Kolkata", cal2.getTimeZone().getID());
  }

  @Test
  public void testCreateCalendarSetTimeZone() {
    cal2.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
    assertEquals("Asia/Kolkata", cal2.getTimeZone().getID());
  }

  @Test
  public void testCreateCalendarSetName() {
    cal2.setName("Cal1");
    cal2.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
    ICalendar cal3 = new Calendar();
    cal3.setName("Cal1");
    cal3.setTimeZone(TimeZone.getTimeZone("UTC"));
    assertEquals(cal2, cal3);
  }

  @Test
  public void testCalendarEquals() {
    cal.setName("Cal1");
    cal.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
    cal2.setName("Cal1");
    cal2.setTimeZone(TimeZone.getTimeZone("UTC"));
    ICalendar cal3 = new Calendar();
    cal3.setName("Cal3");
    cal3.setTimeZone(TimeZone.getTimeZone("UTC"));
    assertEquals(cal, cal2);
    assertNotEquals(cal3, cal2);
  }

  @Test
  public void testCalendarEqualsSameObject() {
    cal.setName("Cal1");
    cal.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
    assertEquals(cal, cal);
  }

  @Test
  public void testCalendarEqualsDifferentObject() {
    cal.setName("Cal1");
    cal.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
    String calendar1 = "Cal1";
    assertNotEquals(cal, calendar1);
  }

  @Test
  public void testCalendarHashCode() {
    cal.setName("Cal1");
    cal.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
    cal2.setName("Cal1");
    cal2.setTimeZone(TimeZone.getTimeZone("UTC"));
    ICalendar cal3 = new Calendar();
    cal3.setName("Cal3");
    cal3.setTimeZone(TimeZone.getTimeZone("UTC"));
    assertEquals(cal2, cal);
    assertEquals(cal2.hashCode(), cal.hashCode());
    assertNotEquals(cal3, cal2);
    assertNotEquals(cal3.hashCode(), cal2.hashCode());
  }


}