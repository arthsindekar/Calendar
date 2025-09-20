package view;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import controller.Features;

import static javax.swing.Box.createRigidArea;

/**
 * {@code EditEventPane} is a user interface panel that allows users to edit
 * the details of multiple existing calendar events. It extends {@link UIPanel}
 * and allows editing of all events with same event name or from a specified pont in time
 *
 * <p>This panel is displayed as a dialog and integrates with the controller through
 * the {@link Features} interface to apply changes to the selected event.
 */
public class EditEventsPane extends ActionPanel {

  private final Component parentComponent;
  private JSpinner dateSpinner;
  private JSpinner timeSpinner;
  private JCheckBox allEventsCheckBox;
  JComboBox<String> fieldsDropdown;
  private final String [] eventFields;

  JTextField subjectField;
  private JRadioButton privateRadioButton;
  private JRadioButton publicRadioButton;
  JTextField textField;
  JTextArea textArea;

  EditEventsPane(Component parentComponent) {
    this.parentComponent = parentComponent;
    eventFields = new String[]{
        "Select a field to edit", "subject", "description", "location", "mode"
    };
  }

  @Override
  JComponent display(Features features) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    JPanel subjectPanel = getSubjectPanel();
    panel.add(subjectPanel);
    panel.add(createRigidArea(new Dimension(0, 25)));

    JPanel datePanel = getDatePanel();
    panel.add(datePanel);
    panel.add(createRigidArea(new Dimension(0, 25)));

    addAllEventsCheckboxListener();

    fieldsDropdown = new JComboBox<>(eventFields);
    panel.add(fieldsDropdown);
    panel.add(createRigidArea(new Dimension(0, 15)));

    textField = new JTextField(19);

    textArea = new JTextArea(5, 18);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setEditable(true);

    privateRadioButton = new JRadioButton("Private");
    publicRadioButton = new JRadioButton("Public");
    ButtonGroup rGroup = new ButtonGroup();
    rGroup.add(privateRadioButton);
    rGroup.add(publicRadioButton);

    fieldsDropdown.setSelectedIndex(0);

    fieldsDropdown.setMaximumSize(new Dimension(200,
            fieldsDropdown.getPreferredSize().height));

    Object[] options = {"Save", "Cancel"};
    JOptionPane optionPane = new JOptionPane(panel,JOptionPane.PLAIN_MESSAGE,
            JOptionPane.YES_NO_OPTION, null, options, options[0]);
    Dialog dialog = optionPane.createDialog(parentComponent, "Edit Multiple Events");
    addFieldDropdownListener(panel, dialog);
    userAction(features, dialog, optionPane);

    return optionPane;
  }

  private void addFieldDropdownListener(JPanel panel, Dialog dialog) {
    JPanel editPanel = new JPanel();
    fieldsDropdown.addActionListener(e -> {
      editPanel.removeAll();
      panel.remove(editPanel);
      if (fieldsDropdown.getSelectedIndex() == 1) {
        editPanel.add(new JLabel("New Subject: "));
        editPanel.add(textField);
      }
      else if (fieldsDropdown.getSelectedIndex() == 2) {
        editPanel.add(new JLabel("New Description: "));
        editPanel.add(textArea);
      }
      else if (fieldsDropdown.getSelectedIndex() == 3) {
        editPanel.add(new JLabel("New Location: "));
        editPanel.add(textField);
      }
      else if (fieldsDropdown.getSelectedIndex() == 4) {
        editPanel.add(privateRadioButton);
        editPanel.add(publicRadioButton);
      }
      editPanel.add(createRigidArea(new Dimension(0, 50)));
      panel.add(editPanel);
      if (fieldsDropdown.getSelectedIndex() == 0) {
        editPanel.removeAll();
        panel.remove(editPanel);
      }
      editPanel.repaint();
      dialog.pack();
    });
  }

  private void addAllEventsCheckboxListener() {
    allEventsCheckBox.addActionListener(e -> {
      if (allEventsCheckBox.isSelected()) {
        dateSpinner.setEnabled(false);
        timeSpinner.setEnabled(false);
      }
      else {
        dateSpinner.setEnabled(true);
        timeSpinner.setEnabled(true);
      }
    });
  }

  private JPanel getDatePanel() {
    JPanel datePanel = new JPanel();
    datePanel.add(new JLabel("Edit After: "));
    dateSpinner = setDateSpinner(new Date(), datePanel);
    timeSpinner = setTimeSpinner(new Date(), datePanel);
    dateSpinner.setToolTipText("<html>Specify date and time on or after which" +
            " events<br/>matching the above subject will be edited</html>");
    timeSpinner.setToolTipText("<html>Specify date and time on or after which" +
            " events<br/>matching the above subject will be edited</html>");
    datePanel.add(createRigidArea(new Dimension(10, 0)));

    allEventsCheckBox = new JCheckBox("All Events");
    allEventsCheckBox.setToolTipText("<html>Check if you want to edit all the" +
            "<br/>events matching the above subject</html>");
    datePanel.add(allEventsCheckBox);
    return datePanel;
  }

  private JPanel getSubjectPanel() {
    JPanel subjectPanel = new JPanel();
    subjectField = new JTextField(20);
    subjectPanel.add(new JLabel(String.format("%-20s", "Subject: ")));
    subjectPanel.add(subjectField);
    return subjectPanel;
  }

  @Override
  boolean performAction(Features features) {
    String subject = subjectField.getText();
    boolean allEvents = allEventsCheckBox.isSelected();
    Date selectedDate;
    if (dateSpinner.getValue() instanceof Date) {
      selectedDate = (Date) dateSpinner.getValue();
    }
    else {
      JOptionPane.showMessageDialog(parentComponent, "Please enter a valid date",
              "Message", JOptionPane.INFORMATION_MESSAGE);
      return false;
    }
    LocalDate startDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    Date selectedTime;
    if (timeSpinner.getValue() instanceof Date) {
      selectedTime = (Date) timeSpinner.getValue();
    }
    else {
      JOptionPane.showMessageDialog(parentComponent, "Please enter a valid end time",
              "Message", JOptionPane.INFORMATION_MESSAGE);
      return false;
    }
    LocalTime startTime = convertCalTimeToLocalTime(selectedTime);

    String property = eventFields[fieldsDropdown.getSelectedIndex()];
    String newValue = "";

    switch (property) {
      case "subject":
      case "location":
        newValue = textField.getText();
        break;
      case "description":
        newValue = textArea.getText();
        break;
      case "mode":
        if (privateRadioButton.isSelected()) {
          newValue = "Private";
        }
        else if (publicRadioButton.isSelected()) {
          newValue = "Public";
        }
        break;
      default:
        JOptionPane.showMessageDialog(parentComponent, "Please select a valid event field",
                "Message", JOptionPane.INFORMATION_MESSAGE);
        return false;
    }
    newValue = newValue.isEmpty() ? null : newValue;
    try {
      features.editEvents(subject, allEvents, startDate, startTime, property, newValue);
      return true;
    } catch (IllegalArgumentException e) {
      JOptionPane.showMessageDialog(parentComponent, e.getMessage(),
              "Message", JOptionPane.INFORMATION_MESSAGE);
      return false;
    }
  }
}
