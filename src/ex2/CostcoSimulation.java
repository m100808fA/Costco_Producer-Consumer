package ex2;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;

public class CostcoSimulation {

    public static void main(String[] args) throws InterruptedException {
        Scanner in = new Scanner(System.in);

        System.out.print("Number of customers: ");
        int numCustomers = in.nextInt();
        System.out.print("Number of cashiers: ");
        int numCashiers = in.nextInt();
        System.out.print("Queue capacity: ");
        int queueCapacity = in.nextInt();
        System.out.print("Min/Max shop time (ms): ");
        int minShop = in.nextInt(), maxShop = in.nextInt();
        System.out.print("Min/Max service time (ms): ");
        int minService = in.nextInt(), maxService = in.nextInt();

        runSimulation(numCustomers, numCashiers, queueCapacity,
                minShop, maxShop, minService, maxService);
    }

    public static void runSimulation(int numCustomers, int numCashiers, int queueCapacity,
                                     int minShop, int maxShop,
                                     int minService, int maxService) throws InterruptedException {

        CheckoutQueue queue = new CheckoutQueue(queueCapacity);
        AtomicLong totalWait = new AtomicLong();
        AtomicLong maxWait   = new AtomicLong();
        AtomicLong queued    = new AtomicLong();
        AtomicLong served    = new AtomicLong();

        long start = System.currentTimeMillis();

        // Cashiers (consumers) — daemon threads so they stop when main exits
        List<Thread> cashierThreads = new ArrayList<>();
        for (int i = 1; i <= numCashiers; i++) {
            Thread t = new Thread(
                    new Cashier(i, queue, minService, maxService, -1,
                            totalWait, maxWait, served), "Cashier-" + i);
            t.setDaemon(true);
            cashierThreads.add(t);
            t.start();
        }

        // Customers (producers)
        List<Thread> customerThreads = new ArrayList<>();
        for (int i = 1; i <= numCustomers; i++) {
            Thread t = new Thread(
                    new Customer(i, queue, minShop, maxShop, queued), "Customer-" + i);
            customerThreads.add(t);
            t.start();
        }

        // Wait for all customers to be served
        for (Thread t : customerThreads) t.join();
        while (served.get() < numCustomers) Thread.sleep(10);

        long elapsed = System.currentTimeMillis() - start;

        for (Thread t : cashierThreads) t.interrupt();
        for (Thread t : cashierThreads) t.join(1000);

        long avgWait = served.get() == 0 ? 0 : totalWait.get() / served.get();
        double throughputPerHour = elapsed == 0 ? 0
                : (numCustomers * 3_600_000.0) / elapsed;

        System.out.println("\n===== SIMULATION REPORT =====");
        System.out.printf("Customers:        %d%n", numCustomers);
        System.out.printf("Cashiers:         %d%n", numCashiers);
        System.out.printf("Queue capacity:   %d%n", queueCapacity);
        System.out.printf("Total time:       %d ms (%.2f s)%n", elapsed, elapsed / 1000.0);
        System.out.printf("Customers served: %d%n", served.get());
        System.out.printf("Average wait:     %d ms%n", avgWait);
        System.out.printf("Max wait:         %d ms%n", maxWait.get());
        System.out.printf("Throughput:       %.1f customers/hour%n", throughputPerHour);
        System.out.println("=============================\n");
    }
}