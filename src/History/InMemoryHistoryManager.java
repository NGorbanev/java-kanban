package History;

import Interfaces.HistoryManager;
import Issues.Task;
import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    final int maxHistoryStorage = 10; // максимальное количество issue в истории
    private ArrayList<Task> history = new ArrayList<>();
    @Override
    public void addToHistory(Task issue){
        history.add(0, issue);
        if (history.size() > 10) history.remove(10);
    }

    @Override
    public List<Task> getHistory() {
        return history;
    }
}
