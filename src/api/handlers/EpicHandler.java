package api.handlers;

import api.adapters.InstantAdapter;
import api.utils.QueryParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import interfaces.TaskManager;
import issues.Epic;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

public class EpicHandler implements HttpHandler {
    TaskManager manager;
    Gson json = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantAdapter()).create();
    QueryParser queryParser = new QueryParser();

    public EpicHandler(TaskManager manager){
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
                    responseData = json.toJson(manager.getEpicList());
                    responseCode = 200;
                } else {
                    try {
                        Epic epic = manager.getEpicById(queryParser.getIdFromQuery(queryGET));
                        if (epic != null){
                            responseData = json.toJson(manager.getEpicById(queryParser.getIdFromQuery(queryGET)));
                            responseCode = 200;
                        } else {
                            responseData = "Epic with ID=" + queryParser.getIdFromQuery(queryGET) + " not found";
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
                    Epic incomingEpic = json.fromJson(requestBody, Epic.class);
                    if (manager.getEpicById(incomingEpic.getId()) != null){
                        manager.updateEpic(incomingEpic);
                        responseCode = 202;
                        responseData = json.toJson(manager.getEpicById(incomingEpic.getId()));
                    } else {
                        Epic newEpic = manager.createEpic(incomingEpic);
                        responseCode = 201;
                        responseData = json.toJson(newEpic);
                    }
                } catch (JsonSyntaxException e){
                    responseData = "Wrong request body format";
                    responseCode = 400;
                }
                break;
            case "DELETE":
                String queryDELETE = exchange.getRequestURI().getQuery();
                if (queryDELETE == null){
                    manager.deleteAllEpics();
                    responseData = "All epics are deleted";
                    responseCode = 200;
                } else {
                    try {
                        Epic epic = manager.getEpicById(queryParser.getIdFromQuery(queryDELETE));
                        if (epic != null) {
                            manager.deleteEpicById(epic.getId());
                            responseData = "Epic ID=" + epic.getId() + " was deleted";
                            responseCode = 200;
                        } else {
                            responseData = "Epic with ID=" + queryParser.getIdFromQuery(queryDELETE) + " not found";
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
