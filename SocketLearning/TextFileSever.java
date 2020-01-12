

import java.io.*;
import java.net.*;

public class TextFileSever {
	private static final int SEVER_PORT = 4000;
	private static String hostname = "localhost";
	private ServerSocket TFSSocket = null;
	private FileInputStream FIS;
	private DataOutputStream DOS;
	private Socket ClientServer;
	
	TextFileSever() {
		try {
			TFSSocket = new ServerSocket(SEVER_PORT);
			System.out.println("the Server started!");
			System.out.println("Waiting......");
			ClientServer = TFSSocket.accept();
		}
		catch(IOException ie) {
			System.err.println(ie);
			System.out.println("Server start error");
		}
		
		
	}
	
	public void FileSend(String TextName) {
		File file = new File(TextName);
		try {
			FIS = new FileInputStream(file);
			DOS = new DataOutputStream(ClientServer.getOutputStream());
			DOS.writeUTF(file.getName());
			DOS.flush();
			DOS.writeLong(file.length());
			DOS.flush();
			System.out.println("Translate start");
			int c;
			c=FIS.read();
			while(c!=-1) {
				DOS.write((char)c);
				DOS.flush();
				c=FIS.read();
			}
			System.out.println("send over");
		}
		catch(IOException e) {
			System.out.println("File Send error!");
			System.err.println(e);
		}
		finally {
			try {
			if(FIS!=null) FIS.close();
			if(DOS!=null) DOS.close();
			if(ClientServer!=null) ClientServer.close();
			}catch(Exception e) {System.out.println("Close error");}
		}
		
	}
	public static void main(String[] args) {
		TextFileSever TFSTest = new TextFileSever();
		TFSTest.FileSend("./out.txt");
		try{TFSTest.TFSSocket.close();
		}catch(IOException ie) {
			System.out.println("Server close error");
			System.err.println(ie);
		}
	}
	
}
