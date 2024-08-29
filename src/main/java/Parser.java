import java.time.LocalDateTime;
import java.time.DateTimeException;

import java.util.regex.Pattern;

import command.*;
import task.*;

import java.util.regex.Matcher;

public class Parser {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]+");
    private static final Pattern TODO_PATTERN = Pattern.compile("todo (.+)");
    private static final Pattern DEADLINE_PATTERN = Pattern.compile("deadline (.+?) /by (.+)");
    private static final Pattern EVENT_PATTERN = Pattern.compile("event (.+?) /from (.+) /to (.+)");
    private static final Pattern DATETIME_PATTERN = Pattern.compile("(\\d+)[ \\/\\\\\\|\\-\\_](\\d+)[ \\/\\\\\\|\\-\\_](\\d+)[ tT]([\\d:]+) *(am|pm)");
    private static final Pattern FIND_CMD_PATTERN = Pattern.compile("find (.+)");

    private static LocalDateTime parseDateTime(String datetime) {
        Matcher datetimeMatcher = DATETIME_PATTERN.matcher(datetime);

        if (datetimeMatcher.find()) {
            Integer year = Integer.parseInt(datetimeMatcher.group(3));
            year = year > 999 ? year : year + 2000;

            int hours = 0;
            int minutes = 0;
            String[] hoursMinutes = datetimeMatcher.group(4).split(":");
            if (hoursMinutes.length > 1) {
                minutes = Integer.parseInt(hoursMinutes[1]);
            }
            hours = Integer.parseInt(hoursMinutes[0]);
            String timeOfDay = datetimeMatcher.group(5);
            hours = timeOfDay.equalsIgnoreCase("am")
                    ? (hours == 12 ? 0 : hours)
                    : (hours == 12 ? 12 : hours + 12);

            try {
                return LocalDateTime.of(
                    year,
                    Integer.parseInt(datetimeMatcher.group(2)),
                    Integer.parseInt(datetimeMatcher.group(1)),
                    hours,
                    minutes
                );
            } catch (DateTimeException e) {
                return null;
            }
        }
        return null;
    }


    public Command parse(String userInput) {
        if (!userInput.equalsIgnoreCase("bye")) {
            if (userInput.equalsIgnoreCase("list")) {
                // if user input is "list", return the tasklist with statuses
                return new ListTasksCommand();
            } else if (userInput.startsWith("mark")) {
                // else if user input starts with "mark" then mark task X as completed
                Matcher taskNumMatch = NUMBER_PATTERN.matcher(userInput);

                // search for an integer using regex
                if (taskNumMatch.find()) {
                    int taskNum = Integer.parseInt(taskNumMatch.group());
                    // if integer represents a valid task, set it as complete
                    // otherwise reprompt
                    return new TaskCompletionCommand(true, taskNum - 1);
                } else {
                    // if no integer found, reprompt for input
                    // printBotOutputString("\tUhhh sorry what did you wanna mark again?");
                    return new ErrorCommand("\tUhhh sorry what did you wanna mark again?");
                }
            } else if (userInput.startsWith("unmark")) {
                // else if user input starts with "unmark" then mark task X as incomplete
                Matcher taskNumMatch = NUMBER_PATTERN.matcher(userInput);

                // repeat same regex and integer validation process
                if (taskNumMatch.find()) {
                    int taskNum = Integer.parseInt(taskNumMatch.group());
                    return new TaskCompletionCommand(false, taskNum - 1);
                } else {
                    return new ErrorCommand("\tUhhh sorry what did you wanna mark again?");
                }
            } else if (userInput.startsWith("todo")) {
                // add add user input to tasklist as todo
                Matcher taskMatcher = TODO_PATTERN.matcher(userInput);

                if (taskMatcher.find()) {
                    Todo task = new Todo(taskMatcher.group(1));
                    return new AddTaskCommand(task);
                } else {
                    return new ErrorCommand("\tHang on it looks like you haven't given me any task to add!");
                }
            } else if (userInput.startsWith("deadline")) {
                // add user input as deadline task, after parsing out the date and task
                Matcher taskMatcher = DEADLINE_PATTERN.matcher(userInput);

                if (taskMatcher.find()) {
                    LocalDateTime deadline = parseDateTime(taskMatcher.group(2));
                    if (deadline != null) {
                        Deadline task = new Deadline(
                            taskMatcher.group(1),
                            deadline
                        );
                        return new AddTaskCommand(task);
                    } else {
                        return new ErrorCommand("\tHang on! You seem to have given me an invalid date and time!");
                    }
                } else {
                    return new ErrorCommand("\tOops! You haven't given me any task or deadline to add!");
                }
            } else if (userInput.startsWith("event")) {
                // add user input as event task, after parsing out the dates and task
                Matcher taskMatcher = EVENT_PATTERN.matcher(userInput);

                if (taskMatcher.find()) {
                    LocalDateTime startTime = parseDateTime(taskMatcher.group(2));
                    LocalDateTime endTime = parseDateTime(taskMatcher.group(3));
                    if (startTime != null && endTime != null) {
                        Event task = new Event(
                            taskMatcher.group(1),
                            startTime,
                            endTime
                        );
                        return new AddTaskCommand(task);
                    } else {
                        return new ErrorCommand("\tHang on! You seem to have given me an invalid date and time!");
                    }
                } else {
                    return new ErrorCommand("\tHold on! You haven't given me any task or timings to add!");
                }
            } else if (userInput.startsWith("delete")) {
                // else if user input starts with "delete" then delete task X

                Matcher taskNumMatch = NUMBER_PATTERN.matcher(userInput);

                // use same integer finder regex
                if (taskNumMatch.find()) {
                    int taskNum = Integer.parseInt(taskNumMatch.group());
                    return new DeleteTaskCommand(taskNum - 1);
                } else {
                    // if no integer found, reprompt for input
                    return new ErrorCommand("\tUhhh sorry which task did you wish to delete?");
                }
            } else if (userInput.startsWith("find")) {
                Matcher searchTermMatch = FIND_CMD_PATTERN.matcher(userInput);

                if (searchTermMatch.find()) {
                    String searchTerm = searchTermMatch.group(1);
                    return new SearchTaskCommand(searchTerm);
                } else {
                    return new ErrorCommand("\tHang on, what do I need to look for?");
                }
            } else {
                return new ErrorCommand("\tUhhh I did not get that so I'm just gonna say yes!");
            }
        } else {
            return new ExitCommand();
        }
    }
}
