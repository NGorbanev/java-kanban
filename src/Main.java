import Issues.SubTask;
import Issues.Task;
import Issues.Epic;
import TaskManager.TaskManager;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        // проверяем работу с тасками
        String[] taskNames = new String[] {"Проверить работу приложения", "Проверить работу методов создания " +
                "тасок", "Проверить работу методов модификации тасок"};
        String[] taskDescriptions = new String[]{"Убедиться что оно вообще запускается", "Убедиться что таски " +
                "создаются как надо", "Проверить модификатора изменения статусов, удаления тасок"};
        for (int i = 0; i <= 2; i++){
            Task task = taskManager.createTask(taskNames[i], taskDescriptions[i]);
            if (i == 0) task.setStatus(2);
            if (i == 1) task.setStatus(1);
            if (i == 2) task.setStatus(1);
            System.out.println("Создана таска ID: " + task.getId() + ", Статус " + task.getStatus() + "\n"
                    + task.getName() + "\n" + task.getDescription() + "\n");
            if (i == 2) {
                taskManager.deleteTaskById(task.getId());
                System.out.println("Последняя задача удалена, значит все работает корректно" +
                        "\nСписок тасок на сейчас: " + taskManager.getTaskList().toString());
            }
        }
        if (!taskManager.getTaskList().containsKey(3)) {
            Task task = taskManager.createTask("Проверить работу алгоритма формирования идентификатора",
                    "У этой задачи должен быть ID = 4");
            task.setStatus(2);
            System.out.println("\nСоздана таска ID: " + task.getId() + ", Статус " + task.getStatus() + "\n"
                    + task.getName() + "\n" + task.getDescription() + "\n");
        }

        // проверяем работу с эпиками и сабтасками
        Epic epic1 = taskManager.createEpic("Проврить создание эпика и подзадач", "привязать пару " +
                "подзадач к этому эпику");
        Epic epic2 = taskManager.createEpic("Проверить работу алгоритма расчета статуса эпика",
                "привязать три подзадачи в разных статусах и проверить как посчитается статус эпика");
       // System.out.println("Создан эпик ID: " + epic1.getId() + ", Статус " + epic1.getStatus() + "\n"
       //         + epic1.getName() + "\n" + epic1.getDescription() + "\n");

        SubTask subTask = taskManager.createSubTask("Подзадача 1", "первого эпика", 5);
        subTask = taskManager.createSubTask("Подзадача 2", "первого эпика", 5);
        subTask = taskManager.createSubTask("Подзадача 1 (статус Инпрог)", "второго эпика", 6);
        taskManager.setSubTaskStatus(subTask, "IN_PROGRESS");
        subTask = taskManager.createSubTask("Подзадача 3 (статус Нью)", "второго эпика", 6);
        taskManager.setSubTaskStatus(subTask, "Типа статус ");
        subTask = taskManager.createSubTask("Подзадача 2 (статус ДАН)", "второго эпика", 6);
        taskManager.setSubTaskStatus(subTask, "DONE");

        for (int i = 0; i <= taskManager.getLastId(); i ++){
            if (taskManager.getEpicById(i) != null) {
                Epic epic = taskManager.getEpicById(i);
                System.out.println("\nСоздан эпик ID: " + epic.getId() + ", Статус " + epic.getStatus() + "\n"
                        + epic.getName() + "\n" + epic.getDescription());
                if (epic.getSubTasks() != null){
                    System.out.println("Список подзадач эпика: ");
                    for (Integer item: epic.getSubTasks()){
                        SubTask sTask = taskManager.getSubTaskById(item);
                        System.out.println(sTask.getId() + " " + sTask.getStatus() + ": " + sTask.getName());
                    }
                }
            }
        }

    }
}
