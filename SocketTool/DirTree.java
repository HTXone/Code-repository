import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Vector;

public class DirTree {

	
	//文件内每行存储形式 ： T/F@@Info@@;DirName@@LastNum@@NextNum@@SonNum;FileName;FileName;....#
	
	private String[] SumTree = null;
	
	private static final long Sum = 10000;
	
	private File BaseFile = null;
	
	private Vector<Long> History = null;	//路径记录数组 可改为Vector进行存储
	
	private long DirSum = 0;				//当前记录中已记录文件夹个数（后从记录文件中读出）
	
	public void initTree() {
		SumTree = new String[10000];
		
		BaseFile = new File("SumTree.txt");
		
		History = new Vector<Long>();
		History.add((long)0);
		
	}
	
	public long getNum(long sum) {				//哈希函数 从空余列中获取值
		long num = sum%997;
		
		try {
			
			File file = new File("SumTree.txt");
			
			FileInputStream FIS = FilePort.getFIS(file, num*1001+1);		//跳至指定行观察是否为空闲行
			
			byte bData[] = new byte[1];
			FIS.read(bData,0,1);
			
			int i = 1;
			
			while(new String(bData).equals("T")) {				//当为占用行时发生冲突 启动冲突处理函数
				num+=i*i;
				i++;
				num = num%1000;
				FIS = FilePort.getFIS(file, num*1001);
				
				FIS.read(bData,0,1);
				
			}
		
			return num;						//无冲突 直接返回可用行
		
		} catch (Exception e) {				//出错 返回0（0行为根目录必占用）
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}

	}

	public String DirIn(long DirNum) {						//进入文件夹 返回文件夹信息 格式：DirInfo;FileName;FileName;FileName;DirName@@DirNum;DirName@@DirNum;
		
		History.add(DirNum);				//将此文件夹路径存入记录数组
		
		try {
			FileInputStream FIS = FilePort.getFIS(BaseFile, DirNum*1001);		//跳转至指定记录行
			
			byte bData[] = new byte[1001];
			
			FIS.read(bData, 0, bData.length);
			String LineInfo = new String(bData).split("#")[0];
			
			String[] LineInfoArgs = LineInfo.split(";",3);
			
			String DirInfo = LineInfoArgs[0].split("@@")[1];
			
			DirInfo = DirInfo+";"+LineInfoArgs[2];
			
			long SonDirNum = Long.valueOf(LineInfoArgs[1].split("@@")[3]);
			
			while(SonDirNum>0) {
				FIS = FilePort.getFIS(BaseFile, SonDirNum*1001);
				
				FIS.read(bData, 0, bData.length);
				String SonDirInfo = (new String(bData).split("#")[0]).split(";",3)[1];
				DirInfo = DirInfo+";"+ SonDirInfo.split("@@")[0];
				
				SonDirNum = Long.valueOf(SonDirInfo.split("@@")[3]);
				
				DirInfo = DirInfo+"@@"+SonDirNum;
				
			}
			
			return DirInfo;
			
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public String DirCheck(long DirNum) {		//检查文件夹 只返回文件夹相关信息
		try {
			FileInputStream FIS = FilePort.getFIS(BaseFile, DirNum*1001);
			
			byte[] bData = new byte[1001];
			FIS.read(bData, 0, bData.length);
			
			String Info = (((new String(bData).split("#")[0]).split(";")[0]).split("@@")[1]);
			
			return Info;
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean DirMake(long DirNum,String NewName) {		//在指定文件夹下新建文件夹 成功返回true
		long NewDirNum = this.getNum(DirSum);
		try {
			FileInputStream FIS = FilePort.getFIS(BaseFile, DirNum*1001);
			byte[] bData = new byte[1001];
			
			FIS.read(bData, 0, bData.length);
			
			long preNum = DirNum;
			String LastLine = new String(bData).split("#")[0];
			long lastNum = Long.valueOf(LastLine.split(";")[2].split("@@")[3]);
			
			while(lastNum!=0) {
				preNum = lastNum;
				
				FIS = FilePort.getFIS(BaseFile, lastNum*1001);
				FIS.read(bData, 0, bData.length);
				
				LastLine = new String(bData).split("#")[0];
				lastNum = Long.valueOf(LastLine.split(";")[2].split("@@")[3]);

			}
			
			String[] Args = LastLine.split(";",3);
			String[] Args2 = Args[1].split("@@");
			LastLine = Args[0]+Args2[0]+Args2[1]+NewDirNum+Args2[3]+Args[2];		//前一文件夹记录变动
			String NewInfo = "T;"+NewName+"@@"+lastNum+"@@0@@0;#";
			
			RandomAccessFile RAF = FilePort.getRAF(BaseFile, lastNum*1001);			//修改上一文件夹记录
			RAF.write(LastLine.getBytes());
			
			RAF = FilePort.getRAF(BaseFile, NewDirNum*1001);						//修改新文件夹记录
			RAF.write(NewInfo.getBytes());
			
			return true;
			
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	public boolean DirDelete(long DirNum) {		//删除文件夹
		try {
			FileInputStream FIS = FilePort.getFIS(BaseFile, DirNum*1001);
			byte[] bData = new byte[1001];
			
			FIS.read(bData, 0, 1001);
			
			String[] Line = new String(bData).split("#")[0].split(";",3);
			
			String[] Line2 = Line[1].split("@@");
			
			long lastNum = Long.valueOf(Line2[1]);
			long nextNum = Long.valueOf(Line2[2]);
			
			FIS = FilePort.getFIS(BaseFile, lastNum*1001);
			FIS.read(bData, 0, 1001);
			
			String[] LastLine = new String(bData).split("#")[0].split(";",3);		//修改上一
			String[] LastLine2 = LastLine[1].split("@@");
			LastLine2[2] = Line2[2];
			String LastLineInfo = LastLine[0]+";"+LastLine2[0]+"@@"+LastLine2[1]+"@@"+LastLine2[2]+"@@"+LastLine2[3]+";"+LastLine[2];
			
			
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}

}
