import interfaces.TaskManager;
import utils.Managers;

public class InMemoryTaskManagerTest extends TaskManagerTest<TaskManager> {
    InMemoryTaskManagerTest() {
        manager = Managers.getDefault();
    }
}
