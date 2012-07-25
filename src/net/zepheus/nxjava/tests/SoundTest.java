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

import net.zepheus.nxjava.*;

public class SoundTest {
	private static final String DATA_PATH = "D:\\Games\\MapleBeta\\Data.nx";
	private static final String[] SOUND_PATH = { "Sound", "Bgm07.img", "FunnyTimeMaker" };
	
	public static void main(String[] args) {
		try {
			NXFile file = new NXFile(DATA_PATH);
			
			NXMP3Node mp3 = (NXMP3Node)file.resolvePath(SOUND_PATH);
			mp3.play();
			
			System.out.println("To exit the process, press [RETURN].");
			System.in.read();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
}
