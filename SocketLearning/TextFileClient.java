

import java.io.*;
import java.net.*;

import javax.sound.midi.SysexMessage;

public class TextFileClient {
	private static final int SEVER_PORT =4000;
	private static String hostname = "localhost";
	private DataInputStream DIS;
	private FileOutputStream FOS;
	private FileInputStream FIS;
	private DataOutputStream DOS;
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
			byte[] bData = new byte[2];
			int length;
			while((length = DIS.read(bData, 0, bData.length)) != -1) {
				FOS.write(byteToChar(bData));
				FOS.flush();
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
			}
			catch(IOException e) {
				System.out.println("Close error");
				System.err.println(e);
			}
		}
	}	
	
	public void FileSend(String TextName) {
		File file = new File(TextName);
		try {
			DOS = new DataOutputStream(Client.getOutputStream());
			FIS = new FileInputStream(file);
			System.out.println("FileName: "+file.getName());
			DOS.writeUTF(file.getName());
			DOS.flush();
			System.out.println("FileLength: "+file.length());
			DOS.writeLong(file.length());
			DOS.flush();
			System.out.println("Translate start");
			byte[] bData = new byte[2];
			int c = FIS.read();
			while(c!=-1) {
				bData = charToByte((char)c);
				DOS.write(bData, 0, bData.length);
				DOS.flush();
				c = FIS.read();
			}
		}
		catch(IOException ie) {
			System.out.println("send error");
			System.err.println(ie);
		}
		finally {
			try {
				DOS.close();
				FIS.close();
			}catch(IOException ie) {
				System.out.println("Close error");
			}
		}
	}
	
	public static void FileToByte(String FileName,String FileName2) {
		File file = new File(FileName);
		File file2 = new File(FileName2);
		FileInputStream FIS=null;
		FileOutputStream FOS=null;
		byte[] bb = null;
		//byte[] by = new byte[(int)file.length()];
		try {
			FIS = new FileInputStream(file);
			FOS = new FileOutputStream(file2);
		//ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			int ch = FIS.read();
			while(ch!=-1) {
				bb = charToByte((char)ch);
				System.out.print(ch+ " " + byteToChar(bb));
				FOS.write(bb,0,bb.length);
				ch = FIS.read();
			}
			bb = null;
			System.out.println("    over");
			//by = byteStream.toByteArray();
		}catch(IOException ie) {
			System.out.println("Change error");
			System.err.println(ie);
		}
		finally {
			try {
				if(FIS!=null)
					FIS.close();
				if(FOS!=null)
					FOS.close();
			}catch(IOException ie) {
				System.out.println("Close error");
				System.err.println(ie);
			}
		}
	}
	
	public static void ByteToFile(String FileName,String FileName2) {
		File file = new File(FileName);
		File file2 = new File(FileName2);
		FileInputStream FIS = null;
		FileOutputStream FOS = null;
		try {
			FIS = new FileInputStream(file);
			FOS = new FileOutputStream(file2);
		//ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			byte[] bb = new byte[2];
			int length = 0;
			while((length=FIS.read(bb, 0, bb.length))!=-1) {
				char c = byteToChar(bb);
				System.out.print(length+" "+c);
				FOS.write(c);
				FOS.flush();
			}
			System.out.println("    over");
		}catch(IOException ie) {
			System.out.println("File Change error");
			System.err.println(ie);
		}
		finally {
			try {
				FIS.close();
				FOS.close();
				
			}catch(IOException ie ) {
				System.out.println("Close error");
			}
		}
	}
	
	public static byte[] charToByte(char c) {
		byte[] b = new byte[2];
		b[0] = (byte)((c & 0xFF00)>>8);
		b[1] = (byte)((c & 0xFF));
		return b;
	}
	
	public static char byteToChar(byte[] b) {
		char c = (char)(((b[0] & 0xFF)<<8) | (b[1] & 0xFF));
		return c;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//TextFileClient.FileToByte("out.txt", "out.bin");
		//TextFileClient.ByteToFile("out.bin", "outTest.txt");
		//System.out.println(TextFileClient.byteToChar(TextFileClient.charToByte('t')));
		TextFileClient a = new TextFileClient();
		a.FileSend("./ClientSend.txt");
		if(a.Client.isClosed())	{System.out.println("Client Closed!!!");}
		else
			a.FileGet("./ClientGet.txt");
		TextFileClient b = new TextFileClient();
		b.FileGet("./ClientGet.txt");
		try {
			a.Client.close();
			b.Client.close();
		}
		catch(IOException ie) {
			System.out.println("Client close error");
		}
	}

}
