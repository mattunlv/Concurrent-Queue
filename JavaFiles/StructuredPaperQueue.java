/**
 * StructuredPaperQueue.java
 * 
 * This class implements a thread-safe queue based on the algorithm described 
 * in a Michael and Scott's paper, following structured programming principles. It provides 
 * methods for enqueueing and dequeueing elements in a non-blocking, lock-free manner 
 * using atomic operations.
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
 * 
 * Dependencies:
 * - TestQueue: Represents the queue structure.
 * - Node: Represents a node in the queue.
 * 
 * Author: Kevin Chalm
 * Date: 24 March 2025
 */

public class StructuredPaperQueue implements QueueImpl
{
    @Override
    public void enqueue(TestQueue Q, Integer value)
    {
        // node = new node
        Node node = new Node();
        // node->value = value
        node.value.set(value);
        // node->next.ptr = null
        node.next.set(null);
        Node tail;
        Node next;
        while (true)
        {
            // tail = Q->tail
            tail = Q.tail.get();
            // next = tail.ptr->next
            next = tail.next.get();
            // if tail == Q->tail
            if (tail == Q.tail.get())
            {
                // if next.ptr == null
                if (next == null)
                {
                    // if CAS(&tail.ptr->next, next, node)
                    if (tail.next.compareAndSet(next, node))
                        break;
                }
                else
                {
                    // if CAS(Q->tail, tail, next.ptr)
                    Q.tail.compareAndSet(tail, next);
                }
            }
        }
        // CAS(&Q->tail, tail, node)
        Q.tail.compareAndSet(tail, node);
    }

    @Override
    public Integer dequeue(TestQueue Q)
    {
        Integer value = null;
        while (true)
        {
            // head = Q->head
            Node head = Q.head.get();
            // tail = Q->tail
            Node tail = Q.tail.get();
            // next = head->next
            Node next = head.next.get();
            // if head == Q->head
            if (head == Q.head.get())
            {
                // if head.ptr == tail.ptr
                if (head == tail)
                {
                    // if next.ptr = null
                    if (next == null)
                        return null;
                    // CAS(&Q->Tail, tail, next.ptr)
                    Q.tail.compareAndSet(tail, next);
                }
                else
                {
                    // pvalue = next.ptr->value
                    value = next.value.get();
                    if (Q.head.compareAndSet(head, next))
                        break;
                }
            }
        }
        return value;
    }

    @Override
    public String getImplName() 
    {
        return "StructuredPaperQueue";
    }
}
