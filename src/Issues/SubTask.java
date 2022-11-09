package Issues;

public class SubTask extends Task {

    private int epicId;

    public SubTask(String name, String description, int status, int parentEpicId, int id){
        super(name, description, status, id);
        this.epicId = parentEpicId;
    }

    @Override
    public String getStatus() {
        return super.getStatus();
    }

    public void setStatus(int newStatus){
        super.setStatus(newStatus);
    }

    public Integer getParentEpicId(){
        return epicId;
    }

    public void setParentEpic(int parentEpic){
        this.epicId = parentEpic;
    }



}
