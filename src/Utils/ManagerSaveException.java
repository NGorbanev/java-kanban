package Utils;

public class ManagerSaveException extends RuntimeException {

    public ManagerSaveException(){
        super();
    }
    public ManagerSaveException(String str){
        super(str);
    }
    public ManagerSaveException(Throwable e){
        super(e);
    }
    public ManagerSaveException(Throwable e, String message) {
        super(message, e);
    }
}
