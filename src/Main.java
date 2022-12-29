import Interfaces.TaskManager;
import Issues.StatusList;
import Issues.SubTask;
import Issues.Task;
import Issues.Epic;

import Utils.Managers;

public class Main {

    public static void main(String[] args) {
        /** в задании сказано сделать метод main прямо в новом таскменеджере
         * Но при этом, чтобы все исполнялось, нужно переводитьвсе методы к Static. Кажется, так не было задумано..
         * Поэтому я сделал тут замену менеджера, добавив второй менеджер в класс Managers
         * Проверка осуществлена как и требуется в задании - создано несколько задач разного типа, с обращениями к ним
         * и проверкой загрузки самих тасок, апдейта статуса и истории
         */

        //TaskManager taskManager = Managers.getDefault();
        TaskManager taskManager = Managers.getFileBaked(
                "./src/Data/SavedData.csv");

        System.out.println("Загружена история: ");
        System.out.println(taskManager.getHistory());
        System.out.println("\n");

        taskManager.createEpic("Тестовый эпик", "Описание тестового эпика");
        taskManager.createSubTask("Тестовая сабтаска","Заодно и проверим как у эпика статус меняется", 2);
        taskManager.setSubTaskStatus(taskManager.getSubTaskById(3), StatusList.DONE);

        System.out.println("Что попало из файла в программу: ");
        for (Task issue: taskManager.getEpicList()){
            System.out.println(issue.toString());
        }
        for (Task issue: taskManager.getTaskList()){
            System.out.println(issue.toString());
        }
        for (Task issue: taskManager.getSubtaskList()){
            System.out.println(issue.toString());
        }
    }
}
