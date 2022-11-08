package Issues;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subTasksList = new ArrayList<>();

    public void addSubTaskToEpic(SubTask subTask){
        if(!subTasksList.contains(subTask.getId())){
            subTasksList.add(subTask.getId());
        }
    }

    public void unlinkSubtask(int id){
        for (int i = 0; i <= subTasksList.size(); i++){
            if(subTasksList.get(i) == id){
                subTasksList.remove(i);
                break;
            }
        }
    }

    public ArrayList getSubTasks(){
        return subTasksList;
    }
}
