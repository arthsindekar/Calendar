package controller;

import java.util.Set;

import model.ICalendar;

/**
 * Stores all the calendar model objects used for creating, editing events.
 * Maintains a collection of calendar models and current calendar.
 */
public class CalendarData {

  private final Set<ICalendar> calendarCollection;
  private final ICalendar calendarModel;
  private ICalendar currentCalendar;

  CalendarData(Set<ICalendar> calendarCollection, ICalendar calendarModel) {
    this.calendarCollection = calendarCollection;
    this.calendarModel = calendarModel;
    this.currentCalendar = null;
  }

  Set<ICalendar> getCalendarCollection() {
    return calendarCollection;
  }

  ICalendar getCalendarModel() {
    return calendarModel;
  }

  ICalendar getCurrentCalendar() {
    return currentCalendar;
  }

  void setCurrentCalendar(ICalendar currentCalendar) {
    this.currentCalendar = currentCalendar;
  }
}
