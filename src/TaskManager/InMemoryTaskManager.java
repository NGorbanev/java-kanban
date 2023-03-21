package TaskManager;

import Interfaces.HistoryManager;
import Interfaces.TaskManager;
import Issues.Epic;
import Issues.StatusList;
import Issues.SubTask;
import Issues.Task;
import Utils.Managers;
import com.sun.source.tree.Tree;

import java.time.Duration;
import java.time.Instant;
import java.util.*;



public class InMemoryTaskManager implements TaskManager {

    // собственные поля и хранение данных

    HashMap<Integer, Epic> epicList = new HashMap<>();
    HashMap<Integer, SubTask> subtaskList = new HashMap<>();
    HashMap<Integer, Task> taskList = new HashMap<>();
    HistoryManager history = new Managers().getDefaultHistoryManager();
    private int id;

    // служебные методы

    @Override
    public List<Task> getHistory(){
        return history.getHistory();
    }

    public void setLastId(int newId){ // метод, нужный для работы загрузчика
        if (this.id == 0){
            this.id = newId;
        }
    }

    private int generateId(){
        id = id + 1;
        return id;
    }

    public void checkTimeline() {
        // todo update method for writing correct data to the base
        TreeSet<Task> timeLine = getPrioritizedTasks();
        Iterator<Task> iterator = timeLine.iterator();
        Instant timeT = timeLine.first().getEndTime();
        while (iterator.hasNext()) {
            Task t = iterator.next();
            if (timeT.isAfter(t.getStartTime())) {
                t.setStartTime(timeT);
                timeT = t.getEndTime();
            }
        }
    }

    public TreeSet<Task> getPrioritizedTasks(){
        TreeSet<Task> sortedIssues = new TreeSet<>(new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                if (o1.getStartTime() == null && o2.getStartTime() != null) return -1;
                else if (o1.getStartTime() != null && o2.getStartTime() == null) return 1;
                else if (o1.getStartTime() == null && o2.getStartTime() == null) return 0;
                return o1.getStartTime().compareTo(o2.getStartTime());
            }
        });
        sortedIssues.addAll(taskList.values());
        sortedIssues.addAll(subtaskList.values());
        sortedIssues.addAll(epicList.values());

        return sortedIssues;
    }

    // методы для класса Issues.Epic
    @Override
    public Epic createEpic(String name, String description){
        Epic epic = new Epic(name, description, StatusList.NEW, generateId());
        updateEpic(epic);
        return epic;
    }

    // метод расчета длительности эпика
    public void calculateEpicDuration(Epic epic){
        ArrayList<Integer> subTaskList = epic.getSubTasks();
        Instant firstSubTaskStartTime = Instant.ofEpochMilli(0);
        Long stDurations = 0L;
        for (int i: subTaskList){
            if (this.subtaskList.get(i).getDuration() == null) this.subtaskList.get(i).setDuration(0);
            stDurations = stDurations + this.subtaskList.get(i).getDuration();
            if (this.subtaskList.get(i).getStartTime() == null)
                this.subtaskList.get(i).setStartTime(Instant.ofEpochMilli(0));
            if (firstSubTaskStartTime.isBefore(this.subtaskList.get(i).getStartTime())){
                firstSubTaskStartTime = this.subtaskList.get(i).getStartTime();
            }
        }
        epicList.get(epic.getId()).setStartTime(firstSubTaskStartTime);
        epicList.get(epic.getId()).setDuration((int) (stDurations/60_000));
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
                switch (getSubtasks().get(index).getStatus().toString()){
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
    public List<Epic> getEpicList(){
        return new ArrayList<>(epicList.values());
    }

    @Override
    public void deleteAllEpics(){ // метод переписан, чтобы корректно взаиимодействовать с историей
        for (Epic epic: epicList.values()){
            deleteEpicById(epic.getId());
        }
    }

    @Override
    public Epic getEpicById(int issueId){
        Epic epic = new Epic();
        if (epicList.containsKey(issueId)){
            epic = epicList.get(issueId);
            history.add(epic);
            return epic;
        }
        return null;
    }

    @Override
    public void deleteEpicById(int epicId){
        Epic epic = getEpicById(epicId);
        int subTaskCount = epic.getSubTasks().size();
        for(int i = subTaskCount-1; i >= 0; i--){  // переписан метод удаление сабтасок, потому что старый работал не корректно
            deleteSubTaskById(epic.getSubTasks().get(i));
        }
        history.remove(epicId);
        epicList.remove(epicId);
    }

    @Override
    public List<SubTask> getAllSubtasksByEpicId(int epicId){
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
        epic.addSubTaskToEpic(subTask);
        subTask.setParentEpic(parentEpic);
        subtaskList.put(subTask.getId(), subTask);
        calculateEpicDuration(epic);
        checkStatus(epic);
        //updateEpic(epic);
        return subTask;
    }

    public HashMap<Integer, SubTask> getSubtasks() {
        HashMap <Integer, SubTask> subTasks = subtaskList;
        return subTasks;
    }

    public List<SubTask> getSubtaskList(){
        List<SubTask> sTasks = new ArrayList<>();
        for (SubTask subTask: subtaskList.values()){
            sTasks.add(subTask);
        }
        return sTasks;
    }

    @Override
    public void setSubTaskStatus(SubTask subTask, StatusList newStatus){
        //boolean statusFound = false;
        StatusList[] statusList = subTask.getStatusList();
        for (int i = 0; i <  statusList.length; i++) {
            if (newStatus.equals(statusList[i])){
                //statusFound = true;
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
/*
    @Override
    public void submitSubTask(SubTask issue){
        int parent = issue.getParentEpicId();
        subtaskList.put(issue.getId(), issue); // регистрируем сабтаску в базе
        Epic epic = epicList.get(parent);
        linkSubTask(epic, issue);
        checkStatus(epic);
        updateEpic(epic);
    }
*/
    @Override
    public SubTask getSubTaskById(int issueId){
        SubTask subTask = null;
        if (subtaskList.containsKey(issueId)){
                subTask = subtaskList.get(issueId);
                history.add(subTask);
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
        history.remove(id); // добавлено для удаления сабтаски из истории
        subtaskList.remove(id);
    }

    @Override
    public void deleteAllSubTasks(){ // метод перепсиан для корректного удаления из истории
        ArrayList<Integer> subTasksIds = new ArrayList<>();
        for (SubTask issue: subtaskList.values()) {
            subTasksIds.add(issue.getId());
        }

        for (int i = 0; i < subTasksIds.size(); i++){
            deleteSubTaskById(subTasksIds.get(i));
        }
    }

    // методы Issues.Task
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
        for (int i = 0; i < statusList.length; i++) {
            if (newStatus.equals(statusList[i])){
                task.setStatus(statusList[i]);
                updateTask(task);

            }
        }
    }

    @Override
    public void deleteAllTasks(){ // метод переписан для корректного удаления из истории
        for (Task issue: taskList.values()){
            deleteTaskById(issue.getId());
        }
    }

    @Override
    public Task getTaskById(int id){
        Task foundTask = null;
        if (taskList.containsKey(id)) {
            foundTask = taskList.get(id);
            history.add(foundTask);
        }
        return foundTask;
    }

    @Override
    public void deleteTaskById(int id){
        history.remove(id);
        taskList.remove(id);
    }

    @Override
    public List<Task> getTaskList(){
        List<Task> tList = new ArrayList();
        for (Task task: taskList.values()){
            if (task != null) tList.add(task);
        }
        return tList;
    }
}
