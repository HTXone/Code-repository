import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
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

class DirInfoObject {
	
	private String DirChangeTime = null;
	private boolean Expand = false;
	private long ExpandNum = 0;
	
	private long Size = -1;			//未做统计为-1
	private long numbers = -1;		
	
	public boolean IsUsing = true;
	private String Infotxt = null;
	
	DirInfoObject(String Infotxt){		//格式：Using(T/F)@@DirChangeTime(YYYY/MM/DD HH/MM/SS)@@Expand(T/F)@@ExpandNum@@Size@@numbers
		String[] S = Infotxt.split("@@");
		if(S[0].equals("F")) this.IsUsing = false;
		else this.IsUsing = true;
		DirChangeTime = S[1];
		if(S[2].equals("T")) {
			Expand = true;
			ExpandNum = Long.valueOf(S[3]);
		}
		else Expand = false;
		
		Size = Long.valueOf(S[4]);
		numbers = Long.valueOf(S[5]);
		System.out.println(this.numbers);
	}
	
	public String getDirChangeTime() {
		return DirChangeTime;
	}
	
	public boolean IsExpand() {
		return Expand;
	}
	
	public long getExpand() {
		return ExpandNum;
	}
	
	public long getSize() {
		return Size;
	}
	
	public long getNumbers() {
		return numbers;
	}
	
	public void setDirChangeTime(String NewTime) {
		this.DirChangeTime = NewTime;
	}
	
	public void setDirChangeTime() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH/mm/ss");//设置日期格式
		this.DirChangeTime = df.format(new Date());
	}
	
	public void setExpand(long num) {
		this.Expand = true;
		this.ExpandNum = num;
	}
	
	public void setSize(long newSize) {
		this.Size = newSize;
	}
	
	public void setNum(long num) {
		this.numbers = num;
	}
	
	public void setIsUsing(boolean F) {
		this.IsUsing = F;
	}
	
	public void numAdd() {
		this.numbers++;
	}
	
	public void numSub() {
		this.numbers--;
	}
	
	public String getInfoTxt() {
		
		String Infotxt = "";
		
		Infotxt = this.IsUsing?"T@@":"F@@";
		
		Infotxt = Infotxt+this.DirChangeTime+"@@";
		if(Expand) {
			Infotxt = Infotxt+"T@@"+this.ExpandNum+"@@";
		}
		else {
			Infotxt = Infotxt+"F@@-1@@";
		}
		Infotxt = Infotxt+this.Size+"@@"+this.numbers;
		
		return Infotxt;
	}
	
}

public class DirTree {

	
	//文件内每行存储形式 ： T/F@@Info@@;DirName@@LastNum@@NextNum@@SonNum;FileName;FileName;....#
	
	private String[] SumTree = null;
	
	private static final long Sum = 10;
	
	private File BaseFile = null;
	
	private Vector<Long> History = null;	//路径记录数组 可改为Vector进行存储
	
	private long DirSum = 0;				//当前记录中已记录文件夹个数（后从记录文件中读出）
	
	private String MainInfo = null;
	
