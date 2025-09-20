package controller;

import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import model.ICalendar;
import view.ICalendarView;

/**
 * This class extends the {@code Command} class and represents the functionalities to edit a
 * calendar. The properties of calendar ie "name" and "timezone" can be modified in this class.
 */
public class EditCalendarCommand extends Command {

  EditCalendarCommand(CalendarData calendarData) {
    this.calendarCollection = calendarData.getCalendarCollection();
  }


  /**
   * This method implements the logic to edit a calendar property. The parsing and
   * editing of a calendar is done inside this method itself.
   * @param calendar     the calendar instance on which the command operates
   * @param calendarView the view component for displaying calendar-related information
   * @param tokens       a list of string tokens representing parsed command input
   * @throws IllegalArgumentException if Invalid Arguments have been passed
   *                                     Calendar name that has been changed already exists.
   */
  @Override
  void execute(ICalendar calendar, ICalendarView calendarView, List<String> tokens)
          throws IllegalArgumentException {
    String calendarName;
    String property;
    String value;

    if (tokens.size() == 7) {
      if (tokens.get(2).equals("--name")) {
        calendarName = tokens.get(3);
        if (tokens.get(4).equals("--property")) {
          if (tokens.get(5).equals("name") || tokens.get(5).equals("timezone")) {
            property = tokens.get(5);
            value = tokens.get(6);
            if (property.equals("name")) {
              editName(calendarName, calendarCollection, value);
              calendarView.outputResponse("Edited calendar '" + calendarName + "'" +
                      " with name '" + value + "'\n");
            } else {
              editTimeZone(calendarName, calendarCollection, value);
              calendarView.outputResponse("Edited calendar '" + calendarName + "'" +
                      " with timezone '" + value + "'\n");
            }
          } else {
            throw new IllegalArgumentException("Invalid property. Expected 'name' or 'timezone'.");
          }
        } else {
          throw new IllegalArgumentException("Invalid command. Expected '--property'");
        }

      } else if (tokens.get(2).equals("--property")) {
        if (tokens.get(3).equals("name") || tokens.get(3).equals("timezone")) {
          property = tokens.get(3);
          value = tokens.get(4);

        } else {
          throw new IllegalArgumentException("Invalid property. Expected 'name' or 'timezone'.");
        }
        if (tokens.get(5).equals("--name")) {
          calendarName = tokens.get(6);
          if (property.equals("name")) {
            editName(calendarName, calendarCollection, value);
            calendarView.outputResponse("Edited calendar '" + calendarName + "'" +
                    " with name '" + value + "'");
          }
          else {
            editTimeZone(calendarName, calendarCollection, value);
            calendarView.outputResponse("Edited calendar '" + calendarName + "'" +
                    " with timezone '" + value + "'");
          }
        }
        else {
          throw new IllegalArgumentException("Invalid command. Expected '--name'.");
        }
      }
      else {
        throw new IllegalArgumentException("Invalid command. Expected '--name' or '--property'.");
      }
    }
    else {
      throw new IllegalArgumentException("Invalid Arguments.");
    }
  }



  private void editName(String calendarName, Set<ICalendar> calendarCollection,
                        String newName)
          throws IllegalArgumentException {

    checkUnavailableCalendars(calendarName);
    //dummy calendar to check if it already exists.
    if (getCalendarUsingName(newName) != null) { //calendar exists.
      //provided new name matches with already existing calendar.
      throw new IllegalArgumentException("Calendar already exists.");
    }
    //remove old calendar and create new one with new name.Copy
    // events and fields to new calendar.
    ICalendar calendar = getCalendarUsingName(calendarName);
    calendar.setName(newName);

  }

  private void editTimeZone(String calendarName, Set<ICalendar> calendarCollection,
                            String newTimeZone) throws IllegalArgumentException {
    checkUnavailableCalendars(calendarName);
    TimeZone tz = checkTimezone(newTimeZone);
    ICalendar calendar = getCalendarUsingName(calendarName);
    calendar.setTimeZone(tz);
  }
}

