public class ParallelTSPGA {

	public static void main(String[] args) throws java.io.IOException {
		ParallelGeneticAlgorithm ga = new ParallelGeneticAlgorithm(args[0]);
		ga.run();
	}

}
