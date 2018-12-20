import java.util.ArrayList;
import processing.core.PApplet;
import processing.core.PVector;
import oscP5.OscMessage;

////////////////////////////////////////////////////////////////////////////
// The Ball object describes one ball, that has the ability to apply and react
//////////////////////////////////////////////////////////////////////////// to
//////////////////////////////////////////////////////////////////////////// forces
////////////////////////////////////////////////////////////////////////////
public class Ball implements runnableLedEffect {
	String name = "Ball";
	PVector pos, vel, dest;
	float r;
	int time = 0;
	int id, partnerId;
	boolean leader;
	float lastCollision;
	LedColor[] bufferLedColors;
	LedColor.LedAlphaMode blendMode = LedColor.LedAlphaMode.NORMAL;
	// RemoteControlledColorParameter remoteColor;
	LedColor ballColor, DNAColor;
	PApplet papplet;

	// Fitness, DNA and a population for the evolution algorithm simulation for acceleration-decision-making
	float fitness;
	DNA dna;
	Population population;
	int dnaLength = 70;
	float dnaMaxForce=0.0025f;
	int numPopulation = 800;

	//this constructor generates a NULL Ball, just for initialization purpose
	Ball(){
	}
	
	// this constructor is used to create a virtual ball (not for display purpose)
	// virtual balls are used for acceleration decision simulations by genetic algorithms
	Ball(PVector _pos, PVector _vel, float _r, DNA _dna) {
		pos = _pos.copy();
		vel = _vel.copy();
		r = _r;
		dna = _dna;
		
	}

	// this constructor is used to create a ball for display purpose
	Ball(float r_, int id_, int partnerId_, boolean leader_, LedColor _color, LedColor _DNAColor) {
		papplet = new PApplet();
		vel = new PVector(0, 0, 0);
		// vel = new PVector(papplet.random(-0.08f, 0.08f), papplet.random(-0.08f, 0.08f), papplet.random(-0.08f, 0.08f));
		r = r_;
		id = id_;
		pos = new PVector((id * 0.4f) - 0.2f, 0, 0);
		dest = new PVector(0, 0, 0);
		leader = leader_;
		partnerId = partnerId_;
		// remoteColor=new RemoteControlledColorParameter("Ball/"+id+"/", 0.5,
		// 0.5, 0.5);
		bufferLedColors = LedColor.createColorArray(ortlicht.ledPositions.length);
		lastCollision = papplet.millis();
		ballColor = _color;
		DNAColor = _DNAColor;
		
		// creates a new population of virtual balls to simulate the next steps
		population = new Population(numPopulation, pos, vel, r, dnaMaxForce, dnaLength);
		papplet.println("Population initialization: "+population.getBall(10).getDNA().getGen(0).mag());
		dna=new DNA(dnaMaxForce, dnaLength);
	}

	public LedColor[] drawMe() {
		// color =remoteColor.getColor()
		LedColor.setAllBlack(bufferLedColors);
		LedSphereDrawer.drawSphere(ortlicht.ledPositions, bufferLedColors, pos, r, 0, ballColor, blendMode, 1);
		//drawDNA();
		// LedSphereDrawer.drawSphere(ortlicht.ledPositions, bufferLedColors, dest, r/2, 0, new LedColor(1,1,1), blendMode, 1);
		return bufferLedColors;
	}

	void drawDNA() {
		PVector dnaTrace = pos.copy();
		/*
		dnaTrace.add(dna.getGen(0).mult(50));
		LedSphereDrawer.drawSphere(ortlicht.ledPositions, bufferLedColors, dnaTrace, r/2, 0, DNAColor, blendMode, 1);
		dnaTrace.add(dna.getGen(1).mult(50));
		LedSphereDrawer.drawSphere(ortlicht.ledPositions, bufferLedColors, dnaTrace, r/2, 0, DNAColor, blendMode, 1);
		*/
		
		if (dna.getGenes().length > 0) {
			for (int i = 0; i < dna.getGenes().length; i++) {
				dnaTrace.add(dna.getGen(i).mult(50));
				LedSphereDrawer.drawSphere(ortlicht.ledPositions, bufferLedColors, dnaTrace, r / 2f, 0, DNAColor, blendMode, 1);
			}
		}
		
	}

	public String getName() {
		return name;
	}

	public PVector getPosition() {
		return pos;
	}

	// calculates the fitness of the ball, depending on distance to partner and wall if it would make the genes move
	// the higher both distances, the higher the fitness
	void fitness(Ball _partner, float _sculptureRadius) {
		PVector partnerVelocity = _partner.vel.copy();
		for (int i = 0; i < dna.genes.length; i++) {
			vel.add(dna.getGen(i));
			pos.add(vel);
			collisionWall(0);
			simulateCollisionBalls(_partner, partnerVelocity);
		}
		fitness = distToPartner(_partner) + distToWall(_sculptureRadius);
		if(fitness<0)fitness=0.01f;
/*		if (distToPartner(_partner) < 0){
			fitness = 0.01f;
			dna.setAllGenesToPause();			
		}
		if (distToWall(_sculptureRadius) < 0){
			fitness = 0.01f;
			dna.setAllGenesToPause();
		}
		*/
		// PApplet.println("X: "+dna.getGen().x+"\t Y: "+dna.getGen().x+"\t Z: "+dna.getGen().x+"\t MAG: "+dna.getGen().mag()+"\t fittnes: "+fitness);
		// PApplet.println("dist To Wall: "+distToWall(_sculptureRadius));
		// PApplet.println("dist To Partner: "+distToPartner(_partner));
		// PApplet.println("first Fitness: "+fitness);
	}

