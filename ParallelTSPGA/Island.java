import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;

public class Island {

    private GAParameters parametersGA;
	private Random randomNumberGenerator;
	private SynchronousDualQueue<Chromosome[]>[] migrationQueues;
	private int islandPopulationSize;
	private int threadID;

    public Island(int threadID, GAParameters parametersGA, int islandPopulationSize, SynchronousDualQueue<Chromosome[]>[] migrationQueues) {
		this.threadID = threadID;
		this.parametersGA = parametersGA;
		this.islandPopulationSize = islandPopulationSize;
		this.migrationQueues = migrationQueues;

		this.randomNumberGenerator = new Random();
		this.randomNumberGenerator.setSeed(parametersGA.randomSeed);
    }
    
	public Callable<ThreadResult> islandCallable(int run) {
		return () -> {

			StringBuilder genStatsBuilder = new StringBuilder();
			double averageFitness;

			double defaltWorst = Double.MIN_VALUE;
			double defaltBest = Double.MAX_VALUE;

			Chromosome[] parents = new Chromosome[islandPopulationSize];
			Chromosome[] children = new Chromosome[islandPopulationSize];

			Chromosome bestOfRunChromosome = new Chromosome(parametersGA.numberOfGenes, randomNumberGenerator);			
			bestOfRunChromosome.fitness = Double.MAX_VALUE;

			// Initialize the first generation of the run
			for (int i = 0; i < islandPopulationSize; i++) {
				parents[i] = new Chromosome(parametersGA.numberOfGenes, randomNumberGenerator);
				children[i] = new Chromosome(parametersGA.numberOfGenes, randomNumberGenerator);
			}
			
			Chromosome bestOfGenChromosome = new Chromosome(parametersGA.numberOfGenes, randomNumberGenerator);
			updateFitness(bestOfGenChromosome);

			for (int generation = 0; generation < parametersGA.numberOfGenerations; generation++) {

				double sumFitness = 0;

				int worstOfGen = -1;
				double worstOfGenFitness = defaltWorst;
				int secondWorstOfGen = -1;
				double secondWorstOfGenFitness = defaltWorst;
				int thirdWorstOfGen = -1;
				double thirdWorstOfGenFitness = defaltWorst;
				int bestOfGen = -1;
				// since we use elitism, we will insert the best of last generation into current 
				// generation, so the best of current generation is at least the best of last generation
				double bestOfGenFitness = defaltBest;
				
				// array of chromosomes to be send to sucessor island
				// sendToSuccessorIsland[0] is the best chromosome in current generation
				// sendToSuccessorIsland[1] is the second best chromosome in current generation
				Chromosome[] sendToSuccessorIsland = new Chromosome[2];
				int secondBestOfGen = -1;
				double secondBestOfGenFitness = defaltBest;

				// Update fitness of each chromosome
				for (int i = 0; i < islandPopulationSize; i++) {

					updateFitness(parents[i]);

					sumFitness = sumFitness + parents[i].fitness;
					
					if (parents[i].fitness > worstOfGenFitness) {
						worstOfGen = i;
						worstOfGenFitness = parents[i].fitness;
					} else if (parents[i].fitness > secondWorstOfGenFitness) {
						secondWorstOfGen = i;
						secondWorstOfGenFitness = parents[i].fitness;
					} else if (parents[i].fitness > thirdWorstOfGenFitness) {
						thirdWorstOfGen = i;
						thirdWorstOfGenFitness = parents[i].fitness;
					}
					if (parents[i].fitness < bestOfGenFitness) {
						bestOfGen = i;
						bestOfGenFitness = parents[i].fitness;
					} else if (parents[i].fitness < secondBestOfGenFitness) {
						secondBestOfGen = i;
						secondBestOfGenFitness = parents[i].fitness;
					}
				}

				// location of best of last generation in case elitism update is successful
				int bestOfLastGen = -1;

				// Elitism: replace worst of current generation with best of last generation
				if (bestOfGenChromosome.fitness < parents[worstOfGen].fitness) {
					sumFitness -= parents[worstOfGen].fitness;
					sumFitness += bestOfGenChromosome.fitness;
					copyChromosome(parents[worstOfGen], bestOfGenChromosome);
					bestOfLastGen = worstOfGen;
					worstOfGen = secondWorstOfGen;
					secondWorstOfGen = thirdWorstOfGen;
				}

				// if the best of last generation is better than the best of current generation, 
				// then update bestOfGen index to point to bestOfLastGen which contains the best value after elitisim update
				if (bestOfGenChromosome.fitness < parents[bestOfGen].fitness) {
					bestOfGen = bestOfLastGen;
				} else {
					copyChromosome(bestOfGenChromosome, parents[bestOfGen]);
				}
				
				sendToSuccessorIsland[0] = new Chromosome(parametersGA.numberOfGenes, randomNumberGenerator);
				copyChromosome(sendToSuccessorIsland[0], bestOfGenChromosome); 

				sendToSuccessorIsland[1] = new Chromosome(parametersGA.numberOfGenes, randomNumberGenerator);
				copyChromosome(sendToSuccessorIsland[1], parents[secondBestOfGen]);  
				             
				if (bestOfGenChromosome.fitness < bestOfRunChromosome.fitness) {
					copyChromosome(bestOfRunChromosome, bestOfGenChromosome);                    
				}

				// migration
				if ((generation + 1) % parametersGA.migrationInterval == 0 && randomNumberGenerator.nextDouble() < parametersGA.migrationRate) {
					// send the 2 best chromosomes of current generation to successor island
					migrationQueues[Math.floorMod(threadID + 1, parametersGA.numberOfIslands)].enq(sendToSuccessorIsland);
					
					// get the 2 best chromosomes of predecessor island and replace the worst 2 chromosomes of current generation
					Chromosome[] receivedFromPredecessorIsland = migrationQueues[Math.floorMod(threadID - 1, parametersGA.numberOfIslands)].deq();

					sumFitness -= parents[worstOfGen].fitness;
					sumFitness += receivedFromPredecessorIsland[0].fitness;
					copyChromosome(parents[worstOfGen], receivedFromPredecessorIsland[0]);

					sumFitness -= parents[secondWorstOfGen].fitness;
					sumFitness += receivedFromPredecessorIsland[1].fitness;
					copyChromosome(parents[secondWorstOfGen], receivedFromPredecessorIsland[1]);
				}

				averageFitness = sumFitness / islandPopulationSize;
				
				genStatsBuilder.append(run + "," + threadID + "," + generation + "," + bestOfGenChromosome.fitness + "," + averageFitness + "\n");


				int parent1 = -1;
				int parent2 = -1;

				// perform crossover
				for (int i = 0; i < islandPopulationSize; i = i + 2) {

					// Select Two Parents
					parent1 = selectParent(parents);
					parent2 = parent1;
					while (parent2 == parent1) {
						parent2 = selectParent(parents);
					}

					// Crossover two parents to create two children
					if (randomNumberGenerator.nextDouble() < parametersGA.crossoverRate) {
						crossover(parents[parent1], parents[parent2], children[i]);
						crossover(parents[parent1], parents[parent2], children[i + 1]);
					} else {
						crossover(parent1, parents[parent1], children[i]);
						crossover(parent2, parents[parent2], children[i + 1]);
					}
				}

				// Mutate children and copy them to parents to become next generation parents
				for (int i = 0; i < islandPopulationSize; i++) {
					
					mutateHM(children[i]);

					if (randomNumberGenerator.nextDouble() < parametersGA.mutationRate * generation / parametersGA.numberOfGenerations){
						mutateEM(children[i]);
					} 

					copyChromosome(parents[i], children[i]);
				}

			} // Repeat the above loop for each generation

			
			ThreadResult result = new ThreadResult();
			result.genStatsBuilder = genStatsBuilder;
			result.bestOfRunChromosome = bestOfGenChromosome;

			return result;
		};
	}

    
	// Select a parent for genetic operations using tournament selection
	private int selectParent(Chromosome[] parents) {

		int best = randomNumberGenerator.nextInt(parents.length);
		int current;
        for (int i = 1; i < parametersGA.tournamentSize; i++){
			current = randomNumberGenerator.nextInt(parents.length);
			if (parents[current].fitness < parents[best].fitness && randomNumberGenerator.nextDouble() < parametersGA.tournamentProbability ) {
				best = current;
			}
		}

        return best;
	}

