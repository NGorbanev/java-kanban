import Interfaces.TaskManager;
import Issues.Task;
import Utils.Managers;

public class Main {

    public static void main(String[] args) {

        //TaskManager taskManager = Managers.getDefault();
        TaskManager taskManager = Managers.getFileBaked(
                "./src/Data/SavedData.csv");

        System.out.println("Загружена история: ");
        //System.out.println(taskManager.getHistory());
        for (Task item: taskManager.getHistory()){
            System.out.println("- " + item.toString());
        }
        System.out.println("\n");

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
