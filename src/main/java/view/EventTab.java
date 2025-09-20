package view;

import java.awt.Component;
import java.awt.Dimension;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.DefaultFormatter;

import controller.Features;

import static javax.swing.Box.createRigidArea;

/**
 * {@code EventTab} is a reusable UI component for both creating and editing calendar events.
 * It provides a structured form interface for entering or updating event details such as
 * subject, date and time, description, location, and visibility (public/private).
 *
 * <p>This class extends {@link UIPanel} and leverages Swing components to allow users
 * to input or modify event information. It supports validation, field synchronization,
 * and rollback functionality in case of editing failures.
 *
 * <p>Used by both {@code CreateEventPane} and {@code EditEventPane}, it centralizes
 * the shared logic between the two workflows.
 */
public abstract class EventTab extends ActionPanel {

  String subject;
  String startDate;
  String endDate;
  String startTime;
  String endTime;
  String description;
  String location;
  String isPrivateString;
  LocalDate localStartDate;
  LocalDate localEndDate;
  LocalTime localStartTime;
  LocalTime localEndTime;
  Boolean isPrivate;

  JTextField subjectField;
  JSpinner startDateSpinner;
  JSpinner startTimeSpinner;
  JSpinner endDateSpinner;
  JSpinner endTimeSpinner;
  JTextArea descriptionField;
  JTextField locationField;
  JRadioButton privateRadioButton;
  JRadioButton publicRadioButton;

  private Date enteredStartTime;
  private Date enteredEndTime;

  final LocalDate date;
  final String event;

  final Component parentComponent;



  /**
   * Constructs an {@code EventTab} panel for the specified date and optional event data.
   *
   * <p>If an existing event string is provided, its contents are parsed and used
   * to prefill the form. Otherwise, a blank form is initialized for new event creation.
   *
   * @param date the date associated with the event
   * @param event a string containing event data (may be empty or null for new events)
   * @param parentComponent the Swing component used for dialog anchoring and messaging
   */
  public EventTab(LocalDate date, String event, Component parentComponent) {
    this.date = date;
    this.event = event;
    this.parentComponent = parentComponent;
  }


