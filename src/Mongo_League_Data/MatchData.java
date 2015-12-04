package Mongo_League_Data;

import org.bson.types.ObjectId;

import org.mongodb.morphia.annotations.Id;

import dto.Match.MatchDetail;

public class MatchData{

	@Id 
	public ObjectId id;
	
	public long matchID;
	
	public MatchDetail details;
	
}
