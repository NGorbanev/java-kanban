package TaskManager;

import Interfaces.TaskManager;
import Issues.Epic;
import Issues.StatusList;
import Issues.SubTask;
import Issues.Task;

import java.util.ArrayList;
import java.util.HashMap;


public class InMemoryTaskManager implements TaskManager {

    // собственные поля и хранение данных
    private int id;

    // служебные методы
    @Override
    public int generateId(){
        id = id + 1;
        return id;
    }
    @Override
    public int getLastId(){
        return id;
    }


    // методы для класса Issues.Epic
    @Override
    public Epic createEpic(String name, String description){
        Epic epic = new Epic(name, description, StatusList.NEW, generateId());
        updateEpic(epic);
        return epic;
    }

    // метод расчета статус эпика, в зависимости от статуса подзадач
    @Override
    public void checkStatus(Epic targetEpic){
        Epic epic = targetEpic;
        ArrayList<Integer> subTasksList = targetEpic.getSubTasks();
        int statusCounter = subTasksList.size();
        boolean isInProgress = false;
        boolean isDONE = false;
        if (statusCounter > 0) {
            for (int index : subTasksList) {
                switch (getSubtaskList().get(index).getStatus().toString()){
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
        if (!isDONE && isInProgress) epic.setStatus(StatusList.IN_PROGRESS);
        else if (isDONE) epic.setStatus(StatusList.DONE);
        else epic.setStatus(StatusList.NEW);
        updateEpic(epic);
    }

    @Override
    public ArrayList<Epic> getEpicList(){
        return new ArrayList<>(epicList.values());
    }

    @Override
    public void deleteAllEpics(){
        epicList.clear();
        subtaskList.clear();
    }

    @Override
    public Epic getEpicById(int issueId){
        Epic epic = new Epic();
        if (epicList.containsKey(issueId)){
            epic = epicList.get(issueId);
            history.addToHistory(epic);
            return epic;
        }
        return null;
    }

    @Override
    public void deleteEpicById(int epicID){
        Epic epic = getEpicById(epicID);
        for(int i = 0; i < epic.getSubTasks().size(); i++){
            subtaskList.remove(epic.getSubTasks().get(i)); // удаляем связанные сабтаски
        }
        epicList.remove(epicID); // удаляем эпик
    }

    @Override
    public ArrayList<SubTask> getAllSubtasksByEpicId(int epicId){
        if (!epicList.containsKey(epicId)) {
            return null;
        }
        Epic epic = epicList.get(epicId);
        ArrayList<SubTask> subTasks = new ArrayList<>();
        for (int i = 0; i < epic.getSubTasks().size(); i++){
            if (epic.getSubTasks().get(i) != null){
                subTasks.add(subtaskList.get(epic.getSubTasks().get(i)));
            }
        }
        return subTasks;
    }

    @Override
    public void updateEpic(Epic issue){
        epicList.put(issue.getId(), issue);
    }

    // методы Issues.SubTask
    @Override
    public SubTask createSubTask(String name, String description, int parentEpic){
        Epic epic = getEpicById(parentEpic);
        SubTask subTask = new SubTask(name, description, StatusList.NEW, parentEpic, generateId());
        //subTask.name = name;
        //subTask.description = description;
        //subTask.setId(generateId());
        epic.addSubTaskToEpic(subTask);
        subTask.setParentEpic(parentEpic);
        subtaskList.put(subTask.getId(), subTask);
        return subTask;
    }

    @Override
    public HashMap<Integer, SubTask> getSubtaskList() {
        HashMap <Integer, SubTask> subTasks = subtaskList;
        return subTasks;
    }

    @Override
    public void setSubTaskStatus(SubTask subTask, StatusList newStatus){
        boolean statusFound = false;
        StatusList[] statusList = subTask.getStatusList();
        for (int i = 0; i <  statusList.length; i++) {
            if (newStatus.equals(statusList[i])){
                statusFound = true;
                subTask.setStatus(statusList[i]);
                updateSubTask(subTask);
            }
        }
    }

    @Override
    public void linkSubTask(Epic epic, SubTask subTask){
        epic.addSubTaskToEpic(subTask);
        SubTask newChildSubtask = subTask;
        newChildSubtask.setParentEpic(epic.getId());
        updateSubTask(newChildSubtask);
    }

    @Override
    public void submitSubTask(SubTask issue){
        int parent = issue.getParentEpicId();
        subtaskList.put(issue.getId(), issue); // регистрируем сабтаску в базе
        Epic epic = epicList.get(parent);
        linkSubTask(epic, issue);
        checkStatus(epic);
        updateEpic(epic);
    }

    @Override
    public SubTask getSubTaskById(int issueId){
        SubTask subTask = null;
        if (subtaskList.containsKey(issueId)){
                subTask = subtaskList.get(issueId);
                history.addToHistory(subTask);
        }
        return subTask;
    }

    @Override
    public void updateSubTask(SubTask issue){
        subtaskList.put(issue.getId(), issue);
        checkStatus(epicList.get(issue.getParentEpicId()));
    }

    @Override
    public void deleteSubTaskById(int id){
        SubTask subTask = subtaskList.get(id);
        Epic epic = epicList.get(subTask.getParentEpicId());
        epic.unlinkSubtask(id);
        checkStatus(epic);
        updateEpic(epic);
        subtaskList.remove(id);
    }

    @Override
    public void deleteAllSubTasks(){
        for (int i = 0; i <= getLastId(); i++){
            if (subtaskList.get(i) != null){
                deleteSubTaskById(subtaskList.get(i).getId());
            }
        }
    }

    // матоды Issues.Task
    @Override
    public Task createTask(String name, String description){
        Task task = new Task(name, description, StatusList.NEW, generateId());
        taskList.put(task.getId(), task);
        return task;
    }

    @Override
    public void updateTask(Task task){
        taskList.put(task.getId(), task);
    }

    @Override
    public void setTaskStatus(Task task, StatusList newStatus){
        StatusList[] statusList = task.getStatusList();
        for (int i = 0; i <  statusList.length; i++) {
            if (newStatus.equals(statusList[i])){
                task.setStatus(statusList[i]);
                updateTask(task);

            }
        }
    }

    @Override
    public void deleteAllTasks(){
        taskList.clear();
    }

    @Override
    public Task getTaskById(int id){
        Task foundTask = null;
        if (taskList.containsKey(id)) {
            foundTask = taskList.get(id);
            history.addToHistory(foundTask);
        }
        return foundTask;
    }

    @Override
    public void deleteTaskById(int id){
        taskList.remove(getTaskById(id).getId());
    }

    @Override
    public HashMap<Integer, Task> getTaskList(){
        return taskList;
    }
}
