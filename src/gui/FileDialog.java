package gui;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
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
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.sun.xml.internal.ws.api.Component;

import core.GameState;
import core.GameType;
import core.Panel;


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
	private GameType gt;
	private boolean success = false;
	
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
		
		fileList.setFont(Panel.myFont);
		text.setFont(Panel.myFont);
		done.setFont(Panel.myFont);
		fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		filePanel.add(fileList, BorderLayout.CENTER);
		
		bot.add(text, BorderLayout.CENTER);
		
		JPanel botRight = new JPanel();
		botRight.add(done);
		
		JButton cancel = new JButton("Cancel");
		botRight.add(cancel);
		cancel.setFont(Panel.myFont);
		
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
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
				out.writeObject(gt);
				out.close();
				closeDialog();
				success = true;
			} catch (IOException e) {
				e.printStackTrace();
				success = false;
				JOptionPane.showMessageDialog(this, "There was an error saving the file", "I/O Error", JOptionPane.ERROR_MESSAGE);
			}
			
			JOptionPane.showMessageDialog(this, "Game saved", "Game saved", JOptionPane.INFORMATION_MESSAGE);
		}else{
			try {
				if(text.getText().trim().isEmpty()){
					JOptionPane.showMessageDialog(this, "You must choose a file", "No file", JOptionPane.ERROR_MESSAGE);
					return;
				}
				File f = new File(fileRoot + locations.get(thing) + text.getText() + ".asg");
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
				gt = (GameType) in.readObject();
				in.close();
				closeDialog();
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
	


	@SuppressWarnings("hiding")
	private void showDialog(Thing thing, Mode mode) {
		if(mode == Mode.SAVE && !gt.getGameState().getCurrentPlayer().isHuman()){
			JOptionPane.showMessageDialog(this, "You can only save the game on your turn", "Wait for your turn", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		
		this.thing = thing;
		this.mode = mode;
		File loc = new File(fileRoot + locations.get(thing));
		if(loc.list() == null){
			loc.mkdirs();
		}
		for(String el : loc.list()){
			String[] parts = el.split("\\.");
			if(parts[1].equals("asg")){
				model.addElement(parts[0]);
			}
		}
		if(mode == Mode.SAVE){
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
	
	public static boolean showSaveGameDialog(Window holder, GameType gt){
		FileDialog fd = new FileDialog(holder);
		fd.gt = gt;
		fd.showDialog(Thing.GAME, Mode.SAVE);
		return fd.success;
	}


	public static GameType showLoadGameDialog(Window holder) {
		FileDialog fd = new FileDialog(holder);
		fd.showDialog(Thing.GAME, Mode.LOAD);
		return fd.gt;
	}
	
	

}
