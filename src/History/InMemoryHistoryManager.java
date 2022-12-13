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
    //final int maxHistoryStorage = 10; // максимальное количество issue в истории

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
    
    public class CustomLinkedList<T>{
        private Node head;
        private Node tail;
        private HashMap<Integer, Node> historyMap = new HashMap<>();

        public int getSize() {
            return historyMap.size();
        }

        public void add(Task issue){
           // перенес логику добавления в конец и удаления повтора сюда
            if (historyMap.containsKey(issue.getId())) {
                remove(issue.getId());
                historyMap.put(issue.getId(), linkLast(issue));
            }
            else {
                historyMap.put(issue.getId(), linkLast(issue)); // после всех проверок добавляем запись в историю, если надо
            }


        }

        public void insert(Task issue){ // хотя метод больше и не нужен, оставлю его на будущее, на всякий случай
            Node insertedNode = historyMap.get(issue.getId());
            insertedNode.data = issue;
            historyMap.put(issue.getId(),insertedNode);
        }

        public void remove(int issueId){
            removeNode(historyMap.remove(issueId));
        }

        public void removeNode(Node node){
            if (node.prev == null){
                head = node.next;
            } else {
                node.prev.next = node.next;
            }
            if (node.next == null){
                tail = node.prev;
            } else {
                node.next.prev = node.prev;
            }
            node = null;
        }

        public Node linkLast(Task item){
            final Node oldTail = tail;
            final Node newTail = new Node(tail, item, null);
            tail = newTail;
            if (oldTail == null) head = newTail; // проверяем не пустой ли был список
            else oldTail.next = newTail; // если не пустой был - регистрируем новый хвост в бывшем старом
            return newTail; // для проверки, ну и на всякий случай, если надо будет получить "ок" о том, что объект добавлен
        }

        public ArrayList<T> getTasks(){ // по заданию надо переложть в ArrayList. Перекладываем
            ArrayList<T> tList = new ArrayList<>();
            for (Node x = head; x != null; x = x.next){
                tList.add((T) x.data);
            }
            return tList;
        }
    }
    @Override
    public void add(Task issue){
            linkedList.add(issue); // переписал в соответствии с комментариями на ревью, при этом логику перенес в класс CustomLinkedList. Так показалось логичнее в итоге
    }

    @Override
    public void remove(int issueId){
        linkedList.remove(issueId);
    }

    @Override
    public List<Task> getHistory() {
        return linkedList.getTasks(); // по заданию надо переложить в ArrayList. Оставил перекладчик в классе, чтобы если надо будет выдавать в другом формаате - проще было менять
    }


}
