package helpers;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import core.Cell;
import core.GameState;
import core.Unit;


public class AI extends Player{
	
	private static final long serialVersionUID = 1L;
	private GameState gs;
	private transient Iterator<Unit> unit;
	
	public AI(Color colour, String name) {
		super(colour, name);
	}

	public void setGameState(GameState gamestate){
		gs = gamestate;
		unit = getUnits().iterator(); 
	}

	public boolean doSomething() {
		
		assert gs != null;
		if(unit.hasNext()){
			Unit u = unit.next();
			takeAction(u);
			return true;
		}
		return false;
		
	}
	
	public void takeAction(Unit u){
		System.out.print(u.getName());
		
		if(u.getNumAttacks() > 0){
			int att = gs.getRandInt(u.getNumAttacks());
			String attack = u.getType().getAttackName(att);
			gs.calculateShooting(u, attack);
			if(u.getMovement() >= u.getAttackCost(attack)){
				for(int i = 0; i < gs.getNumPlayers(); i++){
					if(gs.getPlayer(i).equals(this)){
						continue;
					}
					for(Unit t: gs.getPlayer(i).getUnits()){
						if(gs.getCell(t).canItBeShot(this)){
							System.out.println( "shoots at " + t.getName());
							u.shootAt(t, attack, gs.getCell(u).getElevation() < gs.getCell(t).getElevation());
							gs.getCell(t).isViewed(gs.getHuman());
							return;
						}
					}
				}
			}
		}
		
		findWhereYouCanMove(u);
		int x = -1;
		int y = -1;
		do{
			x = u.getX() - ((int)u.getMovement() / 2) + gs.getRandInt((int) u.getMovement() * 2);
			y = u.getY() - ((int)u.getMovement() / 2) + gs.getRandInt((int) u.getMovement() * 2);
			x = Math.max(Math.min(gs.getMapWidth() - 1, x), 0);
			y = Math.max(Math.min(gs.getMapHeight() - 1, y), 0);
		}while(gs.getCell(x, y).getMoveCost() == GameState.IMPOSSIBLE);
		System.out.println(" move to " + x + ":" + y);
		gs.setDestination(u, x, y);
		return;
	}
	
	public void findWhereYouCanMove(Unit u) {
		assert gs != null;
		Iterator<ArrayList<Cell>> it = gs.getMapIterator();
		while(it.hasNext()){
			for(Cell cell: it.next()){
				cell.setMoveCost(GameState.IMPOSSIBLE);
				cell.setFrom(-1, -1);
			}
		}
		gs.getCell(u).setMoveCost(u.getMovement());
		moveFrom(u.getX(), u.getY(), u.getMovement());
			
	}
	
	private void moveFrom(int x, int y, double m) {
		
		assert gs != null;
		
		for(int i = -1; i <= 1; i++){
			for(int j = -1; j <= 1; j++){
				if(i == 0 && j == 0){
					continue;
				}
				if(x + i >= 0 && x + i < gs.getMapWidth() && y + j >= 0 && y + j < gs.getMapHeight()){
					double mc = gs.getMoveCost(x, y, x + i, y + j, this);
					if(mc != GameState.IMPOSSIBLE){
						if(i != 0 && j != 0){
							mc *= 1.5;
						}
						double ml = m - mc;
						if(ml >= 0 && ml > gs.getCell(x + i, y + j).getMoveCost()){
							gs.getCell(x + i, y + j).setMoveCost(ml);
							gs.getCell(x + i, y + j).setFrom(x, y);
							moveFrom(x + i, y + j, ml);
						}
					}
				}
			}
		}
	}
	
	public void resetMovement(){
		super.resetMovement();
		unit = getUnits().iterator();
	}

}
