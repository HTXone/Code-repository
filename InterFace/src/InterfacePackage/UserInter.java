package InterfacePackage;

public interface UserInter {
	//ʹ�ýӿ�����������ݴ� 
	public boolean Login(String name,String password);		//���ڵ��� ���سɹ���ʧ������״̬
	public String[] getUserInformation();					//���ڶ�ȡ��ǰ�û�����Ϣ
	public boolean SingOut();								//���ڵǳ� ���سɹ���ʧ������״̬
	public boolean setUserInformation(String[] Info);		//�����û�����Ϣ�����޸�	���سɹ���ʧ������״̬
	public boolean Logon(String[] Info);					//�������û�ע��
	public boolean FileTranslation(String FromURL,String ToURL);		//�����ļ����� ͨ���ļ���URL�ַ���������Ŀ������ ���سɹ���ʧ������״̬
	
	
	
}
