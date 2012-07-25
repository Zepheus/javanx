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

import java.io.FileNotFoundException;
import java.io.IOException;

import net.zepheus.nxjava.NXFile;
import net.zepheus.nxjava.NXNode;

public class Benchmark {
	
	private static final int COUNT = 10;
	private static final String FILE_PATH = "D:\\Games\\MapleBeta\\DataOnly.nx";
	
	public static void main(String[] args) {
		System.out.println("Press [RETURN] to start benchmark.");
		try {
				System.in.read();
				NXFile file = loadTest(FILE_PATH);
				accessTest(file, 1000000);
				recurseTest(file);
				memoryTest();
				System.out.println("Finished. Press [RETURN] to end.");
				System.in.read();
				file.close();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
	
	private static NXFile loadTest(String path) throws FileNotFoundException, IOException {
		long start = System.currentTimeMillis();
		NXFile file = new NXFile(path);
		long time = System.currentTimeMillis() - start;
		System.out.println("Loading file & string table took " + time + "ms.");
		return file;
	}
	
	private static void memoryTest() 
	{
		System.gc();
		long usage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		long MB = usage / (1024 * 1024);
		System.out.println("Memory usage after testing: " + MB + "MB");
	}
	
	private static void accessTest(NXFile file, int times) {
		System.gc();
		long start = System.currentTimeMillis();
		for(int i = 0; i < times * COUNT; i++)
		{
			//NXNode node = file.resolvePath("Effect/BasicEff.img/LevelUp/5/origin");
			NXNode node = file.resolvePath("Effect", "BasicEff.img", "LevelUp", "5", "origin");
		}
		long time = System.currentTimeMillis() - start;
		long average = time / COUNT;
		System.out.println("Access took " + average +"ms based on " + COUNT + " results.");
	}
	
	private static void recurseTest(NXFile file) {
		System.gc();
		long start = System.currentTimeMillis();
		for(int i = 0; i < COUNT; i++)
		{
			recurse(file.getRoot());
		}
		long time = System.currentTimeMillis() - start;
		long average = time / COUNT;
		System.out.println("Full recursion took " + average +"ms based on " + COUNT + " results.");
	}
	
	private static void recurse(NXNode node) {
		for(NXNode child : node)
			recurse(child);
	}
}
