package api;

import com.sun.net.httpserver.HttpServer;
import interfaces.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import api.handlers.*;

public class HttpTaskServer {

    private final int PORT = 8080;
    HttpServer server;

    //TaskManager manager = Managers.getFileBacked("./src/data/SavedData.csv");
    //TaskManager manager = Managers.getDefault();
    TaskManager manager;

    public HttpTaskServer(TaskManager manager) throws IOException, InterruptedException {
        this.manager = manager;
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks/", new GroupHandler(manager));
        server.createContext("/tasks/task", new TaskHandler(manager));
        server.createContext("/tasks/subtask", new SubTaskHandler(manager));
        server.createContext("/tasks/epic", new EpicHandler(manager));
        server.createContext("/tasks/history", new HistoryHandler(manager));
    }

    public void startServer() throws IOException {
        System.out.println(this.getClass().getSimpleName() + " started at port " + PORT);
        server.start();
    }

    public void stopServer() throws InterruptedException {
        System.out.println(this.getClass().getSimpleName() + " will be stopped now");
        server.stop(0);
    }
}
