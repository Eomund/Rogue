package core;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import gui.GUI;
import gui.LocalPlayer;
import gui.Selection;
import helpers.AI;
import helpers.Action;
import helpers.ActionHandler;
import helpers.EliminationGame;
import helpers.Player;

public abstract class GameType implements Serializable{

	
	private static final long serialVersionUID = 1L;
	public GameState gs;
	private ArrayList<Player> players = new ArrayList<>();
	public transient LocalPlayer human;
	private int turn = 0;
	private transient Timer time;
	private transient ActionHandler actionHandler;
	
	public abstract Player win();
	
	public abstract void killUnit(Player killer, Player dier);
	public abstract void setup();
	public abstract void startNewTurn();
	
	public GameType( int mapWidth, int mapHeight, int armySize){
		
		gs = new GameState(mapWidth, mapHeight, armySize);

		
		actionHandler = new ActionHandler(this);
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
				gt.human = new LocalPlayer(cols.get(i), playerNames.get(i), gt);
				gt.addPlayer(gt.human);
			}else{
				gt.addPlayer(new AI(cols.get(i), playerNames.get(i), gt));
			}
		}
		gt.setup();
		gt.gs.makeWorld(gt, gameType, hillyness, mapSeed, gameSeed);
		gt.human.setup();
		gt.startTime();
	}
	


	public static void start(int mapWidth, int mapHeight, int hillyness, int armySize, ArrayList<Player> players, long mapSeed, long gameSeed, String gtName) {
		GameType gt = GameType.newGameType(gtName, mapWidth, mapHeight, hillyness, armySize, mapSeed, gameSeed);
		gt.players = players;
		for(Player p:gt.players){
			p.setGame(gt);
			if(!p.isAI()){
				gt.human = (LocalPlayer) p;
			}
		}
		gt.setup();
		gt.gs.makeWorld(gt, gtName, hillyness, mapSeed, gameSeed);
		gt.human.setup();
		gt.startTime();
	}
	

	public static void load(GameType gt, GUI oldGUI){
		gt.actionHandler.kill();
		gt.actionHandler.wakeUp();
		gt.actionHandler = null;
		gt.gs.loadAllImages();
		for(Player p:gt.players){
			p.setGame(gt);
			if(!p.isAI()){
				gt.human = (LocalPlayer)p;
			}
		}
		gt.human.gui = oldGUI;
		gt.human.setup();
		
		gt.human.gui.repaintAll();
		gt.actionHandler = new ActionHandler(gt);
		gt.startTime();
	}
	
	private void startTime() {

		time = new Timer(GUI.TICKTIME, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				tick();
			}
		});
		time.start();
		actionHandler.start();
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


	public void endTurn() {
		
		getCurrentPlayer().noLongerYourTurn();
		incrementTurn();
		startNewTurn();
		System.out.println("It is now " + getCurrentPlayer().getName() + "'s turn...");
		
		getCurrentPlayer().yourTurn();
		
	}
	

	public Player tick(){
		boolean dirty = false;
		Unit active = null;
		for(Player p:getPlayers()){
			for(Unit u:p.getUnits()){
				synchronized (u) {
					
					if(u.isMoving()){
						if(u.getMoveStep() == GUI.MAXSTEP){
							int[] next = u.popNext();
							
							gs.moveUnit(u, next[0], next[1], u.popCost());
							u.getOwner().unitUpdated(u);
							if(actionHandler.isGuiing()){
								actionHandler.wakeUp();
							}
							dirty = true;
						}else{
							u.tick();
						}
					}
					
					if(u.isShooting()){
						if(u.getMoveStep() == GUI.MAXSHOOT || (!gs.getCell(u).isViewed(human) && !gs.getCell(u.getTarget()).isViewed(human))){
							if(u.getTarget().getHP() <= 0){
								removeUnit(u.getOwner(), u.getTarget());
							}
							u.doneShooting();
							if(actionHandler.isGuiing()){
								actionHandler.wakeUp();
							}
							dirty = true;
						}else{
							u.tick();
						}
					}
				}
				if(u.isActive()){
					active = u;
				}
			}
		}
		
	
		Player winner = win();
		if(winner != null){
			JOptionPane.showMessageDialog(human.gui.getPanel(), winner.getName() + " wins the game!", "Winner!", JOptionPane.INFORMATION_MESSAGE);
			System.exit(0);
		}
		
		if(active == null){
			getCurrentPlayer().doSomething();
		}else{
			for(Player p: players){
				p.watch(active);
			}
		}
		if(dirty){
			gs.calculateViewing();
			if(getCurrentPlayer().isAI() && human.gui.getSelectionMode() != Selection.MOVEMODE){
				gs.calculateShooting(human.gui.getSelectedUnit(), human.gui.getSelectedModeString());
			}
			human.findWhereYouCanMove(human.gui.getSelectedUnit());
			if(human.gui.getSelectedUnit() != null && !gs.getCell(human.gui.getSelectedUnit()).isViewed(human)){
				human.gui.deselect();
			}
			
		}
		human.gui.repaintAll();
		
		return win();
	}

	public void addAction(Action a) {
		actionHandler.addAction(a);
		
	}

	public Timer getTime() {
		return time;
	}
	
	
}
