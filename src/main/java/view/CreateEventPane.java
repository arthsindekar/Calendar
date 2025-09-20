package view;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;

import controller.Features;

import static javax.swing.Box.createRigidArea;

/**
 * {@code CreateEventPane} is a user interface panel that facilitates the creation
 * of new events within the calendar application. It extends {@link UIPanel} and provides
 * form elements for entering event details such as recurrence, dates, and time.
 *
 * <p>This panel includes options for configuring recurring events, such as selecting
 * days of the week and specifying either a fixed number of occurrences or a specific
 * end date.
 *
 * <p>It is designed to be embedded within a Swing component hierarchy and interacts
 * with the controller via the {@link Features} interface.
 */
public class CreateEventPane extends EventTab {

  private JCheckBox repeatsCheckBox;
  private JCheckBox mondayCheckBox;
  private JCheckBox tuesdayCheckBox;
  private JCheckBox wednesdayCheckBox;
  private JCheckBox thursdayCheckBox;
  private JCheckBox fridayCheckBox;
  private JCheckBox saturdayCheckBox;
  private JCheckBox sundayCheckBox;
  private JSpinner occurrencesSpinner;
  private JSpinner specificEndDateSpinner;
  private JRadioButton occurrencesButton;
  private JRadioButton specificDateButton;

  private JComponent panel;

  /**
   * Constructs a {@code CreateEventPane} with the given parent component and date.
   *
   * <p>Initializes the form components and configures the panel for the specified date.
   *
   * @param parentComponent the parent Swing component used for dialog positioning
   * @param date the date associated with the event being created
   */
  CreateEventPane(Component parentComponent, LocalDate date) {
    super(date, "", parentComponent);
  }

  @Override
  JComponent display(Features features) {
    panel = super.display(features);
    JPanel repeatsPanel = getRepeatsPanel();
    panel.add(repeatsPanel);
    panel.add(createRigidArea(new Dimension(0, 30)));

    JPanel daysOfWeekPanel = getDaysOfWeekPanel();
    JPanel untilPanel = getUntilPanel();
    JPanel occurrencesPanel = getOccurrencesPanel();
    JPanel endDatePanel = getEndDatePanel();

    JPanel recurPanel = new JPanel();
    recurPanel.setLayout(new BoxLayout(recurPanel, BoxLayout.X_AXIS));
    recurPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    recurPanel.setPreferredSize(new Dimension(250, 50));

    Object[] options = {"Save", "Cancel"};
    JOptionPane optionPane = new JOptionPane(panel,JOptionPane.PLAIN_MESSAGE,
            JOptionPane.YES_NO_OPTION, null, options, options[0]);
    Dialog dialog = optionPane.createDialog(parentComponent, "Create Event");

    addRepeatCheckBoxListener(daysOfWeekPanel, untilPanel, recurPanel,
            occurrencesPanel, endDatePanel, dialog);

    specificDateButton.addActionListener(e ->
            changeRecurringPanel(occurrencesButton, recurPanel, occurrencesPanel,
                    endDatePanel, dialog));

    occurrencesButton.addActionListener(e ->
            changeRecurringPanel(occurrencesButton, recurPanel, occurrencesPanel,
                    endDatePanel, dialog));
    userAction(features, dialog, optionPane);
    return optionPane;
  }

