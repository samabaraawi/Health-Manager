package ds;

public class CircularDoublyLinkedList<T> {

    private Node<T> head;              // points to the first node (null if empty)
    private Node<T> tail;              // points to the last node  (null if empty)
    private int size;                  // number of elements currently in the list

    public CircularDoublyLinkedList() {
        head = tail = null;            // list starts empty (no head/tail)
        size = 0;                      // size is zero initially
    }

    public boolean isEmpty() {
        return size == 0;              // list is empty if size is zero
    }

    public int getSize() {
        return size;                   // return how many elements exist
    }



    // Enable closer access: if index <= size/2, traverse from head; otherwise, start from tail
    // Time: O(n) in the worst case (but practically about twice as fast) | Space: O(1)
    private Node<T> nodeAt(int index) {
        if (index < 0 || index >= size) {
            throw new IllegalArgumentException("Out of index");
        }
        // choose nearer side and walk inline (no extra memory)
         if (index <= size / 2) {
            // forward from head
            Node<T> cur = head;
            for (int i = 0; i < index; i++) cur = cur.getNext();
            return cur;
        } else {
            // backward from tail
            Node<T> cur = tail;
            for (int i = size - 1; i > index; i--) cur = cur.getPrev();
            return cur;
        }
    }

    // Add element at the beginning (as new head)
    // Time: O(1) , Space: O(1)
    public void addFirst(T data) {
        Node<T> n = new Node<>(data);  // create a new node with the given data
        if (isEmpty()) {               // case: the list is currently empty
            head = tail = n;           // head and tail both point to the new node
            n.setNext(n);              // circular link: next points to itself
            n.setPrev(n);              // circular link: prev points to itself
        } else {                       // case: the list has at least one node
            n.setNext(head);           // new node's next becomes the old head
            n.setPrev(tail);           // new node's prev becomes the old tail
            head.setPrev(n);           // old head's prev now points to the new node
            tail.setNext(n);           // old tail's next now points to the new node
            head = n;                  // update head to be the new node
        }
        size++;                        // increase size after successful insert
    }



    // Add element at the end (as new tail)
    // Time: O(1) , Space: O(1)
    public void addLast(T data) {
        Node<T> n = new Node<>(data);  // create a new node with the given data
        if (isEmpty()) {               // same handling as addFirst when empty
            head = tail = n;           // head and tail both point to the new node
            n.setNext(n);              // make it circular to itself
            n.setPrev(n);
        } else {                       // list has elements
            n.setPrev(tail);           // new node comes after the current tail
            n.setNext(head);           // and before the head (circular)
            tail.setNext(n);           // old tail points forward to the new node
            head.setPrev(n);           // head points backward to the new node
            tail = n;                  // update tail to be the new node
        }
        size++;                        // reflect the addition
    }



    // Add element at a specific index
    // Time: O(n) ,Space: O(1)
    public void add(T data, int index) {
        if (index < 0 || index > size) {              // validate bounds
            throw new IllegalArgumentException("Out of index");
        }
        if (index == 0) {                              // inserting at the front
            addFirst(data);                            // reuse addFirst
            return;
        }
        if (index == size) {                           // inserting at the back
            addLast(data);                             // reuse addLast
            return;
        }
        Node<T> prev = nodeAt(index - 1);              // node just before target position
        Node<T> nxt  = prev.getNext();                 // node that will come after the new node

        Node<T> n = new Node<>(data);                  // create the new node
        n.setNext(nxt);                                // link new node forward
        n.setPrev(prev);                               // link new node backward
        prev.setNext(n);                               // link previous to new node
        nxt.setPrev(n);                                // link next to new node
        size++;                                        // update size
    }



    // Remove the first element (head)
    // Time: O(1) , Space: O(1)
    public boolean removeFirst() {
        if (isEmpty()) {                 // nothing to remove
            return false;                // report failure
        }
        if (size == 1) {                 // only one node in the list
            head = tail = null;          // list becomes empty
        } else {                         // more than one node
            Node<T> newHead = head.getNext(); // second node becomes the new head
            newHead.setPrev(tail);       // connect new head back to tail
            tail.setNext(newHead);       // connect tail forward to new head
            head.setNext(null);          // detach old head (help GC)
            head.setPrev(null);
            head = newHead;              // update head reference
        }
        size--;                          // reflect removal
        return true;
    }

    // Remove the last element (tail)
    // Time: O(1) , Space: O(1)
    public boolean removeLast() {
        if (isEmpty()) {                 // nothing to remove
            return false;                // report failure
        }
        if (size == 1) {                 // only one node
            head = tail = null;          // becomes empty
        } else {                         // more than one node
            Node<T> newTail = tail.getPrev(); // second-last becomes the new tail
            newTail.setNext(head);       // connect new tail forward to head
            head.setPrev(newTail);       // connect head back to new tail
            tail.setNext(null);          // detach old tail (help GC)
            tail.setPrev(null);
            tail = newTail;              // update tail reference
        }
        size--;                          // reflect removal
        return true;
    }

    // Remove an element at a specific index
    // Time: O(n) ,Space: O(1)
    public boolean removeAt(int index) {
        if (index < 0 || index >= size) {             // validate bounds
            throw new IllegalArgumentException("Out of index");
        }
        if (index == 0) {                              // remove first
            return removeFirst();                      // reuse logic
        }
        if (index == size - 1) {                       // remove last
            return removeLast();                       // reuse logic
        }
        Node<T> curr = nodeAt(index);                  // target node
        Node<T> p = curr.getPrev();                    // node before current
        Node<T> n = curr.getNext();                    // node after current
        p.setNext(n);                                  // bypass current forward
        n.setPrev(p);                                  // bypass current backward
        curr.setNext(null);                            // detach current (help GC)
        curr.setPrev(null);
        size--;                                        // update size
        return true;
    }

