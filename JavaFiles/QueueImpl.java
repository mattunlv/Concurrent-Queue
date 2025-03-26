public interface QueueImpl 
{
    public void enqueue(TestQueue Q, Integer value);

    public Integer dequeue(TestQueue Q);

    public String getImplName();
}