	// calculates the distance from the ball to the next wall, counted from the balls outer sphere
	float distToWall(float _sculptureRadius) {
		float distToWall = (_sculptureRadius - pos.mag()) - r;
		return distToWall;
	}

	// calculates the distance from the ball to its partner, counted from their outer sphere
	float distToPartner(Ball _partner) {
		float distToPartner = (pos.dist(_partner.pos) - r) - _partner.r;
		return distToPartner;
	}

	// calculates an acceleration depending on a genetic simulation to find the spot with the most free place
	void makeAcceleration(Ball _partner, float _sculptureRadius, int _numPopulation, int _generations, float _force,
			int _lengthDNA) {
		//the real ball did a step aka one gene, so the whole chain of all the population has to move
		population.updateLocation(pos, vel);
		population.moveGeneChain();
		//population.debug();
		
		//check if population size was remote controled changed
		if(_numPopulation!=numPopulation){
			population.resize(_numPopulation);
			numPopulation=_numPopulation;
		}
		//check if max Force size was remote controled changed
		if(_force!=dnaMaxForce){
			population.setMaxForce(_force);
			dnaMaxForce=_force;
		}
		//check if DNALength size was remote controled changed
		if(dnaLength!=_lengthDNA){
			population.setDNALength(_lengthDNA);
			dnaLength=_lengthDNA;
		}
		// starts the simulation and play it _generations times
		for (int i = 0; i < _generations; i++) {
			population.reproduction(0.01f);
			population.fitness(_partner, _sculptureRadius);
			population.selection();
		}
			vel.add(population.getWinnerWayLastGeneration());
			dna=population.getWinnerOfPopulation().getDNA();
		//	papplet.println("Force: "+population.getWinnerWayLastGeneration().mag());
			//papplet.println("Force: "+population.getWinnerOfPopulation().getDNA().getGen(3).mag());
	}

	void sendPosition() {
		OscMessage posMessage = new OscMessage("ball/" + id + "/pos");
		posMessage.add(PApplet.map(pos.x, -ortlicht.sculptureRadius, ortlicht.sculptureRadius, 0, 1));
		posMessage.add(PApplet.map(pos.y, -ortlicht.sculptureRadius, ortlicht.sculptureRadius, 0, 1));
		posMessage.add(PApplet.map(pos.z, -ortlicht.sculptureRadius, ortlicht.sculptureRadius, 0, 1));
		ortlicht.oscP5.send(posMessage, ortlicht.soundGeneratorLocation);
		OscMessage velMessage = new OscMessage("ball/" + id + "/vel");
		velMessage.add(PApplet.map(vel.mag(), 0f, 0.2f, 0f, 1f));
		ortlicht.oscP5.send(velMessage, ortlicht.soundGeneratorLocation);
	}

	void sendDistance(Ball _b) {
		OscMessage posMessage = new OscMessage("ball/distance");
		posMessage.add(PApplet.map(pos.dist(_b.pos), r, 2 * ortlicht.sculptureRadius, 0, 1));
		ortlicht.oscP5.send(posMessage, ortlicht.soundGeneratorLocation);
	}

	void step() { // neue Position ermitteln
		pos.add(vel);
		// vel=new PVector(0,0,0);
	}

	void determineMostSpace(Ball _b) {
		float maxDistBetweenCounterBallAndWall = _b.pos.mag() + ortlicht.sculptureRadius;
		dest = _b.pos.copy();
		dest.mult(-1);
		dest.setMag(maxDistBetweenCounterBallAndWall / 2);
		dest.add(_b.pos);
	}

	void collisionWall(float _friction) // Wand abprallen
	{
		if (distToWall(ortlicht.sculptureRadius) < 0) {
			/*
			 * vel.x=-vel.x; vel.y=-vel.y; vel.z=-vel.z;
			 */
			PVector dirCenter = new PVector(-pos.x, -pos.y, -pos.z);
			dirCenter.setMag(vel.mag());
			// dirCenter.normalize();
			// vel.mult(-1);
			vel.setMag(vel.mag() - _friction);
			vel.add(dirCenter);
//			papplet.println("PHYSIC COLISSION WITH WALL");
		}
	}

	void followForce(Ball _b, float _minForce, float _maxForce) {
		PVector direction = PVector.sub(_b.pos, pos);
		float attraction = PApplet.map(direction.mag(), 2 * r, 2 * ortlicht.sculptureRadius - 2 * r, _minForce,
				_maxForce);
		direction.setMag(attraction);
		vel.add(direction);
	}

