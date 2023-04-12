import interfaces.TaskManager;
import org.junit.jupiter.api.*;
import taskManager.HttpTaskManager;
import utils.KVServer;


import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
//public class HttpTaskManagerTest extends TaskManagerTest <HttpTaskManager>  {
//class HttpTaskManagerTest<T extends TaskManagerTest<HttpTaskManager>>{

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HttpTaskManagerTest extends TaskManagerTest<HttpTaskManager>{
    private KVServer saveServer;

    //@BeforeEach
    HttpTaskManagerTest() throws IOException, InterruptedException {
        saveServer = new KVServer();
        saveServer.start();
        manager = new HttpTaskManager("http://localhost:8078");
    }

    @BeforeEach
    public void KVStart() throws IOException, InterruptedException {
        //saveServer.start();
        //manager.updateClientToken();
        createIssues();
    }

    @Test
    public void testTest(){
        System.out.println("Something works.. ");
    }

    @AfterEach
    public void KVSReset(){
        saveServer.stop();
    }
}
