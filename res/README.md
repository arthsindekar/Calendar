# **README FILE**

Instructions on how to run the app.
Remember: After each command, please press enter, otherwise you won’t see the result.
Date time string should be specified in format “yyyy-mm-ddThh:mm”

1.Download and use the Assignment6.jar file and open the command prompt navigate to
the folder where the jar file is present and type any one of the following commands
and press ENTER to use the file in different modes

 i. GUI mode: java -jar Assignment6.jar

 ii. Interactive mode: java -jar Assignment6.jar --mode interactive

 iii. Headless mode: java -jar Assignment6.jar --mode headless path-of-script-file

the above commands are case-insensitive
For headless mode enter path of the ValidCommands.txt file in place of path-of-script-file
All the commands in the script file must be valid with last command as exit, otherwise if a
command is invalid the application stops at that point.


2.Application runs in three modes Headless and Interactive and GUI
For using the application in GUI mode please refer to the provided USEME file

3.If mode is interactive then the user can give the following commands:
and if mode is headless the following commands can be given in the script file
User can now create multiple calendars with a dedicated timezone.
To create a calendar, use this command:
create calendar --name <calName> --timezone area/location.
OR
create calendar –timezone area/location –name <calName>

**To edit a calendar**

edit calendar --name <name-of-calendar> --property <property-name> <new-property-value>
OR
edit calendar --property <property-name> <new-property-value> --name <name-of-calendar>

**To use a calendar**

use calendar --name <name-of-calendar>

Before using the following commands, the calendar context must be set by use command
otherwise user cannot use event commands.

create event --autoDecline <event-Name> from <date-StringTtime-String> to <date-StringTtime-String>

**Create command**: auto decline does not matter now,
even if it is specified or not conflicting events will be rejected
example:  create event –autoDecline "Meeting One" from 2025-03-05T02:30 to 2025-03-05T03:30
If from is entered, then both start date time and end date time must be specified.
To create an all-day event: Enter only the date on which you want to create event.
create event --autoDecline <event-Name> on <date-StringTtime-String>
example: create event --autoDecline "Meeting One" on 2025-03-05.


4.**To create a recurring event:** Recurring event are not created if there is a conflicting event.
Example: create event --autoDecline Meeting1 from 2025-03-05T02:30 to 2025-03-05T03:30 repeats
MSF for 8 times
The “times” keyword and the letters for the week are mandatory. “MSF” defines on which weekday
the event repeats. This field cannot be null.
The following letters define the weekdays from Monday to Sunday.
Monday - 'M’, Tuesday 'T', Wednesday-'W', Thursday-'R', Friday -'F',
Saturday-'S', Sunday-'U'.

**To create a recurring event until a specific date:**
“until” and days of week are mandatory for this command.
Example:
create event Meeting1 from 2025-03-05T02:30 to 2025-03-05T03:30 repeats MSF until 2025-03-08

5.**Edit event command:**
Edits single event
edit event <property-> <event-Name> from <date-StringTtime-String> to <date-StringTtime-String> with
<NewPropertyValue>
Properties that can be changed: subject, startDate ,startTime, endDate ,endTime, description,
location,isPrivate.
The description, location and isPrivate are optional fields that user may choose to edit.

**Edit recurring events:**
Properties that can be changed: subject, startDate ,startTime, endDate ,endTime, description,
location, isPrivate, daysOFWeek, occurrences , specificEndDateTime
Changes the property (e.g., name) of all events starting at a specific date/time and have
the same event name.
edit events <property-> <event-Name> <New-Property-Value>
edit events description “Meeting One” discussion

**Print event command:**
Print events on a defined date.
print events on <date-String>
If user enters ‘on’ keyword, then user must specify a date of format “yyyy-mm-dd”

Print events within specific date range:
print events from <date-StringTtime-String> to <date-StringTtime-String>
if user specifies ‘to’ keyword then user must specify date and time of format “yyyy-mm-ddThh-mm”

**Export Events:** This command exports all events in csv file format which adheres to google calendar.
Example: export cal fileName.csv the filename must have csv specified and ‘cal’ keyword must
come after export.

**Show Status command:**
Prints busy status if the user has events scheduled on a given day and time, otherwise, available
show status on <dateStringTtimeString>
if user specifies on keyword, then user must specify date and time. Both must be specified.

**Copy Event Command:**
Copies event from one calendar to a specified calendar
copy event <event-Name> on <date-StringTtime-String> --target <calendar-Name>
to <date-StringTtime-String>
This command copies a specific event starting at the given start date time to the target
calendar specified by calendar name at the mentioned target date time.

copy events on <date-String> --target <calendar-Name> to <date-String>
This command copies all the vents on that day from the current calendar to the target
 calendar adjusting to the timezone of target calendar at the specified target calendar date.

copy events between <date-String> and <date-String> --target <calendar-Name> to <date-String>
This command copies all events between the given dates to the target calendar adjusting to
the target calendar timezone and starting on the specified target calendar date.

**Exit command**
To stop the program, use the ‘exit’ and press enter.

**Features working: All features are working.**

**Additions made in the model:**


1.	All the additions made to the calendar model interface and concrete class were 
minimal as only required getters and setters were added for two new fields name
and timezone which naturally define a model representing a calendar, so we made
changes to existing class and did not extend a new interface for it.

2.	Other changes like export events changed signature of method as it was previous
doing IO in model which was a design limitation so changed return type and removed
IO code from its implementation.

