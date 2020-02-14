
import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import javax.net.*;
import javax.net.ssl.*;
import javax.net.SocketFactory;



import java.lang.*;

class ClientFileTranslate implements Runnable{				//文件传输类
	Socket client = null;
	InputStream INS = null;
	OPSW OPS = null;
	String RC4PassWord = null;
	CallBack CB = null;
	
	ClientFileTranslate(Socket client,InputStream INS,OPSW OPS,String RC4PassWord,CallBack CB) {
		// TODO Auto-generated constructor stub
		this.client = client;
		this.INS = INS;
		this.OPS = OPS;
		this.RC4PassWord = RC4PassWord;
		this.CB = CB;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
		byte[] bData = new byte[1024];
		int length;
		byte[]sData;
		if(OPS.OS!=null) {
			while((length = INS.read(bData, 0, bData.length))!=-1) {
				System.out.println(length);
				OPS.OS.write(bData, 0, length);
				OPS.OS.flush();
			}
			OPS.OS.close();
			INS.close();
		}
	
		else {
			while((length = INS.read(bData, 0, bData.length))!=-1) {
				System.out.println(length);
				//sData = RC4.HloveyRC4(bData, RC4PassWord);
				OPS.RAF.write(bData, 0, length);
			}
			OPS.RAF.close();
			INS.close();
		}
		CB.callback();
	}catch(IOException ie) {
		System.out.println("File Translate error");
		ie.printStackTrace();
	}
	}
}

class MergeFileCallBack implements CallBack{				//分段文件接收后整合类
	private FileSplit FS = null;
	private String FileName = null;
	private int FileNum = 0;
	private SocketClient Client = null;
	
	private int index = 0;
	
	
	
	MergeFileCallBack(String FileName,int FileNum,SocketClient Client){
		this.FileName = FileName;
		this.FileNum = FileNum;
		this.Client = Client;
	}
	
	public synchronized void callback() {
		index++;
		if(index >= FileNum) {
			FS = new FileSplit();
			try {
				FS.mergePartFiles(FileNum, FileName+".gz", new MFFileCallBack(FileName, FileNum,FS, Client));
			}catch(Exception e) {
				System.out.println("File Merge Error");
				e.printStackTrace();
			}
		}
	}
		
}

class MFFileCallBack implements CallBack{
	private String FileName = null;
	private int FileNum = 0;
	private CallBack CB = null;
	private int index = 0;
	private FileSplit FS = null;
	
	
	MFFileCallBack(String FileName,int FileNum,FileSplit FS,CallBack CB){
		this.FileName = FileName;
		this.FileNum = FileNum;
		this.CB = CB;
		this.FS = FS;
	}
	
	public synchronized void callback(){
		index++;
		if(index >=FileNum) {
			System.out.println("file merge over");
			FS.threadPool.shutdown();
			FilePort.GZtoFile(FileName+".gz", FileName);
			File file = new File(FileName+".gz");
			file.delete();
			CB.callback(); 				//启动客户端最后回调函数
		}
	}
	
}

class SFClientCallBack implements CallBack{			//文件分割传输辅助类
	private FileSplit FS = null;
	private String hostName;
	private int HTTPS_PORT = 0;
	private String mood = null;
	private String NewFileName = null;
	private String command = null;
	private String SFileName = null;
	private int num = 0;
	private ArrayList<SocketClient>ClientList = null;
	private ArrayList<Thread> ThreadList = null;
	
	SFClientCallBack(FileSplit FS,String hostName,int port, String mood,String NewFileName,String command,String SFileName,ArrayList<SocketClient>ClientList,ArrayList<Thread>ThreadList){
		this.FS = FS;
		this.hostName = hostName;
		this.HTTPS_PORT = port;
		this.mood = mood;
		this.NewFileName = NewFileName;
		this.command = command;
		this.SFileName = SFileName;
		this.ClientList = ClientList;
		this.ThreadList = ThreadList;
		try {
		FS.splitBySize(NewFileName, 1024*60,SFClientCallBack.this);
		System.out.println("all num:"+FS.num);
		FS.splitStart();
		}catch (Exception e) {}
		
	}
	
