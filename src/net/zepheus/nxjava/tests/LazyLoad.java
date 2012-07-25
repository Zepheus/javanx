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

import java.util.EnumSet;

import net.zepheus.nxjava.*;

public class LazyLoad {
	private static final int COUNT = 10;
	private static final EnumSet<NXReadMode> PROPERTIES = EnumSet.of(NXReadMode.LOW_MEMORY);
	
	public static void main(String[] args) {
		try {
			System.in.read();
			long averageTotal = 0;
			for(int i = 0; i < COUNT; i++) {
				long start = System.currentTimeMillis();
				NXFile file = new NXFile("D:\\Games\\MapleBeta\\DataOnly.nx", PROPERTIES);
				System.out.println(file.getString(50 + i));
				long time = System.currentTimeMillis() - start;
				averageTotal += time;
				file.close();
				System.gc();
			}
			long average = averageTotal / COUNT;
			System.out.println("Loading took " + average + "ms for " + COUNT + " tests.");
			System.in.read();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
}
