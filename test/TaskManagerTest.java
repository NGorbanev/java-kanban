import interfaces.TaskManager;
import issues.Epic;
import issues.StatusList;
import issues.SubTask;
import issues.Task;

import java.time.*;
import java.util.HashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.TimeLineCrossingsException;


public abstract class TaskManagerTest<T extends TaskManager> {

    T manager;
    Task testTask;
    Epic testEpic;
    SubTask testSubTask;

    @BeforeEach
    public void createIssues() {
        testTask = manager.createTask(new Task("TestTaskName1", "Task ID should be = 1"));
        manager.createEpic(new Epic("TestEpic1", "Epic ID should be = 2"));
        testSubTask = manager.createSubTask(new SubTask(
                "TestSubTask1",
                "SubTask ID should be = 3",
                2));
        testEpic = manager.getEpicList().get(0);


    }

    // Epic's methods
/*
    @Test
    private void calculateEpicDurationTest(){
        SubTask testSubTask2 = manager.createSubTask(new SubTask(
                "TestSubTask2", "Needed for duration test",2));

        manager.getEpicById(testEpic.getId()).setStartTime(Instant.ofEpochMilli(0));
        manager.getSubTaskById(testSubTask2.getId()).setStartTime(Instant.now());
        testSubTask2.setStartTime(manager.getSubTaskById(testSubTask2.getId()).getStartTime());
        manager.getSubTaskById(testSubTask.getId()).setDuration(1L);
        manager.getSubTaskById(testSubTask2.getId()).setDuration(5L);
        //manager.calculateEpicDuration(testEpic);
        Assertions.assertEquals(2, manager.getEpicById(2).getSubTasks().size()); // two subtasks should be here
        // start time equals the earliest subtask start time:
        Assertions.assertEquals(manager.getSubTaskById(3).getStartTime(), manager.getEpicById(2).getStartTime());
        // end time equals latest subtask end time:
        Assertions.assertEquals(testSubTask2.getEndTime(), manager.getEpicById(2).getEndTime());
    }
*/
    @Test
    public void taskTestingUnit() {
        Assertions.assertEquals(
                "ID=1, TYPE=TASK,STATUS=NEW, NAME=TestTaskName1, DESCRIPTION=Task ID should be = 1, " +
                        "START_TIME=1970-01-01T00:00:00Z, DURATION=0, END_TIME=1970-01-01T00:00:00Z",
                testTask.toString());
        Assertions.assertEquals(1, manager.getTaskList().size(),
                "Task не добавлен в taskList"); // Task list check
        Assertions.assertSame(testTask, manager.getTaskList().get(0),
                "В taskList добавлен некорректный объект Task"); // check if the task is a proper one

        // status changing test
        Assertions.assertEquals(StatusList.NEW, testTask.getStatus(),
                "Неверный статус Task");
        manager.setTaskStatus(testTask, StatusList.IN_PROGRESS);
        Assertions.assertEquals(StatusList.IN_PROGRESS, testTask.getStatus(),
                "Неверный статус Task");
        manager.setTaskStatus(testTask, StatusList.DONE);
        Assertions.assertEquals(StatusList.DONE, testTask.getStatus(),
                "Неверный статус Task");

        // history check
        Assertions.assertEquals(1, manager.getHistory().size(),
                "количество записей в истории не соответствует ожидаемому");
    }
    @Test
    public void epicTestingUnit(){

        // epic count should be = 1
        Assertions.assertEquals(1, manager.getEpicList().size(),
                "Количество эпиков в списке не соответствует ожидаемому");

        // check if Subtasks linked to epic count is equals actual subtasks count
        Assertions.assertEquals(manager.getEpicById(2).getSubTasks().size(), manager.getSubtaskList().size(),
                "Количество SubTask в списке не соответствует ожидаемому");

        // check subtask delete method
        manager.deleteSubTaskById(3);
        Assertions.assertEquals(
                0,
                manager.getSubtasks().size(),
                "SubTask не удален из subTaskList");
        Assertions.assertEquals(
                0,
                testEpic.getSubTasks().size(),
                "SubTask не удалена из родительского эпика"); // check result of a case

        // epic creation test
        Assertions.assertEquals(
                "ID=2, TYPE=EPIC, STATUS=NEW, NAME=TestEpic1, DESCRIPTION=Epic ID should be = 2, " +
                        "START_TIME=1970-01-01T00:00:00Z, DURATION=0, END_TIME=1970-01-01T00:00:00Z",
                testEpic.toString());
        Assertions.assertEquals(1, manager.getEpicList().size(),
                "Epic не добавлен в список epicList"); // check epiclist records
        Assertions.assertSame(testEpic, manager.getEpicList().get(0),
                "Некорректный объект добавлен в epicList"); // check if the epic is proper one
        Assertions.assertEquals(0, testEpic.getSubTasks().size(),
                "Некорректное количество дочерних задач в эпике"); // check if subtasklist of epic is empty
        Assertions.assertNull(manager.getSubTaskById(100)); // request of null subtask

        NullPointerException ex = Assertions.assertThrows( // exception for null subtask request
                NullPointerException.class,
                () -> manager.getSubTaskById(100).getId()
        );
        // create subtasks for epic status check
        testSubTask = manager.createSubTask(new SubTask(
                "TestSubTask1",
                "SubTask ID should be = 3",
                2));
        SubTask testSubTask2 = manager.createSubTask(new SubTask(
                "TestSubTask2",
                "SubTask ID should be = 4",
                2));

        Assertions.assertEquals(2, testEpic.getSubTasks().size()); // check if subtasks were created correctly

        // all subtasks are at NEW status. Epic status should be NEW
        manager.setSubTaskStatus(testSubTask, StatusList.NEW);
        manager.setSubTaskStatus(testSubTask2, StatusList.NEW);
        Assertions.assertEquals(StatusList.NEW, testEpic.getStatus());

        // All subtaskss are at DONE status. Epic status should be DONE
        manager.setSubTaskStatus(testSubTask, StatusList.DONE);
        manager.setSubTaskStatus(testSubTask2, StatusList.DONE);
        Assertions.assertEquals(StatusList.DONE, testEpic.getStatus());

        // some subtasks ar at NEW and some are at DONE statuses. Epic status should be IN_PROGRESS
        manager.setSubTaskStatus(testSubTask, StatusList.DONE);
        manager.setSubTaskStatus(testSubTask2, StatusList.NEW);
        Assertions.assertEquals(StatusList.IN_PROGRESS, testEpic.getStatus());

        // subtasks are at IN_PROGRESS status, epic should be IN_PROGRESS
        manager.setSubTaskStatus(testSubTask, StatusList.IN_PROGRESS);
        manager.setSubTaskStatus(testSubTask2, StatusList.IN_PROGRESS);
        Assertions.assertEquals(StatusList.IN_PROGRESS, testEpic.getStatus());

        // history check
        Assertions.assertEquals(1, manager.getHistory().size());

    }

