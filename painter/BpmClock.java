import processing.core.PApplet;

class BpmClock {
	PApplet papplet;
	float cyclePos;
	float bpm;
	float startTime = 0;
	RemoteControlledFloatParameter remoteBpm = new RemoteControlledFloatParameter("bpm", 60, 5, 200);
	
	//get the bpm clock from ableton live over OSC Value from 0-1 for one cycle
	RemoteControlledFloatParameter beatPos = new RemoteControlledFloatParameter("beatPos", 0f, 0f, 1f);
	

	BpmClock() {
		papplet = new PApplet();
		cyclePos = 0;
		bpm = remoteBpm.getValue();
	}

	public void update() {
		if (cyclePos<0.1f)bpm = remoteBpm.getValue(); //get the value just at the beginning of a cycle to avoid anoing jumps
		cyclePos = (float)papplet.millis() / (60000.0f / bpm);
	//	cyclePos = cyclePos - papplet.floor(cyclePos);  --> schneidet alles vor dem komma ab -> muss in die Effekte aufgenommen werden
		
		//get the bpm clock from Ableton live over OSC Value from 0-1 for one cycle
	//	cyclePos = beatPos.getValue();
	}

	public float getCyclePos() {
		return cyclePos;
	}
}