package gui;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Files;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import core.GameType;
import core.Panel;

@SuppressWarnings("serial")
public class Menu extends JDialog {
	
	
	private JPanel options = new JPanel();
	private String barMode = "Always";
	private boolean showScore = true;
	private JComboBox<String> bars = new JComboBox<>(new String[]{"Always", "Selected", "Hover", "Used"});
	private JCheckBox score = new JCheckBox("Show score");
	private Timer time;
	private JFrame frame;
	private GameType gt;

	public Menu(JFrame frame, String string, boolean b, Timer time) {
		super(frame, string, b);
		this.frame = frame;
		this.time = time;
		setSize(300, 400);
		

		JPanel menuPanel = new JPanel();
		menuPanel.setLayout(new GridLayout(5, 1, 20, 20));
		menuPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
		JMenuItem menuBut = new JMenuItem("Menu");
		add(menuPanel);
		
		JMenuBar bar = new JMenuBar();
		bar.add(Box.createHorizontalGlue());
		JButton optBut = new JButton("Options");
		bar.add(menuBut);
		menuBut.setFont(GUI.myFont);
		frame.setJMenuBar(bar);
		optBut.setFont(GUI.myFont);
		menuBut.setMaximumSize( new Dimension(menuBut.getPreferredSize().width, menuBut.getMaximumSize().height));
		menuBut.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				open(gt);
			}
		});
		
		optBut.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int save = JOptionPane.showConfirmDialog(Menu.this, options, "Options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				if(save == JOptionPane.OK_OPTION){
					barMode = (String)bars.getSelectedItem();
					showScore = score.isSelected();
				}else{
					bars.setSelectedItem(barMode);
					score.setSelected(showScore);
				}
			}
		});
		
		
		
		
		JButton saveGame = new JButton("Save game");
		saveGame.setFont(GUI.myFont);
		saveGame.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				assert gt != null;
				FileDialog.showSaveGameDialog(Menu.this, gt);
			}
		});
		
		
		JButton loadGame = new JButton("Load game");
		loadGame.setFont(GUI.myFont);
		loadGame.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				GameType.load(FileDialog.showLoadGameDialog(Menu.this));
			}
		});


		
		JButton exit = new JButton("Exit Game");
		exit.setFont(GUI.myFont);
		exit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(JOptionPane.showConfirmDialog(Menu.this.frame, "Are you sure you want to exit?", "Exit", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
					System.exit(0);
				}
				
			}
		});
		
		JButton returnBut = new JButton("Return to game");
		returnBut.setFont(GUI.myFont);
		returnBut.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		
		

		menuPanel.add(returnBut);
		menuPanel.add(saveGame);
		menuPanel.add(loadGame);
		menuPanel.add(optBut);
		menuPanel.add(exit);
		
		

		bars.setFont(GUI.myFont);
		JLabel barLabel = new JLabel("Status bars");
		barLabel.setFont(GUI.myFont);
		options.add(barLabel);
		options.add(bars);
		score.setSelected(showScore);
		score.setFont(GUI.myFont);
		options.add(score);
		options.add(new JLabel());
		
		options.setLayout(new GridLayout(2, 2, 20, 20));
	}

	public void open(GameType gameType) {
		Menu.this.time.stop();
		setLocation(frame.getLocationOnScreen().x + (frame.getWidth() - getWidth()) / 2, frame.getLocationOnScreen().y + (frame.getHeight() - getHeight()) / 2);
		this.gt = gameType;
		setVisible(true);
	}

	public JPanel getOptions() {
		return options;
	}

	public String getBarMode() {
		return barMode;
	}

	public boolean doShowScore() {
		return showScore;
	}

	public void close() {
		setVisible(false);
		time.start();
		gt = null;
	}
	
	

}
