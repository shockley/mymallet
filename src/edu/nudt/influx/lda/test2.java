package edu.nudt.influx.lda;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import cc.mallet.types.FeatureSequence;
import cc.mallet.types.InstanceList;

public class test2 {
	public static void main(String[] args) throws IOException {
		
		/*long time0 = System.nanoTime();
		for(int i=1;i<1000;i++){
			System.out.println("This is a kind of test!");
		}
		long time1 = System.nanoTime();
		double t = (double)(time1-time0)/1000000;
		System.out.println("Initial time: "+ time0);
		System.out.println("final time: "+ time1);
		System.out.println("The difference is " +t+"ms");*/
		/*String stemmed_tag_file = "F:\\Experiment\\MLClassification\\original_files\\mallet_tag_file.txt";
		String mallet_file = "F:\\test.mallet";
		InstanceList it = InstanceList.load(new File(mallet_file));
		FeatureSequence fs = (FeatureSequence)it.get(0).getData();
		String file_path = it.get(1).getName().toString();
		String file_name = file_path.substring(file_path.lastIndexOf("/")+1);
		System.out.println(file_name);
		String path = "F:\\test\\";
		File file = new File(path);
		String[] file_list = file.list();
		List list = Arrays.asList(file_list);
		if(list.contains(file_name)){
			System.out.println("Contain");
		}
		for(String file_name1:file_list){
			System.out.println(file_name1);
		}*/
//		for(int i=0;i<it.getDataAlphabet().size();i++){
//			System.out.println(it.getDataAlphabet().lookupObject(i).toString());
//			System.out.println(fs.get(i).toString()+"++++++++++");
//		}
		
/*		HashMap<Integer,Double> test = new HashMap<Integer,Double>();
		test.put(1, 0.5);
		test.put(2, 1.2);
		test.put(3, 3.2);
		if(test.containsKey(4)){
			System.out.println(test.get(4));
		}
		*/
		/*double double_count = 0.590;
		int count = Integer.valueOf(new BigDecimal(double_count).setScale(0, BigDecimal.ROUND_HALF_UP).toString());

		System.out.println(count);
		*/
//		PrintWriter pw = new PrintWriter(new FileWriter(new File(stemmed_tag_file)));
//		
//		int num_file = it.getDataAlphabet().size();
//		int total_size = 0;
//		for(int i=0;i<num_file;i++){
//			String tag = it.getDataAlphabet().lookupObject(i).toString();
////			System.out.println(tag);
//			pw.print(tag);
//			pw.print("\n");
//		}
//		pw.flush();
//		pw.close();
		
/*		String topic_id_file = "F:\\Experiment\\MLClassification5\\tag_id.txt";
		FileWriter fw = new FileWriter(new File(topic_id_file));
		for(int i=0;i<ilist.getDataAlphabet().size();i++){
			fw.write((i+1)+"  "+ilist.getDataAlphabet().lookupObject(i).toString());
			fw.write("\n");
		}
		fw.flush();
		fw.close();*/
//		for(int i=0;i<ilist.getDataAlphabet().size();i++){
		    
//		    ///文件全名
//		    String path = ilist.get(10).getName().toString();
//		    String file = path.concat("//"+path.substring(path.lastIndexOf("/")+1));
		
		String ilist_file = "F:\\Experiment\\LDAInference2\\OhInference\\ohinferencefiles.mallet";
		InstanceList ilist = InstanceList.load(new File(ilist_file));
		
		String ilist_file2 = "F:\\Experiment\\LDAInference2\\Testing\\TestingFiles\\Sf_Fc\\Test_FcFilesTagRemovedPiped.mallet";
		InstanceList ilist2 = InstanceList.load(new File(ilist_file2));
//		System.out.println(ilist.getDataAlphabet().size()+"  "+ilist2.getDataAlphabet().size());
//			FeatureSequence fs = (FeatureSequence)ilist.get(0).getData();
//			FeatureSequence fs2 = (FeatureSequence)ilist2.get(0).getData();
		FileWriter fw = new FileWriter(new File("F:\\oh_w_id.txt"));
		FileWriter fw2 = new FileWriter(new File("F:\\fc_id2.txt"));
//		
//		fw.write(ilist.get(0).getName().toString()+"\n");
/*		for(int i=0;i<ilist.getDataAlphabet().size();i++){
//			System.out.println(i+" "+ilist2.getDataAlphabet().lookupObject(i).toString()+" ");
			fw.write(i+" "+ilist.getDataAlphabet().lookupObject(i).toString()+"\n");
		}
		fw.flush();
		fw.close();
		*/
//		fw2.write(ilist2.get(0).getName().toString()+"\n");
		for(int j=0;j<ilist2.getDataAlphabet().size();j++){
			fw2.write(j+" "+ilist2.getDataAlphabet().lookupObject(j).toString()+"\n");
		}
		fw2.flush();
		fw2.close();
		    //某个文件的tag数目
//		    FeatureSequence fs = (FeatureSequence)ilist.get(10).getData();
//		    int tag_num =fs.size();
//		     for(int i=0;i<tag_num;i++){
//		    	int topic_id =fs.getIndexAtPosition(i);
//		    	System.out.println(file+"\n"+topic_id);
//		    }
		    
//		    //文件个数
//		    int all_length = ilist.size();
//		    System.out.println(all_length);
		    
//		}
		
	}
}
