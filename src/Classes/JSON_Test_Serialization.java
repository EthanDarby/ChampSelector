package Classes;

import java.util.ArrayList;
import java.util.List;



import com.google.gson.*;

public class JSON_Test_Serialization {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		TierDetails testDetails = new TierDetails();
		
		
		
		TierSummary testSummary = new TierSummary();
		
		testSummary.name = "Bronze";
		
		//objects to put in list
		WinRate champ1 = new WinRate();
		champ1.champName = "Teemo";
		champ1.winPercentage = 85.3;
		
		WinRate champ2 = new WinRate();
		champ2.champName = "Garen";
		champ2.winPercentage = 80.6;
		
		WinRate champ3 = new WinRate();
		champ3.champName = "Annie";
		champ3.winPercentage = 76.8;
		
		testSummary.topChamps.add(champ1);
		testSummary.topChamps.add(champ2);
		testSummary.topChamps.add(champ3);
		
		List<TierSummary> list = new ArrayList<TierSummary>();
		list.add(testSummary);
		list.add(testSummary);
		list.add(testSummary);
		
		
		
		
		
		
		WinRate testWinRate = new WinRate();
		
		
		testDetails.name = "Silver";
		testDetails.topChamps = testSummary.topChamps;
		testDetails.worstChamps = testSummary.topChamps;
		testDetails.averageCsPerGame = 98.5;
		testDetails.averageDragonsPerGame = 2.3;
		testDetails.averageBaronsPerGame = 0.7;
		testDetails.averageGameTime = 10.5;
		testDetails.averageKillsPerGame = 45;
		testDetails.averageDeathsPerGame = 23;
		testDetails.strongestLane = "Mid";
		testDetails.averageFirstBaronGameTime = 8.50;
		testDetails.averageFirstBaronGameTime = 20.5;
		testDetails.averageWardsPlacedPerGame = 35;
		testDetails.averageRiftHeraldsPerGame = 0.4;
		
		
		System.out.println(new Gson().toJson(testDetails));

	}

}
