import Interfaces.TaskManager;
import Issues.SubTask;
import Issues.Task;
import Issues.Epic;

import Utils.Managers;

public class Main {

    public static void main(String[] args) {
        TaskManager inMemoryTaskManager = Managers.getDefault();

        String[] taskNames = new String[] { // массивы для создания тестовых данных
                "Первая задача",
                "Вторая задача",
                "Эпик",
                "подзадача 1",
                "подзадача 2"
        };
        String[] taskDescriptions = new String[]{
                "Описание первой таски",
                "Описание второй таски",
                "Описание эпика",
                "Описание первой подзадачи",
                "Описание второй подзадачи"
        };
        int epicId = 0;
        for (int i = 0; i <= 4; i++){
            if (i >= 0 && i <= 1){
                Task task = inMemoryTaskManager.createTask(taskNames[i], taskDescriptions[i]);
                System.out.println("Создана задача [" +
                        task.getId() + "] " +
                        task.getName() + ", " +
                        task.getClass().toString());
            }
            if (i == 2) {
                Epic task = inMemoryTaskManager.createEpic(taskNames[i], taskDescriptions[i]);
                System.out.println("Создана задача [" + task.getId() + "] " +
                        task.getName() +
                        ", " + task.getClass().toString());
                epicId = task.getId();
            }
            if (i > 2) {
                SubTask task = inMemoryTaskManager.createSubTask(taskNames[i], taskDescriptions[i], 3);
                System.out.println("Создана задача [" +
                        task.getId() + "] " +
                        task.getName() + ", " +
                        task.getClass().toString() + "\n");
            }
        }
        // проверяем что многократный запрос объектов с записью в историю работает корректно, история перезаписывается
        for (int i = 1; i <= 42; i++) {
            System.out.println(inMemoryTaskManager.getEpicById(3).getName().toString());
            System.out.println(" - "+inMemoryTaskManager.getSubTaskById(4).getName().toString());
            System.out.println(" - "+inMemoryTaskManager.getSubTaskById(5).getName().toString());
            System.out.println(inMemoryTaskManager.getTaskById(1).getName().toString());
            System.out.println(inMemoryTaskManager.getTaskById(2).getName().toString() + "\n");

        }
        //Для проверки попробовал удаление всех всеми доступными способами. Можно раскомментить методы с удалением тасок рахных типов, проверяя механизм
        System.out.println(inMemoryTaskManager.getHistory().toString());
        //inMemoryTaskManager.deleteEpicById(3);
        //inMemoryTaskManager.deleteAllEpics();
        //inMemoryTaskManager.deleteTaskById(1);
        inMemoryTaskManager.deleteAllSubTasks();
        inMemoryTaskManager.deleteAllTasks();
        inMemoryTaskManager.deleteAllEpics();
        //inMemoryTaskManager.deleteTaskById(2);

        //печатаем что осталось
        System.out.println(inMemoryTaskManager.getHistory().toString());
    }
}
