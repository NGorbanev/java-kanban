package utils;

import history.InMemoryHistoryManager;
import interfaces.HistoryManager;
import interfaces.TaskManager;
import taskManager.FileBackedTasksManager;
import taskManager.HttpTaskManager;
import taskManager.InMemoryTaskManager;

import java.io.IOException;

public class Managers {

      // Таскменеджеры
      public static TaskManager getDefault() throws IOException, InterruptedException {
          return new HttpTaskManager("http://localhost:8078");
      }
      public static TaskManager getFileBacked(String pathString){
          return new FileBackedTasksManager(pathString);
      }
      public static TaskManager getInMemoryTaskManager(){
          return new InMemoryTaskManager();
      }

      // менеджеры истории
      public static HistoryManager getDefaultHistoryManager(){
          return new InMemoryHistoryManager();
      }

}
