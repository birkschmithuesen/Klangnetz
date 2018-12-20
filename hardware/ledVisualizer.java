import processing.core.PApplet;
import processing.core.PVector;

// can be used to draw a collection of leds

class LedVisualizer {
	PApplet parent;

	LedVisualizer(PApplet p) {
		parent = p;
	}

	public void drawLeds(PVector[] ledPositions, LedColor[] ledColors, boolean showStripeBackground, float rotation) {
		float scale = 2000;
		float viewDistance = scale * 2;
		float rotationangle = rotation;// (float)millis()*0.001*rotationSpeed*TWO_PI;
		float camX = parent.sin(rotationangle) * viewDistance;
		float camy = parent.cos(rotationangle) * viewDistance;
		parent.perspective(0.2f, 1f, 1f, 20000f);
		parent.camera(camX, camy, 0f, 0f, 0f, 0f, 0f, 0f, -1f);

		for (int i = 0; i < ledColors.length; i++) {

			PVector curPos = ledPositions[i];
			if (showStripeBackground) {
				parent.stroke(100);
				parent.strokeWeight(2.5f);
				parent.point(curPos.x * scale, curPos.y * scale, curPos.z * scale);
			}
			parent.stroke(ledColors[i].getAsInt32Color(255));
			parent.strokeWeight(2);
			parent.point(curPos.x * scale, curPos.y * scale, curPos.z * scale);
		}
	}
}