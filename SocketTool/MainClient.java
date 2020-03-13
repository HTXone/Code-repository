
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.lang.*;
import java.security.*;
import java.text.SimpleDateFormat;
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
	private String UserName = null;
	
	private String hostName = null;
	
	MainClient(String hostName,int port){
		try {
			client = new Socket(hostName,port);
			
			DIS = new DataInputStream(client.getInputStream());
			DOS = new DataOutputStream(client.getOutputStream());
			
			this.keyPair = RSA.getKeyPair();
			this.RSAPublicKey = RSA.getPublicKey(keyPair);
			this.RSAPrivateKey = RSA.getPrivateKey(keyPair);
			
			this.hostName = hostName;
			
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
	
	//登陆
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
					
					this.UserName = UserName;
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
	
	//文件属性展示 返回一个字符串容器 格式：File:FileName FileLength OriginalFileLength FileGetTime
	public Vector FileShow() {
		
		Vector args = new Vector<String>();
		
		try {
			DOS.writeUTF(RSA.encryptByPrivateKey("FileShow:"+UserName, RSAPrivateKey));
			
			boolean over = true;
			while(over) {
				String cmd = RSA.decryptByPublicKey(DIS.readUTF(), RSAKey);
				
				if(cmd.equals("Over")) over = false;
				
				else args.add(cmd);
			}
			return args;
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//文件删除
	public boolean FileDelete(String FileName) {
		try {
			DOS.writeUTF(RSA.encryptByPrivateKey("FileDelete:"+FileName, RSAPrivateKey));
			
			boolean result = DIS.readBoolean();
			
			if(result)System.out.println("Delete success");
			else System.out.println("Delete fail");
			
			return result;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	//文件传输 成功启动返回true 否则返回false
	public boolean FileTranslate(String mode,String FileName,String toFileName,long LimitedSpeed) {				//读/写 本地文件名 远程文件名 限速
		try {
			
			if(mode.equals("Send")) {							//文件传输
				File file = new File(FileName);
				
				if(!file.exists()) {
					System.out.println("FileNotExits");
					return false;
				}
				
				DOS.writeUTF(RSA.encryptByPrivateKey("FileSend:"+FileName+":"+file.length()+":"+(new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date())), RSAPrivateKey));
				
				boolean reply = DIS.readBoolean();
				
				if(reply) {
					SocketClient Client = new SocketClient(hostName,4000,"Send",FileName,Long.toString(LimitedSpeed),toFileName);
					Client.ClientFirstStart("Send", FileName, Long.toString(LimitedSpeed), toFileName);
					
					return true;
				}
				else return false;
				
			}
			else if(mode.equals("Read")) {
				
				DOS.writeUTF(RSA.encryptByPrivateKey("FileRead:"+toFileName, RSAPrivateKey));
				
				boolean reply = DIS.readBoolean();
				
				if(reply) {
					SocketClient Client = new SocketClient(hostName,4000,"Read",FileName,Long.toString(LimitedSpeed),toFileName);
					Client.ClientFirstStart("Read", FileName, Long.toString(LimitedSpeed), toFileName);
					
					return true;
				}
				else return false;
	
			}
			else {
				System.out.println("mode error");
				return false;
			}
			
			
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean FileTranslate(String mode,String FileName,Long LimitSpeed) {
		return this.FileTranslate(mode, FileName, Path+FileName,LimitSpeed);
	}
	
	
	
	public static void main(String[] args) {
		
		MainClient MC = new MainClient();
		
		if(MC.client.isClosed())System.out.println("Close");
		
		Contorl C = new Contorl(MC);
		Thread thread = new Thread(C);
		thread.start();
	}
	
}
