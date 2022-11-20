package Utils;

import History.InMemoryHistoryManager;
import Interfaces.HistoryManager;
import Interfaces.TaskManager;
import TaskManager.InMemoryTaskManager;

public class Managers {

      public static TaskManager getDefault(){
          return new InMemoryTaskManager();
      }
      public static HistoryManager getDefaultHistoryManager(){
          return new InMemoryHistoryManager();
      }

}
