package api.handlers;

import api.adapters.InstantAdapter;
import api.utils.QueryParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import interfaces.TaskManager;
import issues.SubTask;
import utils.TimeLineCrossingsException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class SubTaskHandler implements HttpHandler {
    TaskManager manager;
    Gson json = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantAdapter()).create();
    QueryParser queryParser = new QueryParser();

    public SubTaskHandler(TaskManager manager){
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
                    responseData = json.toJson(manager.getSubtaskList());
                    responseCode = 200;
                } else {
                    try {
                        SubTask subTask = manager.getSubTaskById(queryParser.getIdFromQuery(queryGET));
                        if (subTask != null){
                            responseData = json.toJson(manager.getSubTaskById(queryParser.getIdFromQuery(queryGET)));
                            responseCode = 200;
                        } else {
                            responseData = "SubTask with ID=" + queryParser.getIdFromQuery(queryGET) + " not found";
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
                    SubTask incomingSubTask = json.fromJson(requestBody, SubTask.class);
                    if (manager.getEpicById(incomingSubTask.getParentEpicId()) == null)
                        throw new NullPointerException("Epic for subtask was not found"); // just in case
                    if (manager.getSubTaskById(incomingSubTask.getId()) != null){
                        manager.updateSubTask(incomingSubTask);
                        responseCode = 202;
                        responseData = json.toJson(manager.getSubTaskById(incomingSubTask.getId()));
                    } else {
                        SubTask newSubTask = manager.createSubTask(incomingSubTask);
                        responseCode = 201;
                        responseData = json.toJson(newSubTask);
                    }
                } catch (JsonSyntaxException e){
                    responseData = "Wrong request body format";
                    responseCode = 400;
                } catch (NullPointerException e){
                    responseData = e.getMessage();
                    responseCode = 409;
                } catch (TimeLineCrossingsException e){
                    responseData = e.getMessage();
                    responseCode = 409;
                }
                break;
            case "DELETE":
                String queryDELETE = exchange.getRequestURI().getQuery();
                if (queryDELETE == null){
                    manager.deleteAllSubTasks();
                    responseData = "All subtasks were deleted";
                    responseCode = 200;
                } else {
                    try {
                        SubTask subTask = manager.getSubTaskById(queryParser.getIdFromQuery(queryDELETE));
                        if (subTask != null) {
                            manager.deleteSubTaskById(subTask.getId());
                            responseData = "Subtask ID=" + subTask.getId() + " was deleted";
                            responseCode = 200;
                        } else {
                            responseData = "Subtask with ID=" + queryParser.getIdFromQuery(queryDELETE) + " not found";
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
        }
        exchange.sendResponseHeaders(responseCode,0);
        try(OutputStream os = exchange.getResponseBody()){
            os.write(responseData.getBytes());
        }
        System.out.println(responseCode);


    }
}
