package ex2;

import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Consumer: repeatedly takes a customer from the queue and serves them.
 */
public class Cashier implements Runnable {
    private static final SecureRandom generator = new SecureRandom();

    private final int id;
    private final CheckoutQueue queue;
    private final int minServiceMs, maxServiceMs;
    private final int customersToServe;   // -1 = serve until interrupted
    private final AtomicLong totalWaitMs;
    private final AtomicLong maxWaitMs;
    private final AtomicLong customersServed;

    public Cashier(int id, CheckoutQueue queue,
                   int minServiceMs, int maxServiceMs, int customersToServe,
                   AtomicLong totalWaitMs, AtomicLong maxWaitMs,
                   AtomicLong customersServed) {
        this.id = id;
        this.queue = queue;
        this.minServiceMs = minServiceMs;
        this.maxServiceMs = maxServiceMs;
        this.customersToServe = customersToServe;
        this.totalWaitMs = totalWaitMs;
        this.maxWaitMs = maxWaitMs;
        this.customersServed = customersServed;
    }

    @Override
    public void run() {
        try {
            while (customersToServe < 0 || customersServed.get() < customersToServe) {
                int customerId = queue.nextCustomer();   // blocks when empty
                long dequeueTime = System.currentTimeMillis();

                Long enqueueTime = CheckoutQueueTimes.getAndRemove(customerId);
                if (enqueueTime != null) {
                    long wait = dequeueTime - enqueueTime;
                    totalWaitMs.addAndGet(wait);
                    maxWaitMs.accumulateAndGet(wait, Math::max);
                }

                int service = minServiceMs + generator.nextInt(maxServiceMs - minServiceMs + 1);
                Thread.sleep(service);

                customersServed.incrementAndGet();
                System.out.printf("[Cashier %d] served customer %d (wait=%s ms, service=%dms)%n",
                        id, customerId,
                        enqueueTime == null ? "?" : (dequeueTime - enqueueTime),
                        service);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}