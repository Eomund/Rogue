package gui;

import helpers.Action.Type;

import java.awt.BorderLayout;
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
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import core.GameState;
import core.Unit;

public class GUI implements MouseListener, MouseMotionListener{
	
	private Selection selection;
	private JPanel sidebar = new JPanel();
	public static final int MAXSTEP = 10;
	public static final int MAXSHOOT = 20;
	public static final int TICKTIME = 50;
	public static final Font myFont = new Font("Courier", Font.BOLD, 20);
	
	private MiniMap mm;
	private JButton end = new JButton("End Turn");
	
	private Menu menu;
	private Panel panel;

	private LocalPlayer player;

	private transient Iterator<Unit> tab;
	

	public GUI(LocalPlayer player){
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher( new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
					if (e.getID() == KeyEvent.KEY_PRESSED && allDialogsClosed()) {
						keyPress(e.getKeyCode(), e.getModifiers());
					}
			        return false;
		        }
		});
		
		this.player = player;
		
		panel = new Panel(player);
		UIManager.put("OptionPane.buttonFont", myFont);
		UIManager.put("OptionPane.messageFont", myFont);
		JFrame frame = new JFrame();
		
		sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
		JPanel top  = new JPanel();

		JButton mapmode = new JButton();
		mm = new MiniMap(player, Panel.squareHeight / (double)Panel.squareWidth, mapmode);
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
		selection = new Selection(player, end);
		end.setFont(myFont);
		end.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				GUI.this.player.endTurn();
			}
		});
		sidebar.add(selection);
		sidebar.add(Box.createVerticalGlue());


		

		menu = new Menu(frame, "Menu", true);
		frame.add(sidebar, BorderLayout.WEST);
		frame.add(panel, BorderLayout.CENTER);
		frame.setSize(1200, 1000);
		frame.setLocation(600, 20);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	
	public void setup(){
		tab = player.getUnits().iterator();
		panel.fixOffsets();
		selection.deselect();
		centerAt(player.getUnits().get(0));
	}
	

	protected void keyPress(int keyCode, int modifiers) {
		switch (keyCode) {
		case KeyEvent.VK_TAB:
			if(player.getGameCurrentPlayer().isHuman()){
				while(!selectNext()){
					tab = player.getUnits().iterator();
				}
			}
			
			break;
		case KeyEvent.VK_BACK_SPACE:
			if(end.isEnabled()){
				player.endTurn();
			}
			break;
			
		case KeyEvent.VK_ESCAPE:
		case KeyEvent.VK_F10:
			menu.open(player);
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
				player.gui.select(u);
				panel.centerAt(u);
				return true;
			}
		}
		
	
		return false;
	}


	
	
	
	protected boolean allDialogsClosed() {
		return !menu.isVisible();
	}




	public void doRightClick(int x, int y, int modifiers) {
		System.out.println(x + ":" + y);
		if(player.getGameCurrentPlayer().isHuman() && x < player.getMapWidth() && y < player.getMapHeight() && selection.getUnit() != null && !selection.getUnit().isActive() && (selection.getUnit().getX() != x || selection.getUnit().getY() != y) && selection.getUnit().getOwner().isHuman() && selection.getMode() == Selection.MOVEMODE){
			if(player.getCellMoveCost(x, y) != GameState.IMPOSSIBLE){
				
				player.addAction(selection.getUnit(), x, y, Type.DESTINATION);
				selection.setButtonsEnabled(false);
				end.setEnabled(false);
			}
		}
		repaintAll();
	}

	

	

	public void doClick(int x, int y, int mod, int num) {
		if(num == 1){
			if((mod & InputEvent.SHIFT_MASK) != 0){
				System.out.println("##############################");
				System.out.println(x + ":" + y);
				System.out.println(player.getCell(x, y));
				System.out.println("##############################");
			}
				
			if(x < player.getMapWidth() && y < player.getMapHeight()){
				if(player.getCell(x, y) != null && player.getCell(x, y).isViewed(player)){
					if(player.getCell(x, y).getUnit() != null){
						if(selection.getUnit() == null){
							selection.select(player.getCell(x, y).getUnit());
						}else{
						
							if(player.getCell(x, y).getUnit().getOwner().isHuman() || selection.getMode() == Selection.MOVEMODE){
								selection.select(player.getCell(x, y).getUnit());
							}else{
								if(player.getGameCurrentPlayer().isHuman() && player.getCell(x, y).getUnit() != null && player.getCell(x, y).getUnit().getOwner().isAI()){
									if(selection.getUnit().getAttackCost(selection.getModeString()) <= selection.getUnit().getMovement()){
										
										player.addAction(selection.getUnit(), x, y, Type.SHOOT);
										selection.dirty();
										end.setEnabled(false);
										player.findWhereYouCanMove(selection.getUnit());
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
		selection.updateSidebar();
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


	public void setSelectionButtonsEnabled(boolean b) {
		selection.setButtonsEnabled(b);
		
	}


	public void dirtySelection() {
		selection.dirty();
		
	}


	public void deselect() {
		selection.deselect();
		
	}


	public void centerAt(Unit u) {
		panel.centerAt(u);
	}


	public JPanel getPanel() {
		return panel;
	}


	public void setEndEnabled(boolean b) {
		end.setEnabled(b);
		
	}


	public void setSelectionButtonsSelected(boolean b) {
		selection.setButtonsSelected(b);
	}


	public void setPlayer(LocalPlayer localPlayer) {
		this.player = localPlayer;
		selection.setPlayer(player);
		panel.setPlayer(player);
		mm.setPlayer(player);
		
	}




}
