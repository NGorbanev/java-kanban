package Issues;
import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subTasksList = new ArrayList<>();

    public Epic(String name, String description, StatusList status, int id){
        super(name, description, status, id);
    }
    public Epic(){}

    public void setName(String newName){
        super.setName(newName);
    }

    public StatusList getStatus(){
        StatusList currentStatus = super.getStatus();
        return currentStatus;
    }

    @Override
    public void setStatus(StatusList newStatus) {
        super.setStatus(newStatus);
    }

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

    public ArrayList<Integer> getSubTasks(){
        ArrayList<Integer> list = subTasksList;
        return list;
    }
}
