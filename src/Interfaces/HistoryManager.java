package Interfaces;

import Issues.Task;

import java.util.ArrayList;

public interface HistoryManager {

    public void addToHistory(Task issue);
    public ArrayList<Task> getHistory() ;

}
