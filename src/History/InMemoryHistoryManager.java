package History;


import Interfaces.HistoryManager;
import Issues.Task;
import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    final int maxHistoryStorage = 10; // максимальное количество issue в истории
    static ArrayList<Task> history = new ArrayList<>();
    @Override
    public void addToHistory(Task issue){
        if (history.size() >= maxHistoryStorage){
            history.remove(0);
            history.add(issue);
        } else history.add(issue);
    }

    @Override
    public ArrayList<Task> getHistory() {
        return history;
    }
}
