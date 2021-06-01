import java.io.*;
import java.util.Scanner;

public class GAParameters {

	
	public long randomSeed;

	public String distancesFileName;
	public int numberOfGenes;

	public int populationSize;
	public int numberOfGenerations;
	public int numberOfRuns;

	public int tournamentSize;
	public double tournamentProbability;

	public double crossoverRate;
	public double mutationRate;

	public int numberOfIslands;
	public int migrationInterval;
	public double migrationRate;
	
	public double distance[][]; // distance[City_A][City_B] == distance[City_B][City_A]
	public double citiesCoordinates[][];

	public GAParameters(String parmfilename) throws java.io.IOException {


		Scanner scanner = new Scanner(new File(parmfilename));
		while (scanner.hasNext()) {
			String parameterName = scanner.next().toLowerCase();
			String parameterValue = scanner.next();
				
			// cpu initiates the read or write to the next level which is level 1 cache
			switch (parameterName) {
				case "distancesfilename":
					distancesFileName = parameterValue;
					break;
				case "populationsize":
					populationSize = Integer.parseInt(parameterValue);
					break;
				case "numberofgenerations":
					numberOfGenerations = Integer.parseInt(parameterValue);
					break;
				case "numberofruns":
					numberOfRuns = Integer.parseInt(parameterValue);
					break;
				case "tournamentsize":
					tournamentSize = Integer.parseInt(parameterValue);
					break;
				case "tournamentprobability":
					tournamentProbability = Double.parseDouble(parameterValue);
					break;
				case "crossoverrate":
					crossoverRate = Double.parseDouble(parameterValue);
					break;
				case "mutationrate":
					mutationRate = Double.parseDouble(parameterValue);
					break;
				case "numberofislands":
					numberOfIslands = Integer.parseInt(parameterValue);
					break;
				case "migrationinterval":
					migrationInterval = Integer.parseInt(parameterValue);
					break;
				case "migrationrate":
					migrationRate = Double.parseDouble(parameterValue);
					break;
				case "randomseed":
					randomSeed = Long.parseLong(parameterValue);
					break;
			}			
		}

		try (BufferedReader br = new BufferedReader(new FileReader(distancesFileName))) {
			String line;
			String dimensionField = "DIMENSION : ";
			int numberOfCities = 0;
			// Find dimension field
			while ((line = br.readLine()) != null) {
				if (line.startsWith(dimensionField)) {
					numberOfCities = Integer.parseInt(line.substring(dimensionField.length()));
					// Skip next 2 lines:
					br.readLine();
					br.readLine();
					break;
				}
			}
			if (numberOfCities == 0) {
				System.out.println("\nERROR - Input distance file is empty.\n");
				return;
			} 
			// city 0 is not used
			distance = new double[numberOfCities][numberOfCities]; 

			numberOfGenes = numberOfCities;

			String cityInfo[];
			int i = 0, x = 0, y = 1;
			citiesCoordinates = new double[numberOfCities][2];

			while (i < numberOfCities) {
				line = br.readLine();
				cityInfo = line.split(" ");
				// x coordinate
				citiesCoordinates[i][x] = Double.valueOf(cityInfo[1]); 
				// y coordinate
				citiesCoordinates[i][y] = Double.valueOf(cityInfo[2]); 
				i++;
			}

			for (int j = 0; j < numberOfCities; j++) {
				for (int k = 0; k < numberOfCities; k++) {
					distance[j][k] = Distance(citiesCoordinates[j][x], citiesCoordinates[j][y], citiesCoordinates[k][x],
							citiesCoordinates[k][y]);
				}
			}
			
		}

	}

	public GAParameters(GAParameters parametersGA){

		distancesFileName = parametersGA.distancesFileName;
		numberOfGenes = parametersGA.numberOfGenes;
	
		populationSize = parametersGA.populationSize;
		numberOfGenerations = parametersGA.numberOfGenerations;
		numberOfRuns = parametersGA.numberOfRuns;
	
		crossoverRate = parametersGA.crossoverRate;	
		mutationRate = parametersGA.mutationRate;
	
		numberOfIslands = parametersGA.numberOfIslands;
		migrationInterval = parametersGA.migrationInterval;
		migrationRate = parametersGA.migrationRate;
		
	
		distance = new double[parametersGA.distance.length][];

        for (int i = 0; i < parametersGA.distance.length; ++i) {
			distance[i] = new double[parametersGA.distance[i].length];
             System.arraycopy(parametersGA.distance[i], 0, distance[i], 0, distance[i].length);
		}
		
		
		citiesCoordinates = new double[parametersGA.citiesCoordinates.length][];

        for (int i = 0; i < parametersGA.citiesCoordinates.length; ++i) {
			citiesCoordinates[i] = new double[parametersGA.citiesCoordinates[i].length];
             System.arraycopy(parametersGA.citiesCoordinates[i], 0, citiesCoordinates[i], 0, citiesCoordinates[i].length);
        }
	}

	public double Distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
	}

	public void writeParameters(FileWriter outputFile) throws java.io.IOException {

		outputFile.write("Distances File Name   : " + distancesFileName + "\n");
		outputFile.write("Number of Genes       : " + numberOfGenes + "\n");
		outputFile.write("Population Size       : " + populationSize + "\n");
		outputFile.write("Number of Generations : " + numberOfGenerations + "\n");
		outputFile.write("Number of Runs        : " + numberOfRuns + "\n");
		outputFile.write("Crossover Rate        : " + crossoverRate + "\n");
		outputFile.write("Mutation Rate         : " + mutationRate + "\n");
		outputFile.write("\n\n");

	}
} 
