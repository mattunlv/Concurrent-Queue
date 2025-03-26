import java.util.concurrent.atomic.AtomicReference;

/**
 * CSPFriendlyJDKQueue.java
 * 
 * This class implements a thread-safe queue designed to be compatible with 
 * Communicating Sequential Processes (CSP) principles. It provides methods 
 * for enqueueing and dequeueing elements in a non-blocking, lock-free manner 
 * using atomic operations.
 * 
 * The queue leverages atomic references to ensure consistency and thread safety 
 * while minimizing contention. It is designed to handle concurrent access 
 * efficiently, making it suitable for high-performance, multi-threaded applications.
 * 
 * Key Methods:
 * - enqueue(TestQueue Q, Integer value): Adds an element to the queue.
 * - dequeue(TestQueue Q): Removes and returns an element from the queue.
 * - getImplName(): Returns the name of the implementation.
 * 
 * Helper Method:
 * - updateHead(TestQueue Q, Node head, Node pointer): Updates the head of the queue 
 *   to maintain consistency during dequeue operations.
 * 
 * Dependencies:
 * - java.util.concurrent.atomic.AtomicReference: Used for atomic operations.
 * - TestQueue: Represents the queue structure.
 * - Node: Represents a node in the queue.
 * 
 * Author: Kevin Chalmers
 * Date: 24 March 2025
 */

public class CSPFriendlyJDKQueue implements QueueImpl
{

    @Override
    public void enqueue(TestQueue Q, Integer value) 
    {
        Node node = new Node();
        AtomicReference<Integer> tmp_int = node.value;
        tmp_int.set(value);
        AtomicReference<Node> tmp_node = node.next;
        tmp_node.set(null);
        tmp_node = Q.tail;
        Node tail = tmp_node.get();
        Node pointer = tail;
        while (true)
        {
            tmp_node = pointer.next;
            Node next = tmp_node.get();
            if (next == null)
            {
                tmp_node = pointer.next;
                boolean success = tmp_node.compareAndSet(null, node);
                if (success)
                {
                    if (pointer != tail)
                    {
                        tmp_node = Q.tail;
                        tmp_node.compareAndSet(tail, node);
                    }
                    return;
                }
            }
            else if (pointer == next)
            {
                tmp_node = Q.tail;
                Node tmp = tail;
                tail = tmp_node.get();
                if (tmp != tail)
                {
                    pointer = tail;
                }
                else
                {
                    tmp_node = Q.head;
                    pointer = tmp_node.get();
                }
            }
            else
            {
                if (pointer != tail)
                {
                    tmp_node = Q.tail;
                    Node tmp = tail;
                    tail = tmp_node.get();
                    if (tmp != tail)
                    {
                        pointer = tail;
                    }
                    else
                    {
                        pointer = next;
                    }
                }
                else
                {
                    pointer = next;
                }
            }
        }
    }

    @Override
    public Integer dequeue(TestQueue Q) 
    {
        restartFromHead: while (true)
        {
            Node head = Q.head.get();
            Node pointer = head;
            Node next = null;
            for (;;)
            {
                AtomicReference<Node> tmp_node;
                AtomicReference<Integer> tmp_int = pointer.value;
                Integer item = tmp_int.get();
                if (item != null)
                {
                    tmp_int = pointer.value;
                    boolean success = tmp_int.compareAndSet(item, null);
                    if (success)
                    {
                        if (pointer != head)
                        {
                            tmp_node = pointer.next;
                            next = tmp_node.get();
                            if (next != null)
                                updateHead(Q, head, next);
                            else
                                updateHead(Q, head, pointer);
                        }
                        else
                        {
                            // Do nothing
                        }
                        return item;
                    }
                    else
                    {
                        tmp_node = pointer.next;
                        next = tmp_node.get();
                        if (next == null)
                        {
                            updateHead(Q, head, pointer);
                            return null;
                        }
                        else if (pointer == next)
                        {
                            pointer = next;
                            continue restartFromHead;
                        }
                        else
                        {
                            pointer = next;
                            // loop (for)
                            continue;
                        }
                    }
                }
                else
                {
                    tmp_node = pointer.next;
                    next = tmp_node.get();
                    if (next == null)
                    {
                        updateHead(Q, head, pointer);
                        return null;
                    }
                    else if (pointer == next)
                    {
                        pointer = next;
                        continue restartFromHead;
                    }
                    else
                    {
                        pointer = next;
                        // loop (for)
                        continue;
                    }
                }
            }
        }
    }

    void updateHead(TestQueue Q, Node head, Node pointer)
    {
        if (head != pointer)
        {
            boolean success = Q.head.compareAndSet(head, pointer);
            if (success)
            {
                AtomicReference<Node> tmp_node = head.next;
                tmp_node.set(head);
                return;
            }
            else
            {
                // Do nothing
                return;
            }
        }
        else
        {
            // Do nothing
            return;
        }
    }

    @Override
    public String getImplName() 
    {
        return "CSPFriendlyJDKQueue";
    }

}
