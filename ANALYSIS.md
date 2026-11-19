# Costco Checkout Simulator — Analysis

Five experiments were run against `ex2.CostcoSimulation` with the fixed
wait-time instrumentation. Each run's full log is in `results/`; the summary
numbers below are extracted from the `===== SIMULATION REPORT =====` block
at the end of each file.

# Inputs

File	            Customers	Cashiers	Queue cap	Shop [min-max] (ms)	    Service [min-max] (ms)	    Variable being tested
exp1_understaffed	20	        1	        5	        500–1500	            800–2000	                Staffing too low
exp2_optimal	    20	        3	        5	        500–1500	            800–2000	                Balanced
exp3_overstaffed	20	        10	        5	        500–1500	            800–2000	                Staffing too high
exp4_small_queue	20	        3	        2	        200–600	                800–2000	                Small buffer, fast arrivals
exp5_large_queue	20	        3	        20	        200–600	                800-2000	                Large buffer, fast arrivals

# Results

The following results are based on the simulation data from each experiment.

Exp	                Cashiers	Queue cap	Total time	    Avg wait	    Max wait	    Throughput/hr
1 Understaffed	    1	        5	        30,323 ms	    13,430 ms	    27,562 ms	    2,374
2 Optimal	        3	        5	        12,021 ms	    4,015 ms	    8,590 ms	    5,990
3 Overstaffed	    10	        5	        4,610 ms	    467 ms	        1,383 ms	    15,618
4 Small queue	    3	        2	        10,157 ms	    3,532 ms	    8,149 ms	    7,089
5 Large queue	    3	        20	        10,569 ms	    4,246 ms	    8,686 ms	    6,812

# Observations

- **Understaffed (Exp 1)** With a single cashier, the queue saturates almost
  immediately and every customer after the first waits. Average wait is
  13.4 s and max wait is 27.6 s — about 91% of the 30.3 s total run time,
  meaning the cashier was never idle. Max-wait ≈ total-time is the signature
  of a service-bound system: the bottleneck is the cashier, not the customers.

- **Optimal (Exp 2)** Three cashiers cut total time 2.5× (30.3 → 12.0 s),
  average wait 3.3× (13.4 → 4.0 s), and max wait 3.2× (27.6 → 8.6 s). The
  cashiers still have occasional idle gaps, so there is no wasted labor;
  this is the knee of the cost-vs-wait curve for this arrival rate.

- **Overstaffed (Exp 3)** Going from 3 to 10 cashiers (3.3× more labor)
  shortens total time to 4.6 s and average wait to 467 ms, but the log shows
  several cashiers serving zero or one customer — the extra lanes sit idle.
  Throughput is capped by the customer arrival rate, not the cashier count,
  so beyond ~3 cashiers the marginal cashier adds cost without adding
  capacity.

- **Queue size (Exp 4 vs 5)** With shop/service ranges held fixed and only
  queue capacity varying, the small queue (cap 2) and large queue (cap 20)
  have essentially the same total time (10.2 vs 10.6 s) and throughput
  (7,089 vs 6,812/hr) — the 3 cashiers are the bottleneck in both. The
  difference is in how the queue absorbs the burst: the large queue has
  higher average wait (4,246 vs 3,532 ms) and higher max wait (8,686 vs
  8,149 ms) because customers sit in line longer instead of the producers
  blocking. Queue capacity changes back-pressure and wait distribution, not
  service capacity.

# Recommendation — 60 customers/hour at $18/hour per cashier

The lab's service time (800–2000 ms, mean 1.4 s) is a simulation parameter
chosen so a 20-customer run finishes in seconds; it is not a realistic
checkout time. To produce a staffing recommendation, scale service time to a
realistic Costco checkout of ~4 minutes (240 s) per customer.

**Per-cashier service rate.** μ = 3600 / 240 = 15 customers/hour.

Required cashiers for arrival rate λ = 60/hr (utilization ρ = λ / (c·μ)):

Cashiers        Labor cost/hr     Utilization ρ       Status 
3               $54               1.33                Unstable — queue grows without bound
4               $72               1.00                Borderline — any burst causes unbounded growth
5               $90               0.80                Recommended — stable, absorbs normal variability
6               $108              0.67                Comfortable, over-provisioned at average load

**Recommendation:** staff **5 cashiers** (~$90/hour) for the average 60
customers/hour load. This keeps utilization at ~80%, the usual operations
target that absorbs day-to-day variability without excessive wait. Add a
**sixth cashier only during peak windows** (λ > ~72/hour, where 5-cashier ρ
exceeds 0.96 and waits climb steeply). Four cashiers (ρ = 1.00) is the
absolute floor and will fail on any burst; do not staff below 5 for a
sustained 60/hr arrival rate.

