package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import controller.Features;

/**
 * {@code GUICalendarView} is a graphical user interface implementation of the
 * {@link ICalendarView} interface. It presents calendar data to the user using
 * a Swing-based interface and allows interaction through buttons, dropdowns, and dialogs.
 *
 * <p>This class serves as the view in the MVC architecture, enabling users to
 * navigate months, select calendars, create or import/export calendars,
 * and view events for specific days.
 *
 * <p>The user need to not create a calendar to create,edit,view,import or export events.
 * The "Default" calendar is already created and in use for the user.
 */
public class GUICalendarView extends JFrame implements ICalendarView  {

  private final JFrame frame;
  private YearMonth currentMonth;
  private final JPanel calendarPanel;
  private final JLabel monthLabel;
  private final JComboBox<String> calendarDropdown;
  private String [] calendars;
  private final JLabel calendarNameLabel;

  private final JButton prevButton;
  private final JButton nextButton;
  private final JButton createCalendarButton;
  private final JButton editEventsButton;
  private final JButton exportCalendarButton;
  private final JButton importCalendarButton;

  /**
   * Constructs a new {@code GUICalendarView} instance, initializing and configuring
   * the GUI components including navigation buttons, calendar panel, and input forms.
   *
   * <p>This sets up the frame layout, default calendar view, and visual styling,
   * but does not attach controller behavior. Call {@code addFeatures()} to bind
   * user actions to controller logic.
   */
  public GUICalendarView() {
    frame = new JFrame("Calendar App");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(600, 600);
    frame.setLayout(new BorderLayout());

    currentMonth = YearMonth.now();

    JPanel topPanel = new JPanel();
    JPanel bottomPanel = new JPanel();
    prevButton = new JButton("<");
    nextButton = new JButton(">");

    calendars = new String[] {"Default"};

    createCalendarButton = new JButton("Add Calendar");
    editEventsButton = new JButton("Edit Events");
    exportCalendarButton = new JButton("Export");
    importCalendarButton = new JButton("Import");

    monthLabel = new JLabel();
    calendarDropdown = new JComboBox<>(calendars);
    calendarDropdown.setSelectedItem(calendars[0]);//set "Default" with default timezone in use.
    topPanel.add(prevButton);
    topPanel.add(monthLabel);
    topPanel.add(nextButton);
    topPanel.add(calendarDropdown);
    topPanel.add(createCalendarButton);
    bottomPanel.add(editEventsButton);
    bottomPanel.add(exportCalendarButton);
    bottomPanel.add(importCalendarButton);

    ToolTipManager.sharedInstance().setDismissDelay(10000);
    ToolTipManager.sharedInstance().setInitialDelay(500);

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)); // Vertical stacking

    frame.add(topPanel, BorderLayout.NORTH);
    frame.add(bottomPanel, BorderLayout.SOUTH);

    calendarPanel = new JPanel();
    JPanel calendarNamePanel = new JPanel();
    calendarNamePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
    calendarNamePanel.setPreferredSize(new Dimension(400,30));
    calendarNamePanel.setMaximumSize(new Dimension(400,30));
    calendarNameLabel = new JLabel("Selected Calendar: Default");

    calendarNamePanel.add(calendarNameLabel);

    mainPanel.add(calendarNamePanel);
    mainPanel.add(calendarPanel);

    frame.add(mainPanel, BorderLayout.CENTER);
    frame.setVisible(true);
    resizeDropDown();
    trimCalendarInsideDropDown();
  }

  @Override
  public void addFeatures(Features features) {
    updateCalendar(features);
    prevButton.addActionListener(e -> changeMonth(-1, features));
    nextButton.addActionListener(e -> changeMonth(1, features));
    calendarDropdown.addActionListener(e -> {
      features.useCalendar(calendars[calendarDropdown.getSelectedIndex()]);
      changeCalendarName(calendars[calendarDropdown.getSelectedIndex()]);
      updateCalendar(features);
    });

    createCalendarButton(features);

    editEventsButton(features);

    exportCalendarButton(features);

    importCalendarButton(features);
  }



  private void changeCalendarName(String calendarName) {
    String[] parts = calendarNameLabel.getText().split(":");
    calendarNameLabel.setText(parts[0] + ": " + calendarName);
  }

  private void resizeDropDown() {
    calendarDropdown.setMaximumRowCount(5); // Max items before scrolling
    fixDropdownSize(calendarDropdown, 100, 100);
  }

  private void fixDropdownSize(JComboBox<String> calendarDropdown, int width, int height) {
    calendarDropdown.setPrototypeDisplayValue("12345678"); // Ensures consistent width

    // Override dropdown size
    calendarDropdown.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
      @Override
      public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
        JPopupMenu popup = (JPopupMenu) calendarDropdown.getUI()
                .getAccessibleChild(calendarDropdown, 0);
        if (popup != null && popup.getComponentCount() > 0) {
          Component scrollPane = popup.getComponent(0);
          if (scrollPane instanceof JScrollPane) {
            scrollPane.setPreferredSize(new Dimension(width, height));
          }
        }
      }

      @Override
      public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
        //Do nothing when popup menu is invisible
      }

      @Override
      public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {
        //Do nothing when popup menu is canceled
      }
    });
  }

  private void trimCalendarInsideDropDown() {
    calendarDropdown.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                    boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof String) {
          String text = (String) value;
          if (text.length() > 8) {
            text = text.substring(0, 8) + "...";
          }
          setText(text);
        }
        return this;
      }
    });
  }

  private void editEventsButton(Features features) {
    editEventsButton.addActionListener(e -> {
      ActionPanel editEventsPane = new EditEventsPane(frame);
      JOptionPane editEventsOptionPane = (JOptionPane) editEventsPane.display(features);
      if (Objects.equals(editEventsOptionPane.getValue(), "Save")) {
        frame.repaint();
      }
    });
  }

  private void importCalendarButton(Features features) {
    importCalendarButton.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setDialogTitle("Import Calendar File");
      int result = fileChooser.showOpenDialog(frame);
      if (result == JFileChooser.APPROVE_OPTION) {
        File file = fileChooser.getSelectedFile();
        try {
          features.importCalendar(file.getAbsolutePath());
          updateCalendar(features);
        } catch (Exception ex) {
          outputResponse(ex.getMessage());
        }
      }
    });
  }

  private void exportCalendarButton(Features features) {
    exportCalendarButton.addActionListener(e -> {
      JTextField fileNameField = new JTextField(15);
      JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
      panel.add(new JLabel("File Name: (.csv)"));
      panel.add(fileNameField);
      int result = JOptionPane.showConfirmDialog(frame,panel,"Export Calendar",
              JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
      if (result == JOptionPane.OK_OPTION && !fileNameField.getText().trim().isEmpty()) {
        try {
          features.exportCalendar(fileNameField.getText());
        } catch (Exception ex) {
          outputResponse(ex.getMessage());
        }
      }
    });
  }

  private void createCalendarButton(Features features) {
    createCalendarButton.addActionListener(e -> {
      JTextField nameField = new JTextField(15);
      JTextField timezoneField = new JTextField(15);

      // Create a panel to hold both fields
      JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
      panel.add(new JLabel("Calendar Name:"));
      panel.add(nameField);
      panel.add(new JLabel("Timezone (e.g., UTC, PST, EST):"));
      panel.add(timezoneField);

      // Show input dialog with both fields
      int result = JOptionPane.showConfirmDialog(frame, panel, "Create Calendar",
              JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

      if (result == JOptionPane.OK_OPTION) {
        String name = nameField.getText();
        String timezone = timezoneField.getText();
        try {
          features.createCalendar(name, timezone);
          calendars = Stream.concat(Arrays.stream(calendars),
                  Stream.of(name)).toArray(String[]::new);
          calendarDropdown.setModel(new DefaultComboBoxModel<>(calendars));
        } catch (Exception exception) {
          outputResponse(exception.getMessage());
        }
      }
    });
  }


  private void updateCalendar(Features features) {
    calendarPanel.removeAll();
    calendarPanel.setLayout(new GridLayout(0, 7));
    monthLabel.setText(currentMonth.getMonth() + " " + currentMonth.getYear());
    //calendarPanel.setBackground(calendars.get(selectedCalendar));

    for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
      LocalDate date = currentMonth.atDay(day);
      String dayOfWeek = date.getDayOfWeek().toString();
      dayOfWeek = dayOfWeek.substring(0, 3);
      JButton dayButton = new JButton("<html><center>" +
              day + "<br>" + dayOfWeek + "</center></html>");
      List<String> checkEvents = features.viewEvents(date);
      if (!checkEvents.isEmpty()) {
        dayButton.setBorder(new LineBorder(Color.RED, 2));
      }
      dayButton.addActionListener(e -> {
        UIPanel dayEventsPanel = new DayEventsPanel(frame, date);
        dayEventsPanel.display(features);
        updateCalendar(features);
      });
      Border originalBorder = dayButton.getBorder();
      dayButton.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
          dayButton.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        }
        @Override
        public void mouseExited(MouseEvent e) {
          dayButton.setBorder(originalBorder);
        }
      });
      calendarPanel.add(dayButton);
    }

    frame.revalidate();
    frame.repaint();
  }

  private void changeMonth(int offset, Features features) {
    currentMonth = currentMonth.plusMonths(offset);
    updateCalendar(features);
  }


  @Override
  public void outputResponse(String data) {
    JOptionPane.showMessageDialog(frame, data, "Message",
            JOptionPane.INFORMATION_MESSAGE);
  }
}
