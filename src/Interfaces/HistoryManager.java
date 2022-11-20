package Interfaces;

import Issues.Task;

import java.util.List;

public interface HistoryManager {

    public void addToHistory(Task issue);
    public List<Task> getHistory() ;

}
