
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.lang.*;
import java.security.*;
import java.io.*;

class ChatSocket implements Runnable{
	
	private KeyPair keyPair = null;
	private String RSAPrivateKey = null;
	private String RSAPublicKey = null;
	private String RSAKey = null;
	
	private String DESKey = null;
	
	private String UserName = "USERTEST";
	private String UserPWD = "TESTPWD";
	private String Path = "TestPath";
	//private String DESPWD = null;
	
	private Socket client = null;
	
	private DataInputStream DIS = null;
	private DataOutputStream DOS = null;
	
	private boolean IsLogin = false;
	
	
	
	ChatSocket(Socket client){
		this.client = client;
		try {
			
			DIS = new DataInputStream(client.getInputStream());
			DOS = new DataOutputStream(client.getOutputStream());
			
			this.keyPair = RSA.getKeyPair();
			this.RSAPublicKey = RSA.getPublicKey(keyPair);
			this.RSAPrivateKey = RSA.getPrivateKey(keyPair);
			System.out.println("This RSA:"+RSAPublicKey);
			
			this.RSAKey = DIS.readUTF();
			DOS.writeUTF(RSAPublicKey);
			System.out.println("RSA: "+RSAKey);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void Close() {
		try {
			this.client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean Login(String[] CMDS) {
		try {
			if(CMDS[1].equals(UserName)&&CMDS[2].equals(UserPWD)) {
				this.IsLogin = true;
				
				DOS.writeUTF(RSA.encryptByPrivateKey("login:Success", RSAPrivateKey));
				
				this.DESKey = Long.toString(new Date().getTime()%100000000);
				
				DOS.writeUTF(RSA.encryptByPrivateKey(Path, RSAPrivateKey));
				DOS.writeUTF(RSA.encryptByPrivateKey(DESKey, RSAPrivateKey));
				
				return true;
			}
			else DOS.writeUTF(RSA.encryptByPrivateKey("login:Fail", RSAPrivateKey));
			return false;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		try {
			while(!client.isClosed()) {
				String CMD = RSA.decryptByPublicKey(DIS.readUTF(), RSAKey);
				System.out.println(CMD);
				
				String[] CMDS = CMD.split(":");
				
				switch(CMDS[0]) {
				
				case "login" : Login(CMDS);break;
				
				default : break;
				
				}			
			}
		}catch(Exception e) {
			e.printStackTrace();
		}	
	}
}

public class MainServer {
	static int PORT = 4001;					//信息服务器端口
	static ServerSocket SERVER = null;		//服务器嵌套字
	static ExecutorService pool = null;		//线程池
	
	
	
	MainServer(int port) {
		this.PORT = port;
	}
	
	MainServer(){
		this(4001);
	}
	
	public static ServerSocket init() {
		try {
			SERVER  = new ServerSocket(PORT);			//开启服务嵌套字
			pool = Executors.newCachedThreadPool();		//开启线程池
		}catch(Exception e) {
			System.out.println("Server start error");
			e.printStackTrace();
		}
		
		return SERVER;
	}
	
	public void Accept() {
		try {
			while(true) {
				Socket client = SERVER.accept();
			
				ChatSocket CS= new ChatSocket(client);
			
				pool.execute(CS);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		MainServer MS = new MainServer();
		
		MS.init();
		
		MS.Accept();
	}
	
}
