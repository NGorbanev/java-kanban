package TaskManager;

import Issues.*;
import Utils.ManagerSaveException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        fbm.createEpic("TestEpic", "Desc test.csv");
        fbm.createSubTask("TestSubtask", "Descr test.csv", 1);
        fbm.createTask("Test task", "Test task desscr test.csv");
        fbm.createTask("Test task2", "2d Test task desscr test.csv");

        for (int i = 1; i <= 42; i++){
            fbm.getEpicById(1);
            fbm.getSubTaskById(2);
            fbm.getTaskById(3);
        }

        System.out.println("\n");

        fbm.loadFromFile(new File("./src/Data/SavedData.csv"));
        System.out.println("Загружено из файла SavedData.csv: ");
        for (Task issue: fbm.getEpicList()){
            System.out.println(issue.toString());
        }
        for (Task issue: fbm.getTaskList()){
            System.out.println(issue.toString());
        }
        for (Task issue: fbm.getSubtaskList()){
            System.out.println(issue.toString());
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
            bw.write("id,type,name,status,description,parentEpic\n");

            // сохраняем эпики
            for (Task epic: epicList.values()){
                bw.write(converter.convertToString(epic) + System.lineSeparator()); // переписано в соответствии с рекомендациями на ревью
            }
            // сохраняем сабтаски
            for (Task subTask: subtaskList.values()){
                bw.write(converter.convertToString(subTask) + System.lineSeparator()); // переписано в соответствии с рекомендациями на ревью
            }
            // сохраняем таски
            for (Task task: taskList.values()){
                bw.write(converter.convertToString(task) + System.lineSeparator()); // переписано в соответствии с рекомендациями на ревью
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

        String[] data = new String[0];// вынесено из try
        try {

            // оставил именно такую реализацию, потому что нужно сначала считать задачи до пустой строки, потом ее пропустить и взять строку с историей
            // так кажется проще реализовать перебор. Исправил разделитель в сохранении
            data = Files.readString(Path.of(file.toURI())).split(System.lineSeparator());
            if (data == null || data.length == 0){ // если файл пуст - информируем в консоли и больше не пытаемся ничего из него получить
                throw new ManagerSaveException("Файл с данными для загрузки пустой");
            }
        } catch (IOException e) {
            new File(file.getPath()); // если файла нет - создаем
        } catch (ManagerSaveException e){
            return; // пропускаем дальнейшие попытки загрузки данных, потому что файл пустой
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
                        Epic epic = new Epic(line[2], line[4], status, index); // создаем эпик
                        epicList.put(index,epic); // переписан метод добавления в я hashmap
                        break;
                    case "SUBTASK":
                        int parentEpicId = Integer.valueOf(line[5]);
                        Epic parentEpic = getEpicById(parentEpicId);
                        SubTask subTask = new SubTask(line[2], line[4], status, parentEpicId, index);
                        parentEpic.addSubTaskToEpic(subTask); // прописали сабтаску в эпике
                        subTask.setParentEpic(parentEpic.getId()); // прописали эпик в сабтаске
                        subtaskList.put(index, subTask); // апдейт сабтаски в hashmap
                        epicList.put(parentEpicId, getEpicById(parentEpicId)); // апдейт эпика в hashmap
                        checkStatus(parentEpic);
                        break;
                    case "TASK":
                        Task task = new Task(line[2], line[4], status, index); // создали таску
                        //updateTask(task); // залили ее в хэшмап
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
                        ((SubTask) issue).getParentEpicId() + "";
                return output;
            } else if (issue instanceof Epic){
               String output = ((Epic) issue).getId() + "," +
                        IssueTypes.EPIC + "," +
                        ((Epic) issue).getName() + "," +
                        ((Epic) issue).getStatus() + "," +
                        ((Epic) issue).getDescription() + ", ";
                return output;
            } else if (issue instanceof Task){
                String output = ((Task) issue).getId() + "," +
                        IssueTypes.TASK + "," +
                        ((Task) issue).getName() + "," +
                        ((Task) issue).getStatus() + "," +
                        ((Task) issue).getDescription() + ", ";
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
