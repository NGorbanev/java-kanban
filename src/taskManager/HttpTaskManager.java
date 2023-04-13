package taskManager;

import api.HttpTaskServer;
import api.adapters.InstantAdapter;
import com.google.gson.*;
import issues.Epic;
import issues.SubTask;
import issues.Task;
import utils.KVTaskClient;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class HttpTaskManager extends FileBackedTasksManager {

    // issue keys
    private final String tasksKey = "tasks";
    private final String epicsKey = "epics";
    private final String subTasksKey = "subtasks";
    private final String historyKey = "history";

    private KVTaskClient client;
    String serverURL;

    private Gson json = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantAdapter()).create();

    public HttpTaskManager(String serverURL) throws IOException, InterruptedException {
        this.serverURL = serverURL;
        client = new KVTaskClient(serverURL);
        loadData();
    }

    public void loadData() throws IOException, InterruptedException {

        // loading issues
        JsonElement tasksJson = JsonParser.parseString(client.load(tasksKey));
        JsonElement epicsJson = JsonParser.parseString(client.load(epicsKey));
        JsonElement subtasksJson = JsonParser.parseString(client.load(subTasksKey));
        JsonElement historyJson = JsonParser.parseString(client.load(historyKey));

        if (!tasksJson.isJsonNull()){
            JsonArray jsonArray = tasksJson.getAsJsonArray();
            for (JsonElement issue : jsonArray){
                Task task = json.fromJson(issue, Task.class);
                taskList.put(task.getId(), task);
            }
        }
        if (!epicsJson.isJsonNull()){
            JsonArray jsonArray = epicsJson.getAsJsonArray();
            for (JsonElement issue : jsonArray){
                Epic epic = json.fromJson(issue, Epic.class);
                epicList.put(epic.getId(), epic);
            }
        }
        if (!subtasksJson.isJsonNull()){
            JsonArray jsonArray = subtasksJson.getAsJsonArray();
            for (JsonElement issue : jsonArray){
                SubTask subTask = json.fromJson(issue, SubTask.class);
                subtaskList.put(subTask.getId(), subTask);
            }
        }
        if (!historyJson.isJsonNull()){
            JsonArray jsonArray = historyJson.getAsJsonArray();
            for (JsonElement issueId : jsonArray){
                int id = issueId.getAsInt();
                if (epicList.containsKey(id)){
                    history.add(getEpicById(id));
                } else if (subtaskList.containsKey(id)) {
                    history.add(getSubTaskById(id));
                } else if (taskList.containsKey(id)) {
                    history.add(getTaskById(id));
                }
            }
        }
    }

    @Override
    public void save(){
        try {
            client.put(tasksKey, json.toJson(taskList.values()));
            client.put(epicsKey, json.toJson(epicList.values()));
            client.put(subTasksKey, json.toJson(subtaskList.values()));
            ArrayList<Integer> historyItemsIDs = new ArrayList<>();
            for (Task issue : history.getHistory()){
                historyItemsIDs.add(issue.getId());
            }
            client.put(historyKey, json.toJson(historyItemsIDs));
        } catch (IOException | InterruptedException e){
            System.out.println(this.getClass().getSimpleName() + ": Save data fail");
        }
    }

}