    @Test
    public void epicEndTimeTest(){
        SubTask secondSubTask = manager.createSubTask(new SubTask(
                "ST For epDurTest",
                "testSubtask",
                StatusList.NEW,
                2,
                0,
                Instant.ofEpochSecond(900),
                150));
        Epic e = manager.getEpicById(2);
        Assertions.assertEquals(secondSubTask.getEndTime(), e.getEndTime());
    }

    @Test
    public void subTaskTestingUnit(){
        Assertions.assertEquals(
                "ID=3, TYPE=SUBTASK, STATUS = NEW, PARENT=2, NAME=TestSubTask1, " +
                        "DESCRIPTION=SubTask ID should be = 3, START_TIME=1970-01-01T00:00:00Z, DURATION=0, " +
                        "END_TIME=1970-01-01T00:00:00Z",
                testSubTask.toString(),
                "Неверное содержание сабтаски"
        );
        Assertions.assertEquals(2, testSubTask.getParentEpicId()); // check if epic has a data of subtask

        // history check
        Assertions.assertEquals(1, manager.getHistory().size());
    }
    @Test
    public void deleteEpicByIdTest(){
        manager.deleteEpicById(2); // delete epic
        Assertions.assertEquals(0, manager.getEpicList().size()); // should be 0
        Assertions.assertThrows(
                NullPointerException.class,() -> {
                    manager.deleteEpicById(2); // trying to delete non-existing epic. Case of trying to rich non-existing epic is also tested here
                });

        // Check if history is cleared after deleting issues
        Assertions.assertEquals(0, manager.getHistory().size());
    }
    @Test
    public void deleteAllEpicsTest(){
        manager.deleteAllEpics();
        Assertions.assertEquals(0, manager.getEpicList().size());

        // Check if history is cleared after deleting issues
        Assertions.assertEquals(0, manager.getHistory().size());
    }
    @Test
    public void getEpicByIdTest(){
        Assertions.assertSame(testEpic, manager.getEpicById(2));

        // Check if history is cleared after deleting issues
        Assertions.assertEquals(1, manager.getHistory().size());
    }
    @Test
    public void getEpicListTest(){
        Assertions.assertEquals(1, manager.getEpicList().size()); // standart case
        manager.deleteAllEpics();
        Assertions.assertEquals(0, manager.getEpicList().size()); // case with empty list
        Assertions.assertNull(manager.getEpicById(100)); // requesting non-existing issue
    }

