package utils;

import history.InMemoryHistoryManager;
import interfaces.HistoryManager;
import interfaces.TaskManager;
import taskManager.InMemoryTaskManager;

public class Managers {

      // Таскменеджеры
      public static TaskManager getDefault(){
          return new InMemoryTaskManager();
      }

      // менеджеры истории
      public static HistoryManager getDefaultHistoryManager(){
          return new InMemoryHistoryManager();
      }

}
