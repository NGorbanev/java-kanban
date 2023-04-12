import api.HttpTaskServer;
import interfaces.TaskManager;
import utils.KVServer;
import utils.KVTaskClient;
import utils.Managers;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        // Start without API

        KVServer kvs = new KVServer();
        kvs.start();
        TaskManager manager = Managers.getDefault();

        // API launch
        HttpTaskServer ts = new HttpTaskServer(manager);
        ts.startServer();

    }
}