    @Test
    public void getAllSubtasksByEpicIdTest(){
        // standart case
        Assertions.assertEquals(1, manager.getAllSubtasksByEpicId(2).size());

        // list clearance check
        manager.deleteAllSubTasks();
        Assertions.assertEquals(0, manager.getAllSubtasksByEpicId(2).size());

        // non-existing issue request. Exception expected
        Assertions.assertThrows(NullPointerException.class, () -> manager.getAllSubtasksByEpicId(100).size());

        // history check
        Assertions.assertEquals(1, manager.getHistory().size());
    }
    @Test
    public void updateEpicTest(){
        Epic epic = manager.getEpicById(2);
        epic.setName("Updated epic name");
        manager.updateEpic(epic);
        Assertions.assertSame(epic, manager.getEpicById(2)); // standart case
        manager.deleteAllEpics();
        final Epic nullEpic = manager.getEpicById(100); // requesting non-existent epic
        Assertions.assertThrows(NullPointerException.class, () -> {
            manager.updateEpic(nullEpic);}); // check method with empty list
    }

    // Issues.SubTask methods
    @Test
    public void createSubTaskTest(){
        Assertions.assertEquals(1, manager.getSubtaskList().size());

        /*
        Assertions.assertThrows(NullPointerException.class, () -> {
            SubTask subTaskForNullEpic = manager.createSubTask(new SubTask(
                    "Test subtask",
                    "Link to null epic",
                    100))
                    ;});
        */
        Assertions.assertNull(manager.createSubTask(new SubTask(
                "Test subtask",
                "Link to null epic",
                100)));
        // history check
        Assertions.assertEquals(1, manager.getHistory().size());
    }
    @Test
    public void getSubtasksTest(){
        manager.createSubTask(new SubTask(
                "TestSubTask2",
                "This subtask is needed for testing get subTasks()",
                2));
        HashMap<Integer, SubTask> testHashMap = manager.getSubtasks();
        Assertions.assertEquals(
                "ID=4, TYPE=SUBTASK, STATUS = NEW, PARENT=2, NAME=TestSubTask2, " +
                        "DESCRIPTION=This subtask is needed for testing get subTasks(), " +
                        "START_TIME=1970-01-01T00:00:00Z, DURATION=0, END_TIME=1970-01-01T00:00:00Z",
                manager.getSubtasks().get(4).toString());
        // history check
        Assertions.assertEquals(1, manager.getHistory().size());
    }
    @Test
    public void getSubtaskListTest(){
        Assertions.assertEquals(testSubTask.toString(), manager.getSubtaskList().get(0).toString());

        // history check
        Assertions.assertEquals(1, manager.getHistory().size());

        manager.deleteAllSubTasks();
        Assertions.assertEquals(0, manager.getSubtasks().size());
        testSubTask = manager.createSubTask(new SubTask(
                "Test subtask",
                "Subtask for test",
                2));
        manager.deleteAllEpics();
        Assertions.assertEquals(0, manager.getSubtasks().size()); // проверяем что при удалении эпиков сабтаски тоже удаляются

        // check if the issues are not in history
        Assertions.assertEquals(0, manager.getHistory().size());
    }
    @Test
    public void setSubTaskStatusTest(){
        testSubTask = manager.getSubTaskById(3);
        manager.setSubTaskStatus(testSubTask, StatusList.DONE);
        Assertions.assertEquals(StatusList.DONE, testSubTask.getStatus());
        // history check. Should be two because getSubTaskById was used before
        Assertions.assertEquals(2, manager.getHistory().size());
    }
    @Test
    public void linkSubTaskTest(){
        manager.createEpic(new Epic(
                "Epic ID 4",
                "Here the subtask should be linked"));
        manager.createSubTask(new SubTask(
                "Subtask for testing linkSubTask()",
                "This subtask should be linked to epic ID=5",
                2));
        Assertions.assertEquals(0, manager.getEpicById(4).getSubTasks().size()); // epic before linking subtask
        manager.linkSubTask(manager.getEpicById(4), manager.getSubTaskById(5));
        Assertions.assertEquals(1, manager.getEpicById(4).getSubTasks().size()); // epic after linking subtask

        // trying to link non-existent subtask to non-existent epic, NullPointerException should be reached
        Assertions.assertThrows(NullPointerException.class, () -> {
            manager.linkSubTask(manager.getEpicById(100), manager.getSubTaskById(5));
        });
    }
    @Test
    public void getSubTaskByIdTest(){
        // direct case
        Assertions.assertEquals(
                manager.getSubtaskList().get(0).toString(),
                manager.getSubTaskById(3).toString());

        // requesting non-existent subTask
        Assertions.assertThrows(NullPointerException.class, () -> {
            manager.getSubTaskById(100).getId();
        });
    }
    @Test
    public void updateSubTaskTest(){
        SubTask subTaskForTest = testSubTask;
        subTaskForTest.setName("New name for update");
        manager.updateSubTask(subTaskForTest);
        Assertions.assertEquals("New name for update", manager.getSubTaskById(testSubTask.getId()).getName());

        // case with empty list. Get saved subtask and trying to link it to empty list
        manager.deleteAllSubTasks(); // deleting all subtasks. subTaskList size = 0
        manager.updateSubTask(subTaskForTest); // updating subtask saved before
        Assertions.assertNotNull(manager.getSubTaskById(subTaskForTest.getId())); // expected: update comes correct

        // deleting epics. Expected behavior - updateSubTask doesn't come at empty lists subTaskList & epicList
        manager.deleteAllEpics();
        Assertions.assertEquals(0, manager.getEpicList().size());

        //if all epics are deleted, none of subtask to be updated, they are also gone
        Assertions.assertEquals(0, manager.getSubtasks().size());
    }
    @Test
    public void deleteSubTaskByIdTest(){
        // direct case
        testSubTask = manager.getSubTaskById(3);
        manager.deleteSubTaskById(testSubTask.getId());
        Assertions.assertEquals(0, manager.getSubtaskList().size());

        // deleting non-existent subtask. Expectations - NullPointerEx. Same behavior should be with empty list
        Assertions.assertThrows(NullPointerException.class, () -> {
            manager.deleteSubTaskById(100);
        });
        /*
        History check. While requesting deleteSubTaskById the maternal epic is also requesting, that's why it remains in History.
        Need to check if it's the only one out there
        */
        Assertions.assertEquals(
                "[ID=2, TYPE=EPIC, STATUS=NEW, NAME=TestEpic1, DESCRIPTION=Epic ID should be = 2, " +
                        "START_TIME=1970-01-01T00:00:00Z, DURATION=0, END_TIME=1970-01-01T00:00:00Z]",
                manager.getHistory().toString());
    }
    @Test
    public void deleteAllSubTasksTest(){
        // direct case
        manager.deleteAllSubTasks();
        Assertions.assertEquals(0, manager.getSubtaskList().size());

        // trying to delete at empty list. Expected result - same as direct case
        manager.deleteAllSubTasks();
        Assertions.assertEquals(0, manager.getSubtaskList().size());

        // same logic as deleting one task is expected. Cause mass deletion goes the same way
        Assertions.assertEquals(
                "[ID=2, TYPE=EPIC, STATUS=NEW, NAME=TestEpic1, DESCRIPTION=Epic ID should be = 2, " +
                        "START_TIME=1970-01-01T00:00:00Z, DURATION=0, END_TIME=1970-01-01T00:00:00Z]",
                manager.getHistory().toString());
    }

