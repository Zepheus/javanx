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

package net.zepheus.nxjava;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class NXNode implements Iterable<NXNode> {

	public static final int SIZE = 20;
	private static final Iterator<NXNode> EMPTY_ITERATOR = new EmptyIterator<NXNode>();
	
	protected String name;
	protected NXNode parent;
	protected NXFile file;
	private int childCount;
	private int firstChildId;
	
	private Map<String, NXNode> children;
	
	public NXNode(String name, NXNode parent, NXFile file, int childCount) {
		this.name = name;
		this.parent = parent;
		this.file = file;
		this.childCount = childCount;
	}
	
	public abstract Object getValue();

	public NXNode getChild(String name) {
		// it seems not calling hasChild here speeds things up
		if(childCount == 0)
			return null;
		else if(children == null)
			parseChildren();
		
		return children.get(name);
	}
	
	private void parseChildren() {
		children = new HashMap<String, NXNode>();
		
		file.lock();
		try {
			SeekableLittleEndianAccessor slea = file.getNodeStream(firstChildId);
			for(int i = 0; i < childCount; i++) {
				NXNode node = NXNodeParser.parse(slea, this, file);
				children.put(node.getName(), node);
			}
		} finally { file.unlock(); }
	}
	
	public boolean hasChild(String name) {
		if(childCount == 0)
			return false;
		else if(children == null)
			parseChildren();
		
		return children.containsKey(name);
	}

	@Override
	public Iterator<NXNode> iterator() {
		if(childCount == 0)
			return EMPTY_ITERATOR;
		
		if(children == null)
			parseChildren();
		return children.values().iterator();
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	//TODO: remove this hack in PKG4
	public void setFirstChildId(int id){
		this.firstChildId = id;
	}

	public static int getSize() {
		return SIZE;
	}

	public String getName() {
		return name;
	}

	public NXNode getParent() {
		return parent;
	}

	public NXFile getFile() {
		return file;
	}

	public int getChildCount() {
		return childCount;
	}
}
