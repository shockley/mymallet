package edu.nudt.influx.Utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;

import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

import common.Configs;
import common.PreProcessor;

/**
 * 
 * Mallet file to plain text
 * @author shockley
 *
 */
public class MalletToPlain {
	public static Logger logger = Logger.getLogger(MalletToPlain.class);
	public static void convert(String mfile){
		InstanceList ilist = InstanceList.load(new File(mfile));
		for(Instance i: ilist){
			logger.info(i.getAlphabet().toString());
		}
		return;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		convert(Configs.testBugMalletsDir+"3006.mallet");
	}

}
