import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;


public class Greedy {
	int mx,st;
	double[][] Matrix=new double[15][15];
	int[] vis=new int[15];
	Pair[][] mM=new Pair[15][15];
	Comparator<Pair> com=new q();
	Vector<Integer> Answer=new Vector<Integer>();
	double min=0;
	Greedy(double[][] m,int a,int b) {
		Matrix=m;st=a;mx=b;
		for(int i=0;i<mx;i++) {
			for(int j=0;j<mx;j++) {
				mM[i][j]=new Pair(Matrix[i][j],j);
			}
			Arrays.sort(mM[i],0,mx,com);
			vis[i]=0;
		}
		int node=st,vst=0;
		Answer.addElement(node);
		while(vst<mx-1) {
			System.out.println(node);
			vis[node]=1;
			for(int i=0;i<mx;i++) {
				if(vis[mM[node][i].Second]==0) {
					min+=mM[node][i].First;
					node=mM[node][i].Second;
					Answer.addElement(node);
					vst++;
					break ;
				}
			}
		}
		min+=Matrix[node][st];
		Answer.addElement(st);
	}
}
