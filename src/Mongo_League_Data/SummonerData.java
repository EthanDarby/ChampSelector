package Mongo_League_Data;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Id;


public class SummonerData {
	@Id 
	public ObjectId id;
	
	public String summonerName;
	public long summonerID;
}
