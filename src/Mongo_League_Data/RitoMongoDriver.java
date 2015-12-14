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

import Classes.MatchData;
import Classes.SummonerData;

import org.mongodb.morphia.*;


public class RitoMongoDriver {
	RiotApi api = null;

	int callCounter = 0;
	Date startTime;
	Date last10SecSleep;
	Date last10MinSleep;
	
	MongoClient mongoClient;
	DB database;
	DBCollection collection_matchIDs;
	DBCollection collection_seedSearchIDs;
	DBCollection collection_match_details;
	DBCollection collection_summonerData;
	DBCollection collection_matchData;
	
	public RitoMongoDriver() {
		api = new RiotApi("2c6decef-0974-4fda-b5d1-d0470cab8a89");
		startTime = new java.util.Date();
		last10SecSleep = new Date();
		last10MinSleep = new Date();
		
		
		//MONGO INITIALIZATION
		mongoClient = new MongoClient();
		database = mongoClient.getDB("LeagueData");
		collection_matchIDs = database.getCollection("MatchIDs");
		collection_seedSearchIDs = database.getCollection("SeedSearchIDs");
		collection_match_details = database.getCollection("MatchDetails");
		collection_summonerData = database.getCollection("SummonerData");
		collection_matchData = database.getCollection("MatchData");
	}

