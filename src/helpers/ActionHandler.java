package helpers;

import java.util.concurrent.ConcurrentLinkedQueue;

import core.Cell;
import core.GameType;

public class ActionHandler extends Thread{
	

	private ConcurrentLinkedQueue<Action> actions = new ConcurrentLinkedQueue<>();
	private GameType game;
	private boolean guiing = false;
	private boolean dead = false;
	
	public ActionHandler(GameType game){
		super();
		this.game = game;
	}

	public void addAction(Action a){
		actions.add(a);
		if(!guiing){
			wakeUp();
		}

	}
	
	public synchronized void wakeUp() {
		notify();
		System.out.println("TO ACTION");
	}

	@Override
	public void run(){
		while(!dead){
			while(!actions.isEmpty()){
				Action active = actions.remove();
				if(active.getPlayer().equals(game.getCurrentPlayer())){
						
					switch (active.getType()) {
					case DESTINATION:
						synchronized (active.getUnit()) {
							guiing = game.gs.setDestination(active.getUnit(), active.getX(), active.getY());
						}
						break;
						
					case SHOOT:
						Cell target = game.gs.getCell(active.getX(), active.getY());
						synchronized (active.getUnit()) {
							active.getUnit().shootAt(target, target.getElevation() > game.gs.getCell(active.getUnit()).getElevation());
						}
						guiing = game.gs.getCell(active.getUnit()).isViewed(game.human) && target.isViewed(game.human);
						break;
					case ENDTURN:
						game.endTurn();
						break;
					default:
						assert false;
						break;
					}
					
					if(guiing){
						goToSleep();
					}
				}
			}
		
				
			if(!dead){
				goToSleep();
			}
		}
		
	}

	public synchronized void goToSleep() {
		try {
			wait();
			System.out.println("TO SLEEP");
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public boolean isGuiing() {
		return guiing;
	}

	public void kill() {
		dead = true;
	}
}
