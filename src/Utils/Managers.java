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

      public static TaskManager getFileBaked(String fileAddress){
          return new FileBackedTasksManager(fileAddress);
      }

      public static TaskManager getFileBakedFree(){
          return new FileBackedTasksManager();
      }

      // менеджеры истории
      public static HistoryManager getDefaultHistoryManager(){
          return new InMemoryHistoryManager();
      }

}
