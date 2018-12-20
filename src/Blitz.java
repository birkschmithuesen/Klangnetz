import processing.core.PApplet;

////////////////////////////////////////////////////////////////////////////
// The Blitz object lets a stroke grow from any point in the sphere along a 
//	stripe till it reaches the outer sphere
//////////////////////////////////////////////////////////////////////////// 

public class Blitz {
	int emitter;
	int direction;
	int position;
	int destination;

	Blitz(int emitter_, int destination_, int direction_) {
		emitter = emitter_;
		direction = direction_;
		position = emitter;
		destination = destination_;
	}

	void update() {
		if (position <= destination) {
			position += direction;
		} else {
			// AUS DEM ARRAY LÖSCHEN
		}
	}

	void show(int id_) {
		int id = id_;
		if (position > destination) {
			float opacity = 0;
			float hue = 0;
			for (int i = position; i < emitter; i++) {
				opacity = PApplet.map(i, position, emitter, 255, 0);
				hue = PApplet.map(i, position, emitter, 20, 120);
				// ledGraphicsEngine.setColor(i, color(hue, 130, opacity));
			}
			position -= 3;
		} else {
			emitter -= 2;
			/*
			if (emitter <= destination)
				blitze.remove(id); // Classe war vorher in Bälle. Löschung muss von dort aus veranlasst werden
			else {
				float opacity = 0;
				float hue = 0;
				for (int i = position; i < emitter; i++) {
					opacity = PApplet.map(i, position, emitter, 255, 0);
					hue = PApplet.map(i, position, emitter, 20, 120);
					// if (i>0 && i<numLeds)ledGraphicsEngine.setColor(i,
					// color(hue, 130, opacity));
				}
				
			}
			*/
		}
	}

}

