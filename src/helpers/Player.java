package helpers;
import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;

import core.Unit;


public class Player implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private Color colour;
	private String name;
	private Zone zone;
	private ArrayList<Unit> myUnits = new ArrayList<>();
	private int score = 0;
	
	
	public Player(Color colour, String name) {
		super();
		this.colour = colour;
		this.name = name;
	}


	public void setZone(Zone zone) {
		assert zone.isGood();
		this.zone = zone;
		
	}


	public Zone getZone() {
		return zone;
	}


	public Color getColour() {
		return colour;
	}


	public boolean isAI() {
		return this instanceof AI;
	}


	public boolean isHuman() {
		return !(this instanceof AI);
	}


	public String getName() {
		return name;
	}


	public void removeUnit(Unit u) {
		myUnits.remove(u);
	}


	public ArrayList<Unit> getUnits() {
		return myUnits;
	}


	public void resetMovement() {
		for(Unit u:myUnits){
			u.setMovement(u.getMaxMove());
		}
	}


	public void addUnit(Unit unit) {
		myUnits.add(unit);
	}


	public int getScore() {
		return score;
	}


	public void setScore(int s) {
		score = s;
	}


	public void addToScore(int i) {
		score += i;
	}


	public void removeZone() {
		zone = null;
		
	}



}
