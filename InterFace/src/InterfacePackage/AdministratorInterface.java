package InterfacePackage;

public interface AdministratorInterface {
	public boolean SignIn(String name,String password);		//用于登入 传回成功与失败两个状态
	public String[] getAdminInfo();							//用于读取当前管理员的信息
	public boolean SignOut();								//用于登出 传回成功与失败两个状态
	public String[] getUserInfo(int UserID);				//通过用户ID来获取用户信息
	public String[] getUSerInfo(String UserName);			//通过用户名来获取用户信息
	public boolean setAdminInfo(String[] Info);				//修改当前管理员信息
	//修改选定用户信息
	public boolean setUserInfo(int UserID,String[] Info);	
	public boolean setUserInfo(String UserName,String[] Info);
	public boolean setUserInfo(UserInter user,String[] Info);
	//用户删除
	public boolean UserDelete(UserInter user);
}
