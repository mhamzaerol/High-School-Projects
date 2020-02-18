import java.util.Arrays;
import java.util.Vector;

public class BitM {
	int st,mx,vst=0;
	double[][] dp=new double[15][(1<<15)];
	int[] vis=new int[15];
	Vector<Integer> Answer=new Vector<Integer>();
	double[][] Matrix=new double[15][15];
	int[] pow=new int[16];
	double inf=98765432;
	BitM(double[][] m,int a,int b) {
		for(int i=0;i<15;i++)
			for(int j=0;j<(1<<15);j++)
				dp[i][j]=-1;
		Arrays.fill(vis,0);
		Matrix=m;st=a;mx=b;
		pow[0]=1;
		for(int i=1;i<16;i++) {
			pow[i]=pow[i-1]*2;
		}
		Bm(st,0);
		int x=st,val=0;
		int[] vis2=new int[15];
		Arrays.fill(vis2,0);
		Answer.addElement(x);
		boolean flag=true;
		while(flag) {
			flag=false;
			for(int i=0;i<mx;i++) {
				if(vis2[i]==0 && Matrix[x][i]+dp[i][val+pow[i]]==dp[x][val]) {
					x=i;
					Answer.addElement(x);
					vis2[x]=1;
					val+=pow[x];
					flag=true;
					break ;
				}
			}
		}
	}
	double Bm(int node,int val) {
		if(dp[node][val]!=-1) return dp[node][val];
		if(node==st && val>0) {
			if(vst==mx) return dp[node][val]=0;
			return dp[node][val]=inf;
		}
		double ret=inf;
		for(int i=0;i<mx;i++) {
			if(vis[i]==0) {
				vis[i]=1;
				val+=pow[i];
				vst++;
				ret=Math.min(Bm(i,val)+Matrix[node][i],ret);
				vst--;
				val-=pow[i];
				vis[i]=0;
			}
		}
		return dp[node][val]=ret;
	}
}
