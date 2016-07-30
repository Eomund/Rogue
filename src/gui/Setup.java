package gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import core.GameType;
import core.Panel;
import helpers.AI;
import helpers.Config;
import helpers.Player;
import helpers.Zone;


public class Setup implements ActionListener, MouseListener, ChangeListener{
	
	private final int mapMinimum;
	private final int MAPMAXIMUM = 100;
	private static final int HILLMINIMUM = 25;
	private static final int HILLMAXIMUM = 50;
	private static final int ARMYMAX = 12;
	private static final int ABSOLUTEMAXTEAMS = 13;
	
	private int mapWidth = -1;
	private int mapHeight = -1;
	
	private int buffer = -1;
	private int largestArmy = -1;
	private JSlider width = new JSlider();
	private JSlider height = new JSlider();
	private JSlider hillyness = new JSlider();
	private JSlider sizeSlider = new JSlider();
	private JTabbedPane tabs = new JTabbedPane();
	private JTextField names[] = new JTextField[ABSOLUTEMAXTEAMS];
	private JLabel colourLabels[] = new JLabel[ABSOLUTEMAXTEAMS]; 
	private JComboBox<String> active[] = new JComboBox[ABSOLUTEMAXTEAMS];
	private Color colours[] = new Color[]{Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW, Color.BLACK, Color.WHITE, Color.CYAN, Color.PINK, Color.MAGENTA, Color.GRAY, Color.LIGHT_GRAY, Color.DARK_GRAY, Color.ORANGE};
	private JFrame frame = new JFrame();
	private final static String[] gameTypes = new String[]{"Elimination"};
	private JComboBox<String> gameType = new JComboBox<>(gameTypes);
	private int horZone = -1;
	private int verZone = -1;
	

    public Setup(final long mapSeed, final long gameSeed){
    	for(String[] u:Config.rawUnits){
    		int val = Integer.parseInt(u[2].trim());
    		if(val > buffer){
    			buffer = val;
    		}
    	}
    	
    	
    	mapMinimum = buffer + 6;
    	
    	
    	
		
		JPanel[] panels = new JPanel[names.length];
		JPanel[] bot = new JPanel[names.length];
		JButton[] next = new JButton[names.length];
		String[] tabNames = new String[]{"Map", "Army Size"};
		
		for(int i = 0; i < tabNames.length; i++){
			panels[i] = new JPanel();
			panels[i].setBorder( new EmptyBorder( 20, 20, 20, 20 ) );
			panels[i].setLayout(new BorderLayout());
			bot[i] = new JPanel();
			bot[i].setLayout(new BorderLayout());
			next[i] = new JButton("Next");
			next[i].addActionListener(this);
			next[i].setFont(GUI.myFont);
			bot[i].add(next[i], BorderLayout.EAST);
			panels[i].add(bot[i], BorderLayout.SOUTH);
			tabs.add(tabNames[i], new JScrollPane(panels[i]));
		}
	

		JPanel main = new JPanel();
		main.setLayout(new GridLayout(4, 1));
		main.setBorder(new EmptyBorder(0, 0, 250, 0));
		JLabel wLabel = new JLabel("Width");
		wLabel.setFont(GUI.myFont);
		main.add(wLabel);
		


		width.setMinimum(mapMinimum);
		width.setMaximum(MAPMAXIMUM);
		width.setMajorTickSpacing(25);
		width.setMinorTickSpacing(5);
		width.setPaintLabels(true);
		width.setPaintTicks(true);
		width.setFont(GUI.myFont);
		width.setValue(width.getMinimum());
		width.setPaintTrack(true);
		width.putClientProperty( "Slider.paintThumbArrowShape", Boolean.TRUE );
		main.add(width);
		
		JLabel hLabel = new JLabel("Height");
		hLabel.setFont(GUI.myFont);
		main.add(hLabel);
		height.setMinimum(mapMinimum);
		height.setMaximum(MAPMAXIMUM);
		height.setMajorTickSpacing(25);
		height.setMinorTickSpacing(5);
		height.setPaintLabels(true);
		height.setPaintTicks(true);
		height.setFont(GUI.myFont);
		height.setValue(height.getMinimum());
		height.putClientProperty( "Slider.paintThumbArrowShape", Boolean.TRUE );
		main.add(height);
		
		JLabel hillLabel = new JLabel("Hillyness");
		hillLabel.setFont(GUI.myFont);
		main.add(hillLabel);
		hillyness.setMinimum(HILLMINIMUM);
		hillyness.setMaximum(HILLMAXIMUM);
		hillyness.setValue(30);
		hillyness.setMajorTickSpacing(10);
		hillyness.setMinorTickSpacing(1);
		hillyness.setPaintLabels(true);
		hillyness.setPaintTicks(true);
		hillyness.setFont(GUI.myFont);
		hillyness.putClientProperty( "Slider.paintThumbArrowShape", Boolean.TRUE );
		main.add(hillyness);
		
		panels[0].add(main, BorderLayout.CENTER);
		
		
		JPanel armySize = new JPanel();
		JLabel sizeLabel = new JLabel("Army Size");
		sizeLabel.setFont(GUI.myFont);
		armySize.add(sizeLabel);
		sizeSlider.setFont(GUI.myFont);
		armySize.add(sizeSlider);
		sizeSlider.setMinimum(1);
		sizeSlider.setMinorTickSpacing(1);
		sizeSlider.setPaintLabels(true);
		sizeSlider.setPaintTicks(true);
		sizeSlider.putClientProperty( "Slider.paintThumbArrowShape", Boolean.TRUE );

		panels[1].add(armySize, BorderLayout.CENTER);
		
		JLabel gt = new JLabel("Game Type");
		armySize.add(gt);
		
		armySize.setLayout(new GridLayout(5, 1, 20, 20));
		armySize.add(gameType);
		gt.setFont(GUI.myFont);
		gameType.setFont(GUI.myFont);
		
		armySize.setBorder(new EmptyBorder(20, 20, 250, 20));
		
		JPanel tMain = new JPanel();
		
		JPanel teamPanel = new JPanel();
		teamPanel.setLayout(new BorderLayout());
		teamPanel.add(tMain, BorderLayout.CENTER);
	
		tMain.setLayout(new GridLayout(ABSOLUTEMAXTEAMS, 3, 10, 10));
		tMain.setBorder(new EmptyBorder(0, 0, 20, 0));
		teamPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

		for(int i = 0; i < colourLabels.length; i++){
			colourLabels[i] = new JLabel();
			colourLabels[i].setOpaque(true);
			colourLabels[i].setBackground(colours[i]);
			colourLabels[i].setBorder(BorderFactory.createLineBorder(Color.black));
			names[i] = new JTextField("Player " + (i + 1));
			active[i] = new JComboBox<String>(new String[]{"AI", "Closed"});
			if(i < 2){
				active[i].setSelectedItem("AI");
			}else{
				active[i].setSelectedItem("Closed");
			}
			active[i].setFont(GUI.myFont);
			tMain.add(active[i]);
			tMain.add(names[i]);
			tMain.add(colourLabels[i]);
			names[i].setFont(GUI.myFont);
			colourLabels[i].addMouseListener(this);
		}
		active[0].removeAllItems();
		active[0].addItem("Human");
		active[0].setSelectedItem("Human");
		active[0].setEnabled(false);

		JButton done = new JButton("Done");
		done.setFont(GUI.myFont);
		JPanel tbot = new JPanel();
		tbot.setLayout(new BorderLayout());
		tbot.add(done, BorderLayout.EAST);	
		teamPanel.add(tbot, BorderLayout.SOUTH);
		done.addActionListener(new ActionListener() {
			

			@Override
			public void actionPerformed(ActionEvent arg0) {
				startTheGame(mapSeed, gameSeed);
			}
		});
		
		tabs.addTab("Teams", new JScrollPane(teamPanel));
		
		
		tabs.addChangeListener(this);
		tabs.setFont(GUI.myFont);
		frame.add(tabs);
		frame.setSize(500, 800);
		frame.setLocation(600, 20);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		
    }
    
    
	protected void startTheGame(long mapSeed, long gameSeed) {

		ArrayList<String> playerNames = new ArrayList<>();
		ArrayList<Color> cols = new ArrayList<>();
		ArrayList<Boolean> bools = new ArrayList<>();
		ArrayList<Player> players = new ArrayList<Player>();
		for(int i = 0; i < ABSOLUTEMAXTEAMS; i++){
			if(!active[i].getSelectedItem().equals("Closed")){
				playerNames.add(names[i].getText());
				cols.add(colourLabels[i].getBackground());
				bools.add(active[i].getSelectedItem().equals("Human"));
			}
		}
		
		System.out.println("Starting game...");
		frame.setVisible(false);
		GameType.start(mapWidth, mapHeight, hillyness.getValue(), sizeSlider.getValue(), playerNames, cols, bools, mapSeed, gameSeed, (String) gameType.getSelectedItem());
		
		
	}

