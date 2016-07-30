package core;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Arc2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;

import gui.FileDialog;
import gui.GUI;
import gui.Menu;
import gui.MiniMap;
import gui.Selection;
import helpers.AI;
import helpers.Corner;
import helpers.Player;
import helpers.Terrain;
import helpers.Zone;


@SuppressWarnings("serial")
public class Panel extends JPanel {

	public static final int squareHeight = 200;
	public static final int squareWidth = 200;

	private int zoom = 10;
	private int xoff;
	private int yoff;
	
	private final static int magicInts[] = {270, 180, 0, 90};
	private final static double magicDoubs[][] = {{.5, .5}, {0, .5}, {.5, 0}, {0, 0}};
	private final static int moreMagic[] = {-3, 0, 0, -1};
	
	private final static BasicStroke dashed = new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2f, new float[]{10f, 4f}, 0.0f);
	private final static BasicStroke d2 = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2f, new float[]{10f, 4f}, 0.0f);

	private int prevY = -1;
	private int prevX = -1;
	private int hovX = -1;
	private int hovY = -1;
	

	private GameType game;
	

	public Panel(GameType myGame){
		super();
		game = myGame;
		setLayout(new BorderLayout());

		
		
		
		addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent arg0) {}
			public void mousePressed(MouseEvent e) {
				prevX = e.getX();
				prevY = e.getY();
			}
			public void mouseExited(MouseEvent arg0) {
				hovX = -1;
				hovY = -1;
				
			}
			public void mouseEntered(MouseEvent arg0) {}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1){
					game.human.doClick((e.getX() - xoff) * 20 / (zoom * squareWidth) , (e.getY() - yoff) * 20 / (zoom * squareHeight), e.getModifiers(), e.getClickCount());
				}else if(e.getButton() == MouseEvent.BUTTON3){
					game.human.doRightClick((e.getX() - xoff) * 20 / (zoom * squareWidth) , (e.getY() - yoff) * 20 / (zoom * squareHeight), e.getModifiers());

				}
				
			}
		});
		
		addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				int zb = zoom;
				if(e.isControlDown()){
					zoom -= 5 * e.getWheelRotation();
				}else{
					zoom -= e.getWheelRotation();
				}
				xoff = (int) (xoff * zoom / (double)zb);
				yoff = (int) (yoff * zoom / (double)zb);
				
					xoff -= e.getX() * (1 - (zb / (double) zoom));
					yoff -= e.getY() * (1 - (zb / (double) zoom));
				fixOffsets();
				
				

				game.human.repaintAll();;
			}
		});
		
		addMouseMotionListener(new MouseMotionListener() {
			
		


			@Override
			public void mouseMoved(MouseEvent e) {
				doHover((e.getX() - xoff) * 20 / (zoom * squareWidth) , (e.getY() - yoff) * 20 / (zoom * squareHeight), e.getModifiers());
				
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				if(prevY != -1){
					yoff -= (prevY - e.getY());
				}
				prevY = e.getY();
				
				if(prevX != -1){
					xoff -= (prevX - e.getX());
				}
				fixOffsets();
				prevX = e.getX();
				prevY = e.getY();
				game.human.repaintAll();;
			}
			
		});
		
		addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent arg0) {}
			
			@Override
			public void componentResized(ComponentEvent arg0) {
				game.human.updateOffset(xoff * -20.0 / (zoom * squareWidth), yoff * -20 / (zoom * squareHeight), Panel.this.getWidth() * 20 / (zoom * squareWidth), Panel.this.getHeight() * 20 / (zoom * squareHeight));

				
			}
			
			@Override
			public void componentMoved(ComponentEvent arg0) {}
			
			@Override
			public void componentHidden(ComponentEvent arg0) {}
		});
		
	}
	
	




	public void fixOffsets(){
		xoff = Math.max(xoff, Panel.this.getWidth() - toPixels(Math.min(game.gs.getFarRight() + 2, game.gs.getMapWidth()), true, false));
		yoff = Math.max(yoff, Panel.this.getHeight() - toPixels(Math.min(game.gs.getFarDown() + 2, game.gs.getMapHeight()), false, false));
		xoff = Math.min(xoff,  -1 * toPixels(Math.max(game.gs.getFarLeft() - 1, 0), true, false));
		yoff = Math.min(yoff,  -1 * toPixels(Math.max(game.gs.getFarUp() - 1, 0), false, false));

		game.human.updateOffset(xoff * -20.0 / (zoom * squareWidth), yoff * -20.0 / (zoom * squareHeight), Panel.this.getWidth() * 20 / (zoom * squareWidth), Panel.this.getHeight() * 20 / (zoom * squareHeight));
	}
	
	

	private int toPixels(double n, boolean x){
		return toPixels(n, x, true);
	}

	private int toPixels(double n, boolean x, boolean off){
		if(x){
			return (int)(n * squareWidth * zoom / 20 + (off ? xoff:0));
		}
		
		return (int)(n * squareHeight * zoom / 20 + (off ? yoff:0));
	}
	
	public void centerAt(Unit u){
		centerAt(u.getX(), u.getY());
	}
	
	public void centerAt(int x, int y){
		xoff = -1 * (toPixels(x, true, false) - (getWidth() / 2));
		yoff = -1 * (toPixels(y, false, false) - (getHeight() / 2));
		fixOffsets();
		game.human.updateOffset(xoff * -20.0 / (zoom * squareWidth), yoff * -20.0 / (zoom * squareHeight), getWidth() * 20.0 / (zoom * squareWidth), getHeight() * 20.0 / (zoom * squareHeight));
		game.human.repaintAll();
	}
	
	public void paintComponent(Graphics og){
		super.paintComponent(og);
		Graphics2D g = (Graphics2D)og;
		
		g.setFont(GUI.myFont);
		
		//Terrain loop
		for(int x = Math.max(0, game.gs.getFarLeft() - 1); x < Math.min(game.gs.getMapWidth(), game.gs.getFarRight() + 2); x++){
			for(int y = Math.max(0, game.gs.getFarUp() - 1); y < Math.min(game.gs.getMapHeight(), game.gs.getFarDown() + 2); y++){
				Cell cell = game.gs.getCell(x, y);
				
				if(cell.hasViewed(game.human)){
					g.drawImage(cell.getTerrain().getMainImage(), toPixels(x, true) , toPixels(y, false) ,  toPixels(1, true, false) ,  toPixels(1, false, false), null);
					for(int i = 0; i < 4; i++){
						if(cell.getCorner(i) == null || cell.getCorner(i).getTerrain() == null){
							g.drawImage(cell.getTerrain().getCornerImg(i), toPixels(x, true) , toPixels(y, false) ,  toPixels(1, true, false) ,  toPixels(1, false, false), null);
	
						}else{
							g.drawImage(cell.getCorner(i).getTerrain().getCornerImg(i), toPixels(x, true) , toPixels(y, false) ,  toPixels(1, true, false) ,  toPixels(1, false, false), null);
						}
					}
				}
			}
		}
		
		
		//Line loop
		for(int i = 0; i <= game.gs.getHighest(); i++){
		
			for(int x = Math.max(0, game.gs.getFarLeft() - 1); x < Math.min(game.gs.getMapWidth(), game.gs.getFarRight() + 2); x++){
				for(int y = Math.max(0, game.gs.getFarUp() - 1); y < Math.min(game.gs.getMapHeight(), game.gs.getFarDown() + 2); y++){
					Cell cell = game.gs.getCell(x, y);
					
					if(cell.getElevation() == i){
						
						
						if(cell.hasViewed(game.human)){
							
							
							//Corners
							for(int c = 0; c < 4; c++){
								if(cell.getCorner(c) != null){
									if(Math.abs(cell.getCorner(c).getHeightDiff()) > 1){
										g.setStroke(new BasicStroke(Math.abs(cell.getCorner(c).getHeightDiff()) * 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
									}else{
										g.setStroke(dashed);
			
									}
					
									
									if(cell.getCorner(c).getType() == Corner.Type.CURVED){
										g.setColor(Color.gray);
										if((c == 0 || c == 2) == cell.getCorner(c).getHeightDiff() > 0){
											g.setColor(Color.black);
										}else{
											g.setColor(Color.white);
										}
										Arc2D arc = null;
										if(c == 0 || c == 3){
											arc = new Arc2D.Double(toPixels(x + magicDoubs[c][0], true) + 1,toPixels( (y + magicDoubs[c][1]), false)  + 1, toPixels(.5, true, false)  - 1,toPixels( .5, false, false)  - 1, magicInts[c], 90, Arc2D.OPEN);
										}else{
											arc = new Arc2D.Double(toPixels(x + magicDoubs[c][0], true)   + 1,toPixels( (y + magicDoubs[c][1]), false)  - 1, toPixels(.5, true, false) ,toPixels( .5, false, false) , magicInts[c], 45, Arc2D.OPEN);
											g.draw(arc);
											if((c == 2) == cell.getCorner(c).getHeightDiff() > 0){
												g.setColor(Color.white);
											}else{
												g.setColor(Color.black);
											}
											arc = new Arc2D.Double(toPixels(x + magicDoubs[c][0], true)   + 1,toPixels( (y + magicDoubs[c][1]), false)  + 1, toPixels(.5, true, false) ,toPixels( .5, false, false) , magicInts[c] + 45, 45, Arc2D.OPEN);
			
										}
										g.draw(arc);
										
										
										if(c == 0 && cell.getCorner(c).getHeightDiff() > 0){
											g.setColor(Color.darkGray);
										}
										
										if((c == 0 || c == 2) == cell.getCorner(c).getHeightDiff() > 0){
											g.setColor(Color.darkGray);
										}else{
											g.setColor(Color.lightGray);
										}
										
										if(c == 0 || c == 3){
											arc = new Arc2D.Double(toPixels(x + magicDoubs[c][0], true)   + moreMagic[c],toPixels( (y + magicDoubs[c][1]), false)  + moreMagic[c], toPixels(.5, true, false)  + 2,toPixels( .5, false, false)  + 2, magicInts[c], 90, Arc2D.OPEN);
										}else{
											arc = new Arc2D.Double(toPixels(x + magicDoubs[c][0], true)   - 1,toPixels( (y + magicDoubs[c][1]), false)  - 1, toPixels(.5, true, false) ,toPixels( .5, false, false) , magicInts[c], 45, Arc2D.OPEN);
											g.draw(arc);
											if((c == 2) == cell.getCorner(c).getHeightDiff() > 0){
												g.setColor(Color.lightGray);
											}else{
												g.setColor(Color.darkGray);
											}
											
											arc = new Arc2D.Double(toPixels(x + magicDoubs[c][0], true)   + 1,toPixels( (y + magicDoubs[c][1]), false)  - 1, toPixels(.5, true, false) ,toPixels( .5, false, false) , magicInts[c] + 45, 45, Arc2D.OPEN);
											
										}
										g.draw(arc);
										
									}else if(cell.getCorner(c).getType() == Corner.Type.HORIZONTAL){
										
										if((cell.getCorner(c).getHeightDiff() > 0) == (c == 0 || c == 1)){
											g.setColor(Color.black);
										}else{
											g.setColor(Color.white);
										}
											
										g.drawLine(toPixels(x + magicDoubs[c][0] * 1.5,true) , (toPixels((y + magicDoubs[c][1] * 2), false)  + 1), toPixels((x + magicDoubs[c][0] * 1.5 + .25) , true), (toPixels((y + magicDoubs[c][1] * 2), false)  + 1));
										
										if((cell.getCorner(c).getHeightDiff() > 0) == (c == 0 || c == 1)){
											g.setColor(Color.darkGray);
										}else{
											g.setColor(Color.lightGray);
										}
										
										g.drawLine(toPixels(x + magicDoubs[c][0] * 1.5,true) , (toPixels((y + magicDoubs[c][1] * 2), false)  - 1), toPixels((x + magicDoubs[c][0] * 1.5 + .25) , true), (toPixels((y + magicDoubs[c][1] * 2), false)  - 1));
								
									}else{
										if((cell.getCorner(c).getHeightDiff() > 0) == (c == 0 || c == 2)){
											g.setColor(Color.black);
										}else{
											g.setColor(Color.white);
										}
										g.drawLine((toPixels(x + magicDoubs[c][0] * 2, true)  + 1), toPixels((y + magicDoubs[c][1] * 1.5), false), toPixels(x + magicDoubs[c][0] * 2 , true)  + 1, toPixels((y + magicDoubs[c][1] * 1.5 + .25), false));
										
										
										if((cell.getCorner(c).getHeightDiff() > 0) == (c == 0 || c == 2)){
											g.setColor(Color.darkGray);
										}else{
											g.setColor(Color.lightGray);
										}
										g.drawLine((toPixels(x + magicDoubs[c][0] * 2, true)  - 1), toPixels((y + magicDoubs[c][1] * 1.5), false), toPixels(x + magicDoubs[c][0] * 2 , true)  - 1, toPixels((y + magicDoubs[c][1] * 1.5 + .25), false));
									}
								}
							}
						}
							//Sides
						if(Math.abs(cell.getEdge(1)) > 0){
							if(Math.abs(cell.getEdge(1)) > 1){
								g.setStroke(new BasicStroke(Math.abs(cell.getEdge(1)) * 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
							}else{
								g.setStroke(dashed);
			
							}
							if(cell.getEdge(1) > 0){
								g.setColor(Color.black);
							}else{
								g.setColor(Color.white);
							}
							g.drawLine(toPixels(x + 1, true)  + 1, toPixels((y + .25), false), toPixels(x + 1, true)  + 1, toPixels((y + .75), false));
							
							if(cell.getEdge(1) > 0){
								g.setColor(Color.darkGray);
							}else{
								g.setColor(Color.lightGray);
							}
							
							g.drawLine(toPixels(x + 1, true)  - 1, toPixels((y + .25), false), toPixels(x + 1, true)  - 1, toPixels((y + .75), false));
						}
						
						if(cell.getEdge(0) != 0){
							if(Math.abs(cell.getEdge(0)) > 1){
								g.setStroke(new BasicStroke(Math.abs(cell.getEdge(0)) * 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
							}else{
								g.setStroke(dashed);
			
							}
							
							if(cell.getEdge(0) > 0){
								g.setColor(Color.black);
							}else{
								g.setColor(Color.white);
							}
							
							g.drawLine(toPixels((x + .25), true),toPixels( (y + 1), false)  + 1, toPixels((x + .75), true),toPixels( (y + 1), false)  + 1);
							
							if(cell.getEdge(0) > 0){
								g.setColor(Color.darkGray);
							}else{
								g.setColor(Color.lightGray);
							}
							
							g.drawLine(toPixels((x + .25), true),toPixels( (y + 1), false)  - 1, toPixels((x + .75), true),toPixels( (y + 1), false)  - 1);
			
			
						}
					}
				}
			}
		}
		
		
		
		
	
		g.setColor(new Color(.5f, .5f, .5f, .9f));
		
		//fog of war
		for(int x = Math.max(0, game.gs.getFarLeft() - 1); x < Math.min(game.gs.getMapWidth(), game.gs.getFarRight() + 2); x++){
			for(int y = Math.max(0, game.gs.getFarUp() - 1); y < Math.min(game.gs.getMapHeight(), game.gs.getFarDown() + 2); y++){
				Cell cell = game.gs.getCell(x, y);
				if(cell.hasViewed(game.human)){
					if(!cell.isViewed(game.human)){
						g.drawImage(Terrain.mainmask, toPixels(x, true) , toPixels(y, false) , toPixels(1, true, false) ,  toPixels(1, false, false), null);
					}
					
					if(y == game.gs.getMapHeight() - 1 || x == game.gs.getMapWidth() - 1){
						if(!cell.isViewed(game.human)){
							g.drawImage(Terrain.masks[0], toPixels(x, true) , toPixels(y, false) , toPixels(1, true, false) ,  toPixels(1, false, false), null);
						}
					}else if((!game.gs.getCell(x, y + 1).isViewed(game.human) && !game.gs.getCell(x + 1, y).isViewed(game.human))  || (!cell.isViewed(game.human) &&  (!game.gs.getCell(x, y + 1).isViewed(game.human) || !game.gs.getCell(x + 1, y).isViewed(game.human)))){
						if(!game.gs.getCell(x + 1, y + 1).isViewed(game.human) || !cell.isViewed(game.human)){
							g.drawImage(Terrain.masks[0], toPixels(x, true) , toPixels(y, false) , toPixels(1, true, false) ,  toPixels(1, false, false), null);
						}
					}
					
					if(y == game.gs.getMapHeight() - 1 || x == 0){
						if(!cell.isViewed(game.human)){
							g.drawImage(Terrain.masks[1], toPixels(x, true) , toPixels(y, false) , toPixels(1, true, false) ,  toPixels(1, false, false), null);
						}
					}else if((!game.gs.getCell(x, y + 1).isViewed(game.human) && !game.gs.getCell(x - 1, y).isViewed(game.human))  || (!cell.isViewed(game.human) &&  (!game.gs.getCell(x, y + 1).isViewed(game.human) || !game.gs.getCell(x - 1, y).isViewed(game.human)))){
						if(!game.gs.getCell(x - 1, y + 1).isViewed(game.human) || !cell.isViewed(game.human)){
							g.drawImage(Terrain.masks[1], toPixels(x, true) , toPixels(y, false) , toPixels(1, true, false) ,  toPixels(1, false, false), null);
						}				
					}
					
					if(y == 0|| x == game.gs.getMapWidth() - 1){
						if(!cell.isViewed(game.human)){
							g.drawImage(Terrain.masks[2], toPixels(x, true) , toPixels(y, false) , toPixels(1, true, false) ,  toPixels(1, false, false), null);
						}
					}else if((!game.gs.getCell(x, y - 1).isViewed(game.human) && !game.gs.getCell(x + 1, y).isViewed(game.human)) || (!cell.isViewed(game.human) &&  (!game.gs.getCell(x, y - 1).isViewed(game.human) || !game.gs.getCell(x + 1, y).isViewed(game.human)))){
						if(!game.gs.getCell(x + 1, y - 1).isViewed(game.human) || !cell.isViewed(game.human)){
							g.drawImage(Terrain.masks[2], toPixels(x, true) , toPixels(y, false) , toPixels(1, true, false) ,  toPixels(1, false, false), null);
						}
					}
					
					if(y == 0|| x == 0){
						if(!cell.isViewed(game.human)){
							g.drawImage(Terrain.masks[3], toPixels(x, true) , toPixels(y, false) , toPixels(1, true, false) ,  toPixels(1, false, false), null);
						}
					}else if((!game.gs.getCell(x, y - 1).isViewed(game.human) && !game.gs.getCell(x - 1, y).isViewed(game.human))  || (!cell.isViewed(game.human) &&  (!game.gs.getCell(x, y - 1).isViewed(game.human) || !game.gs.getCell(x - 1, y).isViewed(game.human)))){
						
						if(!game.gs.getCell(x - 1, y - 1).isViewed(game.human) || !cell.isViewed(game.human)){
							g.drawImage(Terrain.masks[3], toPixels(x, true) , toPixels(y, false) , toPixels(1, true, false) ,  toPixels(1, false, false), null);
						}
					}
				}else{
					g.setColor(Color.black);
					g.fillRect(toPixels(x, true) , toPixels(y, false) ,  toPixels(1, true, false) ,  toPixels(1, false, false));

				}
				

			}
		}
		
		Unit selectedUnit = game.human.getSelectedUnit();
		
		if(selectedUnit != null ){
			float selectoffx = 0;
			float selectoffy = 0;
			if(selectedUnit.isMoving()){
				int[] next = selectedUnit.lookAtNext();
				selectoffx = (next[0] - selectedUnit.getX()) * selectedUnit.getMoveStep() / (float)GUI.MAXSTEP;
				selectoffy  = (next[1] - selectedUnit.getY()) * selectedUnit.getMoveStep() / (float)GUI.MAXSTEP;
			}
			//Selected unit
			g.setColor(Color.yellow);
			g.setStroke(d2);
			g.drawArc(toPixels(selectedUnit.getX() + selectoffx, true), toPixels(selectedUnit.getY() + selectoffy, false), toPixels(1, true, false), toPixels(1, false, false), 0 , 360);
			//Path of selected unit
			if(game.getCurrentPlayer().isHuman() && !selectedUnit.isActive() && selectedUnit.getOwner().isHuman() && game.human.getSelectionMode() == Selection.MOVEMODE){
				if(hovX >=  0 && hovX < game.gs.getMapWidth() && hovY >= 0 && hovY < game.gs.getMapHeight()){
					g.setColor(Color.WHITE);
					g.setStroke(new BasicStroke(2));
					g.setFont(GUI.myFont);
					int x = hovX;
					int y = hovY;
					if((x != selectedUnit.getX() || y != selectedUnit.getY()) && game.gs.getCell(x, y).getMoveCost() != GameState.IMPOSSIBLE){
						g.drawString(String.valueOf(selectedUnit.getMovement() - game.gs.getCell(x, y).getMoveCost()), (toPixels(x + .4, true)  ), toPixels((y + .4), false));
						while(game.gs.getCell(x, y).getFromX() != -1){
							int newX = game.gs.getCell(x, y).getFromX();
							int newY = game.gs.getCell(x, y).getFromY();
							if(game.gs.getCell(newX, newY).getFromX() == -1){
								g.drawLine(toPixels(x + .5, true), toPixels(y + .5, false), toPixels(newX + .5 + selectoffx, true) , toPixels(newY + .5 + selectoffy, false));
	
							}else{
								g.drawLine(toPixels(x + .5, true), toPixels(y + .5, false), toPixels(newX + .5, true) , toPixels(newY + .5, false));
	
							}
							x = newX;
							y = newY;
						}
					}
				}
			}
		}
		
		

		boolean canMove = false;
		if(selectedUnit != null){
			for(int i = -1; i <= 1; i++){
				for(int j = -1; j <= 1; j++){
					if((i != 0 || j != 0) && selectedUnit.getX() + i >= 0 && selectedUnit.getX() + i < game.gs.getMapWidth() && selectedUnit.getY() + j >= 0 && selectedUnit.getY() + j < game.gs.getMapHeight() && game.gs.getCell(selectedUnit.getX() + i, selectedUnit.getY() + j).getMoveCost() != GameState.IMPOSSIBLE){
						canMove = true;
						break;
					}
					
				}
				if(canMove){
					break;
				}
			}
		}
		//Yellow movement lines
		for(int x = Math.max(0, game.gs.getFarLeft() - 1); x < Math.min(game.gs.getMapWidth(), game.gs.getFarRight() + 2); x++){
			for(int y = Math.max(0, game.gs.getFarUp() - 1); y < Math.min(game.gs.getMapHeight(), game.gs.getFarDown() + 2); y++){	
				Cell cell = game.gs.getCell(x, y);
				if(game.getCurrentPlayer().isHuman() && selectedUnit != null && !selectedUnit.isActive() && canMove && selectedUnit.getOwner().isHuman() && game.human.getSelectionMode() == Selection.MOVEMODE){
					g.setColor(Color.yellow);
					g.setStroke(dashed);
					if(x > 0 && (cell.getMoveCost() != GameState.IMPOSSIBLE) && (game.gs.getCell(x - 1, y).getMoveCost() == GameState.IMPOSSIBLE || game.gs.getCell(x - 1, y).isOccupied())){
						g.drawLine(toPixels(x,  true) - 10, toPixels(y, false) - 10, toPixels(x, true) - 10, toPixels(y + 1, false) + 10);
					}
					
					if(y > 0 && (cell.getMoveCost() != GameState.IMPOSSIBLE) && (game.gs.getCell(x, y - 1).getMoveCost() == GameState.IMPOSSIBLE || game.gs.getCell(x, y - 1).isOccupied())){
						g.drawLine(toPixels(x,  true) - 10, toPixels(y, false) - 10, toPixels(x + 1, true) + 10, toPixels(y, false) - 10);
					}
					
					if(x < game.gs.getMapWidth() - 1 && (cell.getMoveCost() != GameState.IMPOSSIBLE) && (game.gs.getCell(x + 1, y).getMoveCost() == GameState.IMPOSSIBLE || game.gs.getCell(x + 1, y).isOccupied())){
						g.drawLine(toPixels(x + 1,  true) + 10, toPixels(y, false) - 10, toPixels(x + 1, true) + 10, toPixels(y + 1, false) + 10);
					}
					
					if(y < game.gs.getMapHeight() - 1 && (cell.getMoveCost() != GameState.IMPOSSIBLE) && (game.gs.getCell(x, y + 1).getMoveCost() == GameState.IMPOSSIBLE || game.gs.getCell(x, y + 1).isOccupied())){
						g.drawLine(toPixels(x,  true) - 10, toPixels(y + 1, false) + 10, toPixels(x + 1, true) + 10, toPixels(y + 1, false) + 10);
					}
				}
				
				//Red shooting lines
				if(game.getCurrentPlayer().isHuman() && selectedUnit != null && game.human.getSelectionMode() != Selection.MOVEMODE && selectedUnit.getAttackCost(game.human.getSelectedModeString()) <= selectedUnit.getMovement()){
					g.setColor(Color.red);
					g.setStroke(dashed);
					if(x > 0 && (cell.canItBeShot(game.human)) && (!game.gs.getCell(x - 1, y).canItBeShot(game.human))){
						g.drawLine(toPixels(x,  true) - 10, toPixels(y, false) - 10, toPixels(x, true) - 10, toPixels(y + 1, false) + 10);
					}
					
					if(y > 0 && (cell.canItBeShot(game.human)) && (!game.gs.getCell(x, y - 1).canItBeShot(game.human))){
						g.drawLine(toPixels(x,  true) - 10, toPixels(y, false) - 10, toPixels(x + 1, true) + 10, toPixels(y, false) - 10);
					}
					
					if(x < game.gs.getMapWidth() - 1 && (cell.canItBeShot(game.human)) && (!game.gs.getCell(x + 1, y).canItBeShot(game.human))){
						g.drawLine(toPixels(x + 1,  true) + 10, toPixels(y, false) - 10, toPixels(x + 1, true) + 10, toPixels(y + 1, false) + 10);
					}
					
					if(y < game.gs.getMapHeight() - 1 && (cell.canItBeShot(game.human)) && (!game.gs.getCell(x, y + 1).canItBeShot(game.human))){
						g.drawLine(toPixels(x,  true) - 10, toPixels(y + 1, false) + 10, toPixels(x + 1, true) + 10, toPixels(y + 1, false) + 10);
					}
				}
				
				
				// draw the unit
				if(cell.getUnit() != null){
					
					
					//Red target circles
					if(selectedUnit != null  && game.human.getSelectionMode() != Selection.MOVEMODE && cell.canItBeShot(game.human) && cell.getUnit().getOwner().isAI() && selectedUnit.getAttackCost(game.human.getSelectedModeString()) <= selectedUnit.getMovement()){
						g.setColor(Color.red);
						if(hovX == x && hovY == y && !selectedUnit.isShooting()){
							g.fillArc(toPixels(x, true), toPixels(y, false), toPixels(1, true, false), toPixels(1, false, false), 0 , 360);
						}else{
							
							g.drawArc(toPixels(x, true), toPixels(y, false), toPixels(1, true, false), toPixels(1, false, false), 0 , 360);
	
						}
					}
				
				
				

					if(cell.isViewed(game.human) || cell.getUnit().isMoving()){
						boolean draw = cell.isViewed(game.human);
						float xAmount = 0;
						float yAmount = 0;
						if(cell.getUnit().isMoving()){
							int[] next = cell.getUnit().lookAtNext();
							draw = draw || game.gs.getCell(next[0], next[1]).isViewed(game.human);
							xAmount = (next[0] - x) * cell.getUnit().getMoveStep() / (float)GUI.MAXSTEP;
							yAmount = (next[1] - y) * cell.getUnit().getMoveStep() / (float)GUI.MAXSTEP;
						}
						if(draw){
							g.setColor(Color.WHITE);
							g.setStroke(new BasicStroke(10));
							
							//path of any moving unit
							int pathX = x;
							int pathY = y;
							ListIterator<int[]> it = cell.getUnit().getPathIterator();
							if(it.hasNext()){
								int[] next = it.next();
								g.drawLine(toPixels(pathX + .5 + xAmount, true), toPixels(pathY + .5 + yAmount, false), toPixels(next[0] + .5, true), toPixels(next[1] + .5, false));
								pathX = next[0];
								pathY = next[1];
								while(it.hasNext()){
									next = it.next();
									g.drawLine(toPixels(pathX + .5, true), toPixels(pathY + .5, false), toPixels(next[0] + .5, true), toPixels(next[1] + .5, false));
									pathX = next[0];
									pathY = next[1];
								}
							}
							
							
							g.drawImage(cell.getUnit().getImage(), toPixels(x + xAmount + .1, true), toPixels(y + yAmount + .1, false), toPixels(.8, true, false),  toPixels(.8, true, false), null);
						
							if(game.human.getBarMode().equals("Always") ||
								(game.human.getBarMode().equals("Selected") && cell.getUnit().equals(selectedUnit) || 
								(game.human.getBarMode().equals("Hover") && hovX == x && hovY == y))){
								
									drawHPBar(g, x, y, xAmount, yAmount, cell);
									drawMoveBar(g, x, y, xAmount, yAmount, cell);
							}else if(game.human.getBarMode().equals("Used")){
								if(cell.getUnit().getHP() < cell.getUnit().getType().getMaxHP()){
									drawHPBar(g, x, y, xAmount, yAmount, cell);
								}
								
								if(cell.getUnit().getMovement() < cell.getUnit().getMaxMove()){
									drawMoveBar(g, x, y, xAmount, yAmount, cell);
								}
							}
							
							
						}
					}
					if(selectedUnit != null  && game.human.getSelectionMode() != Selection.MOVEMODE && cell.canItBeShot(game.human) && cell.getUnit().getOwner().isAI() && selectedUnit.getAttackCost(game.human.getSelectedModeString()) <= selectedUnit.getMovement()){
						if(hovX == x && hovY == y && !selectedUnit.isShooting()){
							g.setColor(Color.black);
							g.fillRect(toPixels(x + .3, true), toPixels(y + .3, false), toPixels(.55, true, false), toPixels(.3, false, false));
							int acc = selectedUnit.getAcc(game.human.getSelectedModeString(), game.gs.getCell(selectedUnit).getElevation() < cell.getElevation());
							g.setColor(getColor(acc / 100f));
							g.drawString(selectedUnit.getAcc(game.human.getSelectedModeString(), game.gs.getCell(selectedUnit).getElevation() < cell.getElevation()) + "%", toPixels(x + .4, true), toPixels(y + .5, false));
						}
					}
				}
				
			}
		}
		
		for(int x = 0; x < game.gs.getMapWidth(); x++){
			for(int y = 0; y < game.gs.getMapHeight(); y++){
				Cell cell = game.gs.getCell(x, y);
				if(cell.getUnit() != null){
					if(cell.getUnit().isShooting()){
						if(game.gs.getCell(cell.getUnit().getTarget()).isViewed(game.human)){
							g.setColor(Color.red);
							g.setStroke(new BasicStroke(3));
							g.drawArc(toPixels(cell.getUnit().getTarget().getX(), true), toPixels(cell.getUnit().getTarget().getY(), false), toPixels(1, true, false), toPixels(1, false, false), 0 , 360);
							
							if(cell.getUnit().getTarget().getY() == 0){
								g.drawString("-" + cell.getUnit().getDamage(), toPixels(cell.getUnit().getTarget().getX() + .4, true), toPixels(cell.getUnit().getTarget().getY() + .2, false));
							}else{
								g.drawString("-" + cell.getUnit().getDamage(), toPixels(cell.getUnit().getTarget().getX() + .4, true), toPixels(cell.getUnit().getTarget().getY() - .1, false));

							}
						}
						if(cell.getUnit().getOwner().isAI()){
							g.setColor(Color.YELLOW);
							g.setStroke(dashed);
							g.drawArc(toPixels(x, true), toPixels(y, false), toPixels(1, true, false), toPixels(1, false, false), 0 , 360);
						}
						
					}
				}
				//XXX debuging
				//g.setColor(Color.YELLOW);
				//g.drawString(x + ":" + y, toPixels(x + .5, true), toPixels(y + .5, false));
			}
		}
		
		
		//Scoreboard
		
		if(game.human.doShowScore()){

			int count = 0;
			for(Player p: game.getPlayers()){
				g.setColor(ContrastColor(p.getColour()));
				g.fillRect(getWidth() - 160, 25 * count, 160, 25);
				g.setColor(p.getColour());

				g.drawString(p.getName() + ": ", getWidth() - 150, 25 * (count) + 18);
				String s = String.valueOf(p.getScore());
				int amount = g.getFontMetrics().stringWidth(s);
				
				g.drawString(s, getWidth() - 5 - amount, 25 * (count) + 18);
				count ++;
			}
			
		}

	}
	
	
	private static Color ContrastColor(Color color){
	    int d = 0;

	    // Counting the perceptive luminance - human eye favors green color... 
	    double a = 1 - ( 0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue())/255;

	    if (a < 0.5)
	       d = 0; // bright colors - black font
	    else
	       d = 255; // dark colors - white font

	    return  new Color(d, d, d);
	}
	
	

	private void drawHPBar(Graphics2D g, int x, int y, float xAmount, float yAmount, Cell cell) {
		g.setStroke(new BasicStroke());
		g.setColor(Color.black);
		g.fillRect(toPixels(x + xAmount + .1, true), toPixels(y + yAmount, false), toPixels(.8, true, false), toPixels(.05, false, false));
		float hp = cell.getUnit().getHP() / (float)cell.getUnit().getType().getMaxHP();
		hp = Math.max(0, hp);
		g.setColor(getColor(hp));
		g.fillRect(toPixels(x + xAmount + .1, true), toPixels(y + yAmount, false), toPixels(hp * .8, true, false), toPixels(.05, false, false));

	}
	
	private static Color getColor(float value) {
		if(value < .5){
			return new Color(1, value, 0);
		}
		return new Color(1 - value, 1, 0);
	}


	private void drawMoveBar(Graphics2D g, int x, int y, float xAmount, float yAmount, Cell cell) {
		g.setColor(Color.darkGray);
		g.fillRect(toPixels(x + xAmount + .1, true), toPixels(y + yAmount + .05, false), toPixels(.8, true, false), toPixels(.05, false, false));
		g.setColor(Color.lightGray);
		double move = cell.getUnit().getMovement() / cell.getUnit().getMaxMove();
		g.fillRect(toPixels(x + xAmount + .1, true), toPixels(y + yAmount + .05, false), toPixels(move * .8, true, false), toPixels(.05, false, false));

	}

	

	public void doHover(int x, int y, int modifiers) {
		if(modifiers == 0){
			hovX = x;
			hovY = y;
		}
		game.human.repaintAll();
		
	}


	
}
