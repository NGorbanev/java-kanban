import interfaces.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import utils.Managers;

public class InMemoryTaskManagerTest extends TaskManagerTest<TaskManager> {
    InMemoryTaskManagerTest() {
        manager = Managers.getInMemoryTaskManager();
    }

    @BeforeEach
    public void start(){
        createIssues();
    }
}
