package issues;

import java.time.Instant;
import java.time.LocalDateTime;

public class SubTask extends Task {

    private int epicId;

    public SubTask(){}
    public SubTask(String name, String description, int parentEpicId){
        super(name, description);
        this.epicId = parentEpicId;
    }
    public SubTask(
            String name,
            String description,
            StatusList status,
            int parentEpicId,
            int id){
        super(name, description, status, id);
        this.epicId = parentEpicId;
    }

    public SubTask(String name,
                   String description,
                   StatusList status,
                   int parentEpicId,
                   int id,
                   LocalDateTime dateTime,
                   int dur){
        super(name, description, status, id, dateTime, dur);
        this.epicId = parentEpicId;
    }
    public SubTask(String name,
                   String description,
                   StatusList status,
                   int parentEpicId,
                   int id,
                   Long UTCDate,
                   int dur){
        super(name, description, status, id, UTCDate, dur);
        this.epicId = parentEpicId;
    }

    public SubTask(String name,
                   String description,
                   StatusList status,
                   int parentEpicId,
                   int id,
                   Instant startTime,
                   int dur){
        super(name, description, status, id, startTime, dur);
        this.epicId = parentEpicId;
    }


    @Override
    public StatusList getStatus() {
        return super.getStatus();
    }

    public void setStatus(StatusList newStatus){
        super.setStatus(newStatus);
    }

    public Integer getParentEpicId(){
        return epicId;
    }

    public void setParentEpic(int parentEpic){
        this.epicId = parentEpic;
    }

    @Override
    public String toString() {
        String str = "ID=" + getId() + ", " +
                "TYPE=" + IssueTypes.SUBTASK + ", " +
                "STATUS = " + getStatus() + ", " +
                "PARENT=" + getParentEpicId() + ", " +
                "NAME=" + getName() + ", " +
                "DESCRIPTION=" + getDescription() + ", " +
                "START_TIME=" + getStartTime() + ", " +
                "DURATION=" + getDuration() + ", " +
                "END_TIME=" + getEndTime();
        return str;
    }
}
