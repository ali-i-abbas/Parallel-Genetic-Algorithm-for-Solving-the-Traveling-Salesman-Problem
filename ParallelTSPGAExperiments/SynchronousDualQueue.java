import java.util.concurrent.atomic.AtomicReference;

// code is from Herlihy, M., & Shavit, N. (2011). The art of multiprocessor programming. Morgan Kaufmann. pp. 238-241.
public class SynchronousDualQueue<T> {

    private enum NodeType {ITEM, RESERVATION};

    private AtomicReference<SynchronousDualQueue<T>.Node> head;
    private AtomicReference<SynchronousDualQueue<T>.Node> tail;

    public SynchronousDualQueue() {
        Node sentinel = new Node(null, NodeType.ITEM);
        head = new AtomicReference<Node>(sentinel);
        tail = new AtomicReference<Node>(sentinel);
    }


    private class Node {
        volatile NodeType type;
        volatile AtomicReference<T> item;
        volatile AtomicReference<Node> next;
        
        Node(T myItem, NodeType myType) {
            item = new AtomicReference<T>(myItem);
            next = new AtomicReference<Node>(null);
            type = myType;
        }
    }

    public void enq(T e) {
        Node offer = new Node(e, NodeType.ITEM);

        while (true) {
            Node t = tail.get(), h = head.get();

            if (h == t || t.type == NodeType.ITEM) {
                Node n = t.next.get();

                if (t == tail.get()) {
                    if (n != null) {
                        tail.compareAndSet(t, n);
                    } else if (t.next.compareAndSet(n, offer)) {
                        tail.compareAndSet(t, offer);

                        // removing this section of code eliminates the blocking part of the enq()
                        
                        // while (offer.item.get() == e);

                        // h = head.get();
                        // if (offer == h.next.get()) {
                        //     head.compareAndSet(h, offer);
                        // }
                        
                        return;
                    }
                }
            } else {
                Node n = h.next.get();

                if (t != tail.get() || h != head.get() || n == null) {
                    continue;
                }

                boolean success = n.item.compareAndSet(null, e);
                head.compareAndSet(h, n);

                if (success) {
                    return;
                }                
            }
        }
    }

    public T deq(T e) { 
        Node offer = new Node(e, NodeType.RESERVATION); 

        while (true) { 
            Node t = tail.get(); 
            Node h = head.get(); 

            if (h == t || t.type == NodeType.RESERVATION) { 
                Node n = t.next.get(); 

                if (t == tail.get()) { 
                    if (n != null) { 
                        tail.compareAndSet(t, n); 
                    } else if (t.next.compareAndSet(n, offer)) { 
                        tail.compareAndSet(t, offer); 

                        while (offer.item.get() == e); 

                        h = head.get(); 
                        if (offer == h.next.get()) { 
                            head.compareAndSet(h, offer); 
                        } 

                        return offer.item.get(); 
                    } 
                } 
            } else { 
                Node n = h.next.get(); 

                if (t != tail.get() || h != head.get() || n == null || n.type != NodeType.ITEM) { 
                    continue; // inconsistent snapshot 
                } 

                T item = n.item.get(); 
                if(item == null || item.equals(e)) { 
                    // System.out.println("Can not feed self ");
                    continue; 
                } 
                boolean success = n.item.compareAndSet(item, null); 
                head.compareAndSet(h, n); 
                if (success) { 
                    return item; 
                } 
            } 
        } 
    }

    public T deq() { 
        Node offer = new Node(null, NodeType.RESERVATION); 

        while (true) { 
            Node t = tail.get(); 
            Node h = head.get(); 

            if (h == t || t.type == NodeType.RESERVATION) { 
                Node n = t.next.get(); 

                if (t == tail.get()) { 
                    if (n != null) { 
                        tail.compareAndSet(t, n); 
                    } else if (t.next.compareAndSet(n, offer)) { 
                        tail.compareAndSet(t, offer); 

                        while (offer.item.get() == null); 

                        h = head.get(); 
                        if (offer == h.next.get()) { 
                            head.compareAndSet(h, offer); 
                        } 

                        return offer.item.get(); 
                    } 
                } 
            } else { 
                Node n = h.next.get(); 

                if (t != tail.get() || h != head.get() || n == null || n.type != NodeType.ITEM) { 
                    continue; 
                } 

                T item = n.item.get(); 
                if(item == null) {
                    continue; 
                }

                boolean success = n.item.compareAndSet(item, null); 
                head.compareAndSet(h, n); 
                if (success) { 
                    return item; 
                } 
            } 
        } 
    }
}