package trunk;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

		extract(mutationRoot);

		for(Entry<Integer, Double> e: averageBest.entrySet()){
			System.out.println(e.getKey()+","+e.getValue());
		}

		//System.out.println("===========================");

		/*for(Entry<Integer, Double> e: SD.entrySet()){
			System.out.println(e.getKey()+","+e.getValue());
		}*/


	}

	/*
	 * This extracts fitness and SD in the given file root.
	 */
	private static void extract(String root) throws FileNotFoundException, UnsupportedEncodingException{
		double[] fitnesses = new double[30];
		crawling(root,fitnesses,801,809);//dataset0801-0808
		crawling(root,fitnesses,901,906);//dataset0901-0905
	}

	/*
	 * This crawls through files of a dataset to help extract fitness and SD.
	 */
	private static void crawling(String root, double[] fitnesses, int dataset, int upper) throws FileNotFoundException, UnsupportedEncodingException{
		for(; dataset<upper; dataset++){
			String fileroot = root+dataset;
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
			calcSD(fitnesses, dataset);
		}
	}

	/*
	 * This grabs the best fitness and overall time from the file. Also writes it to the fitness file.
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
