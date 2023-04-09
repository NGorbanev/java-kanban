package utils;

import history.InMemoryHistoryManager;
import interfaces.HistoryManager;
import interfaces.TaskManager;
import taskManager.FileBackedTasksManager;
import taskManager.InMemoryTaskManager;

public class Managers {

      // Таскменеджеры
      public static TaskManager getDefault(){
          return new InMemoryTaskManager();
      }

      public static TaskManager getFileBacked(String pathString){
          return new FileBackedTasksManager(pathString);
      }

      // менеджеры истории
      public static HistoryManager getDefaultHistoryManager(){
          return new InMemoryHistoryManager();
      }

}
