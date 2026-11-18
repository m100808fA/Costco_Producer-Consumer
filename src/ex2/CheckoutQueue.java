package ex2;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Thread-safe FIFO queue of customer IDs (the shared "buffer").
 * Wraps ArrayBlockingQueue, which provides put()/take() blocking semantics
 * identical to BlockingBuffer.blockingPut()/blockingGet().
 */
public class CheckoutQueue {
    private final BlockingQueue<Integer> queue;

    public CheckoutQueue(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    /** Producer side: blocks if the queue is full. */
    public void joinQueue(int customerId) throws InterruptedException {
        queue.put(customerId);
    }

    /** Consumer side: blocks if the queue is empty. */
    public int nextCustomer() throws InterruptedException {
        return queue.take();
    }

    public int size()              { return queue.size(); }
    public int capacity()          { return queue.size() + queue.remainingCapacity(); }
    public int remainingCapacity() { return queue.remainingCapacity(); }
}