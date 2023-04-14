package managersTests;

import org.junit.jupiter.api.*;
import taskManager.HttpTaskManager;
import utils.KVServer;
import utils.Managers;

import java.io.IOException;

public class HttpTaskManagerTest extends TaskManagerTest<HttpTaskManager>{
    private KVServer saveServer;

    HttpTaskManagerTest() throws IOException, InterruptedException {
        saveServer = new KVServer();
        saveServer.start();
        manager = (HttpTaskManager) Managers.getDefault();
    }

    @BeforeEach
    public void createAll() {
        createIssues();
    }

    @Test
    public void SaveNLoadTest() throws IOException, InterruptedException {
        manager.loadData();
        Assertions.assertEquals(1, manager.getTaskList().size());
        Assertions.assertEquals(1, manager.getSubtaskList().size());
        Assertions.assertEquals(1, manager.getEpicList().size());
        Assertions.assertEquals(testTask.toString(), manager.getTaskList().get(0).toString());
        Assertions.assertEquals(testEpic.toString(), manager.getEpicList().get(0).toString());
        Assertions.assertEquals(testSubTask.toString(), manager.getSubtaskList().get(0).toString());
    }

    @AfterEach
    public void KVSReset(){
        saveServer.stop();
    }
}
