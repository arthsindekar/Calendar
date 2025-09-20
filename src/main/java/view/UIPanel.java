package view;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

import controller.Features;

/**
 * {@code UIPanel} is an abstract base class for building user interface components
 * within the calendar application. It provides common utility methods for event parsing,
 * date/time conversion, and spinner setup that can be reused across multiple UI panels.
 *
 * <p>Subclasses must implement the {@code display} method to render specific UI content
 * and connect it with controller {@link Features}.
 *
 * <p>This class supports Swing-based views and promotes consistency and reuse of
 * shared UI logic.
 */
public abstract class UIPanel {

  String[] parts(String line) {
    String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    for (int i = 0; i < parts.length; i++) {
      parts[i] = parts[i].replaceAll("^\"|\"$", "").trim();
    }
    return parts;
  }

  Date convertLocalTimeToCalTime(LocalTime time) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, time.getHour());
    cal.set(Calendar.MINUTE, time.getMinute());
    cal.set(Calendar.SECOND, time.getSecond());
    return cal.getTime();
  }

  LocalTime convertCalTimeToLocalTime(Date date) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    return LocalTime.of(
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE)
    );
  }

  JSpinner setDateSpinner(Date eventStartDate, JPanel datePanel) {
    SpinnerDateModel startDateModel = new SpinnerDateModel();
    JSpinner startDateSpinner = new JSpinner(startDateModel);
    startDateSpinner.setEditor(new JSpinner.DateEditor(startDateSpinner, "dd.MM.yyyy"));
    startDateSpinner.setValue(eventStartDate);
    datePanel.add(startDateSpinner);
    return startDateSpinner;
  }

  JSpinner setTimeSpinner(Date eventStartTime, JPanel timePanel) {
    SpinnerDateModel startTimeModel = new SpinnerDateModel();
    JSpinner startTimeSpinner = new JSpinner(startTimeModel);
    JSpinner.DateEditor startTimeEditor = new JSpinner.DateEditor(startTimeSpinner, "HH:mm");
    startTimeSpinner.setEditor(startTimeEditor);
    startTimeSpinner.setValue(eventStartTime);
    timePanel.add(startTimeSpinner);
    return startTimeSpinner;
  }

  abstract JComponent display(Features features);
}
