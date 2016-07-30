package helpers;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.imageio.ImageIO;


public class UnitType implements Serializable{

	private static final long serialVersionUID = 1L;
	private static final int FP = 0;
	private static final int ACC = 1;
	private static final int RANGE = 2;
	private static final int COST = 3;
	
	private String name;
	private transient HashMap<Player, BufferedImage> img;
	private transient BufferedImage base;
	private int sight;
	private int movement;
	private int hp;
	private int firePower;
	private int acc;
	private int range;
	private LinkedHashMap<String, float[]> attacks = new LinkedHashMap<String, float[]>();
	private int baseAttackCost;
	private String imgfile;
	
	public UnitType(String name, String imgfile, int sight, int movement, int hp, int fp, int acc , int range, int bac, ArrayList<Player> ps) {
		super();
		this.name = name;
		this.imgfile = imgfile;
		loadImages(ps);
		this.sight = sight;
		this.movement = movement;
		this.firePower = fp;
		this.hp = hp;
		this.acc = acc;
		this.range = range;
		this.baseAttackCost = bac;
		attacks.put("Attack", new float[]{1,1,1,1});
	}


	public UnitType(String n, String f, int[] partNum, ArrayList<Player> ps) {
		this(n, f, partNum[0], partNum[1], partNum[2], partNum[3], partNum[4], partNum[5], partNum[6], ps);
		
		
	}


	public BufferedImage getImage(Player owner) {
		return img.get(owner);
	}


	public int getSight() {
		return sight;
	}


	public String getName() {
		return name;
	}




	public int getMaxHP() {
		return hp;
	}


	public int getMovement() {
		return movement;
	}


	public int getFirePower() {
		return firePower;
	}

	private static void mergeImages(BufferedImage image, BufferedImage out, Color changeFrom, Color changeTo){
	    int width = image.getWidth();
	    int height = image.getHeight();

	    for(int i = 0; i < width; i++){
	    	for(int j = 0; j < height; j++){
	    		Color imgCol = new Color(image.getRGB(i, j), true);
	    		if(imgCol.equals(changeFrom)){
	    			out.setRGB(i, j, changeTo.getRGB());
	    		}else{
	    			out.setRGB(i, j, imgCol.getRGB());
	    		}
	    	}
	    }
	}




	public int getNumAttack() {
		return attacks.size();
	}


	public Set<String> getAttackNames() {
		return attacks.keySet();
	}




	public void addAttack(String n, float[] floats) {
		attacks.put(n, floats);
		
	}


	public int getRange() {
		return range;
	}


	public int getAcc() {
		return acc;
	}


	public float getRangeMod(String s) {
		return attacks.get(s)[RANGE];
	}


	public float getAccMod(String s) {
		
		return attacks.get(s)[ACC];
	}


	public float getFirePowerMod(String s) {
		return attacks.get(s)[FP];
	}


	public float getMoveCostMod(String attack) {
		return attacks.get(attack)[COST];
	}


	public int getBaseAttackCost() {
		return baseAttackCost;
	}


	public String getAttackName(int att) {
		Iterator<String> it = attacks.keySet().iterator();
		for(int i = 0; i < att; i++){
			assert it.hasNext();
			it.next();
		}
		
		return it.next();
	}


	public void loadImages(ArrayList<Player> ps) {
		try{
			this.base = ImageIO.read(new File(imgfile));
			img = new HashMap<>();
			for(Player p:ps){
				img.put(p, new BufferedImage(base.getWidth(), base.getHeight(), base.getType()));
				mergeImages(base, img.get(p), new Color(255, 0, 255), p.getColour());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	
	
}
