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

public class HistoryHandler implements HttpHandler {
    TaskManager manager;
    Gson json = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantAdapter()).create();

    public HistoryHandler(TaskManager manager){
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
                responseData = json.toJson(manager.getHistory());
                responseCode = 200;
                break;

            default:
                responseData = "This request is not supported";
                responseCode = 405;

        }
        exchange.sendResponseHeaders(responseCode,0);
        try(OutputStream os = exchange.getResponseBody()){
            os.write(responseData.getBytes());
        }
        System.out.println(responseCode);
    }
}
