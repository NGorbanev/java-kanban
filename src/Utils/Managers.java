package Utils;

import History.InMemoryHistoryManager;
import Interfaces.HistoryManager;
import Interfaces.TaskManager;
import TaskManager.InMemoryTaskManager;
import TaskManager.FileBackedTasksManager;

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
