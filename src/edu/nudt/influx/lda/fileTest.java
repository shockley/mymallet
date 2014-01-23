package edu.nudt.influx.lda;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class fileTest {
	public static void main(String[] args) throws IOException {
		FileWriter fw = new FileWriter(new File("F:\\test1\\1.txt"),true);
		fw.write("this is a kiss");
		
		fw.flush();
		fw.close();
	}

}