  @Override
  boolean performAction(Features features) {
    String subject = subjectField.getText();

    LocalDate startDate = super.getSelectedStartDate();
    if (startDate == null) {
      return false;
    }

    LocalDate endDate = super.getSelectedEndDate();
    if (endDate == null) {
      return false;
    }

    LocalTime startTime = super.getSelectedStartTime();
    if (startTime == null) {
      return false;
    }

    LocalTime endTime = super.getSelectedEndTime();
    if (endTime == null) {
      return false;
    }

    String description = descriptionField.getText();
    description = description.isEmpty() ? null : description;
    String location = locationField.getText();
    location = location.isEmpty() ? null : location;
    Boolean isPrivate = privateRadioButton.isSelected();
    isPrivate = !publicRadioButton.isSelected() && !privateRadioButton.isSelected()
            ? null : isPrivate;

    boolean isRecurring;
    isRecurring = repeatsCheckBox.isSelected();
    String daysOfWeek = getDaysOfWeekString();
    boolean isOccurrences;
    isOccurrences = occurrencesButton.isSelected();
    int occurrences;
    if (occurrencesSpinner.getValue() instanceof Integer
            && (int) occurrencesSpinner.getValue() > 0) {
      occurrences = (int) occurrencesSpinner.getValue();
    }
    else {
      JOptionPane.showMessageDialog(parentComponent, "Please enter a valid occurrences",
              "Message", JOptionPane.INFORMATION_MESSAGE);
      return false;
    }
    Date selectedSpecificEndDate;
    if (specificEndDateSpinner.getValue() instanceof Date) {
      selectedSpecificEndDate = (Date) specificEndDateSpinner.getValue();
    }
    else {
      JOptionPane.showMessageDialog(parentComponent, "Please enter a valid specific end date",
              "Message", JOptionPane.INFORMATION_MESSAGE);
      return false;
    }
    LocalDate specificEndDate = selectedSpecificEndDate.toInstant().atZone(ZoneId.systemDefault())
            .toLocalDate();
    try {
      features.createEvent(subject, startDate, startTime, endDate, endTime, description, location,
              isPrivate, isRecurring, daysOfWeek, isOccurrences, occurrences, specificEndDate);
      return true;
    } catch (IllegalArgumentException e) {
      JOptionPane.showMessageDialog(parentComponent, e.getMessage(),
              "Message", JOptionPane.INFORMATION_MESSAGE);
      return false;
    }
  }

  private void addRepeatCheckBoxListener(JPanel daysOfWeekPanel, JPanel untilPanel,
                                         JPanel recurPanel, JPanel occurrencesPanel,
                                         JPanel endDatePanel, Dialog dialog) {
    repeatsCheckBox.addActionListener(e -> {
      if (repeatsCheckBox.isSelected()) {
        panel.add(daysOfWeekPanel);
        panel.add(untilPanel);
        panel.add(recurPanel);
        if (occurrencesButton.isSelected()) {
          recurPanel.add(occurrencesPanel);
          recurPanel.remove(endDatePanel);
        }
        else {
          recurPanel.add(endDatePanel);
          recurPanel.remove(occurrencesPanel);
        }
      }
      else {
        panel.remove(daysOfWeekPanel);
        panel.remove(untilPanel);
        panel.remove(recurPanel);
      }
      dialog.pack();
    });
  }

  private JPanel getEndDatePanel() {
    JPanel endDatePanel = new JPanel();
    Date specificEndDate = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    specificEndDateSpinner = setDateSpinner(specificEndDate, endDatePanel);
    specificEndDateSpinner.setPreferredSize(new Dimension(80, 20));
    specificEndDateSpinner.setMaximumSize(new Dimension(80, 20));
    return getRecurPanel(endDatePanel, specificEndDateSpinner);
  }

  private JPanel getRecurPanel(JPanel endDatePanel, JSpinner specificEndDateSpinner) {
    endDatePanel.add(specificEndDateSpinner);
    endDatePanel.setLayout(new BoxLayout(endDatePanel, BoxLayout.X_AXIS));
    endDatePanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    endDatePanel.add(createRigidArea(new Dimension(150, 0)));
    endDatePanel.setPreferredSize(new Dimension(250, 50));
    return endDatePanel;
  }

  private JPanel getOccurrencesPanel() {
    JPanel occurrencesPanel = new JPanel();
    occurrencesSpinner = new JSpinner();
    occurrencesSpinner.setPreferredSize(new Dimension(40, 20));
    occurrencesSpinner.setMaximumSize(new Dimension(40, 20));
    occurrencesSpinner.setValue(1);
    return getRecurPanel(occurrencesPanel, occurrencesSpinner);
  }

  private JPanel getUntilPanel() {
    JPanel untilPanel = new JPanel();
    occurrencesButton = new JRadioButton("Occurrences");
    specificDateButton = new JRadioButton("Recurrence End Date");
    occurrencesButton.setSelected(true);
    ButtonGroup rGroup = new ButtonGroup();
    rGroup.add(occurrencesButton);
    rGroup.add(specificDateButton);
    untilPanel.add(occurrencesButton);
    untilPanel.add(specificDateButton);
    untilPanel.setLayout(new BoxLayout(untilPanel, BoxLayout.X_AXIS));
    untilPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    untilPanel.add(createRigidArea(new Dimension(60, 0)));
    Dimension fixedSize = new Dimension(250, 80);
    untilPanel.setPreferredSize(fixedSize);
    return untilPanel;
  }

