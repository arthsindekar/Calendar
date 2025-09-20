package view;

import java.awt.Dialog;
import java.util.Objects;

import javax.swing.JOptionPane;

import controller.Features;

/**
 * {@code ActionPanel} is an abstract class for building user interface components
 * within the calendar application and add an action after to perform on the input values.
 * It provides common utility methods for event parsing, date/time conversion,
 * and spinner setup that can be reused across multiple UI panels.
 *
 * <p>Subclasses must implement the {@code display} and {@code performAction} method
 * to render specific UI content and connect it with controller {@link Features}.
 *
 * <p>This class supports Swing-based views and promotes consistency and reuse of
 * shared UI logic.
 */
public abstract class ActionPanel extends UIPanel {

  abstract boolean performAction(Features features);

  void userAction(Features features, Dialog dialog, JOptionPane optionPane) {
    while (true) {
      dialog.setVisible(true);
      if (Objects.equals(optionPane.getValue(), "Save")) {
        if (performAction(features)) {
          break;
        }
        else {
          optionPane.setValue("Cancel");
        }
      }
      else {
        break;
      }
    }
  }

}
