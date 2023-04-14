package httpServerTest;

import api.HttpTaskServer;
import api.adapters.InstantAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import interfaces.TaskManager;
import issues.Epic;
import issues.StatusList;
import issues.SubTask;
import issues.Task;
import org.junit.jupiter.api.*;
import utils.KVServer;
import utils.Managers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class HttpServerTest {

    // 1. Need to start servers
    private static KVServer saveServer;
    private static TaskManager manager;
    private static HttpTaskServer apiServer;

    // 2. Need to define httpClient
    private static HttpClient client;

    // 3. Define issues
    static Task testTask;
    static Epic testEpic;
    static SubTask testSubTask;

    // 4. Define data for cycles
    List<String> methods = Arrays.asList("GET", "DELETE", "POST");

    // 5. add GSON instance
    Gson json = new GsonBuilder().registerTypeAdapter(Instant.class, new InstantAdapter()).create();


    @BeforeAll
    public void serversStart() throws IOException, InterruptedException {
        saveServer = new KVServer();
        saveServer.start();

        manager = Managers.getDefault();
        apiServer = new HttpTaskServer(manager);
        apiServer.startServer();

        client = HttpClient.newHttpClient();
    }

    public Task createNonCrossingTask(){
        Instant newStartTime = Instant.ofEpochMilli(0);
        int duration = 300;
        for (Task issue : manager.getPrioritizedTasks()){
            if (newStartTime.isBefore(issue.getEndTime()) && !newStartTime.equals(issue.getEndTime())){
                newStartTime = issue.getEndTime();
            }
        }
        Task task = new Task(
                "Task",
                "Task for unit tests",
                StatusList.NEW,
                0,
                newStartTime.plusMillis(1),
                duration
        );
        task = manager.createTask(task);
        task.setName("Task " + task.getId());
        manager.updateTask(task);
        return task;
    }
    public SubTask createNonCrossingSubTask(){
        Instant newStartTime = Instant.ofEpochMilli(0);
        int duration = 300;
        for (Task issue : manager.getPrioritizedTasks()){
            if (newStartTime.isBefore(issue.getEndTime()) && !newStartTime.equals(issue.getEndTime())){
                newStartTime = issue.getEndTime();
            }
        }
        if (manager.getEpicList().size() == 0) return null;
        else {
            int lastEpicId = 0;
            for (Epic epic : manager.getEpicList()) {
                lastEpicId = epic.getId();
            }
            SubTask subTask = new SubTask(
                    "SubTask",
                    "SubTask for unit tests",
                    StatusList.NEW,
                    lastEpicId,
                    0,
                    newStartTime.plusMillis(1),
                    duration
            );
            subTask = manager.createSubTask(subTask);
            subTask.setName("SubTask " + subTask.getId());
            manager.updateSubTask(subTask);
            return subTask;
        }
    }
    public Epic createEpic(){
        Epic epic = new Epic("Epic", "TestEpic");
        epic = manager.createEpic(epic);
        epic.setName("Epic " + epic.getId());
        manager.updateEpic(epic);
        return epic;
    }

    @BeforeEach
    public void start() {
        testTask = createNonCrossingTask();
        testEpic = createEpic();
        testSubTask = createNonCrossingSubTask();
    }

    @AfterEach
    public void stop(){
        manager.deleteAllEpics(); // this will delete subtasks also
        manager.deleteAllTasks();
        System.out.println(this.getClass().getSimpleName() + ": data was reset \n");
        Assertions.assertEquals(0, manager.getTaskList().size(), "Failed to reset tasks list");
        Assertions.assertEquals(0, manager.getSubtaskList().size(), "Failed to reset subtasks list");
        Assertions.assertEquals(0, manager.getEpicList().size(), "Failed to reset epics list");
    }

    @AfterAll
    public void finish() throws InterruptedException {
        apiServer.stopServer();
        saveServer.stop();
    }

    @Test
    public void groupEndpointTest(){
        URI url = URI.create("http://localhost:8080/tasks/");
        for (String method : methods) {
            HttpRequest request = null;
            switch (method) {
                case "GET":
                    request = HttpRequest.newBuilder()
                            .GET()
                            .uri(url)
                            .build();
                    try {
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        Assertions.assertEquals(200, response.statusCode());
                        Assertions.assertEquals("[" +
                                            json.toJson(testTask) + "," +
                                            json.toJson(testSubTask)+ "]", response.body(),
                                    method.toString() + " wrong result GET /tasks/");
                    } catch (IOException | InterruptedException e) {
                        System.out.println(this.getClass().getSimpleName() + ": error");
                    }

                    break;
                case "DELETE":
                    request = HttpRequest.newBuilder()
                            .DELETE()
                            .uri(url)
                            .build();
                    try {
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        Assertions.assertEquals(200, response.statusCode());
                        Assertions.assertEquals("All issues were deleted", response.body());
                    } catch (IOException | InterruptedException e) {
                        System.out.println(this.getClass().getSimpleName() + ": error");
                    }
                    break;
            }
        }
    }
    @Test
    public void tasksEndPointTest(){
        URI url = URI.create("http://localhost:8080/tasks/task");
        for (String method : methods) {
            HttpRequest request = null;
            switch (method) {
                case "GET":
                    // get all tasks
                    request = HttpRequest.newBuilder()
                            .GET()
                            .uri(url)
                            .build();
                    try {
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        Assertions.assertEquals(200, response.statusCode(),
                                "wrong response for " + method + " " + url);
                        Assertions.assertEquals(
                                "[" + json.toJson(testTask) + "]", response.body(),
                                "wrong body for " + method + " " + url);
                    } catch (IOException | InterruptedException e) {
                        System.out.println(this.getClass().getSimpleName() + ": error");
                    }

                    //get task by id
                    URI urlById = URI.create(url.toString() + "?id=" + testTask.getId());
                    request = HttpRequest.newBuilder()
                            .GET()
                            .uri(urlById)
                            .build();
                    try {
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        Assertions.assertEquals(200, response.statusCode(),
                                "wrong response for " + method + " " + urlById);
                        Assertions.assertEquals(json.toJson(testTask), response.body(),
                                "wrong body for " + method + " " + urlById);
                    } catch (IOException | InterruptedException e) {
                        System.out.println(this.getClass().getSimpleName() + ": error");
                    }

                    //get task by wrong id
                    urlById = URI.create(url.toString() + "?id=300");
                    request = HttpRequest.newBuilder()
                            .GET()
                            .uri(urlById)
                            .build();
                    try {
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        Assertions.assertEquals(404, response.statusCode(),
                                "wrong response for " + method + " " + urlById);
                        Assertions.assertEquals("Task with ID=300 not found", response.body(),
                                "wrong body for " + method + " " + urlById);
                    } catch (IOException | InterruptedException e) {
                        System.out.println(this.getClass().getSimpleName() + ": error");
                    }
                    break;
                case "POST":
                    Task taskForPost = createNonCrossingTask();
                    // post new task
                    request = HttpRequest.newBuilder()
                            .POST(HttpRequest.BodyPublishers.ofString(json.toJson(taskForPost)))
                            .uri(url)
                            .build();
                    try {
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        Assertions.assertEquals(202, response.statusCode(),
                                "wrong response for " + method + " " + url);
                        Assertions.assertEquals(json.toJson(taskForPost), response.body(),
                                "wrong body for " + method + " " + url);
                    } catch (IOException | InterruptedException e) {
                        System.out.println(this.getClass().getSimpleName() + ": error");
                    }

                    // update existing task
                    testTask = manager.getTaskList().get(0);
                    testTask.setName("UPDATED "+ testTask.getName());
                    request = HttpRequest.newBuilder()
                            .POST(HttpRequest.BodyPublishers.ofString(json.toJson(testTask)))
                            .uri(url)
                            .build();
                    try {
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        Assertions.assertEquals(202, response.statusCode(),
                                "wrong response for " + method + " " + url);
                        Assertions.assertEquals(json.toJson(testTask), response.body(),
                                "wrong body for " + method + " " + url);
                    } catch (IOException | InterruptedException e) {
                        System.out.println(this.getClass().getSimpleName() + ": error");
                    }
                    break;

                case "DELETE":
                    // delete all tasks
                    request = HttpRequest.newBuilder()
                            .DELETE()
                            .uri(url)
                            .build();
                    try {
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        Assertions.assertEquals(200, response.statusCode(),
                                "wrong response for " + method + " " + url);
                        Assertions.assertEquals("All tasks were deleted", response.body(),
                                "wrong body for " + method + " " + url);
                    } catch (IOException | InterruptedException e) {
                        System.out.println(this.getClass().getSimpleName() + ": error");
                    }

                    // delete task by id
                    manager.createTask(testTask);
                    urlById = URI.create(url.toString() + "?id=" + testTask.getId());
                    request = HttpRequest.newBuilder()
                            .DELETE()
                            .uri(urlById)
                            .build();
                    try {
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        Assertions.assertEquals(200, response.statusCode(),
                                "wrong response for " + method + " " + urlById);
                        Assertions.assertEquals("Task ID=" + testTask.getId() + " was deleted", response.body(),
                                "wrong body for " + method + " " + urlById);
                    } catch (IOException | InterruptedException e) {
                        System.out.println(this.getClass().getSimpleName() + ": error");
                    }

                    // delete task by wrong id
                    urlById = URI.create(url.toString() + "?id=200");
                    request = HttpRequest.newBuilder()
                            .DELETE()
                            .uri(urlById)
                            .build();
                    try {
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        Assertions.assertEquals(404, response.statusCode(),
                                "wrong response for " + method + " " + urlById);
                        Assertions.assertEquals("Task with ID=200 not found", response.body(),
                                "wrong body for " + method + " " + urlById);
                    } catch (IOException | InterruptedException e) {
                        System.out.println(this.getClass().getSimpleName() + ": error");
                    }
                    break;
            }
        }
    }
    @Test
    public void subtasksEndpointTest(){
        URI url = URI.create("http://localhost:8080/tasks/subtask");
        for (String method : methods) {
            HttpRequest request = null;
            switch (method) {
                case "GET":
                    // get all subtasks
                    request = HttpRequest.newBuilder()
                            .GET()
                            .uri(url)
                            .build();
                    try {
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        Assertions.assertEquals(200, response.statusCode(),
                                "wrong response for " + method + " " + url);
                        Assertions.assertEquals(
                                "[" + json.toJson(testSubTask) + "]", response.body(),
                                "wrong body for " + method + " " + url);
                    } catch (IOException | InterruptedException e) {
                        System.out.println(this.getClass().getSimpleName() + ": error");
                    }

                    //get subtask by id
                    testSubTask.setId(manager.getSubtaskList().get(0).getId());
                    URI urlById = URI.create(url.toString() + "?id=" + testSubTask.getId());
                    request = HttpRequest.newBuilder()
                            .GET()
                            .uri(urlById)
                            .build();
                    try{
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        Assertions.assertEquals(200, response.statusCode(),
                                "wrong response for " + method + " " + url);
                        Assertions.assertEquals(
                                json.toJson(testSubTask), response.body(),
                                "wrong body for " + method + " " + url);
                    } catch (IOException | InterruptedException e){
                        System.out.println(this.getClass().getSimpleName() + ": error");
                    }

                    // get wrong subtask
                    urlById = URI.create(url.toString() + "?id=300");
                    request = HttpRequest.newBuilder()
                            .GET()
                            .uri(urlById)
                            .build();
                    try{
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        Assertions.assertEquals(404, response.statusCode(),
                                "wrong response for " + method + " " + url);
                        Assertions.assertEquals(
                                "SubTask with ID=300 not found", response.body(),
                                "wrong body for " + method + " " + url);
                    } catch (IOException | InterruptedException e){
                        System.out.println(this.getClass().getSimpleName() + ": error");
                    }
                    break;
                case "POST":
                    //post new subtask
                    SubTask subtaskForPosting = createNonCrossingSubTask();
                    subtaskForPosting.setId(11); // manager will set this id at the moment
                    subtaskForPosting.setName("ST for testing POST");
                    request = HttpRequest.newBuilder()
                            .POST(HttpRequest.BodyPublishers.ofString(json.toJson(subtaskForPosting)))
                            .uri(url)
                            .build();
                    try{
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        Assertions.assertEquals(201, response.statusCode(),
                                "wrong response for " + method + " " + url);
                        Assertions.assertEquals(json.toJson(subtaskForPosting), response.body(),
                                "wrong body for " + method + " " + url);
                    } catch (IOException | InterruptedException e){
                        System.out.println(this.getClass().getSimpleName() + ": error");
                    }
                    break;

                case "DELETE":
                    // delete subtask by id
                    SubTask subTaskForDeleting = createNonCrossingSubTask();
                    urlById = URI.create(url.toString() + "?id=" + subTaskForDeleting.getId());
                    request = HttpRequest.newBuilder()
                            .DELETE()
                            .uri(urlById)
                            .build();
                    try {
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        Assertions.assertEquals(200, response.statusCode(),
                                "wrong response for " + method + " " + url);
                        Assertions.assertEquals("Subtask ID=" + subTaskForDeleting.getId() + " was deleted", response.body(),
                                "wrong body for " + method + " " + url);
                    } catch (IOException | InterruptedException e){
                        System.out.println(this.getClass().getSimpleName() + ": error");
                    }

                    //delete all subtasks
                    request = HttpRequest.newBuilder()
                            .DELETE()
                            .uri(url)
                            .build();
                    try{
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        Assertions.assertEquals(200, response.statusCode(),
                                "wrong response for " + method + " " + url);
                        Assertions.assertEquals("All subtasks were deleted", response.body(),
                                "wrong body for " + method + " " + url);
                    } catch (IOException | InterruptedException e){
                        System.out.println(this.getClass().getSimpleName() + ": error");
                    }
                    break;
            }
        }
    }
    @Test
    public void epicEndpointTest(){
        URI url = URI.create("http://localhost:8080/tasks/epic");
        for (String method : methods){
            switch (method){
                case "GET":
                    // get all epics
                    HttpRequest request = HttpRequest.newBuilder()
                            .GET()
                            .uri(url)
                            .build();
                    try{
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        Assertions.assertEquals(200, response.statusCode(),
                                "wrong response for " + method + " " + url);
                        Assertions.assertEquals("[" + json.toJson(testEpic) + "]", response.body(),
                                "wrong body for " + method + " " + url);
                    } catch (IOException | InterruptedException e) {
                        System.out.println(this.getClass().getSimpleName() + ": error");
                    }

                    //get epic by id
                    URI urlById = URI.create(url.toString() + "?id=" + testEpic.getId());
                    request = HttpRequest.newBuilder()
                            .GET()
                            .uri(urlById)
                            .build();
                    try{
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        Assertions.assertEquals(200, response.statusCode(),
                                "wrong response for " + method + " " + url);
                        Assertions.assertEquals(json.toJson(testEpic), response.body(),
                                "wrong body for " + method + " " + url);
                    } catch (IOException | InterruptedException e) {
                        System.out.println(this.getClass().getSimpleName() + ": error");
                    }

                    //get epic by wrong id
                    urlById = URI.create(url.toString() + "?id=300");
                    request = HttpRequest.newBuilder()
                            .GET()
                            .uri(urlById)
                            .build();
                    try{
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        Assertions.assertEquals(404, response.statusCode(),
                                "wrong response for " + method + " " + url);
                        Assertions.assertEquals("Epic with ID=300 not found", response.body(),
                                "wrong body for " + method + " " + url);
                    } catch (IOException | InterruptedException e) {
                        System.out.println(this.getClass().getSimpleName() + ": error");
                    }
                    break;
                case "DELETE":
                    //delete epic by id
                    urlById = URI.create(url.toString() + "?id=" + testEpic.getId());
                    request = HttpRequest.newBuilder()
                            .DELETE()
                            .uri(urlById)
                            .build();
                    try{
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        Assertions.assertEquals(200, response.statusCode(),
                                "wrong response for " + method + " " + url);
                        Assertions.assertEquals("Epic ID=" + testEpic.getId() + " was deleted", response.body(),
                                "wrong body for " + method + " " + url);
                    } catch (IOException | InterruptedException e) {
                        System.out.println(this.getClass().getSimpleName() + ": error");
                    }

                    //delete all epics
                    request = HttpRequest.newBuilder()
                            .DELETE()
                            .uri(url)
                            .build();
                    try{
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        Assertions.assertEquals(200, response.statusCode(),
                                "wrong response for " + method + " " + url);
                        Assertions.assertEquals("All epics are deleted", response.body(),
                                "wrong body for " + method + " " + url);
                    } catch (IOException | InterruptedException e) {
                        System.out.println(this.getClass().getSimpleName() + ": error");
                    }
                    break;

                case "POST":
                    //post new epic
                    Epic epicForPost = createEpic();
                    epicForPost.setId(manager.getEpicList().get(0).getId());
                    manager.deleteEpicById(epicForPost.getId());
                    epicForPost.setId(epicForPost.getId()+1);
                    request = HttpRequest.newBuilder()
                            .POST(HttpRequest.BodyPublishers.ofString(json.toJson(epicForPost)))
                            .uri(url)
                            .build();
                    try{
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        Assertions.assertEquals(201, response.statusCode(),
                                "wrong response for " + method + " " + url);
                        Assertions.assertEquals(json.toJson(epicForPost), response.body(),
                                "wrong body for " + method + " " + url);
                    } catch (IOException | InterruptedException e) {
                        System.out.println(this.getClass().getSimpleName() + ": error");
                    }

                    // modify epic
                    epicForPost.setDescription("Update test");
                    request = HttpRequest.newBuilder()
                            .POST(HttpRequest.BodyPublishers.ofString(json.toJson(epicForPost)))
                            .uri(url)
                            .build();
                    try {
                        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                        Assertions.assertEquals(202, response.statusCode(),
                                "wrong response for " + method + " " + url);
                        Assertions.assertEquals(json.toJson(epicForPost), response.body(),
                                "wrong body for " + method + " " + url);
                    } catch (IOException | InterruptedException e) {
                        System.out.println(this.getClass().getSimpleName() + ": error");
                    }
                    break;
            }
        }
    }

    @Test
    public void historyEndPointTest(){
        URI url = URI.create("http://localhost:8080/tasks/epic");
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(url)
                .build();
        try{
            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Assertions.assertEquals(200, response.statusCode(),
                    "wrong response for " + request.method() + " " + url);
            Assertions.assertEquals("[" + json.toJson(manager.getEpicList().get(0)) + "]", response.body());
        } catch (IOException | InterruptedException e) {
            System.out.println(this.getClass().getSimpleName() + ": error");
        }
    }
}
