package core;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;

import gui.FileDialog;
import helpers.AI;
import helpers.Config;
import helpers.Corner;
import helpers.Player;
import helpers.Terrain;
import helpers.UnitType;
import helpers.Zone;


public class GameState implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private int mapwidth;
	private int mapheight;
	private static final int wetness = 2;
	
	private ArrayList<ArrayList<Cell>> map;
	private Random rand;
	private int highest;
	private HashMap<String, Terrain> terrs = new HashMap<String, Terrain>();
	private HashMap<String, UnitType> unitTypes = new HashMap<String, UnitType>();
	
	public static final int IMPOSSIBLE = -1;
	private static final int MOVEGUESS = 2;
	private int farLeft;
	private int farUp;
	private int farRight = 0;
	private int farDown = 0;
	private ArrayList<Zone> zones = new ArrayList<>();
	private int largestRegion = -1;
	private int armySize;
	private GameType game;


	public GameState(int mapwidth, int mapheight, int armySize){
		this.mapwidth = mapwidth;
		this.mapheight = mapheight;
		this.armySize  = armySize;
		farLeft = mapwidth;
		farUp = mapheight;

	}
	
	public void makeWorld(GameType g, String name, int hillyness, long mapSeed, long gs){
		game = g;
		writeToTemp(mapwidth, mapheight, hillyness, armySize, game.getPlayers(), name);
		
		for(String[] t: Config.rawTerrain){
			System.out.println("Loading terrain: " + t[0]);
			terrs.put(t[0].trim(), new Terrain(t[1].trim(), Config.isTrue(t[2].trim())));
		}
		
		for(String[] u: Config.rawUnits){

			
			int[] partNum = new int[7];
			for(int i = 2; i < partNum.length + 2; i++){
				partNum[i - 2] = Integer.parseInt(u[i].trim());
			}
			
			System.out.println("Loading unit : " + 	u[0].trim());
			unitTypes.put(u[0].trim(), new UnitType(u[0].trim(), u[1].trim(), partNum, game.getPlayers()));
			for(int i = 9; i < u.length; i+= 5){
				if(i + 5 > u.length){
					break;
				}
				
				
				float[] floats = new float[4];
				for(int j = 0; j < floats.length; j++){
					floats[j] = Float.parseFloat(u[i + j + 1]);
				}
				System.out.println("	Added Attack : " + u[i].trim());
				unitTypes.get(u[0].trim()).addAttack(u[i].trim(), floats);
			}
		}
		
		System.out.println(mapSeed);
		rand = new Random(mapSeed);
		boolean success = false;
		while(!success){
			success = generateElevation(hillyness) && generateWater() && generateRegions() && generateZones(gs) && testZones() && handOutZones() && generateLines() && generateUnits();
		}
		
	}



	private boolean generateZones(long gs) {
		long gameSeed = gs;
		System.out.println(gameSeed);
		rand.setSeed(gameSeed);
		
		
		System.out.println("Generating zones ...");
		
		int buffer = -1;
    	for(String[] u:Config.rawUnits){
    		int val = Integer.parseInt(u[2].trim());
    		if(val > buffer){
    			buffer = val;
    		}
    	}
    	
		Zone.size = (int)(Math.ceil(Math.sqrt(2 * armySize)) + 1);
		int horZone = (int)((mapwidth + buffer) / (float)(Zone.size + buffer));
		int verZone = (int)((mapheight + buffer) / (float)(Zone.size + buffer));

		int extraWidth = mapwidth - (horZone * Zone.size + (horZone - 1) * buffer);
		int extraHeight = mapheight - (verZone * Zone.size + (verZone -1) * buffer);
		int[] horbuffers = new int[horZone];
		int[] verbuffers = new int[verZone];
		ArrayList<Integer> choices = new ArrayList<Integer>();
		
		
		for(int i = 0; i < extraWidth; i++){
			if(choices.size() == 0){
				for(int j = 0; j < horbuffers.length; j++){
					choices.add(j);
				}
			}
			horbuffers[choices.remove(rand.nextInt(choices.size()))]++;
			
		}
		
		choices.clear();
		for(int i = 0; i < extraHeight; i++){
			if(choices.size() == 0){
				for(int j = 0; j < verbuffers.length; j++){
					choices.add(j);
				}
			}
			verbuffers[choices.remove(rand.nextInt(choices.size()))]++;
			
		}
		

		
		for(int i = 0; i < horZone; i++){
			for(int j = 0; j < verZone; j++){
				int x = i * (Zone.size + buffer);
				for(int k = 0; k < i; k++){
					x += horbuffers[k];
				}
				int y = j * (Zone.size + buffer);
				for(int k = 0; k < j; k++){
					y += verbuffers[k];
				}
				zones.add(new Zone(x, y));
			}
		}
		
		return true;
	}



	

	
	private static void writeToTemp(int mapw, int maph, int hillyness, int armysize, ArrayList<Player> players, String gtName) {
		File f = new File(FileDialog.fileRoot);
		if(!f.exists()){
			f.mkdirs();
		}
		f = new File(FileDialog.fileRoot + "/quick");
		try {
			f.createNewFile();
	
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
			out.writeInt(mapw);
			out.writeInt(maph);
			out.writeInt(hillyness);
			out.writeInt(armysize);
			out.writeObject(players);
			out.writeUTF(gtName);
			out.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}




	private boolean handOutZones() {
		for(Player p: game.getPlayers()){
			p.removeZone();
		}
		
		System.out.println("Handing out zones...");
		for(Player p: game.getPlayers()){
			int z = -1;
			boolean good = true;
			do{
				good = true;
				z = rand.nextInt(zones.size());
				
				if(!zones.get(z).isGood()){
					good = false;
					continue;
				}
				
				
				for(Player other:game.getPlayers()){
					if(other.equals(p)){
						continue;
					}
					if(other.getZone() == null){
						break;
					}
					if(other.getZone().equals(zones.get(z))){
						good = false;
						break;
					}
				}
					
				
			}while(!good);
			System.out.println(z + " given to " + p.getName());
			p.setZone(zones.get(z));
			
		}
		
		return true;
	}




	private boolean testZones() {
		for(Zone z:zones){
			z.makeBad();
		}
		
		System.out.println("Testing zones...");
		int goodZones = 0;
		for(Zone z: zones){
			z.findGoodness(this, largestRegion, armySize * 2);
			if(z.isGood()){
				goodZones++;
			}
		}
		System.out.println(goodZones + " > " + game.getNumPlayers());
		return goodZones > game.getNumPlayers();
		
	}




	private boolean generateRegions() {
		System.out.println("Generating regions...");
		int r = 0;
		for(int x = 0; x < mapwidth; x++){
			for(int y = 0; y < mapheight; y++){
				if(!getCell(x, y).isRegionSet() && getCell(x, y).getTerrain().isWalkable()){
					ArrayList<ArrayList<Integer>> first = new ArrayList<>(1);
					ArrayList<Integer> f = new ArrayList<>(2);
					f.add(x);
					f.add(y);
					first.add(f);
					walkTo(first, r);
					r++;
				}
			}
		}
		int[] regs = new int[r];
		
		for(int x = 0; x < mapwidth; x++){
			for(int y = 0; y < mapheight; y++){
				if(getCell(x, y).isRegionSet()){
					regs[getCell(x, y).getRegion()]++;
				}
			}
		}

		int big = -1;
		for(int i = 0; i < regs.length; i++){
			if(regs[i] > big){
				big = regs[i];
				largestRegion = i;
			}
		}
		
		return true;
	}




	private void walkTo(ArrayList<ArrayList<Integer>> places, int region) {
		
		ArrayList<ArrayList<Integer>> next = new ArrayList<>();
		for(ArrayList<Integer> p : places){
			int x = p.get(0);
			int y = p.get(1);
			getCell(x, y).setRegion(region);
		
		
			for(int i = -1; i <= 1; i++){
				for(int j = -1; j <= 1; j++){
					if(x + i >= 0 && x + i < mapwidth && y + j >= 0 && y + j < mapheight  && !getCell(x + i, y + j).isRegionSet() && getMoveCost(x, y, x + i, y + j) != IMPOSSIBLE){
						ArrayList<Integer> t = new ArrayList<>(2);
						t.add(x + i);
						t.add(y + j);
						if(!next.contains(t)){
							next.add(t);
						}
					}
				}
			}
			
		}
		if(next.size() > 0){
			walkTo(next, region);
		}
	}






	private boolean isUsed(int i) {
		for(Player p: game.getPlayers()){
			for(Unit u: p.getUnits()){
				if(i == Integer.parseInt(u.getName().split("[a-z]")[0])){
					return true;
				}
			}
		}
		return false;
	}




	private boolean generateUnits() {
		System.out.println("Generating units...");
		for(Player p:game.getPlayers()){
			for(int i = 0; i < armySize; i++){
				spawnUnit(p);
			}
		}
		
		calculateViewing();
		return true;
		
	}




	private void spawnUnit(Player p) {

		int x = -1;
		int y = -1;
		do{
			x = rand.nextInt(Zone.size) + p.getZone().getX();
			y = rand.nextInt(Zone.size) + p.getZone().getY();
		}while(getCell(x, y).getUnit() != null || getCell(x, y).getRegion() != largestRegion);
		
		getCell(x, y).setUnit(new Unit(unitTypes.get("Infantry"), p, x, y, generateName(unitTypes.get("Infantry")), rand));
		p.addUnit(getCell(x, y).getUnit());
		
	}




	private String generateName(UnitType type) {
		int num;
		do{
			num = rand.nextInt(100);
		}while(isUsed(num + 1));
		
		String name = String.valueOf(num);
		switch(num % 10){
		case 1:
			name += "st";
			break;
		case 2:
			name += "nd";
			break;
		case 3:
			name += "rd";
			break;
		default:
			name += "th";	
		}
		
		name += " " + type.getName() + " Regiment";
		return name;
	}




	public void calculateViewing(){
		for(ArrayList<Cell> row:map){
			for(Cell cell:row){
				cell.resetView();
			}
		}
		
		for(Player p:game.getPlayers()){
			for(Unit u: p.getUnits()){
				for(ArrayList<Cell> row: map){
					for(Cell cell: row){
						cell.resetAngle();
					}
				}
			
				double theta = Math.asin(1.0 / u.getSight()) / 2.0;
				for(int i = 0; i * theta < Math.PI * 2.0; i++){
					
					findLOS(u.getX(), u.getY(), u.getX() + u.getSight() * Math.cos(i * theta), u.getY() + u.getSight() * Math.sin(i * theta), u.getOwner(), false);
				
				}
				
			}
		}
		
	}
	


	
	
	
	public void calculateShooting(Unit who, String attack) {
		
		if(who != null){
			assert attack != null;
			for(ArrayList<Cell> row: map){
				for(Cell cell: row){
					cell.resetAngle();
					cell.itCannotBeShot(who.getOwner());
				}
			}
		
			double theta = Math.asin(1.0 / who.getRange(attack)) / 2.0;
			for(int i = 0; i * theta < Math.PI * 2.0; i++){
			
				findLOS(who.getX(), who.getY(), who.getX() + who.getRange(attack) * Math.cos(i * theta), who.getY() + who.getRange(attack) * Math.sin(i * theta), who.getOwner(), true);
			
			}
				
		}
		
	}



	private void findLOS(int fromX, int fromY, double tX, double tY, Player p, boolean shooting) {
		double toX = Math.min(Math.max(0,  tX), mapwidth - 1);
		double toY = Math.min(Math.max(0,  tY), mapheight - 1);
		ArrayList<int[]> targets = new ArrayList<int[]>();
		boolean swapmode = false;
		int step = fromX < toX? 1 : -1;
		double m = (toY - fromY) / (toX - fromX);
		if(Math.abs(m) > 1){
			m = 1 / m;
			swapmode = true;
			step = fromY < toY? 1 : -1;
		}
		double b;
		if(!swapmode){
			b = fromY - m * fromX;
		}else{
			b = fromX - m * fromY;
		}
		
		if(!swapmode){
			for(int x = fromX + step; (step == 1 && x <= toX) || (step == -1 && x >= toX); x += step){
				targets.add(new int[]{Math.round(x), (int) Math.round(m * x + b)});
			}
		}else{
			
			for(int y = fromY + step; (step == 1 && y <= toY) || (step == -1 && y >= toY) ; y += step){
				targets.add(new int[]{(int) Math.round(m * y + b), Math.round(y)});
			}
		}
		if(!shooting){
			if(p.isHuman()){
				setExtremes(fromX, fromY);
				getCell(fromX, fromY).setViewed(p);
			}
		}else{
			getCell(fromX, fromY).itCanBeShot(p);
		}
		
		double maxangle = Double.NEGATIVE_INFINITY;
		for(int i = 0; i < targets.size(); i++){

			Cell cell = getCell(targets.get(i)[0], targets.get(i)[1]);
			if(!cell.isAngleSet()){
				double angle = (cell.getElevation() - getCell(fromX, fromY).getElevation()) / (i + 1.0);
				cell.setAngle(angle);
			}
			if(cell.getAngle() >= maxangle){
				if(p.isHuman()){
					setExtremes(targets.get(i)[0], targets.get(i)[1]);
				}
				if(!shooting){
					cell.setViewed(p);
				}else{
					if(cell.isViewed(p)){
						cell.itCanBeShot(p);
					}
				}
				maxangle = cell.getAngle();
			}
		}
	}




	private void setExtremes(int x, int y) {
		if(x < farLeft){
			farLeft = Math.max(x, 0);
		}
		
		if(y < farUp){
			farUp = Math.max(y, 0);
		}
		
		if(x > farRight){
			farRight = Math.min(x, mapwidth);
		}
		
		if(y > farDown){
			farDown = Math.min(y, mapheight);
		}
		
	}







	private boolean generateWater() {
		System.out.println("Generating water...");
		int drops = rand.nextInt(wetness) + 1;
		for(int d = 0; d < drops; d++){
			ArrayList<ArrayList<Integer>> poss = new ArrayList<>();
			ListIterator<ArrayList<Cell>> outer = map.listIterator();
			while(outer.hasNext()){
				ListIterator<Cell> inner = outer.next().listIterator();
				while(inner.hasNext()){
					Cell cell = inner.next();
					for(int i = 0; i < highest - cell.getElevation(); i++){
						ArrayList<Integer> t = new ArrayList<>(1);
						t.add(inner.nextIndex() - 1);
						t.add(outer.nextIndex() - 1);
						poss.add(t);
					}
				}
			}
			if(poss.size() > 0){
				ArrayList<Integer> chosen = poss.get(rand.nextInt(poss.size()));
				int newEl = getCell(chosen).getElevation();
				ArrayList<ArrayList<Integer>> t = new ArrayList<>(1);
				t.add(chosen);
				flowTo(t, newEl);
			}
		}
		
		
		int lowest = Integer.MAX_VALUE;
		
		for(ArrayList<Cell> row: map){
			for(Cell c: row){
				if(c.getElevation() < lowest){
					lowest = c.getElevation();
				}
			}
		}
		for(ArrayList<Cell> row: map){
			for(Cell c: row){
				c.lowerBy(lowest);
				if(c.getElevation() > highest){
					highest = c.getElevation();
				}
				
			}
		}
		
		return true;
	}

	private void flowTo(ArrayList<ArrayList<Integer>> where, int newEl) {
		ArrayList<ArrayList<Integer>> next = new ArrayList<>();
		
		for(ArrayList<Integer> w: where){
			
			int x = w.get(0);
			int y = w.get(1);
			if(getCell(x, y).getElevation() < newEl || (getCell(x, y).getElevation() == newEl && getCell(x, y).getTerrain() != terrs.get("Water"))){
				getCell(x, y).setElevation(newEl);
				getCell(x, y).setTerrain(terrs.get("Water"));
				
				for(int i = -1; i <= 1; i++){
					for(int j = -1; j <= 1; j++){
						if(x + i >= 0 && x + i < mapwidth && y + j >= 0 && y + j < mapheight){
							ArrayList<Integer> t = new ArrayList<>(2);
							t.add(x + i);
							t.add(y + j);
							if(!next.contains(t)){
								next.add(t);
							}
						}
					}
				}
				
			}
			
		}
		
		if(next.size() > 0){
			flowTo(next, newEl);
		}
		
	}


	private boolean generateLines() {

		for(int x = 0; x < mapwidth - 1; x++){		
			for(int y = 0; y < mapheight - 1; y++){
				processCorners(new Cell[] {getCell(x, y), getCell(x + 1, y), getCell(x, y + 1), getCell(x + 1, y + 1) });
				

			}
		}
		for(int x = 0; x < mapwidth; x++){
			for(int y = 0; y < mapheight; y++){

				if(y < mapheight - 1){
					getCell(x, y).addEdge(0,  getCell(x, y).getElevation() - getCell(x, y + 1).getElevation());
				}
				if(x < mapwidth - 1){
					getCell(x, y).addEdge(1, getCell(x, y).getElevation() - getCell(x + 1, y).getElevation());
				}
			}
		}
		
		for(int y = 0; y < mapheight - 1; y++){
			if(getCell(0, y).getElevation() != getCell(0, y + 1).getElevation()){
				getCell(0, y).addCorner(1, Corner.Type.HORIZONTAL, getCell(0, y).getElevation() - getCell(0, y + 1).getElevation(), null);
			}
			
			if(getCell(mapwidth - 1, y).getElevation() != getCell(mapwidth - 1, y + 1).getElevation()){
				getCell(mapwidth - 1, y).addCorner(0, Corner.Type.HORIZONTAL, getCell(mapwidth - 1, y).getElevation() - getCell(mapwidth - 1, y + 1).getElevation(), null);
			}
		}
		
		for(int x = 0; x < mapwidth - 1; x++){
			if(getCell(x, 0).getElevation() != getCell(x + 1, 0).getElevation()){
				getCell(x, 0).addCorner(2, Corner.Type.VERTICLE, getCell(x, 0).getElevation() - getCell(x + 1, 0).getElevation(), null);
			}
			
			if(getCell(x, mapheight - 1).getElevation() != getCell(x + 1, mapheight - 1).getElevation()){
				getCell(x, mapheight - 1).addCorner(0, Corner.Type.VERTICLE, getCell(x, mapheight - 1).getElevation() - getCell(x + 1, mapheight - 1).getElevation(), null);
			}
		}
		
		return true;
	}

	private void processCorners(Cell[] cells) {
		assert cells.length == 4;
		
		HashMap<Integer, ArrayList<Integer>> vals = new HashMap<Integer, ArrayList<Integer>>();
		for(int i = 0; i < cells.length; i++){
			int e =  cells[i].getElevation();
			if(vals.get(e) == null){
				ArrayList<Integer> t = new ArrayList<Integer>();
				t.add(i);
				vals.put(e, t);
			}else{
				vals.get(e).add(i);
			}
		}
		
		ArrayList<Integer> largest = new ArrayList<Integer>();
		for(ArrayList<Integer> item: vals.values()){
			if(item.size() > largest.size()){
				largest = item;
			}
		}
		
		if(largest.size() == 4){
			// All for are the same
			return;
		}
		
		if(largest.size() == 3){
			//One odd one out
			int largeval = cells[largest.get(0)].getElevation();
			for(int i = 0; i < cells.length; i++){
				if(cells[i].getElevation() != largeval){
					cells[i].addCorner(i, Corner.Type.CURVED, cells[i].getElevation() - largeval, cells[largest.get(0)].getTerrain());	
					return;
				}
			}
		}
		
		if(largest.size() == 2){
			//Two are the same
			if(largest.get(0) + largest.get(1) == 3){
				//If opposites are the same
				
				
				if(cells[0].getElevation() == cells[2].getElevation() && cells[1].getElevation() == cells[3].getElevation()){
					//If we have two pairs of levels
					
					//The lower ones get the middle
					if(cells[0].getElevation() > cells[1].getElevation()){
						cells[0].addCorner(0, Corner.Type.CURVED, cells[0].getElevation() - cells[1].getElevation(), cells[1].getTerrain());
						cells[3].addCorner(3, Corner.Type.CURVED, cells[0].getElevation() - cells[1].getElevation(), cells[1].getTerrain());
					}else{
						cells[1].addCorner(1, Corner.Type.CURVED, cells[1].getElevation() - cells[0].getElevation(), cells[0].getTerrain());
						cells[2].addCorner(2, Corner.Type.CURVED, cells[1].getElevation() - cells[0].getElevation(), cells[0].getTerrain());
						
					}
				}else{
					//Otherwise pair up the two that are the same
					if(largest.contains(0)){
						//0 and 3 are the same 1 and 2 are different
						cells[1].addCorner(1, Corner.Type.CURVED, cells[1].getElevation() - cells[0].getElevation(), cells[0].getTerrain());
						cells[2].addCorner(2, Corner.Type.CURVED, cells[2].getElevation() - cells[0].getElevation(), cells[0].getTerrain());
					}else{
						cells[0].addCorner(0, Corner.Type.CURVED, cells[0].getElevation() - cells[1].getElevation(), cells[1].getTerrain());
						cells[3].addCorner(3, Corner.Type.CURVED, cells[3].getElevation() - cells[1].getElevation(), cells[1].getTerrain());
					}
				}
			}else{
				//The same are side by side
				Corner.Type t;
				if(largest.get(0) % 2 == largest.get(1) % 2){
					t = Corner.Type.VERTICLE;
				}else{
					t = Corner.Type.HORIZONTAL;
				}

				
				
				
				int inCurve = -1;
				if(cells[3 - largest.get(0)].getTerrain() != terrs.get("Water") || cells[3 - largest.get(1)].getTerrain() != terrs.get("Water")){
					if(cells[3 - largest.get(0)].getTerrain() == terrs.get("Water")){
						inCurve = 0;
					}else if(cells[3 - largest.get(1)].getTerrain() == terrs.get("Water")){
						inCurve = 1;
					}else if(cells[3 - largest.get(0)].getElevation() > cells[3 - largest.get(1)].getElevation()){
						inCurve = 0;
					}else if(cells[3 - largest.get(0)].getElevation() < cells[3 - largest.get(1)].getElevation()){
						inCurve = 1;
					}
				}
				if(inCurve != -1){
					cells[3 - largest.get(inCurve)].addCorner(3 - largest.get(inCurve), Corner.Type.CURVED, cells[3 - largest.get(inCurve)].getElevation() - cells[3 - largest.get(1 - inCurve)].getElevation(), cells[3 - largest.get(1 - inCurve)].getTerrain());
					
					cells[largest.get(0)].addCorner(largest.get(0), t, cells[largest.get(0)].getElevation() - cells[3 - largest.get(1 - inCurve)].getElevation(), null);
					cells[largest.get(1)].addCorner(largest.get(1), t,  cells[largest.get(1)].getElevation() - cells[3 - largest.get(1 - inCurve)].getElevation(), null);

				
				}else{
					cells[largest.get(0)].addCorner(largest.get(0), t, cells[largest.get(0)].getElevation() -  Math.min(cells[3 - largest.get(0)].getElevation(), cells[3 - largest.get(1)].getElevation()), null);
					cells[largest.get(1)].addCorner(largest.get(1), t,  cells[largest.get(1)].getElevation() - Math.min(cells[3 - largest.get(0)].getElevation(), cells[3 - largest.get(1)].getElevation()), null);
				}
			}
			
			return;
		}
		
		if(largest.size() == 1){
			// all different
			
			int water = 0;
			for(int i = 0; i < 4; i++){
				if(cells[i].getTerrain() == terrs.get("Water")){
					water++;
				}
			}
			if(water == 1){
				for(int i = 0; i < 4; i++){
					if(cells[i].getTerrain() != terrs.get("Water")){
						cells[i].addCorner(i, Corner.Type.CURVED, 2, terrs.get("Grass"));
					}else{
						cells[i].addCorner(i, Corner.Type.CURVED, -2, terrs.get("Grass"));
					}
				}
			}else{
				Terrain t;
				if(rand.nextBoolean()){
					t = terrs.get("Water");
				}else{
					t = terrs.get("Grass");
				}
				for(int i = 0; i < 4; i++){
					cells[i].addCorner(i, Corner.Type.CURVED, 2, t);
				}
			}
			
			
		}
		
	}


	private boolean generateElevation(int hillyness) {
		System.out.println("Generating hills...");
		map = new ArrayList<ArrayList<Cell>>();
		map.add(new ArrayList<Cell>());
		map.get(0).add(new Cell(0, terrs.get("Grass")));
		int curEl = 0;
		for(int i = 1; i < mapwidth; i++){
			int change = 0;
			if(rand.nextBoolean() == true){
				change = -1;
			}else{
				change = 1;
			}
			while(rand.nextInt(100) < hillyness){
				curEl += change;
			}
			map.get(0).add(new Cell(curEl, terrs.get("Grass")));
		}
		
		curEl = map.get(0).get(0).getElevation();
		for(int i = 1; i < mapheight; i++){
			map.add(new ArrayList<Cell>());
			int change = 0;
			if(rand.nextBoolean()){
				change = -1;
			}else{
				change = 1;
			}
			while(rand.nextInt(100) < hillyness){
				curEl += change;
			}
			map.get(i).add(new Cell(curEl, terrs.get("Grass")));
		}
		
		for(int i = 1; i < mapheight; i++){
			for(int j = 1; j < mapwidth; j++){
				
				int change = 0;
				if(rand.nextBoolean()){
					curEl = map.get(i).get(j - 1).getElevation();
					
					if(map.get(i - 1).get(j).getElevation() > curEl){
						change = 1;
					}else if (map.get(i - 1).get(j).getElevation() < curEl || rand.nextBoolean()){
						change = -1;
					}else{
						change = -1;
					}
				}else{
					curEl = map.get(i - 1).get(j).getElevation();
					if(map.get(i).get(j - 1).getElevation() > curEl){
						change = 1;
					}else if(map.get(i).get(j - 1).getElevation() < curEl || rand.nextBoolean()){
						change = -1;
					}else{
						change = -1;
					}
				}
				
				while(rand.nextInt(100) < hillyness){
					curEl += change;
				}
				map.get(i).add(new Cell(curEl, terrs.get("Grass")));
			}
			
		}
		
		/*
		int lowest = 0;
		
		for(ArrayList<Cell> row: map){
			for(Cell c: row){
				if(c.getElevation() < lowest){
					lowest = c.getElevation();
				}
			}
		}
		
		for(ArrayList<Cell> row: map){
			for(Cell c: row){
				c.lowerBy(lowest);
				if(c.getElevation() > highest){
					highest = c.getElevation();
				}
				
			}
		}
		*/
		return true;
	}





	public int getHighest() {
		return highest;
	}
	
	
	


	public int getMoveCost(int fromX, int fromY, int toX, int toY){
		return getMoveCost(fromX, fromY, toX, toY, null);
	}

	public int getMoveCost(int fromX, int fromY, int toX, int toY, Player p) {
		
		assert Math.abs(fromX - toX) + Math.abs(fromY - toY)  <= 2;
		
		
		if(p != null && p.isHuman() && !getCell(toX, toY).hasViewed(p)){
			return MOVEGUESS;
		}
		
		if(!map.get(toY).get(toX).getTerrain().isWalkable()){
			return IMPOSSIBLE;
		}
		
		if((p == null || getCell(toX, toY).isViewed(p)) && getCell(toX, toY).getUnit() != null){
			return IMPOSSIBLE;
		}
		
		if((p == null || getCell(toX, toY).isViewed(p)) && getCell(toX, toY).isOccupied()){
			return IMPOSSIBLE;
		}
		
		if(Math.abs(map.get(fromY).get(fromX).getElevation() - map.get(toY).get(toX).getElevation()) > 1){
			return IMPOSSIBLE;
		}
		
		if(map.get(fromY).get(fromX).getElevation()  < map.get(toY).get(toX).getElevation()){
			return 4;
		}
		
		if(map.get(fromY).get(fromX).getElevation()  > map.get(toY).get(toX).getElevation()){
			return 1;
		}
		
		if(map.get(fromY).get(fromX).getElevation()  == map.get(toY).get(toX).getElevation()){
			return 2;
		}
		
		assert false;
		return IMPOSSIBLE;
	}




	public boolean setDestination(Unit unit, int toX, int toY) {
			
			
			
		int x = toX;
		int y = toY;
		int fromX = unit.getX();
		int fromY = unit.getY();
		
		if(fromX == toX && fromY == toY){
			return false;
		}
		
		while(x != fromX || y != fromY){
			unit.pushNext(new int[]{x, y}, getCell(x, y).getMoveCost());
			int tx = x;
			int ty = y;
			x = getCell(tx, ty).getFromX();
			y = getCell(tx, ty).getFromY();
		}
		
		int[] next = unit.lookAtNext();
		while(!getCell(unit).isViewed(game.human) && !getCell(next[0], next[1]).isViewed(game.human)){
			unit.popNext();
			moveUnit(unit, next[0], next[1], unit.popCost());
			if(!unit.isMoving()){
				return false;
			}
			next = unit.lookAtNext();
		}
		
	
		
		int mc = getMoveCost(unit.getX(), unit.getY(), next[0], next[1]);
		if(mc == IMPOSSIBLE || unit.getMovement() < mc){
			unit.stop();
		}else{
			getCell(next[0], next[1]).setOccupied(true);
		}
		return true;
		
	}


	public Cell getCell(int x, int y) {
		return map.get(y).get(x);
	}




	public ListIterator<ArrayList<Cell>> getMapIterator() {
		return  map.listIterator();
	}




	public Cell getCell(Unit unit) {
		return getCell(unit.getX(), unit.getY());
	}

	public Cell getCell(ArrayList<Integer> a){
		return getCell(a.get(0), a.get(1));
	}


	public boolean moveUnit(Unit unit, int toX, int toY, double d) {
		
		int fromX = unit.getX();
		int fromY = unit.getY();
		
		getCell(toX, toY).setOccupied(false);
		getCell(toX, toY).setUnit(unit);
		unit.setPosition(toX, toY);
		getCell(fromX, fromY).setUnit(null);
		unit.setMovement(d);
		

		if(unit.isMoving()){
			int[] next = unit.lookAtNext();
			
			while(!getCell(unit).isViewed(game.human) && !getCell(next[0], next[1]).isViewed(game.human)){
				next = unit.popNext();
				moveUnit(unit, next[0], next[1], unit.popCost());
				if(!unit.isMoving()){
					return true;
				}
			
			}
			
			int mc = getMoveCost(unit.getX(), unit.getY(), next[0], next[1]);
			if(mc == IMPOSSIBLE || unit.getMovement() < mc){
				unit.stop();
			}else{
				getCell(next[0], next[1]).setOccupied(true);
			}
		}
		
		return unit.isMoving();
	}


	
	public int getFarLeft() {
		return farLeft;
	}


	public int getFarUp() {
		return farUp;
	}
	
	public int getFarRight() {
		return farRight;
	}


	public int getFarDown() {
		return farDown;
	}





	public void removeUnits(ArrayList<Unit> rems) {
		for(Unit u : rems){
			getCell(u).setUnit(null);
			u.getOwner().removeUnit(u);
		}
	}



	public int getMapWidth(){
		return mapwidth;
	}
	
	public int getMapHeight(){
		return mapheight;
	}








	public void loadAllImages() {
		Iterator<Entry<String, Terrain>> it = terrs.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, Terrain> pair = it.next();
	        pair.getValue().loadImages();
	    }
	    Iterator<Entry<String, UnitType>> iter = unitTypes.entrySet().iterator();
	    while (iter.hasNext()) {
	        Map.Entry<String, UnitType> pair = iter.next();
	        pair.getValue().loadImages(game.getPlayers());
	    }
	}



	public int getRandInt(int bound) {
		return rand.nextInt(bound);
	}


}
