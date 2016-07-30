package helpers;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;


public class Terrain implements Serializable{

	
	private static final long serialVersionUID = 1L;
	
	private transient BufferedImage corners[];
	private transient BufferedImage main;
	private transient BufferedImage base;
	public static BufferedImage mainmask = null;
	public static BufferedImage masks[] = new BufferedImage[4];
	private boolean walkable;
	private String imgfile;
	
	static{
		try {
			mainmask =  ImageIO.read(new File("img/main.png"));
			for(int i = 0; i < 4; i++){
				masks[i] = ImageIO.read(new File("img/" + i + ".png"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public Terrain(String imgfile, boolean walk) {
		this.imgfile = imgfile;
		loadImages();
		walkable = walk;

	}
	


	
	private static void mergeImages(BufferedImage image, BufferedImage mask, BufferedImage out){
	    int width = image.getWidth();
	    int height = image.getHeight();

	    for(int i = 0; i < width; i++){
	    	for(int j = 0; j < height; j++){
	    		Color imgCol = new Color(image.getRGB(i, j), true);
	    		Color mCol = new Color(mask.getRGB(i, j), true);
	    		if(mCol.getAlpha() > 10){
	    			out.setRGB(i, j, imgCol.getRGB());
	    		}
	    	}
	    }
	}
	


	public boolean isWalkable() {
		return walkable;
	}




	public BufferedImage getMainImage() {
		return main;
	}




	public Image getCornerImg(int i) {
		return corners[i];
	}




	public BufferedImage getBaseImage() {
		return base;
	}




	public void loadImages() {
		try {
			base = ImageIO.read(new File(imgfile));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		main = new BufferedImage(base.getWidth(), base.getHeight(), base.getType());
		mergeImages(base, mainmask, main);
		corners = new BufferedImage[4];
		for(int i = 0; i < 4; i++){
			corners[i] = new BufferedImage(base.getWidth(), base.getHeight(), base.getType());
			mergeImages(base, masks[i], corners[i]);
		}
	}
	
}
