package api.utils;

public class QueryParser {
    /**
     * Helping class for defining issue id from http request input query
     * @param inputString
     * @return
     */

    public int getIdFromQuery(String inputString){
        int issueID = Integer.parseInt(inputString.substring(inputString.indexOf("id=")+3));
        return issueID;
    }

}
