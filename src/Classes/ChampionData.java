package Classes;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;



public class ChampionData {
	@Id 
	public ObjectId id;
	
	public String name;
	public long champId;
	public List<Matchup> matchups;
	
	public ChampionData(){
		
		this.name = "none";
		this.champId = 0;
		this.matchups = new ArrayList<Matchup>();
	
	}
	
}
