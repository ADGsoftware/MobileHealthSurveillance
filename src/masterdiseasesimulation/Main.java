package masterdiseasesimulation;

public class Main {
	//private static boolean music = Math.random() < 1; //Random chance of music!

	public static void main(String[] args) throws Exception {
		int numUsers = 40;
		int numDays = 10;
		String town = "Needham";
		String state = "MA";
		int[][] arrays = simulateddata.Main.run(numUsers, numDays, town, state);
		
		//Here are these variables
		int initiallySick = arrays[1][arrays[1].length - 1];
		int initiallyVacc = arrays[2][arrays[2].length - 1];
		//System.out.println(initiallySick);
		//System.out.println(initiallyVacc);
		
		//INSERT DYUSHAS PROGRAM TO GET percentSick HERE
		int percentSick = 10; //For now
		
		///GRISHA HAVE FUN :)
		//ManyLinesAverageObject.run();
	}
}