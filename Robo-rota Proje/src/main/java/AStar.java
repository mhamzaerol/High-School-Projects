import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Vector;

public class AStar {
	int mx,st;
	double[][] Matrix=new double[15][15],mMatrix=new double[15][15];
	double min;
	Vector<Integer> Answer=new Vector<Integer>();
	Comparator<AStarVar> com=new c();
	PriorityQueue<AStarVar> Queue=new PriorityQueue<AStarVar>(1,com);
	AStar(double[][] m,int a,int b) {
		AStarVar x=new AStarVar();
		double send=0;
		Matrix=m;st=a;mx=b;
		x.now=a;
		Arrays.fill(x.vis,0);
		for(int i=0;i<mx;i++) {
			for(int j=0;j<mx;j++) {
				mMatrix[i][j]=Matrix[i][j];
			}
			Arrays.sort(mMatrix[i],0,mx);
			send+=mMatrix[i][1]+mMatrix[i][2];
		}
		x.h=send;
		x.g=0;
		x.Route.addElement(st);
		x.vst=0;
		Queue.add(x);
		solve();
	}
	void solve() {
		while(!Queue.isEmpty()) {
			AStarVar a=Queue.poll();
			if(a.now==st && a.vst==mx) {
				Answer=a.Route;
				min=a.g/2;
				return ;
			}
			for(int i=0;i<mx;i++) {
				if(a.vis[i]==0 && a.now!=i) {
					AStarVar yedek=new AStarVar(a);
					yedek.vis[i]=1;
					yedek.Route.addElement(i);
					yedek.g+=2*Matrix[a.now][i];
					yedek.h-=mMatrix[a.now][2]+mMatrix[i][1];
					yedek.vst++;
					yedek.now=i;
					Queue.add(yedek);
				}
			}
		}
	}
}
