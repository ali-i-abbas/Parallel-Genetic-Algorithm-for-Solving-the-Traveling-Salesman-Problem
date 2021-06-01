import java.util.*;

public class Chromosome {

	public List<Integer> genes;

	public double fitness;

	public Chromosome(int numberOfGenes, Random randomNumberGenerator) {

		genes = new ArrayList<Integer>(numberOfGenes);
		for (int i = 0; i < numberOfGenes; i++) {
			genes.add(i+1);
		}

		// set to a random permutation
		Collections.shuffle(genes, randomNumberGenerator);

		this.fitness = -1; 
	}


}
