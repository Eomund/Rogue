package helpers;

import java.io.Serializable;

public class Corner implements Serializable{
	
	
	private static final long serialVersionUID = 1L;
	private int heightDiff;
	private Type type;
	private Terrain terr;
	
	public enum Type{
		VERTICLE, HORIZONTAL, CURVED
	}
	



	public Corner (Type t, int h, Terrain terr){
		heightDiff = h;
		type = t;
		this.terr = terr;
	}


	public Type getType() {
		return type;
	}


	public int getHeightDiff() {
		return heightDiff;
	}
	
	public String toString(){
		String str = "Type: ";
		
		switch(type){
		case VERTICLE:
			str += "vertical";
			break;
		case HORIZONTAL:
			str += "horizontal";
			break;
		case CURVED:
			str += "curved";
			break;
		}
		str += "\nHeight diff: " + heightDiff + "\n";
		
		return str;
	}


	public Terrain getTerrain() {
		return terr;
	}

}

