import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Fret {
    private static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]+");
    private static final Pattern TODO_PATTERN = Pattern.compile("todo (.+)");
    private static final Pattern TODO_PATTERN2 = Pattern.compile("\\[T\\]\\[( |X)\\] (.+)");
    private static final Pattern DEADLINE_PATTERN = Pattern.compile("deadline (.+?) /by (.+)");
    private static final Pattern DEADLINE_PATTERN2 = Pattern.compile("\\[D\\]\\[( |X)\\] (.+) \\(by: (.+)\\)");
    private static final Pattern EVENT_PATTERN = Pattern.compile("event (.+?) /from (.+) /to (.+)");
    private static final Pattern EVENT_PATTERN2 = Pattern.compile("\\[E\\]\\[( |X)\\] (.+) \\(from: (.+), to: (.+)\\)");
    private static final Random RNG = new Random();

    private static final String[] ADD_TASK_PREFIXES = new String[] {
        "\tYou got it! Adding task:\n\t",
        "\tYou got it! Adding task:\n\t",
        "\tYou got it! Adding task:\n\t",
        "\tYou sure wanna do that?\n\t",
        "\tAlright! Adding task:\n\t",
        "\tAlright! Adding task:\n\t",
        "\tAlright! Adding task:\n\t",
        "\tOn it! Task added:\n\t",
        "\tOn it! Task added:\n\t",
        "\tWhatever you say! *nervous laughter*:\n\t",
        "\tHmmmm..... Done. Task added:\n\t",
        "\tWorking..... Done. Task added:\n\t",
    };

    private static final String[] TASK_COMPLETED_PREFIXES = new String[] {
        "\tNice! Task marked as completed\n",
        "\tNice! Task marked as completed\n",
        "\tNice! Task marked as completed\n",
        "\tAlright! Task marked as completed\n",
        "\tPhew! Got that one out of the way!\n",
        "\tPhew! Got that one out of the way!\n",
        "\tWahoo! Task complete!\n",
        "\tWahoo! Task complete!\n",
        "\tDone and done! Should we do it again?\n",
        "\tDone and done! Should we do it again?\n"
    };

    private static final String[] TASK_UNCOMPLETED_PREFIXES = new String[] {
        "\tMarking as incomplete.\n",
        "\tBooooo\n",
        "\tBooooo\n",
        "\tBooooo\n",
        "\tAw man. Task marked as incomplete\n",
        "\tAw man. Task marked as incomplete\n",
        "\tOh well. Marking task as incomplete\n",
        "\tOh well. Marking task as incomplete\n",
        "\tDamn. Thought we had that.\n",
        "\tAw man. Thought we had that.\n",
        "\tDamn. Thought we had that.\n"
    };

    private static final String[] TASK_DELETED_PREFIXES = new String[] {
        "\tO-Okay, deleting task now.\n",
        "\tAlright, task removed.\n",
        "\tWhatever you say! Task deleted.\n",
        "\tCtrl-alt-del\n",
        "\tWatch me make this task disappear!\n"
    };

    /**
     * Randomly selects and returns a prefix for a command from a given list
     * 
     * @param prefixes
     * @return a randomly selected prefix from the given list of prefixes
     */
    private static String generateRandomPrefix(String[] prefixes) {
        return prefixes[RNG.nextInt(prefixes.length)];
    }

    /**
     * Creates and returns an enumeration of the tasks added and stored by the user
     * 
     * @param tasks the list of tasks
     * @param numTasks the number of tasks
     * @return an enumeration of the tasks
     */
    private static String taskListToString(ArrayList<Task> tasks, int numTasks) {
        if (numTasks == 0) {
            return "\tempty";
        }

        try {
            String[] tempTasks = new String[numTasks];

            for (int i = 1; i <= numTasks; i++) {
                tempTasks[i - 1] = "\t" + i + ". " + tasks.get(i - 1).toString();
            }

            return String.join("\n", tempTasks);
        } catch (NullPointerException e) {
            System.out.println("Oops! It seems like there's an invalid task in your list!\nI can't display the list yet.");
            return "";
        }
    }

    /**
     * Prints chatbot output to console with surrounding lines
     * 
     * @param output the output string
     */
    private static void printBotOutputString(String output) {
        System.out.println("\t-----------------------------------------");
        System.out.println(output);
        System.out.println("\t-----------------------------------------");
    }

    private static Task processTaskLine(String taskLine) {
        // try to match the task as a todo
        Matcher todoMatcher = TODO_PATTERN2.matcher(taskLine);

        if (todoMatcher.find()) {
            Todo todo = new Todo(todoMatcher.group(2));
            if (todoMatcher.group(1).equals("X")) {
                todo.markAsCompleted();
            }
            return todo;
        }

        // otherwise try to match task as a deadline
        Matcher deadlineMatcher = DEADLINE_PATTERN2.matcher(taskLine);

        if (deadlineMatcher.find()) {
            Deadline deadline = new Deadline(deadlineMatcher.group(2), deadlineMatcher.group(3));
            if (deadlineMatcher.group(1).equals("X")) {
                deadline.markAsCompleted();
            }
            return deadline;
        }

        // finally try to match task as an event
        Matcher eventMatcher = EVENT_PATTERN2.matcher(taskLine);

        if (eventMatcher.find()) {
            Event event = new Event(eventMatcher.group(2), eventMatcher.group(3), eventMatcher.group(4));
            if (eventMatcher.group(1).equals("X")) {
                event.markAsCompleted();
            }
            return event;
        }

        // if all fails, return null and handle exception
        return null;
    }

    private static boolean loadTasksFromMemory(File taskFile, ArrayList<Task> taskList) {
        try {
            Scanner taskFileReader = new Scanner(taskFile);
            while (taskFileReader.hasNextLine()) {
                String data = taskFileReader.nextLine();
                taskList.add(processTaskLine(data));
            }
            taskFileReader.close();
        } catch (FileNotFoundException e) {
            try {
                taskFile.createNewFile();
            } catch (IOException f) {
                f.printStackTrace();
                System.out.println("Error with task file. Fret shutting down...");
                return false;
            }
        }
        
        return true;
    }

    public static void main(String[] args) {
        // declare the list of tasks
        ArrayList<Task> tasks = new ArrayList<>();

        String logo = "________                ___ \n"
                + "| _____|             ___| |___ \n"
                + "| |___  __   _   ___ |__   __|\n"
                + "| ___|  | |/ /  / _ \\   | |\n"
                + "| |     |   /  <  __/   | |__\n"
                + "|_|     |__|    \\___|   |___|\n";
        
        System.out.println("Initiating...\n" + logo);

        printBotOutputString("\tPersonal AI Fret, coming online!\n\tHey, how can I be of assistance?");

        Scanner input = new Scanner(System.in);
        String userInput;

        // open task-list file and read tasks into list
        String taskFilePath;
        if (System.getProperty("os.name").startsWith("Windows")) {
            taskFilePath = "data\\taskList.txt";
        } else {
            taskFilePath = "data/taskList.txt";
        }
        File taskFile = new File(taskFilePath);
        if (!loadTasksFromMemory(taskFile, tasks)) {
            input.close();
            return;
        }

        int numTasks = tasks.size();

        do {
            userInput = input.nextLine().toLowerCase(); // get user input and make it small letters

            if (!userInput.equals("bye")) {
                if (userInput.equals("list")) {
                    // if user input is "list", print the tasklist with statuses
                    printBotOutputString(taskListToString(tasks, numTasks));
                } else if (userInput.startsWith("mark")) {
                    // else if user input starts with "mark" then mark task X as completed

                    Matcher taskNumMatch = NUMBER_PATTERN.matcher(userInput);

                    // search for an integer using regex
                    if (taskNumMatch.find()) {
                        int taskNum = Integer.parseInt(taskNumMatch.group());
                        // if integer represents a valid task, set it as complete
                        // otherwise reprompt
                        try {
                            tasks.get(taskNum - 1).markAsCompleted();
                            printBotOutputString(
                                generateRandomPrefix(TASK_COMPLETED_PREFIXES) + taskListToString(tasks, numTasks)
                            );
                        } catch (NullPointerException | IndexOutOfBoundsException e) {
                            printBotOutputString("\tOops! You don't have those many tasks!");
                        }
                    } else {
                        // if no integer found, reprompt for input
                        printBotOutputString("\tUhhh sorry what did you wanna mark again?");
                    }
                } else if (userInput.startsWith("unmark")) {
                    // else if user input starts with "unmark" then mark task X as incomplete

                    Matcher taskNumMatch = NUMBER_PATTERN.matcher(userInput);

                    // repeat same regex and integer validation process
                    if (taskNumMatch.find()) {
                        int taskNum = Integer.parseInt(taskNumMatch.group());
                        try {
                            tasks.get(taskNum - 1).markAsNotCompleted();
                            printBotOutputString(
                                generateRandomPrefix(TASK_UNCOMPLETED_PREFIXES) + taskListToString(tasks, numTasks));
                        } catch (NullPointerException e) {
                            printBotOutputString("\tOops! You don't have those many tasks!");
                        }
                    } else {
                        printBotOutputString("\tUhhh hang on what did you want to unmark?");
                    }
                } 
                else if (userInput.startsWith("todo")) {
                    // add add user input to tasklist as todo
                    Matcher taskMatcher = TODO_PATTERN.matcher(userInput);

                    if (taskMatcher.find()) {
                        Todo task = new Todo(taskMatcher.group(1));
                        tasks.add(task);
                        numTasks++;
                        printBotOutputString(generateRandomPrefix(ADD_TASK_PREFIXES) + task.toString());
                    } else {
                        printBotOutputString("\tHang on it looks like you haven't given me any task to add!");
                    }
                } else if (userInput.startsWith("deadline")) {
                    // add user input as deadline task, after parsing out the date and task
                    Matcher taskMatcher = DEADLINE_PATTERN.matcher(userInput);

                    if (taskMatcher.find()) {
                        Deadline task = new Deadline(taskMatcher.group(1), taskMatcher.group(2));
                        tasks.add(task);
                        numTasks++;
                        printBotOutputString(generateRandomPrefix(ADD_TASK_PREFIXES) + task.toString());
                    } else {
                        printBotOutputString("\tOops! You haven't given me any task and deadline to add!");
                    }
                } else if (userInput.startsWith("event")) {
                    // add user input as event task, after parsing out the dates and task
                    Matcher taskMatcher = EVENT_PATTERN.matcher(userInput);

                    if (taskMatcher.find()) {
                        Event task = new Event(
                            taskMatcher.group(1),
                            taskMatcher.group(2),
                            taskMatcher.group(3)
                        );
                        tasks.add(task);
                        numTasks++;
                        printBotOutputString(generateRandomPrefix(ADD_TASK_PREFIXES) + task.toString());
                    } else {
                        printBotOutputString("\tHold on! You haven't given me any task and timings to add!");
                    }
                } else if (userInput.startsWith("delete")) {
                    // else if user input starts with "delete" then delete task X

                    Matcher taskNumMatch = NUMBER_PATTERN.matcher(userInput);

                    // use same integer finder regex
                    if (taskNumMatch.find()) {
                        int taskNum = Integer.parseInt(taskNumMatch.group());

                        try {
                            tasks.remove(taskNum - 1);
                            numTasks--;
                            printBotOutputString(
                                generateRandomPrefix(TASK_DELETED_PREFIXES) + taskListToString(tasks, numTasks)
                            );
                        } catch (NullPointerException | IndexOutOfBoundsException e) {
                            printBotOutputString("\tHold on a second! You don't have those many tasks!");
                        }
                    } else {
                        // if no integer found, reprompt for input
                        printBotOutputString("\tUhhh sorry which task did you wish to delete?");
                    }
                }
                else {
                    printBotOutputString("\tUhhh I did not get that so I'm just gonna say yes!");
                }
            }
        } while (!userInput.equals("bye"));

        // once user enters "bye", leave the loop and exit the program

        // but first store the tasks back into the text file
        try {
            FileWriter taskFileWriter = new FileWriter(taskFile);
            taskFileWriter.write(taskListToString(tasks, numTasks));
            taskFileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Oops! There was an error when trying to store your tasks.");
            input.close();
            return;
        }

        input.close();

        printBotOutputString("\tOh well, it was fun while it lasted. Goodbye!");
    }
}
