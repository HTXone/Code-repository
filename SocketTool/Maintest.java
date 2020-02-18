import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocket;

public class Maintest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DoSomething DS = new DoSomething();
		
		Speed speed = new Speed(DS);
		
		DS.addObserver(speed);
		
		Thread thread = new Thread(DS);
		Thread thread2 = new Thread(speed);
		thread.start();
		thread2.start();
	
	}

}

@SuppressWarnings("deprecation")
class Speed implements Runnable,Observer{
	
	public long start = 0;
	public long end = 0;
	private int sum = 0;
	private boolean close = true;
	private DoSomething DS = null;
	
	private int aaaa;
	
	Speed(DoSomething DS){
		this.DS = DS ;
		close = true;
		end = start = 0;
		sum = 0;
	}
	
	public void SpeedS() {
		start = end;
		end = DS.index;
		System.out.println("Start: "+start+"end: "+end+"Speed: "+(end-start)+" kb/s");
	}
	
	@Override
	public synchronized void run() {
		// TODO Auto-generated method stub
		while(close) {
			try {
				TimeUnit.SECONDS.sleep(1);
			}catch(InterruptedException ie) {System.out.println("Speed watch error");}
			if(close) {SpeedS();DS.nindex = 0; DS.limit  = true;}
			else {SpeedS();System.out.println("Closed");}
		}
		return ;
	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		close = false;
		System.out.println("will close");
	}
	
}

@SuppressWarnings("deprecation")
class DoSomething extends Observable implements Runnable{
	
	public long index = 0;
	public boolean limit = true;
	private long speed = 5;
	int nindex = 0;
	
	DoSomething(){
		index = 0;
		limit = true;
		
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		while(index<20) {
			if(limit) {
				try{TimeUnit.MILLISECONDS.sleep(100);}catch(InterruptedException ie) {}
				index++;
				nindex++;
				if(nindex>=speed) limit = false;
			}
			else {
				try{TimeUnit.MILLISECONDS.sleep(100);}catch(InterruptedException ie){}
				System.out.println("limiting");
			}
			
		}
		super.setChanged();
		notifyObservers();
	}
	
	
	
}
