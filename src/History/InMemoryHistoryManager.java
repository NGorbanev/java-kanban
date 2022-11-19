package History;


import Interfaces.HistoryManager;
import Issues.Task;
import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {

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
