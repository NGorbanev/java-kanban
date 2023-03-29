import interfaces.TaskManager;
import issues.StatusList;
import issues.Task;
import issues.Epic;
import issues.SubTask;
import utils.Managers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;

public class OLDTaskManagerTest<T extends TaskManager> {

    //T manager = (T) Managers.getDefault();

    Path path = Path.of("./src/Data/test.csv"); // путь сохранения файла с тестовыми данными
    TaskManager manager = Managers.getFileBaked(path.toString()); // такой вариант гарантирует тестирования InMemoryTM & FileBackedTM
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

    @AfterEach // после каждого теста чистим файл, чтобы не мешали задачи, созданные в тестах ранее
    public void fileClear() throws FileNotFoundException {
        File file = new File(path.toString());
        PrintWriter writer = new PrintWriter(file);
        writer.print("");
        writer.close();
    }

    @Test
    public void calculateEpicDurationTest(){
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
    public void taskTestingUnit() {
        Assertions.assertEquals(
                "ID=1, TYPE=TASK,STATUS=NEW, NAME=TestTaskName1, DESCRIPTION=Task ID should be = 1",
                testTask.toString());
        Assertions.assertEquals(1, manager.getTaskList().size(),
                "Task не добавлен в taskList"); // проверяем что запись попала в список
        Assertions.assertSame(testTask, manager.getTaskList().get(0),
                "В taskList добавлен некорректный объект Task"); // проверяем что в списке именно тот объект

        // проверка изменений статусов
        Assertions.assertEquals(StatusList.NEW, testTask.getStatus(),
                "Неверный статус Task");
        manager.setTaskStatus(testTask, StatusList.IN_PROGRESS);
        Assertions.assertEquals(StatusList.IN_PROGRESS, testTask.getStatus(),
                "Неверный статус Task");
        manager.setTaskStatus(testTask, StatusList.DONE);
        Assertions.assertEquals(StatusList.DONE, testTask.getStatus(),
                "Неверный статус Task");

        // проверка наличия задачи в истории
        Assertions.assertEquals(1, manager.getHistory().size());
    }
    @Test
    public void epicTestingUnit(){

        Assertions.assertEquals(1, manager.getEpicList().size(),
                "Количество эпиков в списке не соответствует ожидаемому");
        Assertions.assertEquals(1, manager.getSubtaskList().size(),
                "Количество SubTask в списке не соответствует ожидаемому");
        manager.deleteSubTaskById(3);
        Assertions.assertEquals(
                0,
                manager.getSubtasks().size(),
                "SubTask не удален из subTaskList"); // проверка удаления сабтаски
        Assertions.assertEquals(
                0,
                testEpic.getSubTasks().size(),
                "SubTask не удалена из родительского эпика"); // проверка удаления сабтаски из эпика

        // проверка создания эпика
        Assertions.assertEquals(
                "ID=2, TYPE=EPIC, STATUS=NEW, NAME=TestEpic1, DESCRIPTION=Epic ID should be = 2",
                testEpic.toString());
        Assertions.assertEquals(1, manager.getEpicList().size(),
                "Epic не добавлен в список epicList"); // проверяем что запись попала в список
        Assertions.assertSame(testEpic, manager.getEpicList().get(0),
                "Некорректный объект добавлен в epicList"); // проверяем что в списке именно тот объект
        Assertions.assertEquals(0, testEpic.getSubTasks().size(),
                "Некорректное количество дочерних задач в эпике"); // проверяем что сабтасклист пуст
        Assertions.assertEquals(null, manager.getSubTaskById(100)); // проверка на получение несуществующей сабтаски

        NullPointerException ex = Assertions.assertThrows( // проверка обращения к параметрам несуществующей сабтаски
                NullPointerException.class,
                () -> manager.getSubTaskById(100).getId()
        );
        // создаем сабтаски для проверки работы расчета статуса
        testSubTask = manager.createSubTask(
                "TestSubTask1",
                "SubTask ID should be = 3",
                2);
        SubTask testSubTask2 = manager.createSubTask(
                "TestSubTask2",
                "SubTask ID should be = 4",
                2);

        Assertions.assertEquals(2, testEpic.getSubTasks().size()); // проверяем что сабтасклист теперь не пуст

        // Все подздадачи в статусе NEW
        manager.setSubTaskStatus(testSubTask, StatusList.NEW);
        manager.setSubTaskStatus(testSubTask2, StatusList.NEW);
        Assertions.assertEquals(StatusList.NEW, testEpic.getStatus());

        // Все подздадачи в статусе DONE
        manager.setSubTaskStatus(testSubTask, StatusList.DONE);
        manager.setSubTaskStatus(testSubTask2, StatusList.DONE);
        Assertions.assertEquals(StatusList.DONE, testEpic.getStatus());

        // подзадачи в статусе NEW и DONE
        manager.setSubTaskStatus(testSubTask, StatusList.DONE);
        manager.setSubTaskStatus(testSubTask2, StatusList.NEW);
        Assertions.assertEquals(StatusList.IN_PROGRESS, testEpic.getStatus());

        // подзадачи со статусом IN_PROGRESS
        manager.setSubTaskStatus(testSubTask, StatusList.IN_PROGRESS);
        manager.setSubTaskStatus(testSubTask2, StatusList.IN_PROGRESS);
        Assertions.assertEquals(StatusList.IN_PROGRESS, testEpic.getStatus());

        // проверка наличия задач в истории
        Assertions.assertEquals(1, manager.getHistory().size());

    }

    @Test
    public void subTaskTestingUnit(){
        Assertions.assertEquals(
                "ID=3, TYPE=SUBTASK, STATUS = NEW, PARENT=2, NAME=TestSubTask1, DESCRIPTION=SubTask ID should be = 3",
                testSubTask.toString(),
                "Неверное содержание сабтаски"
        );
        Assertions.assertEquals(2, testSubTask.getParentEpicId()); // проверяем что эпик привязан к сабтаске

        // проверка наличия задач в истории
        Assertions.assertEquals(1, manager.getHistory().size());
    }

    // тестируем все методы
    @Test
    public void deleteEpicByIdTest(){
        manager.deleteEpicById(2);
        Assertions.assertEquals(0, manager.getEpicList().size()); // стандартное поведение
        Assertions.assertThrows(
            NullPointerException.class,() -> {
            manager.deleteEpicById(2); // удаляем несуществующий эпик. Это же проверка на пустой список
        });

        // проверка удаления задач в истории
        Assertions.assertEquals(0, manager.getHistory().size());
    }
    @Test
    public void deleteAllEpicsTest(){
        manager.deleteAllEpics();
        Assertions.assertEquals(0, manager.getEpicList().size());

        // проверка отсутствия задач в истории
        Assertions.assertEquals(0, manager.getHistory().size());
    }
    @Test
    public void getEpicByIdTest(){
        Assertions.assertSame(testEpic, manager.getEpicById(2)); // тут только стандартное поведение. Вызов несуществуюшего - дальше

        // проверка наличия задач в истории
        Assertions.assertEquals(1, manager.getHistory().size());
    };

    @Test
    public void getEpicListTest(){
        Assertions.assertEquals(1, manager.getEpicList().size()); // стандартное поведение
        manager.deleteAllEpics();
        Assertions.assertEquals(0, manager.getEpicList().size()); // с пустым списком
        Assertions.assertNull(manager.getEpicById(100)); // вызов несуществующего эпика
    }
    @Test
    public void getAllSubtasksByEpicIdTest(){
        // стандартный кейс
        Assertions.assertEquals(1, manager.getAllSubtasksByEpicId(2).size());

        // проверка очистки списка
        manager.deleteAllSubTasks();
        Assertions.assertEquals(0, manager.getAllSubtasksByEpicId(2).size());

        // вызов по несуществующей фиче - ожидаем выброс исключения
        Assertions.assertThrows(NullPointerException.class, () -> {
            manager.getAllSubtasksByEpicId(100).size();
        });
        // проверка наличия задач в истории
        Assertions.assertEquals(1, manager.getHistory().size());
    }

    @Test
    public void updateEpicTest(){
        Epic epic = manager.getEpicById(2);
        epic.setName("Updated epic name");
        manager.updateEpic(epic);
        Assertions.assertSame(epic, manager.getEpicById(2)); // стандартный кейс
        manager.deleteAllEpics();
        final Epic nullEpic = manager.getEpicById(100); // берем несуществующий эпик
        Assertions.assertThrows(NullPointerException.class, () -> {
            manager.updateEpic(nullEpic);}); // проверяем на пустом списке
    };
    // методы Issues.SubTask
    @Test
    public void createSubTaskTest(){
        Assertions.assertEquals(1, manager.getSubtaskList().size());

        // проверяем что при привязке сабтаски к несуществующему эпику выбрасывается NullPointerException
        Assertions.assertThrows(NullPointerException.class, () -> {
        SubTask subTaskForNullEpic = manager.createSubTask(
                "Test subtask",
                "Link to null epic",
                100)
        ;});
        // проверка наличия задач в истории
        Assertions.assertEquals(1, manager.getHistory().size());
    }
    @Test
    public void getSubtasksTest(){
        manager.createSubTask("TestSubTask2", "This subtask is needed for testing get subTasks()", 2);
        HashMap<Integer, SubTask> testHashMap = manager.getSubtasks();
        Assertions.assertEquals(
                "ID=4, TYPE=SUBTASK, STATUS = NEW, PARENT=2, NAME=TestSubTask2," +
                        " DESCRIPTION=This subtask is needed for testing get subTasks()",
                manager.getSubtasks().get(4).toString());
        // проверка наличия задач в истории
        Assertions.assertEquals(1, manager.getHistory().size());
    };
    @Test
    public void getSubtaskListTest(){
        Assertions.assertEquals(testSubTask.toString(), manager.getSubtaskList().get(0).toString());

        // попутно проверка наличия задач в истории
        Assertions.assertEquals(1, manager.getHistory().size());

        manager.deleteAllSubTasks();
        Assertions.assertEquals(0, manager.getSubtasks().size());
        testSubTask = manager.createSubTask("Test subtask", "Subtask for test", 2);
        manager.deleteAllEpics();
        Assertions.assertEquals(0, manager.getSubtasks().size()); // проверяем что при удалении эпиков сабтаски тоже удаляются

        // проверка отсутствия задач в истории
        Assertions.assertEquals(0, manager.getHistory().size());
    }
    @Test
    public void setSubTaskStatusTest(){
        manager.setSubTaskStatus(testSubTask, StatusList.DONE);
        Assertions.assertEquals(StatusList.DONE, testSubTask.getStatus());
        // проверка наличия задач в истории
        Assertions.assertEquals(1, manager.getHistory().size());
    }
    @Test
    public void linkSubTaskTest(){
        manager.createEpic(
                "Epic ID 4",
                "Here the subtask should be linked");
        manager.createSubTask(
                "Subtask for testing linkSubTask()",
                "This subtask should be linked to epic ID=5",
                2);
        Assertions.assertEquals(0, manager.getEpicById(4).getSubTasks().size()); // эпик до привязки сабтаски
        manager.linkSubTask(manager.getEpicById(4), manager.getSubTaskById(5));
        Assertions.assertEquals(1, manager.getEpicById(4).getSubTasks().size()); // эпик после привязки сабтаски

        // пробуем привязать сабтаску к несуществующему эпику ID=100, должны получить NullPointerException
        Assertions.assertThrows(NullPointerException.class, () -> {
            manager.linkSubTask(manager.getEpicById(100), manager.getSubTaskById(5));
        });
    }
    @Test
    public void getSubTaskByIdTest(){
        // стандартный кейс
        Assertions.assertEquals(
                manager.getSubtaskList().get(0).toString(),
                manager.getSubTaskById(3).toString());

        // обращение к несуществующей subTask
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

        // сценарий с пустым списком. Берем сохраненную ранее сабтаску и пробуем заапдейтить ее в пустой список
        manager.deleteAllSubTasks(); // удаляем все сабтаски. subTaskList size = 0
        manager.updateSubTask(subTaskForTest); // апдейт сабтаски, сохраненной ранее
        Assertions.assertNotNull(manager.getSubTaskById(subTaskForTest.getId())); // ожидаемое поведение - update происходит штатно

        // удаляем эпики. Ожидаемое поведение - updateSubTask не происходит на пустых списках subTaskList & epicList
        manager.deleteAllEpics();
        Assertions.assertEquals(0, manager.getEpicList().size());
        Assertions.assertThrows(NullPointerException.class, () -> {
            manager.updateSubTask(subTaskForTest);
        });

    };
    @Test
    public void deleteSubTaskByIdTest(){
        // прямой сценарий
        manager.deleteSubTaskById(testSubTask.getId());
        Assertions.assertEquals(0, manager.getSubtaskList().size());

        // удаление несуществующей сабтаски. Ожидание - NullPointerEx. Это же поведение будет при пустом списке
        Assertions.assertThrows(NullPointerException.class, () -> {
            manager.deleteSubTaskById(100);
        });
        /* проверка отсутствия задач в истории. При обращении к удалению сабтаски, вызывается в том числе
         и материанский эпик, потому он остается в истории. Проверяем что там только он */
        Assertions.assertEquals(
                "[ID=2, TYPE=EPIC, STATUS=NEW, NAME=TestEpic1, DESCRIPTION=Epic ID should be = 2]",
                manager.getHistory().toString());
    };
    @Test
    public void deleteAllSubTasksTest(){
        // прямой кейс
        manager.deleteAllSubTasks();
        Assertions.assertEquals(0, manager.getSubtaskList().size());

        // повтор удаления на пустом списке. Ожидаемый результат - тот же что и прямом сценарии
        manager.deleteAllSubTasks();
        Assertions.assertEquals(0, manager.getSubtaskList().size());

        // тут та же логика что и при удалении таски, так как массовое удаление удаляет все, по одной
        Assertions.assertEquals(
                "[ID=2, TYPE=EPIC, STATUS=NEW, NAME=TestEpic1, DESCRIPTION=Epic ID should be = 2]",
                manager.getHistory().toString());
    }
    @Test
    public void createTaskTest(){
        manager.createTask("Test task2", "Issue id should be 4");
        Assertions.assertEquals(2, manager.getTaskList().size());

        // проверка наличия задач в истории
        Assertions.assertEquals(1, manager.getHistory().size());
    }
    @Test
    public void updateTaskTest(){
        // прямой кейс
        testTask.setName("New name for test task");
        manager.updateTask(testTask);
        Assertions.assertEquals("New name for test task", manager.getTaskById(1).getName());

        // попытка апдейта удаленной таски
        Task deletedTask = testTask;
        manager.deleteAllTasks();
        manager.updateTask(deletedTask);
        Assertions.assertSame(deletedTask, manager.getTaskById(deletedTask.getId())); // проверяем что апдейт прошел
        Assertions.assertNotNull(manager.getTaskById(deletedTask.getId())); // проверяем что получаемый объект != null
    }
    @Test
    public void setTaskStatus(){
        // прямой кейс
        testTask.setStatus(StatusList.IN_PROGRESS);
        manager.updateTask(testTask);
        Assertions.assertEquals(StatusList.IN_PROGRESS, manager.getTaskById(testTask.getId()).getStatus());

        // попытка изменить статус несуществующей таске должна вызвать NullPointEx
        Assertions.assertThrows(NullPointerException.class, ()->{
            manager.setTaskStatus(manager.getTaskById(100), StatusList.DONE);
        });
    };
    @Test
    public void deleteAllTasksTest(){
        // Основной кейс
        manager.deleteAllTasks();
        Assertions.assertEquals(0, manager.getTaskList().size());

        // Дублируем на пустом списке, ожидаемый результат - как и при основном
        manager.deleteAllTasks();
        Assertions.assertEquals(0, manager.getTaskList().size());
    }
    @Test
    public void getTaskByIdTest(){
        // Прямой кейс
        Assertions.assertSame(testTask, manager.getTaskById(1));

        // Запрос несуществующей таски
        Assertions.assertNull(manager.getTaskById(100));
    };
    @Test
    public void deleteTaskByIdTest(){
        // Основной кейс
        manager.deleteTaskById(1);
        Assertions.assertEquals(0, manager.getTaskList().size());

        // Попытка удалить несуществующую задачу
        manager.deleteTaskById(1);
        Assertions.assertEquals(0, manager.getTaskList().size());
    };
    @Test
    public void getTaskListTest(){
        // Прямой кейс
        Assertions.assertEquals(1, manager.getTaskList().size());

        // Попытка обращения к пустому списку. Ожидаемый результат - taskList.size() = 0
        manager.deleteAllTasks();
        //Assertions.assertEquals(0, manager.getTaskList().size());
        Assertions.assertEquals(0, manager.getTaskList().size());
    };

    @Test
    public void getHistoryTest(){
        // прямой кейс
        Assertions.assertEquals(
                "[ID=2, TYPE=EPIC, STATUS=NEW, NAME=TestEpic1, DESCRIPTION=Epic ID should be = 2]",
                manager.getHistory().toString());
    }

    //todo create save() method test
}
