package api.handlers;

import api.adapters.InstantAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import interfaces.TaskManager;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;

public class GroupHandler implements HttpHandler {
    TaskManager manager;
    Gson gson = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantAdapter()).create();

    public GroupHandler(TaskManager manager){
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        int responseCode = 400;
        String method = exchange.getRequestMethod();
        String path = String.valueOf(exchange.getRequestURI());
        String responseData;
        System.out.print(this.getClass().getSimpleName() + ": request to " + path + ", type = " + method + "... ");
        if (!path.equals("/tasks/")) { // in case of trash in request
            responseCode = 404;
            method = "null";
        }
        switch (method){
            case "GET":
                responseCode = 200;
                responseData = gson.toJson(manager.getPrioritizedTasks());
                break;
/*
            case "DELETE":
                responseCode = 200;
                manager.deleteAllSubTasks();;
                manager.deleteAllEpics();
                manager.deleteAllTasks();
                responseData = "All issues were deleted";
                break;
*/
            default:
                responseData = "Wrong request method";
        }
        exchange.sendResponseHeaders(responseCode,0);
        try(OutputStream os = exchange.getResponseBody()){
            os.write(responseData.getBytes());
        }
        System.out.println(responseCode);
    }
}
