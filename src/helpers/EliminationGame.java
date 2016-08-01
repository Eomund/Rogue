package helpers;
import core.GameType;

public class EliminationGame extends GameType {

	private static final long serialVersionUID = 1L;

	public EliminationGame(int mapWidth, int mapHeight, int armySize) {
		super(mapWidth, mapHeight, armySize);
	}
	
	@Override
	public void setup(){
		for(Player p: getPlayers()){
			p.setScore(p.getUnits().size());
		}
	}
	
	@Override
	public Player win() {
		int count = 0;
		Player win = null;
		for(Player p: getPlayers()){
			if(p.getUnits().size() != 0){
				count++;
				win = p;
			}
		}
		if(count == 1){
			assert win != null;
			return win;
		}
		
		return null;
	}

	@Override
	public void killUnit(Player killer, Player dier) {
		dier.addToScore(-1);

	}

	@Override
	public void startNewTurn() {
		// TODO Auto-generated method stub
		
	}

}
