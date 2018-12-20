import oscP5.*;
import processing.core.PApplet;
import processing.core.PVector;
import netP5.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

// models a set of activations travelling along the stripes
public class LedNetworkTransportEffect implements runnableLedEffect, OscMessageSink {


	PApplet papplet;
	String name = "Impulse";
	String id;
	PVector[] ledPositions;
	StripeInfo[] stripeInfos;
	LedInNetInfo[] ledNetInfo;
	LedColor[] bufferLedColors;
	ArrayList <LedNetworkNode> nodes;	
	double lastCyclePos=(double)System.currentTimeMillis()/1000;

	LinkedList<TravellingActivation> activations= new LinkedList<TravellingActivation>();

	//osc out
	OscP5 oscP5;
	NetAddress remoteLocation;


	//settings
	RemoteControlledFloatParameter nodeDeadTime; // Time between two activations of a node
	RemoteControlledFloatParameter impulseDecay; // loss of energy/second
	RemoteControlledFloatParameter impulseSpeed; // speed (leds/second)
	
	RemoteControlledFloatParameter impulseGamma= new RemoteControlledFloatParameter("/net/impulse/color/gamma", 0f, 0.1f, 5f);
	
	RemoteControlledFloatParameter impulseR;
	RemoteControlledFloatParameter impulseG;
	RemoteControlledFloatParameter impulseB;

	RemoteControlledFloatParameter fadeOutR;
	RemoteControlledFloatParameter fadeOutG;
	RemoteControlledFloatParameter fadeOutB;



	LedNetworkTransportEffect(String _id, PVector[] _ledPositions, LedInNetInfo[] _ledNetInfo,	ArrayList <LedNetworkNode> nodes_, OscP5 _oscP5, NetAddress _remoteLocation){
		id=_id;
		ledPositions = _ledPositions;
		bufferLedColors = LedColor.createColorArray(ledPositions.length);
		ledNetInfo=_ledNetInfo;
		nodes=nodes_;
		oscP5=_oscP5;
		remoteLocation=_remoteLocation;
		nodeDeadTime= new RemoteControlledFloatParameter("/net/impulse/nodeDeadTime", 0f, 0.0f, 10);
		impulseDecay= new RemoteControlledFloatParameter("/net/impulse/energyDecay", 0.05f, 0.0001f, 0.5f);
		impulseSpeed= new RemoteControlledFloatParameter("/net/impulse/speed", 20, 1, 200);

		
		impulseR= new RemoteControlledFloatParameter("/net/impulse/color/r", 1, 0, 1); // color of travelling impulse
		impulseG= new RemoteControlledFloatParameter("/net/impulse/color/g", 1, 0, 1); // color of travelling impulse
		impulseB= new RemoteControlledFloatParameter("/net/impulse/color/b", 1, 0, 1); // color of travelling impulse

		fadeOutR= new RemoteControlledFloatParameter("/net/impulse/fadeOut/r", 0.98f, 0f, 1f); // color of travelling impulse
		fadeOutG= new RemoteControlledFloatParameter("/net/impulse/fadeOut/g", 0.97f, 0f, 1f); // color of travelling impulse
		fadeOutB= new RemoteControlledFloatParameter("/net/impulse/fadeOut/b", 0.95f, 0f, 1f); // color of travelling impulse

		OscMessageDistributor.registerAdress("/net/activateNode",this);
	}

	public void digestMessage(OscMessage newMessage) {
		if (newMessage.checkAddrPattern("/net/activateNode") &&
				newMessage.arguments().length>0&&
				newMessage.getTypetagAsBytes()[0]=='i'
				) {
			int theValue=newMessage.get(0).intValue();
			if(theValue>0&&theValue<nodes.size()) {
				LedNetworkNode activeNode=nodes.get(theValue);
				int nLeds=ledNetInfo.length;
				for (Integer nodeLedIdx : activeNode.ledIndices) {
					LedInNetInfo curLedInfo=ledNetInfo[nodeLedIdx]; //which stripe are we on?
					//  activation spreads in boths directions
					int forwPos=nodeLedIdx +1;           
					if(forwPos>0&&forwPos<nLeds)activations.add(new TravellingActivation(forwPos, curLedInfo.stripeIndex, impulseSpeed.getValue(),1f ));
					//do not go back the same stripe:
					int backwPos=nodeLedIdx -1;            
					if(backwPos>0&&backwPos<nLeds)activations.add(new TravellingActivation(backwPos, curLedInfo.stripeIndex, -impulseSpeed.getValue(), 1f));
				}
			}
		}
	}
   public void writeToStream(DataOutputStream outStream) {
	    String outData="int"+"\t"+"/net/activateNode"+"\t"+"sactivateNode"+"\t"+0+"\t"+0+"\t"+(nodes.size()-1)+"\n";  
	    try {
	      outStream.writeBytes(outData);
	    }  
	    catch (
	      IOException e) {
	      System.err.println("Could not write to file"+e);
	    }
	  }

