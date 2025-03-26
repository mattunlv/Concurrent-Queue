import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;

class Producer extends Thread
{
    TestQueue queue;
    QueueImpl impl;

    Producer(TestQueue queue, QueueImpl impl)
    {
        this.queue = queue;
        this.impl = impl;
    }

    public void run()
    {
        for (int i = 0; i < 10000; ++i)
        {
            impl.enqueue(queue, i);
        }
    }
}

class Consumer extends Thread
{
    TestQueue queue;
    QueueImpl impl;

    Consumer(TestQueue queue, QueueImpl impl)
    {
        this.queue = queue;
        this.impl = impl;
    }

    public void run()
    {
        for (int i = 0; i < 10000; ++i)
        {
            Integer n = null;
            while (n == null)
            {
                n = impl.dequeue(queue);
            }
            if (n.intValue() != i)
            {
                System.out.println("Error");
                System.out.println(i);
                break;
            }
        }
    }
}

class Worker extends Thread
{
    TestQueue queue;
    QueueImpl impl;
    int n;

    Worker(TestQueue queue, QueueImpl impl, int n)
    {
        this.queue = queue;
        this.impl = impl;
        this.n = n;
    }

    public void run()
    {
        for (int i = 0; i < 10000; ++i)
            impl.enqueue(queue, n + i);
    }
}

class Merger extends Thread
{
    TestQueue queue;
    QueueImpl impl;
    ConcurrentLinkedQueue<Integer> merge;

    Merger(TestQueue queue, QueueImpl impl, ConcurrentLinkedQueue<Integer> merge)
    {
        this.queue = queue;
        this.impl = impl;
        this.merge = merge;
    }

    public void run()
    {
        for (int i = 0; i < 10000; ++i)
            merge.add(impl.dequeue(queue));
    }
}

public class RunQueueTests 
{
    public static void main(String[] args)
    {
        QueueImpl[] impls = new QueueImpl[4];
        impls[0] = new StructuredPaperQueue();
        impls[1] = new CSPFriendlyPaperQueue();
        impls[2] = new StructuredJDKQueue();
        impls[3] = new CSPFriendlyJDKQueue();
        for (QueueImpl impl : impls)
        {
            test1(impl);
            test2(impl);
            test3(impl);
        }
    }

    public static void test1(QueueImpl impl)
    {
        System.out.println(impl.getImplName());
        System.out.println("Test 1");
        TestQueue queue = new TestQueue();
        try
        {
            Producer p = new Producer(queue, impl);
            Consumer c = new Consumer(queue, impl);
            p.start();
            c.start();
            p.join();
            c.join();
        }
        catch (InterruptedException e)
        {
            System.out.println("Error" + e.getMessage());
        }
    }

    public static void test2(QueueImpl impl)
    {
        System.out.println(impl.getImplName());
        System.out.println("Test 2");
        TestQueue queue = new TestQueue();
        try
        {
            Worker[] workers = new Worker[10];
            for (int i = 0; i < 10; ++i)
            {
                workers[i] = new Worker(queue, impl, i * 10000);
                workers[i].start();
            }
            for (int i = 0; i < 10; ++i)
            {
                workers[i].join();
            }
            ArrayList<Integer> numbers = new ArrayList<Integer>(10 * 10000);
            for (int i = 0; i < 10 * 10000; ++i)
            {
                numbers.add(impl.dequeue(queue));
            }
            Collections.sort(numbers);
            for (int i = 0; i < 10 * 10000; ++i)
            {
                if (i != numbers.get(i))
                {
                    System.out.println("Error");
                    System.out.println(Integer.toString(i) + " " + Integer.toString(numbers.get(i)));
                    break;
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Error" + e.getMessage());
        }
    }

    public static void test3(QueueImpl impl)
    {
        System.out.println(impl.getImplName());
        System.out.println("Test 3");
        TestQueue queue = new TestQueue();
        try
        {
            Worker[] workers = new Worker[10];
            for (int i = 0; i < 10; ++i)
            {
                workers[i] = new Worker(queue, impl, i * 10000);
                workers[i].start();
            }
            for (int i = 0; i < 10; ++i)
            {
                workers[i].join();
            }
            ConcurrentLinkedQueue<Integer> merge = new ConcurrentLinkedQueue<Integer>();
            Merger[] mergers = new Merger[10];
            for (int i = 0; i < 10; ++i)
            {
                mergers[i] = new Merger(queue, impl, merge);
                mergers[i].start();
            }
            for (int i = 0; i < 10; ++i)
            {
                mergers[i].join();
            }
            ArrayList<Integer> numbers = new ArrayList<Integer>(merge);
            Collections.sort(numbers);
            for (int i = 0; i < 10 * 10000; ++i)
            {
                if (i != numbers.get(i))
                {
                    System.out.println("Error");
                    System.out.println(Integer.toString(i) + " " + Integer.toString(numbers.get(i)));
                    break;
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Error" + e.getMessage());
        }
    }
}