	public static void main(String[] args) {
		
		Scanner keyboard = new Scanner(System.in);
		String summonerName = null;		
		Summoner summonerSeed = null;
		RitoMongoDriver driver = new RitoMongoDriver();
		
		
		System.out.println("Beginning to scrape Rito servers for data...");
		
		DBCursor cursor = driver.collection_summonerData.find();
		
		while(cursor.hasNext()){
			
			DBObject currentID = cursor.next();
			long summonerID = (long) currentID.get("summonerID");
			

			System.out.println("Starting to gather data for: " + (String) currentID.get("summonerName") + " - " + summonerID);
			try {
				driver.getIDs(summonerID, driver.api);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			
		}
		
	

	}
	
	
public void insertMatchDetails(MatchDetail detailsIn){
	Morphia morphia = new Morphia();
	Datastore ds = morphia.createDatastore(this.mongoClient,"LeagueData");
	
	morphia.map(MatchData.class);
	MatchData matchData = new MatchData();
	matchData.details = detailsIn;
	matchData.matchID = detailsIn.getMatchId();
	
	ds.save(matchData);
	}



public void insertSummonerDetails(SummonerData summdetailsIn){
	Morphia morphia = new Morphia();
	Datastore ds = morphia.createDatastore(this.mongoClient,"LeagueData");
	
	morphia.map(SummonerData.class);
	
	
	ds.save(summdetailsIn);
}

public void checkRateLimit(){
	Date now = new Date();
	System.out.println("Checking Rate Limit");
	System.out.println("CallCounter: " + this.callCounter + "---> " + this.callCounter % 10);
	long differenceInSeconds = (now.getTime() - this.startTime.getTime()) / 1000;
	
	long timeSinceLast10SecSleep = (now.getTime() - this.last10SecSleep.getTime()) / 1000;
	long timeSinceLast10MinSleep = (now.getTime() - this.last10MinSleep.getTime()) / 1000;
	
	
	
	
	if((this.callCounter % 10) == 7 || (this.callCounter % 10) == 9){
		System.out.println("Time since last 10 Sec Sleep: " + timeSinceLast10SecSleep + "(sec)");
		if(true){
			System.out.println("Sleeping for" + (10 - timeSinceLast10SecSleep) + " seconds");
			try {
				
				TimeUnit.SECONDS.sleep(10 - timeSinceLast10SecSleep);
				this.last10SecSleep = new Date();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	if((this.callCounter % 500) > 490){
		System.out.println("Time Since last 10 Min Sleep: " + timeSinceLast10MinSleep + "(sec)");
		if((timeSinceLast10MinSleep & 600) < 599){
			System.out.println("Sleeping for " + (650 - timeSinceLast10MinSleep) + " seconds");
			try {
				//find the remaining time left until 10 minutes
				
				TimeUnit.MINUTES.sleep(650 - differenceInSeconds);
				this.last10MinSleep = new Date();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}

public boolean doesSummonerExist(SummonerData summIn){
	boolean doesExist = false;
	
	BasicDBObject query_For_Summ_ID = new BasicDBObject("summonerID", summIn.summonerID);
	long result = this.collection_summonerData.count(query_For_Summ_ID);
	
	if(result > 0){
		doesExist = true;
	}
	
	return doesExist;
}
	
public boolean doesMatchExist(long matchId){
	boolean matchExists = false;
	
	BasicDBObject query_For_Match_ID = new BasicDBObject("matchID", matchId);
	
	long result = this.collection_matchData.count(query_For_Match_ID);
	
	if(result > 0){
		matchExists = true;
	}
	
	return matchExists;
}


	public void getIDs(long seedIn, RiotApi apiIn) throws InterruptedException{
		
		int apiCallCounter = 0;
						
		//now that we have a connection to our local database, lets get a list of summonerIDs
		try {
			
			//1.) Check to see if we have used this id to seed the data first
			long seedID = seedIn;
			BasicDBObject query_For_Seed_ID = new BasicDBObject("seedSummId", seedID);
			if(this.collection_seedSearchIDs.count(query_For_Seed_ID) == 0){
				
				//SUMMONER LEVEL
				//this hasn't been used as a seed before, so save it to the seed database and move on
				this.collection_seedSearchIDs.insert(query_For_Seed_ID);
				
				//now use this seed to get other ids and ALL MATCH DATA
				//RITO CALL
				//rate limit detection
				this.callCounter++;
				this.checkRateLimit();
				MatchList matches = apiIn.getMatchList(seedIn);
				
				System.out.println("Played " + matches.getTotalGames() + " games.");
				
				//iterate through the list of matches
				for(MatchReference match: matches.getMatches()){
					
					//INDIVIDUAL MATCH LEVEL
					
					//check to see if we already have this matchID in the database, avoid a rito call if we do
					
					if(!this.doesMatchExist(match.getMatchId())){
						
						System.out.println("Adding match: " + match.getMatchId() + " - " + match.getChampion() + " to database.");
						
						//not in the database, so add it before saving all it's data
						//collection_matchIDs.insert(query_For_Match_ID);
						
						//now, get the match details from rito
						//RITO CALL
						this.callCounter++;
						this.checkRateLimit();
						MatchDetail matchDetails = new MatchDetail();
						boolean limitExceeded = false;
						try{
							matchDetails = apiIn.getMatch(match.getMatchId());
						}
						
						catch(RiotApiException riotException){
							if(riotException.getErrorCode() == 429){
							  limitExceeded = true;
							}
						}
						
						if(limitExceeded){
							//try to sleep for 10 seconds and keep going
							TimeUnit.SECONDS.sleep(10);
							matchDetails = apiIn.getMatch(match.getMatchId()); 
						}
						
						//now store the entire massive object in our database
						this.insertMatchDetails(matchDetails);
						
						//save the list of summoners associated with this match into our database
						List<ParticipantIdentity> participants = matchDetails.getParticipantIdentities();
						for(ParticipantIdentity participant: participants){
							Player player = participant.getPlayer();
							SummonerData newSumm = new SummonerData();
							newSumm.summonerID = player.getSummonerId();
							newSumm.summonerName = player.getSummonerName();
							
							//check to see if we have this summoner id, if we don't then save it
							if(!this.doesSummonerExist(newSumm)){
								System.out.println("Adding summoner to database: " + newSumm.summonerName + " - " + newSumm.summonerID);
								this.insertSummonerDetails(newSumm);
							}
							
							else{
								System.out.println("Duplicate summoner detected: " + newSumm.summonerName + " - " + newSumm.summonerID + ". Not adding.");
							}
							
							
						}
						
					}
					
					else{
						System.out.println("Dublicate match detected: " + match.getMatchId() + ". Not adding.");
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
