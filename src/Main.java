public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();
        // далее - разного рода проверки. Оставил все на случай исправлений
        System.out.println("Проверка основного класса Task");
        System.out.println("Создание задач.. ");
        Task task1 = new Task("Проверить класса Task", "Создать первую задачу");
        Task task2 = new Task("Проверить счетчика", "Создать вторую задачу");
        System.out.println("Созданы задачи: " + task1.getId() + ", " + task2.getId());
        System.out.println("\nМеняем статусы задач: ");
        if (!task1.setStatus("AnyStatus")) System.out.println("Task" + task1.getId() + " error: статус не изменен");
        task2.setStatus("DONE");
        System.out.println("➡️ " + task1.name + ", ID: " + task1.getId() + ", Статус: " + task1.getStatus());
        System.out.println("➡️ " + task2.name + ", ID: " + task2.getId() + ", Статус: " + task2.getStatus());

        System.out.println("\nПроверка класса Epic");
        String[] epicsNames = new String[]{"Построить дом", "Сварить картошку", "Лечь спать"};
        String[] epicDescriptions = new String[]{"Надо ж где-то жить", "Надо ж что-то есть", "Надо ж отдохнуть"};
        for (int i = 0; i < 3; i++) {
            Epic epic = new Epic(epicsNames[i], epicDescriptions[i]);
            System.out.println("Создан " + epic.getClass() + ", ID: " + epic.getId());
            System.out.println("Название: " + epic.name);
            System.out.println("Название: " + epic.description);
        }
        System.out.println("\nПроверка SubTask и алгоритма вычисления статуса эпика");
        SubTask subTask = new SubTask("Залить фундамент", "Лучше не сиропом", 3);
        subTask.changeStatus("DONE");
        subTask = new SubTask("Построить стены", "Из дерева", 3);
        subTask.changeStatus("NEW");
        subTask = new SubTask("Почистить картошку", "ножом надо", 4);
        subTask.changeStatus("DONE");
        subTask = new SubTask("Помыть картошку", "чистой водой", 4);
        subTask = new SubTask("Вскипятить воду", "в катрюле", 4);
        subTask.changeStatus("IN_PROGRESS");
        Epic testEpic = taskManager.getEpicById(3); // проверка методо получения epic по идентификатору
        System.out.println(testEpic.getStatus() + ": " + testEpic.name);
        System.out.println("(" + testEpic.description + "):");
        for (int i = 0; i < testEpic.showSubTasks().size(); i++){
            System.out.println(" " + taskManager.getSubtaskList().get(testEpic.showSubTasks().get(i)).getStatus() +
                    ": " + taskManager.getSubtaskList().get(testEpic.showSubTasks().get(i)).name +
                    " (" + taskManager.getSubtaskList().get(testEpic.showSubTasks().get(i)).description + ")");
        }
        testEpic = taskManager.getEpicById(4);
        System.out.println(testEpic.getStatus() + ": " + testEpic.name);
        System.out.println("(" + testEpic.description + "):");
        for (int i = 0; i < testEpic.showSubTasks().size(); i++){
            System.out.println(" " + taskManager.getSubtaskList().get(testEpic.showSubTasks().get(i)).getStatus() +
                    ": " + taskManager.getSubtaskList().get(testEpic.showSubTasks().get(i)).name +
                    " (" + taskManager.getSubtaskList().get(testEpic.showSubTasks().get(i)).description + ")");
        }
        System.out.println("\nПроверка методов менеджера");
        System.out.println(taskManager.getEpicList().toString()); // получение списка всех эпиков
        System.out.println(taskManager.getSubtaskList().toString()); // получаем список подзадач

        System.out.println("\nУдаляем эпик и проверяем что удалились и его подзадачи");
        taskManager.deleteEpicById(3); // удаляем эпик
        System.out.println(taskManager.getEpicList().toString()); // получение списка всех задач
        System.out.println(taskManager.getSubtaskList().toString()); // получаем список подзадач

        System.out.println("\nПолучаем список сабтасков эпика '" + taskManager.getEpicById(4).name + "':");
        //System.out.println(taskManager.getAllSubtasksByEpicId(4));
        for(int i = 0; i < taskManager.getAllSubtasksByEpicId(4).size(); i++){
            SubTask s = taskManager.getAllSubtasksByEpicId(4).get(i);
            System.out.println("ID "+ s.getId() + ": " + s.name);
        }

        System.out.println("\nПроверка получения всех сабтасок");
        System.out.println(taskManager.getSubtaskList().toString()); // получение списка всех задач
        System.out.println("\nУдаляем одну сабтаску и проверяем что ее больше нет и в эпике нет связи с ней");
        testEpic = taskManager.getEpicById(taskManager.getSubtaskList().get(9).getParentEpicId());
        taskManager.deleteSubTaskById(8);
        System.out.println("Список сабтасок после удаления: " + taskManager.getSubtaskList().toString() +
                "\nСписок сабтасок эпика " + testEpic.name +
                "\n" + taskManager.getAllSubtasksByEpicId(testEpic.getId()));

        System.out.println("\nМетода получения сабтаски по ID");
        SubTask s = taskManager.getSubTaskById(10);
        System.out.println("Название " + s.name + " ID: " + s.getId() + "\nОписание: " + s.description +
                "\nРодительский эпик: " + taskManager.getEpicById(s.getParentEpicId()).name);

        System.out.println("\nПроверка метода удаления всех сабтасок\nУдаление.. ");
        taskManager.deleteAllSubTasks();
        System.out.println("Удалено. Вывод списка subTaskList:");
        System.out.println(taskManager.getSubtaskList().toString()); // получаем список подзадач

        System.out.println("\nУдаляем все эпики и их поздадачи соответственно");
        taskManager.deleteAllEpics();
        System.out.println(taskManager.getEpicList().toString()); // получение списка всех задач
        System.out.println(taskManager.getSubtaskList().toString()); // получаем список подзадач

        System.out.println("\nПолучение списка всех тасок");
        System.out.println(taskManager.getTaskList());

        System.out.println("\nUpdate одной таски и удаление второй");
        Task task3 = new Task("Проверить класса Task", "Проверить удаление таски");
        taskManager.deleteTaskById(task3.getId());
    }
}
