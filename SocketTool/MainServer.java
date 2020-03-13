
import java.net.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.lang.*;
import java.security.*;
import java.io.*;
import java.sql.*;

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
	
	private PostgreSQL SQL = null;
	
	private static final String BTableName = "BaseTable";
	
	ChatSocket(Socket client){
		this.client = client;
		try {
			
			SQL = new PostgreSQL();
			
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
	
	//关闭
	public void Close() {
		try {
			this.client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//注册(待完善)
	public void Logon(String[] CMDS) {			//注册格式：Logon:UserName:UserPWD;
		try {
			ResultSet result = SQL.Search(BTableName, "*", "WHERE NAME = "+CMDS[1]);
			if(result.next()) {
				DOS.writeUTF(RSA.encryptByPrivateKey("Logon:False:UserNameAlreadyExits", RSAPrivateKey));		//用户名已存在
			}
			else {
				File dir = new File(CMDS[1]);
				if(!dir.exists()) {
					dir.mkdir();
				}
				
				SQL.Insert(BTableName, "..."); 					//未完成
				
				SQL.CreatNewTable(CMDS[1], "...");					//未完成
			}
			
			DOS.writeUTF(RSA.encryptByPrivateKey("Logon:True", RSAPrivateKey));
			
		}catch(Exception e) {
			try {
				DOS.writeUTF(RSA.encryptByPrivateKey("Logon:False:Error", RSAPrivateKey));
				e.printStackTrace();
			}catch(Exception ie) {
				ie.printStackTrace();
			}
		}
	}
	
	//登录
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
	
	//文件属性展示
	public void FileShow(String[] CMDS) {			//CMDS格式： FileShow:TableName;
		try {
			ResultSet result = SQL.Search(CMDS[1], "*", "");
			
			while(result.next()) {		//属性格式：File:FileName FileLength OriginalFileLength FileGetTime
				DOS.writeUTF(RSA.encryptByPrivateKey("File:"+result.getString("FileName")+" "+result.getString("FileLength")+" "+result.getString("OriginalFileLength")+" "+result.getString("FileGetTime"), RSAPrivateKey));
			}
			
			DOS.writeUTF(RSA.decryptByPrivateKey("Over", RSAPrivateKey));		//传输完成提示
			SQL.StatementClose();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//文件删除
	public void FileDelete(String[] CMDS) {			//CMDS格式：FileDelete:FileName:TableName;
		try {
			File file = new File(CMDS[1]);
			
			file.delete();							//文件删除
			
			SQL.Delete(CMDS[2], "WHERE NAME = "+CMDS[1]);		//记录删除
			
			DOS.writeBoolean(true); 				//完成提示
			
		}catch(Exception e) {
			e.printStackTrace();
			try {
				DOS.writeBoolean(false);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
	}
	
	//文件传输
	public void FileGet(String[] CMDS) {
		try{
			SQL.Insert(UserName, "FileNAme = "+CMDS[1]+" Filelength = "+CMDS[2]+" FileGetTime = "+CMDS[3]);
			
			DOS.writeBoolean(true);
		
		}catch(Exception e) {
			try {
				DOS.writeBoolean(false);
				e.printStackTrace();
			}catch(IOException ie) {
				ie.printStackTrace();
			}
		}
	}
	
	public void FileSend(String[] CMDS) {
		try {
			DOS.writeBoolean(true);
		}catch(Exception e) {
			
			e.printStackTrace();
			
			try {
				DOS.writeBoolean(false);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
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
