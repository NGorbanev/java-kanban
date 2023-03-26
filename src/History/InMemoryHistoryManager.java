package History;

import Interfaces.HistoryManager;
import Issues.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * В History добавил два класса - Node и CustomLinkedList
 * Последний по заданию было предложено не делать классом, а оставить все методы прямо в History, но кажется сделать
 * отдельным классом удобнее и читабельнее
 * Внутри LinkedList данные хранятся в HashMap, где индекс = id Task, а value = Node
 * По заданию было не явно написано зачем HashMap, я решил что именно такая реализация обеспечит скорость O(1), так как
 * обращение происходит постоянно через индекс и не надо ничего искать цикллами.
 */


public class InMemoryHistoryManager implements HistoryManager {
    CustomLinkedList linkedList = new CustomLinkedList();

    class Node<T>{
        public T data;
        public Node<T> next;
        public Node<T> prev;


        public Node (Node<T> prev, T data, Node next) {
            this.prev = prev;
            this.data = data;
            this.next = next;
        }
    }
    
    private class CustomLinkedList<T>{
        private Node head;
        private Node tail;
        private HashMap<Integer, Node> historyMap = new HashMap<>();

        private void add(Task issue) {
            if (historyMap.containsKey(issue.getId())) {
                remove(issue.getId());
                historyMap.put(issue.getId(), linkLast(issue));
            } else {
                historyMap.put(issue.getId(), linkLast(issue));
            }
        }
        private void remove(int issueId){
            Node node = historyMap.get(issueId);
            removeNode(node);
        }

        private void removeNode(Node node){
            if (node != null) {
                if (node.prev == null) {
                    head = node.next;
                } else {
                    node.prev.next = node.next;
                }
                if (node.next == null) {
                    tail = node.prev;
                } else {
                    node.next.prev = node.prev;
                }
                node = null;
            }
        }

        private Node linkLast(Task item){
            final Node oldTail = tail;
            final Node newTail = new Node(tail, item, null);
            tail = newTail;
            if (oldTail == null) head = newTail; // check if the list is empty
            else oldTail.next = newTail; // if not empty - register new tail instead of old one
            return newTail; // added hust in case if returning new tail would be necessary
        }

        private ArrayList<T> getTasks(){
            ArrayList<T> tList = new ArrayList<>();
            for (Node x = head; x != null; x = x.next){
                tList.add((T) x.data);
            }
            return tList;
        }
    }
    @Override
    public void add(Task issue){
            linkedList.add(issue);
    }

    @Override
    public void remove(int issueId){
        linkedList.remove(issueId);
    }

    @Override
    public List<Task> getHistory() {
        return linkedList.getTasks();
    }


}
