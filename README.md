# Costco Checkout Simulator — Producer-Consumer Lab

A Java simulation of a Costco checkout line built on the Producer-Consumer
pattern. Customers (producers) shop for a random time and join a shared
bounded queue; cashiers (consumers) take customers from the queue and serve
them. The project transforms the Deitel `BlockingBuffer` example (`ex2`
package) into a configurable checkout simulator with wait-time instrumentation,
and uses five experiments to derive a data-driven staffing recommendation.

## Quick start

```bash
# compile
mkdir -p out && javac -d out src/ex2/*.java

# run the original Deitel baseline (Buffer/BlockingBuffer/Producer/Consumer)
java -cp out ex2.BlockingBufferTest

# run the Costco simulator (interactive)
java -cp out ex2.CostcoSimulation
```

## How the Producer-Consumer mapping works

Pattern role	        Lab class	                                            Costco role
Shared buffer	        BlockingBuffer (given) → CheckoutQueue (new)	        The checkout line
Producer	            Producer (given) → Customer (new)	                    Shoppers joining the line
Consumer	            Consumer (given) → Cashier (new)	                    Cashiers serving customers
Payload	                Integer (1..10)	                                        Customer ID (Integer)
Synchronization	        ArrayBlockingQueue.put() / take()	                    Same, wrapped in CheckoutQueue

The original Deitel classes are preserved unchanged (BlockingBufferTest still
runs), so the transformation from the abstract example to the Costco
simulator is visible side by side.

## Experiments and findings

Five runs were recorded in results/. Full interpretation is in
ANALYSIS.md; the short version:

    Understaffed (1 cashier): queue saturates, average wait 13.4 s, max
wait 27.6 s — ~91% of total run time. The cashier is the bottleneck.

    Optimal (3 cashiers): total time 12.0 s, average wait 4.0 s — the knee
of the cost-vs-wait curve for this arrival rate.

    Overstaffed (10 cashiers): total time 4.6 s, average wait 467 ms, but
the log shows several cashiers idle. Throughput is capped by arrivals, not
by cashier count.

    Queue size (cap 2 vs cap 20, same 3 cashiers): nearly identical total
time and throughput; the large queue has higher average and max wait
because customers sit in line longer instead of producers blocking. Queue
capacity changes back-pressure, not service capacity.


    **Staffing recommendation for 60 customers/hour at $18/hour per cashier**
(using a realistic 4-minute service time; see `ANALYSIS.md` for the
derivation): staff **5 cashiers** (~$90/hour, utilization ρ ≈ 0.80) for
average load, with a sixth only during peak windows (λ ≳ 72/hour).

## Build requirements

JDK 8 or newer (java, javac on PATH)

No external dependencies — only java.util.concurrent and java.security