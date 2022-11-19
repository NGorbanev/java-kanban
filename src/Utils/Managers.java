package Utils;

import Interfaces.TaskManager;
import TaskManager.InMemoryTaskManager;

import java.util.ArrayList;

public class Managers {
    //private static ArrayList<TaskManager> managers = new ArrayList<>();
      static TaskManager defaultTaskManager = new InMemoryTaskManager();

      public TaskManager getDefault(){
          return defaultTaskManager;
      }

}
