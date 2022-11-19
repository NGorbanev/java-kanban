package Interfaces;

import History.History;
import Issues.Epic;
import Issues.SubTask;
import Issues.Task;

import java.util.ArrayList;
import java.util.HashMap;


public interface TaskManager {

    // собственные поля и хранение данных
    HashMap<Integer, Epic> epicList = new HashMap<>();
    HashMap<Integer, SubTask> subtaskList = new HashMap<>();
    HashMap<Integer, Task> taskList = new HashMap<>();
    History history = new History();
    Integer id = null; // счетчик для ID задач

    // служебные методы
    public int generateId();

    public int getLastId();

    // методы для класса Issues.Epic
    public Epic createEpic(String name, String description);

    // метод расчета статус эпика, в зависимости от статуса подзадач
    public void checkStatus(Epic targetEpic);

    public ArrayList<Epic> getEpicList();

    public void deleteAllEpics();

    public Epic getEpicById(int issueId);

    public void deleteEpicById(int epicID);

    public ArrayList<SubTask> getAllSubtasksByEpicId(int epicId);

    public void updateEpic(Epic issue);

    // методы Issues.SubTask
    public SubTask createSubTask(String name, String description, int parentEpic);

    public HashMap<Integer, SubTask> getSubtaskList();

    public void setSubTaskStatus(SubTask subTask, String newStatus);

    public void linkSubTask(Epic epic, SubTask subTask);

    public void submitSubTask(SubTask issue);

    public SubTask getSubTaskById(int issueId);

    public void updateSubTask(SubTask issue);

    public void deleteSubTaskById(int id);

    public void deleteAllSubTasks();

    // матоды Issues.Task
    public Task createTask(String name, String description);

    public void updateTask(Task task);

    public void setTaskStatus(Task task, String newStatus);

    public void deleteAllTasks();

    public Task getTaskById(int id);

    public void deleteTaskById(int id);
    public HashMap<Integer, Task> getTaskList();
}
