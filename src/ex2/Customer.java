package ex2;

import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Producer: shops for a random time, then joins the checkout queue.
 */
public class Customer implements Runnable {
    private static final SecureRandom generator = new SecureRandom();

    private final int id;
    private final CheckoutQueue queue;
    private final int minShopMs, maxShopMs;
    private final AtomicLong customersQueued;

    public Customer(int id, CheckoutQueue queue,
                    int minShopMs, int maxShopMs,
                    AtomicLong customersQueued) {
        this.id = id;
        this.queue = queue;
        this.minShopMs = minShopMs;
        this.maxShopMs = maxShopMs;
        this.customersQueued = customersQueued;
    }

    @Override
    public void run() {
        try {
            // 1. Shop for a random time
            int shopTime = minShopMs + generator.nextInt(maxShopMs - minShopMs + 1);
            Thread.sleep(shopTime);

            // 2. Join queue (blocks if full)
            long enqueueTime = System.currentTimeMillis();
            CheckoutQueueTimes.recordEnqueue(id, enqueueTime);
            queue.joinQueue(id);
            customersQueued.incrementAndGet();

            System.out.printf("[Customer %d] shopped %dms, joined queue%n", id, shopTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}