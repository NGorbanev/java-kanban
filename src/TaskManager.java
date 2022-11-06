import java.util.ArrayList;
import java.util.HashMap;


public class TaskManager {

    // собственные поля и хранение данных
    public static HashMap<Integer, Epic> epicList = new HashMap<>();
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


    // методы для класса Epic
    public HashMap<Integer, Epic> getEpicList(){
        return epicList;
    }

    public void deleteAllEpics(){
        for (int i = 0; i < getLastId(); i++){
            if (epicList.get(i) != null){
                deleteEpicById(i);
            }
        }
    }

    public Epic getEpicById(int issueId){
        Epic epic = new Epic();
        if (epicList.containsKey(issueId)){
            for(int i = 0; i < epicList.size(); i++){
                epic = epicList.get(issueId);
            }
        }
        return epic;
    }

    public void deleteEpicById(int epicID){
        Epic epic = getEpicById(epicID);
        for(int i = 0; i < epic.showSubTasks().size(); i++){
            subtaskList.remove(epic.showSubTasks().get(i)); // удаляем связанные сабтаски
        }
        epicList.remove(epicID); // удаляем эпик
    }

    public ArrayList<SubTask> getAllSubtasksByEpicId(int epicId){
        Epic epic = epicList.get(epicId);
        ArrayList<SubTask> subTasks = new ArrayList<>();
        for (int i = 0; i < epic.showSubTasks().size(); i++){
            if (epic.showSubTasks().get(i) != null){
                subTasks.add(subtaskList.get(epic.showSubTasks().get(i)));
            }
        }
        return subTasks;
    }

    public void submitEpic(Epic issue){
        epicList.put(issue.getId(), issue); // регистрируем епик в базе
    }

    public void updateEpic(Epic issue){
        epicList.put(issue.getId(), issue);
    }

    // методы SubTask
    public HashMap<Integer, SubTask> getSubtaskList() {
        return subtaskList;
    }

    public void submitSubTask(SubTask issue){
        int parent = issue.getParentEpicId();
        subtaskList.put(issue.getId(), issue); // регистрируем сабтаску в базе
        Epic epic = epicList.get(parent);
        epic.linkSubTask(issue.getId());
        epic.checkStatus();
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
        epicList.get(issue.getParentEpicId()).checkStatus();
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

    // матоды Task
    public void submitTask(Task task){
        taskList.put(task.getId(), task);
    }

    public void updateTask(Task task){
        taskList.put(task.getId(), task);
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
