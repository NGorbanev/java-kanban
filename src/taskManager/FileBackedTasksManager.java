package taskManager;

import issues.*;
import utils.ManagerSaveException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

public class FileBackedTasksManager extends InMemoryTaskManager {
    // variable for path to file with data
    Path path;
    File file;

    Converter converter = new Converter(); // helping class for converting issue to string

    // constructor
    public FileBackedTasksManager(String pathToFile){
        this.path = Paths.get(pathToFile);
        this.file = new File(path.toString());
        loadFromFile(file);
    }

    // own methods
    // saving to file
    public void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path.toString()))) {
            bw.write("id,type,name,status,description,startDate,duration,parentEpic" + System.lineSeparator());

            // saving epics
            for (Task epic: epicList.values()){
                bw.write(converter.convertToString(epic) + System.lineSeparator());
            }
            // saving subtasks
            for (Task subTask: subtaskList.values()){
                bw.write(converter.convertToString(subTask) + System.lineSeparator());
            }
            // saving tasks
            for (Task task: taskList.values()){
                bw.write(converter.convertToString(task) + System.lineSeparator());
            }
            // saving history
            bw.write("\n");
            for (Task item: history.getHistory()){
                bw.write(item.getId() + ",");
            }
        } catch (IOException e) {
            throw new ManagerSaveException(e, "Ошибка сохранения файла");
        }
    }

    public void loadFromFile(File file) {

        // updating data for file
        this.file = file;
        this.path = file.toPath();

        // clearing hashmap with data for recording new data from file
        taskList.clear();
        epicList.clear();
        subtaskList.clear();

        String[] data = new String[0];
        try {
            data = Files.readString(Path.of(file.toURI())).split(System.lineSeparator());
            if (data == null || data.length == 0){ // if file is blank - just info at console and no more tries of reading it
                throw new ManagerSaveException("Файл с данными для загрузки пустой");
            }
        } catch (IOException e) {
            new File(file.getPath()); // creating file if no exists
        }
        int lastId = 0; // is needed for setting proper id for new issues after loading data
        for (int i = 1; i < data.length; i++){
            // first load all epics, then subtasks and tasks
            if (!data[i].isEmpty()){ // in case of empty file
                String[] line = data[i].split(",");
                StatusList status = StatusList.valueOf(line[3]);
                int index = Integer.valueOf(line[0]); // variable for setting issueID
                if (index > lastId) lastId = index; // need to find max value of index to set proper issueID

                switch (line[1]){
                    case "EPIC":
                        if (line[5].equals("null")) line[5] = Instant.ofEpochMilli(0).toString();
                        if (line[6].equals("null")) line[6] = "0";
                        Instant epicStartDate = Instant.parse(line[5]);
                        int epicDuration = Integer.parseInt(line[6]);
                        Epic epic = new Epic(line[2], line[4], status, index, epicStartDate, epicDuration);
                        epicList.put(index,epic); // epic goes with id=2 as expected
                        break;
                    case "SUBTASK":
                        if (line[5].equals("null")) line[5] = Instant.ofEpochMilli(0).toString();
                        if (line[6].equals("null")) line[6] = "0";
                        int parentEpicId = Integer.valueOf(line[7]);
                        Epic parentEpic = epicList.get(parentEpicId);
                        Instant subTaskStartDate = Instant.parse(line[5]);
                        int subTaskDuration = Integer.parseInt(line[6]);
                        SubTask subTask = new SubTask(line[2], line[4], status, parentEpicId, index, subTaskStartDate, subTaskDuration);
                        parentEpic.addSubTaskToEpic(subTask); // epic ID is 2. The query is looking for an epic with id=2. Seems to be ok here
                        subTask.setParentEpic(parentEpic.getId());
                        subtaskList.put(index, subTask);
                        epicList.put(parentEpicId, parentEpic);
                        break;
                    case "TASK":
                        if (line[5].equals("null")) line[5] = Instant.ofEpochMilli(0).toString();
                        if (line[6].equals("null")) line[6] = "0";
                        Instant taskStartDate = Instant.parse(line[5]);
                        int taskDuration = Integer.parseInt(line[6]);
                        Task task = new Task(line[2], line[4], status, index, taskStartDate, taskDuration);
                        taskList.put(index, task);
                        break;
                }
            } else {
                break; // if the file is empty - don't load anything
            }
        }
        setLastId(lastId);

        // loading history
        if (data == null || data.length == 0){
            System.out.println("Файл с данными для загрузки пустой\n");
            return;
        } else if (data.length > 1){ // check if the file is not empty and there is anything but header of csv
            String[] historyLine = data[data.length - 1].split(",");
            for (int i = 0; i < historyLine.length; i++) {
                try { // if the file is damaged of there is no saved ID for history - method processing should be skipped
                    int issueId = Integer.parseInt(historyLine[i]);
                    if (epicList.containsKey(issueId)) {
                        history.add(getEpicById(issueId));
                    }
                    if (subtaskList.containsKey(issueId)) {
                        history.add(getSubTaskById(issueId));
                    }
                    if (taskList.containsKey(issueId)) {
                        history.add(getTaskById(issueId));
                    }
                }
                catch (NumberFormatException e){
                    throw new ManagerSaveException(e, "В файле нет записей об истории обращения к задачам");
                }
            }
        } // if the file hasn't even headers - don't load anything

    }

    /**
     * Helping class for writing to file
     * method convertToString
     * Converts tasks to String depending on type
     */
    class Converter<T>{
        public String convertToString (T issue){
            if (issue instanceof SubTask){
                String output = ((SubTask) issue).getId() + "," +
                        IssueTypes.SUBTASK + "," +
                        ((SubTask) issue).getName() + "," +
                        ((SubTask) issue).getStatus() + "," +
                        ((SubTask) issue).getDescription() + "," +
                        ((SubTask) issue).getStartTime() + "," +
                        ((SubTask) issue).getDuration() + "," +
                        ((SubTask) issue).getParentEpicId() + "";
                return output;
            } else if (issue instanceof Epic){
               String output = ((Epic) issue).getId() + "," +
                        IssueTypes.EPIC + "," +
                        ((Epic) issue).getName() + "," +
                        ((Epic) issue).getStatus() + "," +
                        ((Epic) issue).getDescription() + "," +
                        ((Epic) issue).getStartTime() + "," +
                        ((Epic) issue).getDuration();
                return output;
            } else if (issue instanceof Task){
                String output = ((Task) issue).getId() + "," +
                        IssueTypes.TASK + "," +
                        ((Task) issue).getName() + "," +
                        ((Task) issue).getStatus() + "," +
                        ((Task) issue).getDescription() + "," +
                        ((Task) issue).getStartTime() + "," +
                        ((Task) issue).getDuration();
                return output;
            }
            return null;
        }
    }

    // Overrided methods
    // Epic
    @Override
    public void setLastId(int newId){
        super.setLastId(newId);
    }

    @Override
    public Epic createEpic(Epic epic){
        Epic ep = super.createEpic(epic);
        save();
        return ep;
    }

    @Override
    public void deleteAllEpics(){
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteEpicById(int epicId){
        super.deleteEpicById(epicId);
        save();
    }

    @Override
    public void updateEpic(Epic issue){
        super.updateEpic(issue);
        save();
    }

    //@Override
    //public void calculateEpicDuration(Epic epic){
    //    super.calculateEpicDuration(epic);
    //}

    // SubTask

    @Override
    public SubTask createSubTask(SubTask subTask){
        SubTask st = super.createSubTask(subTask);
        save();
        return st;
    }

    @Override
    public void setSubTaskStatus(SubTask subTask, StatusList newStatus){
        super.setSubTaskStatus(subTask, newStatus);
        save();
    }

    @Override
    public void linkSubTask(Epic epic, SubTask subTask){
        super.linkSubTask(epic, subTask);
        save();
    }

    @Override
    public void updateSubTask(SubTask issue){
        super.updateSubTask(issue);
        save();
    }

    @Override
    public void deleteSubTaskById(int id){
        super.deleteSubTaskById(id);
        save();
    }

    @Override
    public void deleteAllSubTasks(){
        super.deleteAllSubTasks();
        save();
    }

    // Task
    @Override
    public Task createTask(Task task){
        Task t = super.createTask(task);
        save();
        return t;
    }


    @Override
    public void updateTask(Task task){
        super.updateTask(task);
        save();
    }

    @Override
    public void setTaskStatus(Task task, StatusList newStatus){
        super.setTaskStatus(task, newStatus);
        save();
    }

    @Override
    public void deleteAllTasks(){
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteTaskById(int id){
        super.deleteTaskById(id);
        save();
    }

    @Override
    public Epic getEpicById(int issueId){
        Epic epic = super.getEpicById(issueId);
        save();
        return epic;
    }
    @Override
    public SubTask getSubTaskById(int issueId){
        SubTask subTask = super.getSubTaskById(issueId);
        save();
        return subTask;
    }

    @Override
    public Task getTaskById(int id){
        Task task = super.getTaskById(id);
        save();
        return task;
    }
}
