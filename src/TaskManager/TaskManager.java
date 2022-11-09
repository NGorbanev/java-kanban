package TaskManager;

import Issues.Epic;
import Issues.SubTask;
import Issues.Task;

import java.util.ArrayList;
import java.util.HashMap;


public class TaskManager {

    // собственные поля и хранение данных
    private static HashMap<Integer, Epic> epicList = new HashMap<>();
    private static HashMap<Integer, SubTask> subtaskList = new HashMap<>();
    private static HashMap<Integer, Task> taskList = new HashMap<>();
    private static int id; // счетчик для ID задач

    // служебные методы
    public int generateId(){
        id = id + 1;
        return id;
    }

    public int getLastId(){
        return id;
    }


    // методы для класса Issues.Epic
    public Epic createEpic(String name, String description){
        Epic epic = new Epic(name, description, 0, generateId());
        //epic.name = name;
        //epic.description = description;
        //epic.setId(generateId());
        updateEpic(epic);
        return epic;
    }

    // метод расчета статус эпика, в зависимости от статуса подзадач
    public void checkStatus(Epic targetEpic){
        Epic epic = targetEpic;
        ArrayList<Integer> subTasksList = targetEpic.getSubTasks();
        int statusCounter = subTasksList.size();
        boolean isInProgress = false;
        boolean isDONE = false;
        if (statusCounter > 0) {
            for (int index : subTasksList) {
                switch (getSubtaskList().get(index).getStatus()){
                    case ("IN_PROGRESS"): {
                        isInProgress = true;
                        break;
                    }
                    case ("DONE"): {
                        statusCounter--;
                        isInProgress = true;
                    }
                }
            }
            if (statusCounter <= 0) isDONE = true;
        }
        if (!isDONE && isInProgress) epic.setStatus(1);
        else if (isDONE) epic.setStatus(2);
        else epic.setStatus(0);
        updateEpic(epic);
    }

    public HashMap<Integer, Epic> getEpicList(){
        HashMap <Integer, Epic> epics = epicList;
        return epics;
    }

    public void deleteAllEpics(){
        epicList.clear();
        subtaskList.clear();
    }

    public Epic getEpicById(int issueId){
        Epic epic = new Epic();
        if (epicList.containsKey(issueId)){
            for(int i = 0; i < epicList.size(); i++){
                epic = epicList.get(issueId);
            }
            return epic;
        }
        return null;
    }

    public void deleteEpicById(int epicID){
        Epic epic = getEpicById(epicID);
        for(int i = 0; i < epic.getSubTasks().size(); i++){
            subtaskList.remove(epic.getSubTasks().get(i)); // удаляем связанные сабтаски
        }
        epicList.remove(epicID); // удаляем эпик
    }

    public ArrayList<SubTask> getAllSubtasksByEpicId(int epicId){
        Epic epic = epicList.get(epicId);
        ArrayList<SubTask> subTasks = new ArrayList<>();
        for (int i = 0; i < epic.getSubTasks().size(); i++){
            if (epic.getSubTasks().get(i) != null){
                subTasks.add(subtaskList.get(epic.getSubTasks().get(i)));
            }
        }
        return subTasks;
    }

    public void updateEpic(Epic issue){
        epicList.put(issue.getId(), issue);
    }

    // методы Issues.SubTask
    public SubTask createSubTask(String name, String description, int parentEpic){
        Epic epic = getEpicById(parentEpic);
        SubTask subTask = new SubTask(name, description, 0, parentEpic, generateId());
        //subTask.name = name;
        //subTask.description = description;
        //subTask.setId(generateId());
        epic.addSubTaskToEpic(subTask);
        subTask.setParentEpic(parentEpic);
        subtaskList.put(subTask.getId(), subTask);
        return subTask;
    }

    public HashMap<Integer, SubTask> getSubtaskList() {
        HashMap <Integer, SubTask> subTasks = subtaskList;
        return subTasks;
    }

    public void setSubTaskStatus(SubTask subTask, String newStatus){
        boolean statusFound = false;
        String[] statusList = subTask.getStatusList();
        for (int i = 0; i <  statusList.length; i++) {
            if (newStatus.equals(statusList[i])){
                statusFound = true;
                subTask.setStatus(i);
                updateSubTask(subTask);
            }
        }
    }

    public void linkSubTask(Epic epic, SubTask subTask){
        epic.addSubTaskToEpic(subTask);
        SubTask newChildSubtask = subTask;
        newChildSubtask.setParentEpic(epic.getId());
        updateSubTask(newChildSubtask);
    }

    public void submitSubTask(SubTask issue){
        int parent = issue.getParentEpicId();
        subtaskList.put(issue.getId(), issue); // регистрируем сабтаску в базе
        Epic epic = epicList.get(parent);
        linkSubTask(epic, issue);
        checkStatus(epic);
        updateEpic(epic);
    }

    public SubTask getSubTaskById(int issueId){
        SubTask subTask = null;
        if (subtaskList.containsKey(issueId)){
                subTask = subtaskList.get(issueId);
        }
        return subTask;
    }

    public void updateSubTask(SubTask issue){
        subtaskList.put(issue.getId(), issue);
        checkStatus(epicList.get(issue.getParentEpicId()));
        updateEpic(epicList.get(issue.getParentEpicId()));
    }

    public void deleteSubTaskById(int id){
        SubTask subTask = subtaskList.get(id);
        Epic epic = epicList.get(subTask.getParentEpicId());
        epic.unlinkSubtask(id);
        updateEpic(epic);
        subtaskList.remove(id);
    }

    public void deleteAllSubTasks(){
        for (int i = 0; i <= getLastId(); i++){
            if (subtaskList.get(i) != null){
                deleteSubTaskById(subtaskList.get(i).getId());
            }
        }
    }

    // матоды Issues.Task
    public Task createTask(String name, String description){
        Task task = new Task(name, description, 0, generateId());
        taskList.put(task.getId(), task);
        return task;
    }

    public void updateTask(Task task){
        taskList.put(task.getId(), task);
    }

    public void setTaskStatus(Task task, String newStatus){
        boolean statusFound = false;
        String[] statusList = task.getStatusList();
        for (int i = 0; i <  statusList.length; i++) {
            if (newStatus.equals(statusList[i])){
                statusFound = true;
                task.setStatus(1);
                updateTask(task);

            }
        }
    }

    public void deleteAllTasks(){
        taskList.clear();
    }

    public Task getTaskById(int id){
        Task foundTask = null;
        if (taskList.containsKey(id)) {
            foundTask = taskList.get(id);
        }
        return foundTask;
    }

    public void deleteTaskById(int id){
        taskList.remove(getTaskById(id).getId());
    }

    public HashMap<Integer, Task> getTaskList(){
        return taskList;
    }
}
