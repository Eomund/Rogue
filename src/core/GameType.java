package core;
import java.io.Serializable;

import helpers.EliminationGame;
import helpers.Player;

public abstract class GameType implements Serializable{

	
	private static final long serialVersionUID = 1L;
	protected GameState gs;
	
	public GameType(GameState g){
		gs = g;
	}
	
	public abstract Player win();
	
	public abstract void killUnit(Player killer, Player dier);
	
	public Player tick(){
		return win();
	}
	
	
	public static GameType newGameType(String name, GameState gs){
		if(name.equalsIgnoreCase("Elimination")){
			return new EliminationGame(gs);
		}
		
		assert false;
		return null;
	}

	public GameState getGameState() {
		return gs;
	}

}
