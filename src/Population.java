import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;

////////////////////////////////////////////////////////////////////////////
//Describes a population of virtual (not for display purpose) balls
////////////////////////////////////////////////////////////////////////////

public class Population {
	ArrayList<Ball> population; // Array to hold the current population
	ArrayList<Ball> matingPool; // ArrayList which we will use for our "mating pool"
	ArrayList<Ball> winner; // ArrayList where the winner of each generation is saved
	int generations, lengthDNA; // Number of generations
	float maxForce = 0.001f;
	static PApplet papplet;
	PVector pos, vel;
	float r;

	// Initialize the population
	Population(int _num, PVector _pos, PVector _vel, float _r, float _force, int _lengthDNA) {
		papplet = new PApplet();
		pos = _pos;
		vel = _vel;
		r = _r;
		maxForce = _force;
		population = new ArrayList<Ball>();
		matingPool = new ArrayList<Ball>();
		winner = new ArrayList<Ball>();
		generations = 0;
		lengthDNA = _lengthDNA;
		// make a new set of virtual balls
		for (int i = 0; i < _num; i++) {
			DNA theDNA=new DNA(maxForce, lengthDNA);
			
			population.add(new Ball(pos, vel, r, theDNA));
		}
	}

	// moves the gene Chain for each simulated ball
	void moveGeneChain(){
		for (int i=0; i<population.size(); i++){
			population.get(i).dna.moveGeneChain(maxForce);
		}
	}
	
	// resizes the population
	void resize(int _num){
		//if the desired size is smaller
		if(_num<population.size()){
			//calculate the difference
			int difference = population.size()-_num;
			for(int i=0; i<difference;i++){
				//remove the last object from the list
				population.remove(population.size()-1);
			}
		}
		//if the desired size is higher
		else{
			//calculate the difference
			int difference = _num-population.size();
			for(int i=0; i<difference;i++){
				population.add(new Ball(pos, vel, r, new DNA(maxForce, lengthDNA)));
			}
		}
	}
	// calculates the fitness for each simulated ball
	void fitness(Ball _partner, float _sculptureRadius) {
		for (int i = 0; i < population.size(); i++) {
			population.get(i).fitness(_partner, _sculptureRadius);
		}
	}

	// Generate a mating pool
	void selection() {
		// Clear the ArrayList
		matingPool.clear();

		// Calculate total fitness of whole population
		float maxFitness = getMaxFitness();

		// Calculate fitness for each member of the population (scaled to value between 0 and 1)
		// Based on fitness, each member will get added to the mating pool a certain number of times
		// A higher fitness = more entries to mating pool = more likely to be picked as a parent
		// A lower fitness = fewer entries to mating pool = less likely to be picked as a parent
		for (int i = 0; i < population.size(); i++) {
			if (population.get(i).getFitness() > 0f) {
				float fitnessNormal = PApplet.map(population.get(i).getFitness(), 0, maxFitness, 0, 1);
				int n = (int) (fitnessNormal * 100); // Arbitrary multiplier
				for (int j = 0; j < n; j++) {
					matingPool.add(population.get(i));
				}
			}
		}
		if (matingPool.isEmpty()) {
			papplet.println("Generation without succes");
			for (int i = 0; i < population.size(); i++) {
				matingPool.add(new Ball(pos, vel, r, new DNA(maxForce, lengthDNA)));
			}
		}
	}

	// Make the next generation
	void reproduction(float _mutationRate) {
		// Refill the population with children from the mating pool
		if (matingPool.size() > 0) {
			for (int i = 0; i < population.size(); i++) {
				// Pick two random parents out of the matingPool
				int m = (int) (papplet.random(0, matingPool.size()));
				int d = (int) (papplet.random(0, matingPool.size()));
				// Pick the parents from the pool
				Ball mom = matingPool.get(m);
				Ball dad = matingPool.get(d);
				// Get their genes
				DNA momgenes = mom.getDNA();
				DNA dadgenes = dad.getDNA();
				// Mate their genes
				DNA child = momgenes.crossover(dadgenes);
				//Mutate theier genes
				child.mutate(_mutationRate, maxForce);
				// Fill the new population with the new child
				population.remove(i);
				population.add(i,new Ball(pos, vel, r, child));
			}
			generations++;
		}
	}

	//returns the Winner Acceleration of the last population
	PVector getWinnerWayLastGeneration(){
		return getWinnerOfPopulation().dna.getGen(0);
	}
	
	// determine the winner Acceleration of all populations
	PVector[] getWinnerWay() {
		float record = 0;
		PVector[] winnerGen = new PVector[population.size()];
		for (int i=0; i<population.size(); i++){
			winnerGen[i]=new PVector (0,0,0);
		}
		int winnerGeneration=0;
		for (int i = 0; i < winner.size(); i++) {
			if (winner.get(i).getFitness() > record) {
				record = winner.get(i).getFitness();
				winnerGen = new PVector[winner.get(i).getDNA().genes.length];
				winnerGen = winner.get(i).getDNA().genes;
				winnerGeneration=i;
			}
		}
		return winnerGen;
	}
	
	Ball getWinner(){
		float record = 0;
		Ball winnerSimulation=new Ball();
		int winnerGeneration=0;
		for (int i = 0; i < winner.size(); i++) {
			if (winner.get(i).getFitness() > record) {
				record = winner.get(i).getFitness();
				winnerSimulation = winner.get(i);
				winnerGeneration=i;
			}
		}
		return winnerSimulation;
	}
	
	//returns the Ball with the highest fitness of the last generation
	Ball getWinnerOfPopulation(){
		float record = 0;
		int recordIndex = 0;
		for (int i = 0; i < population.size(); i++) {
			if (population.get(i).getFitness() > record) {
				record = population.get(i).getFitness();
				recordIndex = i;
			}
		}
		return population.get(recordIndex);
	}
	
	//updates the population to the ball real position
	void updateLocation(PVector _pos, PVector _vel){
		pos=_pos;
		vel=_vel;
	}
	
	// sets the length of the DNA's
	void setDNALength(int _length){
		for(int i=0; i<population.size();i++){
			population.get(i).setDNALength(_length);
		}
		lengthDNA=_length;
	}
	
	//sets the max Force
	void setMaxForce(float _force){
		maxForce=_force;
	}
	// find highest fitness for the population
	float getMaxFitness() {
		float record = 0;
		int recordIndex = 0;
		for (int i = 0; i < population.size(); i++) {
			if (population.get(i).getFitness() > record) {
				record = population.get(i).getFitness();
				recordIndex = i;
			}
		}
	
		//winner.add(population.get(recordIndex));
		return record;
	}

	int getGenerations() {
		return generations;
	}
	
	Ball getBall(int _population){
		return population.get(_population);
	}
	
	void debug(){
		papplet.println("population: "+population.size()+"\t"+"matingPool: "+matingPool.size()+"\t"+"winner: "+winner.size());
	}
}

