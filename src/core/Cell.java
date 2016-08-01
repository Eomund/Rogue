package core;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import helpers.Corner;
import helpers.Player;
import helpers.Terrain;


public class Cell implements Serializable{


	private static final long serialVersionUID = 1L;
	private static final int NOTSET = -1;
	private int elevation;
	private Corner corner[] = new Corner[4];
	private int edge[] = new int[2];
	private Terrain terr;
	private double angle;
	private boolean angleSet = false;
	private double move = -1;
	
	public enum ViewState{NOTHING, VIEWING, VIEWED};
	
	private HashMap<Player, ViewState> viewStatus = new HashMap<>();
	
	private HashMap<Player, Boolean> canShoot = new HashMap<>();
	
	private Unit unit;
	private int fromX = -1;
	private int fromY = -1;
	
	private int region = -1;

	
	private boolean occupied = false;

	public Cell(Cell cell) {
		this.unit = cell.unit;
		this.fromX = cell.fromX;
		this.fromY = cell.fromY;
		this.region = cell.region;
		this.occupied = cell.occupied;
		this.canShoot = cell.canShoot;
		this.viewStatus = cell.viewStatus;
		this.elevation = cell.elevation;
		this.corner = cell.corner;
		this.edge = cell.edge;
		this.terr = cell.terr;
		this.angle = cell.angle;
		this.angleSet = cell.angleSet;
		this.move = cell.move;
	}
	
	public boolean isOccupied() {
		return occupied;
	}


	public void setOccupied(boolean occupied) {
		this.occupied = occupied;
	}



	public Cell(int e, Terrain t) {
		elevation = e;
		terr = t;
	}




	public int getElevation() {
		return elevation;
	}


	public void addCorner(int i, Corner.Type t, int h, Terrain terrain) {
		corner[i] = new Corner(t, h, terrain);
		
	}


	public Corner getCorner(int i) {
		return corner[i];
	}


	public void addEdge(int e, int h) {
		edge[e] = h;		
	}


	public int getEdge(int i) {
		return edge[i];
	}
	
	@Override
	public String toString(){
		String str = "";
		str += "MC: " + move + "\n";
		str += "El: " + elevation + "\n";
		for(int i = 0; i < 4; i++){
			if(corner[i] != null){
				str += "C" + i + ": " + corner[i] + "\n";
			}
		}
		
		for(int i = 0; i < 2; i++){
			str += "E" + i + ": " + edge[i] + "\n";
		}
		return str;
	}


	public void lowerBy(int amount) {
		elevation = elevation - amount;
	}


	public void setElevation(int newEl) {
		elevation = newEl;
		
	}


	public void setTerrain(Terrain terrain) {
		assert terrain != null;
		terr = terrain;
		
	}




	public Terrain getTerrain() {
		return terr;
	}
	
	


	public void setUnit(Unit unit) {
		this.unit = unit;
		
	}


	public Unit getUnit() {
		return unit;
	}


	public void resetView() {
		Iterator<Entry<Player, ViewState>> it = viewStatus.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry<Player, ViewState>pair = it.next();
			if(pair.getValue() != ViewState.NOTHING){
				pair.setValue(ViewState.VIEWED);
			}else{
				pair.setValue(ViewState.NOTHING);
			}
		}
		
	}


	public void resetAngle() {
		angleSet = false;
	}


	public boolean isAngleSet() {
		return angleSet;
	}


	public void setAngle(double ang) {
		angleSet = true;
		angle = ang;
	}


	public double getAngle() {
	//	assert angleSet;
		return angle;
	}

		
	public void setViewed(Player p) {
		viewStatus.put(p, ViewState.VIEWING);
	}


	public boolean isViewed(Player p) {
		return viewStatus.get(p) == ViewState.VIEWING;
	}


	public void setMoveCost(double ml) {
		move = ml;
		
	}


	public double getMoveCost() {
		return move;
	}


	public void setFrom(int x, int y) {
		fromX = x;
		fromY = y;
		
	}


	public int getFromX() {
		return fromX;
	}

	public int getFromY() {
		return fromY;
	}


	public void removeUnit() {
		unit = null;
		
	}


	public boolean hasViewed(Player p) {
		return viewStatus.get(p) == ViewState.VIEWED || viewStatus.get(p) == ViewState.VIEWING;
	}


	public void itCanBeShot(Player p) {
		canShoot.put(p, true);
		
	}


	public boolean canItBeShot(Player p) {
		return canShoot.get(p);
	}


	public void itCannotBeShot(Player p) {
		canShoot.put(p, false);
		
	}
	
	public void setRegion(int r){
		region = r;
	}
	
	public boolean isRegionSet(){
		return region != NOTSET;
	}
	
	public int getRegion(){
		return region;
	}

}
