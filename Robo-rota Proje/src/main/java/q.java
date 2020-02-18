import java.util.Comparator;

public class q implements Comparator<Pair> {
	public int compare(Pair x,Pair y) {
		if(x.First<y.First) return -1;
		else return 1;
	}
}