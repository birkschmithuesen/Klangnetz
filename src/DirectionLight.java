import processing.core.PApplet;
import processing.core.PVector;

public class DirectionLight implements runnableLedEffect {

	PApplet papplet;
	String name = "Direction";
	String id;
	PVector[] ledNormals;
	LedColor[] bufferLedColors;
	PVector angleFrom, angleTill;
	LedColor theColor;
	RemoteControlledColorParameter remoteColor;
	RemoteControlledFloatParameter remoteBlendOut;
	RemoteControlledFloatParameter remoteDirectionX;
	RemoteControlledFloatParameter remoteDirectionY;
	RemoteControlledFloatParameter remoteDirectionZ;
	RemoteControlledFloatParameter remoteSize;
	RemoteControlledFloatParameter remoteCyclePos;
	LedColor.LedAlphaMode blendMode = LedColor.LedAlphaMode.NORMAL;
	float cyclePos;

	DirectionLight(String _id, PVector[] _ledNormals, LedColor _color) {
		id = _id;
		ledNormals = _ledNormals;
		bufferLedColors = LedColor.createColorArray(ledNormals.length);
		theColor = _color;
		remoteColor = new RemoteControlledColorParameter("/Direction" + id, 0.5f, 0.5f, 0.5f);
		remoteBlendOut = new RemoteControlledFloatParameter("/Direction" + id + "BlendOut", 1.f, 0.f, 1.f);
		remoteDirectionX = new RemoteControlledFloatParameter("/Direction" + id + "XFrom", 0.2f, -0.5f, 0.5f);
		remoteDirectionY = new RemoteControlledFloatParameter("/Direction" + id + "YFrom", 0.2f, -1f, 1f);
		remoteDirectionZ = new RemoteControlledFloatParameter("/Direction" + id + "ZFrom", 0.2f, -0.5f, 0.5f);
		remoteSize = new RemoteControlledFloatParameter("/Direction" + id + "Size", 0.2f, 0.f, 1.f);
		remoteCyclePos = new RemoteControlledFloatParameter("/Direction" + id + "cyclePos", 0.0f, 0.f, 1.f);
	}

	public LedColor[] drawMe() {
		//choose whether auto cycle intern is giving the beat, or remote controlled
				cyclePos=remoteCyclePos.getValue();
				//cyclePos=ortlicht.bpmClock.getCyclePos()
		
		//float direction = papplet.map(ortlicht.bpmClock.getCyclePos(), 0.f, 1.f, -0.5f, 0.5f);
		
		float directionX = papplet.sin(cyclePos*papplet.TWO_PI)/4f;

		float directionZ = papplet.cos(cyclePos*papplet.TWO_PI)/4f;
		
		//ledDirectionDrawer.drawDirection(ledNormals, bufferLedColors, new PVector(remoteDirectionX.getValue(),remoteDirectionY.getValue(),remoteDirectionZ.getValue()), remoteSize.getValue(),
		//ledDirectionDrawer.drawDirection(ledNormals, bufferLedColors, new PVector(direction,remoteDirectionY.getValue(),remoteDirectionZ.getValue()), remoteSize.getValue(),
		
		ledDirectionDrawer.drawDirection(ledNormals, bufferLedColors, new PVector(directionX, remoteDirectionY.getValue(),directionZ), remoteSize.getValue(),
		
		remoteColor.getColor(), blendMode, remoteBlendOut.getValue());
		return bufferLedColors;
	}

	public String getName() {
		return name;
	}
}