  @Override
  JComponent display(Features features) {
    String[] eventFields = parts(event);
    if (!event.isEmpty()) {
      subject = eventFields[0];
      startDate = eventFields[1];
      startTime = eventFields[2];
      endDate = eventFields[3];
      endTime = eventFields[4];
      description = eventFields[5];
      location = eventFields[6];
      isPrivateString = eventFields[7];
    }
    localStartDate = startDate != null ? LocalDate.parse(startDate) : date;
    localStartTime = startTime != null ? LocalTime.parse(startTime) : LocalTime.MIN;
    Date eventStartDate = Date.from(localStartDate.atStartOfDay(ZoneId.systemDefault())
            .toInstant());
    Date eventStartTime = convertLocalTimeToCalTime(localStartTime);
    localEndDate = endDate != null ? LocalDate.parse(endDate) : date;
    localEndTime = endTime != null ? LocalTime.parse(endTime) : LocalTime.MAX;
    Date eventEndDate = Date.from(localEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    Date eventEndTime = convertLocalTimeToCalTime(localEndTime);
    isPrivate = null;
    if (isPrivateString != null && !isPrivateString.isEmpty()) {
      isPrivate = Boolean.parseBoolean(isPrivateString);
    }

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    JPanel subjectPanel = getSubjectPanel();
    panel.add(subjectPanel);
    panel.add(createRigidArea(new Dimension(0, 35)));

    JPanel datePanel = getDatePanel(eventStartDate, eventStartTime, eventEndDate, eventEndTime);
    panel.add(datePanel);
    addDateSpinnerChangeListeners();
    addTimeSpinnerChangeListeners();
    panel.add(createRigidArea(new Dimension(0, 30)));

    JPanel descriptionPanel = getDescriptionPanel();
    panel.add(descriptionPanel);
    panel.add(createRigidArea(new Dimension(0, 30)));

    JPanel locationPanel = getLocationPanel();
    panel.add(locationPanel);
    panel.add(createRigidArea(new Dimension(0, 30)));

    JPanel typePanel = getTypePanel();
    panel.add(typePanel);
    panel.add(createRigidArea(new Dimension(0, 30)));

    return panel;
  }

  private void addTimeSpinnerChangeListeners() {
    JFormattedTextField startTimeText = ((JSpinner.DateEditor)
            startTimeSpinner.getEditor()).getTextField();
    ((DefaultFormatter) startTimeText.getFormatter()).setCommitsOnValidEdit(false);

    enteredStartTime = (Date) startTimeSpinner.getValue();
    enteredEndTime = (Date) endTimeSpinner.getValue();
    startTimeSpinner.addChangeListener(e -> {
      try {
        startTimeSpinner.commitEdit();
        long duration = enteredEndTime.getTime() - enteredStartTime.getTime();

        Date newEnteredStartTime = (Date) startTimeSpinner.getValue();
        Date newEnteredEndTime = getNewEnteredEndTime(newEnteredStartTime, duration);
        endTimeSpinner.setValue(newEnteredEndTime);
        enteredStartTime = (Date) startTimeSpinner.getValue();
        enteredEndTime = (Date) endTimeSpinner.getValue();
      } catch (ParseException ignored) {
      }
    });

    endTimeSpinner.addChangeListener(e -> {
      enteredStartTime = (Date) startTimeSpinner.getValue();
      enteredEndTime = (Date) endTimeSpinner.getValue();
    });
  }

  private void addDateSpinnerChangeListeners() {
    JFormattedTextField startDateText = ((JSpinner.DateEditor)
            startDateSpinner.getEditor()).getTextField();
    ((DefaultFormatter) startDateText.getFormatter()).setCommitsOnValidEdit(false);
    JFormattedTextField endDateText = ((JSpinner.DateEditor)
            endDateSpinner.getEditor()).getTextField();
    ((DefaultFormatter) endDateText.getFormatter()).setCommitsOnValidEdit(false);

    startDateSpinner.addChangeListener(e -> {
      try {
        startDateSpinner.commitEdit();
        Date enteredDate = (Date) startDateSpinner.getValue();
        endDateSpinner.setValue(enteredDate);
      } catch (ParseException ignored) {
      }
    });

    endDateSpinner.addChangeListener(e -> {
      try {
        endDateSpinner.commitEdit();
        Date enteredDate = (Date) endDateSpinner.getValue();
        startDateSpinner.setValue(enteredDate);
      } catch (ParseException ignored) {
      }
    });
  }

  private JPanel getSubjectPanel() {
    JPanel subjectPanel = new JPanel();

    subjectPanel.add(new JLabel(String.format("%-20s", "Subject: ")));
    subjectField = new JTextField(subject, 20);
    subjectPanel.add(subjectField);
    subjectPanel.setLayout(new BoxLayout(subjectPanel, BoxLayout.X_AXIS));
    subjectPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    return subjectPanel;
  }

  private JPanel getDatePanel(Date eventStartDate, Date eventStartTime,
                              Date eventEndDate, Date eventEndTime) {
    JPanel datePanel = new JPanel();
    datePanel.add(new JLabel("Start: "));
    startDateSpinner = setDateSpinner(eventStartDate, datePanel);
    startTimeSpinner = setTimeSpinner(eventStartTime, datePanel);

    datePanel.add(new JLabel(String.format("%10s", "End: ")));
    endDateSpinner = setDateSpinner(eventEndDate, datePanel);
    endTimeSpinner = setTimeSpinner(eventEndTime, datePanel);

    datePanel.setLayout(new BoxLayout(datePanel, BoxLayout.X_AXIS));
    datePanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    datePanel.setSize(300, 100);
    return datePanel;
  }

  private JPanel getTypePanel() {
    JPanel typePanel = new JPanel();
    typePanel.add(new JLabel(String.format("%-20s", "Type: ")));
    privateRadioButton = new JRadioButton("Private");
    publicRadioButton = new JRadioButton("Public");
    ButtonGroup rGroup = new ButtonGroup();
    rGroup.add(privateRadioButton);
    rGroup.add(publicRadioButton);
    typePanel.add(privateRadioButton);
    typePanel.add(createRigidArea(new Dimension(50, 0)));
    typePanel.add(publicRadioButton);
    typePanel.add(createRigidArea(new Dimension(90, 0)));
    if (isPrivate == null) {
      privateRadioButton.setSelected(false);
      publicRadioButton.setSelected(false);
    }
    else if (!isPrivate) {
      publicRadioButton.setSelected(true);
    }
    else {
      privateRadioButton.setSelected(true);
    }

    typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.X_AXIS));
    typePanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    return typePanel;
  }

  private JPanel getLocationPanel() {
    JPanel locationPanel = new JPanel();
    locationField = new JTextField(location, 20);
    locationPanel.add(new JLabel(String.format("%-20s", "Location: ")));
    locationPanel.add(locationField);
    locationPanel.setLayout(new BoxLayout(locationPanel, BoxLayout.X_AXIS));
    locationPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    return locationPanel;
  }

  private JPanel getDescriptionPanel() {
    JPanel descriptionPanel = new JPanel();
    descriptionField = new JTextArea(description, 5, 15);
    descriptionField.setLineWrap(true);
    descriptionField.setWrapStyleWord(true);
    descriptionField.setEditable(true);
    Dimension fixedSize = new Dimension(250, 100);
    JScrollPane scrollPane = new JScrollPane(descriptionField,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setPreferredSize(fixedSize);
    descriptionPanel.add(new JLabel(String.format("%-20s", "Description: ")));
    descriptionPanel.add(scrollPane);
    descriptionPanel.setLayout(new BoxLayout(descriptionPanel, BoxLayout.X_AXIS));
    descriptionPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    return descriptionPanel;
  }

  private static Date getNewEnteredEndTime(Date newEnteredStartTime, long duration) {
    Date newEnteredEndTime = new Date(newEnteredStartTime.getTime() + duration);
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, 1970);
    calendar.set(Calendar.MONTH, Calendar.JANUARY);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    calendar.set(Calendar.HOUR_OF_DAY, 23);
    calendar.set(Calendar.MINUTE, 59);
    calendar.set(Calendar.SECOND, 59);
    Date timeMax = new Date(calendar.getTimeInMillis());
    if (newEnteredEndTime.getTime() > timeMax.getTime()) {
      newEnteredEndTime.setTime(timeMax.getTime());
    }
    return newEnteredEndTime;
  }

  LocalDate getSelectedStartDate() {
    Date selectedStartDate;
    if (startDateSpinner.getValue() instanceof Date) {
      selectedStartDate = (Date) startDateSpinner.getValue();
    }
    else {
      JOptionPane.showMessageDialog(parentComponent, "Please enter a valid start date",
              "Message", JOptionPane.INFORMATION_MESSAGE);
      return null;
    }
    return selectedStartDate.toInstant()
            .atZone(ZoneId.systemDefault()).toLocalDate();
  }

  LocalDate getSelectedEndDate() {
    Date selectedEndDate;
    if (endDateSpinner.getValue() instanceof Date) {
      selectedEndDate = (Date) endDateSpinner.getValue();
    }
    else {
      JOptionPane.showMessageDialog(parentComponent, "Please enter a valid end date",
              "Message", JOptionPane.INFORMATION_MESSAGE);
      return null;
    }
    return selectedEndDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
  }

  LocalTime getSelectedStartTime() {
    Date selectedStartTime;
    if (startTimeSpinner.getValue() instanceof Date) {
      selectedStartTime = (Date) startTimeSpinner.getValue();
    }
    else {
      JOptionPane.showMessageDialog(parentComponent, "Please enter a valid start time",
              "Message", JOptionPane.INFORMATION_MESSAGE);
      return null;
    }
    return convertCalTimeToLocalTime(selectedStartTime);
  }

  LocalTime getSelectedEndTime() {
    Date selectedEndTime;
    if (endTimeSpinner.getValue() instanceof Date) {
      selectedEndTime = (Date) endTimeSpinner.getValue();
    }
    else {
      JOptionPane.showMessageDialog(parentComponent, "Please enter a valid end time",
              "Message", JOptionPane.INFORMATION_MESSAGE);
      return null;
    }
    return convertCalTimeToLocalTime(selectedEndTime);
  }

}
