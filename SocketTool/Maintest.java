import javax.net.ssl.SSLSocket;

public class Maintest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			SSLSocketClient SSC = new SSLSocketClient("localhost", 4000, "Send", "in.bin.gz_1.part", "s", "test3.bin.gz_1.part");
			SSC.INS = IOStream.BufferedIn(IOStream.DataIn(SSC.FileSend("in.bin.gz_1.part","test3.bin.gz_1.part")));
			SSC.OPS.OS = IOStream.BufferedOut(IOStream.Dataout(SSC.client.getOutputStream()));
			
			ClientFileTranslate CFT = new ClientFileTranslate((SSLSocket)SSC.client, SSC.INS, SSC.OPS, "");
			Thread thread = new Thread(CFT);
			thread.run();
		}catch(Exception e) {
			System.out.println("error");
			e.printStackTrace();
		}
	
	}

}
