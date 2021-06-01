This will run the PGA experiments for all 3 tsp files with variable number of islands.

Compile the code using this command:

javac *.java

To run the main program use this command:

java ParallelTSPGA

The results will be shown on screen and in a number of output files:

<problem_name>_results_<number of threads>.txt : shows a summary of the experiment.
<problem_name>_best_route_<number of threads>.csv will contain the x, y coordinates of the best route found.
<problem_name>_stats_<number of threads>.csv contains the best and average fitness of each generation for each thread in each run.
<problem_name>_times.csv contains the execution time for each run for each number of threads

You can change the GA parameters for each problem in tsp_parameters_<problem_name>.txt.