	@Override
	public synchronized void callback() {
		// TODO Auto-generated method stub
		this.num++;
		System.out.println("num: "+num);
		if(num >= FS.num) {
			FS.threadPool.shutdown();
			try {
			FileSend();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		return ;
	}
	
	public void FileSend() throws Exception{
		
		SplitFileSendCallBack SFSCB = new SplitFileSendCallBack(FS.num);
	
		File file = new File(NewFileName);
		file.delete();
		
		for(int i = 0;i<FS.num;i++) {													//开启多个传输客户端嵌套字
			System.out.println("Split send start");
	
			SocketClient SSC = new SocketClient(this.hostName, this.HTTPS_PORT,this.mood,NewFileName+"_"+(int)(i+1)+".part",this.command,SFileName+".gz_"+(int)(i+1)+".part");
			
			System.out.println(i+"begin");
			
			if (SSC.client.isClosed())
				System.out.println("CLOSEDDDDD");
			else System.out.println("START");
			SSC.INS = IOStream.BufferedIn(IOStream.DataIn(SSC.FileSend(NewFileName+"_"+(int)(i+1)+".part",SFileName+".gz_"+(int)(i+1)+".part")));
			SSC.OPS.OS = IOStream.BufferedOut(IOStream.Dataout(SSC.client.getOutputStream()));
			
			ClientList.add(SSC);
			
			ClientFileTranslate CFT = new ClientFileTranslate((Socket)SSC.client, SSC.INS, SSC.OPS, "...",SFSCB);
			Thread thread = new Thread(CFT);
			System.out.println("start run");
			ThreadList.add(thread);
			thread.start();

		}
		
	//	for(int i = 0;i<ThreadList.size();i++) {
		//	Thread thread = ThreadList.get(i);
		//	System.out.println(i);
			
	//	}
		
	}
	
}

class SplitFileSendCallBack implements CallBack{
	int SFnum = 0;
	int index = 0;
	
	public SplitFileSendCallBack(int num) {
		// TODO Auto-generated constructor stub
		this.SFnum = num;
	}
	
	
	
	public synchronized void callback() {
		index++;
		if(index >= SFnum) {
			System.out.println("All file send over");
			return ;
		}
	}
	
}


public class SocketClient implements CallBack{		//增加回调接口
	int HTTPS_PORT = 4000;
	String hostName = "182.92.197.26";
	InetAddress hostAddress = null;
	SocketFactory factory = null;
	Socket client = null;
	private String DESPassWord = "12345678";
	private String RC4PassWord = "123456789";
	private String SSLPWD = "123456789";
	private String SSLKeyPath = "SSLKey";
	
	InputStream INS = null;											//传输流
	OPSW OPS = null;
	String mood = null;
	String command = null;
	String FileName = null;
	String SFileName = null;
	
	public SocketClient(String hostName,int port,String mood,String FileName,String command,String SFileName) throws Exception {				
		//SSLContext context  = SSLContext.getInstance("SSL");			//SSL环境初始化
		
		this.HTTPS_PORT  = port;
		this.hostName = hostName;
		this.mood = mood;
		this.command = command;
		this.FileName = FileName;
		this.SFileName = SFileName;
		this.OPS = new OPSW(null, null);
		
		
		//KeyStore trustKS = KeyStore.getInstance("JKS");					//密匙仓库
	//	trustKS.load(new FileInputStream("SSLKey"),"123456789".toCharArray());	//保存服务器授权证书
		//TrustManagerFactory kmf = TrustManagerFactory.getInstance("Sunx509");	//加载信任证书仓库
		//kmf.init(trustKS);														//初始化
		//context.init(null, kmf.getTrustManagers(), null);						//环境初始化
		
		//factory = context.getSocketFactory();									//获取工厂
		client = new Socket(hostName,port);			//得到套接字
		
		DataOutputStream DOS = new DataOutputStream(client.getOutputStream());		//向服务器发送传输命令
		DOS.writeUTF(mood+":"+FileName+":"+command+":"+SFileName);
		
	}
	
	/*public SocketClient(InetAddress hostAddress,int port) throws IOException{
		this.hostAddress = hostAddress;
		this.HTTPS_PORT = port;
		//factory = SocketFactory.getDefault();
		//client = factory.createSocket(hostAddress, port);
	}*/
	
	public OutputStream getClientOutputStream() throws IOException{			//嵌套字输出流
		return client==null?null:client.getOutputStream();
	}
	
	public InputStream getClientInputStream()	throws IOException{			//嵌套字输入流
		return client==null?null:client.getInputStream();
	}
	
	public void ClientFirstStart(String mood,String FileName,String command,String SFileName) {	//mood:读/写 FileName：操作文件名 command：文件是否加密选项	在接收请求后执行
		try {	
			
			switch(mood) {
			case "Send" :														
			//传输操作
			{
				File file = new File(FileName);
				if(!file.exists()) {
					System.out.println("File not exits!");
					return ;
				}
				//文件大小解析
				switch(FilePort.FileSize(file)) {
				case 1 : 
					{
						INS = IOStream.BufferedIn(IOStream.DataIn(FileSend(FileName,SFileName)));
						
						OPS.OS = IOStream.BufferedOut(IOStream.Dataout(client.getOutputStream()));
						
						ClientFileTranslate CFT = new ClientFileTranslate((Socket)client, INS, OPS, RC4PassWord,SocketClient.this);			//创建传输线程
						Thread thread = new Thread(CFT);
						thread.start();
						
					};break;
				case 2 : 					//大于10M的文件压缩传输
				{
					SFileName += ".gz";
					
					INS = IOStream.BufferedIn(IOStream.DataIn(FileSend(FileName,SFileName)));	//压缩传输流
					
					OPS.OS = IOStream.BufferedOut(IOStream.GZipout(IOStream.Dataout(client.getOutputStream())));
					
					ClientFileTranslate CFT = new ClientFileTranslate((Socket)client, INS, OPS, RC4PassWord,SocketClient.this);			//创建传输线程
					Thread thread = new Thread(CFT);
					thread.start();
					
				};break;
				case 3 : {					//大于1G的文件分段压缩传输
					client.close();		//关闭当前嵌套字
					System.out.println("ZS in here");
					String NewFileName = FileName+".gz";
					File NewFile = new File(NewFileName);
					
					System.out.println("GZip start");
					if(!NewFile.exists())
						NewFileName = FilePort.GZipFile(file);
					
					ArrayList<SocketClient> ClientList = new ArrayList<SocketClient>();
					ArrayList<Thread> ThreadList = new ArrayList<Thread>();
					
					System.out.println(NewFileName);
					
					FileSplit FS = new FileSplit();
					System.out.println("spliting");
					//启用带回调函数的类实例进行传输
					SFClientCallBack SFCCB = new SFClientCallBack(FS, this.hostName, this.HTTPS_PORT, this.mood, NewFileName, this.command, SFileName, ClientList, ThreadList);
					
				};break;
				default : break;
				}
			};break;//传输初始化完成
			case "Read" :
			//接收操作
			{
				DataOutputStream DOS = new DataOutputStream(client.getOutputStream());
				DataInputStream DIS = new DataInputStream(client.getInputStream());
				
				String SocketFileName = DIS.readUTF();				//服务端直接接收从客户端发来的文件
				long fileLength = DIS.readLong();					//单个文件返回长度 分段文件返回文件数
				
				System.out.println(SocketFileName+" "+fileLength);
				
				
				String[] FNS = SocketFileName.split("@");
				if(FNS.length>1&&FNS[FNS.length-1].equals("SF") ) {					//若为分段文件
					client.close();			//关闭当前嵌套字
					System.out.println("in S");
					ArrayList<SocketClient> ClientList = new ArrayList<SocketClient>();
					ArrayList<Thread> ThreadList = new ArrayList<Thread>();
					
					MergeFileCallBack MFCB = new MergeFileCallBack(FileName, (int)fileLength, SocketClient.this);
					
					for(int i = 0;i<fileLength;i++) {
						String NewFileName  = this.FileName+".gz_"+(int)(i+1)+".part";			//分段文件各文件名 待修改
						System.out.println("Split file read start");
						SocketClient SSC = new SocketClient(hostName, HTTPS_PORT,this.mood,NewFileName,this.command,this.SFileName+".gz_"+(i+1)+".part");
						
						DataInputStream SDOS = new DataInputStream(SSC.client.getInputStream());
						String SplitFileName = SDOS.readUTF();
						long sflength = SDOS.readLong();
						
						System.out.println(SplitFileName+" "+sflength);
						
						SSC.INS = IOStream.BufferedIn(IOStream.DataIn(SSC.client.getInputStream()));
						
						SSC.OPS.RAF = SSC.FileGet(NewFileName,SFileName+"gz_"+(i+1)+".part",sflength);
						ClientList.add(SSC);
						
						ClientFileTranslate CFT = new ClientFileTranslate((Socket)SSC.client, SSC.INS, SSC.OPS, this.RC4PassWord,MFCB);
				
						Thread thread = new Thread(CFT);
						ThreadList.add(thread);
						thread.start();
						//需要对分段文件进行整合
					}
				}
				
				else {
					System.out.println("right");
					if(client.isClosed()) {System.out.println("Client is closed");
					}
					else System.out.println("Client is still work");
					
					INS = IOStream.BufferedIn(IOStream.DataIn(client.getInputStream()));
					OPS.RAF = FileGet(FileName,SocketFileName,fileLength);
					
					ClientFileTranslate CFT = new ClientFileTranslate((Socket)client, INS, OPS, this.RC4PassWord,SocketClient.this);
					
					CFT.run();
					
					System.out.println(SocketFileName);
					
					if((SocketFileName.split("\\.")[SocketFileName.split("\\.").length-1]).equals("gz")) {				//压缩文件直接使用服务端文件名
						String NFileName = "";
						for(int i=0;i<SocketFileName.split("\\.").length-1;i++)
							NFileName+=SocketFileName.split("\\.")[i];
						
						System.out.println("gzFile: "+SocketFileName+"a"+" File: "+FileName);
						
						
						
						FilePort.GZtoFile(SocketFileName+"a", FileName);			//解压
						
					//	File file = new File(SocketFileName+"a");
						//file.delete();
				
					}
					}
				};break;
			}
		}catch(Exception ie) {
			System.out.println("Client start error");
			ie.printStackTrace();
		}
	}
	
	public FileInputStream FileSend(String fileName,String SFileName) {
		File file = new File(fileName);
		int num = 0;
		if(!file.exists()){System.out.println("file not exits!");return null;}
		try {
			DataOutputStream DOS =new DataOutputStream(client.getOutputStream());
			DataInputStream DIS = new DataInputStream(client.getInputStream());
			
			DOS.writeUTF(SFileName);
			DOS.writeLong(file.length());
			
			long fileLength = DIS.readLong();
			FileInputStream FIS = FilePort.getFIS(file, fileLength);		//文件传输位置定位
			
			return FIS;
		}catch(IOException ie) {
			System.out.println("File send start error");
			ie.printStackTrace();
			return null;
		}
	}
		
	public RandomAccessFile FileGet(String FileName,String SFileName,long fileLength) {
		try {
			DataOutputStream DOS = new DataOutputStream(client.getOutputStream());
			DataInputStream DIS = new DataInputStream(client.getInputStream());
			
			System.out.println(SFileName);
			String[] SFileNameS = SFileName.split("\\.");
			if(SFileNameS[SFileNameS.length-1].equals("gz")&&!FileName.split("\\.")[FileName.split("\\.").length-1].equals("gz")) {
				FileName = SFileName+"a";		//若为压缩文件直接使用服务端保存文件的文件名
			}
			
			File file = new File(FileName);
			RandomAccessFile FOS;
			if(file.exists()) {
				if(file.length()<fileLength) {
					DOS.writeLong(file.length());
					System.out.println("here right");
					FOS = FilePort.getRAF(file, file.length());
					INS =client.getInputStream();
					OPS.RAF = FOS;
				}
				else {
					file.delete();
					DOS.writeLong(0);
					FOS = new RandomAccessFile(file, "rw");
					INS = client.getInputStream();
					OPS.RAF = FOS;
				}
			}else {
				DOS.writeLong(0);
				FOS = new RandomAccessFile(file, "rw");
				INS = client.getInputStream();
				OPS.RAF = FOS;
			}
			
			return FOS;
		}catch(IOException ie) {
			System.out.println("file get start error");
			ie.printStackTrace();
			return null;
		}
	}
	
	public void FileTranslate(InputStream INS, OPSW OPS) throws IOException{		//文件中转
		byte[] bData = new byte[1024];
		int length;
		
		if(OPS.OS!=null) {
		while((length = INS.read(bData, 0, bData.length))!=-1) {
			System.out.println(length);
				OPS.OS.write(bData, 0, length);
				OPS.OS.flush();
			}
			OPS.OS.close();
			INS.close();
		}
		
		else {
			while((length = INS.read(bData, 0, bData.length))!=-1) {
				OPS.RAF.write(bData, 0, length);
			}
			OPS.RAF.close();
			INS.close();
		}
	}
	
	public void RC4FileTranslate() throws IOException{
		byte[] bData = new byte[1024];
		int length;
		byte[]sData;
		if(OPS.OS!=null) {
			while((length = INS.read(bData, 0, bData.length))!=-1) {
				//sData = RC4.HloveyRC4(bData, RC4PassWord);
				OPS.OS.write(bData, 0, length);
				OPS.OS.flush();
			}
			OPS.OS.close();
			INS.close();
		}
	
		else {
			while((length = INS.read(bData, 0, bData.length))!=-1) {
				//sData = RC4.HloveyRC4(bData, RC4PassWord);
				OPS.RAF.write(bData, 0, length);
			}
			OPS.RAF.close();
			INS.close();
		}
	}
	
	public static void main(String[] args) {
		try {
			SocketClient Client = new SocketClient("182.92.197.26",4000,args[0],args[1],args[2],args[3]);
			Client.ClientFirstStart(args[0], args[1], args[2],args[3]);
			//Client.FileTranslate(new FileInputStream("./in.bin"), new BufferedOutputStream(Client.getClientOutputStream())); 
		}catch(Exception ie) {}
		
	}


	@Override
	public void callback() {
		// TODO Auto-generated method stub
		System.out.println("file translate over");
	}	
	
}
