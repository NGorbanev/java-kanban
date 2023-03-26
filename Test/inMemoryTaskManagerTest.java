import Interfaces.TaskManager;
import TaskManager.FileBackedTasksManager;
import TaskManager.InMemoryTaskManager;
import Utils.Managers;

public class inMemoryTaskManagerTest extends taskManagerTest<TaskManager> {
    inMemoryTaskManagerTest() {
        manager = Managers.getDefault();
    }
}
