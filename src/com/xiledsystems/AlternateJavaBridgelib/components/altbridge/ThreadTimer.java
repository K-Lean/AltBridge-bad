package com.xiledsystems.AlternateJavaBridgelib.components.altbridge;

import android.util.Log;

import com.xiledsystems.AlternateJavaBridgelib.components.events.EventDispatcher;

public class ThreadTimer extends AndroidNonvisibleComponent implements OnResumeListener, OnDestroySvcListener, OnStopListener {
	
	private Thread thread;
	private boolean running=false;
	private boolean isRunning=false;
	private int interval=1000;
	private boolean autoToggle=false;	
	private Runnable threadRunner;
	
	public ThreadTimer(ComponentContainer container) {
		super(container.$form());
		//thread = new Thread		
		threadRunner = new Runnable() {			
			@Override
			public void run() {				
				int sleepTime;
				long beginTime;
				long timeDiff;				
				while (running) {					
					// Here we setup a loop to keep running the dipatched event.
					
					beginTime = System.currentTimeMillis();
					
					dispatchTimerEvent();
					
					timeDiff = System.currentTimeMillis() - beginTime;
					
					sleepTime = (int) (interval - timeDiff);
					
					if (sleepTime > 0) {
						try {							
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {}
					}					
				}								
			}			
		};		
		form.registerForOnResume(this);
		form.registerForOnStop(this);
	}
	
	public ThreadTimer(SvcComponentContainer container) {
		super(container.$formService());
				
		threadRunner = new Runnable() {			
			@Override
			public void run() {				
				int sleepTime;
				long beginTime;
				long timeDiff;				
				while (running) {					
					// Here we setup a loop to keep running the dipatched event.					
					beginTime = System.currentTimeMillis();
					
					dispatchTimerEvent();
					
					timeDiff = System.currentTimeMillis() - beginTime;
					
					sleepTime = (int) (interval - timeDiff);
					
					if (sleepTime > 0) {
						try {							
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {}
					}					
				}								
			}			
		};
		
		formservice.registerForOnDestroy(this);
	}
	
	public int Interval() {
		return this.interval;
	}
	
	public void AutoToggle(boolean toggle) {
		this.autoToggle = toggle;
	}
	
	public boolean isAutoToggle() {
		return this.autoToggle;
	}
	
	public void Interval(int interval) {
		this.interval = interval;
	}
	
	public boolean Enabled() {
		return this.running;
	}
	
	public void Enabled(boolean enabled) {
		if (running && enabled) {
			boolean retry = true;
			while (retry) {
				try {						
					thread.join();					
					retry=false;
					isRunning=false;
				} catch (InterruptedException e) {
					
				}
			}			
		}
		this.running = enabled;
		if (running) {
			if (thread!=null) {
				Thread.State state = thread.getState();
				Log.w("ThreadTimer", "Thread State: "+state.toString());
			
			if (!state.equals(Thread.State.NEW)) {
				thread = new Thread(threadRunner);
			}
			} else {
				thread = new Thread(threadRunner);
			}
			thread.start();			
			isRunning=true;
		} else {
			if (isRunning) {
				boolean retry = true;
				while (retry) {
					try {						
						thread.join();
						retry=false;
						isRunning=false;
					} catch (InterruptedException e) {
						
					}
				}
			}
		}
	}
	
	private void dispatchTimerEvent() {
		
		EventDispatcher.dispatchEvent(this, "Timer");
		
	}

	@Override
	public void onStop() {		
		if (autoToggle) {
			this.running = false;
			boolean retry = true;
			while (retry) {
				try {
					thread.join();
					retry=false;
				} catch (InterruptedException e) {					
				}
			}
		}		
	}

	@Override
	public void onResume() {		
		if (autoToggle) {
			this.running = true;
			Thread.State state = thread.getState();
			if (state.equals(Thread.State.TERMINATED) || state.equals(Thread.State.WAITING)) {
				thread = new Thread(threadRunner);
			}
			thread.start();
		}		
	}

	@Override
	public void onDestroy() {		
		if (running) {
			this.running = false;
			boolean retry = true;
			while (retry) {
				try {
					thread.join();
					retry = false;
				} catch (InterruptedException e) {					
				}
			}
		}		
	}
}
