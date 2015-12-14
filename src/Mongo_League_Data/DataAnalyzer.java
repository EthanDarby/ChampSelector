package Mongo_League_Data;

import java.util.Date;
import java.io.Console;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.gson.*;

import constant.Region;
import dto.Champion.Champion;
import dto.Match.MatchDetail;
import dto.Match.Participant;
import dto.Match.ParticipantIdentity;
import dto.Match.ParticipantStats;
import dto.Match.ParticipantTimeline;
import dto.Match.Player;
import dto.MatchList.MatchList;
import dto.MatchList.MatchReference;
import dto.Static.ChampionList;
import dto.Stats.AggregatedStats;
import dto.Stats.ChampionStats;
import dto.Stats.RankedStats;
import dto.Summoner.*;
import main.java.riotapi.*;

import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.BulkWriteResult;
import com.mongodb.Cursor;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ParallelScanOptions;
import com.mongodb.ServerAddress;

import Classes.ChampionData;
import Classes.MatchData;
import Classes.Matchup;
import Classes.PlayerDetails;

import org.bson.Document;
import org.mongodb.morphia.*;
import org.mongodb.morphia.query.Query;



/** Class for doing various analysis on the data contained in the database.
 * 
 * @author EthanDarby
 *
 */
public class DataAnalyzer {
	
	public void analyzeTierDataFrommatch(MatchDetail detailsIn){
		
	}
	
	public void analyzePlayerDataFromMatch(MatchDetail detailsIn){		
		
	}
	
	
	
	public void analyzeChampionDataFromMatch(MatchDetail detailsIn){
		//Go through each champion and update their tables appropriately
		List<Participant> participants = detailsIn.getParticipants();
		
		for(Participant participant:participants){
			//get champ name from database
			int championId = participant.getChampionId();
			int teamId = participant.getTeamId();
			long kills = 0;
			long deaths = 0;
			
			String lane = participant.getTimeline().getLane();
			
			int counterId = 0;
			long counterKills = 0;
			long counterDeaths = 0;
			
			ParticipantTimeline timelineData = participant.getTimeline();
			ParticipantStats stats = participant.getStats();
			boolean results = stats.isWinner();
			kills = stats.getKills();
			deaths = stats.getDeaths();
			
			//find the counter
			
			for(Participant counter:participants){
				String counterLane = counter.getTimeline().getLane();
				int counterTeam = counter.getTeamId();
				if(counterLane.equals(lane) && counterTeam != teamId){
					System.out.println("Found counter");
					counterId = counter.getChampionId();
					counterKills = counter.getStats().getKills();
					counterDeaths = counter.getStats().getDeaths();
				}
			}
			
			if(counterId == 0){
				System.out.println("NO COUNTER FOUND FOR: ");
				System.out.println(championId + " - " + lane);
				System.out.println("Why?");
			}
			else{
				//now that we have the champ and counter, lets get their names and print the data
				MongoClient mongoClient = new MongoClient();
				DB database = mongoClient.getDB("LeagueData");
				DBCollection matchDataCollection = database.getCollection("ChampionData");
				DBCursor champ = matchDataCollection.find(new BasicDBObject("champId",championId));
				DBCursor counter = matchDataCollection.find(new BasicDBObject("champId",counterId));
				String champName = champ.next().get("name").toString();
				String counterName = counter.next().get("name").toString();
			
				System.out.println("SUMMARY FOR A CHAMPION");
				System.out.println(champName + " vs " + counterName);
				System.out.println("Kills: " + kills + " - " + counterKills);
				System.out.println("Deaths: " + deaths + " - " + counterDeaths);
				System.out.println("Results: " + results + " - " + !results);
			
				System.out.println("Finished");
			
			
			}
		}
	}
	
	
	/** Analyzes all the matches in the database 
	 * @param None
	 * @return None
	 */
	@SuppressWarnings("all")
	public void analyzeTierData(){
		MongoClient mongoClient = new MongoClient();
		DB database = mongoClient.getDB("LeagueData");
		DBCollection matchDataCollection = database.getCollection("MatchData");
		DBCursor cursor  = matchDataCollection.find();
		Morphia morphia = new Morphia();
		Datastore ds = morphia.createDatastore(mongoClient,"LeagueData");
		morphia.map(MatchData.class);
		
		//iterate through each matchData object and save what needs to be saved
		int i = 0;
		System.out.println("Starting matchData loop");
		int pre2015Matches = 0;
		int season2015Matches = 0;
		int pre2016Matches = 0;
		try{
		while(cursor.hasNext()){
			DBObject temp = cursor.next();
			
			MatchData match = morphia.fromDBObject(MatchData.class, temp);
			System.out.println("MatchId: " + match.matchID);
			MatchDetail details = match.details;
			String season = null;
			season = details.getSeason();
			
			switch (season){
			
			case "PRESEASON2015":
				pre2015Matches++;
				break;
			
			case "SEASON2015":   
				season2015Matches++;
				break;
				
			case "PRESEASON2016":
				//do analysis on current matches
				pre2016Matches++;
				this.analyzeChampionDataFromMatch(details);
			
				break;
			
			}
			//1) Save the info for each player
			//2) Save the info for each champion
			//3) Save the info for each tier
			//4) Mark as analyzed
			i++;
			
		}
		}
		catch(Exception error){
			System.out.println("Error");
			System.out.println(error.getMessage());
		}
		
		System.out.println("Total Matches Analyzed: " + i);
		System.out.println("Preseason 2015: " + pre2015Matches);
		System.out.println("Season 2015: " + season2015Matches);
		System.out.println("Preseason 2016: " + pre2016Matches);
	}
	
	/** Analyzes the match that is sent in and saves relevant data to the database.
	 * 
	 * @param matchIn - The match data object from the database that needs to be analyzed 
	 * @return None
	 */
	public void analyzeMatchData(MatchData matchIn){
		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DataAnalyzer driver = new DataAnalyzer();
		driver.analyzeTierData();
	}

}