    // Remove first occurrence of a value
    // Time: O(n) , Space: O(1)
    public boolean remove(T data) {
        if (isEmpty()) {                               // empty list cannot contain data
            return false;                              // report failure
        }
        // check head quickly
        if ((head.getData() == null && data == null) ||
                (head.getData() != null && head.getData().equals(data))) {
            return removeFirst();                      // if matches head, reuse removeFirst
        }
        // check tail quickly
        if ((tail.getData() == null && data == null) ||
                (tail.getData() != null && tail.getData().equals(data))) {
            return removeLast();                       // if matches tail, reuse removeLast
        }
        // search between head.next and tail.prev
        Node<T> curr = head.getNext();                 // start after head
        for (int i = 1; i < size - 1; i++) {           // iterate middle nodes only
            T val = curr.getData();                    // current value
            boolean match = (val == null && data == null) ||
                    (val != null && val.equals(data)); // equals check with null safety
            if (match) {                               // found target
                Node<T> p = curr.getPrev();            // link previous
                Node<T> n = curr.getNext();            // link next
                p.setNext(n);
                n.setPrev(p);
                curr.setNext(null);
                curr.setPrev(null);
                size--;                                // update size
                return true;
            }
            curr = curr.getNext();
        }
        return false;
    }


    // Get element at index
    // Time: O(n) ,Space: O(1)
    public T get(int index) {
        Node<T> curr = nodeAt(index);                  // start via helper (nearest side)
        return curr.getData();                         // return the value
    }

    // Set element at index to a new value , returns old value
    // Time: O(n) ,Space: O(1)
    public T set(int index, T data) {
        Node<T> curr = nodeAt(index);                  // get target node via nearest walk
        T old = curr.getData();                        // remember old value
        curr.setData(data);                            // overwrite with new value
        return old;                                    // return replaced value
    }


    // Returns true if the list contains the given value (first occurrence)
    // Time: O(n) ,Space: O(1)
    public boolean contains(T data) {
        if (isEmpty()) {                               // no elements at all
            return false;                              // cannot contain
        }
        Node<T> curr = head;                           // start at head
        for (int i = 0; i < size; i++) {
            T val = curr.getData();                    // current value
            if ((val == null && data == null) ||
                    (val != null && val.equals(data))) {
                return true;                           // found match
            }
            curr = curr.getNext();                     // move to next
        }
        return false;
    }

    // Returns index of first occurrence, or -1 if not
    // Time: O(n) , Space: O(1)
    public int indexOf(T data) {
        if (isEmpty()) {                               // quick reject
            return -1;                                 // not present
        }
        Node<T> curr = head;                           // start at head
        for (int i = 0; i < size; i++) {               // check each position once
            T val = curr.getData();                    // value at current node
            if ((val == null && data == null) ||
                    (val != null && val.equals(data))) {
                return i;                              // return position
            }
            curr = curr.getNext();                     // step ahead
        }
        return -1;                                     // value not found
    }


    // Remove all elements and detach links safely
    // Time: O(n) ,Space: O(1)
    public void clear() {
        if (!isEmpty()) {                              // only do work if not already empty
            Node<T> curr = head;                       // start from head
            for (int i = 0; i < size; i++) {           // visit each node once
                Node<T> nxt = curr.getNext();          // keep next node
                curr.setNext(null);                    // detach forward link
                curr.setPrev(null);                    // detach backward link
                curr = nxt;                            // move to next
            }
        }
        head = tail = null;                            // reset head and tail
        size = 0;                                      // reset size
    }

    // Print elements from head to tail (one line)
    // Time: O(n) , Space: O(1)
    public void displayForward() {
        if (isEmpty()) {                               // handle empty case nicely
            System.out.println("List is empty.");      // say it's empty
            return;                                    // exit early
        }
        Node<T> curr = head;                           // start at head
        for (int i = 0; i < size; i++) {               // loop exactly size times
            System.out.print(curr.getData() + " ");    // print current value with a space
            curr = curr.getNext();                     // go to next node
        }
        System.out.println();                          // end line after printing all
    }

    // Print elements from tail to head (one line)
    // Time: O(n) , Space: O(1)
    public void displayBackward() {
        if (isEmpty()) {                               // if empty, nothing to show
            System.out.println("List is empty.");
            return;
        }
        Node<T> curr = tail;                           // start at tail
        for (int i = 0; i < size; i++) {               // loop exactly size times
            System.out.print(curr.getData() + " ");    // print current value with a space
            curr = curr.getPrev();                     // move backward
        }
        System.out.println();                          // end line after printing all
    }

    // Time: O(n) , Space: O(1)
    public void iterate() {
        if (isEmpty()) {
            System.out.println("List is empty.");
            return;
        }
        Node<T> curr = head;                           // begin at head
        for (int i = 0; i < size; i++) {               // visit each node exactly once
            System.out.println(curr.getData());        // print current value (one per line)
            curr = curr.getNext();                     // step forward
        }
    }

    // Time: O(n) , Space: O(n)
    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();        // build a string efficiently
        sb.append("[");                                // opening bracket
        Node<T> curr = head;                           // start from head
        for (int i = 0; i < size; i++) {               // iterate all nodes once
            sb.append(curr.getData());                 // append current value
            if (i < size - 1) sb.append(", ");         // add comma+space between elements
            curr = curr.getNext();                     // move to next node
        }
        sb.append("]");                                // closing bracket
        return sb.toString();                          // return the final string
    }
}
