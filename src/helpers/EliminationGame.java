package helpers;
import core.GameState;
import core.GameType;

public class EliminationGame extends GameType {

	private static final long serialVersionUID = 1L;

	public EliminationGame(GameState gs) {
		super(gs);
		for(Player p: gs.getPlayers()){
			p.setScore(p.getUnits().size());
		}
	}
	
	@Override
	public Player win() {
		int count = 0;
		Player win = null;
		for(Player p: gs.getPlayers()){
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

}
