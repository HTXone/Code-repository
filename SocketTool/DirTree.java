import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Vector;

class TreeNode{
	private String[] Linetxt = null;
	
	private String Infotxt = null;
	
	private String DirName = null;
	private long LastNum = 0;
	private long NextNum = 0;
	private long SonNum = 0;
	
	private String Filetxt = null;
	
	TreeNode(String txt){
		this.Linetxt = txt.split(";",3);
		
		this.Infotxt = Linetxt[0];
		this.Filetxt = Linetxt[2];
		
		String[] S = Linetxt[1].split("@@");
		
		DirName = S[0];
		LastNum = Long.valueOf(S[1]);
		NextNum = Long.valueOf(S[2]);
		SonNum = Long.valueOf(S[3]);
		
	}
	
	public long getSonNum() {
		return SonNum;
	}
	
	public void setSonNum(long num) {
		this.SonNum = num;
	}
	
	public long getLastNum() {
		return LastNum;
	}
	
	public void setLastNum(long num) {
		this.LastNum = num;
	}
	
	public long getNextNum() {
		return NextNum;
	}
	
	public void setNextNum(long num) {
		this.NextNum = num;
	}
	
	public String getDirName() {
		return DirName;
	}
	
	public void setDirName(String Name) {
		this.DirName = Name;
	}
	
	public String getInfotxt() {
		return Infotxt;
	}
	
	public void setinfotxt(String txt) {
		this.Infotxt = txt;
	}
	
	public String getFiletxt() {
		return Filetxt;
	}
	
	public void setFiletxt(String txt) {
		this.Filetxt = txt;
	}
	
	public void deleteMark() {
		String[] s = this.Infotxt.split("@@");
		this.Infotxt = "F@@"+s[1];
	}
	
	public String getLinetxt() {
		String Line = Infotxt+";"+DirName+"@@"+LastNum+"@@"+NextNum+"@@"+SonNum+";"+Filetxt+"#";
		
		return Line;
	}
	
}

public class DirTree {

	
	//文件内每行存储形式 ： T/F@@Info@@;DirName@@LastNum@@NextNum@@SonNum;FileName;FileName;....#
	
	private String[] SumTree = null;
	
	private static final long Sum = 10000;
	
	private File BaseFile = null;
	
	private Vector<Long> History = null;	//路径记录数组 可改为Vector进行存储
	
	private long DirSum = 0;				//当前记录中已记录文件夹个数（后从记录文件中读出）
	
	public void initTree() {
		SumTree = new String[10000];
		
		BaseFile = new File("Tree.txt");
		
		History = new Vector<Long>();
		History.add((long)0);
		
	}
	
