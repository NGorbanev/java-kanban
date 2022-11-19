package Issues;

public class Task {

    private String name;
    private String description;
    //private int status = 0;
    private StatusList status = StatusList.NEW;
    private int id;
    //public static String[] statusList = new String[] {"NEW", "IN_PROGRESS", "DONE"};



    public Task(String name, String description, StatusList status, int id){
        this.name = name;
        this.description = description;
        this.status = status;
        this.id = id;
    }

    public Task(){}


    // методы класса
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
        //return statusList[status];
        return status;
    }

    public void setStatus(StatusList newStatus){
        status = newStatus;
    }

    public StatusList[] getStatusList(){
        return StatusList.values();
    }

}
