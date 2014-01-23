package edu.nudt.influx.Utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class Stopword {
	public static Logger logger = Logger.getLogger(Stopword.class);
	private ArrayList<String> stopWordList = new ArrayList<String>();
	private ArrayList<String> allWordList = new ArrayList<String>();
	
	
	public Stopword()
	{
		String path = "stoplists/en.txt";
		try {
			setStopWord(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean IsStopword(String word)
	{
		for (int j = 0; j < stopWordList.size(); j++) {
			String stopWord = stopWordList.get(j);
			if (word.trim().equals(stopWord))// need to change!
			{
				return true;
			} 
		}
		return false;
	}
	
	public void setStopWord(String stopWordFile) throws IOException {
		File file = new File(stopWordFile);
		BufferedReader br = null;
		if (file.exists())
			try {
				br = new BufferedReader(new InputStreamReader(
						new FileInputStream(stopWordFile), "UTF-8"));
				String senWord;
				Boolean first = true;
				while ((senWord = br.readLine()) != null) {
					/*if (first == true)
						senWord = senWord.substring(1);*/
					stopWordList.add(senWord);
					// System.out.println(senWord);
					first = false;
				}
				
			} catch (IOException ioexception) {
			}finally
			{
				if(br!=null)
					br.close();
			}
	}
	
	public static void main(String args []){
		Stopword s =new Stopword();
		boolean yes = s.IsStopword("zero");
		logger.info(yes);
	}
}