	void leaderForce(float _deltaSpeed, float _fraction) {
		if (leader) {
			// int factor=50;
			float velX = ((papplet.noise(papplet.millis() / _deltaSpeed, 0) - 0.5f) / _fraction);
			// float velX=noise(papplet.millis()/_deltaSpeed, 0);
			float velY = ((papplet.noise(papplet.millis() / _deltaSpeed, 1) - 0.5f) / _fraction);
			float velZ = ((papplet.noise(papplet.millis() / _deltaSpeed, 2) - 0.5f) / _fraction);
			PVector perlinForce = new PVector(velX, velY, velZ);
			vel.add(perlinForce);
			// papplet.println(velX + "time:" + papplet.millis());
		}
	}

	void destForce(float _drive) {
		PVector dirDest = dest.copy();
		dirDest.sub(pos);
		dirDest.setMag(_drive);
		vel.add(dirDest);
	}

	void gravitationForce(PVector _gravitation) {
		vel.add(_gravitation);
	}

	void centerForce(float _centerGravitation) {
		PVector dirCenter = new PVector(-pos.x, -pos.y, -pos.z);
		dirCenter.setMag(_centerGravitation * pos.mag());
		vel.add(dirCenter);
	}

	void collisionBalls(Ball _b, LedColor _leaderCol, LedColor _followCol) {
		if (distToPartner(_b) < 0) {
			OscMessage theMessage = new OscMessage("ball/collision");
			theMessage.add(1f);// PApplet.map(vel.mag(), 0f, 0.5f, 0f, 1f));
			ortlicht.oscP5.send(theMessage, ortlicht.soundGeneratorLocation);

			// exchange velocities between the balls
			PVector counterBall = _b.vel.copy();
			_b.vel = vel.copy();
			vel = counterBall.copy();

			// reset position of this ball, so that they exactly touch each other
			// create the Vector between the two balls, by copying the position of this ball
			PVector connection = pos.copy();
			// substract the partners position
			connection.sub(_b.pos);
			// set the length of the connection equal two radiuses
			connection.setMag(r + _b.r);
			// the new position is the partners position plus the connection Vector:
			connection.add(_b.pos);
			// set this position to the new position
			pos = connection.copy();

			// send OCS signal for sound
			//papplet.println("PHYSIC COLISSION WITH BALL");
			theMessage = new OscMessage("ball/collision");
			theMessage.add(0f);
			ortlicht.oscP5.send(theMessage, ortlicht.soundGeneratorLocation);

			//connection = PVector.sub(pos, _b.pos); 
			//connection.div(2); 
			//PVector blitzEmmiter = PVector.add(pos, connection); 
			//FindBlitz(blitzEmmiter);
			
			/*
			 * PVector connection = PVector.sub(pos, _b.pos); connection.div(2); PVector blitzEmmiter = PVector.add(pos, connection); FindBlitz(blitzEmmiter);
			 * lastCollision = papplet.millis();
			 * 
			 * midi.sendNoteOn(0, 64, 100); midi.sendNoteOff(0, 64, 100); /* leader = !leader; if (leader) { ballColor = _leaderCol; _b.ballColor = _followCol;
			 * } else { ballColor = _followCol; _b.ballColor = _leaderCol; }
			 */
			// ledGraphicsEngine.setAllToColor(0x27041b);
		}
	}

	void simulateCollisionBalls(Ball _b, PVector _partnerVel) {
		if (distToPartner(_b) < 0) {
			// exchange velocities between the balls
			PVector counterBall = _partnerVel.copy();
			_partnerVel = vel.copy();
			vel = counterBall.copy();

			// reset position of this ball, so that they exactly touch each other
			// create the Vector between the two balls, by copying the position of this ball
			PVector connection = pos.copy();
			// substract the partners position
			connection.sub(_b.pos);
			// set the length of the connection equal two radiuses
			connection.setMag(r + _b.r);
			// the new position is the partners position plus the connection Vector:
			connection.add(_b.pos);
			// set this position to the new position
			pos = connection.copy();
		}
	}

	ArrayList<Blitz> blitze = new ArrayList<Blitz>();

	void FindBlitz(PVector emitter) {
		float lastDist = 10;
		int nearestLed = 0;
		/*
		 * for (int i=0; i<leds.length; i++) { float theDist=emitter.dist(leds[i].pos); if (theDist<lastDist) { lastDist=theDist; nearestLed=i; } } int
		 * blitzStripe=int(nearestLed/300); //Auf welchem Led Stripe befindet sich der Blitz? int theDestination = (blitzStripe*300); for (int i=0; i<180; i++)
		 * { int virtuellePos=nearestLed-i; if (leds[virtuellePos].pos.mag()>0.36) { theDestination=virtuellePos; i=180; }
		 * 
		 * }
		 * 
		 * blitze.add(new Blitz(nearestLed, theDestination, -1));
		 */
	}
	
	void setDNALength(int _length){
		dna.setLength(_length);
	}
	
	float getFitness() {
		return fitness;
	}

	DNA getDNA() {
		return dna;
	}

}
