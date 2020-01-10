package apalabrados.model;

import org.json.JSONException;

public class Timer implements Runnable {

	private Thread thread;
	
	//Tiempo máximo al que se debe notificar a la partida
	private int deadline;
	
	//Estado actual del contador
	private int countdown;
	
	//Determina si el timer debe estár contando
	private boolean isRunning;
	
	//Determina si el hilo está en funcionamiento
	private boolean live; 
	
	//Partida a la que está asociado el timer
	private Match match;

	public Timer(int deadline, Match match) {
		this.thread = new Thread(this);
		this.thread.start();
		this.deadline = deadline;
		this.countdown = deadline;
		this.isRunning = false;
		this.live = true;
		this.match = match;
	}

	// run() method contains the code that is executed by the thread.
	@Override
	public void run() {
		System.out.println("starting");
		while (live) {
			try {
				if(this.isRunning) {
					this.countdown--;
					if(this.countdown == 0) {
						//Tiempo expirado, notificar la partida
						this.stop();
						this.match.expiredTime();
					}
				}
				Thread.sleep(1000L); // 1000L = 1000ms = 1 second
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public void start() {
		this.countdown = this.deadline;
		this.isRunning = true;
	}
	

	public void stop() {
		this.isRunning = false;
	}
	
	public void kill() {
		this.live = false;
	}

}
