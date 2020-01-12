

import java.io.*;
import java.net.*;

public class TextFileClient {
	private static final int SEVER_PORT =4000;
	private static String hostname = "localhost";
	private DataInputStream DIS;
	private FileOutputStream FOS;
	private Socket Client;
	
	TextFileClient(){
		try {
			Client = new Socket(hostname,SEVER_PORT);
			System.out.println("Link Start");
		}
		catch(IOException ie) {
			System.out.println("Client start error");
			System.err.println(ie);
		}
	}
	
	public void FileGet(String TextName) {
		File file = new File(TextName);
		try {
			DIS = new DataInputStream(Client.getInputStream());
			FOS = new FileOutputStream(file);
			System.out.println("FileName:"+DIS.readUTF());
			System.out.println("FlieLength:"+DIS.readLong());
			int c;
			c=DIS.read();
			while(c!=-1) {
				FOS.write(c);
				FOS.flush();
				c=DIS.read();
			}
		}
		catch(IOException e) {
			System.out.println("File Get Error");
			System.err.println(e);
		}
		finally {
			try {
				DIS.close();
				FOS.close();
				Client.close();
			}
			catch(IOException e) {
				System.out.println("Close error");
				System.err.println(e);
			}
		}
	}	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TextFileClient a = new TextFileClient();
		a.FileGet("./Get.txt");
	}

}
