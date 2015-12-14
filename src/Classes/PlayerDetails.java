package Classes;

import java.util.List;

public class PlayerDetails {
	
	String summonerName, tier, division; 
	long summonerId, kills, deaths;
	double KDR, WL, CSPerGame;
	List<WinRate> topChamps;
	List<WinRate> worstChamps;
	
	public PlayerDetails(){
		
		this.summonerName = "none";
		this.summonerId = 0;
		this.kills = 0;
		this.deaths = 0;
		this.KDR = 0.0;
		this.WL = 0.0;
		this.CSPerGame = 0.0;
		
	}
	
}
