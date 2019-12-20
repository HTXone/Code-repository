package InterfacePackage;

public interface UserInter {
	//使用接口类进行数据暂存 
	public boolean Login(String name,String password);		//用于登入 传回成功与失败两个状态
	public String[] getUserInformation();					//用于读取当前用户的信息
	public boolean SingOut();								//用于登出 传回成功与失败两个状态
	public boolean setUserInformation(String[] Info);		//用于用户对信息进行修改	传回成功与失败两个状态
	public boolean Logon(String[] Info);					//用于新用户注册
	public boolean FileTranslation(String FromURL,String ToURL);		//用于文件传输 通过文件的URL字符串来进行目标锁定 返回成功与失败两个状态
	
	
	
}
