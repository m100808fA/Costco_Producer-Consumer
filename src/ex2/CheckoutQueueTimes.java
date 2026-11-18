package ex2;

import java.util.concurrent.ConcurrentHashMap;

/** Shared map: customerId -> enqueue timestamp (ms). */
public final class CheckoutQueueTimes {
    private static final ConcurrentHashMap<Integer, Long> ENQUEUE = new ConcurrentHashMap<>();
    private CheckoutQueueTimes() {}
    public static void recordEnqueue(int id, long t) { ENQUEUE.put(id, t); }
    public static Long getAndRemove(int id)          { return ENQUEUE.remove(id); }
}