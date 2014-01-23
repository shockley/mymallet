package edu.nudt.influx.lda;

import java.util.ArrayList;

public class Utils {
	public void maoPao(ArrayList <Integer> y) 
	 {   
		 boolean flag = true;
		 while(flag)
		 {
			 flag = false;
			 for(int j = 0 ; j<y.size()-1 ; j++)
			 {
				 int a;
				 if(y.get(j)>y.get(j+1)) 
				 {
					 a = y.get(j);
					 y.set(j,y.get(j+1));
					 y.set(j+1,a);
					 flag = true;
				 }
			 }
		 }
	 }
}
