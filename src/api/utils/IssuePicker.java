package api.utils;

import interfaces.TaskManager;
import issues.Epic;
import issues.IssueTypes;
import issues.SubTask;
import issues.Task;

public class IssuePicker<T extends Task> {

    private Task T;

    public T pickIssue(TaskManager manager, Integer issueID, IssueTypes type){
        switch (type){
            case TASK:
                Task task = manager.getTaskById(issueID);
                if (task != null) return (T) task;
                else return null;
            case EPIC:
                Epic epic = manager.getEpicById(issueID);
                if (epic != null) return (T) epic;
                else return null;
            case SUBTASK:
                SubTask subTask = manager.getSubTaskById(issueID);
                if (subTask != null) return (T) subTask;
                else return null;
        }
    return null;
    }
}
