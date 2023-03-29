import History.InMemoryHistoryManager;
import Issues.StatusList;
import Issues.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InMemoryHistoryManagerTest {
    int id = 0;
    InMemoryHistoryManager manager;
    // public Task(String name, String description, StatusList status, int id, Instant startTime, int dur)

    public Task createTask(){
        return new Task(
                "Test task",
                "Test description",
                StatusList.NEW,
                id++,
                Instant.now(),
                10);
    }

    @BeforeEach
    public void beforeEach(){
        manager = new InMemoryHistoryManager();
    }

    @Test
    public void addIssueTest(){
        Task task = createTask();
        manager.add(task);
        Assertions.assertEquals(task, manager.getHistory().get(0));
    }

    @Test
    public void removeIssueTest(){
        Task task = createTask();
        manager.add(task);
        manager.remove(task.getId());
        IndexOutOfBoundsException thrown = assertThrows(
                IndexOutOfBoundsException.class,
                () -> manager.getHistory().get(0)
        );
        assertEquals("Index 0 out of bounds for length 0", thrown.getMessage());
    }


}
