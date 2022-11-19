package Interfaces;

import Issues.Task;

import java.util.ArrayList;

public interface HistoryManager {

    static ArrayList<Task> history = new ArrayList<>();
    final int maxHistoryStorage = 10; // максимальное количество issue в истории

    public void addToHistory(Task issue);
    public ArrayList<Task> getHistory() ;

}
