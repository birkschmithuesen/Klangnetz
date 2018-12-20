import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import oscP5.OscMessage;
import oscP5.OscP5;
import netP5.*;
import processing.core.PApplet;
import processing.core.PVector;
//import java.io.*;

public class ortlicht extends PApplet {
	public static void main(String[] args) {
		PApplet.main("ortlicht");
	}
	// import themidibus.*; //Import the library
	// MidiBus midi; // The MidiBus

	int artNetFrameSkip=3; // push only every n'th frame to the leds to allow higher framerate in simulation
	int globalFramerate=120;
	// layout of Stripes
	int numStripes = 8;
	int numLedsPerStripe = 600;
	int numStripesPerController = 2;
	StripeConfigurator stripeConfiguration = new StripeConfigurator(numStripes, numLedsPerStripe,
			numStripesPerController); // used to generate per led info.

	// ip -configuration of Art-Net-Interface
	String ipPrefix = "2.0.0."; // first three numbers of IP adreess of
	// controlles
	int startIP = 100; // last number of first controller IP
	ArtNetSender artNetSender = new ArtNetSender(stripeConfiguration, ipPrefix, startIP); // used
	// to
	// send
	// data
	// to
	// leds

	LedVisualizer visualizer = new LedVisualizer(this);

	// all data about our leds
	public static PVector[] ledPositions;
	public static PVector[] ledNormals;
	public static LedColor[] ledColors;
	LedInStripeInfo[] stripeInfos;
	LedInNetInfo[] ledNetInfo;
	ArrayList <LedNetworkNode> listOfNodes; 
	Mixer mixer;

	//effekte
	LedNetworkTransportEffect ledNetworkTransportEffect;
	MovingWallEffect movingWall;
	// Information about the sculpture dimensions
	public static float sculptureRadius = 0.2f; // <<<<----------------------this
	// must be calculated
	// automatically
	public static LedBoundingBox boundingBox;
	// EFX Objects
	// ExpandingSphere expandingSphere;
	// Bälle
	// AttractingBalls attractingBalls;

	// BPM Master
	public static BpmClock bpmClock = new BpmClock();

	// OSC Remote Control
	public static OscP5 oscP5;
	public static NetAddress soundGeneratorLocation;

	// One PApplet object to hand it over to every class that needs it.
	PApplet papplet; 

	long frameCount=0;

	public void settings(){
		oscP5 = new OscP5(this,8001);  //given variable defines the port
		size(100, 100, P3D);

	}

