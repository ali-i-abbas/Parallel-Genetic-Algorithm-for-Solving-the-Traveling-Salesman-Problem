import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ParallelGeneticAlgorithm {

	private String parametersGAFileName;
	private GAParameters parametersGA;
	private Random randomNumberGenerator;

    private Chromosome bestOverAllChromosome;


    public ParallelGeneticAlgorithm(String parametersGAFileName) throws IOException {

		this.parametersGAFileName = parametersGAFileName;
		this.parametersGA = new GAParameters("tsp_parameters_" + parametersGAFileName + ".txt");

		this.randomNumberGenerator = new Random();
		this.randomNumberGenerator.setSeed(parametersGA.randomSeed);
    }

    public void run() throws IOException {
		
		String timesFileName = parametersGAFileName + "_times.csv";
		FileWriter timesOutput = new FileWriter(timesFileName);
		timesOutput.write("R,threads,time,bestF\n");
		
		for (int numberOfIslands = 1; numberOfIslands < 64; numberOfIslands*=2) {

			parametersGA.numberOfIslands = numberOfIslands;

			System.out.println("\n---------------------\nProblem: " + parametersGAFileName + "\nNumber of Islands: " + numberOfIslands);

			String resultsFileName = parametersGAFileName + "_results_" + numberOfIslands + ".txt";
			FileWriter resultsOutput = new FileWriter(resultsFileName);
			// Write parameters to results output file
			parametersGA.writeParameters(resultsOutput);
	
			//  Write best route to output CSV file (x, y)
			String bestRouteFileName = parametersGAFileName + "_best_route_" + numberOfIslands + ".csv";
			FileWriter bestRouteOutput = new FileWriter(bestRouteFileName);
			bestRouteOutput.write("x,y\n");
			StringBuilder bestRouteBuilder = new StringBuilder();
	
			//  Write Stats To Output CSV File (RunIndex, GenIndex, BestFitness, AvgFitness)
			String genStatsFileName = parametersGAFileName + "_stats_" + numberOfIslands + ".csv";
			FileWriter genStatsOutput = new FileWriter(genStatsFileName);
			genStatsOutput.write("R,threadID,G,bestF,avgF\n");
			StringBuilder genStatsBuilder = new StringBuilder();
			
		
			int islandPopulationSize = parametersGA.populationSize / parametersGA.numberOfIslands;
			// make sure islandPopulationSize is always even to ensure correct crossover operation
			if (islandPopulationSize % 2 == 1) {
				islandPopulationSize++;
			}
			System.out.println("\nIslandPopulationSize: " + islandPopulationSize);
			resultsOutput.write("IslandPopulationSize: " + islandPopulationSize + "\n");
			System.out.println("TotalPopulationSize: " + (islandPopulationSize * parametersGA.numberOfIslands) + "\n\n");
			resultsOutput.write("TotalPopulationSize: " + (islandPopulationSize * parametersGA.numberOfIslands) + "\n\n");
	
			bestOverAllChromosome = new Chromosome(parametersGA.numberOfGenes, randomNumberGenerator);
			bestOverAllChromosome.fitness = Double.MAX_VALUE;
	
			SynchronousDualQueue<Chromosome[]>[] migrationQueues = (SynchronousDualQueue<Chromosome[]>[]) new SynchronousDualQueue[parametersGA.numberOfIslands];
	
			Island[] islands = new Island[parametersGA.numberOfIslands];
			for (int k = 0; k < parametersGA.numberOfIslands; k++) {
				migrationQueues[k] = new SynchronousDualQueue<Chromosome[]>();
				islands[k] = new Island(k, parametersGA, islandPopulationSize, migrationQueues);
			}
	
	
			for (int run = 1; run <= parametersGA.numberOfRuns; run++) {

				long startTime = System.nanoTime();
	
				ExecutorService executor = Executors.newFixedThreadPool(parametersGA.numberOfIslands);
				List<Callable<ThreadResult>> islandsCallables = new ArrayList<Callable<ThreadResult>>();
				
				for (int k = 0; k < parametersGA.numberOfIslands; k++) {
					islandsCallables.add(islands[k].islandCallable(run));            
				}
	
				
				List<Future<ThreadResult>> threadResultList = null;
	
				try {
					// execute all threads and wait for them to finish and collect the results of each thread
					threadResultList = executor.invokeAll(islandsCallables);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				} 
		
				try {
					executor.shutdown();
					executor.awaitTermination(5, TimeUnit.SECONDS);
				}
				catch (InterruptedException e) {
					System.err.println("tasks interrupted");
				}
				finally {
					if (!executor.isTerminated()) {
						System.err.println("cancel non-finished tasks");
					}
					executor.shutdownNow();
				}
	
				
				Chromosome bestOfRunChromosome = new Chromosome(parametersGA.numberOfGenes, randomNumberGenerator);
				bestOfRunChromosome.fitness = Double.MAX_VALUE;
	
				// aggregate the results of each thread to get the final result
				for (int i = 0; i < threadResultList.size(); i++) {
					Future<ThreadResult> future = threadResultList.get(i);
					try {
						ThreadResult result = future.get();
	
						if (result.bestOfRunChromosome.fitness < bestOfRunChromosome.fitness) {
							copyChromosome(bestOfRunChromosome, result.bestOfRunChromosome);
						}
						genStatsBuilder.append(result.genStatsBuilder);
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
				}
	
				if (bestOfRunChromosome.fitness < bestOverAllChromosome.fitness) {
					copyChromosome(bestOverAllChromosome, bestOfRunChromosome);
				}
	
				//writeGenes(bestOfRunChromosome, resultsOutput);
	
				System.out.println(run + "\t" + "Best of Run Fitness: " + "\t" + bestOfRunChromosome.fitness);
				
				long endTime = System.nanoTime(); 
				long durationInNano = (endTime - startTime);  //Total execution time in nano seconds 
				double executionTimeInSeconds = durationInNano / 1e9;

				timesOutput.write(run + "," + numberOfIslands + "," + executionTimeInSeconds + "," + bestOfRunChromosome.fitness + "\n");
		
				System.out.println("\nExecution Time In Seconds: " + executionTimeInSeconds);
		
			} 
			
			
			System.out.println("\nBest Overall Fitness: " + bestOverAllChromosome.fitness + "\n");
	
	
			resultsOutput.write("\nBest Overall Chromosome:\n\n");
			writeGenes(bestOverAllChromosome, resultsOutput);
	
	
			resultsOutput.close();
	
			// Output Stats to CSV File
			genStatsOutput.write(genStatsBuilder.toString());
			genStatsOutput.close();
	
			for (int j = 0; j < parametersGA.numberOfGenes; j++) {
				bestRouteBuilder.append(parametersGA.citiesCoordinates[bestOverAllChromosome.genes.get(j)-1][0] + "," + parametersGA.citiesCoordinates[bestOverAllChromosome.genes.get(j)-1][1] + "\n");
			}
			bestRouteOutput.write(bestRouteBuilder.toString());
			bestRouteOutput.close();
			
		}

		timesOutput.close();

    }

	// Copy source chromosome to destination chromosome
	private void copyChromosome(Chromosome destination, Chromosome source) {
		destination.genes = new ArrayList<Integer>(source.genes);
		destination.fitness = source.fitness;
	}

    //  write chromosome genes to output file
    public void writeGenes(Chromosome chromosome, FileWriter output) throws java.io.IOException{
        for (int i = 0; i < parametersGA.numberOfGenes; i++){
            output.write(chromosome.genes.get(i) + " ");
        }
        output.write("\n\nFitness:       " + chromosome.fitness + "\n\n");        
    }
    
}