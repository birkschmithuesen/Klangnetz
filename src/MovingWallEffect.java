import processing.core.*;

public class MovingWallEffect implements runnableLedEffect {
	LedColor[] ledColors;
	PVector[] ledPositions;

	RemoteControlledFloatParameter 	wallPosition;
	RemoteControlledFloatParameter fullOnWidth;
	RemoteControlledFloatParameter decayWidth;
	RemoteControlledFloatParameter decayGamma;

	RemoteControlledColorParameter wallColor=new RemoteControlledColorParameter("/wall/color",0,0,0);

	// used to tilt the wall in space
	RemoteControlledFloatParameter wallNormalX;	
	RemoteControlledFloatParameter wallNormalY;
	RemoteControlledFloatParameter wallNormalZ;

	MovingWallEffect(PVector[] ledPositions_,String name){
		ledPositions=ledPositions_;
		ledColors=LedColor.createColorArray(ledPositions.length);

		wallPosition=new RemoteControlledFloatParameter("/wall/"+name+"/position",0f,-1f,1f);
		fullOnWidth=new RemoteControlledFloatParameter("/wall/"+name+"/width",0.01f,0f,1f);
		decayWidth=new RemoteControlledFloatParameter("/wall/"+name+"/fadeOutWidth",0.5f,0f,1f);
		decayGamma=new RemoteControlledFloatParameter("/wall/"+name+"/fadeOutGamma",0.2f,0.01f,10f);

		wallNormalX= new RemoteControlledFloatParameter("/wall/"+name+"/normal/X",0.44f,-1f,1f);
		wallNormalY= new RemoteControlledFloatParameter("/wall/"+name+"/normal/Y",1f,-1f,1f);
		wallNormalZ= new RemoteControlledFloatParameter("/wall/"+name+"/normal/Z",0.55f,-1f,1f);
	}
	public LedColor[] drawMe() {
		
		PVector wallNormal= new PVector(wallNormalX.getValue(),wallNormalY.getValue(),wallNormalZ.getValue());
		if(wallNormal.magSq()>0.0001)		wallNormal.normalize(); // avoid dev/0
		
		float wallPos=wallPosition.getValue();
		float startFade=-(fullOnWidth.getValue()+decayWidth.getValue())/2.0f;
		float startFull=-(fullOnWidth.getValue())/2.0f;
		float endFull=+(fullOnWidth.getValue())/2.0f;
		float endFade=+(fullOnWidth.getValue()+decayWidth.getValue())/2.0f;
		float gamma=decayGamma.getValue();
		LedColor wallColor_=wallColor.getColor();
		for(int i=0;i<ledPositions.length;i++) {
			float fadeMult=0;
			float relPos=ledPositions[i].dot(wallNormal)-wallPos;//change this to make the wall travel in another direction
			if(relPos>startFade&&relPos<startFull) {
				fadeMult=(float)Math.pow(PApplet.map(relPos,startFade, startFull, 0f, 1f ),gamma);
			}
			if(relPos>startFull&&relPos<endFull) {
				fadeMult=1f;
			}			
			if(relPos>endFull&&relPos<endFade) {
				fadeMult=(float)Math.pow(PApplet.map(relPos,endFull, endFade, 1f, 0f ),gamma);
			}
			ledColors[i].set(wallColor_.x*fadeMult,wallColor_.y*fadeMult,wallColor_.z*fadeMult);

		}
		return ledColors;
	}

	public String getName() {
		return "movingWall";
	}
}
