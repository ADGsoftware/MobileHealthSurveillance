package masterdiseasesimulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import datacontainers.InfoJungStorage;
import datacontainers.InfoStorage;
import infectionrate.Simulator;
import moremethods.GetData;
import moremethods.MoreMethods;
import simulateddata.DataSimulator;
public class SniffleForecast {
	public static void run() throws IOException, InterruptedException {
		//Variables
		int numUsers = 40;
		int numDays = 10;
		String town = "Needham";
		String state =  "MA";
		
		int minFriends = 2;
		int maxFriends = 5;
		int hubNumber = 0;
		Random random = new Random();
		int initiallySick;
		int initiallyVacc;
		int percentSick;
		
		int getWellDays = 3;
		int getVac = 0;
		int curfewDays = 0;
		int runtimes = 100;
		int percentCurfew = 0;
		
		// Create MoreMethods instance
		MoreMethods methods = new MoreMethods();
		
		//Begin by Simulating Data
		DataSimulator dataSim = new DataSimulator();
		int[][] simulatedData = dataSim.run(numUsers, numDays, town, state);
		
		// Get infection rate
		Simulator infectionRate = new Simulator(numUsers);
		float p = infectionRate.run(simulatedData[1]);
		System.out.println(p);
		System.out.println("-------------------------------------------------------------------------------------------");
		
		// Place holder for when Dyushka comes back and helps us out with Z
		percentSick = 10;
		
		// Create a network
		GetData gd = new GetData();
		gd.run(town, state);
		int[] hs = gd.hs;
		double[] ha = gd.ha;
		double[] ages = gd.ages;
		
		ModelTown modelTown = new ModelTown("Scale-Free", minFriends, maxFriends, hubNumber, random, hs, ha, ages);
		
		// Initiate Lists
		ArrayList<Person> people = modelTown.getPeople();
		
		// Modify ArrayLists to fit requirements
		float numPeople = people.size();
		float suceptible = simulatedData[0][numDays - 1];
		float infected = simulatedData[1][numDays - 1];
		float recovered = simulatedData[2][numDays - 1];
		float denominator = suceptible + infected + recovered;
		
		System.out.println("PPLSIZE: " + people.size());
		System.out.println("S: " + suceptible);
		System.out.println("I: " + infected);
		System.out.println("R: " + recovered);
		int suceptibleN = (int)(numPeople * suceptible/denominator);
		initiallySick = (int)(numPeople * infected/denominator);
		initiallyVacc = (int)(numPeople * recovered/denominator);
		
		System.out.println("Suc: " + suceptibleN);
		System.out.println("initiallySick: " + initiallySick);
		System.out.println("initiallyVacc: " + initiallyVacc);
		
		//System.out.println("InitiallySick is "  + initiallySick);
		//System.out.println("InitiallyVacc is "  + initiallyVacc);
		//Run Sims Using last entry of simulated Data/Dyushka's thing
		InfoJungStorage results = methods.simulateForSniffleForecast(people, getWellDays, initiallySick, initiallyVacc, percentSick, getVac, runtimes, true);
		
		//Make Universal Timeline Table
		ArrayList<ArrayList<Integer>> finalTable = finalTable(simulatedData, results, numDays, methods, people, denominator);
		System.out.println(finalTable);
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
