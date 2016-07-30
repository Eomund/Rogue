package core;
import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import gui.GUI;
import helpers.AI;
import helpers.EliminationGame;
import helpers.Player;

public abstract class GameType implements Serializable{

	
	private static final long serialVersionUID = 1L;
	public GameState gs;
	private ArrayList<Player> players = new ArrayList<>();
	public transient GUI human;
	private int turn = 0;
	
	public abstract Player win();
	
	public abstract void killUnit(Player killer, Player dier);
	public abstract void Setup();
	
	public GameType( int mapWidth, int mapHeight, int armySize){
		
		gs = new GameState(mapWidth, mapHeight, armySize);
		
	}
	
	
	
	public Player tick(){
		return win();
	}
	
	
	public static GameType newGameType(String name, int mapWidth, int mapHeight, int hillyness, int armySize, long mapSeed, long gameSeed){
		if(name.equalsIgnoreCase("Elimination")){
			return new EliminationGame(mapWidth, mapHeight, armySize);
		}
		
		assert false;
		return null;
	}

	public GameState getGameState() {
		return gs;
	}
	

	public static void start(int mapWidth, int mapHeight, int hillyness, int armySize, ArrayList<String> playerNames,	ArrayList<Color> cols, ArrayList<Boolean> bools, long mapSeed, long gameSeed, String gameType) {

		GameType gt = GameType.newGameType(gameType, mapWidth, mapHeight, hillyness, armySize, mapSeed, gameSeed);
		for(int i = 0; i < playerNames.size(); i++){
			if(bools.get(i)){
				gt.human = new GUI(cols.get(i), playerNames.get(i), gt);
				gt.addPlayer(gt.human);
			}else{
				gt.addPlayer(new AI(cols.get(i), playerNames.get(i), gt));
			}
		}
		gt.Setup();
		gt.gs.makeWorld(gt, gameType, hillyness, mapSeed, gameSeed);
		gt.human.setup();
	}
	
	public static void start(int mapWidth, int mapHeight, int hillyness, int armySize, ArrayList<Player> players, long mapSeed, long gameSeed, String gtName) {
		GameType gt = GameType.newGameType(gtName, mapWidth, mapHeight, hillyness, armySize, mapSeed, gameSeed);
		gt.players = players;
		for(Player p:gt.players){
			p.setGame(gt);
			if(!p.isAI()){
				gt.human = (GUI)p;
			}
		}
		gt.Setup();
		gt.gs.makeWorld(gt, gtName, hillyness, mapSeed, gameSeed);
	}
	

	public static void load(GameType gt){
		
		gt.gs.loadAllImages();
		for(Player p:gt.players){
			p.setGame(gt);
			if(!p.isAI()){
				gt.human = (GUI)p;
			}
		}
	}
	
	private void addPlayer(Player p) {
		players.add(p);
		
	}



	public ArrayList<Player> getPlayers() {
		return players;
	}

	public int getNumPlayers() {
		return players.size();
	}


	
	public void incrementTurn(){
		turn++;
		turn = turn % players.size();
		players.get(turn).resetMovement();
	}

	public Player getCurrentPlayer() {
		return players.get(turn);
	}

	public Player getPlayer(int i) {
		return players.get(i);
	}

	public void removeUnit(Player owner, Unit target) {
		owner.removeUnit(target);
		gs.getCell(target).setUnit(null);
	}

	public int getTurn() {
		return turn;
	}




	
	
	
}
