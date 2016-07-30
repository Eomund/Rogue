package helpers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JOptionPane;

import gui.FileDialog;

public class Config {


	public static ArrayList<String[]> rawTerrain = new ArrayList<>();
	public static ArrayList<String[]> rawUnits = new ArrayList<>();
	
	private static final String def = "[Terrain]\nGrass,img/grass.png, of course\nWater, img/water.png, never\n\n[UNITS]\n;name, 		image, 			sight, 		movement,	max hp, 	firepower	acc		range, 	attack cost,		special attacks\nInfantry, img/redcoat.png, 	6, 			10,			20,			5,			50,		5,		4,					Volley, 2, 1.5, 1,2,   Bayonet,4,1.5,.2,1.2\n\n[GENERAL]\n\n;save stuff in folder\nsaves=saves";

	public static void loadConfigValues(){
		ArrayList<String> data = readFile(new File("config"));
		
		String mode = null;
		for(String line : data){
			if(line.charAt(0) == '[' && line.charAt(line.length() -1) == ']'){
				mode = line.substring(1, line.length() - 1);
				continue;
			}
			
			
			
			if(mode.equalsIgnoreCase("TERRAIN")){
				rawTerrain.add(line.split(","));
			}
			
			if(mode.equalsIgnoreCase("UNITS")){
				rawUnits.add(line.split(","));
			}
			
			if(mode.equalsIgnoreCase("GENERAL")){
				String[] parts = line.split("=");
				
				if(parts[0].equalsIgnoreCase("saves")){
					FileDialog.fileRoot = parts[1];
				}
			}
			
			
		}
	}
	
	private static ArrayList<String> readFile(File f){
		ArrayList<String> res = new ArrayList<String>();
		try {
			if(!f.exists()){
				f.createNewFile();
				PrintWriter w = new PrintWriter(f.getAbsolutePath());
				w.write(def);
				w.close();
				JOptionPane.showMessageDialog(null, "Config file not found. Generated new one from scratch", "New config file", JOptionPane.WARNING_MESSAGE);
			}
			Scanner sc = new Scanner(f);
			while(sc.hasNextLine()){
				String str = sc.nextLine();
				str = str.split(";")[0].trim();
				if(str.equals("")){
					continue;
				}
				res.add(str);
			}
			sc.close();
		} catch (IOException e ) {
			e.printStackTrace();
			System.exit(-1);
		}
		return res;
	}
	
	public static boolean isTrue(String s) {
		String truewords[] = {"T", "TRUE", "YES", "Y", "1", "OF COURSE", "WHY NOT"};
		String test = s.toUpperCase();
		for(String t : truewords){
			if(t.equals(test)){
				return true;
			}
		}
		return false;
		
	}
	
	
}
