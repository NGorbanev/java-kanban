import taskManager.FileBackedTasksManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTasksManager> {
    Path path = Path.of("./src/Data/test.csv");

    FileBackedTaskManagerTest(){
        manager = new FileBackedTasksManager(path.toString());
    }

    @AfterEach
    public void fileClear() throws FileNotFoundException {
        File file = new File(path.toString());
        PrintWriter writer = new PrintWriter(file);
        writer.print("");
        writer.close();
    }

    @Test
    public void saveAndLoadTest(){
        // load data from file to a new manager
        manager.save();
        FileBackedTasksManager managerForTest = new FileBackedTasksManager(path.toString());

        // check count of tasks and proper task
        Assertions.assertEquals(manager.getTaskList().size(), managerForTest.getTaskList().size(),
                "Неверно загружен список Task");
        Assertions.assertEquals(manager.getTaskList().get(0).toString(), managerForTest.getTaskList().get(0).toString(),
                "Задачи до загрузки не совпадают с задачами после загрузки");

        Assertions.assertEquals(manager.getEpicList().size(), managerForTest.getEpicList().size(),
                "Неверно загружен список Epic");
        Assertions.assertEquals(manager.getEpicList().get(0).toString(), managerForTest.getEpicList().get(0).toString(),
                "Эпики до загрузки не совпадают с эпиками после загрузки");

        Assertions.assertEquals(manager.getSubtaskList().size(), managerForTest.getSubtaskList().size(),
                "Неверно загружен список SubTask");
        Assertions.assertEquals(manager.getSubtaskList().get(0).toString(), managerForTest.getSubtaskList().get(0).toString(),
                "Сабтаски до загрузки не совпадают с сабтасками после загрузки");

    }
}
