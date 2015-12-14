package Classes;

import org.bson.types.ObjectId;

import org.mongodb.morphia.annotations.Id;

import dto.Match.MatchDetail;

public class MatchData{

	@Id 
	public ObjectId id;
	
	public long matchID;
	public boolean hasBeenAnalyzed;
	public MatchDetail details;
	
	public MatchData(){
		
		this.hasBeenAnalyzed = false;
	}
	
}
