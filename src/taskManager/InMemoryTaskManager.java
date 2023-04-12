package taskManager;

import interfaces.HistoryManager;
import interfaces.TaskManager;
import issues.Epic;
import issues.StatusList;
import issues.SubTask;
import issues.Task;
import utils.Managers;
import utils.TimeLineCrossingsException;

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

    private boolean taskCrossingsCheck(Task task){
        for (Task issue : getPrioritizedTasks()){
            if (issue.getStartTime() != issue.getEndTime()) {
                if ((task.getStartTime().isBefore(issue.getStartTime()) && task.getEndTime().isBefore(issue.getStartTime()) ||
                        task.getStartTime().isAfter(issue.getEndTime()))) {
                    return false;
                } else throw new TimeLineCrossingsException(
                        "Task crossings check failure:\n" +
                                "ID=" + task.getId() + " Start: " + task.getStartTime() + " End: " + task.getEndTime() +"\n" +
                                "ID=" + issue.getId() + " Start: " + issue.getStartTime() + " End: " + issue.getEndTime());
            }
        }
        return true;
    }


    // new method for receiving tasks ordered by priority (based on StartTime)
    public TreeSet<Task> getPrioritizedTasks(){

        // method was rewritten according review recommendations
        TreeSet<Task> sortedIssues = new TreeSet<>(Comparator.comparing(Task::getStartTime));

        sortedIssues.addAll(taskList.values());
        sortedIssues.addAll(subtaskList.values());
        return sortedIssues;
    }

    // methods for Issues.Epic class

    @Override
    public Epic createEpic(Epic epic){
        if (epic == null) return null;
        // just for no having NULL at key fields:
        epic.setDuration(0L);
        epic.setStartTime(Instant.ofEpochMilli(0));
        epic.setStatus(StatusList.NEW);
        epic.setEndTime(Instant.ofEpochMilli(0));
        // if everything is ok - generate ID and register issue at the manager
        epic.setId(generateId());
        updateEpic(epic);
        return epic;
    }

    // epic duration calculation method
    /**
     * Method logic description.
     * Limitations:
     * - epic knows only ID's of it's subtasks. Without manager epic's endTime can't be calculated inside Epic class
     * - field endTime is easier to leave as it is at Task class. The sprint technical task s also contains:
     * "EndTime() — время завершения задачи, которое рассчитывается исходя из startTime и duration.". It also contains
     * text about need to work with Epic additionally, but seems that there is no really need to do such individual implementation
     * at Epic class level. Manager can manage it easily.
     *
     * Realisation:
     * 1. get start time of a first subtask
     * 2. get end time of a last subtask
     * 3. Duration would make a difference between start time of first subtask and end time of the last one
     * 4. As we don't save endDate of Task and Epic as well, the epic should only have start time and duration.
     * End time of epic should be calculated every time, but it will always be equal to last subtask end time
     *
     * Result:
     * 1. Epic endTime is still can be calculated, using same as at Task / SubTask logic
     * 2. Additional unit tests were added to calculateEpicDurationTest()
     */
    private void calculateEpicDuration(Epic epic){

        ArrayList<Integer> subTaskList = epic.getSubTasks();
        Instant firstSubTaskStartTime = Instant.ofEpochMilli(0);
        Instant lastSubTaskEndTime = Instant.ofEpochMilli(0);
        Long subTasksDuration = 0L;
        for (int i: subTaskList){
            if (firstSubTaskStartTime.isAfter(this.subtaskList.get(i).getStartTime())){
                firstSubTaskStartTime = this.subtaskList.get(i).getStartTime();
            }
            if (lastSubTaskEndTime.isBefore(this.subtaskList.get(i).getEndTime())){
                lastSubTaskEndTime = this.subtaskList.get(i).getEndTime();
            }
            subTasksDuration = subTasksDuration + this.subtaskList.get(i).getDuration();
        }
        epicList.get(epic.getId()).setStartTime(firstSubTaskStartTime);
        epicList.get(epic.getId()).setDuration(subTasksDuration);
        epicList.get(epic.getId()).setEndTime(lastSubTaskEndTime);
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
        if (subtaskList.size()>0) subtaskList.clear();
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

    @Override
    public SubTask createSubTask(SubTask subTask){
        if (subTask == null) return null;
        // a few tests for not having NULL at key fields
        if (subTask.getParentEpicId() == 0) return null;
        if (getEpicById(subTask.getParentEpicId()) == null) return null; // check for not-existent epic
        if (subTask.getStartTime() == null) subTask.setStartTime(Instant.ofEpochMilli(0));
        if (subTask.getDuration() == null) subTask.setDuration(0L);
        if (subTask.getStatus() == null) subTask.setStatus(StatusList.NEW);
        // ex-timeline check
        taskCrossingsCheck(subTask);
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
        if (getEpicById(issue.getParentEpicId()) == null) return;
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

    @Override
    public Task createTask(Task task){
        if (task != null) {
            // few checks for not having NULL at key fields
            if (task.getStartTime() == null) task.setStartTime(Instant.ofEpochMilli(0));
            if (task.getDuration() == null) task.setDuration(0L);
            if (task.getStatus() == null) task.setStatus(StatusList.NEW);
            // ex checkTimeLine
            taskCrossingsCheck(task);
            // if everything is ok - generate ID and register issue at the manager
            task.setId(generateId());
            taskList.put(task.getId(), task);
            return task;
        }
        else return null;
    }


    @Override
    public void updateTask(Task task){
        //taskCrossingsCheck(task);
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
