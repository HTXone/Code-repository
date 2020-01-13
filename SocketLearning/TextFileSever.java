
import java.io.*;
import java.net.*;

public class TextFileSever {
	private static final int SEVER_PORT = 4000;
	private static String hostname = "localhost";
	private ServerSocket TFSSocket = null;
	private FileInputStream FIS;
	private DataOutputStream DOS;
	private Socket ClientServer;
	private FileOutputStream FOS;
	private DataInputStream DIS;
	
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
	
	public void FileGet(String TextName) {
		File file = new File(TextName);
		try {
			FOS = new FileOutputStream(file);
			DIS = new DataInputStream(ClientServer.getInputStream());
			System.out.println("FileName: "+DIS.readUTF());
			System.out.println("FileLength: "+DIS.readLong());
			byte[] bytes = new byte[2];
			int length = 0;
			while ((length = DIS.read(bytes,0,bytes.length))!=-1) {
				FOS.write(bytes, 0, bytes.length);
				FOS.flush();
			}
			System.out.println("File get over!");
			
		}catch(IOException ie) {
			System.out.println("File get error");
			System.err.println(ie);
		}
		finally {
			try {
				FOS.close();
				DIS.close();
			}catch(IOException ie) {
				System.out.println("Close error");
				System.err.println(ie);
			}
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
			byte[] bData = new byte[2];
			int length;
			while((length = FIS.read(bData, 0, bData.length))!=-1) {
				DOS.write(bData,0,bData.length);
				DOS.flush();
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
			}catch(Exception e) {System.out.println("Close error");}
		}
		
	}
	public static void main(String[] args) {
		TextFileSever TFSTest = new TextFileSever();
		TFSTest.FileGet("./ServerIn.bin");
		if(TFSTest.ClientServer.isClosed()) {System.out.println("Server Client is Closed!!");}
		else if(TFSTest.TFSSocket.isClosed()) System.out.println("Server is Closed!");
		try{TFSTest.ClientServer = TFSTest.TFSSocket.accept();}catch(IOException ie) {System.out.println("reAcccept error!");System.err.println(ie);}
		TFSTest.FileSend("./ServerIn.bin");
		try{
			TFSTest.ClientServer.close();
			TFSTest.TFSSocket.close();
			
		}catch(IOException ie) {
			System.out.println("Server close error");
			System.err.println(ie);
		}
	}
	
}
