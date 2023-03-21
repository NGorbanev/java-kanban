import Issues.Epic;
import Issues.SubTask;
import Issues.Task;
import TaskManager.*;

import java.nio.file.Path;
import java.time.Instant;

import Utils.Managers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryManagerTest<T extends InMemoryTaskManager> {
    T manager = (T) Managers.getDefault();
    Path path = Path.of("./src/Data/test.csv"); // путь сохранения файла с тестовыми данными

    Task testTask;
    Epic testEpic;
    SubTask testSubTask;


    @BeforeEach
    public void createIssues() {
        testTask = manager.createTask("TestTaskName1", "Task ID should be = 1");
        testEpic = manager.createEpic("TestEpic1", "Epic ID should be = 2");
        testSubTask = manager.createSubTask(
                "TestSubTask1",
                "SubTask ID should be = 3",
                2);
    }

    @Test
    public void calculateEpicDuration(){
        SubTask testSubTask2 = manager.createSubTask(
                "TestSubTask2",
                "Needed for duration test",
                2
        );
        manager.getEpicById(testEpic.getId()).setStartTime(Instant.ofEpochMilli(0));
        manager.getSubTaskById(testSubTask.getId()).setDuration(1);
        manager.getSubTaskById(testSubTask2.getId()).setDuration(5);
        manager.calculateEpicDuration(testEpic);
        Assertions.assertEquals(360_000L, manager.getEpicById(testEpic.getId()).getDuration());
    }

    @Test
    public void getPrioritizedTasks(){
        manager.getTaskById(testTask.getId()).setStartTime(Instant.ofEpochMilli(1L));
        manager.getEpicById(testEpic.getId()).setStartTime(Instant.ofEpochMilli(60_000L));
        manager.getSubTaskById(testSubTask.getId()).setStartTime(Instant.ofEpochMilli(40_000L));
        //testTask.setStartTime(Instant.ofEpochMilli(0L)); // ID=1 expected to be first at treeSet
        //testEpic.setStartTime(Instant.ofEpochMilli(60_000L)); // ID=2 expected to be third at treeSet
        //testSubTask.setStartTime(Instant.ofEpochMilli(40_000L)); // ID=3 expected to be second at treeSet
        Assertions.assertNotNull(manager.getPrioritizedTasks());
        Assertions.assertEquals(
                "[ID=1, TYPE=TASK,STATUS=NEW, NAME=TestTaskName1, DESCRIPTION=Task ID should be = 1," +
                        " ID=3, TYPE=SUBTASK, STATUS = NEW, PARENT=2, NAME=TestSubTask1, DESCRIPTION=SubTask ID should be = 3," +
                        " ID=2, TYPE=EPIC, STATUS=NEW, NAME=TestEpic1, DESCRIPTION=Epic ID should be = 2]",
                manager.getPrioritizedTasks().toString());
    }

    @Test
    public void taskCrossingTest(){
        testTask.setStartTime(Instant.ofEpochMilli(0)); // Start time should be 0
        testTask.setDuration(5); // duration is 5 minutes for now (300_000MS)
        testSubTask.setStartTime(Instant.ofEpochMilli(40_000L)); // startTime should be 0 + 300_000MS
        testSubTask.setDuration(10); // duration is 10 minute (600_000MS)
        manager.calculateEpicDuration(manager.getEpicById(testEpic.getId()));
        manager.checkTimeline();
        Assertions.assertEquals(testTask.getEndTime(), testSubTask.getStartTime());
    }

}
