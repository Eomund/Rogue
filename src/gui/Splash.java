package gui;


import helpers.Config;
import helpers.Player;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import core.GameType;

@SuppressWarnings("serial")
public class Splash extends JFrame{
	
	private int mapwidth;
	private int mapheight;
	private int hillyness;
	private int armySize;
	private ArrayList<Player> players;
	private String gtName;

	public Splash(final long mapSeed, final long gameSeed){
		JButton newGame = new JButton("New Game");
		newGame.setFont(GUI.myFont);
		setLayout(new BorderLayout());
		JPanel buttons = new JPanel();
		buttons.add(newGame);
		newGame.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				new Setup(mapSeed, gameSeed);
			}
		});
		
		JButton quick = new JButton("Quick start");
		quick.setFont(GUI.myFont);
		buttons.add(quick);
		
		
		if(!loadQuick()){
			quick.setEnabled(false);
		}
		
		quick.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				GameType.start(mapwidth, mapheight, hillyness, armySize, players, mapSeed, gameSeed, gtName);
				setVisible(false);
			}
		});
		
		add(buttons, BorderLayout.SOUTH);
		setSize(500, 400);
		setLocation(600, 20);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
    @SuppressWarnings("unchecked")
	private boolean loadQuick() {
    	File f = new File(FileDialog.fileRoot);
		if(!f.exists()){
			f.mkdirs();
		}
		f = new File(FileDialog.fileRoot + "/quick");
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
			
			mapwidth = in.readInt();
			mapheight = in.readInt();
			hillyness = in.readInt();
			armySize = in.readInt();
			players = (ArrayList<Player>) in.readObject();
			gtName = in.readUTF();
			
			in.close();
			
		} catch (IOException | ClassNotFoundException e) {
			return false;
		}
		
		return true;
	}

	public static void main(String[] args) {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
        final long mapSeed = args.length > 0 ? Long.parseLong(args[0]) : System.nanoTime();
        Config.loadConfigValues();
        final long gameSeed = args.length > 1 ? Long.parseLong(args[1]) : System.nanoTime();
        java.awt.EventQueue.invokeLater(new Runnable() { 
        @Override
		public void run() {
        	new Splash(mapSeed, gameSeed);
        }});
    }
	
}
