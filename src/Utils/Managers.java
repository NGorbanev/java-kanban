package Utils;

import Interfaces.TaskManager;
import TaskManager.InMemoryTaskManager;

public class Managers {
      static TaskManager defaultTaskManager = new InMemoryTaskManager();

      public TaskManager getDefault(){
          return defaultTaskManager;
      }

}
