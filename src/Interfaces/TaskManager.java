package Interfaces;

import History.InMemoryHistoryManager;
import Issues.Epic;
import Issues.StatusList;
import Issues.SubTask;
import Issues.Task;

import java.util.HashMap;
import java.util.List;


public interface TaskManager {

    // служебные методы
    public List<Task> getHistory();

    public int getLastId();

    // методы для класса Issues.Epic
    public Epic createEpic(String name, String description);

    // метод расчета статус эпика, в зависимости от статуса подзадач
    public void checkStatus(Epic targetEpic);

    public List<Epic> getEpicList();

    public void deleteAllEpics();

    public Epic getEpicById(int issueId);

    public void deleteEpicById(int epicID);

    public List<SubTask> getAllSubtasksByEpicId(int epicId);

    public void updateEpic(Epic issue);

    // методы Issues.SubTask
    public SubTask createSubTask(String name, String description, int parentEpic);

    public HashMap<Integer, SubTask> getSubtaskList();

    public void setSubTaskStatus(SubTask subTask, StatusList newStatus);

    public void linkSubTask(Epic epic, SubTask subTask);

    public void submitSubTask(SubTask issue);

    public SubTask getSubTaskById(int issueId);

    public void updateSubTask(SubTask issue);

    public void deleteSubTaskById(int id);

    public void deleteAllSubTasks();

    // матоды Issues.Task
    public Task createTask(String name, String description);

    public void updateTask(Task task);

    public void setTaskStatus(Task task, StatusList newStatus);

    public void deleteAllTasks();

    public Task getTaskById(int id);

    public void deleteTaskById(int id);
    public HashMap<Integer, Task> getTaskList();
}
