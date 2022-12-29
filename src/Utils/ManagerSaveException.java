package Utils;

import java.io.IOException;

public class ManagerSaveException extends IOException {
    public ManagerSaveException(String str){
        super(str);
        System.out.println(str);
    }

    public ManagerSaveException(IOException e) {
        e.getMessage();
    }
}
