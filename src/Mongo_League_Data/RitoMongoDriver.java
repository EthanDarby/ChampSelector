package Mongo_League_Data;

import java.util.Date;
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
import dto.Match.MatchDetail;
import dto.Match.ParticipantIdentity;
import dto.Match.Player;
import dto.MatchList.MatchList;
import dto.MatchList.MatchReference;
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

import org.mongodb.morphia.*;


public class RitoMongoDriver {
	RiotApi api = null;

	int callCounter = 0;
	Date startTime;
	
	public RitoMongoDriver() {
		api = new RiotApi("2c6decef-0974-4fda-b5d1-d0470cab8a89");
		startTime = new java.util.Date();
	}

	public static void main(String[] args) {
		
		Scanner keyboard = new Scanner(System.in);
		String summonerName = null;		
		Summoner summonerSeed = null;
		RitoMongoDriver driver = new RitoMongoDriver();
		
		
		System.out.println("Enter a summoner name: ");
		summonerName = keyboard.nextLine();
		
		try {
			summonerSeed = driver.api.getSummonerByName(summonerName);
			driver.callCounter++;
		} catch (RiotApiException e) {
			e.printStackTrace();
		}
		
		
		
		System.out.println("Starting to gather summonerids...");
		try {
			driver.getIDs(summonerSeed, driver.api);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
public void insertMatchDetails(MatchDetail detailsIn, MongoClient clientIn){
	Morphia morphia = new Morphia();
	Datastore ds = morphia.createDatastore(clientIn,"MatchDetails");
	
	morphia.map(MatchData.class);
	MatchData matchData = new MatchData();
	matchData.details = detailsIn;
	matchData.matchID = detailsIn.getMatchId();
	
	ds.save(matchData);
	}



public void insertSummonerDetails(SummonerData summdetailsIn, MongoClient clientIn){
	Morphia morphia = new Morphia();
	Datastore ds = morphia.createDatastore(clientIn,"SummonerDetails");
	
	morphia.map(SummonerData.class);
	
	
	ds.save(summdetailsIn);
}

public void checkRateLimit(){
	Date now = new Date();
	System.out.println("Checking Rate Limit");
	System.out.println("CallCounter: " + this.callCounter);
	long differenceInSeconds = (now.getTime() - this.startTime.getTime()) / 1000;
	System.out.println("Time since start: " + differenceInSeconds);
	if((this.callCounter % 10) >= 8){
		if((differenceInSeconds & 10) <= 8){
			System.out.println("Sleeping for 10 seconds");
			try {
				TimeUnit.SECONDS.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	if((this.callCounter % 500) > 490){
		if((differenceInSeconds & 600) < 599){
			System.out.println("Sleeping for " + (650 - differenceInSeconds) + " seconds");
			try {
				//find the remaining time left until 10 minutes
				TimeUnit.MINUTES.sleep(650 - differenceInSeconds);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
}
	
	public void getIDs(Summoner seedIn, RiotApi apiIn) throws InterruptedException{
		
		int apiCallCounter = 0;
				
		//MONGO INITIALIZATION
		MongoClient mongoClient = new MongoClient();
		DB database = mongoClient.getDB("LeagueData");
		DBCollection collection_summonerIDs = database.getCollection("SummonerIDs");
		DBCollection collection_matchIDs = database.getCollection("MatchIDs");
		DBCollection collection_seedSearchIDs = database.getCollection("SeedSearchIDs");
		DBCollection collection_match_details = database.getCollection("MatchDetails");
		
		//now that we have a connection to our local database, lets get a list of summonerIDs
		try {
			
			//1.) Check to see if we have used this id to seed the data first
			long seedID = seedIn.getId();
			BasicDBObject query_For_Seed_ID = new BasicDBObject("id", seedID);
			if(collection_summonerIDs.count(query_For_Seed_ID) == 0){
				
				//SUMMONER LEVEL
				//this hasn't been used as a seed before, so save it to the seed database and move on
				collection_seedSearchIDs.insert(query_For_Seed_ID);
				
				//now use this seed to get other ids and ALL MATCH DATA
				//RITO CALL
				//rate limit detection
				this.checkRateLimit();
				MatchList matches = apiIn.getMatchList(seedIn.getId());
				this.callCounter++;
				//iterate through the list of matches
				for(MatchReference match: matches.getMatches()){
					
					//INDIVIDUAL MATCH LEVEL
					
					//check to see if we already have this matchID in the database, avoid a rito call if we do
					BasicDBObject query_For_Match_ID = new BasicDBObject("matchId", match.getMatchId());
					if(collection_matchIDs.count(query_For_Match_ID) == 0){
						//not in the database, so add it before saving all it's data
						collection_matchIDs.insert(query_For_Match_ID);
						
						//now, get the match details from rito
						//RITO CALL
						this.checkRateLimit();
						MatchDetail matchDetails = apiIn.getMatch(match.getMatchId());
						this.callCounter++;
						//now store the entire massive object in our database
						this.insertMatchDetails(matchDetails, mongoClient);
						
						//save the list of summoners associated with this match into our database
						List<ParticipantIdentity> participants = matchDetails.getParticipantIdentities();
						for(ParticipantIdentity participant: participants){
							Player player = participant.getPlayer();
							SummonerData newSumm = new SummonerData();
							newSumm.summonerID = player.getSummonerId();
							newSumm.summonerName = player.getSummonerName();
							
							BasicDBObject query_For_Summ_ID = new BasicDBObject("summonerID", newSumm.summonerID);
							//check to see if we have this summoner id, if we don't then save it
							if(collection_summonerIDs.count(query_For_Summ_ID) == 0){
								collection_summonerIDs.insert(query_For_Summ_ID);
								this.insertSummonerDetails(newSumm, mongoClient);
							}
							
						}
						
					}
				}
				
			}
			
			
			
			
			
			
			
		} catch (RiotApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Done!");
	}
	
	

}
