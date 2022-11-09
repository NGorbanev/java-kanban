package Issues;

public class Task {

    private String name;
    private String description;
    private int status = 0;

    private int id;
    public static String[] statusList = new String[] {"NEW", "IN_PROGRESS", "DONE"};

    public Task(String name, String description, int status, int id){
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

    public String getStatus() {
        return statusList[status];
    }

    public void setStatus(int newStatus){
        if (newStatus >= 0 && newStatus < statusList.length) {
            this.status = newStatus;
        }
    }

    public String[] getStatusList(){
        return statusList;
    }

}