	private void changeTo(int tab) {
		if(tab > 0){
			changeTo(tab -1);
		}
		
		switch (tab) {
		case 0:
			break;
		case 1:
			mapHeight = height.getValue();
			mapWidth = width.getValue();
			if(mapWidth < mapHeight){
				largestArmy = (int)Math.floor(Math.pow(((mapWidth - buffer) / 2.0) - 1, 2) / 2);
			}else{
				largestArmy = (int)Math.floor(Math.pow(((mapHeight - buffer) / 2.0) - 1, 2) / 2);
			}
			largestArmy = Math.min(largestArmy, ARMYMAX);
			sizeSlider.setMaximum(largestArmy);
			sizeSlider.setMajorTickSpacing(largestArmy > 5 ? 2 : 1);
			
			
			break;
		case 2:
			Zone.size = (int)(Math.ceil(Math.sqrt(2 * sizeSlider.getValue())) + 1);
			horZone = (int)((mapWidth + buffer) / (float)(Zone.size + buffer));
			verZone = (int)((mapHeight + buffer) / (float)(Zone.size + buffer));
			
			int maxTeams;
			
			if(horZone == 2 || verZone == 2){
				maxTeams = 2;
			}else{
				maxTeams = (horZone - 1) * (verZone - 1);
				maxTeams = Math.min(ABSOLUTEMAXTEAMS, maxTeams);
			}
			
			for(int i = 0; i < maxTeams; i++){
				names[i].setEnabled(true);
				active[i].setSelectedItem("AI");
				active[i].setEnabled(true);
				
			}
			
			
			for(int i = maxTeams; i < ABSOLUTEMAXTEAMS; i++){
				names[i].setEnabled(false);
				active[i].setSelectedItem("Closed");
				active[i].setEnabled(false);
			}
			
			
			break;

		default:
			assert false;
			break;
		}
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		tabs.setSelectedIndex(tabs.getSelectedIndex() + 1);
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
	public void mouseClicked(MouseEvent e) {
		for(int i = 0; i < colourLabels.length; i++){
			if(colourLabels[i].equals(e.getSource())){
				colourLabels[i].setBackground( JColorChooser.showDialog(frame, "Choose colour for " + names[i].getText(), colourLabels[i].getBackground()));
				return;
			}
		}
		
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		changeTo(tabs.getSelectedIndex());
		
	}
	
	

}
