package org.kuali.maven.ec2;

import java.util.List;

public class WaitControl {
    boolean wait;
    int timeout;
    int sleep = Constants.DEFAULT_SLEEP_MILLIS;
    int initialPause = Constants.DEFAULT_INITIAL_PAUSE_MILLIS;
    String state;
    List<Integer> waitPorts;
	int extraWait;


	public int getExtraWait() {
		return extraWait;
	}

	public void setExtraWait(int extraWait) {
		this.extraWait = extraWait;
	}

	public WaitControl() {
        this(false, 0, null);
    }
	
    public WaitControl(boolean wait, int timeout, String state) {
        super();
        this.wait = wait;
        this.timeout = timeout;
        this.state = state;
    }

    public WaitControl(boolean wait, int timeout, List<Integer> waitPorts, String state, int extraWait) {
        super();
        this.wait = wait;
        this.timeout = timeout;
        this.waitPorts = waitPorts;
        this.state = state;
        this.extraWait = extraWait;
    }

    public boolean isWait() {
        return wait;
    }

    public void setWait(boolean wait) {
        this.wait = wait;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<Integer> getWaitPorts() {
    	return waitPorts;
    }
    
    public void setWaitPorts(List<Integer> waitPorts) {
    	this.waitPorts = waitPorts;
    }

    public int getSleep() {
        return sleep;
    }

    public void setSleep(int sleep) {
        this.sleep = sleep;
    }

    public int getInitialPause() {
        return initialPause;
    }

    public void setInitialPause(int initialPause) {
        this.initialPause = initialPause;
    }

}
