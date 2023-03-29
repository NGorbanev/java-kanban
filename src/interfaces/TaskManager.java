package interfaces;

import issues.Epic;
import issues.StatusList;
import issues.SubTask;
import issues.Task;

import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;


public interface TaskManager {

    // service methods
    public List<Task> getHistory();

    // methods of Issues.Epic class
    public Epic createEpic(Epic epic);

    public List<Epic> getEpicList();

    public void deleteAllEpics();

    public Epic getEpicById(int issueId);

    public void deleteEpicById(int epicID);

    public List<SubTask> getAllSubtasksByEpicId(int epicId);

    public void updateEpic(Epic issue);

    // methods of Issues.SubTask class
    public SubTask createSubTask(SubTask subTask);

    public HashMap<Integer, SubTask> getSubtasks();

    public List<SubTask> getSubtaskList();

    public void setSubTaskStatus(SubTask subTask, StatusList newStatus);

    public void linkSubTask(Epic epic, SubTask subTask);

    public SubTask getSubTaskById(int issueId);

    public void updateSubTask(SubTask issue);

    public void deleteSubTaskById(int id);

    public void deleteAllSubTasks();

    // methods of Issues.Task class
    public Task createTask(Task task);

    public void updateTask(Task task);

    public void setTaskStatus(Task task, StatusList newStatus);

    public void deleteAllTasks();

    public Task getTaskById(int id);

    public void deleteTaskById(int id);
    public List<Task> getTaskList();

    public void calculateEpicDuration(Epic testEpic);

    public TreeSet<Task> getPrioritizedTasks();
}
