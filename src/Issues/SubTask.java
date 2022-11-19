package Issues;

public class SubTask extends Task {

    private int epicId;

    public SubTask(String name, String description, StatusList status, int parentEpicId, int id){
        super(name, description, status, id);
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
}
