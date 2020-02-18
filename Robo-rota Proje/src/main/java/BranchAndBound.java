import java.util.Arrays;
import java.util.Vector;

public class BranchAndBound {
	int st,mx,vst;
	double[][] Matrix=new double[15][15],MinM=new double[15][15];
	double min=1000000000;
	int[] vis=new int[15];
	Vector<Integer> Route=new Vector<Integer>();
	Vector<Integer> Answer=new Vector<Integer>();
	BranchAndBound(double[][] mt,int a,int b) {
		double send=0;
		Matrix=mt;st=a;mx=b;
		Arrays.fill(vis,0);
		for(int i=0;i<mx;i++) {
			for(int j=0;j<mx;j++) {
				MinM[i][j]=Matrix[i][j];
			}
			Arrays.sort(MinM[i],0,mx);
			send+=MinM[i][1]+MinM[i][2];
		}
		vst=0;
		Route.addElement(st);
		BB(st,0,send);
	}
	void BB(int node,double g,double h) {
		if(node==st && g>0) {
			if(vst==mx && g/2<min) {
				min=g/2;
				Answer.clear();
				for(int i=0;i<Route.size();i++)
					Answer.addElement(Route.elementAt(i));
			}
			return ;
		}
		if((g+h)/2>=min) return ;
		for(int i=0;i<mx;i++) {
			if(vis[i]==0 && i!=node) {
				vis[i]=1;
				Route.addElement(i);
				vst++;
				BB(i,g+2*Matrix[node][i],h-MinM[node][2]-MinM[i][1]);
				vst--;
				Route.removeElementAt(Route.size()-1);
				vis[i]=0;
			}
		}
	}
}
