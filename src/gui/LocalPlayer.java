package gui;

import java.awt.Color;

import core.GameType;
import core.Unit;
import helpers.Action;
import helpers.Player;


public class LocalPlayer extends Player {

	
	private static final long serialVersionUID = 1L;

	public transient GUI gui;
	
	private int farLeft;
	private int farUp;
	private int farRight = 0;
	private int farDown = 0;
	private boolean canAct = false;
	
	
	public LocalPlayer(Color colour, String name, GameType gt) {
		super(colour, name, gt);
		farLeft = getMapWidth();
		farUp = getMapHeight();
	}

	public void setup() {
		if(gui == null){
			gui = new GUI(this);
		}
		gui.setPlayer(this);
		gui.setup();
		
	}


	public void setExtremes(int x, int y) {
		if(x < farLeft){
			farLeft = Math.max(x, 0);
		}
		
		if(y < farUp){
			farUp = Math.max(y, 0);
		}
		
		if(x > farRight){
			farRight = Math.min(x, getMapWidth());
		}
		
		if(y > farDown){
			farDown = Math.min(y, getMapHeight());
		}
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

	@Override
	public void yourTurn() {
		canAct = true;
		gui.setSelectionButtonsEnabled(true);
		if(gui.getSelectedUnit() != null){
			findWhereYouCanMove(gui.getSelectedUnit());
		}
		gui.setEndEnabled(true);
	}

	@Override
	public void noLongerYourTurn() {
		canAct = false;
		gui.setSelectionButtonsEnabled(false);
		gui.setSelectionButtonsSelected(false);
		gui.setEndEnabled(false);
	}

	@Override
	public String getAttack() {
		return gui.getSelectedModeString();
	}

	@Override
	public void doSomething() {
		if(!canAct){
			canAct = true;
			gui.setSelectionButtonsEnabled(true);
			gui.setEndEnabled(true);
		}
	}
	
	@Override
	public void addAction(Unit unit, int x, int y, Action.Type type){
		super.addAction(unit, x, y, type);
		canAct = false;
	}
	
	@Override
	public void removeUnit(Unit u){
		super.removeUnit(u);
		if(u.equals(gui.getSelectedUnit())){
			gui.deselect();
		}
	}

	@Override
	public void unitUpdated(Unit unit) {
		if(unit.equals(gui.getSelectedUnit())){
			gui.dirtySelection();
		}
	}

	@Override
	public void watch(Unit unit) {
		if(unit.getOwner() != this && getCell(unit) != null && getCell(unit).getUnit() != null){
			gui.centerAt(unit);
		}
	}
	
	
	
}
