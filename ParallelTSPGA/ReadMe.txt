This will run the PGA with the specified parameters in tsp_parameters.txt file.

Compile the code using this command:

javac *.java

To run the main program use this command:

java ParallelTSPGA tsp_parameters.txt

The results will be shown on screen and in results.txt file. best_route.csv will contain the x, y coordinates of the best route found. gen_stats.csv contains the best and average fitness of each generation for each thread in each run.

You can change the GA parameters in tsp_parameters.txt.
