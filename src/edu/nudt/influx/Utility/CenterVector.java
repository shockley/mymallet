package edu.nudt.influx.Utility;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class CenterVector implements Serializable{
	
	public ArrayList<Double> topicvector;
	String filename = "vector";
	public CenterVector()
	{
		topicvector = new ArrayList<Double>();
	}
	
	public void addVector(double number)
	{
		topicvector.add(number);
	}
	public void getVector(int i)
	{
		topicvector.get(i);
	}
	
	public double Similarity(CenterVector cv)
	{
		double sim=0;
		double up = 0;
		double lay1 = 0;
		double lay2 = 0;
		for(int i=0;i<cv.topicvector.size();i++)
		{
			double c1 = this.topicvector.get(i);
			double c2 = cv.topicvector.get(i);
			up += c1*c2;
			lay1+= Math.pow(c1, 2);
			lay2+= Math.pow(c2, 2);
		}
		sim=up/(Math.sqrt(lay1*lay2));
		return sim;
	}
	public double Similarity2(CenterVector cv)
	{
		double sim=0;
		double up = 0;
		double lay1 = 0;
		double lay2 = 0;
		for(int i=0;i<cv.topicvector.size();i++)
		{
			double c1 = this.topicvector.get(i);
			double c2 = cv.topicvector.get(i);
			lay1 = c1-c2;
			lay2+= Math.pow(lay1, 2);
		}
		sim= Math.sqrt(lay2);
		return sim;
	}
	
	public double JSDistance(CenterVector cv)
	{
		double distance=0;
		CenterVector cvc = this.AverageCenter(cv);
		distance = (this.KLDistance(cvc)+cv.KLDistance(cvc))/2;
		return distance;
	}
	
	public double KLDistance(CenterVector cv)
	{
		double distance=0;
		for(int i=0;i<cv.topicvector.size();i++)
		{
			double c1 = this.topicvector.get(i);
			double c2 = cv.topicvector.get(i);
			double c = c1/c2;
			double d = Math.log(c)/Math.log(2);
			distance += c1*d;
		}
		return distance;
	}
	
	public CenterVector AverageCenter(CenterVector cv)
	{
		CenterVector cvc = new CenterVector();
		for(int i=0;i<cv.topicvector.size();i++)
		{
			double c1 = this.topicvector.get(i);
			double c2 = cv.topicvector.get(i);
			cvc.addVector((c1+c2)/2);
		}
		return cvc;
	}
	
}
