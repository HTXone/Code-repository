package InterfacePackage;

public interface UserInter {
	
	public boolean SignIn(String name,String password);		//���ڵ��� ���سɹ���ʧ������״̬
	public String[] getUserInfo();					//���ڶ�ȡ��ǰ�û�����Ϣ
	public boolean SignOut();								//���ڵǳ� ���سɹ���ʧ������״̬
	public boolean setUserInformation(String[] Info);		//�����û�����Ϣ�����޸�	���سɹ���ʧ������״̬
	public boolean Logon(String[] Info);					//�������û�ע��
	public boolean FileTranslate(String FromURL,String ToURL);		//�����ļ����� ͨ���ļ���URL�ַ���������Ŀ������ ���سɹ���ʧ������״̬
	
}