	public void setup(){
		frameRate(globalFramerate);
		soundGeneratorLocation = new NetAddress("127.0.0.1",8000);
		ledPositions=LedPositionFile.readFromFile(dataPath("ledPositions.txt")); // read positions from file
		ledNormals=LedPositionFile.readFromFile(dataPath("regressionNormals.txt")); // read positions from file
		ledColors=LedColor.createColorArray(ledPositions.length);        // build a color buffer with the length of the position file
		stripeInfos= stripeConfiguration.builtStripeInfo();                      // create stripe date for each LED (used only for specific visualizations
		ledNetInfo=LedInNetInfo.buildNetInfo(numStripes, numLedsPerStripe); //create an Array with data for each LED if they are part of a node
		listOfNodes=LedInNetInfo.buildClusterInfo(ledNetInfo, ledPositions);  // all sets of Leds that are on different stripes but close to each other

		mixer = new Mixer();

		//get Infos about sculpture
		boundingBox=LedBoundingBox.getForPositions(ledPositions);

		println("minX: "+boundingBox.minX);
		println("maxX: "+boundingBox.maxX);
		println("minY: "+boundingBox.minY);
		println("maxY: "+boundingBox.maxY);
		println("minZ: "+boundingBox.minZ);
		println("maxZ: "+boundingBox.maxZ);


		//initialize EFX 
		// ExpandingSphere

		// expandingSphere=new ExpandingSphere(ledPositions, thickness, new LedColor(0.2, 1, 0.5));

		// sets up a midi bus
		//  midi = new MidiBus(this, "Bus 1", "Bus 1");

		//add effects to EffectArray

		//		   mixer.addEffect(new AttractingBalls());
		ledNetworkTransportEffect = new LedNetworkTransportEffect("1", ledPositions, ledNetInfo,listOfNodes, oscP5, soundGeneratorLocation);
		mixer.addEffect(ledNetworkTransportEffect);

		mixer.addEffect(new MovingWallEffect(ledPositions,"quer"));
		mixer.addEffect(new MovingWallEffect(ledPositions,"laengs"));		

		mixer.addEffect(new LedNetworkNodeEffects("1", ledPositions, ledNetInfo, listOfNodes));


		/*
	      mixer.addEffect(new ExpandingSphere("/1/", ledPositions, new LedColor(0.2f, 1f, 0.5f),sculptureRadius, 0.1f));
	      mixer.addEffect(new ExpandingSphere("/2/", ledPositions, new LedColor(1f, 0.2f, 0.5f),sculptureRadius, 0.1f));
	      mixer.addEffect(new ExpandingSphere("/3/", ledPositions, new LedColor(1f, 0.2f, 0.5f),sculptureRadius, 0.1f));
	      mixer.addEffect(new ExpandingSphere("/4/", ledPositions, new LedColor(1f, 0.2f, 0.5f),sculptureRadius, 0.1f));
	      mixer.addEffect(new ExpandingSphere("/5/", ledPositions, new LedColor(1f, 0.2f, 0.5f),sculptureRadius, 0.1f));

	      mixer.addEffect(new DirectionLight("/1/", ledNormals, new LedColor(1f, 0.2f, 0.5f)));
	      mixer.addEffect(new DirectionLight("/2/", ledNormals, new LedColor(1f, 0.2f, 0.5f)));
	      mixer.addEffect(new DirectionLight("/3/", ledNormals, new LedColor(1f, 0.2f, 0.5f)));
	      mixer.addEffect(new DirectionLight("/4/", ledNormals, new LedColor(1f, 0.2f, 0.5f)));
	      mixer.addEffect(new DirectionLight("/5/", ledNormals, new LedColor(1f, 0.2f, 0.5f)));

		 */

		//	  mixer.addEffect(new ManualSphere("/1/", ledPositions, thickness, new LedColor(0.2f, 1f, 0.5f)));
		//	  mixer.addEffect(new ManualSphere("/2/", ledPositions, thickness, new LedColor(0.2f, 1f, 0.5f)));
		//	  mixer.addEffect(new ManualSphere("/3/", ledPositions, thickness, new LedColor(0.2f, 1f, 0.5f)));

		//to save remote Settings and Preset Data
		try{
			DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(dataPath("remoteSettings.txt")));
			OscMessageDistributor.dumpParameterInfo(dataOut);
		} catch (FileNotFoundException e){
			println("file not found");
		}

	}


	public void draw() {
		background(0);

		OscMessageDistributor.distributeMessages();
		bpmClock.update();
		ledColors=mixer.mix();


		drawScreen();
		//send data to ArtNet Interface
		if(frameCount%artNetFrameSkip==0) {
			artNetSender.sendToLeds(ledColors);
		}


		frameCount++;
	}


	public void mouseClicked(){
		ledNetworkTransportEffect.createRandomActivation();
	}

	public void stop(){
		oscP5.stop();
	}

	void drawContent(){
		/*
		  // draw a volumentric sphere into the led color buffer
		  LedSphereDrawer.drawSphere(
		    ledPositions, // array of LED-positions (as PVector)
		    ledColors, // array of LED Colors (as LedColor)
		    new PVector(sin(millis()/1000.0+3)*0.3, sin(millis()/700.0+1)*0.3, sin(millis()/3000.0+2)*0.3), //center of the sphere 
		    0.1, // outer radius of the hollow sphere
		    0.0, // inner radius of the hollow sphere
		    new LedColor(1, 0.5, 0.2),  // color of the sphere
		    LedColor.LedAlphaMode.ADD
		    );
		 */
	}



	void drawScreen(){
		// draw the leds on screen 
		visualizer.drawLeds(
				ledPositions, // array of LED-positions (as PVector)
				ledColors, // array of LED Colors (as LedColor) 
				true, //shall we draw a dark gray ring around the Leds? (helps to see structure of unlit stripe
				1//millis()/7000.0f  //rotation angle around y axis (so we see the model from all sides
				);
	}

	void oscEvent(OscMessage theOscMessage) {
		OscMessageDistributor.queueMessage(theOscMessage);
	}

}
