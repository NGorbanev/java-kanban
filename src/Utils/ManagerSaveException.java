package Utils;

import java.io.IOException;

public class ManagerSaveException extends RuntimeException {

    public ManagerSaveException(){

    }
    public ManagerSaveException(String str){
        super(str);
        System.out.println(str);
    }
    public ManagerSaveException(IOException e, String message) {
        super(message);
    }
    public ManagerSaveException(Throwable e, String message) {
        super(e);
    }
    public ManagerSaveException(NumberFormatException e, String message){
        super(message);
    }
}
