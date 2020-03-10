
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.lang.*;
import java.security.*;
import java.io.*;

class Contorl implements Runnable{
	
	private Scanner input = null;
	private MainClient MC = null;
	private boolean IsClose = false;
	
	Contorl(MainClient MC){
		this.MC = MC;
		input = new Scanner(System.in);
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(!IsClose) {
			String CMD = input.next();
			switch(CMD) {
			case "Login" : {System.out.println("Please input UserName and PWD");MC.Login(input.next(), input.next());}break;
			case "Close" : MC.Close();this.IsClose = true;break;
			default : break;
			}
		}
	}
	
}

public class MainClient {
	
	private KeyPair keyPair = null;
	private String RSAPrivateKey = null;
	private String RSAPublicKey = null;
	private String RSAKey = null;
	
	private String DESKey = null;
	
	private boolean IsLogin = false;
	
	Socket client = null;
	
	private DataInputStream DIS = null;
	private DataOutputStream DOS = null;
	
	private String Path = null;
	
	MainClient(String hostName,int port){
		try {
			client = new Socket(hostName,port);
			
			DIS = new DataInputStream(client.getInputStream());
			DOS = new DataOutputStream(client.getOutputStream());
			
			this.keyPair = RSA.getKeyPair();
			this.RSAPublicKey = RSA.getPublicKey(keyPair);
			this.RSAPrivateKey = RSA.getPrivateKey(keyPair);
			
			System.out.println("This RSA:"+RSAPublicKey);
			
			DOS.writeUTF(RSAPublicKey);
			this.RSAKey = DIS.readUTF();
			
			System.out.println("RSA: "+RSAKey);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	MainClient(){
		this("localhost",4001);
	}
	
	public void Close() {
		try {
			this.client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void Login(String UserName,String PWD) {
		try {
			
			DOS.writeUTF(RSA.encryptByPrivateKey("login:"+UserName+":"+PWD, RSAPrivateKey));
			String CMD = RSA.decryptByPublicKey(DIS.readUTF(), RSAKey);
			
			String[] CMDS = CMD.split(":");
			
			if(CMDS[0].equals("login")) {
				if(CMDS[1].equals("Success")) {
					System.out.println("Login Success");
					
					this.IsLogin = true;
					
					this.Path = RSA.decryptByPublicKey(DIS.readUTF(), RSAKey);
					
					this.DESKey = RSA.decryptByPublicKey(DIS.readUTF(), RSAKey);
					
					System.out.println("Path: "+Path+" DES: "+DESKey);
					
				}
				else {
					System.out.println("Login Fail");
				}
			}else {
				System.out.println("ERROR");
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		MainClient MC = new MainClient();
		
		if(MC.client.isClosed())System.out.println("Close");
		
		Contorl C = new Contorl(MC);
		Thread thread = new Thread(C);
		thread.start();
	}
	
}
