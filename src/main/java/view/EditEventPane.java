package view;

import java.awt.Component;
import java.awt.Dialog;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Stack;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import controller.Features;

/**
 * {@code EditEventPane} is a user interface panel that allows users to edit
 * the details of an existing calendar event. It extends {@link UIPanel} and uses
 * an embedded {@link EventTab} to populate and manage the editable fields.
 *
 * <p>This panel is displayed as a dialog and integrates with the controller through
 * the {@link Features} interface to apply changes to the selected event.
 */
public class EditEventPane extends EventTab {

  private final Stack<String> editActions;

  /**
   * Constructs an {@code EditEventPane} for editing an existing event.
   * Initializes the panel with event data and sets the parent component
   * for dialog display.
   *
   * @param parentComponent the UI component that owns the dialog
   * @param date the date of the event to be edited
   * @param event the event string containing details to prefill the form
   */
  EditEventPane(Component parentComponent, LocalDate date, String event) {
    super(date, event, parentComponent);
    editActions = new Stack<>();
  }

  @Override
  JComponent display(Features features) {
    JComponent panel = super.display(features);
    Object[] options = {"Save", "Cancel"};
    JOptionPane optionPane = new JOptionPane(panel,JOptionPane.PLAIN_MESSAGE,
            JOptionPane.YES_NO_OPTION, null, options, options[0]);
    Dialog dialog = optionPane.createDialog(parentComponent, "Edit Event");
    dialog.pack();
    userAction(features, dialog, optionPane);
    return optionPane;
  }

  @Override
  boolean performAction(Features features) {
    String subject = subjectField.getText();
    LocalDate startDate = getSelectedStartDate();
    if (startDate == null) {
      return false;
    }

    LocalDate endDate = getSelectedEndDate();
    if (endDate == null) {
      return false;
    }

    LocalTime startTime = getSelectedStartTime();
    if (startTime == null) {
      return false;
    }

    LocalTime endTime = getSelectedEndTime();
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
    LocalTime newEndTime = this.localEndTime;
    try {
      if (!Objects.equals(this.subject, subject)) {
        features.editEvent(this.subject, this.localStartDate, this.localStartTime,
                this.localEndDate, this.localEndTime, "subject", subject);
        editActions.push("subject");
      }
      if (!this.localStartDate.isEqual(startDate)) {
        features.editEvent(subject, this.localStartDate, this.localStartTime,
                this.localEndDate, this.localEndTime, "startDate", startDate.toString());
        editActions.push("startDate");
      }
      if (!this.localEndDate.isEqual(endDate)) {
        features.editEvent(subject, startDate, this.localStartTime,
                startDate, this.localEndTime, "endDate", endDate.toString());
        editActions.push("endDate");
        editActions.push("startDate");
      }
      if (!Objects.equals(this.localStartTime, startTime)) {
        features.editEvent(subject, startDate, this.localStartTime,
                endDate, this.localEndTime, "startTime", startTime.toString());
        Duration duration = Duration.between(this.localStartTime, this.localEndTime);
        newEndTime = startTime.plus(duration);
        if (duration.compareTo(Duration.between(startTime, LocalTime.MAX)) > 0) {
          newEndTime = LocalTime.of(23, 59, 59);
        }
        editActions.push("startTime");
      }
      if (!Objects.equals(newEndTime, endTime)) {
        features.editEvent(subject, startDate, startTime,
                endDate, newEndTime, "endTime", endTime.toString());
        editActions.push("endTime");
        editActions.push("startTime");
      }
      if (!Objects.equals(this.description, description)) {
        features.editEvent(subject, startDate, startTime,
                endDate, endTime, "description", description);
        editActions.push("description");
      }
      if (!Objects.equals(this.location, location)) {
        features.editEvent(subject, startDate, startTime,
                endDate, endTime, "location", location);
        editActions.push("location");
      }
      if (!Objects.equals(this.isPrivate, isPrivate)) {
        features.editEvent(subject, startDate, startTime,
                endDate, endTime, "isPrivate", isPrivate != null ? isPrivate.toString() : null);
        editActions.push("isPrivate");
      }
      editActions.empty();
      JOptionPane.showMessageDialog(parentComponent, "Event edited successfully",
              "Message", JOptionPane.INFORMATION_MESSAGE);
      return true;
    } catch (IllegalArgumentException e) {
      rollbackEditActions(subject, startDate, startTime, endDate, newEndTime, features);
      JOptionPane.showMessageDialog(parentComponent, e.getMessage(),
              "Message", JOptionPane.INFORMATION_MESSAGE);
      return false;
    }
  }

  private void rollbackEditActions(String subject, LocalDate startDate, LocalTime startTime,
                                   LocalDate endDate, LocalTime endTime, Features features) {
    LocalTime newEndTime = endTime;
    while (!editActions.isEmpty()) {
      String property = editActions.pop();
      Duration duration = Duration.between(startTime, endTime);
      switch (property) {
        case "subject":
          features.editEvent(subject, startDate, startTime,
                  endDate, newEndTime, property, this.subject);
          subject = this.subject;
          break;
        case "startDate":
          features.editEvent(subject, startDate, startTime,
                  endDate, newEndTime, property, this.startDate);
          startDate = LocalDate.parse(this.startDate);
          break;
        case "endDate":
          features.editEvent(subject, startDate, startTime,
                  endDate, newEndTime, property, this.endDate);
          endDate = LocalDate.parse(this.endDate);
          break;
        case "startTime":
          features.editEvent(subject, startDate, startTime,
                  endDate, newEndTime, property, this.startTime);
          startTime = LocalTime.parse(this.startTime);
          newEndTime = startTime.plus(duration);
          if (duration.compareTo(Duration.between(startTime, LocalTime.MAX)) > 0) {
            newEndTime = LocalTime.MAX;
          }
          break;
        case "endTime":
          features.editEvent(subject, startDate, startTime,
                  endDate, newEndTime, property, this.endTime);
          endTime = LocalTime.parse(this.endTime);
          break;
        case "description":
          features.editEvent(subject, startDate, startTime,
                  endDate, newEndTime, property, this.description);
          break;
        case "location":
          features.editEvent(subject, startDate, startTime,
                  endDate, newEndTime, property, this.location);
          break;
        case "isPrivate":
          features.editEvent(subject, startDate, startTime,
                  endDate, newEndTime, property, this.isPrivateString);
          break;
        default:
      }
    }
  }
}