    // Task methods
    @Test
    public void createTaskTest(){
        manager.createTask(new Task("Test task2", "Issue id should be 4"));
        Assertions.assertEquals(2, manager.getTaskList().size());

        // history check
        Assertions.assertEquals(1, manager.getHistory().size());
    }
    @Test
    public void updateTaskTest(){
        // direct case
        testTask.setName("New name for test task");
        manager.updateTask(testTask);
        Assertions.assertEquals("New name for test task", manager.getTaskById(1).getName());

        // trying to update non-existing task
        Task deletedTask = testTask;
        manager.deleteAllTasks();
        manager.updateTask(deletedTask);
        Assertions.assertSame(deletedTask, manager.getTaskById(deletedTask.getId())); // check if update is done
        Assertions.assertNotNull(manager.getTaskById(deletedTask.getId())); // check that received object is != null
    }
    @Test
    public void setTaskStatus(){
        // direct case
        testTask.setStatus(StatusList.IN_PROGRESS);
        manager.updateTask(testTask);
        Assertions.assertEquals(StatusList.IN_PROGRESS, manager.getTaskById(testTask.getId()).getStatus());

        // trying to change status of non-existing task should cause NullPointException
        Assertions.assertThrows(NullPointerException.class, ()->{
            manager.setTaskStatus(manager.getTaskById(100), StatusList.DONE);
        });
    }
    @Test
    public void deleteAllTasksTest(){
        // Direct case
        manager.deleteAllTasks();
        Assertions.assertEquals(0, manager.getTaskList().size());

        // Same attempt on an empty list. Expected result - same as direct case
        manager.deleteAllTasks();
        Assertions.assertEquals(0, manager.getTaskList().size());
    }
    @Test
    public void getTaskByIdTest(){
        // Direct case
        Assertions.assertSame(testTask, manager.getTaskById(1));

        // request of non-existent task
        Assertions.assertNull(manager.getTaskById(100));
    }
    @Test
    public void deleteTaskByIdTest(){
        // Direct case
        manager.deleteTaskById(1);
        Assertions.assertEquals(0, manager.getTaskList().size());

        // Attempt to delete non-existing task
        manager.deleteTaskById(1);
        Assertions.assertEquals(0, manager.getTaskList().size());
    }
    @Test
    public void getTaskListTest(){
        // Direct case
        Assertions.assertEquals(1, manager.getTaskList().size());

        // Attempt to request an empty list. Expected result - taskList.size() = 0
        manager.deleteAllTasks();
        //Assertions.assertEquals(0, manager.getTaskList().size());
        Assertions.assertEquals(0, manager.getTaskList().size());
    }
    @Test
    public void getHistoryTest(){
        // direct case
        Assertions.assertEquals(
                "[ID=2, TYPE=EPIC, STATUS=NEW, NAME=TestEpic1, DESCRIPTION=Epic ID should be = 2, " +
                        "START_TIME=1970-01-01T00:00:00Z, DURATION=0, END_TIME=1970-01-01T00:00:00Z]",
                manager.getHistory().toString());
    }
    @Test
    public void getPrioritizedTasks(){
        manager.getTaskById(testTask.getId()).setStartTime(Instant.ofEpochMilli(1L));
        manager.getEpicById(testEpic.getId()).setStartTime(Instant.ofEpochMilli(60_000L));
        manager.getSubTaskById(testSubTask.getId()).setStartTime(Instant.ofEpochMilli(40_000L));
        Assertions.assertNotNull(manager.getPrioritizedTasks());
        Assertions.assertEquals(
                "[ID=1, TYPE=TASK,STATUS=NEW, NAME=TestTaskName1, DESCRIPTION=Task ID should be = 1, " +
                        "START_TIME=1970-01-01T00:00:00.001Z, DURATION=0, END_TIME=1970-01-01T00:00:00.001Z, " +
                        "ID=3, TYPE=SUBTASK, STATUS = NEW, PARENT=2, NAME=TestSubTask1, " +
                        "DESCRIPTION=SubTask ID should be = 3, START_TIME=1970-01-01T00:00:40Z, " +
                        "DURATION=0, END_TIME=1970-01-01T00:00:40Z]",
                manager.getPrioritizedTasks().toString());
    }

