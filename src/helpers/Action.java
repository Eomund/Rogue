package helpers;

import core.Unit;

public class Action {
	
	private Unit unit;
	private int x;
	private int y;
	private Type type;
	private Player player;
	
	public enum Type{DESTINATION, SHOOT, ENDTURN};
	
	

	public Action(Unit unit, int x, int y, Type type, Player player) {
		super();
		this.unit = unit;
		this.x = x;
		this.y = y;
		this.type = type;
		this.player = player;
	}

	public Action(Player player) {
		super();
		this.type = Type.ENDTURN;
		this.player = player;
		this.unit = null;
	}

	public Unit getUnit() {
		return unit;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public Type getType() {
		return type;
	}

	public Player getPlayer() {
		return player;
	};
	
	

}
