package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;

import core.GameState;
import core.GameType;
import core.Panel;
import core.Unit;
import helpers.AI;
import helpers.Player;

public class GUI extends Player implements MouseListener, MouseMotionListener{
	
	private static final long serialVersionUID = 1L;
	private transient Selection selection;
	private transient JPanel sidebar = new JPanel();
	public transient static final int MAXSTEP = 10;
	private static final int MAXSHOOT = 20;
	private static final int TICKTIME = 50;
	public static final Font myFont = new Font("Courier", Font.BOLD, 20);
	
	private transient  MiniMap mm;
	private transient  JButton end = new JButton("End Turn");
	
	private transient  Menu menu;
	private transient  Panel panel;
	private transient  Timer time;

	private transient Iterator<Unit> tab;
	

	public GUI(Color col, String name, GameType gt){
		super(col, name, gt);
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
		
		panel = new Panel(gt);
		UIManager.put("OptionPane.buttonFont", myFont);
		UIManager.put("OptionPane.messageFont", myFont);
		JFrame frame = new JFrame();
		
		sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
		JPanel top  = new JPanel();

		JButton mapmode = new JButton();
		mm = new MiniMap(gt, Panel.squareHeight / (double)Panel.squareWidth, mapmode);
		sidebar.add(mm);
		

		
		mm.addMouseListener(this);
		mm.addMouseMotionListener(this);
		JLabel bob = new JLabel("Map mode:");
		bob.setFont(myFont);
		top.add(Box.createRigidArea(new Dimension(10,0)));
		top.add(bob);
		top.add(Box.createRigidArea(new Dimension(20,0)));
		
		mapmode.setFont(myFont);
		top.add(mapmode);
		top.add(Box.createHorizontalGlue());
		top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
		top.setPreferredSize(new Dimension(310, 50));
		
		mapmode.addActionListener(mm);
		
		sidebar.add(top);
		selection = new Selection(game, end);
		end.setFont(myFont);
		end.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				endTurn();
			}
		});
		sidebar.add(selection);
		sidebar.add(Box.createVerticalGlue());


		

		menu = new Menu(frame, "Menu", true, time);
		frame.add(sidebar, BorderLayout.WEST);
		frame.add(panel, BorderLayout.CENTER);
		frame.setSize(1200, 1000);
		frame.setLocation(600, 20);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		time.start();
	}
	
	
	public void setup(){
		tab = getUnits().iterator();
		panel.fixOffsets();
		selection.deselect();
	}
	

	@SuppressWarnings("unused")
	protected void keyPress(int keyCode, int modifiers) {
		switch (keyCode) {
		case KeyEvent.VK_TAB:
			if(game.getCurrentPlayer().isHuman()){
				while(!selectNext()){
					tab = game.human.getUnits().iterator();
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
			menu.open(game);
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
				game.human.select(u);
				panel.centerAt(u);
				return true;
			}
		}
		
	
		return false;
	}

	protected void tick() {
		boolean dirty = false;
		boolean busy = false;
		for(Player p:game.getPlayers()){
			for(Unit u:p.getUnits()){
				if(u.isMoving()){
					if(u.getMoveStep() == MAXSTEP){
						int[] next = u.popNext();
						
						if(!game.gs.moveUnit(u, next[0], next[1], u.popCost())){
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
					if(u.getMoveStep() == MAXSHOOT || (!game.gs.getCell(u).isViewed(game.human) && !game.gs.getCell(u.getTarget()).isViewed(game.human))){
						if(u.getTarget().getHP() <= 0){
							game.removeUnit(u.getOwner(), u.getTarget());
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
					if(!game.getCurrentPlayer().isHuman()){
						panel.centerAt(u);
					}
					busy = true;
				}
			}
		}
		
	
		Player winner = game.tick();
		if(winner != null){
			JOptionPane.showMessageDialog(panel, winner.getName() + " wins the game!", "Winner!", JOptionPane.INFORMATION_MESSAGE);
			System.exit(0);
		}
		
		if(!busy){
			if(game.getCurrentPlayer().isHuman()){
				end.setEnabled(true);
			}else{
				if(!((AI) game.getCurrentPlayer()).doSomething()){
					endTurn();
				}
			}
		}
		if(dirty){
			game.gs.calculateViewing();
			if(game.getCurrentPlayer().isAI() && selection.getMode() != Selection.MOVEMODE){
				game.gs.calculateShooting(selection.getUnit(), selection.getModeString());
			}
			selection.findWhereYouCanMove();
			if(selection.getUnit() != null && !game.gs.getCell(selection.getUnit()).isViewed(game.human)){
				selection.deselect();
			}
		}
		repaintAll();
	}

	
	
	
	protected boolean allDialogsClosed() {
		return !menu.isVisible();
	}




	@SuppressWarnings("unused")
	public void doRightClick(int x, int y, int modifiers) {
		System.out.println(x + ":" + y);
		if(game.getCurrentPlayer().isHuman() && x < game.gs.getMapWidth() && y < game.gs.getMapHeight() && selection.getUnit() != null && !selection.getUnit().isActive() && (selection.getUnit().getX() != x || selection.getUnit().getY() != y) && selection.getUnit().getOwner().isHuman() && selection.getMode() == Selection.MOVEMODE){
			if(game.gs.getCell(x, y).getMoveCost() != GameState.IMPOSSIBLE){
				game.gs.setDestination(selection.getUnit(), x, y);
				selection.setButtonsEnabled(false);
				end.setEnabled(false);
			}
		}
		repaintAll();
	}

	protected void endTurn() {
		game.incrementTurn();
		
		System.out.println("It is now " + game.getCurrentPlayer().getName() + "'s turn...");
		selection.dirty();
		if(game.getCurrentPlayer().isHuman()){
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

	

	public void doClick(int x, int y, int mod, int num) {
		if(num == 1){
			if((mod & InputEvent.SHIFT_MASK) != 0){
				System.out.println("##############################");
				System.out.println(x + ":" + y);
				System.out.println(game.gs.getCell(x, y));
				System.out.println("##############################");
			}
				
			if(x < game.gs.getMapWidth() && y < game.gs.getMapHeight()){
				if(game.gs.getCell(x, y) != null && game.gs.getCell(x, y).isViewed(game.human)){
					if(game.gs.getCell(x, y).getUnit() != null){
						if(selection.getUnit() == null){
							selection.select(game.gs.getCell(x, y).getUnit());
						}else{
						
							if(game.gs.getCell(x, y).getUnit().getOwner().isHuman() || selection.getMode() == Selection.MOVEMODE){
								selection.select(game.gs.getCell(x, y).getUnit());
							}else{
								if(game.getCurrentPlayer().isHuman() && game.gs.getCell(x, y).getUnit() != null && game.gs.getCell(x, y).getUnit().getOwner().isAI()){
									if(selection.getUnit().getAttackCost(selection.getModeString()) <= selection.getUnit().getMovement()){
										selection.getUnit().shootAt(game.gs.getCell(x, y).getUnit(), selection.getModeString(), game.gs.getCell(selection.getUnit()).getElevation() < game.gs.getCell(x, y).getElevation());
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
	

	
	public void repaintAll() {
		selection.updateSidebar(game.getTurn());
		sidebar.repaint();
		panel.repaint();
	}



	public void updateOffset(double xo, double yo, double width, double height) {
		mm.updateOffset(xo, yo, width, height);
		
	}



	public void select(Unit u) {
		selection.select(u);
		
	}



	public Unit getSelectedUnit() {
		return selection.getUnit();
	}



	public int getSelectionMode() {
		return selection.getMode();
	}



	public String getSelectedModeString() {
		return selection.getModeString();
	}








	/* MINI MAP */
	
	@Override
	public void mouseClicked(MouseEvent e) {
		int x = mm.toCell(e.getX(), true);
		int y = mm.toCell(e.getY(), false);
		System.out.println(x + ":" + y);
		panel.centerAt(x, y);
		
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
		panel.centerAt(x, y);
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public boolean doShowScore() {
		return menu.doShowScore();
	}

	public String getBarMode() {
		return menu.getBarMode();
	}




}
