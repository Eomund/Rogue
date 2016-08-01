package helpers;
import java.awt.Color;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.Timer;

import core.Cell;
import core.GameState;
import core.GameType;
import core.Unit;


public abstract class Player implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private Color colour;
	private String name;
	private Zone zone;
	private ArrayList<Unit> myUnits = new ArrayList<>();
	private int score = 0;
	private GameType game;
	
	public abstract void yourTurn();
	public abstract void noLongerYourTurn();
	public abstract String getAttack();
	public abstract void doSomething();
	public abstract void unitUpdated(Unit unit);
	public abstract void watch(Unit unit);
	
	public Player(Color colour, String name, GameType gt) {
		super();
		this.colour = colour;
		this.name = name;
		this.game = gt;
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


	public void setGame(GameType game2) {
		this.game = game2;
	}

	
	public int getRandInt(int bound){
		return game.gs.getRandInt(bound);
	}
	
	public void calculateShooting(Unit who, String attack){
		assert who.getOwner() == this;
		
		game.gs.calculateShooting(who, attack);
	}


	public ArrayList<Player> getPlayers(){
		return game.getPlayers();
	}
	
	public Cell getCell(Unit u){
		return getCell(u.getX(), u.getY());
	}
	
	public Cell getCell(int x, int y){
		Cell cell = game.gs.getCell(x, y);
		if(cell.isViewed(this)){
			return cell;
		}else if(cell.hasViewed(this)){
			Cell ret = new Cell(cell);
			ret.removeUnit();
			return ret;
		}else{
			return null;
		}
	}
	
	public int getMapWidth(){
		return game.gs.getMapWidth();
	}

	public int getMapHeight(){
		return game.gs.getMapHeight();
	}

	public void findWhereYouCanMove(Unit unit) {
		if(unit != null && unit.getOwner() == this && game.getCurrentPlayer() == this){
			Iterator<ArrayList<Cell>> it = game.gs.getMapIterator();
			while(it.hasNext()){
				for(Cell cell: it.next()){
					cell.setMoveCost(-1);
					cell.setFrom(-1, -1);
				}
			}
			game.gs.getCell(unit).setMoveCost(unit.getMovement());
			moveFrom(unit.getX(), unit.getY(), unit.getMovement());
			
			
		}
	}
	


	private void moveFrom(int x, int y, double m) {
		for(int i = -1; i <= 1; i++){
			for(int j = -1; j <= 1; j++){
				if(i == 0 && j == 0){
					continue;
				}
				if(x + i >= 0 && x + i <game.gs.getMapWidth() && y + j >= 0 && y + j <game.gs.getMapHeight()){
					double mc = game.gs.getMoveCost(x, y, x + i, y + j, game.human);
					if(mc != GameState.IMPOSSIBLE){
						if(i != 0 && j != 0){
							mc *= 1.5;
						}
						double ml = m - mc;
						if(ml >= 0 && ml > game.gs.getCell(x + i, y + j).getMoveCost()){
							game.gs.getCell(x + i, y + j).setMoveCost(ml);
							game.gs.getCell(x + i, y + j).setFrom(x, y);
							moveFrom(x + i, y + j, ml);
						}
					}
				}
			}
		}
	}

	public Player getGameCurrentPlayer() {
		return game.getCurrentPlayer();
	}
	
	public int getMapHighest(){
		return game.gs.getHighest();
	}
	
	public void saveGame(ObjectOutputStream out) throws IOException{
		out.writeObject(game);
		out.close();
	}
	

	public void addAction(Unit unit, int x, int y, Action.Type type){
		game.addAction(new Action(unit, x, y, type, this));
	}

	public void endTurn(){
		game.addAction(new Action(this));
	}
	
	public double getCellMoveCost(int x, int y){
		if(game.getCurrentPlayer() == this){
			return game.gs.getCell(x, y).getMoveCost();
		}
		return GameState.IMPOSSIBLE;
	}
	
	public int getCellFromX(int x, int y){
		if(game.getCurrentPlayer() == this){
			return game.gs.getCell(x, y).getFromX();
		}
		return -1;
	}
	
	public int getCellFromY(int x, int y){
		if(game.getCurrentPlayer() == this){
			return game.gs.getCell(x, y).getFromY();
		}
		return -1;
	}
	
	public Timer getTime(){
		return game.getTime();
	}

}

