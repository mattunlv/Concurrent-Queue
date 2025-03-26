import java.util.concurrent.atomic.AtomicReference;

/**
 * StructuredJDKQueue.java
 * 
 * This class implements a thread-safe queue designed to follow structured 
 * programming principles. It provides methods for enqueueing and dequeueing 
 * elements in a non-blocking, lock-free manner using atomic operations.
 * 
 * The queue leverages atomic references to ensure thread safety and consistency 
 * while minimizing contention. It is optimized for efficient concurrent access, 
 * making it suitable for high-performance, multi-threaded applications.
 * 
 * Key Methods:
 * - enqueue(TestQueue Q, Integer value): Adds an element to the queue using 
 *   a lock-free algorithm.
 * - dequeue(TestQueue Q): Removes and returns an element from the queue using 
 *   a lock-free algorithm.
 * - getImplName(): Returns the name of the implementation.
 * - updateHead(TestQueue Q, Node head, Node pointer): Updates the head of the 
 *   queue to maintain consistency during dequeue operations.
 * 
 * Dependencies:
 * - java.util.concurrent.atomic.AtomicReference: Used for atomic operations.
 * - TestQueue: Represents the queue structure.
 * - Node: Represents a node in the queue.
 * 
 * Author: Kevin Chalmers
 * Date: 24 March 2025
 */

public class StructuredJDKQueue implements QueueImpl
{

    @Override
    public void enqueue(TestQueue Q, Integer value) 
    {
        Node node = new Node();
        node.value.set(value);
        node.next.set(null);
        Node tail = Q.tail.get();
        Node pointer = tail;
        while (true)
        {
            Node next = pointer.next.get();
            if (next == null)
            {
                if (pointer.next.compareAndSet(null, node))
                {
                    if (pointer != tail)
                        Q.tail.compareAndSet(tail, node);
                    return;
                }
            }
            else if (pointer == next)
                pointer = (tail != (tail = Q.tail.get())) ? tail : Q.head.get();
            else
                pointer = (pointer != tail && tail != (tail = Q.tail.get())) ? tail : next;
        }
    }

    @Override
    public Integer dequeue(TestQueue Q) 
    {
        restartFromHead: while (true)
        {
            for (Node head = Q.head.get(), pointer = head, next;; pointer = next)
            {
                Integer item;
                if ((item = pointer.value.get()) != null && pointer.value.compareAndSet(item, null))
                {
                    if (pointer != head)
                        updateHead(Q, head, ((next = pointer.next.get()) != null) ? next : pointer);
                    return item;
                }
                else if ((next = pointer.next.get()) == null)
                {
                    updateHead(Q, head, pointer);
                    return null;
                }
                else if (pointer == next)
                {
                    continue restartFromHead;
                }
            }
        }
    }

    @Override
    public String getImplName() 
    {
        return "StructuredJDKQueue";
    }

    void updateHead(TestQueue Q, Node head, Node pointer)
    {
        if (head != pointer)
        {
            AtomicReference<Node> tmp_node = Q.head;
            boolean success = tmp_node.compareAndSet(head, pointer);
            if (success)
            {
                tmp_node = head.next;
                tmp_node.set(head);
            }
        }
    }
}
