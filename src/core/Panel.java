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
import gui.Menu;
import gui.MiniMap;
import gui.Selection;
import helpers.AI;
import helpers.Corner;
import helpers.Player;
import helpers.Terrain;
import helpers.Zone;


@SuppressWarnings("serial")
public class Panel extends JPanel implements MouseListener, MouseMotionListener{

	private static final int squareHeight = 200;
	private static final int squareWidth = 200;
	private Selection selection;
	private JPanel sidebar = new JPanel();

	private int zoom = 10;
	private int xoff;
	private int yoff;
	
	private final static int magicInts[] = {270, 180, 0, 90};
	private final static double magicDoubs[][] = {{.5, .5}, {0, .5}, {.5, 0}, {0, 0}};
	private final static int moreMagic[] = {-3, 0, 0, -1};
	
	private final static BasicStroke dashed = new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2f, new float[]{10f, 4f}, 0.0f);
	private final static BasicStroke d2 = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2f, new float[]{10f, 4f}, 0.0f);

	private static final int MAXSTEP = 10;
	private static final int MAXSHOOT = 20;
	private static final int TICKTIME = 50;
	public static final Font myFont = new Font("Courier", Font.BOLD, 20);
	private int prevY = -1;
	private int prevX = -1;
	private int hovX = -1;
	private int hovY = -1;
	private MiniMap mm;
	private JButton end = new JButton("End Turn");
	

	private Iterator<Unit> tab;
	
	private GameType gt;
	private Menu menu;
	
	private Timer time;

	
	
	public static void start(int mapWidth, int mapHeight, int hillyness, int armySize, ArrayList<Player> players, long mapSeed, long gameSeed, String gameType) {
	
		start(GameType.newGameType(gameType, new GameState(mapWidth, mapHeight, hillyness, armySize, players, mapSeed, gameSeed, gameType)));
		
		
	}
	
	public static void start(GameType gt){
		
		
		final Panel p = new Panel(gt);
		UIManager.put("OptionPane.buttonFont", myFont);
		UIManager.put("OptionPane.messageFont", myFont);
		JFrame frame = new JFrame();
		
		p.setLayout(new BorderLayout());
		p.sidebar.setLayout(new BoxLayout(p.sidebar, BoxLayout.Y_AXIS));
		JPanel top  = new JPanel();

		JButton mapmode = new JButton();
		p.mm = new MiniMap(p.gt.gs, squareHeight / (double)squareWidth, mapmode);
		p.sidebar.add(p.mm);
		

		
		p.mm.addMouseListener(p);
		p.mm.addMouseMotionListener(p);
		JLabel bob = new JLabel("Map mode:");
		bob.setFont(myFont);
		top.add(Box.createRigidArea(new Dimension(10,0)));
		top.add(bob);
		top.add(Box.createRigidArea(new Dimension(20,0)));
		
		mapmode.setFont(myFont);
		top.add(mapmode);
		top.add(Box.createHorizontalGlue());
		top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
		top.setPreferredSize(new Dimension(300, 50));
		
		mapmode.addActionListener(p.mm);
		
		p.sidebar.add(top);
		p.selection = new Selection(p.gt.gs, p.end);
		p.end.setFont(myFont);
		p.end.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				p.endTurn();
			}
		});
		p.sidebar.add(p.selection);
		p.sidebar.add(Box.createVerticalGlue());

	
		

		p.menu = new Menu(frame, "Menu", true, p.time, p);
		frame.add(p.sidebar, BorderLayout.WEST);
		frame.add(p, BorderLayout.CENTER);
		frame.setSize(1200, 1000);
		frame.setLocation(600, 20);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		p.fixOffsets();
		p.time.start();
		System.out.println("It is now " + p.gt.gs.getPlayer(p.gt.gs.getTurn()).getName() + "'s turn...");
		
	}
	







	public Panel(GameType gameType){
		super();
		gt = gameType;

		tab = gt.gs.getHuman().getUnits().iterator();
		
		for(Player p:gt.gs.getPlayers()){
			if(p.isAI()){
				((AI)p).setGameState(gt.gs);
			}
		}
		
		
		
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
					doClick((e.getX() - xoff) * 20 / (zoom * squareWidth) , (e.getY() - yoff) * 20 / (zoom * squareHeight), e.getModifiers(), e.getClickCount());
				}else if(e.getButton() == MouseEvent.BUTTON3){
					doRightClick((e.getX() - xoff) * 20 / (zoom * squareWidth) , (e.getY() - yoff) * 20 / (zoom * squareHeight), e.getModifiers());

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
				
				

				Panel.this.repaintAll();;
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
				Panel.this.repaintAll();;
			}
			
		});
		
		addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent arg0) {}
			
			@Override
			public void componentResized(ComponentEvent arg0) {
				mm.updateOffset(xoff * -20.0 / (zoom * squareWidth), yoff * -20 / (zoom * squareHeight), Panel.this.getWidth() * 20 / (zoom * squareWidth), Panel.this.getHeight() * 20 / (zoom * squareHeight));

				
			}
			
			@Override
			public void componentMoved(ComponentEvent arg0) {}
			
			@Override
			public void componentHidden(ComponentEvent arg0) {}
		});
		
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher( new KeyEventDispatcher() {
			public boolean dispatchKeyEvent(KeyEvent e) {
					if (e.getID() == KeyEvent.KEY_PRESSED && allDialogsClosed()) {
						keyPress(e.getKeyCode(), e.getModifiers());
					}
			        return false;
		        }
		});
		



		time = new Timer(TICKTIME, new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				tick();
			}
		});
	
		
	}
	
	

	public void loadGame(GameType gameType) {
		gt = gameType;

		

		gt.gs = gameType.getGameState();
		gt.gs.loadAllImages();
		for(Player p:gt.gs.getPlayers()){
			if(p.isAI()){
				((AI)p).setGameState(gt.gs);
			}
		}
		tab = gt.gs.getHuman().getUnits().iterator();
		selection.setGameState(gt.gs);
		mm.setGameState(gt.gs);

		menu.close();
		centerAt(gt.gs.getHuman().getUnits().get(0));
		repaintAll();
	}
	
	
	
	protected boolean allDialogsClosed() {
		return !menu.isVisible();
	}



	@SuppressWarnings("unused")
	protected void keyPress(int keyCode, int modifiers) {
		switch (keyCode) {
		case KeyEvent.VK_TAB:
			if(gt.gs.getCurrentPlayer().isHuman()){
				while(!selectNext()){
					tab = gt.gs.getHuman().getUnits().iterator();
				}
			}
			
			break;
		case KeyEvent.VK_BACK_SPACE:
			if(end.isEnabled()){
				endTurn();
			}
			break;
			
		case KeyEvent.VK_ESCAPE:
		case KeyEvent.VK_F10:
			menu.open(gt);
			break;
		default:
			System.out.println("Keypress: " + KeyEvent.getKeyText(keyCode));
			break;
		}
		
	}
	
	


	private boolean selectNext() {
		Unit u;
		while(tab.hasNext()){
			u = tab.next();
			if(u.getMovement() > 0){
				selection.select(u);
				centerAt(u);
				return true;
			}
		}
		
	
		return false;
	}


	private void fixOffsets(){
		xoff = Math.max(xoff, Panel.this.getWidth() - toPixels(Math.min(gt.gs.getFarRight() + 2, gt.gs.getMapWidth()), true, false));
		yoff = Math.max(yoff, Panel.this.getHeight() - toPixels(Math.min(gt.gs.getFarDown() + 2, gt.gs.getMapHeight()), false, false));
		xoff = Math.min(xoff,  -1 * toPixels(Math.max(gt.gs.getFarLeft() - 1, 0), true, false));
		yoff = Math.min(yoff,  -1 * toPixels(Math.max(gt.gs.getFarUp() - 1, 0), false, false));

		mm.updateOffset(xoff * -20.0 / (zoom * squareWidth), yoff * -20.0 / (zoom * squareHeight), Panel.this.getWidth() * 20 / (zoom * squareWidth), Panel.this.getHeight() * 20 / (zoom * squareHeight));
	}

	protected void tick() {
		boolean dirty = false;
		boolean busy = false;
		for(Player p:gt.gs.getPlayers()){
			for(Unit u:p.getUnits()){
				if(u.isMoving()){
					if(u.getMoveStep() == MAXSTEP){
						int[] next = u.popNext();
						
						if(!gt.gs.moveUnit(u, next[0], next[1], u.popCost())){
							selection.setButtonsEnabled(true);
						}
						if(u.equals(selection.getUnit())){
							selection.dirty();
						}
						dirty = true;
					}else{
						u.tick();
					}
				}
				
				if(u.isShooting()){
					if(u.getMoveStep() == MAXSHOOT || (!gt.gs.getCell(u).isViewed(gt.gs.getHuman()) && !gt.gs.getCell(u.getTarget()).isViewed(gt.gs.getHuman()))){
						if(u.getTarget().getHP() <= 0){
							removeUnit(u.getOwner(), u.getTarget());
							if(u.getTarget().equals(selection.getUnit())){
								selection.deselect();
							}
						}
						u.doneShooting();
						dirty = true;
					}else{
						u.tick();
					}
				}
				
				if(u.isActive()){
					if(!gt.gs.getCurrentPlayer().isHuman()){
						centerAt(u);
					}
					busy = true;
				}
			}
		}
		
	
		Player winner = gt.tick();
		if(winner != null){
			JOptionPane.showMessageDialog(this, winner.getName() + " wins the game!", "Winner!", JOptionPane.INFORMATION_MESSAGE);
			System.exit(0);
		}
		
		if(!busy){
			if(gt.gs.getCurrentPlayer().isHuman()){
				end.setEnabled(true);
			}else{
				if(!((AI) gt.gs.getCurrentPlayer()).doSomething()){
					endTurn();
				}
			}
		}
		if(dirty){
			gt.gs.calculateViewing();
			if(gt.gs.getCurrentPlayer().isAI() && selection.getMode() != Selection.MOVEMODE){
				gt.gs.calculateShooting(selection.getUnit(), selection.getModeString());
			}
			selection.findWhereYouCanMove();
			if(selection.getUnit() != null && !gt.gs.getCell(selection.getUnit()).isViewed(gt.gs.getHuman())){
				selection.deselect();
			}
		}
		repaintAll();
	}

	private void removeUnit(Player killer, Unit unit) {
		gt.killUnit(killer, unit.getOwner());
		gt.gs.getCell(unit).removeUnit();
		unit.getOwner().removeUnit(unit);
		
	}


	@SuppressWarnings("unused")
	protected void doRightClick(int x, int y, int modifiers) {
		System.out.println(x + ":" + y);
		if(gt.gs.getCurrentPlayer().isHuman() && x < gt.gs.getMapWidth() && y < gt.gs.getMapHeight() && selection.getUnit() != null && !selection.getUnit().isActive() && (selection.getUnit().getX() != x || selection.getUnit().getY() != y) && selection.getUnit().getOwner().isHuman() && selection.getMode() == Selection.MOVEMODE){
			if(gt.gs.getCell(x, y).getMoveCost() != GameState.IMPOSSIBLE){
				gt.gs.setDestination(selection.getUnit(), x, y);
				selection.setButtonsEnabled(false);
				end.setEnabled(false);
			}
		}
		repaintAll();
	}

	protected void endTurn() {
		gt.gs.incrementTurn();
		
		System.out.println("It is now " + gt.gs.getCurrentPlayer().getName() + "'s turn...");
		selection.dirty();
		if(gt.gs.getCurrentPlayer().isHuman()){
			selection.setButtonsEnabled(true);
			if(selection.getUnit() != null){
				selection.findWhereYouCanMove();
			}
		}else{
			selection.setButtensSelected(false);
			selection.setButtonsEnabled(false);
			end.setEnabled(false);
		}
		
	}

	

	protected void doClick(int x, int y, int mod, int num) {
		if(num == 1){
			if((mod & InputEvent.SHIFT_MASK) != 0){
				System.out.println("##############################");
				System.out.println(x + ":" + y);
				System.out.println(gt.gs.getCell(x, y));
				System.out.println("##############################");
			}
				
			if(x < gt.gs.getMapWidth() && y < gt.gs.getMapHeight()){
				if(gt.gs.getCell(x, y) != null && gt.gs.getCell(x, y).isViewed(gt.gs.getHuman())){
					if(gt.gs.getCell(x, y).getUnit() != null){
						if(selection.getUnit() == null){
							selection.select(gt.gs.getCell(x, y).getUnit());
						}else{
						
							if(gt.gs.getCell(x, y).getUnit().getOwner().isHuman() || selection.getMode() == Selection.MOVEMODE){
								selection.select(gt.gs.getCell(x, y).getUnit());
							}else{
								if(gt.gs.getCurrentPlayer().isHuman() && gt.gs.getCell(x, y).getUnit() != null && gt.gs.getCell(x, y).getUnit().getOwner().isAI()){
									if(selection.getUnit().getAttackCost(selection.getModeString()) <= selection.getUnit().getMovement()){
										selection.getUnit().shootAt(gt.gs.getCell(x, y).getUnit(), selection.getModeString(), gt.gs.getCell(selection.getUnit()).getElevation() < gt.gs.getCell(x, y).getElevation());
										selection.dirty();
										end.setEnabled(false);
										selection.findWhereYouCanMove();
										selection.setMode(Selection.MOVEMODE);
									}
								}
							}
						}
						
					}else{
						selection.deselect();
					}
				}
			}
		}else if(num == 2){
			/*
			if( selection.getUnit() != null && selection.getMode() == Selection.MOVEMODE){
				selection.getUnit().setMovement(selection.getUnit().getMaxMove());
			}*/
			
		}
		repaintAll();
		
	}
	
	protected void doHover(int x, int y, int modifiers) {
		if(modifiers == 0){
			hovX = x;
			hovY = y;
		}
		repaintAll();
		
	}

	
	private void repaintAll() {
		selection.updateSidebar(gt.gs.getTurn());
		sidebar.repaint();
		repaint();
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
	
	private void centerAt(Unit u){
		centerAt(u.getX(), u.getY());
	}
	
	private void centerAt(int x, int y){
		xoff = -1 * (toPixels(x, true, false) - (getWidth() / 2));
		yoff = -1 * (toPixels(y, false, false) - (getHeight() / 2));
		fixOffsets();
		mm.updateOffset(xoff * -20.0 / (zoom * squareWidth), yoff * -20.0 / (zoom * squareHeight), getWidth() * 20.0 / (zoom * squareWidth), getHeight() * 20.0 / (zoom * squareHeight));
		repaintAll();
	}
	
	public void paintComponent(Graphics og){
		super.paintComponent(og);
		Graphics2D g = (Graphics2D)og;
		
		g.setFont(myFont);
		
		//Terrain loop
		for(int x = Math.max(0, gt.gs.getFarLeft() - 1); x < Math.min(gt.gs.getMapWidth(), gt.gs.getFarRight() + 2); x++){
			for(int y = Math.max(0, gt.gs.getFarUp() - 1); y < Math.min(gt.gs.getMapHeight(), gt.gs.getFarDown() + 2); y++){
				Cell cell = gt.gs.getCell(x, y);
				
				if(cell.hasViewed(gt.gs.getHuman())){
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
		for(int i = 0; i <= gt.gs.getHighest(); i++){
		
			for(int x = Math.max(0, gt.gs.getFarLeft() - 1); x < Math.min(gt.gs.getMapWidth(), gt.gs.getFarRight() + 2); x++){
				for(int y = Math.max(0, gt.gs.getFarUp() - 1); y < Math.min(gt.gs.getMapHeight(), gt.gs.getFarDown() + 2); y++){
					Cell cell = gt.gs.getCell(x, y);
					
					if(cell.getElevation() == i){
						
						
						if(cell.hasViewed(gt.gs.getHuman())){
							
							
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
		for(int x = Math.max(0, gt.gs.getFarLeft() - 1); x < Math.min(gt.gs.getMapWidth(), gt.gs.getFarRight() + 2); x++){
			for(int y = Math.max(0, gt.gs.getFarUp() - 1); y < Math.min(gt.gs.getMapHeight(), gt.gs.getFarDown() + 2); y++){
				Cell cell = gt.gs.getCell(x, y);
				if(cell.hasViewed(gt.gs.getHuman())){
					if(!cell.isViewed(gt.gs.getHuman())){
						g.drawImage(Terrain.mainmask, toPixels(x, true) , toPixels(y, false) , toPixels(1, true, false) ,  toPixels(1, false, false), null);
					}
					
					if(y == gt.gs.getMapHeight() - 1 || x == gt.gs.getMapWidth() - 1){
						if(!cell.isViewed(gt.gs.getHuman())){
							g.drawImage(Terrain.masks[0], toPixels(x, true) , toPixels(y, false) , toPixels(1, true, false) ,  toPixels(1, false, false), null);
						}
					}else if((!gt.gs.getCell(x, y + 1).isViewed(gt.gs.getHuman()) && !gt.gs.getCell(x + 1, y).isViewed(gt.gs.getHuman()))  || (!cell.isViewed(gt.gs.getHuman()) &&  (!gt.gs.getCell(x, y + 1).isViewed(gt.gs.getHuman()) || !gt.gs.getCell(x + 1, y).isViewed(gt.gs.getHuman())))){
						if(!gt.gs.getCell(x + 1, y + 1).isViewed(gt.gs.getHuman()) || !cell.isViewed(gt.gs.getHuman())){
							g.drawImage(Terrain.masks[0], toPixels(x, true) , toPixels(y, false) , toPixels(1, true, false) ,  toPixels(1, false, false), null);
						}
					}
					
					if(y == gt.gs.getMapHeight() - 1 || x == 0){
						if(!cell.isViewed(gt.gs.getHuman())){
							g.drawImage(Terrain.masks[1], toPixels(x, true) , toPixels(y, false) , toPixels(1, true, false) ,  toPixels(1, false, false), null);
						}
					}else if((!gt.gs.getCell(x, y + 1).isViewed(gt.gs.getHuman()) && !gt.gs.getCell(x - 1, y).isViewed(gt.gs.getHuman()))  || (!cell.isViewed(gt.gs.getHuman()) &&  (!gt.gs.getCell(x, y + 1).isViewed(gt.gs.getHuman()) || !gt.gs.getCell(x - 1, y).isViewed(gt.gs.getHuman())))){
						if(!gt.gs.getCell(x - 1, y + 1).isViewed(gt.gs.getHuman()) || !cell.isViewed(gt.gs.getHuman())){
							g.drawImage(Terrain.masks[1], toPixels(x, true) , toPixels(y, false) , toPixels(1, true, false) ,  toPixels(1, false, false), null);
						}				
					}
					
					if(y == 0|| x == gt.gs.getMapWidth() - 1){
						if(!cell.isViewed(gt.gs.getHuman())){
							g.drawImage(Terrain.masks[2], toPixels(x, true) , toPixels(y, false) , toPixels(1, true, false) ,  toPixels(1, false, false), null);
						}
					}else if((!gt.gs.getCell(x, y - 1).isViewed(gt.gs.getHuman()) && !gt.gs.getCell(x + 1, y).isViewed(gt.gs.getHuman())) || (!cell.isViewed(gt.gs.getHuman()) &&  (!gt.gs.getCell(x, y - 1).isViewed(gt.gs.getHuman()) || !gt.gs.getCell(x + 1, y).isViewed(gt.gs.getHuman())))){
						if(!gt.gs.getCell(x + 1, y - 1).isViewed(gt.gs.getHuman()) || !cell.isViewed(gt.gs.getHuman())){
							g.drawImage(Terrain.masks[2], toPixels(x, true) , toPixels(y, false) , toPixels(1, true, false) ,  toPixels(1, false, false), null);
						}
					}
					
					if(y == 0|| x == 0){
						if(!cell.isViewed(gt.gs.getHuman())){
							g.drawImage(Terrain.masks[3], toPixels(x, true) , toPixels(y, false) , toPixels(1, true, false) ,  toPixels(1, false, false), null);
						}
					}else if((!gt.gs.getCell(x, y - 1).isViewed(gt.gs.getHuman()) && !gt.gs.getCell(x - 1, y).isViewed(gt.gs.getHuman()))  || (!cell.isViewed(gt.gs.getHuman()) &&  (!gt.gs.getCell(x, y - 1).isViewed(gt.gs.getHuman()) || !gt.gs.getCell(x - 1, y).isViewed(gt.gs.getHuman())))){
						
						if(!gt.gs.getCell(x - 1, y - 1).isViewed(gt.gs.getHuman()) || !cell.isViewed(gt.gs.getHuman())){
							g.drawImage(Terrain.masks[3], toPixels(x, true) , toPixels(y, false) , toPixels(1, true, false) ,  toPixels(1, false, false), null);
						}
					}
				}else{
					g.setColor(Color.black);
					g.fillRect(toPixels(x, true) , toPixels(y, false) ,  toPixels(1, true, false) ,  toPixels(1, false, false));

				}
				

			}
		}
		
		

		if(selection.getUnit() != null ){
			float selectoffx = 0;
			float selectoffy = 0;
			if(selection.getUnit().isMoving()){
				int[] next = selection.getUnit().lookAtNext();
				selectoffx = (next[0] - selection.getUnit().getX()) * selection.getUnit().getMoveStep() / (float)MAXSTEP;
				selectoffy  = (next[1] - selection.getUnit().getY()) * selection.getUnit().getMoveStep() / (float)MAXSTEP;
			}
			//Selected unit
			g.setColor(Color.yellow);
			g.setStroke(d2);
			g.drawArc(toPixels(selection.getUnit().getX() + selectoffx, true), toPixels(selection.getUnit().getY() + selectoffy, false), toPixels(1, true, false), toPixels(1, false, false), 0 , 360);
			//Path of selected unit
			if(gt.gs.getCurrentPlayer().isHuman() && !selection.getUnit().isActive() && selection.getUnit().getOwner().isHuman() && selection.getMode() == Selection.MOVEMODE){
				if(hovX >=  0 && hovX < gt.gs.getMapWidth() && hovY >= 0 && hovY < gt.gs.getMapHeight()){
					g.setColor(Color.WHITE);
					g.setStroke(new BasicStroke(2));
					g.setFont(myFont);
					int x = hovX;
					int y = hovY;
					if((x != selection.getUnit().getX() || y != selection.getUnit().getY()) && gt.gs.getCell(x, y).getMoveCost() != GameState.IMPOSSIBLE){
						g.drawString(String.valueOf(selection.getUnit().getMovement() - gt.gs.getCell(x, y).getMoveCost()), (toPixels(x + .4, true)  ), toPixels((y + .4), false));
						while(gt.gs.getCell(x, y).getFromX() != -1){
							int newX = gt.gs.getCell(x, y).getFromX();
							int newY = gt.gs.getCell(x, y).getFromY();
							if(gt.gs.getCell(newX, newY).getFromX() == -1){
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
		if(selection.getUnit() != null){
			for(int i = -1; i <= 1; i++){
				for(int j = -1; j <= 1; j++){
					if((i != 0 || j != 0) && selection.getUnit().getX() + i >= 0 && selection.getUnit().getX() + i < gt.gs.getMapWidth() && selection.getUnit().getY() + j >= 0 && selection.getUnit().getY() + j < gt.gs.getMapHeight() && gt.gs.getCell(selection.getUnit().getX() + i, selection.getUnit().getY() + j).getMoveCost() != GameState.IMPOSSIBLE){
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
		for(int x = Math.max(0, gt.gs.getFarLeft() - 1); x < Math.min(gt.gs.getMapWidth(), gt.gs.getFarRight() + 2); x++){
			for(int y = Math.max(0, gt.gs.getFarUp() - 1); y < Math.min(gt.gs.getMapHeight(), gt.gs.getFarDown() + 2); y++){	
				Cell cell = gt.gs.getCell(x, y);
				if(gt.gs.getCurrentPlayer().isHuman() && selection.getUnit() != null && !selection.getUnit().isActive() && canMove && selection.getUnit().getOwner().isHuman() && selection.getMode() == Selection.MOVEMODE){
					g.setColor(Color.yellow);
					g.setStroke(dashed);
					if(x > 0 && (cell.getMoveCost() != GameState.IMPOSSIBLE) && (gt.gs.getCell(x - 1, y).getMoveCost() == GameState.IMPOSSIBLE || gt.gs.getCell(x - 1, y).isOccupied())){
						g.drawLine(toPixels(x,  true) - 10, toPixels(y, false) - 10, toPixels(x, true) - 10, toPixels(y + 1, false) + 10);
					}
					
					if(y > 0 && (cell.getMoveCost() != GameState.IMPOSSIBLE) && (gt.gs.getCell(x, y - 1).getMoveCost() == GameState.IMPOSSIBLE || gt.gs.getCell(x, y - 1).isOccupied())){
						g.drawLine(toPixels(x,  true) - 10, toPixels(y, false) - 10, toPixels(x + 1, true) + 10, toPixels(y, false) - 10);
					}
					
					if(x < gt.gs.getMapWidth() - 1 && (cell.getMoveCost() != GameState.IMPOSSIBLE) && (gt.gs.getCell(x + 1, y).getMoveCost() == GameState.IMPOSSIBLE || gt.gs.getCell(x + 1, y).isOccupied())){
						g.drawLine(toPixels(x + 1,  true) + 10, toPixels(y, false) - 10, toPixels(x + 1, true) + 10, toPixels(y + 1, false) + 10);
					}
					
					if(y < gt.gs.getMapHeight() - 1 && (cell.getMoveCost() != GameState.IMPOSSIBLE) && (gt.gs.getCell(x, y + 1).getMoveCost() == GameState.IMPOSSIBLE || gt.gs.getCell(x, y + 1).isOccupied())){
						g.drawLine(toPixels(x,  true) - 10, toPixels(y + 1, false) + 10, toPixels(x + 1, true) + 10, toPixels(y + 1, false) + 10);
					}
				}
				
				//Red shooting lines
				if(gt.gs.getCurrentPlayer().isHuman() && selection.getUnit() != null && selection.getMode() != Selection.MOVEMODE && selection.getUnit().getAttackCost(selection.getModeString()) <= selection.getUnit().getMovement()){
					g.setColor(Color.red);
					g.setStroke(dashed);
					if(x > 0 && (cell.canItBeShot(gt.gs.getHuman())) && (!gt.gs.getCell(x - 1, y).canItBeShot(gt.gs.getHuman()))){
						g.drawLine(toPixels(x,  true) - 10, toPixels(y, false) - 10, toPixels(x, true) - 10, toPixels(y + 1, false) + 10);
					}
					
					if(y > 0 && (cell.canItBeShot(gt.gs.getHuman())) && (!gt.gs.getCell(x, y - 1).canItBeShot(gt.gs.getHuman()))){
						g.drawLine(toPixels(x,  true) - 10, toPixels(y, false) - 10, toPixels(x + 1, true) + 10, toPixels(y, false) - 10);
					}
					
					if(x < gt.gs.getMapWidth() - 1 && (cell.canItBeShot(gt.gs.getHuman())) && (!gt.gs.getCell(x + 1, y).canItBeShot(gt.gs.getHuman()))){
						g.drawLine(toPixels(x + 1,  true) + 10, toPixels(y, false) - 10, toPixels(x + 1, true) + 10, toPixels(y + 1, false) + 10);
					}
					
					if(y < gt.gs.getMapHeight() - 1 && (cell.canItBeShot(gt.gs.getHuman())) && (!gt.gs.getCell(x, y + 1).canItBeShot(gt.gs.getHuman()))){
						g.drawLine(toPixels(x,  true) - 10, toPixels(y + 1, false) + 10, toPixels(x + 1, true) + 10, toPixels(y + 1, false) + 10);
					}
				}
				
				
				// draw the unit
				if(cell.getUnit() != null){
					
					
					//Red target circles
					if(selection.getUnit() != null  && selection.getMode() != Selection.MOVEMODE && cell.canItBeShot(gt.gs.getHuman()) && cell.getUnit().getOwner().isAI() && selection.getUnit().getAttackCost(selection.getModeString()) <= selection.getUnit().getMovement()){
						g.setColor(Color.red);
						if(hovX == x && hovY == y && !selection.getUnit().isShooting()){
							g.fillArc(toPixels(x, true), toPixels(y, false), toPixels(1, true, false), toPixels(1, false, false), 0 , 360);
						}else{
							
							g.drawArc(toPixels(x, true), toPixels(y, false), toPixels(1, true, false), toPixels(1, false, false), 0 , 360);
	
						}
					}
				
				
				

					if(cell.isViewed(gt.gs.getHuman()) || cell.getUnit().isMoving()){
						boolean draw = cell.isViewed(gt.gs.getHuman());
						float xAmount = 0;
						float yAmount = 0;
						if(cell.getUnit().isMoving()){
							int[] next = cell.getUnit().lookAtNext();
							draw = draw || gt.gs.getCell(next[0], next[1]).isViewed(gt.gs.getHuman());
							xAmount = (next[0] - x) * cell.getUnit().getMoveStep() / (float)MAXSTEP;
							yAmount = (next[1] - y) * cell.getUnit().getMoveStep() / (float)MAXSTEP;
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
						
							if(menu.getBarMode().equals("Always") ||
								(menu.getBarMode().equals("Selected") && cell.getUnit().equals(selection.getUnit()) || 
								(menu.getBarMode().equals("Hover") && hovX == x && hovY == y))){
								
									drawHPBar(g, x, y, xAmount, yAmount, cell);
									drawMoveBar(g, x, y, xAmount, yAmount, cell);
							}else if(menu.getBarMode().equals("Used")){
								if(cell.getUnit().getHP() < cell.getUnit().getType().getMaxHP()){
									drawHPBar(g, x, y, xAmount, yAmount, cell);
								}
								
								if(cell.getUnit().getMovement() < cell.getUnit().getMaxMove()){
									drawMoveBar(g, x, y, xAmount, yAmount, cell);
								}
							}
							
							
						}
					}
					if(selection.getUnit() != null  && selection.getMode() != Selection.MOVEMODE && cell.canItBeShot(gt.gs.getHuman()) && cell.getUnit().getOwner().isAI() && selection.getUnit().getAttackCost(selection.getModeString()) <= selection.getUnit().getMovement()){
						if(hovX == x && hovY == y && !selection.getUnit().isShooting()){
							g.setColor(Color.black);
							g.fillRect(toPixels(x + .3, true), toPixels(y + .3, false), toPixels(.55, true, false), toPixels(.3, false, false));
							int acc = selection.getUnit().getAcc(selection.getModeString(), gt.gs.getCell(selection.getUnit()).getElevation() < cell.getElevation());
							g.setColor(getColor(acc / 100f));
							g.drawString(selection.getUnit().getAcc(selection.getModeString(), gt.gs.getCell(selection.getUnit()).getElevation() < cell.getElevation()) + "%", toPixels(x + .4, true), toPixels(y + .5, false));
						}
					}
				}
				
			}
		}
		
		for(int x = 0; x < gt.gs.getMapWidth(); x++){
			for(int y = 0; y < gt.gs.getMapHeight(); y++){
				Cell cell = gt.gs.getCell(x, y);
				if(cell.getUnit() != null){
					if(cell.getUnit().isShooting()){
						if(gt.gs.getCell(cell.getUnit().getTarget()).isViewed(gt.gs.getHuman())){
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
		
		if(menu.doShowScore()){

			int count = 0;
			for(Player p: gt.gs.getPlayers()){
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
	
	
	/* The methods belong are for the minimap only!!*/

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

	@Override
	public void mouseClicked(MouseEvent e) {
		int x = mm.toCell(e.getX(), true);
		int y = mm.toCell(e.getY(), false);
		System.out.println(x + ":" + y);
		centerAt(x, y);
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int x = mm.toCell(e.getX(), true);
		int y = mm.toCell(e.getY(), false);
		System.out.println(x + ":" + y);
		centerAt(x, y);
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}








	public GameType getGameType() {
		return gt;
	}

	
}
