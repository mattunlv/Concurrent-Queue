import java.util.concurrent.atomic.AtomicReference;

/**
 * CSPFriendlyPaperQueue.java
 * 
 * This class implements a thread-safe queue based on the algorithm described 
 * in a Michael and Scott's paper. It provides methods for enqueueing and dequeueing 
 * elements in a non-blocking, lock-free manner using atomic operations.
 * 
 * The queue uses atomic references to ensure thread safety and consistency 
 * while minimizing contention. It is designed for efficient concurrent access, 
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
 * - java.util.concurrent.atomic.AtomicReference: Used for atomic operations.
 * - TestQueue: Represents the queue structure.
 * - Node: Represents a node in the queue.
 * 
 * Author: Kevin Chalmers
 * Date: 24 March 2025
 */

public class CSPFriendlyPaperQueue implements QueueImpl
{
    @Override
    public void enqueue(TestQueue Q, Integer value) 
    {
        AtomicReference<Node> tmp_node;
        AtomicReference<Integer> tmp_int;
        // node = new node
        Node node = new Node();
        // node->value = value
        tmp_int = node.value;
        tmp_int.set(value);
        // node->next.ptr = null
        tmp_node = node.next;
        tmp_node.set(null);
        Node tail;
        Node next;
        while (true)
        {
            // tail = Q->tail
            tail = Q.tail.get();
            // next = tail.ptr->next
            tmp_node = tail.next;
            next = tmp_node.get();
            // if tail == Q->tail
            Node tmp = Q.tail.get();
            if (tail == tmp)
            {
                // if next.ptr == null
                if (next == null)
                {
                    // if CAS(&tail.ptr->next, next, node)
                    tmp_node = tail.next;
                    boolean succ = tmp_node.compareAndSet(next, node);
                    if (succ)
                    {
                        // CAS(&Q->tail, tail, node)
                        Q.tail.compareAndSet(tail, node);
                        break;
                    }
                    else
                    {
                        // loop
                        continue;
                    }
                }
                else
                {
                    // if CAS(Q->tail, tail, next.ptr)
                    Q.tail.compareAndSet(tail, next);
                    // loop
                    continue;
                }
            }
            else
            {
                // loop
                continue;
            }
        }
    }

    @Override
    public Integer dequeue(TestQueue Q) 
    {
        while (true)
        {
            // head = Q->head
            Node head = Q.head.get();
            // tail = Q->tail
            Node tail = Q.tail.get();
            // next = head->next
            AtomicReference<Node> tmp = head.next;
            Node next = tmp.get();
            // if head == Q->head
            Node tmp_node = Q.head.get();
            if (head == tmp_node)
            {
                // if head.ptr == tail.ptr
                if (head == tail)
                {
                    // if next.ptr == null
                    if (next == null)
                        return null;
                    else
                    {
                        // CAS(&Q->Tail, tail, next.ptr)
                        Q.tail.compareAndSet(tail, next);
                    }
                }
                else
                {
                    // *pvalue = next.ptr->value
                    AtomicReference<Integer> tmp_int = next.value;
                    Integer value = tmp_int.get();
                    // if CAS(&Q->Head, head, next_ptr)
                    boolean success = Q.head.compareAndSet(head, next);
                    if (success)
                    {
                        // break
                        return value;
                    }
                    else
                    {
                        // loop
                        continue;
                    }
                }
            }
            else
            {
                // loop
                continue;
            }
        }
    }

    @Override
    public String getImplName() 
    {
        return "CSPFriendlyPaperQueue";
    }

}
