package helpers;
import java.awt.Color;
import java.util.Iterator;

import core.GameState;
import core.GameType;
import core.Unit;


public class AI extends Player{
	
	private static final long serialVersionUID = 1L;
	private transient Iterator<Unit> unit;
	private String attack;
	
	public AI(Color colour, String name, GameType gt) {
		super(colour, name, gt);
	}

	@Override
	public void setGame(GameType game){
		super.setGame(game);
		unit = getUnits().iterator(); 
	}

	@Override
	public void doSomething() {
		
		if(unit.hasNext()){
			Unit u = unit.next();
			takeAction(u);
			return;
		}
		endTurn();
		
	}
	
	public void takeAction(Unit u){
		System.out.print(u.getName());
		
		if(u.getNumAttacks() > 0){
			int att = getRandInt(u.getNumAttacks());
			attack = u.getType().getAttackName(att);
			calculateShooting(u, attack);
			if(u.getMovement() >= u.getAttackCost(attack)){
				for(int i = 0; i < getPlayers().size(); i++){
					if(getPlayers().get(i).equals(this)){
						continue;
					}
					for(Unit t: getPlayers().get(i).getUnits()){
						if(getCell(t) != null && getCell(t).canItBeShot(this)){
							System.out.println( "shoots at " + t.getName());
							
							addAction(u, t.getX(), t.getY(), Action.Type.SHOOT);
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
			x = u.getX() - ((int)u.getMovement() / 2) + getRandInt((int) u.getMovement() * 2);
			y = u.getY() - ((int)u.getMovement() / 2) + getRandInt((int) u.getMovement() * 2);
			x = Math.max(Math.min(getMapWidth() - 1, x), 0);
			y = Math.max(Math.min(getMapHeight() - 1, y), 0);
		}while(getCellMoveCost(x, y) == GameState.IMPOSSIBLE);
		System.out.println(" move to " + x + ":" + y);
		addAction(u, x, y, Action.Type.DESTINATION);
		return;
	}
	@Override
	public void resetMovement(){
		super.resetMovement();
		unit = getUnits().iterator();
	}

	@Override
	public void yourTurn() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void noLongerYourTurn() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getAttack() {
		return attack;
	}

	@SuppressWarnings("hiding")
	@Override
	public void unitUpdated(Unit unit) {
		// TODO Auto-generated method stub
		
	}

	@SuppressWarnings("hiding")
	@Override
	public void watch(Unit unit) {
		// TODO Auto-generated method stub
		
	}


}
