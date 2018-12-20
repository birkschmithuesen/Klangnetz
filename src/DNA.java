
import processing.core.PApplet;
import processing.core.PVector;
import java.util.Random;

////////////////////////////////////////////////////////////////////////////
// Stores a DNA for the evolutionary algorithm to determine the way/Vector for an object
//////////////////////////////////////////////////////////////////////////// 

class DNA {
	// The genetic sequence - in this case just one genes / one step
	PVector[] genes;
	final PApplet papplet;
	
	Random rnd = new Random(123);
	
	 // Constructor (makes a DNA regulated by a maximal force)
	DNA(float _maxForce, int _length){
		papplet = new PApplet();
		genes = new PVector[_length];
		for (int i=0; i<genes.length; i++){
			//generates a random normalized vector
			genes[i] = new PVector(papplet.random(-1,1),papplet.random(-1,1),papplet.random(-1,1));
			//applies a random force to the vector
			genes[i].mult(_maxForce*papplet.random(0,1));
		}
	}
	
	// Constructor #2, creates the instance based on an existing Vector
	DNA(PVector[] _newgene){
		papplet = new PApplet();
		genes=_newgene;
	}

	/*
	//CROSSOVER
	//Creates new DNA sequence from two (this & and a partner)
	DNA crossover(DNA _partner){
	    PVector[] child = new PVector[genes.length];
	    for (int i = 0; i < genes.length; i++) {
	    	float randomizer=papplet.random(0,1);
	      if (randomizer > 0.5f) child[i] = genes[i].copy();
	      else               child[i] = _partner.genes[i].copy();
	    }    
	    
	    for (int i=0; i<genes.length; i++){
	    	if(child[i].mag()>maxForce)child[i].setMag(maxForce);
	    }
	    
	    DNA newgenes = new DNA(child);
	    //papplet.println(newgenes.getGen(0).mag());
	    return newgenes;
	}
	*/
	
	//CROSSOVER
	//Creates new DNA sequence from two (this & and a partner)
	DNA crossover(DNA _partner){
	    PVector[] child = new PVector[genes.length];
	    // Pick a midpoint
	    int crossover = (int)(papplet.random(0,genes.length));
	    // Take "half" from one and "half" from the other
	    for (int i = 0; i < genes.length; i++) {
	      if (i > crossover) child[i] = genes[i].copy();
	      else               child[i] = _partner.genes[i].copy();
	    } 
	    DNA newgenes = new DNA(child);
	    return newgenes;
	}
	
	  // Based on a mutation probability, picks a new random Vector
	  void mutate(float m, float _maxForce) {
	    for (int i = 0; i < genes.length; i++) {
	      if (papplet.random(1) < m) {
	        genes[i] = new PVector(papplet.random(-1,1),papplet.random(-1,1),papplet.random(-1,1));
	        genes[i].mult(papplet.random(0, _maxForce));
	      }
	    }
	  }
	
	
	// erases the first gene and create a new one at the end - that should happen, when the real ball did one step / gene
	void moveGeneChain(float _maxForce){
		for(int i=0; i<genes.length-1;i++){
			genes[i]=genes[i+1].copy();
		}
		genes[genes.length-1]=new PVector(papplet.random(-1,1),papplet.random(-1,1),papplet.random(-1,1));
		genes[genes.length-1].mult(papplet.random(0, _maxForce));
	}
	
	void setAllGenesToPause(){
		for(int i=0; i<genes.length; i++){
			genes[i]=new PVector (0,0,0);
		}
	}
	
	PVector getGen(int _n){
		return genes[_n];
	}

	PVector[] getGenes(){
		return genes;
	}

	void setLength(int _length){
		PVector genesBuffer[]=new PVector[genes.length];
		genesBuffer=genes;
		genes=new PVector[_length];		
		for(int i=0; i<_length;i++){
			if(i<genesBuffer.length){
				genes[i]=genesBuffer[i].copy();
			}
			else{
				genes[i]=new PVector(papplet.random(-1,1),papplet.random(-1,1),papplet.random(-1,1));
			}
		}
			
	}
	

	
	int getLength(){
		return (int)genes.length;
	}
	
}
