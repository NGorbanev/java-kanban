package Issues;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subTasksList = new ArrayList<>();

    // Epic duration comes from Task class. Duration calculation is implemented at InMemoryTaskManager with calculateEpicDuration method
    // bunch of constructors for all possible needed cases
    public Epic(String name, String description){
        super(name, description);
    }

    public Epic(String name, String description, StatusList status, int id){
        super(name, description, status, id);
    }
    public Epic(String name, String description, StatusList status, int id, LocalDateTime dateTime, int dur){
        super(name, description,status,id,dateTime,dur);
    }
    public Epic(String name, String description, StatusList status, int id, Long UTCDate, int dur){
        super(name,description,status,id,UTCDate,dur);
    }
    public Epic (String name, String description, StatusList status, int id, Instant startTime, int dur){
        super(name, description, status, id, startTime, dur);
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

    @Override
    public String toString() {
        String str = "ID=" + getId() + ", " +
                "TYPE=" + IssueTypes.EPIC + ", " +
                "STATUS=" + getStatus() + ", " +
                "NAME=" + getName() + ", " +
                "DESCRIPTION=" + getDescription() + ", " +
                "START_TIME=" + getStartTime() + ", " +
                "DURATION=" + getDuration() + ", " +
                "END_TIME=" + getEndTime();
        return str;
    }
}
