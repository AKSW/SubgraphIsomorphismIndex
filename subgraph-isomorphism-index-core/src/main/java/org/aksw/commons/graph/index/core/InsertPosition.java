package org.aksw.commons.graph.index.core;

import java.util.Set;

import com.google.common.collect.BiMap;

public class InsertPosition<K, G, N, T> {
    protected IndexNode<K, G, N, T> node;
    protected G residualQueryGraph;
    //protected G residualViewGraph;

    protected Set<T> residualQueryGraphTags;
    //protected Set<Object> residualViewGraphTags;

    protected BiMap<N, N> iso;
    protected BiMap<N, N> latestIsoAB;

    // The cumulative iso up to the insert node
    protected BiMap<N, N> nodeBaseIso;
    
    public InsertPosition(
            IndexNode<K, G, N, T> node,
//            Graph residualViewGraph,
            G residualQueryGraph,
            Set<T> residualQueryGraphTags,
            BiMap<N, N> nodeBaseIso,
            BiMap<N, N> iso,
            BiMap<N, N> latestIsoAB) {
        super();
        this.node = node;
//        this.residualViewGraph = residualViewGraph;
        this.residualQueryGraph = residualQueryGraph;
        this.residualQueryGraphTags = residualQueryGraphTags;
        this.nodeBaseIso = nodeBaseIso;
        this.iso = iso;
        this.latestIsoAB = latestIsoAB;
    }

    public IndexNode<K, G, N, T> getNode() {
        return node;
    }

//    public Graph getResidualViewGraph() {
//        return residualViewGraph;
//    }

    public G getResidualQueryGraph() {
        return residualQueryGraph;
    }

    public Set<T> getResidualQueryGraphTags() {
        return residualQueryGraphTags;
    }

    public BiMap<N, N> getNodeBaseIso() {
    	return nodeBaseIso;
    }
    
    public BiMap<N, N> getIso() {
        return iso;
    }


    public BiMap<N, N> getLatestIsoAB() {
        return latestIsoAB;
    }

    @Override
    public String toString() {
        return "InsertPosition [node=" + node + ", residualQueryGraph=" + residualQueryGraph + ", iso=" + iso + "]";
    }
}