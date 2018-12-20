import processing.core.PApplet;
import processing.core.PVector;

public class ManualSphere implements runnableLedEffect {
	PApplet papplet;
	String name = "Sphere";
	String id;
	PVector[] ledPositions;
	LedColor[] bufferLedColors;
	float outerRadius, innerRadius, thickness, expansion;
	LedColor theColor;
	RemoteControlledColorParameter remoteColor;
	RemoteControlledFloatParameter remoteBlendOut;
	RemoteControlledFloatParameter remoteExpansion;
	PVector center = new PVector(0, 0, 0);
	LedColor.LedAlphaMode blendMode = LedColor.LedAlphaMode.NORMAL;

	ManualSphere(String _id, PVector[] _ledPositions, float _thickness, LedColor _color) {
		id = _id;
		ledPositions = _ledPositions;
		bufferLedColors = LedColor.createColorArray(ledPositions.length);
		thickness = _thickness;
		theColor = _color;
		remoteColor = new RemoteControlledColorParameter("Sphere" + id, 0.5f, 0.5f, 0.5f);
		remoteBlendOut = new RemoteControlledFloatParameter("Sphere" + id + "BlendOut", 0.f, 0.f, 1.f);
		remoteExpansion = new RemoteControlledFloatParameter("/Live/Sphere" + id + "expansion", 0.2f, 0.f, 1.f);
	}

	public LedColor[] drawMe() {
		expansion=remoteExpansion.getValue();
		outerRadius = papplet.map(expansion, 0.f, 1.f, 0f, ortlicht.sculptureRadius);
		innerRadius = outerRadius - thickness;
		LedSphereDrawer.drawSphere(ledPositions, bufferLedColors, center, outerRadius, innerRadius,
				remoteColor.getColor(), blendMode, remoteBlendOut.getValue());
		return bufferLedColors;
	}

	public String getName() {
		return name;
	}

}