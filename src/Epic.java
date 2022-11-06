import java.util.ArrayList;

public class Epic extends Task {
    TaskManager taskManager = new TaskManager();
    private ArrayList<Integer> subTasksList = new ArrayList<>();
    Epic(){ }; // дефолтный контструктор
    Epic(String name, String description){
        this.name = name;
        this.description = description;
        this.setId();
        taskManager.submitEpic(this);
    }
    public void linkSubTask(int subTaskId){
        this.subTasksList.add(subTaskId);
        SubTask newChildSubtask = taskManager.getSubTaskById(subTaskId);
        newChildSubtask.setParentEpic(this.getId());
        taskManager.updateSubTask(newChildSubtask);
    }

    public void unlinkSubtask(int id){
        for (int i = 0; i <= subTasksList.size(); i++){
            if(subTasksList.get(i) == id){
                subTasksList.remove(i);
                break;
            }
        }
    }

    public ArrayList showSubTasks(){
        return subTasksList;
    }

    // метод расчета статус эпика, в зависимости от статуса подзадач
    public void checkStatus(){
        int statusCounter = subTasksList.size();
        boolean isInProgress = false;
        boolean isDONE = false;
        if (statusCounter > 0) {
            for (int index : subTasksList) {
                switch (taskManager.getSubtaskList().get(index).getStatus()){
                    case ("IN_PROGRESS"): {
                        isInProgress = true;
                        break;
                    }
                    case ("DONE"): {
                        statusCounter--;
                        isInProgress = true;
                    }
                }
            }
            if (statusCounter <= 0) isDONE = true;
        }
        if (!isDONE && isInProgress) setStatus("IN_PROGRESS");
        else if (isDONE) setStatus("DONE");
        else setStatus("NEW");
    }
}
