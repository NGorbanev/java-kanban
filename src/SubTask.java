public class SubTask extends Task {
    TaskManager taskManager = new TaskManager();

    private int epicId;

    SubTask(String name, String description, Integer parentEpic){
        this.name = name;
        this.description = description;
        this.setId();
        epicId = parentEpic;
        taskManager.submitSubTask(this);
    }

    public Integer getParentEpicId(){
        return epicId;
    }

    public void setParentEpic(int parentEpic){
        this.epicId = parentEpic;
    }

    public void changeStatus(String toStatus){
        setStatus(toStatus);
        /*
        boolean statusFound = false;
        for (int i = 0; i < statusList.length; i++) {
            if (toStatus.equals(statusList[i])){
                statusFound = true;
                status = i;
            }
        }
         */
        taskManager.updateSubTask(this);
    }

}
