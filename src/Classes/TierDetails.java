package Classes;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;

public class TierDetails {

	@Id 
	public ObjectId id;
	
	public String name;
	public List<WinRate> topChamps;
	public List<WinRate> worstChamps;
	public double averageCsPerGame;
	public double averageDragonsPerGame;
	public double averageBaronsPerGame;
	public double averageGameTime;
	public double averageKillsPerGame;
	public double averageDeathsPerGame;
	public String strongestLane;
	public double averageFirstDragonGameTime;
	public double averageFirstBaronGameTime;
	public double averageWardsPlacedPerGame;
	public int totalMatchesAnalyzed;
	public double averageRiftHeraldsPerGame;
	public long totalKills;
	public long totalDeaths;
	public long totalDragons;
	public long totalBarons;
	public long totalRiftHeralds;
	public long totalWardsPlaced;
	
	
	public TierDetails(){
		
		
		this.name = "none";
		this.averageCsPerGame =  0.0;
		this.averageDragonsPerGame = 0.0;
		this.averageBaronsPerGame = 0.0;
		this.averageGameTime = 0.0;
		this.averageKillsPerGame = 0.0;
		this.averageDeathsPerGame = 0.0;
		this.averageFirstDragonGameTime = 0.0;
		this.averageFirstBaronGameTime = 0.0;
		this.averageWardsPlacedPerGame = 0.0;
		this.totalMatchesAnalyzed = 0;
		this.averageRiftHeraldsPerGame = 0;
		this.totalBarons = 0;
		this.totalDeaths = 0;
		this.totalKills = 0;
		this.totalDragons = 0;
		this.totalWardsPlaced = 0;
		this.totalRiftHeralds = 0;                          
		
		this.topChamps = new ArrayList<WinRate>();
			WinRate blank = new WinRate();
			this.topChamps.add(blank);
		this.worstChamps = new ArrayList<WinRate>();
			this.worstChamps.add(blank);
		
	}
	
	
}
