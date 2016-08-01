package gui;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import core.Cell;



@SuppressWarnings("serial")
public class MiniMap extends JPanel implements ActionListener{
	
	
	private LocalPlayer player;
	
	private int mode = 0;
	private String[] modes = {"Elevation", "Terrain"};
	private JButton but;
	private double bigXoff;
	private double bigYoff;
	private int bigWidth;
	private int bigHeight;
	
	private int pxHeight;
	private int pxWidth;
	private int xoff;
	private int yoff;
	private double cellRatio;

	
	private static final int MAXHEIGHT = 300;
	
	
	public MiniMap(LocalPlayer player, double r, JButton but){
		super();
		assert player != null;
		this.player = player;
		cellRatio = r;
		setBackground(Color.white);
		but.setText(modes[mode]);
		this.but = but;
		
	}
	

	@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		int width = 3 + player.getFarRight() - player.getFarLeft();
		int height = 3 + player.getFarDown() - player.getFarUp();
		double mapRatio = height / (double)width;
		int mmWidth = getWidth() - 10;
		int mmHeight = (int) (mmWidth * mapRatio);
		
		if(mmHeight > MAXHEIGHT){
			mmHeight = MAXHEIGHT;
			mmWidth = (int)(mmHeight / mapRatio);
		}
		setPreferredSize(new Dimension(getWidth(), mmHeight + 10));
		setMaximumSize(new Dimension(getWidth(), mmHeight + 10));
		if(width != 0 && height != 0){
			
			pxWidth = mmWidth / width;
			pxHeight = (int) (cellRatio * pxWidth);
			xoff = 5 + (mmWidth - (pxWidth * width)) / 2;
			yoff = 5 + (mmHeight - (pxHeight * height)) / 2;
			for(int x = Math.max(0, player.getFarLeft() - 1); x < Math.min(player.getMapWidth(), player.getFarRight() + 2); x++){
				for(int y = Math.max(0, player.getFarUp() - 1); y < Math.min(player.getMapHeight(), player.getFarDown() + 2); y++){
					Cell cell = player.getCell(x, y);
					if(cell != null){
						if(modes[mode].equals("Terrain")){
							g.drawImage(cell.getTerrain().getBaseImage(), toPixels(x, true), toPixels(y, false), pxWidth, pxHeight, null);
						}
						if(modes[mode].equals("Elevation")){
							g.setColor(new Color(0, 0, 0, 1 - (cell.getElevation() /(float) player.getMapHighest())));
							g.fillRect( toPixels(x, true), toPixels(y, false), pxWidth, pxHeight);
						}
						
						if(cell.isViewed(player) && cell.getUnit() != null){
							g.setColor(cell.getUnit().getOwner().getColour());
							g.fillRect(toPixels(x, true), toPixels(y, false), pxWidth, pxHeight);
							
						}
					
					}else{
						g.setColor(Color.black);
						g.fillRect( toPixels(x, true), toPixels(y, false), pxWidth, pxHeight);
					}
					
					
					
				}
			}
			g.setColor(Color.lightGray);
			g.drawRect(toPixels(bigXoff, true), toPixels(bigYoff, false), (bigWidth + 1) * pxWidth, (bigHeight + 1) * pxHeight);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		mode = (mode + 1) % modes.length;
		but.setText(modes[mode]);
	}

	public void updateOffset(double xo, double yo, double width, double height) {
		bigXoff =  xo;
		bigYoff = yo;
		bigWidth = (int) width;
		bigHeight = (int) height;
		repaint();
	}
	
	private int toPixels(double m, boolean x){
		if(x){
			return (int)Math.round(xoff + (1 + m - player.getFarLeft()) * pxWidth);
		}
		return (int)Math.round(yoff + (1 + m - player.getFarUp()) * pxHeight);
	}

	public int toCell(int m, boolean x) {
		if(x){
			return (m - xoff) / pxWidth + player.getFarLeft() - 1;
		}
		return (m - yoff) / pxHeight + player.getFarUp() - 1;
	}


	public void setPlayer(LocalPlayer player) {
		this.player = player;
		
	}


	

	
}
