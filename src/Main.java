import api.HttpTaskServer;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, InterruptedException {
        HttpTaskServer ts = new HttpTaskServer();
        ts.startServer();
        //ts.stopServer();

    }
}
