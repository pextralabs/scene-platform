package br.ufes.inf.lprm.situation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class CastSnapshotSet extends LinkedList<CastSnapshot> {

	private static final long serialVersionUID = 1L;
	
	public CastSnapshot getStable() throws NoSuchElementException {		
		if (this.size() == 0) throw new NoSuchElementException();
		if (this.size() == 1) return this.getFirst();
		CastSnapshot  current, next, stable = null;
		Iterator<CastSnapshot> it = this.iterator();
		current = it.next();
		long currentDuration, lastDuration = 0;
		while (it.hasNext()) {
			next = it.next();			
			currentDuration = next.getTimestamp() - current.getTimestamp(); 
			if (currentDuration > lastDuration) stable = next;
			lastDuration = currentDuration;
			current = next;
		}			
		return stable;	
	}

}
