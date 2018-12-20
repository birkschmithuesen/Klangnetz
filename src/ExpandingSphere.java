import processing.core.PApplet;
import processing.core.PVector;

////////////////////////////////////////////////////////////////////////////
// A technoEFX: simply a sphere that expands from the middle of the ball, synced to a bpm rate
////////////////////////////////////////////////////////////////////////////

class ExpandingSphere implements runnableLedEffect {
	PApplet parent;
	String name = "ExpandingSphere";
	String id;
	PVector center = new PVector(0, 0, 0);
	float outerRadius, innerRadius, thickness;
	float cyclePosition; // the Position in the beat cycle, given by the master
							// bpm. value goes from 0-1
	float minRange = 0;
	float maxRange = 0;
	boolean cycle = false, directionOut = true; // if cycle is true, the sphere
												// axpands and in and out. if
												// false, the sphere just
												// expands out.
	float bpmMultiplier = 1.f; // for double, half... the speed of the master
								// bpm clock
	PVector[] ledPositions;
	LedColor[] bufferLedColors;
	LedColor theColor;
	LedColor.LedAlphaMode blendMode = LedColor.LedAlphaMode.NORMAL;
	int amount = 1;
	RemoteControlledColorParameter remoteColor;
	RemoteControlledFloatParameter remoteBlendOut;
	RemoteControlledFloatParameter remoteThickness;
	RemoteControlledFloatParameter remoteCyclePos;
	float maxRadius, minRadius;
	float cyclePos;

	ExpandingSphere(String _id, PVector[] _ledPositions, LedColor _color, float _maxRadius, float _minRadius) {
		id = _id;
		ledPositions = _ledPositions;
		bufferLedColors = LedColor.createColorArray(ledPositions.length);
		theColor = _color;
		remoteColor = new RemoteControlledColorParameter("ExpandingSphere" + id, 0.5f, 0.5f, 0.5f);
		remoteBlendOut = new RemoteControlledFloatParameter("ExpandingSphere" + id + "BlendOut", 1.f, 0.f, 1.f);
		remoteThickness =  new RemoteControlledFloatParameter("ExpandingSphere" + id + "thickness", 0.1f, 0.f, 1.f);
		remoteCyclePos = new RemoteControlledFloatParameter("ExpandingSphere" + id + "cyclePos", 0.0f, 0.f, 1.f);
		maxRadius=_maxRadius;
		minRadius=_minRadius;
	}

	public LedColor[] drawMe() {
		//choose whether auto cycle intern is giving the beat, or remote controlled
		cyclePos=remoteCyclePos.getValue();
		//cyclePos=ortlicht.bpmClock.getCyclePos()
		
		// LedColor.mult(bufferLedColors, remoteBlendOut.getValue()); //sets the
		// trace/blendOut for the effect
		thickness=remoteThickness.getValue();
		if (cycle) {
			if (directionOut) {
				outerRadius = parent.map(cyclePos, 0.f, 1.f, 0f, ortlicht.sculptureRadius+thickness);
				innerRadius = outerRadius - thickness;
				if (ortlicht.bpmClock.getCyclePos() > maxRadius)
					directionOut = false;
			} else {
				outerRadius = parent.map(cyclePos, 1.f, 0.f, 0f, ortlicht.sculptureRadius+thickness); 
				innerRadius = outerRadius - thickness;
				if (ortlicht.bpmClock.getCyclePos() < minRadius)
					directionOut = true;
			}
			LedSphereDrawer.drawSphere(ledPositions, bufferLedColors, center, outerRadius, innerRadius,
					remoteColor.getColor(), blendMode, remoteBlendOut.getValue());
		} else {
			outerRadius = parent.map(cyclePos, 0.f, 1.f, 0, ortlicht.sculptureRadius+thickness); 
			innerRadius = outerRadius - thickness;
			LedSphereDrawer.drawSphere(ledPositions, bufferLedColors, center, outerRadius, innerRadius,
					remoteColor.getColor(), blendMode, remoteBlendOut.getValue());
		}
		return bufferLedColors;
	}

	public String getName() {
		return name;
	}

	public LedColor[] getColorBuffer() {
		return bufferLedColors;
	}

}
