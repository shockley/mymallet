package edu.nudt.influx.Utility;

import java.util.HashMap;
import java.util.Map;

public class HashProcessor {
	public void addInteger(HashMap<Integer,Integer> map, int id){
		if(map.containsKey(id)){
			map.put(id,map.get(id)+1);
		}else{
			map.put(id, new Integer(1));
		}		
	}
	
	public void addDouble(HashMap<Integer,Double> map, int id){
		if(map.containsKey(id)){
			map.put(id,map.get(id)+1);
		}else{
			map.put(id, new Double(1));
		}		
	}
	public void reduceInteger(HashMap<Integer,Integer> map,Integer id){
		if(map.containsKey(id)){
			int count = map.get(id);
			if(count ==1){
				map.remove(id);
			}else{
				map.put(id,count-1);
			}
			
		}
	}
	public void reduceDouble(HashMap<Integer,Double> map,Integer id){
		if(map.containsKey(id)){
			double count = map.get(id);
			if(count ==1){
				map.remove(id);
			}else{
				map.put(id,count-1);
			}
			
		}
	}
	public int getCountInteger(HashMap<Integer,Integer> map,int key){
		if(map.containsKey(key)){
			return (int) map.get(key);
		}else 
			return 0;	
	}
	public double getCountDouble(HashMap<Integer,Double> map,int key){
		if(map.containsKey(key)){
			return (double) map.get(key);
		}else 
			return (double)0;	
	}
	public void initial(HashMap[] map, int length){
		for(int i=0;i<length;i++)
			map[i]=new HashMap();
	}
	
	
	public static void main(String[] args) {
		HashProcessor hp = new HashProcessor();
		HashMap[] hm = new HashMap[5];
		hp.initial(hm,5);
		hp.addInteger(hm[0], 1);
		hp.addInteger(hm[0], 1);
		System.out.println(hm[0].toString());
//		if(hm[0].containsKey(5)){
//			System.out.println("Include");
//		}
//		hp.addInteger(hm[0], 1);
//		hp.getCountInteger(hm[0], 5);
//		HashMap<Integer,Integer> hm = new HashMap<Integer,Integer>();
//		hm.put(3,4);
/*
		HashMap<Integer, Integer> docTopicCounts = new HashMap<Integer, Integer>();
		for(int i = 0; i<20;i++){
			hp.addInteger(docTopicCounts, i);
		}
		hp.addInteger(docTopicCounts, 10);
		hp.reduceInteger(docTopicCounts, 10);
//		hp.reduce(docTopicCounts, 11);
		Object[] values = docTopicCounts.keySet().toArray();
		System.out.println((Integer)values[10]+" "+values.length);*/
//		Iterator<?> it = docTopicCounts.entrySet().iterator();
//		while(it.hasNext()){
//			Entry<String, Integer> entry = (Entry<String, Integer>)it.next();
//			Object value = entry.getValue();
//			Object key = entry.getKey();
//			System.out.println("key: " +key+"   value:"+value);
//		}
//		System.out.println(hp.getCount(docTopicCounts, 10));
	}

}
