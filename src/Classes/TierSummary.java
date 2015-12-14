package Classes;

import java.util.ArrayList;
import java.util.List;

public class TierSummary {
	
	public String name;
	public List<WinRate> topChamps;
	
	
	public TierSummary(){
		
		this.name = "none";
		this.topChamps = new ArrayList<WinRate>();
		
	}

}