	public long getNum(long sum) {				//哈希函数 从空余列中获取值
		long num = (sum+1)%997;
		
		try {
			
			File file = new File("SumTree.txt");
			
			FileInputStream FIS = FilePort.getFIS(file, num*1002);		//跳至指定行观察是否为空闲行
			
			byte bData[] = new byte[1];
			FIS.read(bData,0,1);
			
			int i = 1;
			
			while(new String(bData).equals("T")) {				//当为占用行时发生冲突 启动冲突处理函数
				num+=i*i;
				i++;
				num = num%1000;
				FIS = FilePort.getFIS(file, num*1002);
				
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
			FileInputStream FIS = FilePort.getFIS(BaseFile, DirNum*1002);		//跳转至指定记录行
			
			byte bData[] = new byte[1002];
			
			FIS.read(bData, 0, bData.length);
			String LineInfo = new String(bData).split("#")[0];
			
			TreeNode LineNode = new TreeNode(LineInfo);
			
			//String DirInfo = LineInfoArgs[0].split("@@")[1];
			
			String DirInfo = LineNode.getInfotxt().split("@@")[1]+";"+LineNode.getFiletxt();
			
			long SonDirNum = LineNode.getSonNum();
			
			while(SonDirNum>0) {
				FIS = FilePort.getFIS(BaseFile, SonDirNum*1002);
				
				FIS.read(bData, 0, bData.length);
				String SonDirInfo = (new String(bData).split("#")[0]);
				
				LineNode = new TreeNode(SonDirInfo);
				DirInfo = DirInfo+";"+LineNode.getDirName()+"@@"+SonDirNum;
				
				SonDirNum = LineNode.getNextNum();
				//DirInfo = DirInfo+"@@"+SonDirNum;
				
			}
			
			return DirInfo;
			
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public String DirCheck(long DirNum) {		//检查文件夹 只返回文件夹相关信息
		try {
			FileInputStream FIS = FilePort.getFIS(BaseFile, DirNum*1002);
			
			byte[] bData = new byte[1002];
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
			FileInputStream FIS = FilePort.getFIS(BaseFile, DirNum*1002);
			byte[] bData = new byte[1002];
			
			FIS.read(bData, 0, bData.length);
			
			long preNum = DirNum;
			String LastLine = new String(bData).split("#")[0];
			TreeNode LastNode = new TreeNode(LastLine);
			
			long lastNum = LastNode.getSonNum();
			if(lastNum>0) {
			while(lastNum>0) {
				preNum = lastNum;
				
				FIS = FilePort.getFIS(BaseFile, lastNum*1002);
				FIS.read(bData, 0, bData.length);
				
				LastLine = new String(bData).split("#")[0];
				
				LastNode = new TreeNode(LastLine);
				
				lastNum = LastNode.getNextNum();

			}
			LastNode.setNextNum(NewDirNum);
			}else {
				LastNode.setSonNum(NewDirNum);
			}
			//LastLine = Args[0]+Args2[0]+Args2[1]+NewDirNum+Args2[3]+Args[2];		//前一文件夹记录变动
			
			String NewInfo = "T@@Info;"+NewName+"@@"+preNum+"@@0@@0;;#";
			
			
			RandomAccessFile RAF = FilePort.getRAF(BaseFile, preNum*1002);			//修改上一文件夹记录
			RAF.write(LastNode.getLinetxt().getBytes());
			
			RAF = FilePort.getRAF(BaseFile, NewDirNum*1002);						//修改新文件夹记录
			RAF.write(NewInfo.getBytes());
			
			return true;
			
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	public boolean DirDelete(long DirNum) {		//删除文件夹
		try {
			FileInputStream FIS = FilePort.getFIS(BaseFile, DirNum*1002);
			byte[] bData = new byte[1002];
			
			FIS.read(bData, 0, 1002);
			
			String Line = new String(bData).split("#")[0];
			TreeNode Node = new TreeNode(Line);
			
			long lastNum = Node.getLastNum();
			long nextNum = Node.getNextNum();
			
			if(lastNum>0) {
				FIS = FilePort.getFIS(BaseFile, lastNum*1002);
				FIS.read(bData, 0, 1002);
			
				String LastLine = new String(bData).split("#")[0];		//修改上一记录文件
				TreeNode LastNode = new TreeNode(LastLine);
				if(LastNode.getSonNum()==DirNum) {			//判断是同级文件夹还是子文件夹
					LastNode.setSonNum(nextNum);
				}
				else {
					LastNode.setNextNum(nextNum);
				}			//前后两节点相连
				
				RandomAccessFile RAF = FilePort.getRAF(BaseFile, lastNum*1002);		//修改后文本写入
				RAF.write(LastNode.getLinetxt().getBytes());
				
			}
			if(nextNum>0) {
				FIS = FilePort.getFIS(BaseFile, nextNum*1002);
				FIS.read(bData,0,1002);
			
				String NextLine = new String(bData).split("#")[0];
				TreeNode NextNode = new TreeNode(NextLine);
				
				NextNode.setLastNum(lastNum);
				
				RandomAccessFile RAF = FilePort.getRAF(BaseFile, nextNum*1002);
				RAF.write(NextNode.getLinetxt().getBytes());
		
			}
			
			Node.deleteMark();
			RandomAccessFile RAF = FilePort.getRAF(BaseFile, DirNum*1002);
			RAF.write(Node.getLinetxt().getBytes());
			
			if(Node.getSonNum()>0) {
				DirNum = Node.getSonNum();
				FIS = FilePort.getFIS(BaseFile, Node.getSonNum()*1002);
				FIS.read(bData, 0, 1002);
				Node = new TreeNode(new String(bData).split("#")[0]);
				
				DBS(Node);
			
				RAF = FilePort.getRAF(BaseFile, DirNum*1002);
				RAF.write(Node.getLinetxt().getBytes());
			}
			return true;
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private void DBS(TreeNode Node) {			//返回后修改导入
		if(Node.getNextNum() == 0) {
			if(Node.getSonNum() == 0) {
				Node.deleteMark();
				return ;
			}
			
			else {
				try {
					FileInputStream FIS = FilePort.getFIS(BaseFile, Node.getSonNum()*1002);
					byte[] bData = new byte[1002];
					FIS.read(bData, 0, bData.length);
					TreeNode Son = new TreeNode(new String(bData).split("#")[0]);
					
					DBS(Son);
					
					RandomAccessFile RAF = FilePort.getRAF(BaseFile, Node.getSonNum()*1002);
					RAF.write(Son.getLinetxt().getBytes());
					
					Node.deleteMark();
					return ;
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
			
		}
		
		else {
			try {
				FileInputStream FIS = FilePort.getFIS(BaseFile, Node.getNextNum()*1002);
				byte[] bData = new byte[1002];
				FIS.read(bData, 0, bData.length);
				TreeNode Next = new TreeNode(new String(bData).split("#")[0]);
				
				DBS(Next);
				
				RandomAccessFile RAF = FilePort.getRAF(BaseFile, Node.getNextNum()*1002);
				RAF.write(Next.getLinetxt().getBytes());
				
				Node.deleteMark();
				return ;
			}catch(Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DirTree DT = new DirTree();
		
		DT.initTree();
		
		//System.out.println(DT.DirIn(1));
		//System.out.println(DT.DirCheck(1));
		
		//DT.DirSum = 5;
		
		//System.out.println(DT.DirMake(1, "F"));
		
		System.out.println(DT.DirDelete(3));
	}

}