	// Create two children from two parents using heuristic greedy crossover (HGreX)
	private void crossover(Chromosome parent1, Chromosome parent2, Chromosome child) {

		boolean[] visitedParent1 = new boolean[parametersGA.numberOfGenes];
		boolean[] visitedParent2 = new boolean[parametersGA.numberOfGenes];

		int selectedLocationParent1 = randomNumberGenerator.nextInt(parametersGA.numberOfGenes);
			
		int selectedVertex = parent1.genes.get(selectedLocationParent1);

		int selectedLocationParent2 = parent2.genes.indexOf(selectedVertex);

		visitedParent1[selectedLocationParent1] = true;
		visitedParent2[selectedLocationParent2] = true;
	
		child.genes.set(0, selectedVertex);

		for (int i = 1; i < child.genes.size(); i++) {
			selectedLocationParent1 = Math.floorMod(selectedLocationParent1 + 1, parametersGA.numberOfGenes);
			selectedLocationParent2 = Math.floorMod(selectedLocationParent2 + 1, parametersGA.numberOfGenes);

			if (visitedParent1[selectedLocationParent1] && visitedParent2[selectedLocationParent2]) {
				for (int j = 0; j < visitedParent1.length; j++) {
					if (!visitedParent1[Math.floorMod(selectedLocationParent1 + j + 1, parametersGA.numberOfGenes)]) {
						selectedLocationParent1 = Math.floorMod(selectedLocationParent1 + j + 1, parametersGA.numberOfGenes);
						break;
					}
				}
				for (int j = 0; j < visitedParent2.length; j++) {
					if (!visitedParent2[Math.floorMod(selectedLocationParent2 + j + 1, parametersGA.numberOfGenes)]) {
						selectedLocationParent2 = Math.floorMod(selectedLocationParent2 + j + 1, parametersGA.numberOfGenes);
						break;
					}
				}
				if (parametersGA.distance[selectedVertex-1][parent1.genes.get(selectedLocationParent1)-1] < parametersGA.distance[selectedVertex-1][parent2.genes.get(selectedLocationParent2)-1]) {
					selectedVertex = parent1.genes.get(selectedLocationParent1);
					child.genes.set(i, selectedVertex);
					selectedLocationParent2 = parent2.genes.indexOf(selectedVertex);
					visitedParent1[selectedLocationParent1] = true;
					visitedParent2[selectedLocationParent2] = true;
				} else {
					selectedVertex = parent2.genes.get(selectedLocationParent2);
					child.genes.set(i, selectedVertex);
					selectedLocationParent1 = parent1.genes.indexOf(selectedVertex);
					visitedParent1[selectedLocationParent1] = true;
					visitedParent2[selectedLocationParent2] = true;
				}
			} else if (visitedParent1[selectedLocationParent1] && !visitedParent2[selectedLocationParent2]) {
				selectedVertex = parent2.genes.get(selectedLocationParent2);
				child.genes.set(i, selectedVertex);
				selectedLocationParent1 = parent1.genes.indexOf(selectedVertex);
				visitedParent1[selectedLocationParent1] = true;
				visitedParent2[selectedLocationParent2] = true;
			} else if (!visitedParent1[selectedLocationParent1] && visitedParent2[selectedLocationParent2]) {
				selectedVertex = parent1.genes.get(selectedLocationParent1);
				child.genes.set(i, selectedVertex);
				selectedLocationParent2 = parent2.genes.indexOf(selectedVertex);
				visitedParent1[selectedLocationParent1] = true;
				visitedParent2[selectedLocationParent2] = true;
			} else if (parametersGA.distance[selectedVertex-1][parent1.genes.get(selectedLocationParent1)-1] < parametersGA.distance[selectedVertex-1][parent2.genes.get(selectedLocationParent2)-1]) {
				selectedVertex = parent1.genes.get(selectedLocationParent1);
				child.genes.set(i, selectedVertex);
				selectedLocationParent2 = parent2.genes.indexOf(selectedVertex);
				visitedParent1[selectedLocationParent1] = true;
				visitedParent2[selectedLocationParent2] = true;
			} else {
				selectedVertex = parent2.genes.get(selectedLocationParent2);
				child.genes.set(i, selectedVertex);
				selectedLocationParent1 = parent1.genes.indexOf(selectedVertex);
				visitedParent1[selectedLocationParent1] = true;
				visitedParent2[selectedLocationParent2] = true;
			}
		}
		
		// Set fitness value back to not evaluated
		child.fitness = -1;
	}

