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
import dto.Match.ParticipantIdentity;
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
import Classes.Matchup;
import Classes.TierDetails;

import org.mongodb.morphia.*;

public class DatabaseSchemas {
	/**
	 * This method builds the ChampionData collection.
	 * This DOES NOT take into consideration any of the data present, and could/will overwrite data.
	 */
	public void buildChampionDataCollection(){
		//this is just a tool to build the collection, it is assuming that the collection either doesn't exist or is empty
		
		MongoClient mongoClient = new MongoClient();
		
		RiotApi api = new RiotApi("2c6decef-0974-4fda-b5d1-d0470cab8a89");
		List<ChampionData> allChampionsAndMatchups = new ArrayList<ChampionData>();
		try {
			ChampionList apidata = api.getDataChampionList();
			
		
			
			Map<String, dto.Static.Champion> champList = apidata.getData();
			Map<String, dto.Static.Champion> listCopy = champList;
			
			for(Map.Entry<String, dto.Static.Champion> entry: champList.entrySet()){
				ChampionData newChamp = new ChampionData();
				dto.Static.Champion temp = entry.getValue();
				newChamp.name = temp.getName();
				newChamp.champId = temp.getId();
				
				for(Map.Entry<String, dto.Static.Champion> entry_copy: listCopy.entrySet()){
					if(entry_copy.getKey() != entry.getKey()){
						Matchup newMatchup = new Matchup();
						dto.Static.Champion matchupData = entry_copy.getValue();
						
						newMatchup.name = matchupData.getName();
						newMatchup.champId = matchupData.getId();
						
						newChamp.matchups.add(newMatchup);
						
					}
				}
			allChampionsAndMatchups.add(newChamp);
			}
			
			System.out.println("Done building schema...");
			
			System.out.println("Total Champs: " + allChampionsAndMatchups.size());
			/*for(ChampionData champ:allChampionsAndMatchups){
				i++;
				System.out.println(i + ") " + new Gson().toJson(champ));
				System.out.println("Total Matchups: " + champ.matchups.size());
			}
			*/
			
		} catch (RiotApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Morphia morphia = new Morphia();
		Datastore ds = morphia.createDatastore(mongoClient,"LeagueData");
		
		morphia.map(ChampionData.class);
		
		
		ds.save(allChampionsAndMatchups);
		
		System.out.println("Done adding the ChampionData schema to mongo.");
	}
	
	/**
	 * This method builds the TierData collection.
	 * This DOES NOT take into consideration any of the data present, and could/will overwrite data.
	 */
	public void buildTierDataCollection(){
		
		MongoClient mongoClient = new MongoClient();
		RiotApi api = new RiotApi("2c6decef-0974-4fda-b5d1-d0470cab8a89");
		
		TierDetails bronzeLeague = new TierDetails();
		bronzeLeague.name = "Bronze";
		
		TierDetails silverLeague = new TierDetails();
		silverLeague.name = "Silver";
		
		TierDetails goldLeague = new TierDetails();
		goldLeague.name = "Gold";
		
		TierDetails platinumLeague = new TierDetails();
		platinumLeague.name = "Platinum";
		
		TierDetails diamondLeague = new TierDetails();
		diamondLeague.name = "Diamond";
		
		Morphia morphia = new Morphia();
		Datastore ds = morphia.createDatastore(mongoClient,"LeagueData");
		
		morphia.map(TierDetails.class);
		
		
		ds.save(bronzeLeague);
		ds.save(silverLeague);
		ds.save(goldLeague);
		ds.save(platinumLeague);
		ds.save(diamondLeague);
		
		System.out.println("Done adding TierData schema to mongo.");
	}
	
	public static void main(String[] args) {
		
		DatabaseSchemas driver = new DatabaseSchemas();
		
		System.out.println("//////////////////    DB SCHEMA MASTER PROGRAM     //////////////////");
		System.out.println("!! WARNING !!");
		System.out.println("This program may add, overwrite, or remove entire collections.");
		System.out.println("DO NOT PROCEED IF YOU DO NOT KNOW WHAT YOU ARE DOING");
		
		System.out.println("................");
		System.out.println("Main Menu");
		
		System.out.println("1) Build ChampionData collection - High Risk");
		System.out.println("2) Build TierData collection - High risk");
		System.out.println("9) Exit");
		
				
		Scanner keyboard = new Scanner(System.in);
		int selection = keyboard.nextInt();
		
		switch(selection){
		case 1:
			System.out.println("Please confirm wanting to build the ChampionData collection anew by entering 1 for yes.");
			int confirmationBuildChampData = keyboard.nextInt();
			if(confirmationBuildChampData == 1){
				driver.buildChampionDataCollection();
			}
			
			break;
			
			
		case 2:
			System.out.println("Please confirm wanting to build the TierData collection anew by entering 1 for yes.");
			int confirmationBuildTierData = keyboard.nextInt();
			if(confirmationBuildTierData == 1){
				driver.buildTierDataCollection();
			}
			break;
			
		case 9:
			System.out.println("Leaving...");
			break;
		
		default:
			System.out.println("Leaving...");
			break;
		
		}
		
		
		
	}
	
	
	

	
}