	public void BulidTree(String FileName) {
		try {
			RandomAccessFile RAF = new RandomAccessFile(new File(FileName), "rw");
			for(int i = 0;i<Sum;i++) {
				for(int j=0;j<1000;j++) {
					RAF.write(" ".getBytes());
				}
				RAF.write("#\n".getBytes());
			}
			RAF.write("1@@\n".getBytes());
			
			RAF = FilePort.getRAF(new File(FileName), 0);
			RAF.write("T@@2000/01/01 00/00/00@@F@@-1@@-1@@0;ROOT@@0@@0@@0;;#".getBytes());
			
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public String initTree(String FileName) {				//返回较上次登入后的修改记录
		SumTree = new String[10000];
		
		BaseFile = new File(FileName);
		
		History = new Vector<Long>();
		//History.add((long)0);
		
		try {
			RandomAccessFile RAF = FilePort.getRAF(BaseFile, Sum*1002);
			MainInfo = RAF.readLine();
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
		String lastInfo = this.MainInfo;
		
		this.DirSum = Long.valueOf(this.MainInfo.split("@@")[0]);
		
		this.MainInfo = "";
		
		return lastInfo;
	}
	
	public long getNum(long sum) {				//哈希函数 从空余列中获取值
		long num = (sum)%997;
		
		try {
			
			//File file = new File(FileName);
			
			FileInputStream FIS = FilePort.getFIS(BaseFile, num*1002);		//跳至指定行观察是否为空闲行
			
			byte bData[] = new byte[1];
			FIS.read(bData,0,1);
			
			int i = 1;
			
			while(new String(bData).equals("T")) {				//当为占用行时发生冲突 启动冲突处理函数
				
				System.out.println(new String(bData));	
				
				num+=i*i;
				i++;
				num = num%1000;
				FIS = FilePort.getFIS(BaseFile, num*1002);
				
				FIS.read(bData,0,1);
				
			}
			
			System.out.println(new String(bData));
		
			return num;						//无冲突 直接返回可用行
		
		} catch (Exception e) {				//出错 返回0（0行为根目录必占用）
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}

	}

	public String DirCheck(long DirNum) {						//进入文件夹 返回文件夹信息 格式：DirInfo;FileName;FileName;FileName;DirName@@DirNum;DirName@@DirNum;
		
		
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
	
	public String DirIn(long DirNum) {		//检查文件夹 只返回文件夹相关信息
		try {
		
			History.add(DirNum);				//将此文件夹路径存入记录数组
			
			return this.DirCheck(DirNum);
			
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean DirMake(long DirNum,String NewName) {		//在指定文件夹下新建文件夹 (因为记录条原因 建议使用重写方法) 成功返回true
		
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
			
			DirInfoObject DIO = new DirInfoObject("T@@2000/01/01 00/00/00@@F@@-1@@-1@@0");
			DIO.setDirChangeTime();
			
			String NewInfo = DIO.getInfoTxt()+";"+NewName+"@@"+preNum+"@@0@@0;;#";
			
			
			RandomAccessFile RAF = FilePort.getRAF(BaseFile, preNum*1002);			//修改上一文件夹记录
			RAF.write(LastNode.getLinetxt().getBytes());
			
			RAF = FilePort.getRAF(BaseFile, NewDirNum*1002);						//修改新文件夹记录
			RAF.write(NewInfo.getBytes());
			
			this.DirSum++;
			return true;
			
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	public boolean DirMake(String NewName) {	//在当前目录下新建文件夹(建议使用此方法进行新建)
		
		String path = "";
		
		long DirNum = 0;
		
		FileInputStream FIS = null;
		byte[] bData = new byte[1002];
		try {
		for(int i =0;i<History.size();i++) {
			FIS = FilePort.getFIS(BaseFile, History.get(i)*1002);
			FIS.read(bData, 0, bData.length);
			TreeNode HNode = new TreeNode(new String(bData).split("#")[0]);
			
			path = path+HNode.getDirName()+"\\";
			
			if(i == History.size()-1) {
				DirInfoObject DIO = new DirInfoObject(HNode.getInfotxt());
				DIO.setDirChangeTime();
				DIO.numAdd();
				HNode.setinfotxt(DIO.getInfoTxt());
				DirNum = History.get(i);
			}
		}
		//修改记录
		this.MainInfo = this.MainInfo+"A "+path+NewName+";";
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return this.DirMake(DirNum,NewName);
		
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
			
			String path ="";
			
			for(int i =0;i<History.size();i++) {
				FIS = FilePort.getFIS(BaseFile, History.get(i)*1002);
				FIS.read(bData, 0, bData.length);
				TreeNode HNode = new TreeNode(new String(bData).split("#")[0]);
				
				path = path+HNode.getDirName()+"\\";
				
				if(i == History.size()-1) {
					DirInfoObject DIO = new DirInfoObject(HNode.getInfotxt());
					DIO.setDirChangeTime();
					HNode.setinfotxt(DIO.getInfoTxt());
				}
			}
			//修改记录
			this.MainInfo = this.MainInfo+"D "+path+Node.getDirName()+";";
			
			if(!(lastNum<0)) {
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
			
			
			this.DirSum--;
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
	
	public void FileChange(String FileName) {	//文件修改  只修改当前正在导入文件信息 最后关闭时写入
		
		String path = "";						//目录
		
		try {
			FileInputStream FIS = null;
			byte[] bData = new byte[1002];
			
			for(int i =0;i<History.size();i++) {
				FIS = FilePort.getFIS(BaseFile, History.get(i)*1002);
				FIS.read(bData, 0, bData.length);
				TreeNode Node = new TreeNode(new String(bData).split("#")[0]);
				
				path = path+Node.getDirName()+"\\";
			}
			
			this.MainInfo = this.MainInfo+"M "+path+FileName+";";
			
			}catch(Exception e) {
				e.printStackTrace();
			}
	}
	
	public void FileAdd(String FileName) {		//文件增加记录
		String path = "";						//目录
		
		try {
			FileInputStream FIS = null;
			byte[] bData = new byte[1002];
			
			for(int i =0;i<History.size();i++) {
				FIS = FilePort.getFIS(BaseFile, History.get(i)*1002);
				FIS.read(bData, 0, bData.length);
				TreeNode Node = new TreeNode(new String(bData).split("#")[0]);
				
				path = path+Node.getDirName()+"\\";
				
				if(i == History.size()-1) {
					DirInfoObject DIO = new DirInfoObject(Node.getInfotxt());
					DIO.numAdd();
					DIO.setDirChangeTime();
					Node.setinfotxt(DIO.getInfoTxt());
					Node.setFiletxt(Node.getFiletxt()+FileName+";");
					
					RandomAccessFile RAF = FilePort.getRAF(BaseFile, History.get(i)*1002);
					RAF.write(Node.getLinetxt().getBytes());
					
				}
			}
			
			this.MainInfo = this.MainInfo+"A "+path+FileName+";";

		}catch(Exception e) {
			e.printStackTrace();
		}
			
	}
	
	public void FileDelete(String FileName) {
		String path = "";
		
		try {
			FileInputStream FIS = null;
			byte[] bData = new byte[1002];
			
			for(int i =0;i<History.size();i++) {
				FIS = FilePort.getFIS(BaseFile, History.get(i)*1002);
				FIS.read(bData, 0, bData.length);
				TreeNode Node = new TreeNode(new String(bData).split("#")[0]);
				
				path = path+Node.getDirName()+"\\";
				
				if(i == History.size()-1) {
					DirInfoObject DIO = new DirInfoObject(Node.getInfotxt());
					DIO.numSub();;
					DIO.setDirChangeTime();
					Node.setinfotxt(DIO.getInfoTxt());
					this.DirSum--;
				}
			}
			this.MainInfo = this.MainInfo+"D "+path+FileName+";";
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public String DirBack() {					//目录后退 返回父文件夹信息
		
		if(History.size() == 0 ) {				//判断 当到达最后文件夹时报错返回null
			System.out.println("No father Dir Error");
			return null;								
		}
		
		long FatherNum = History.get(History.size()-2);		//记录路径数组中删除
		History.remove(History.size()-1);
		
		System.out.println(FatherNum);
		
		return this.DirCheck(FatherNum);
		
	}

	public void Close() {						//将此次登入后修改记录计入文件
		try {
		RandomAccessFile RAF = FilePort.getRAF(BaseFile, Sum*1002);
		
		RAF.write((String.valueOf(this.DirSum)+"@@"+this.MainInfo+"\n").getBytes());
		}catch(Exception e) {
			System.out.println("Close Error!!!");
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DirTree DT = new DirTree();
		
		//DT.BulidTree("Treetest2.txt");
		
		System.out.println(DT.initTree("Treetest2.txt")+"Sum: "+DT.DirSum);
		System.out.println(DT.DirIn(0));
		
		System.out.println(DT.DirIn(2));
		
		//DT.FileAdd("FileA");
		
		//System.out.println(DT.DirIn(6));
		
		//System.out.println(DT.DirBack());
		
		//System.out.println(DT.History.size());
		
		//System.out.println(DT.DirBack());
		
		//DT.FileAdd("FileB");
		
		//DT.DirDelete(1);
		
		//System.out.println(DT.getNum(4));
		
		DT.DirMake("DirD");
		
		//DT.DirMake("DirB");
		
		System.out.println(DT.DirCheck(2));
		
		DT.Close();
		
		//System.out.println(DT.DirIn(1));
		//System.out.println(DT.DirCheck(1));
		
		//DT.DirSum = 3;
		
		//System.out.println(DT.DirMake(1, "G"));
		
		//System.out.println(DT.DirDelete(3));
	}

}
