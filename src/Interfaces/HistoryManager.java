package Interfaces;

import Issues.Task;

import java.util.List;

public interface HistoryManager {

    public void add(Task issue);
    public void remove(int i);
    public List<Task> getHistory() ;

}
