package InterfacePackage;

public interface AdministratorInterface {
	public boolean SignIn(String name,String password);		//���ڵ��� ���سɹ���ʧ������״̬
	public String[] getAdminInfo();							//���ڶ�ȡ��ǰ����Ա����Ϣ
	public boolean SignOut();								//���ڵǳ� ���سɹ���ʧ������״̬
	public String[] getUserInfo(int UserID);				//ͨ���û�ID����ȡ�û���Ϣ
	public String[] getUSerInfo(String UserName);			//ͨ���û�������ȡ�û���Ϣ
	public boolean setAdminInfo(String[] Info);				//�޸ĵ�ǰ����Ա��Ϣ
	//�޸�ѡ���û���Ϣ
	public boolean setUserInfo(int UserID,String[] Info);	
	public boolean setUserInfo(String UserName,String[] Info);
	public boolean setUserInfo(UserInter user,String[] Info);
	//�û�ɾ��
	public boolean UserDelete(UserInter user);
}
