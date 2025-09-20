package view;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import controller.Features;

import static javax.swing.Box.createRigidArea;

/**
 * {@code DayEventsPanel} is a user interface component that displays all events
 * scheduled for a specific date. It allows users to view, edit, or create events
 * for that day using interactive buttons and dialog popups. The event details itself is a button
 * through which user can edit that particular event.
 *
 * <p>This panel is part of the Swing-based GUI and extends {@link UIPanel} to reuse
 * utility functionality such as event parsing. It interacts with the controller
 * through the {@link Features} interface to retrieve and update event data.
 */
public class DayEventsPanel extends UIPanel {

  private final Component parentComponent;
  private final LocalDate date;

  /**
   * Constructs a {@code DayEventsPanel} for the specified date and parent component.
   *
   * <p>The parent component is used to anchor dialog windows that this panel opens
   * (e.g., for editing or creating events).
   *
   * @param parentComponent the parent UI component used for positioning dialogs
   * @param date the date for which events will be displayed and managed
   */
  DayEventsPanel(Component parentComponent, LocalDate date) {
    this.parentComponent = parentComponent;
    this.date = date;
  }

  @Override
  JComponent display(Features features) {
    List<String> dayEvents = features.viewEvents(date);
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy ");
    JLabel dateLabel = new JLabel(date.format(formatter));
    dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    panel.add(dateLabel);
    panel.add(createRigidArea(new Dimension(0, 25)));
    JOptionPane optionPane = new JOptionPane(panel,JOptionPane.PLAIN_MESSAGE,
            JOptionPane.DEFAULT_OPTION, null, new Object[]{}, null);

    Dialog dialog = optionPane.createDialog(parentComponent, "Events");
    dialog.setLocationRelativeTo(parentComponent);
    JPanel viewButtonPanel = new JPanel();
    viewButtonPanel.setLayout(new BoxLayout(viewButtonPanel, BoxLayout.Y_AXIS));
    for (String dayEvent : dayEvents) {
      String[] eventFields = parts(dayEvent);
      String subject = eventFields[0];
      String startDate = eventFields[1];
      String startTime = eventFields[2];
      String endDate = eventFields[3];
      String endTime = eventFields[4];
      String description = eventFields[5];
      String location = eventFields[6];
      String isPrivateString = eventFields[7];
      StringBuilder eventDetailString = new StringBuilder();
      eventDetailString.append("<html>Subject : ").append(subject).append("<br/>");
      eventDetailString.append("Start : ").append(startDate).append(" ").append(startTime);
      eventDetailString.append(" End : ").append(endDate).append(" ")
              .append(endTime).append("<br/>");
      if (!description.isEmpty()) {
        eventDetailString.append("Description : ").append(description).append("<br/>");
      }
      if (!location.isEmpty()) {
        eventDetailString.append("Location : ").append(location).append("<br/>");
      }
      if (!isPrivateString.isEmpty()) {
        isPrivateString = isPrivateString.equals("True") ? "Private" : "Public";
        eventDetailString.append("Mode : ").append(isPrivateString);
      }
      eventDetailString.append("</html>");
      JButton viewEditButton = new JButton( "<html>" + subject + "<br/>"
              + startTime + "-" + endTime + "</html>");
      viewEditButton.setToolTipText(eventDetailString.toString());
      viewEditButton.setAlignmentX(Component.CENTER_ALIGNMENT);
      viewEditButton.addActionListener(e -> {
        ActionPanel editEventPane = new EditEventPane(parentComponent, date, dayEvent);
        JOptionPane editOptionPane = (JOptionPane) editEventPane.display(features);
        if (Objects.equals(editOptionPane.getValue(), "Save")) {
          dialog.dispose();
          display(features);
        }
      });
      viewButtonPanel.add(viewEditButton);
      viewButtonPanel.add(createRigidArea(new Dimension(0, 10)));
    }
    Dimension maxSize = new Dimension(200, 500);
    viewButtonPanel.setMaximumSize(maxSize);
    JScrollPane scrollPane = new JScrollPane(viewButtonPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setBorder(null);
    int viewButtonPanelHeight = viewButtonPanel.getComponentCount() * 30;
    int maxScrollHeight = 300;
    int scrollHeight = Math.min(viewButtonPanelHeight, maxScrollHeight);
    scrollPane.setPreferredSize(new Dimension(220, scrollHeight));

    panel.add(scrollPane);
    JButton createButton = getCreateButton(features, dialog);
    panel.add(createRigidArea(new Dimension(0, 10)));
    panel.add(createButton);
    dialog.pack();
    dialog.setVisible(true);
    return panel;
  }

  private JButton getCreateButton(Features features, Dialog dialog) {
    JButton createButton = new JButton("New Event");
    createButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    createButton.addActionListener(e -> {
      ActionPanel createEventPane = new CreateEventPane(parentComponent, date);
      JOptionPane createOptionPane = (JOptionPane) createEventPane.display(features);
      if (Objects.equals(createOptionPane.getValue(), "Save")) {
        dialog.dispose();
        display(features);
      }
    });
    return createButton;
  }
}
