public class ParallelTSPGA {

	public static void main(String[] args) throws java.io.IOException {

		String[] parametersGAFileNames = new String[] {"berlin52", "pr76", "rd100"};

		for (String parametersGAFileName : parametersGAFileNames) {
			ParallelGeneticAlgorithm ga = new ParallelGeneticAlgorithm(parametersGAFileName);
			ga.run();
		}
		
	}

}
