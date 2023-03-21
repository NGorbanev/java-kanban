import Interfaces.HistoryManager;
import Issues.StatusList;
import Issues.Task;
import Utils.Managers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class HistoryManagerTest {
    HistoryManager manager = Managers.getDefaultHistoryManager();
    Task testIssue1;

    @BeforeEach
    public void testIsueConfigure(){
        testIssue1 = new Task();
        testIssue1.setId(1);
        testIssue1.setName("Test task");
        testIssue1.setDescription("test task description");
        testIssue1.setStatus(StatusList.NEW);
    }
    @Test
    public void getHistory() {
        // проверка пустой истории. Список есть и должен быть пустым
        Assertions.assertEquals(0, manager.getHistory().size());
    }
    @Test
    public void addTest(){
        // стандартное поведение
        manager.add(testIssue1);
        Assertions.assertEquals(1, manager.getHistory().size());

        // множественное дублирование
        for (int i = 1; i <= 100500; i++) manager.add(testIssue1);
        Assertions.assertEquals(1, manager.getHistory().size()); // проверяем что всего один объект в истории
        Assertions.assertEquals(testIssue1.toString(), manager.getHistory().get(0).toString()); // проверяем что объект тот же, что и множественно добавлялся
    }
    @Test
    public void remove(){
        // Прямой кейс
        manager.remove(1);
        Assertions.assertEquals(0, manager.getHistory().size());

        // удаляем несуществующий объект в истории и поведение не меняется, ошибок не возникает
        manager.remove(100);
        Assertions.assertEquals(0, manager.getHistory().size());
    }
}
