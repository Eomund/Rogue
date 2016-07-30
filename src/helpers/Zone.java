package helpers;
import java.io.Serializable;

import core.GameState;

public class Zone implements Serializable{
	
	private static final long serialVersionUID = 1L;
	public static int size;
	
	private int x;
	private int y;
	private boolean good = false;
	
	public Zone(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}
	
	

	public void findGoodness(GameState gameState, int bigReg, int sizeNeeded) {
		assert !good;
		int accessibility = 0;
		
		for(int i = x; i < x + size; i++){
			for(int j = y; j < y + size; j++){
				if(gameState.getCell(i, j).getRegion() == bigReg){
					accessibility++;
					if(accessibility >= sizeNeeded){
						good = true;
						return;
					}
				}
			}
		}
	}


	public boolean isGood() {
		return good;
	}



	public int getX() {
		return x;
	}



	public int getY() {
		return y;
	}



	public void makeBad() {
		good = false;
		
	}
	
	

}