    @Test
    public void taskCrossingsTest(){
        testTask.setStartTime(Instant.ofEpochMilli(0)); // Start time should be 0
        testTask.setDuration(300_000L); // duration is 5 minutes for now (300_000MS)
        Task wrongTask = new Task("Unbeatable task", "Task not to be registered");
        wrongTask.setStartTime(Instant.ofEpochSecond(3));
        try {
            Assertions.assertNull(manager.createTask(wrongTask));
        } catch (TimeLineCrossingsException ex){
            //System.out.println(ex.getMessage());
            return;
        }

        SubTask testSubtask2 = manager.createSubTask(new SubTask(
                "Right subtask",
                "this subtask should be ok", 2));
        testSubtask2.setStartTime(Instant.ofEpochMilli(300_001L));
        testSubtask2.setDuration(600_000L);
        Assertions.assertEquals(1,
                manager.getSubTaskById(testSubtask2.getId()).getStartTime().
                        minusMillis(manager.getTaskById(testTask.getId()).getDuration()).toEpochMilli());

    }
/*
    @Test
    public void testIntersection() {
        Instant now = LocalDateTime.of(
                LocalDate.now().plusYears(1),
                LocalTime.of(0, 0, 0)).toInstant(ZoneOffset.UTC);
        Task task1 = manager.createTask(
                new Task("task 1", "", StatusList.NEW, 0, now, 60));
        Assertions.assertThrows(TimeLineCrossingsException.class,
                ()->{
                    Task task2 = manager.createTask(
                            new Task(
                                    "task 2",
                                    "",
                                    StatusList.NEW,
                                    0,
                                    task1.getStartTime().plusSeconds(-1800), 60));
                });
    }
    */
}


