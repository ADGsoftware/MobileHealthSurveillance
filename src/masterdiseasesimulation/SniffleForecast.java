package masterdiseasesimulation;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import datacontainers.InfoJungStorage;
import datacontainers.InfoStorage;
import infectionrate.InfiniteLoopException;
import infectionrate.NotYetSupportedException;
import infectionrate.ObsoleteException;
import infectionrate.Simulator;
import moremethods.GetData;
import moremethods.MoreMethods;
import simulateddata.DataSimulator;
public class SniffleForecast {
	public static void run() throws IOException, InterruptedException, NotYetSupportedException, InfiniteLoopException, ObsoleteException {
		System.out.println("Instantiating variables...");
		//Variables
		int numUsers = 40;
		int numDays = 10; //If set to ~40, 0 people sick on last day crashes Simulation
		String town = "Natick";
		String state =  "MA";
		
		int minFriends = 2;
		int maxFriends = 5;
		int hubNumber = 0;
		Random random = new Random();
		int initiallySick;
		int initiallyVacc;
		int percentSick = 10;
		int totalDays = numDays + 10 - 1;
		
		int getWellDays = 3;
		int getVac = 0;
		int curfewDays = 0;
		int runtimes = 100;
		int percentCurfew = 0;
		int overallRTs = 10;
		
		// Create MoreMethods instance
		MoreMethods methods = new MoreMethods();
		
		double[] finalResults = new double[totalDays];
		for (int runTime = 0; runTime < overallRTs; runTime++) {
		
			System.out.println("\n\n");
			System.out.println("Creating simulated data...");
			//Begin by Simulating Data
			DataSimulator dataSim = new DataSimulator();
			int[][] simulatedData = dataSim.run(numUsers, numDays, town, state);
			
			System.out.println("\n\n");
			System.out.println("Obtaining town census data...");
			// Create a network
			GetData gd = new GetData();
			gd.run(town, state);
			int[] hs = gd.hs;
			double[] ha = gd.ha;
			double[] ages = gd.ages;
			
			System.out.println("\n\n");
			System.out.println("Creating model town...");
			ModelTown modelTown = new ModelTown("Scale-Free", minFriends, maxFriends, hubNumber, random, hs, ha, ages);
	
			// Initiate Lists
			ArrayList<Person> people = modelTown.getPeople();
			
			System.out.println("\n\n");
			System.out.println("Getting infection rate...");
			// Get infection rate
			Simulator infectionRate = new Simulator(numUsers, minFriends, maxFriends);
			float p = infectionRate.run(simulatedData[1]);
	//		System.out.println(p);
	//		System.out.println("-------------------------------------------------------------------------------------------");
			
			// Place holder for when Dyushka comes back and helps us out with Z
			percentSick = (int)p;
			System.out.println("percentSick: " + p);
			
			System.out.println("\n\n");
			// Modify ArrayLists to fit requirements
			System.out.println("Modifying/scaling data...");
			float numPeople = people.size();
			float suceptible = simulatedData[0][numDays - 1];
			float infected = simulatedData[1][numDays - 1];
			float recovered = simulatedData[2][numDays - 1];
			float denominator = suceptible + infected + recovered;
			
	//		System.out.println("PPLSIZE: " + people.size());
	//		System.out.println("S: " + suceptible);
	//		System.out.println("I: " + infected);
	//		System.out.println("R: " + recovered);
			int suceptibleN = (int)(numPeople * suceptible/denominator);
			initiallySick = (int)(numPeople * infected/denominator);
			initiallyVacc = (int)(numPeople * recovered/denominator);
			
	//		System.out.println("Suc: " + suceptibleN);
	//		System.out.println("initiallySick: " + initiallySick);
	//		System.out.println("initiallyVacc: " + initiallyVacc);
			
			System.out.println("\n\n");
			System.out.println("Running simulation...");
			//System.out.println("InitiallySick is "  + initiallySick);
			//System.out.println("InitiallyVacc is "  + initiallyVacc);
			//Run Sims Using last entry of simulated Data/Dyushka's thing
			InfoJungStorage results = methods.simulateForSniffleForecast(people, getWellDays, initiallySick, initiallyVacc, percentSick, getVac, runtimes, true);
			
			//Make Universal Timeline Table
			ArrayList<ArrayList<Integer>> finalTable = finalTable(simulatedData, results, numDays, methods, people, denominator);
			finalTable.get(11).set(1, (finalTable.get(10).get(1) + finalTable.get(12).get(1)) / 2); // Smooth out bump
			for (int i = 0; i < finalTable.size(); i++) {
				System.out.println(i + ": " + finalTable.get(i));
			}
			int totalPeople = finalTable.get(0).get(0) + finalTable.get(0).get(1) + finalTable.get(0).get(2);
			System.out.println("totalPeople: " + totalPeople);
			System.out.println(finalTable);
			
			for (int i = 0; i < totalDays; i++) {
				try {
					finalResults[i] += ((double)finalTable.get(i).get(1)) / totalPeople;
				}
				catch (IndexOutOfBoundsException e) {
					finalResults[i] += 0;
				}
			}
			
			System.out.println(runTime + "-----------------------------------------------");
		}
		
		System.out.println("\n\n");
		System.out.println("Generating graph...");
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries series = new XYSeries("% of People Infected");
		dataset.addSeries(series);
		for (int i = 0; i < totalDays; i++) {
			MoreMethods.addPoint(series, i, finalResults[i] / overallRTs);
		}
		
		MoreMethods.makeChartSniffle(dataset, "sniffleResults", "Number of Sick People", "Day", "% of People Infected");
		
//		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
//		for (int i = 0; i < finalTable.size(); i++) {
//			MoreMethods.addPoint(dataset, finalTable.get(i).get(1), "Infected People", Integer.toString(i));
//		}
//		MoreMethods.makeChart(dataset, "sniffleResults", "Number of Sick People", "Days", "Infected People");
		
		System.out.println("DONE!!!");
	}
	static ArrayList<ArrayList<Integer>> finalTable(int[][] simulatedData, InfoJungStorage results, int numDays, MoreMethods methods, ArrayList<Person> people, float denominator){
		ArrayList<ArrayList<Integer>> table = new ArrayList<ArrayList<Integer>>();
		for(int i = 0; i < numDays; i++){
			table.add(new ArrayList<Integer>());
		}
		//Work with simulatedData
		for(int[] SIR : simulatedData){//Iterates through day by day S,I, and R lists
			int day = 0;
			for(int dayValue: SIR){
				table.get(day).add(Math.round(dayValue/denominator * people.size()));
				day++;
			}
		}
		//Work with InfoJungStorageResults
		ArrayList<ArrayList<InfoStorage>> infoStorages = results.getInfoStorages();
		ArrayList<InfoStorage> averageInfoStorages = methods.averagedInfostorageLog(infoStorages);
		for(InfoStorage dayEntry : averageInfoStorages){
			ArrayList<Integer> day = new ArrayList<Integer>(); 
			day.add((int) (people.size() - dayEntry.getImmune() - dayEntry.getNumSick()));
			day.add((int) dayEntry.getNumSick());
			day.add((int) dayEntry.getImmune());
			table.add(day);
		}
		return table;
	}
}
