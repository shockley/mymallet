package edu.nudt.influx.Utility;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Mydebug {

	static FileWriter fw;
	static BufferedWriter bw;

	public static void init(){
		try{
		fw = new FileWriter("E:\\tsina_data\\Result\\debug.txt");
		
		}catch(Exception e){
			System.out.println("something wrong in mydebug init()");
		}
	}
	
	
	public static void outputInfo(String content) {
		try{
			bw = new BufferedWriter(fw);
			bw.append(content);
			bw.flush();
			bw.close();
		
		}catch(Exception e){
			System.out.println("something wrong in mydebug outputInfo");
		}
	}
		
	public static void closefile(){
		try{
			fw.close();
		}catch(Exception e){
			System.out.println("something wrong in mydebug closefile");
		}
	}
	
}
