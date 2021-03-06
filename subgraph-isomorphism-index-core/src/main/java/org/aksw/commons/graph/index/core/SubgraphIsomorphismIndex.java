package org.aksw.commons.graph.index.core;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;


/**
 * 
 *
 * @author raven
 *
 * @param <K>
 * @param <G>
 * @param <N>
 */
public interface SubgraphIsomorphismIndex<K, G, N> {

    void removeKey(Object key);

    /**
     * For each key, returns sets of objects each comprising:
     * - the index node, holding the residual index graph
     * - the isomorphism from the residual index graph to the residual query graph
     * - the residual query graph
     *
     *
     *
     * @param queryGraph
     * @return
     */
    //Multimap<K, InsertPosition<K, G, N>> lookup(G queryGraph, boolean exactMatch);

    // Add a new entry, thereby allocating a new key
    // TODO This method should maybe not be part of the core interface
    //K add(G graph);

    /**
     * Insert a graph pattern with a specific key, thereby replacing any existing one having this key already
     *
     * @param key
     * @param graph
     * @return
     */
    K put(K key, G graph);

    // FIXME Maybe this should be removed: This class represents an *index* and not a map
    // This means, that the original graph for a key may no longer exist in its initial form, but is now distributed across
    // the datastructure for the index
    G get(K key);
    
    // Return the set of keys together with the isomorphisms
    //Map<K, Iterable<BiMap<N, N>>> lookupStream(G queryGraph, boolean exactMatch);


    Multimap<K, BiMap<N, N>> lookup(G queryGraph, boolean exactMatch, BiMap<? extends N, ? extends N> baseIso);

    // Convenience default method where the baseIso is omitted and thus considered an empty map
    default Multimap<K, BiMap<N, N>> lookup(G queryGraph, boolean exactMatch) {
    	Multimap<K, BiMap<N, N>> result = lookup(queryGraph, exactMatch, HashBiMap.create());
    	return result;
    }
    
    // FIXME Temporary debug method
    void printTree();


//    Iterable<BiMap<N, N>> match(BiMap<N, N> baseIso, G a, G b); // QueryToJenaGraph.match(baseIso, viewGraph, insertGraph).collect(Collectors.toSet());

}
