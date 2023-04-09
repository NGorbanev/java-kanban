import api.HttpTaskServer;
import utils.KVServer;
import utils.KVTaskClient;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        KVServer kvs = new KVServer();
        kvs.start();
        HttpTaskServer ts = new HttpTaskServer();
        ts.startServer();

        // KVTaskClient client = new KVTaskClient("http://localhost:8078");
        //client.put("SomeKey", "Some shit");
        //client.load("SomeKey");

        //ts.stopServer();

    }
}
