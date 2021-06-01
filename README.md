# Implementation of a Parallel Genetic Algorithm for Solving the Traveling Salesman Problem

Traveling Salesman Problem (TSP) is of great interest in many fields of study due to its wide array of applications. Finding
an exact solution to TSP is impractical, therefore we use a Parallel Genetic Algorithm (PGA) to find good solutions. We used an Island
model of PGA, where each island is a thread that runs the basic GA on a sub-population, and the results are combined after all threads
are finished. We used a Synchronous Dual Queue to handle the data exchange between threads that is needed for the migration
between islands. The results of evaluating the implementation on 3 problems in TSPLIB showed that the implementation either found
the optimal solution or at least found a good solution that was at most 3% worse than the optimal solution. We also showed that the
speedup of multi-threading over sequential implementation was linear up to the number of physical CPU cores.


