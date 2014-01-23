package edu.nudt.influx.lda;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import cc.mallet.types.FeatureSequence;
import cc.mallet.types.InstanceList;

public class test {
	/**
	 * turn the original mallet file of all training text files to arff format
	 * @param mallet_file the original mallet file complete path
	 * @param target_file the target file include arff and xml format, target_file gives the 
	 *        complete path
	 */
	public void turnMalletToMulan(String text_mallet_file,String tag_mallet_file,String target_file){
		PrintWriter xml_pw = null;
		PrintWriter arff_pw = null;
		try {
			xml_pw = new PrintWriter(new FileWriter(target_file+".xml"));
			arff_pw = new PrintWriter(new FileWriter(target_file+".arff"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		HashMap<Integer,Integer> ilist_hash;
		
		
		
		//calculate and print every document's word distribution
		InstanceList ilist = InstanceList.load(new File(text_mallet_file));
		int doc_num = ilist.size();
		for(int i=0;i<doc_num;i++){
			ilist_hash  = new HashMap<Integer,Integer>();
			FeatureSequence fs = (FeatureSequence)ilist.get(i).getData();
			int doc_size = fs.getLength();
			for(int j=0;j<doc_size;j++){
				int word_index = fs.getIndexAtPosition(j);
				if(ilist_hash.containsKey(word_index)){
					ilist_hash.put(word_index, ilist_hash.get(word_index)+1);
				}else{
					ilist_hash.put(word_index, 1);
				}
			}
			Iterator ilist_it = ilist_hash.keySet().iterator();
			while(ilist_it.hasNext()){
				int word_index = (Integer) ilist_it.next();
				int word_count = ilist_hash.get(word_index);
				arff_pw.print(word_index+":"+word_count+";");
			}
			arff_pw.println();
		}
	}
	
	public static void maoPao(ArrayList <Integer> y) 
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
		 //System.out.println("\n"+tagnum.size());
	 }
	public static void main(String[] args) {
//ilist.size----the size of documents in this ilist;
//
		String tag_file = "F:\\Experiment\\MLClassification\\mallet\\sf_all_stemmed_tags.mallet";
		InstanceList ilist = InstanceList.load(new File(tag_file));	
				//				"F:\\Experiment\\MLClassification\\mallet\\tag.mallet"));
//				"F:\\Experiment\\LDA\\Experiments\\ouyang\\mallet\\sf_test\\a--g--e_278643.mallet"));
//		for(int i=0;i<ilist.getDataAlphabet().size();i++){
//			System.out.println(ilist.getDataAlphabet().lookupObject(i));
//		}
//		System.out.println(ilist.getDataAlphabet().);
//		for(int i=0;i<ilist.getDataAlphabet().size();i++){
//			System.out.println(ilist.getDataAlphabet().lookupObject(i));
//		}
//		
//		FeatureSequence fs = (FeatureSequence)ilist.get(1).getData();
////		int test[] = fs.getFeatures();
//		for(int i=0;i<fs.size();i++){
//			System.out.println(fs.getIndexAtPosition(i));
//		}
		
////		fs.
//		ArrayList<Integer> test_array = new ArrayList<Integer>();
//		for(int t:test){
//			test_array.add(t);
//			System.out.println(test.length);
//		}
//		if(test_array.contains(61)){
//			
//		}

		
/*		ArrayList<Integer> test = new ArrayList<Integer>();
		test.add(10);
		test.add(4);
		test.add(12);
		test.add(4);
		maoPao(test);
		for(int i=0;i<test.size();i++){
			System.out.println(test.get(i));
		}
*/
//		System.out.println(ilist.getDataAlphabet().size());
//		FeatureSequence fs = (FeatureSequence)ilist.get(0).getData();
//		System.out.println(fs.get);
/*		try {
			PrintWriter pw = new PrintWriter(new FileWriter("D:\\1.txt"));
			for(int i =0;i<10;i++){
				pw.print(Integer.toString(i));
			}
			pw.flush();
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}

}