	// Create a child from a parent by copying
	private void crossover(int pnum, Chromosome parent, Chromosome child) {

		// Create child chromosome from parental material
		child.genes = new ArrayList<Integer>(parent.genes);

		// Set fitness value back to not evaluated
		child.fitness = -1;
	}

	// Mutate a chromosome using Exchange Mutation (EM)
	private void mutateEM(Chromosome chromosome) {		
		int point1 = randomNumberGenerator.nextInt(parametersGA.numberOfGenes);
		int point2;
		do {	
			point2 = randomNumberGenerator.nextInt(parametersGA.numberOfGenes);				
		} while (point1 == point2);
		
		int temp = chromosome.genes.get(point1);
		chromosome.genes.set(point1, chromosome.genes.get(point2));
		chromosome.genes.set(point2, temp);
	}

	// Mutate a chromosome using Heuristic Mutation (HM)
	private void mutateHM(Chromosome chromosome) {	

		double bestDistance = Double.MAX_VALUE;
		double distance;
		int temp;
		
		int selectedCityLocation = randomNumberGenerator.nextInt(parametersGA.numberOfGenes);
		int selectedCity = chromosome.genes.get(selectedCityLocation);
		int closestCityLocation = -1;

		for (int i = 0; i < parametersGA.numberOfGenes; i++) {
			if (i != selectedCityLocation) {
				distance = parametersGA.distance[selectedCity-1][chromosome.genes.get(i)-1];
				if (distance < bestDistance) {
					bestDistance = distance;
					closestCityLocation = i;
				}
			}
		}

		if (closestCityLocation < selectedCityLocation) {
			closestCityLocation += parametersGA.numberOfGenes;
		}
		
		for (int i = selectedCityLocation + 1; i < (selectedCityLocation + closestCityLocation + 2) / 2 ; i++) {
			temp = chromosome.genes.get(Math.floorMod(i, parametersGA.numberOfGenes));
			chromosome.genes.set(Math.floorMod(i, parametersGA.numberOfGenes), chromosome.genes.get(Math.floorMod(selectedCityLocation + closestCityLocation - i + 1, parametersGA.numberOfGenes)));
			chromosome.genes.set(Math.floorMod(selectedCityLocation + closestCityLocation - i + 1, parametersGA.numberOfGenes), temp);
		}				
	}

	// Copy source chromosome to destination chromosome
	private void copyChromosome(Chromosome destination, Chromosome source) {
		destination.genes = new ArrayList<Integer>(source.genes);
		destination.fitness = source.fitness;
	}

	// calculate the sum of distances between cities of a tour as fitness
    private void updateFitness(Chromosome chromosome){
        chromosome.fitness = 0;
        for (int i = 0; i < parametersGA.numberOfGenes - 1; i++){
            chromosome.fitness += parametersGA.distance[chromosome.genes.get(i)-1][chromosome.genes.get(i+1)-1];
        }
        chromosome.fitness += parametersGA.distance[chromosome.genes.get(parametersGA.numberOfGenes-1)-1][chromosome.genes.get(0)-1];
    }

}