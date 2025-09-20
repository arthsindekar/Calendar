package controller;


/**
 * An interface for handling calendar-related user input.
 * Implementing classes should define how input is received and processed.
 * This can be used to listen for user commands or interactions with a calendar system.
 */
public interface ICalendarController {

  /**
   * Listens for user input and processes it accordingly.
   * Handles retrieval, validation, and execution of appropriate calendar-related actions.
   */
  void listenInput();
}
