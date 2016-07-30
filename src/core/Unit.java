package core;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Random;

import helpers.Player;
import helpers.UnitType;


public class Unit implements Serializable{
	

	private static final long serialVersionUID = 1L;
	private int x;
	private int y;
	
	private ArrayList<int[]> path = new ArrayList<int[]>();
	private ArrayList<Double> cost = new ArrayList<Double>();

	private UnitType type;
	private int hp;
	private double move;
	private int moveStep = 0;
	private int level = 1;
	private Unit target = null;
	private int damage;
	private String name;
	private Player owner;
	private Random rand;

	
	public Unit(UnitType unitType, Player owner, int x, int y, String n, Random rand) {
		type = unitType;
		this.owner = owner;
		this.x = x;
		this.y = y;
		this.hp = type.getMaxHP();
		this.move = type.getMovement();
		this.name = n;
		this.rand = rand;
	}
	
	public BufferedImage getImage() {
		return type.getImage(owner);
	}

	public int getX() {
		return x;
	}

	public int getSight() {
		return type.getSight();
	}

	public int getY() {
		return y;
	}



	public int getRange(String attack) {
		return Math.round(type.getRange() * type.getRangeMod(attack));
	}

	public int getAcc(String attack, boolean up) {
		return Math.round(type.getAcc() * type.getAccMod(attack) * (up?.5f:1.0f));
	}

	private int getFirePower(String attack) {
		return Math.round(type.getFirePower() * type.getFirePowerMod(attack));
	}
	
	public int getAttackCost(String attack){
		return Math.round(type.getBaseAttackCost() * type.getMoveCostMod(attack));
	}

	public double getMovement() {
		return move;
	}

	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
		
	}

	public void setMovement(double moveCost) {
		this.move = moveCost;
		
	}

	public int getMaxMove() {
		return type.getMovement();
	}

	public void pushNext(int[] step, double c) {
		assert step.length == 2;
		path.add(0, step);
		cost.add(0, c);
		
	}

	public boolean isActive() {
		return isMoving() || isShooting();
	}

	public boolean isShooting() {
		return target != null;
	}

	public boolean isMoving() {
		return path.size() > 0;
	}

	public int getMoveStep() {
		return moveStep;
	}
	
	public double popCost(){
		return cost.remove(0);
	}

	public int[] popNext() {
		moveStep = 0;

		return path.remove(0);
	}

	public void tick() {
		moveStep++;
	}

	public int[] lookAtNext() {
		return path.get(0);
	}

	public void stop() {
		path.clear();
		cost.clear();
	}

	public ListIterator<int[]> getPathIterator() {
		return path.listIterator();
	}


	public int getNumAttacks() {
		return type.getNumAttack();
	}

	public UnitType getType() {
		return type;
	}

	public String sidebarPrint(String attack) {
		String str = name + "\n"
		+ "Level: " + level + "\n"
		+ "HP: " + hp + " / " + type.getMaxHP() + "\n"
		+ "Move: " + move + "/" + type.getMovement() + "\n";
		if(attack != null){
			str += "Fire Power: " + getFPString(attack) + "\n"
			+ "Range: " + getRangeString(attack) + "\n"
			+ "Move Cost: " + getAttackCostString(attack) + "\n";
		}else{
			str += "\n\n";
		}
			
		
		return str;
	}


	private String getAttackCostString(String attack) {
		String str = "";
		if(getAttackCost(attack) > getMovement()){
			str += "<font color='red'>";
		}
		str += getAttackCost(attack);
		if(getAttackCost(attack) > getMovement()){
			str += "</font>";
		}
		return str;
	}

	private String getFPString(String attack) {
		String str = "";
		if(getFirePower(attack) > type.getFirePower()){
			str += "<font color='green'>";
		}
		if(getFirePower(attack) < type.getFirePower()){
			str += "<font color='red'>";
		}
		str += getFirePower(attack);
		if(getFirePower(attack) != type.getFirePower()){
			str += "</font>";
		}
		return str;
	}
	
	
	private String getRangeString(String attack) {
		String str = "";
		if(getRange(attack) > type.getRange()){
			str += "<font color='green'>";
		}
		if(getRange(attack) < type.getRange()){
			str += "<font color='red'>";
		}
		str += getRange(attack);
		if(getRange(attack) != type.getRange()){
			str += "</font>";
		}
		return str;
	}

	public void shootAt(Unit unit, String attack, boolean up) {
		target = unit;
		moveStep = 0;
		move = 0;
		damage = 0;
		int fp = getFirePower(attack);
		for(int i = 0; i < fp; i++){
			if(rand.nextInt(100) < getAcc(attack, up)){
				damage++;
			}
		}
		unit.reduceHP(damage);
	}

	private void reduceHP(int d) {
		hp -= d;
	}

	public void doneShooting() {
		target = null;
		moveStep = 0;
	}

	public Unit getTarget() {
		assert target != null;
		return target;
	}

	public int getDamage() {
		return damage;
	}

	public int getHP() {
		return hp;
	}

	public String getName() {
		return name;
	}

	public Player getOwner() {
		return owner;
	}





	
}
