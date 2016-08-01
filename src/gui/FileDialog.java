package gui;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import core.GameType;
import core.Unit;


@SuppressWarnings("serial")
public class FileDialog extends JDialog {
	
	
	private JTextField text = new JTextField();
	private JButton done = new JButton();
	private DefaultListModel<String> model = new DefaultListModel<>();
	private JList<String> fileList = new JList<>(model);
	public static String fileRoot = "/saves/";
	
	private static HashMap<Thing, String> locations = new HashMap<>();
	
	public enum Thing {GAME, TERRAIN, ARMY};
	public enum Mode {SAVE, LOAD};
	private Window frame;
	private Thing thing;
	private Mode mode;
	private boolean success = false;
	private LocalPlayer player;
	private GUI oldGUI;
	private Timer time;
	
	static{
		locations.put(Thing.GAME, "/games/");
		locations.put(Thing.TERRAIN, "/maps/");
		locations.put(Thing.TERRAIN, "/armies/");
	}
	
	public FileDialog(Window frame) {
		super(frame, Dialog.ModalityType.DOCUMENT_MODAL);
		this.frame = frame;
		setSize(500, 300);
		setPreferredSize(new Dimension(500, 300));
		
		JPanel filePanel = new JPanel();
		filePanel.setBorder(new EmptyBorder(20, 20, 20, 20));
		filePanel.setLayout(new BorderLayout());
		
		JPanel bot = new JPanel();
		bot.setBorder(new EmptyBorder(20, 0, 20, 0));
		bot.setLayout(new BorderLayout());
		
		fileList.setFont(GUI.myFont);
		text.setFont(GUI.myFont);
		done.setFont(GUI.myFont);
		fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		filePanel.add(fileList, BorderLayout.CENTER);
		
		bot.add(text, BorderLayout.CENTER);
		
		JPanel botRight = new JPanel();
		botRight.add(done);
		
		JButton cancel = new JButton("Cancel");
		botRight.add(cancel);
		cancel.setFont(GUI.myFont);
		
		done.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				done();
				
			}
		});
		
		cancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				closeDialog();
			}
		});
		
		fileList.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(e.getFirstIndex() < model.getSize()){
					text.setText(model.getElementAt(e.getFirstIndex()));
				}
			}
		});
		
		bot.add(botRight, BorderLayout.EAST);
		
		filePanel.add(bot, BorderLayout.SOUTH);
		
		add(filePanel);
		
	}


	protected void done() {

		if(mode == Mode.SAVE){
			try {
				if(text.getText().trim().isEmpty()){
					JOptionPane.showMessageDialog(this, "You must enter a filename", "No filename", JOptionPane.ERROR_MESSAGE);
					return;
				}
				File f = new File(fileRoot + locations.get(thing) + text.getText() + ".asg");
				
				if(!f.createNewFile()){
					if(JOptionPane.showConfirmDialog(this, "Do you want to overwrite that saved game?", "File exists!", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION){
						return;
					}
				}
				player.saveGame(new ObjectOutputStream(new FileOutputStream(f)));
				success = true;
				closeDialog();
			} catch (IOException e) {
				e.printStackTrace();
				success = false;
				JOptionPane.showMessageDialog(this, "There was an error saving the file", "I/O Error", JOptionPane.ERROR_MESSAGE);
			}
			
			JOptionPane.showMessageDialog(this, "Game saved", "Game saved", JOptionPane.INFORMATION_MESSAGE);
		}else{
			try {
				time.stop();
				for(ActionListener list:time.getActionListeners()){
					time.removeActionListener(list);
				}
				if(text.getText().trim().isEmpty()){
					JOptionPane.showMessageDialog(this, "You must choose a file", "No file", JOptionPane.ERROR_MESSAGE);
					return;
				}
				File f = new File(fileRoot + locations.get(thing) + text.getText() + ".asg");
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
				GameType gt = (GameType) in.readObject();
				GameType.load(gt, oldGUI);
				in.close();
				closeDialog();
				frame.setVisible(false);
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "There was an error reading the file", "I/O Error", JOptionPane.ERROR_MESSAGE);
			}catch(ClassNotFoundException e){
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "There was an error reading the file", "I/O Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	
	}


	protected void closeDialog() {
		setVisible(false);
		model.removeAllElements();
	}
	


	private void showDialog(Thing t, Mode m) {
		if(m == Mode.SAVE && !player.getGameCurrentPlayer().isHuman()){
			JOptionPane.showMessageDialog(this, "You can only save the game on your turn", "Wait for your turn", JOptionPane.ERROR_MESSAGE);
			return;
		}
		if(m == Mode.SAVE){
			boolean unitActive = false;
			for(Unit u:player.getUnits()){
				if(u.isActive()){
					unitActive = true;
					return;
				}
			}
			if(unitActive){
				JOptionPane.showMessageDialog(this, "You cannot save while your units are active", "Please wait for your units to stop moving.", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		this.thing = t;
		this.mode = m;
		File loc = new File(fileRoot + locations.get(t));
		if(loc.list() == null){
			loc.mkdirs();
		}
		for(String el : loc.list()){
			String[] parts = el.split("\\.");
			if(parts[1].equals("asg")){
				model.addElement(parts[0]);
			}
		}
		if(m == Mode.SAVE){
			done.setText("Save");
			text.setEditable(true);
			setTitle("Save");
		}else{
			setTitle("Load");
			done.setText("Load");
			text.setEditable(false);
		}
		setLocation(frame.getLocationOnScreen().x + (frame.getWidth() - getWidth()) / 2, frame.getLocationOnScreen().y + (frame.getHeight() - getHeight()) / 2);
		pack();
		text.requestFocusInWindow();
		setVisible(true);
		
	}
	
	public static boolean showSaveGameDialog(Window holder, LocalPlayer player){
		
		FileDialog fd = new FileDialog(holder);
		fd.player = player;
		fd.showDialog(Thing.GAME, Mode.SAVE);
		return fd.success;
	}


	public static void showLoadGameDialog(Window holder, GUI gui, Timer t) {
		FileDialog fd = new FileDialog(holder);
		fd.oldGUI = gui;
		fd.time = t;
		fd.showDialog(Thing.GAME, Mode.LOAD);
	}
	
	

}
