package Issues;

import java.time.*;

public class Task {

    private String name;
    private String description;
    //private int status = 0;
    private StatusList status = StatusList.NEW;
    private int id;
    private Instant startTime;
    private Long duration;


    // bunch of constructors for all possible needed cases
    public Task(String name, String description, StatusList status, int id){
        this.name = name;
        this.description = description;
        this.status = status;
        this.id = id;
    }
    public Task(String name, String description, StatusList status, int id, LocalDateTime dateTime, int dur){
        this.name = name;
        this.description = description;
        this.status = status;
        this.id = id;
        this.startTime = dateTime.toInstant(ZoneOffset.of("Europe/Moscow"));
        this.duration = Long.valueOf(dur * 60_000);
    }
    public Task(String name, String description, StatusList status, int id, Long UTCDate, int dur){
        this.name = name;
        this.description = description;
        this.status = status;
        this.id = id;
        this.startTime = Instant.ofEpochMilli(UTCDate);
        this.duration = Long.valueOf(dur * 60_000);
    }
    public Task(String name, String description, StatusList status, int id, Instant startTime, int dur){
        this.name = name;
        this.description = description;
        this.status = status;
        this.id = id;
        this.startTime = startTime;
        this.duration = Long.valueOf(dur * 60_000);
    }
    public Task(){}


    // methods of the class
    public String getDescription(){
        String desc = this.description;
        return desc;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public String getName(){
        String name = this.name;
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getId(){
        int id = this.id;
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public StatusList getStatus() {
        return status;
    }

    public void setStatus(StatusList newStatus){
        status = newStatus;
    }

    public StatusList[] getStatusList(){
        return StatusList.values();
    }

    @Override
    public String toString() {
        String str = "ID=" + getId() + ", " +
                "TYPE=" + IssueTypes.TASK + "," +
                "STATUS=" + getStatus() + ", " +
                "NAME=" + getName() + ", " +
                "DESCRIPTION=" + getDescription();
        return str;
    }

    // methods for working with time and duration
    public void setStartTime(Instant startTime){
        this.startTime = startTime;
    }

    public Instant getStartTime(){
        return this.startTime;
    }

    public void setDuration(int duration) {
        this.duration = Long.valueOf(duration * 60_000); // recevie minutes, convert to mills, calculate duration
    }

    public Long getDuration(){
        return this.duration;
    }

    public Instant getEndTime(){
        return Instant.ofEpochMilli(startTime.plusMillis(this.getDuration()).toEpochMilli());
    }
}
