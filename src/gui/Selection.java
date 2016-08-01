package gui;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

import core.Unit;


@SuppressWarnings("serial")
public class Selection extends JPanel implements ActionListener{
	
	public static final int MOVEMODE = -1;
	
	//private static final int STATSHEIGHT = 239;
	private LocalPlayer player;
	private JLabel stats = new JLabel();
	private JPanel bp = new JPanel();
	
	private Unit unit = null;
	private boolean dirty = true;
	
	private int mode = MOVEMODE;
	
	private ArrayList<JToggleButton> buttons = new ArrayList<JToggleButton>();
	
	public Selection (LocalPlayer player, JButton end){
		assert player != null;
		this.player = player;
		stats.setFont(GUI.myFont);
		stats.setBorder( new EmptyBorder( 20, 20, 20, 20 ) );
		setLayout(new BorderLayout());
		add(stats, BorderLayout.NORTH);
		add(bp, BorderLayout.CENTER);
		add(end, BorderLayout.SOUTH);
		bp.setLayout(new BoxLayout(bp, BoxLayout.Y_AXIS));
		stats.setOpaque(true);
		
	}

	


	public void select(Unit u) {
		dirty = true;
		this.unit = u;
		setMode(MOVEMODE);
		player.findWhereYouCanMove(u);
		setButtonsEnabled(!unit.isActive());
		repaint();
	}

	
	public void setMode(int i) {
		assert unit != null;
		mode = i;
		updateStatsText(mode);
		if(i != MOVEMODE){
			player.calculateShooting(unit, getModeString());
		}
	}




	private void updateStatsText(int i) {
		if(i == MOVEMODE){
			stats.setText("<html><pre>" + unit.sidebarPrint(null) + "</pre></html>");
		}else{
			stats.setText("<html><pre>" + unit.sidebarPrint(getModeString(i)) + "</pre></html>");
		}
	}




	private String getModeString(int i) {
		return buttons.get(i).getText();
	}




	

	public void updateSidebar(){
		if(dirty){
			dirty = false;
			if(unit != null){
				updateStatsText(mode);
				if(unit.getOwner().isHuman()){
					int i = 0;
					for(String name:unit.getType().getAttackNames()){
						if(buttons.size() <= i){
							buttons.add(new JToggleButton());
							bp.add(buttons.get(i));
							buttons.get(i).addActionListener(this);
							buttons.get(i).setMaximumSize(new Dimension(200, 40));
							bp.add(Box.createRigidArea(new Dimension(0, 20)));
							buttons.get(i).setAlignmentX(Component.CENTER_ALIGNMENT);
							buttons.get(i).setFont(GUI.myFont);
						}
						
						buttons.get(i).setText(name);
						if(mode == MOVEMODE){
							buttons.get(i).setSelected(false);
						}
						if(player.getGameCurrentPlayer().isHuman() && unit.getMovement() > unit.getAttackCost(name)){
							buttons.get(i).setEnabled(true);
						}else{
							buttons.get(i).setEnabled(false);
						}
						buttons.get(i).setVisible(true);
						i++;
					}
					for(;i < buttons.size(); i++){
						buttons.get(i).setVisible(false);
					}
					
					bp.setVisible(true);
					
				}else{
					bp.setVisible(false);
					for(JToggleButton b : buttons){
						b.setVisible(false);
					}
					
				}
	
			}else{
				stats.setText("");
				bp.setVisible(false);
				for(JToggleButton b : buttons){
					b.setVisible(false);
				}
			}
			revalidate();
			repaint();
		}
	}


	public Unit getUnit() {
		return unit;
	}


	public void deselect() {
		unit = null;
		dirty = true;
		repaint();
	}



//This is for when attack buttons are clicked
	@Override
	public void actionPerformed(ActionEvent e) {
		for(int i = 0; i < buttons.size(); i++){
			if(buttons.get(i).equals(e.getSource())){
				if(buttons.get(i).isSelected()){
					setMode(i);
				}else{
					setMode(MOVEMODE);					
				}
			}else{
				buttons.get(i).setSelected(false);
			}
		}
		
	}




	public int getMode() {
		return mode;
	}




	public void dirty() {
		dirty = true;
		
	}




	public void setButtonsEnabled(boolean b) {
		if(unit != null){
			for(JToggleButton but: buttons){
				if(unit.getAttackCost(but.getText()) >= unit.getMovement()){
					but.setEnabled(b);
				}
			}
		}
		
	}




	public String getModeString() {
		assert mode != MOVEMODE;
		return getModeString(mode);
	}




	public void setButtonsSelected(boolean b) {
		for(JToggleButton but: buttons){
			but.setSelected(b);
		}
		mode = MOVEMODE;
	}




	public void setPlayer(LocalPlayer player) {
		this.player = player;
		
	}






}


