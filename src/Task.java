public class Task {
    TaskManager taskManager = new TaskManager();
    // публичные поля
    public String name;
    public String description;
    public int status = 0;

    // приватные поля
    private int id;
    public String[] statusList = new String[] {"NEW", "IN_PROGRESS", "DONE"};


    // конструктор
    public Task (String name, String description){
        this.name = name;
        this.description = description;
        setId();
        taskManager.submitTask(this);
    }

    //  Дефолтный конструктор
    public Task() { }

    // методы класса
    public int getId(){
        return id;
    }
    public void setId() {
        id = taskManager.generateId();
    }
    public String getStatus() {
        return statusList[status];
    }
    public boolean setStatus(String newStatus){
        boolean statusFound = false;
        for (int i = 0; i < statusList.length; i++) {
            if (newStatus.equals(statusList[i])){
                statusFound = true;
                status = i;
                taskManager.updateTask(this);
                return true;
            }
        }
        return false;
    }

}
