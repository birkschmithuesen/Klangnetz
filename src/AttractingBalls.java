import processing.core.PApplet;
import processing.core.PVector;

import oscP5.*;
import netP5.*;

/*
 * TO DO: get OSC data send from within this class. At the moment work-around by sending from main class
 */

////////////////////////////////////////////////////////////////////////////
// The class AttractingBalls creates two balls, that dance with each other
////////////////////////////////////////////////////////////////////////////

public class AttractingBalls implements runnableLedEffect {
	String name = "attractingBalls";
	PVector gravitation = new PVector(0f, 0.0008f, 0f);
	float centerGravitation = 0.0015f;
	float deltaSpeed = 5; // determinates the tempo for the changing of the leader force
	float fraction = 50; // devides the force of the leading ball
	float followMinForce = -0.0025f, followMaxForce = 0.0025f; // determes how the follow ball follows the leader. 
	float destinationForce = 0.0015f; // the force to go to the MOST FREE SPACE
	// the force is linked to the disrance. the negativ value means, that it gets pushed when it comes to nearer
	float wallFriction = 0.003f;
	Ball b[] = new Ball[2];
	RemoteControlledFloatParameter centerGrav, leaderDelta, leaderForce, followMin, followMax, destForce;
	RemoteControlledFloatParameter geneticForce;
	RemoteControlledIntParameter generations, population, DNALength;
	int numGeneration, numPopulation, lengthDNA;
	float genForce;
	LedColor leaderCol = new LedColor((50f / 255f), (170f / 255f), 1f);
	LedColor followCol = new LedColor(1f, (110f / 255f), (40f / 255f));
	LedColor DNACol = new LedColor(255f/255, (255f / 255f), (255f / 255f));
	PApplet papplet;

	AttractingBalls() {
		// initialize Bälle
		b[0] = new Ball(0.15f, 0, 1, true, leaderCol,DNACol);
		b[1] = new Ball(0.15f, 1, 0, false, followCol,DNACol);
		centerGrav = new RemoteControlledFloatParameter("AttractingBalls/centerGrav", 0.f, 0.f, 1f);
		destForce = new RemoteControlledFloatParameter("AttractingBalls/destForce", 0.0015f, 0.f, 0.01f);
		leaderDelta = new RemoteControlledFloatParameter("AttractingBalls/leaderDelta", 5f, 0f, 200f);
		leaderForce = new RemoteControlledFloatParameter("AttractingBalls/leaderForce", 0.0015f, 0.f, 0.01f);
		followMin = new RemoteControlledFloatParameter("AttractingBalls/followMin", -0.0025f, -0.1f, 0.f);
		followMax = new RemoteControlledFloatParameter("AttractingBalls/followMax", 0.0025f, 0.f, 0.1f);
		geneticForce = new RemoteControlledFloatParameter("AttractingBalls/geneticForce", 0.35f, 0.f, 1f);
		population = new RemoteControlledIntParameter("AttractingBalls/population", 200, 1, 2000);
		generations = new RemoteControlledIntParameter("AttractingBalls/generations", 6, 1, 100);
		DNALength = new RemoteControlledIntParameter("AttractingBalls/DNALength", 60, 1, 100);
		
/*
		OscMessage posMessage = new OscMessage("ball/pos");
		posMessage.add(1);
*/	

	}

	public LedColor[] drawMe() {
		//update all remote controlled values an map them to the right range
		centerGravitation = papplet.map(centerGrav.getValue(),0f,1f,0f,0.1f);
		deltaSpeed = leaderDelta.getValue();	
		fraction = leaderForce.getValue();
		followMinForce = followMin.getValue();
		followMaxForce = followMax.getValue();
		destinationForce = destForce.getValue();
		genForce=papplet.map(geneticForce.getValue(), 0f, 1f, 0f, 0.025f);
		numPopulation=population.getValue();
		numGeneration=generations.getValue();
		lengthDNA=DNALength.getValue();
		b[0].step();
		b[1].step();
		
		b[0].centerForce(centerGravitation);
		b[1].centerForce(centerGravitation);
//		b[0].leaderForce(deltaSpeed, fraction);
//		b[1].leaderForce(deltaSpeed, fraction);
//		b[0].followForce(b[1], followMinForce, followMaxForce);
//		b[1].followForce(b[0], followMinForce, followMaxForce);

		/*
		b[0].determineMostSpace(b[1]);
		b[1].determineMostSpace(b[0]);
		b[0].destForce(destinationForce);
		b[1].destForce(destinationForce);
		*/
		
//		PApplet.println("++++++++++++++BALL 1+++++++++++++++");
		b[0].makeAcceleration(b[1],ortlicht.sculptureRadius, numPopulation,numGeneration,genForce, lengthDNA);

//		PApplet.println("++++++++++++++BALL 2+++++++++++++++");
		b[1].makeAcceleration(b[0],ortlicht.sculptureRadius, numPopulation,numGeneration,genForce, lengthDNA);
		
		b[0].collisionWall(wallFriction);
		b[1].collisionWall(wallFriction);
		b[0].collisionBalls(b[1], leaderCol, followCol);
		
		b[0].sendPosition();
		b[1].sendPosition();
		b[0].sendDistance(b[1]);

		LedColor[] ballBufferLedColors = b[0].drawMe();
		LedColor[] ball2BufferLedColors = b[1].drawMe();
		for (int j = 0; j < ortlicht.ledColors.length; j++) {
			ballBufferLedColors[j].mixWithAlpha(ball2BufferLedColors[j], LedColor.LedAlphaMode.ADD, 1);
		}


		return ballBufferLedColors;
	}


	public String getName() {
		return name;
	}
	public PVector getB0pos(){
		return b[0].getPosition();
	}
	public PVector getB1pos(){
		return b[1].getPosition();
	}
}