	//represents one travelling activation
	public class TravellingActivation {
		TravellingActivation(float ledIdxPos_, int stripeIdx_, float speed_, float energy_) {
			ledIdxPos=ledIdxPos_;
			stripeIdx=stripeIdx_;
			speed=speed_;
			energy=energy_;
		}

		int getLedIndex() {
			return (int)(ledIdxPos+0.5f); // global led position
		}
		float ledIdxPos; // absolute led position - used for mapping to led buffer
		int stripeIdx; // stripe the activation was created on
		float speed; // [leds/second] also encodes direction in sign
		float energy; // some measure of strength
	}




	//simulate one time step
	public LedColor[] drawMe() { 
		float spotR=impulseR.getValue(); 
		float spotG=impulseG.getValue();
		float spotB=impulseB.getValue();
		float gamma =impulseGamma.getValue();
		
		//parameters
		double currentTime=(double)System.currentTimeMillis()/1000;
		float timeStep=(float) (currentTime-lastCyclePos);
		lastCyclePos=currentTime;
		float speed=impulseSpeed.getValue(); 
		float energyLoss=impulseDecay.getValue();
		int nLeds=ledNetInfo.length;
		//iterate through activations and build a new list of activations in the meanwhile.
		LinkedList<TravellingActivation> newActivations=new LinkedList<TravellingActivation>();

		for (TravellingActivation curActivation : activations) {
			// let each activation travel a bit in it's direction
			curActivation.ledIdxPos+=curActivation.speed*timeStep;
			// if the activation hasn't fallen off the end of the stripe...
			int activationLedIdx=curActivation.getLedIndex(); // global led position
			// should the activation survive this round?
			if (
					activationLedIdx>=0&&activationLedIdx<=(nLeds-1)&& //ledIndex is valid
					ledNetInfo[activationLedIdx].stripeIndex==curActivation.stripeIdx&& // activation is in it's original stripe
					curActivation.energy>0
					) {
				//if activation hits a stripe crossing, create a new activation for each of the branches
				if (ledNetInfo[activationLedIdx].partOfNode!=null) {
					LedNetworkNode hitNode=ledNetInfo[activationLedIdx].partOfNode;
					// only multiply at nodes that have not been active for a while
					if(currentTime-hitNode.lastActivationTime>nodeDeadTime.getValue()) {

						hitNode.lastActivationTime=currentTime;
						//send osc Notification
						OscMessage myMessage = new OscMessage("/net/hitNode");
						myMessage.add(hitNode.id);
						myMessage.add(curActivation.energy);
						myMessage.add(hitNode.position.x);
						myMessage.add(hitNode.position.y);
						myMessage.add(hitNode.position.z);
						oscP5.send(myMessage, remoteLocation);


						float nActivations=hitNode.ledIndices.size();
						for (Integer nodeLedIdx : hitNode.ledIndices) {
							LedInNetInfo curLedInfo=ledNetInfo[nodeLedIdx]; //which stripe are we on?

							float childEnergy=curActivation.energy/nActivations/2.0f-energyLoss;

							int jump; // jump one led to avoid activating the same node over and over again
							if(curActivation.speed>0)jump=1;else jump=-1;
							//  activation spreads in boths directions
							int forwPos=nodeLedIdx +jump;           
							if(forwPos>0&&forwPos<nLeds)newActivations.add(new TravellingActivation(forwPos, curLedInfo.stripeIndex, curActivation.speed,childEnergy ));
							//do not go back the same stripe:
							if(nodeLedIdx!=activationLedIdx){
								int backwPos=nodeLedIdx -jump;            
								if(backwPos>0&&backwPos<nLeds)newActivations.add(new TravellingActivation(backwPos, curLedInfo.stripeIndex, -curActivation.speed, childEnergy));
							}
						}
					}				
				} else {
					//nothing special has happened, keep the activation for next round.
					newActivations.add(curActivation);
				}
			}
		}
		activations=newActivations;

		//draw all
		LedColor.mult (bufferLedColors, new LedColor(fadeOutR.getValue(),fadeOutG.getValue(),fadeOutB.getValue()));
		for (LedNetworkTransportEffect.TravellingActivation curActivation : activations) {
			int curLedIndex=curActivation.getLedIndex(); // global led position
			float fade=(float)Math.pow(curActivation.energy, gamma);
			bufferLedColors[curLedIndex].set(spotR*fade, spotG*fade, spotB*fade);
		}

		return bufferLedColors;
	}

	void createRandomActivation() {
		int ledIdx=200;//papplet.floor(papplet.random(ledNetInfo.length));
		activations.add(new TravellingActivation(ledIdx, ledNetInfo[ledIdx].stripeIndex, 20, 1));
	}

	public String getName() {
		return name;
	}

}

