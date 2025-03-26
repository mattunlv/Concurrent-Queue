import java.util.concurrent.atomic.AtomicReference;

public class Node
{
    public AtomicReference<Integer> value = new AtomicReference<Integer>(null);
    public AtomicReference<Node> next = new AtomicReference<Node>(null);
}