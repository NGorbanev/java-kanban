import Interfaces.TaskManager;
import Utils.Managers;

public class InMemoryTaskManagerTest extends TaskManagerTest<TaskManager> {
    InMemoryTaskManagerTest() {
        manager = Managers.getDefault();
    }
}
