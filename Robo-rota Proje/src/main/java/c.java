import java.util.Comparator;

public class c implements Comparator<AStarVar> {
	public int compare(AStarVar x,AStarVar y) {
		if(x.g+x.h<y.g+y.h) return -1;
		else return 1;
	}
}