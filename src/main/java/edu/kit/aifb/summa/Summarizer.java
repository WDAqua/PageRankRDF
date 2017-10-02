package edu.kit.aifb.summa;

import java.util.LinkedList;
import edu.kit.aifb.summa.model.TripleMeta;


/**
 * This interface needs to be implemented by each entity summarization system.
 *
 */
public interface Summarizer {

	public LinkedList<TripleMeta> summarize(java.net.URI uri, String[] fixedProperties,
			Integer topK, Integer maxHops, String language) ;
	
}
