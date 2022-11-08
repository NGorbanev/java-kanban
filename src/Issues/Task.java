package Issues;

public class Task {

    // публичные поля
    public String name;
    public String description;
    public int status = 0;

    // приватные поля
    private int id;
    public String[] statusList = new String[] {"NEW", "IN_PROGRESS", "DONE"};

    // методы класса
    public int getId(){
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return statusList[status];
    }

    public String[] getStatusList(){
        return statusList;
    }

}
