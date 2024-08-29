public class ListTasksCommand extends Command {
    public ListTasksCommand() {
        super(0, null);
    }

    public String execute(TaskList tasks) {
        return tasks.taskListToString();
    }    
}
