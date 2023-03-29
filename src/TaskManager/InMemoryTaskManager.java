package TaskManager;

import Interfaces.HistoryManager;
import Interfaces.TaskManager;
import Issues.Epic;
import Issues.StatusList;
import Issues.SubTask;
import Issues.Task;
import Utils.Managers;
import Utils.TimeLineCrossingsException;

import java.time.Instant;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    // own fields and data storage
    HashMap<Integer, Epic> epicList = new HashMap<>();
    HashMap<Integer, SubTask> subtaskList = new HashMap<>();
    HashMap<Integer, Task> taskList = new HashMap<>();
    HistoryManager history = new Managers().getDefaultHistoryManager();
    private int id;

    // service methods
    @Override
    public List<Task> getHistory(){
        return history.getHistory();
    }

    public void setLastId(int newId){
        if (this.id == 0){
            this.id = newId;
        }
    }

    private int generateId(){
        id = id + 1;
        return id;
    }
/*
    public void checkTimeline() {
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
*/
    public boolean taskCrossingsCheck(Task task){
        for (Task issue : getPrioritizedTasks()){
            if (issue.getStartTime() != issue.getEndTime()) {
                if (task.getStartTime().isAfter(issue.getStartTime()) &&
                        task.getEndTime().isBefore(issue.getEndTime())) {
                    throw new TimeLineCrossingsException("Пересечение с задачей ID=" + issue.getId());
                }
            }
        }
        return false;
    }


    // new method for receiving tasks ordered by priority (based on StartTime)
    public TreeSet<Task> getPrioritizedTasks(){

        // method was rewritten according review recommendations
        TreeSet<Task> sortedIssues = new TreeSet<>(Comparator.comparing(Task::getStartTime));

        sortedIssues.addAll(taskList.values());
        sortedIssues.addAll(subtaskList.values());
        sortedIssues.addAll(epicList.values());

        return sortedIssues;
    }

    // methods for Issues.Epic class
    /* todo clean the comments
    @Override
    public Epic createEpic(String name, String description){
        Epic epic = new Epic(name, description, StatusList.NEW, generateId());
        epic.setDuration(0L);
        epic.setStartTime(Instant.ofEpochMilli(0));
        updateEpic(epic);
        return epic;
    }
    */

    @Override
    public Epic createEpic(Epic epic){
        if (epic == null) return null;
        epic.setDuration(0L);
        epic.setStartTime(Instant.ofEpochMilli(0));
        epic.setStatus(StatusList.NEW);
        epic.setId(generateId());
        updateEpic(epic);
        return epic;
    }

    // epic duration calculation method
    public void calculateEpicDuration(Epic epic){

        /**
         * Method logic description.
         * Limitations:
         * - epic knows only ID's of it's subtasks. Without manager epic's endTime can't be calculated inside Epic class
         * - field endTime is easier to leave as it is at Task class. The technical task s also contains:
         * "EndTime() — время завершения задачи, которое рассчитывается исходя из startTime и duration.". It also contains
         * text about need to work with Epic additionally, but seems that there is no really need to do such individual implemetation
         * at Epic class level. Manager can manage it easily too.
         *
         * Realisation:
         * 1. get all subtasks duration sum and set epic duration equals this sum. stDurations contains it.
         * 2. Find out the start time - it should be equal to first subtask startTime. firstSubTaskStartTime contains it
         * 3. Calculate epic duration by adding stDurations to firstSubTaskStartTime
         *
         * Result:
         * 1. Epic endTime is still to be calculated, same as at Task / SubTask
         * 2. Additional tests were added to calculateEpicDurationTest()
         */

        ArrayList<Integer> subTaskList = epic.getSubTasks();
        Instant firstSubTaskStartTime = Instant.ofEpochMilli(0);
        Instant lastSubTaskEndTime = Instant.ofEpochMilli(0);
        for (int i: subTaskList){
            if (firstSubTaskStartTime.isAfter(this.subtaskList.get(i).getStartTime())){
                firstSubTaskStartTime = this.subtaskList.get(i).getStartTime();
            }
            if (lastSubTaskEndTime.isBefore(this.subtaskList.get(i).getEndTime())){
                lastSubTaskEndTime = this.subtaskList.get(i).getEndTime();
            }
        }
        epicList.get(epic.getId()).setStartTime(firstSubTaskStartTime);
        epicList.get(epic.getId()).setDuration(lastSubTaskEndTime.toEpochMilli()
                - firstSubTaskStartTime.toEpochMilli());
    }

    // method of epic status calculation in dependency of subtasks statuses
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
    public void deleteAllEpics(){
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
        for(int i = subTaskCount-1; i >= 0; i--){
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

    // methods for Issues.SubTask class

    /*  todo clean the comments
    @Override
    public SubTask createSubTask(String name, String description, int parentEpic){
        Epic epic = getEpicById(parentEpic);
        SubTask subTask = new SubTask(name, description, StatusList.NEW, parentEpic, generateId());
        epic.addSubTaskToEpic(subTask);
        subTask.setParentEpic(parentEpic);
        subTask.setStartTime(Instant.ofEpochMilli(0)); // added to guarantee not NULL value
        subTask.setDuration(0L); // added to guarantee not NULL value
        subtaskList.put(subTask.getId(), subTask);
        calculateEpicDuration(epic);
        checkStatus(epic);
        return subTask;
    }
*/
    @Override
    public SubTask createSubTask(SubTask subTask){
        if (subTask == null) return null;
        if (subTask.getParentEpicId() == 0) return null;
        if (subTask.getStartTime() == null) subTask.setStartTime(Instant.ofEpochMilli(0));
        if (subTask.getDuration() == null) subTask.setDuration(0L);
        if (subTask.getStatus() == null) subTask.setStatus(StatusList.NEW);
        try{
            taskCrossingsCheck(subTask);
        } catch (TimeLineCrossingsException ex){
            return null;
        }
        subTask.setId(generateId());
        updateSubTask(subTask);
        linkSubTask(getEpicById(subTask.getParentEpicId()), subTask);
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
        calculateEpicDuration(epic);
        updateSubTask(newChildSubtask);
    }

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
        calculateEpicDuration(getEpicById(issue.getParentEpicId()));
        checkStatus(epicList.get(issue.getParentEpicId()));
    }

    @Override
    public void deleteSubTaskById(int id){
        SubTask subTask = subtaskList.get(id);
        Epic epic = epicList.get(subTask.getParentEpicId());
        epic.unlinkSubtask(id);
        checkStatus(epic);
        calculateEpicDuration(getEpicById(subTask.getParentEpicId()));
        updateEpic(epic);
        history.remove(id);
        subtaskList.remove(id);
    }

    @Override
    public void deleteAllSubTasks(){
        ArrayList<Integer> subTasksIds = new ArrayList<>();
        for (SubTask issue: subtaskList.values()) {
            subTasksIds.add(issue.getId());
        }

        for (int i = 0; i < subTasksIds.size(); i++){
            deleteSubTaskById(subTasksIds.get(i));
        }
    }

    // methods for Issues.Task class

    //todo clean the comments
    /*
    @Override
    public Task createTask(String name, String description){
        Task task = new Task(name, description, StatusList.NEW, generateId());
        task.setStartTime(Instant.ofEpochMilli(0));
        task.setDuration(0L);
        taskList.put(task.getId(), task);
        return task;
    }
    */
    @Override
    public Task createTask(Task task){
        if (task != null) {
            if (task.getStartTime() == null) task.setStartTime(Instant.ofEpochMilli(0));
            if (task.getDuration() == null) task.setDuration(0L);
            if (task.getStatus() == null) task.setStatus(StatusList.NEW);
            try{
                taskCrossingsCheck(task);
            } catch (TimeLineCrossingsException exception) {
                //System.out.println(this.getClass().getName() +" | " + task.getName() + ": " + exception.getMessage());
                return null;
            }
            task.setId(generateId());
            taskList.put(task.getId(), task);
            return task;
        }
        else return null;
    }


    @Override
    public void updateTask(Task task){
        try{
            taskCrossingsCheck(task);
        } catch (TimeLineCrossingsException exception) {
            System.out.println(exception.getMessage() + ". Задача ID=" + task.getId() + " не обновлена");
            return;
        }
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
    public void deleteAllTasks(){
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
