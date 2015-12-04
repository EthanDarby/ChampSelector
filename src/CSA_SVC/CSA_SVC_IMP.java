


package CSA_SVC;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import com.google.gson.*;

import constant.Region;
import dto.Match.MatchDetail;
import dto.MatchList.MatchList;
import dto.MatchList.MatchReference;
import dto.Stats.AggregatedStats;
import dto.Stats.ChampionStats;
import dto.Stats.RankedStats;
import dto.Summoner.*;
import main.java.riotapi.*;

import CSA_SVC.Champion;


/*
 * This is the implementation of the champion select analyzer service
 */
public class CSA_SVC_IMP {
	

	public static void main(String[] args) {
		
		CSA_SVC_IMP analyzer = new CSA_SVC_IMP();
		//let's get a summoner name from the command line and send it to rito for some data
		Scanner keyboard = new Scanner(System.in);
		System.out.print("Enter a summoner name to retrieve data for: ");
		String summonerName = keyboard.nextLine();
		
		RiotApi api = new RiotApi("2c6decef-0974-4fda-b5d1-d0470cab8a89");
		
		Summoner summoner = null;
		
		try {
			summoner = api.getSummonerByName(Region.NA, summonerName);
			long _id = summoner.getId();
			
			MatchList list = new MatchList();
			list = api.getMatchList(_id);
			
			System.out.println("\nSummoner: " + summonerName);
			System.out.println("ID: " + _id);
			System.out.println("Total Matches Played: " + list.getTotalGames());
			
			List<MatchReference> matches = list.getMatches();
			
			
			int topCounter = 0;
			int midCounter = 0;
			int jgCounter = 0;
			int botCounter = 0;
			
			//get list of all the ranked stats
			RankedStats rankedStats = api.getRankedStats(_id);
			
			//now get a list of the champions played
			List<ChampionStats> champList = rankedStats.getChampions();
			
			List<Champion> bestChamps = analyzer.getTopChampsForSummoner(champList);
			
			for(MatchReference match:matches){
			
				String lane = match.getLane();
				long matchId = match.getMatchId();
				
				switch(lane){
				
					case "MID":
							midCounter++;
						break;
						
					case "MIDDLE":
						midCounter++;
						break;
					
					case "TOP":
						topCounter++;
						break;
						
					case "JUNGLE":
						jgCounter++;
						break;
						
					case "BOT":
						botCounter++;
						break;
						
					case "BOTTOM":
						botCounter++;
						break;
						
						
					default:
						
						break;
							
				}				
				
			}
			
			
			String results = null;
			
			double total = matches.size();
			
			results = "Percentage played each role:\n";
			results += "Top: " + (float)((topCounter / total) * 100) + "% \n";
			results += "Mid: " + (float)((midCounter / total)* 100) + "% \n";
			results += "Jungle: " + (float)((jgCounter / total)* 100) + "% \n";
			results += "Bot : " + (float)((botCounter / total)* 100) + "% \n";
			
			results += "------------------\n";
			results += "Best Champions: \n";
			for(Champion champ:bestChamps){
				results += "\nName: " + champ.id;
				results += "\nTotal Soloque: " + champ.totalSoloQueGames;
				results += "\nOverall W/L: " + champ.winLossRatio;
				results += "\nKills: " + champ.kills;
			}
			
			System.out.println(results);
		} catch (RiotApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	public int countOccurences(List<Long> listIn, Long elementIn){
		int counter = 0;
		
		for(Long element:listIn){
			if(element == elementIn){
				counter++;
			}
		}
		
		return counter;
	}

	
	public List<Champion> getTopChampsForSummoner(List<ChampionStats> listIn){
		
		List<Champion> topChamps = new ArrayList<Champion>();
		
		for(ChampionStats champ:listIn){
			Champion tempChampObject = new Champion();
			AggregatedStats champStats = champ.getStats();
			
			tempChampObject.id = champ.getId();
			tempChampObject.deathsPerSession = champStats.getAverageNumDeaths();
			tempChampObject.totalSoloQueGames = champStats.getRankedSoloGamesPlayed();
			if(champStats.getTotalSessionsLost() > 0){
				tempChampObject.winLossRatio= ((float)champStats.getTotalSessionsWon() / (float)champStats.getTotalSessionsLost());
				
			}
					
			else{
				tempChampObject.winLossRatio = (float) champStats.getTotalSessionsWon();
			}
			tempChampObject.kills = champStats.getTotalChampionKills();
			topChamps.add(tempChampObject);
			System.out.println(champStats.getRankedSoloGamesPlayed());
		}
		
		//sort the list based on the total games played, and then on win loss
		Collections.sort(topChamps, new CompareByTotalSoloqueGames());
		if(topChamps.size() > 5){
			topChamps = topChamps.subList(topChamps.size() - 5, topChamps.size() - 1);
		}
		
		Collections.sort(topChamps, new CompareByWinLossRatio());
		
		return topChamps;
	}
	
}


