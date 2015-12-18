package trunk;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Handler {
	private static Map<Integer, Double> averageBest = new HashMap<Integer,Double>();
	private static Map<Integer, Double> SD = new HashMap<Integer,Double>(); //SD is standard deviation
	private static String root = "/am/st-james/home1/yanlong/Downloads/WebServiceComposition/Experiments";

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException{
		String mutationRoot = root+"/Mutation0";
		String memeticRoot = root+"/Memetic0";

		extract(mutationRoot,"mutation");
		extract(memeticRoot,"memetic");

		/*for(Entry<Integer, Double> e: averageBest.entrySet()){
			System.out.println(e.getKey()+","+e.getValue());
		}*/

		//System.out.println("===========================");

		/*for(Entry<Integer, Double> e: SD.entrySet()){
			System.out.println(e.getKey()+","+e.getValue());
		}*/


	}

	/*
	 * This extracts fitness and SD in the given file root.
	 */
	private static void extract(String mRoot, String type) throws FileNotFoundException, UnsupportedEncodingException{
		double[] fitnesses = new double[30];
		//Create print writers for the summary files
		String summaryFitnessFile = root+"/"+type+"Fitness.txt";
		String summaryTimeFile = root+"/"+type+"Time.txt";
		PrintWriter allFitness = new PrintWriter(summaryFitnessFile, "UTF-8");
		PrintWriter allTime = new PrintWriter(summaryTimeFile, "UTF-8");

		crawling(mRoot,fitnesses,801,809,allFitness,allTime);//dataset0801-0808
		crawling(mRoot,fitnesses,901,906,allFitness,allTime);//dataset0901-0905

		allFitness.close();
		allTime.close();
	}

	/*
	 * This crawls through files of a dataset to help extract fitness and SD.
	 */
	private static void crawling(String mRoot, double[] fitnesses, int dataset, int upper, PrintWriter allFitness, PrintWriter allTime) throws FileNotFoundException, UnsupportedEncodingException{

		for(; dataset<upper; dataset++){
			String fileroot = mRoot+dataset;
			String fitnessFile = fileroot+"/fitness.txt";
			String timeFile = fileroot+"/time.txt";
			PrintWriter fitnessWriter = new PrintWriter(fitnessFile, "UTF-8");
			PrintWriter timeWriter = new PrintWriter(timeFile, "UTF-8");

			for(int seed = 0; seed < 30; seed++){
				String filename = "out"+seed+".stat";
				String filepath = fileroot+"/"+filename;
				grabData(fitnesses, filepath, fitnessWriter, timeWriter, seed);
			}
			fitnessWriter.close();
			timeWriter.close();

			calcStat(fitnessFile, allFitness,dataset);
			calcStat(timeFile, allTime,dataset);
			//calcSD(fitnesses, dataset);
		}
	}

	/*
	 * This grabs the best fitness and overall time from the given file and writes it to the fitness.txt and time.txt.
	 * This method does both reading and writing.
	 */
	private static void grabData(double[] fitnesses, String filepath, PrintWriter fitnessWriter, PrintWriter timeWriter, int seed){
		Path path = Paths.get(filepath);
		try(BufferedReader reader = Files.newBufferedReader(path)){
			String line = null;
			int timeSum = 0;
			while((line = reader.readLine()) != null){
				String[] words = line.split(" ");
				timeSum += Integer.parseInt(words[1]);
				timeSum += Integer.parseInt(words[2]);
				if(words[0].equals("50")){
					fitnesses[seed]=Double.parseDouble(words[5]);
					fitnessWriter.println(seed+" "+words[5]);
					timeWriter.println(seed+" "+timeSum);
					break;
				}
			}
		} catch (IOException x){
			System.err.format("IOException: %s%n", x);
		}
	}

	/*
	 * This calculates the average & SD of best fitness and overall time, and writes it to summary texts.
	 */
	private static void calcStat(String dataFile, PrintWriter writer, int dataset){
		Path dataPath = Paths.get(dataFile);
		try(BufferedReader reader = Files.newBufferedReader(dataPath)){
			String line = null;
			double sum = 0;
			ArrayList<Double> dataEntry = new ArrayList<Double>();
			while((line = reader.readLine()) != null){
				String[] words = line.split(" ");
				double data = Double.parseDouble(words[1]);
				sum += data;
				dataEntry.add(data);
			}
			double mean = sum*1.0/30;
			double powersum = 0;
			for(double d: dataEntry){
				powersum += Math.pow(d-mean, 2);
			}
			double sd = Math.sqrt(powersum/29);
			writer.println(dataset+" "+mean+" "+sd);
		} catch (IOException x){
			System.err.format("IOException: %s%n", x);
		}
	}

	/*
	 * This calculates the standard deviation of the fitness array, the average will also be calculated.
	 */
	private static void calcSD(double[] fitnesses, int dataset){
		double sum = 0;
		for(int i=0; i<fitnesses.length; i++){
			sum += fitnesses[i];
		}
		double average = sum/30;
		averageBest.put(dataset, average);
		double powersum = 0;
		for(int i=0; i<fitnesses.length; i++){
			powersum += Math.pow(fitnesses[i]-average, 2);
		}
		double sd = Math.sqrt(powersum/29);
		SD.put(dataset,sd);
	}

}
