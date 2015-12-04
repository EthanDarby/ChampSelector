package CSA_SVC;

import java.util.Comparator;

public class CompareByTotalSoloqueGames implements Comparator<Champion>{

	public CompareByTotalSoloqueGames() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(Champion arg0, Champion arg1) {
		return Integer.compare(arg0.totalSoloQueGames, arg1.totalSoloQueGames);
	}

}
