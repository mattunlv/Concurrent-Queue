import java.util.concurrent.atomic.AtomicReference;

public class TestQueue 
{
    public AtomicReference<Node> head = new AtomicReference<Node>();
    public AtomicReference<Node> tail = new AtomicReference<Node>();

    public TestQueue()
    {
        // Node = new node
        Node node = new Node();
        // node->next.ptr = null
        node.next.set(null);
        // Q->head = Q->tail = node
        this.head.set(node);
        this.tail.set(node);
    }
}
