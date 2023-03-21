package TaskManager;

import Issues.*;
import Utils.ManagerSaveException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.TreeSet;

public class FileBackedTasksManager extends InMemoryTaskManager {
    // переменная для пути к файлу с сохранениями
    Path path;
    File file;

    Converter converter = new Converter(); // вспомогательный класс для конвертации issue в строку

    // конструктор
    public FileBackedTasksManager(String pathToFile){
        this.path = Paths.get(pathToFile);
        this.file = new File(path.toString());
        loadFromFile(file);
    }

    public FileBackedTasksManager(){

    }

    public static void main(String[] args) {
        FileBackedTasksManager fbm = new FileBackedTasksManager("./src/Data/test.csv");
/*        fbm.createEpic("TestEpic", "Desc test.csv");
        fbm.createSubTask("TestSubtask", "Descr test.csv", 1);
        fbm.createTask("Test task", "Test task desscr test.csv");
        fbm.createTask("Test task2", "2d Test task desscr test.csv");

        for (int i = 1; i <= 42; i++){
            fbm.getEpicById(1);
            fbm.getSubTaskById(2);
            fbm.getTaskById(3);
        }
*/
        System.out.println("\n");

        fbm.loadFromFile(new File("./src/Data/SavedData.csv"));
        fbm.getPrioritizedTasks().forEach(s -> {
            System.out.println(s.toString());
        });

        fbm.checkTimeline();
        System.out.println("Загружено из файла SavedData.csv: ");

        for (Task issue: fbm.getEpicList()){
            System.out.println(issue.toString() + " Start time: " + issue.getStartTime());
        }
        for (Task issue: fbm.getTaskList()){
            System.out.println(issue.toString() + " Start time: " + issue.getStartTime());
        }
        for (Task issue: fbm.getSubtaskList()){
            System.out.println(issue.toString() + " Start time: " + issue.getStartTime());
        }

        System.out.println("\n");

        fbm.loadFromFile(new File("./src/Data/test.csv"));
        System.out.println("Загружено из файла test.csv: ");
        for (Task issue: fbm.getEpicList()){
            System.out.println(issue.toString());
        }
        for (Task issue: fbm.getTaskList()){
            System.out.println(issue.toString());
        }
        for (Task issue: fbm.getSubtaskList()){
            System.out.println(issue.toString());
        }

    }

    // свои методы
    // сохранение в файл
    public void save() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path.toString()))) {
            bw.write("id,type,name,status,description,startDate,duration,parentEpic\n");

            // сохраняем эпики
            for (Task epic: epicList.values()){
                bw.write(converter.convertToString(epic) + System.lineSeparator());
            }
            // сохраняем сабтаски
            for (Task subTask: subtaskList.values()){
                bw.write(converter.convertToString(subTask) + System.lineSeparator());
            }
            // сохраняем таски
            for (Task task: taskList.values()){
                bw.write(converter.convertToString(task) + System.lineSeparator());
            }
            //сохраняем историю
            bw.write("\n");
            for (Task item: history.getHistory()){
                bw.write(item.getId() + ",");
            }
        } catch (IOException e) {
            throw new ManagerSaveException(e, "Ошибка сохранения файла");
        }
    }

    public void loadFromFile(File file) {

        // обновляем данные файла
        this.file = file;
        this.path = file.toPath();

        // чистим hashMap с данными для записи новых данных из файла
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
            new File(file.getPath()); // если файла нет - создаем
        }
        int lastId = 0; // нужна для того, чтобы после всех загрузок выставить корректный id для новых issue
        for (int i = 1; i < data.length; i++){
            // Сначала грузим все эпики, сабтаски и таски
            if (!data[i].isEmpty()){ // вдруг файл пустой
                String[] line = data[i].split(",");
                StatusList status = StatusList.valueOf(line[3]);
                int index = Integer.valueOf(line[0]); // переменная для установки issueID
                if (index > lastId) lastId = index; // нужно найти максимальное значение index, чтобы в конце установить корректный issueId

                switch (line[1]){
                    case "EPIC":
                        if (line[5].equals("null")) line[5] = Instant.ofEpochMilli(0).toString();
                        if (line[6].equals("null")) line[6] = "0";
                        Instant epicStartDate = Instant.parse(line[5]);
                        int epicDuration = Integer.parseInt(line[6]);
                        Epic epic = new Epic(line[2], line[4], status, index, epicStartDate, epicDuration); // создаем эпик
                        epicList.put(index,epic); // переписан метод добавления в я hashmap
                        break;
                    case "SUBTASK":
                        if (line[5].equals("null")) line[5] = Instant.ofEpochMilli(0).toString();
                        if (line[6].equals("null")) line[6] = "0";
                        int parentEpicId = Integer.valueOf(line[7]);
                        Epic parentEpic = epicList.get(parentEpicId);
                        Instant subTaskStartDate = Instant.parse(line[5]);
                        int subTaskDuration = Integer.parseInt(line[6]);
                        SubTask subTask = new SubTask(line[2], line[4], status, parentEpicId, index, subTaskStartDate, subTaskDuration);
                        parentEpic.addSubTaskToEpic(subTask); // прописали сабтаску в эпике
                        subTask.setParentEpic(parentEpic.getId()); // прописали эпик в сабтаске
                        subtaskList.put(index, subTask); // апдейт сабтаски в hashmap
                        epicList.put(parentEpicId, parentEpic); // апдейт эпика в hashmap
                        break;
                    case "TASK":
                        if (line[5].equals("null")) line[5] = Instant.ofEpochMilli(0).toString();
                        if (line[6].equals("null")) line[6] = "0";
                        Instant taskStartDate = Instant.parse(line[5]);
                        int taskDuration = Integer.parseInt(line[6]);
                        Task task = new Task(line[2], line[4], status, index, taskStartDate, taskDuration); // создали таску
                        taskList.put(index, task);
                        break;
                }
            } else {
                break; // если файл пустой - не грузим из него ничего
            }
        }
        setLastId(lastId); // обновляем индекс задач, чтобы новые добавлялись с последнего имеющегося

        // грузим историю
        if (data == null || data.length == 0){
            System.out.println("Файл с данными для загрузки пустой\n");
            return;
        } else if (data.length > 1){ // проверяем что если файл не пустой, там есть что-то кроме заголовка csv
            String[] historyLine = data[data.length - 1].split(",");
            for (int i = 0; i < historyLine.length; i++) {
                try { // если файл с данными поврежден или нет сохраненных ID для истории в конце файла - надо пропустить обработку метода
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
        } // если в файле нет даже заголовка - не грузим из него ничего

    }

    /**
     * Вспомогательный класс для записи в файл
     * метод convertToString
     * Конвертирует задачи в String, в зависимости от типа
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

    // переопределенные методы
    // Epic
    @Override
    public void setLastId(int newId){ // метод, нужный для работы загрузчика
        super.setLastId(newId);
    }

    @Override
    public Epic createEpic(String name, String description){
        Epic epic = super.createEpic(name, description);
        save();
        return epic;
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

    // SubTask
    @Override
    public SubTask createSubTask(String name, String description, int parentEpic){
        SubTask st = super.createSubTask(name, description, parentEpic);
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
    public Task createTask(String name, String description){
        Task t = super.createTask(name, description);
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