  private JPanel getDaysOfWeekPanel() {
    JPanel daysOfWeekPanel = new JPanel();
    daysOfWeekPanel.add(new JLabel(String.format("%-20s", "Days: ")));
    mondayCheckBox = new JCheckBox("M");
    tuesdayCheckBox = new JCheckBox("Tu");
    wednesdayCheckBox = new JCheckBox("W");
    thursdayCheckBox = new JCheckBox("Th");
    fridayCheckBox = new JCheckBox("F");
    saturdayCheckBox = new JCheckBox("Sa");
    sundayCheckBox = new JCheckBox("Su");
    daysOfWeekPanel.add(mondayCheckBox);
    daysOfWeekPanel.add(tuesdayCheckBox);
    daysOfWeekPanel.add(wednesdayCheckBox);
    daysOfWeekPanel.add(thursdayCheckBox);
    daysOfWeekPanel.add(fridayCheckBox);
    daysOfWeekPanel.add(saturdayCheckBox);
    daysOfWeekPanel.add(sundayCheckBox);
    DayOfWeek day = date.getDayOfWeek();
    setSelectedDay(day);
    daysOfWeekPanel.setLayout(new BoxLayout(daysOfWeekPanel, BoxLayout.X_AXIS));
    daysOfWeekPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    return daysOfWeekPanel;
  }

  private JPanel getRepeatsPanel() {
    JPanel repeatsPanel = new JPanel();
    repeatsCheckBox = new JCheckBox("Repeats");
    repeatsCheckBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
    repeatsPanel.setLayout(new BoxLayout(repeatsPanel, BoxLayout.X_AXIS));
    repeatsPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
    repeatsPanel.add(repeatsCheckBox);
    repeatsPanel.add(createRigidArea(new Dimension(130, 0)));
    return repeatsPanel;
  }

  private void setSelectedDay(DayOfWeek day) {
    switch (day) {
      case MONDAY:
        mondayCheckBox.setSelected(true);
        break;
      case TUESDAY:
        tuesdayCheckBox.setSelected(true);
        break;
      case WEDNESDAY:
        wednesdayCheckBox.setSelected(true);
        break;
      case THURSDAY:
        thursdayCheckBox.setSelected(true);
        break;
      case FRIDAY:
        fridayCheckBox.setSelected(true);
        break;
      case SATURDAY:
        saturdayCheckBox.setSelected(true);
        break;
      case SUNDAY:
        sundayCheckBox.setSelected(true);
        break;
      default:
    }
  }

  private void changeRecurringPanel(JRadioButton occurrencesButton, JPanel recurPanel,
                                    JPanel occurrencesPanel, JPanel endDatePanel,
                                    Dialog dialog) {
    if (occurrencesButton.isSelected()) {
      recurPanel.add(occurrencesPanel);
      recurPanel.remove(endDatePanel);
    }
    else {
      recurPanel.add(endDatePanel);
      recurPanel.remove(occurrencesPanel);
    }
    dialog.revalidate();
    dialog.repaint();
    dialog.pack();
  }

  private String getDaysOfWeekString() {
    StringBuilder daysOfWeek = new StringBuilder();
    if (mondayCheckBox.isSelected()) {
      daysOfWeek.append('M');
    }
    if (tuesdayCheckBox.isSelected()) {
      daysOfWeek.append('T');
    }
    if (wednesdayCheckBox.isSelected()) {
      daysOfWeek.append('W');
    }
    if (thursdayCheckBox.isSelected()) {
      daysOfWeek.append('R');
    }
    if (fridayCheckBox.isSelected()) {
      daysOfWeek.append('F');
    }
    if (saturdayCheckBox.isSelected()) {
      daysOfWeek.append('S');
    }
    if (sundayCheckBox.isSelected()) {
      daysOfWeek.append('U');
    }
    return daysOfWeek.toString();
  }
}