3.	Added field Timezone timezone so that each calendar will have a specific time zone.
We added this field to the calendar model because the assignment mentioned to have a
time zone for each calendar. Added field String name so that each calendar
can be uniquely identified.

4.	Added public TimeZone getTimeZone() Reason : This method was added to retrieve
the time zone in the controller since the logic to copy the time zone needs that field.
This logic is completely written outside the model.
5.	Added public void setTimeZone() Reason: This method was added to set
the timezone from the controller. While editing and creating a calendar
the timezone is set by this method.

6.	Added public String getName() Reason : This method was added to retrieve the name of
the calendar from the controller so that we can find the calendar to operate on.

7.	Added public void setName() Reason: This method was added to set the name of the calendar
from the controller. This method is used by edit and create calendar commands.

8.	Added public ICalendar getCalendarCopy() Reason: This method was added to get a copy of
the calendar that we are currently working on in the controller. Without this method we
cannot create a new calendar. To avoid creation of concrete creation of Calendar class,
we used this method to create a new ICalendar object from the model passed by the CalendarApp class.

9.	Added implementation of equals and hash code method as we want to maintain a set of
multiple unique model objects in controller.

10.	Added package private methods such as AbstractEvent convertTimeZone(TimeZone timeZone,
TimeZone newTimeZone), LocalDateTime convertTimeZoneHelper(TimeZone timeZone,TimeZone newTimeZone,
       LocalDateTime dateTime),EventBuilder getEventBuilderWithNewTimeZone(TimeZone timeZone,
TimeZone newTimeZone)Reason: These methods were added so that when the user changes the timezone,
the timezone of each event will also be converted.

11.	Added package private method in Abstract, single and recurring event builders to set any
event property this method was present in calendar class before as it was editing only events
for recurring event now it dynamically dispatches to single or recurring event class.

12.	Added a different package private method to an event copy builder by getting a event builder of
the event instances after or before a particular date time.

13.	Added a private method to check conflict by passing an AbstractEvent object, before it used to
check conflict only using a start date time and end date time.


**Changes after Assignment 4**
**Changes in the model:**
1.	Export Events method was changed because the earlier model was working with IO classes.
Now the controller deals with File IO. The method boolean exportEvents(String fileName) was
changed to List<List<String>> exportEvents(); This method was changed because we did not consider
that controller was supposed to work with FileIO.

2.	Changed the edit event logic to allow editing of single events also when editing multiple
events after a datetime. Removed the part where it was checking if it was a recurring event or
not and delegated the setting of event properties to the event classes.


**Additions in the controller:**
1.	Added CalendarData class in which we are storing set of multiple ICalendars and also maintains
current calendar model. It has package private getters and setters for currentCalendar field so that
we can use it in the other command classes. Initially current calendar is null so no one can create
edit or print

2.	Added CreateCalendarCommand Class Reason: It can deal with the additional requirements of
dealing with creating a calendar at a time and adding to the calendar collection in CalendarData.

3.	Added EditCalendarCommand class Reason: It can deal with the additional requirements of dealing
with editing multiple calendars name and timezone.

4.	Added UseCalendarCommand class Reason: It can deal with the additional requirements of dealing
changing the current calendar context to the specified calendar.

5.	Added EditCommand Class Reason: It can delegate the edit command to two different commands
which include EditCalendarCommand and EditEventCommand. This provides a better approach to editing
because this approach accounts for future edit commands that can be easily added in this design.

6.	Added CreateCommand Class Reason: It can delegate the create command to two different commands
which include CreateCalendarCommand and CreateEventCommand. This provides a better approach to
editing because this approach accounts for future create commands that can be easily
added in this design.

7.	Added CopyEventCommand Class Reason: It can deal with the additional requirements of dealing
with copying events between multiple calendars. All logic is present in the controller so that
model did not have to change. It uses existing print event command on current calendar and create
event command on target calendar and conflicts work same as it works for create event commands.

8.	Added CheckNullCalendar method in all event command classes to check whether the current
calendar context is selected otherwise it throws an error because by requirement event cannot
be created unless calendar is selected.



**Changes in controller:**
1.	We were already using command design pattern in Assignment4 so almost no changes were required
in calendar controller class only new addition of command classes were done with some changes in
constructor signatures of these command classes.

2.	Used Function<CalendarData, Command>> Reason: We can pass the calendar data to the different
command classes through the constructors of command classes, initially we used Supplier as
we didn’t need any data to be passed to the individual command classes through constructor,
but now some of them need access to the calendarData which contains current calendar and
calendar collection set.

3. Autodecline is set as true by default while creating an event to satisfy the requirements
where conflicting events are not allowed. Model did not change for this requirement.

4. All constructors of the event command classes now accept the calendar data object to set
the current calendar context.

**Changes after Assignment 5**

**Changes in the model:**
None

**Changes in controller:**
Added a Features interface inside the controller package to offer callback methods
for GUI view to register on action listeners.
Added a new GraphicalController concrete class which implements
Features and ICalendarController interfaces.
No changes in existing controller concrete classes.

**Changes in the view:**
Added a new implementation of ICalendarView interface, GUICalendarView which also extends JFrame
and offers a method to add call back functions to the action listeners of view swing objects.

**Distribution of work:**

Utkarsh Milind Khursale

Create and Edit Events
Create, Edit, Print,
Copy Commands, 
Create Event view, Edit event view,
Edit multiple event view
print event view

Arth Sindekar

Print, Show, and Export Events
Show and Export Commands,
Use, Edit Calendar, Create calendar
Create calendar view, use calendar dropdown,
import calendar, export calendar view



``