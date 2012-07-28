package net.zepheus.nxjava;

import java.util.Iterator;

public class EmptyIterator<T> implements Iterator<T>
{
	public T next()
	{
		return null;
	}

	public boolean hasNext()
	{
		return false;
	}

	public void remove()
	{
		throw new UnsupportedOperationException();
	}
}
