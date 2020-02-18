import java.util.Arrays;
import java.util.Vector;

public class Exh {
	boolean flag=false;
	int mx,st,vst=0;
	double[][] Matrix=new double[15][15];
	int[] vis=new int[15];
	Vector<Integer> Answer=new Vector<Integer>();
	Vector<Integer> Route=new Vector<Integer>();
	double min=1000000000;
	long tm;
	long p=0L;
	Exh(double[][] m,int a,int b) {
		Matrix=m;st=a;mx=b;
		Arrays.fill(vis,0);
		tm=System.currentTimeMillis();
		Route.addElement(st);
		Ex(st,0);
	}
	void Ex(int node,double cost) {
		if(flag) return ;
		p=System.currentTimeMillis()-tm;
		if(p>5*60*1000) flag=true;
		if(node==st && cost>0) {
			if(vst==mx && cost<min) {
				min=cost;
				Answer.clear();
				for(int i=0;i<Route.size();i++) {
					Answer.addElement(Route.elementAt(i));
				}
			}
			return ;
		}
		for(int i=0;i<mx;i++) {
			if(vis[i]==0) {
				vis[i]=1;
				vst++;
				Route.addElement(i);
				Ex(i,cost+Matrix[node][i]);
				Route.removeElementAt(Route.size()-1);
				vst--;
				vis[i]=0;
			}
		}
	}
}
