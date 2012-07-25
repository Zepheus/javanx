/**
 * nxjava: a library for loading the NX file format
 * Copyright (C) 2012 Cedric Van Goethem
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.zepheus.nxjava.tests;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import net.zepheus.nxjava.NXCanvasNode;
import net.zepheus.nxjava.NXEmptyNode;
import net.zepheus.nxjava.NXFile;
import net.zepheus.nxjava.NXNode;

public class NXAnimation extends JPanel implements ActionListener {
	
	private static final String FILE_PATH = "D:\\Games\\MapleBeta\\Data.nx";
	private static final String[] ANIMATION_PATH = { "Mob", "8800000.img", "attack1" };

	private NXFile file;
	private BufferedImage[] sprites;
	private int index;
	
	private Timer timer;
	
	public NXAnimation(NXFile file, String[] animationPath) {
		this.file = file;
		loadSprites(animationPath);
		
		timer = new Timer(100, this);
		timer.start();
	}
	
	private void loadSprites(String[] animationPath) {
		NXNode node = file.resolvePath(animationPath);
		if(node instanceof NXEmptyNode) {
			List<BufferedImage> images = new ArrayList<BufferedImage>();
			for(NXNode sprite : node) {
				if(sprite instanceof NXCanvasNode) {
					NXCanvasNode canvas = (NXCanvasNode)sprite;
					images.add(canvas.getImage());
				}
			}
			sprites = new BufferedImage[images.size()];
			for(int i = 0; i < sprites.length; i++) {
				sprites[i] = images.get(i);
			}
			
			System.out.println("Loaded " + sprites.length + " sprites.");
			this.setPreferredSize(new Dimension(sprites[0].getWidth(), sprites[0].getHeight()));
		} else throw new RuntimeException("Animations should be located in parent folders.");
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(sprites[index], 0, 0, this);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		index++;
		if(index == sprites.length)
			index = 0;
		repaint();
	}
	
	public static void main(String[] args) {
		try {
			NXFile file = new NXFile(FILE_PATH);
			JFrame frame = new JFrame("NX Animation Test: " + join('/', ANIMATION_PATH));
			JPanel panel = new JPanel();
			panel.add(new NXAnimation(file, ANIMATION_PATH));
			frame.setContentPane(panel);
			
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.pack();
			frame.setVisible(true);
		} catch (Exception ex) {
			System.err.println("Error loading file: " + ex.getMessage()); 
		}
	}
	
	private static String join(char delimiter, String... args) {
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i < args.length - 1; i++) {
			builder.append(args[i] + delimiter);
		}
		builder.append(args[args.length - 1]);
		return builder.toString();
	}
}
