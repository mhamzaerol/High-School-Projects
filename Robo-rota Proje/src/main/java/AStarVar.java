import java.util.Vector;

public class AStarVar {
	Vector<Integer> Route=new Vector<Integer>();
	int[] vis=new int[15];
	int now,vst;
	double g,h;
	AStarVar(AStarVar x) {
		for(int i=0;i<x.Route.size();i++)
			Route.addElement(x.Route.elementAt(i));
		for(int i=0;i<15;i++)
			vis[i]=x.vis[i];
		now=x.now;vst=x.vst;
		g=x.g;h=x.h;
	}
	AStarVar() {
		for(int i=0;i<15;i++)
			vis[i]=0;
		now=0;vst=0;
		g=0;h=0;	
	}
}
