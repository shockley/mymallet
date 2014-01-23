package edu.nudt.influx.Utility;

import java.util.ArrayList;

public class ListProcessor {
	
	public void add(ArrayList<Integer> docTopic,int topic){
		int length = docTopic.size();
		int topic_length = length/2;
		int flag = 0;
		for(int i = 0; i < topic_length; i++){
			if(docTopic.get(2*i)==topic){
				int doc_topic_count =  docTopic.get(2*i+1);
				docTopic.set(2*i+1, doc_topic_count+1);
				flag = 1;
				break;
			}
		}
		if(flag == 0){
			docTopic.add(topic);
			docTopic.add(1);
		}
	}

	public void reduce(ArrayList<Integer> docTopic,int topic){
		int length = docTopic.size();
		int topic_length = length/2;
		for(int i = 0; i < topic_length; i++){
			if(docTopic.get(2*i)==topic){
				int doc_topic_count =  docTopic.get(2*i+1);
				docTopic.set(2*i+1, doc_topic_count-1);
				break;
			}
//			System.out.println("at position "+i);
		}
	}
	
	public int getCount(ArrayList<Integer> docTopic, int topic){
		int length = docTopic.size();
		int topic_length = length/2;
		for(int i = 0; i < topic_length; i++){
			if(docTopic.get(2*i)==topic){
				int doc_topic_count =  docTopic.get(2*i+1);
				return doc_topic_count;
			}
		}
		return 0;
	}
	
	public void initialArrayList(ArrayList<Integer>[] al,int num){
		for(int i=0; i<num; i++){
			al[i] = new ArrayList<Integer>();
		}
	}
	
	public static void main(String[] args) {
		ListProcessor lp = new ListProcessor();
		
		ArrayList<Integer>[] al = new ArrayList[3];
//		for(int t=0;t<10;t++){
//			al.add(t);
//		}
		al[0] = new ArrayList<Integer>();
		lp.add(al[0], 9);
		for(int j=0;j<al[0].size();j++){
			System.out.println(al[0].get(j));
		}
	}
}
