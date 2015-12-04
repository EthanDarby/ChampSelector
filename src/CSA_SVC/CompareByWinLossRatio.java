package CSA_SVC;

import java.util.Comparator;

public class CompareByWinLossRatio implements Comparator<Champion> {

	public CompareByWinLossRatio() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compare(Champion o1, Champion o2) {		
		return Double.compare(o1.winLossRatio, o2.winLossRatio);
	}

}
