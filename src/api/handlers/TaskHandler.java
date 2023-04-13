package api.handlers;

import api.adapters.InstantAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import interfaces.TaskManager;
import api.utils.QueryParser;
import issues.Task;
import utils.TimeLineCrossingsException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class TaskHandler implements HttpHandler {
    TaskManager manager;
    Gson json = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantAdapter()).create();
    QueryParser queryParser = new QueryParser();


    public TaskHandler(TaskManager manager){
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        int responseCode = 400;
        String method = exchange.getRequestMethod();
        String path = String.valueOf(exchange.getRequestURI());
        String responseData = null;
        // info message to command line
        System.out.print(this.getClass().getSimpleName() + ": request to " + path + ", type = " + method + "... ");

        switch (method){
            case "GET":
                String queryGET = exchange.getRequestURI().getQuery();
                if (queryGET == null){
                    responseData = json.toJson(manager.getTaskList());
                    responseCode = 200;
                } else {
                    try {
                        Task task = manager.getTaskById(queryParser.getIdFromQuery(queryGET));
                        if (task != null){
                            responseData = json.toJson(manager.getTaskById(queryParser.getIdFromQuery(queryGET)));
                            responseCode = 200;
                        } else {
                            responseData = "Task with ID=" + queryParser.getIdFromQuery(queryGET) + " not found";
                            responseCode = 404;
                        }
                    } catch (StringIndexOutOfBoundsException e) {
                        responseCode = 400;
                        responseData = "Needed parameter \"id\" not found at request";
                    } catch (NumberFormatException e) {
                        responseCode = 400;
                        responseData = "Wrong id format";
                    }
                }
                break;
            case "POST":
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                try {
                    Task incomingTask = json.fromJson(requestBody, Task.class);
                    if (manager.getTaskById(incomingTask.getId()) != null){
                        manager.updateTask(incomingTask);
                        responseCode = 202;
                        responseData = json.toJson(manager.getTaskById(incomingTask.getId()));
                    } else {
                        Task newTask = manager.createTask(incomingTask);
                        responseCode = 201;
                        responseData = json.toJson(newTask);
                    }
                } catch (JsonSyntaxException e){
                    responseData = "Wrong request body format";
                    responseCode = 400;
                } catch (TimeLineCrossingsException e){
                    responseData = e.getMessage();
                    responseCode = 409;

                }
                break;
            case "DELETE":
                String queryDELETE = exchange.getRequestURI().getQuery();
                if (queryDELETE == null){
                    manager.deleteAllTasks();
                    responseData = "All tasks were deleted";
                    responseCode = 200;
                } else {
                    try {
                        Task task = manager.getTaskById(queryParser.getIdFromQuery(queryDELETE));
                        if (task != null) {
                            manager.deleteTaskById(task.getId());
                            responseData = "Task ID=" + task.getId() + " was deleted";
                            responseCode = 200;
                        } else {
                            responseData = "Task with ID=" + queryParser.getIdFromQuery(queryDELETE) + " not found";
                            responseCode = 404;
                        }
                    } catch (StringIndexOutOfBoundsException e) {
                        responseCode = 400;
                        responseData = "Needed parameter \"id\" not found at request";
                    } catch (NumberFormatException e) {
                        responseCode = 400;
                        responseData = "Wrong id format";
                    }
                } break;
            default:
                responseData = "This request is not supported";
                responseCode = 405;
                break;

        }
        exchange.sendResponseHeaders(responseCode,0);
        try(OutputStream os = exchange.getResponseBody()){
            os.write(responseData.getBytes());
        }
        System.out.println(responseCode);
    }
}
