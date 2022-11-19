import Interfaces.TaskManager;
import Issues.StatusList;
import Issues.SubTask;
import Issues.Task;
import Issues.Epic;
import TaskManager.InMemoryTaskManager;
import History.InMemoryHistoryManager;
import Utils.Managers;

public class Main {

    public static void main(String[] args) {
        TaskManager inMemoryTaskManager = new Managers().getDefault();

        // проверяем работу с тасками
        String[] taskNames = new String[] {"Проверить работу приложения", "Проверить работу методов создания " +
                "тасок", "Проверить работу методов модификации тасок"};
        String[] taskDescriptions = new String[]{"Убедиться что оно вообще запускается", "Убедиться что таски " +
                "создаются как надо", "Проверить модификатора изменения статусов, удаления тасок"};
        for (int i = 0; i <= 2; i++){
            Task task = inMemoryTaskManager.createTask(taskNames[i], taskDescriptions[i]);
            if (i == 0) inMemoryTaskManager.setTaskStatus(task, StatusList.NEW);
            if (i == 1) inMemoryTaskManager.setTaskStatus(task, StatusList.IN_PROGRESS);
            if (i == 2) inMemoryTaskManager.setTaskStatus(task, StatusList.DONE);
            System.out.println("Создана таска ID: " + task.getId() + ", Статус " + task.getStatus() + "\n"
                    + task.getName() + "\n" + task.getDescription() + "\n");
            if (i == 2) {
                inMemoryTaskManager.deleteTaskById(task.getId());
                System.out.println("Последняя задача удалена, значит все работает корректно" +
                        "\nСписок тасок на сейчас: " + inMemoryTaskManager.getTaskList().toString());
            }
        }
        if (!inMemoryTaskManager.getTaskList().containsKey(3)) {
            Task task = inMemoryTaskManager.createTask("Проверить работу алгоритма формирования идентификатора",
                    "У этой задачи должен быть ID = 4");
            task.setStatus(StatusList.DONE);
            System.out.println("\nСоздана таска ID: " + task.getId() + ", Статус " + task.getStatus() + "\n"
                    + task.getName() + "\n" + task.getDescription() + "\n");
        }

        // проверяем работу с эпиками и сабтасками
        Epic epic1 = inMemoryTaskManager.createEpic("Проврить создание эпика и подзадач", "привязать пару " +
                "подзадач к этому эпику");
        Epic epic2 = inMemoryTaskManager.createEpic("Проверить работу алгоритма расчета статуса эпика",
                "привязать три подзадачи в разных статусах и проверить как посчитается статус эпика");

        SubTask subTask = inMemoryTaskManager.createSubTask("Подзадача 1", "первого эпика", 5);
        subTask = inMemoryTaskManager.createSubTask("Подзадача 2", "первого эпика", 5);
        subTask = inMemoryTaskManager.createSubTask("Подзадача 1 ", "второго эпика", 6);
        inMemoryTaskManager.setSubTaskStatus(subTask, StatusList.IN_PROGRESS);
        subTask = inMemoryTaskManager.createSubTask("Подзадача 3 ", "второго эпика", 6);
        inMemoryTaskManager.setSubTaskStatus(subTask, StatusList.DONE);
        subTask = inMemoryTaskManager.createSubTask("Подзадача 2 ", "второго эпика", 6);
        inMemoryTaskManager.setSubTaskStatus(subTask, StatusList.DONE);

        for (int i = 0; i <= inMemoryTaskManager.getLastId(); i ++){
            if (inMemoryTaskManager.getEpicById(i) != null) {
                Epic epic = inMemoryTaskManager.getEpicById(i);
                System.out.println("\nСоздан эпик ID: " + epic.getId() + ", Статус " + epic.getStatus() + "\n"
                        + epic.getName() + "\n" + epic.getDescription());
                if (epic.getSubTasks() != null){
                    System.out.println("Список подзадач эпика: ");
                    for (Integer item: epic.getSubTasks()){
                        SubTask sTask = inMemoryTaskManager.getSubTaskById(item);
                        System.out.println(sTask.getId() + " " + sTask.getStatus() + ": " + sTask.getName());
                    }
                }
            }
        }
        inMemoryTaskManager.deleteSubTaskById(11);
        System.out.println("Subtasks of 6th epic:  " + inMemoryTaskManager.getAllSubtasksByEpicId(6).toString());
        for (int i = 0; i <= inMemoryTaskManager.getLastId(); i ++){
            if (inMemoryTaskManager.getEpicById(i) != null) {
                Epic epic = inMemoryTaskManager.getEpicById(i);
                System.out.println("\nЭпик ID: " + epic.getId() + ", Статус " + epic.getStatus() + "\n"
                        + epic.getName() + "\n" + epic.getDescription());
                if (epic.getSubTasks() != null){
                    System.out.println("Список подзадач эпика: ");
                    for (Integer item: epic.getSubTasks()){
                        SubTask sTask = inMemoryTaskManager.getSubTaskById(item);
                        System.out.println(sTask.getId() + " " + sTask.getStatus() + ": " + sTask.getName());
                    }
                }
            }
        }
        System.out.println(inMemoryTaskManager.getEpicList().toString());
        InMemoryHistoryManager h = new InMemoryHistoryManager();
        System.out.println("История просмотров: \n" + h.getHistory().toString());
    }
}
