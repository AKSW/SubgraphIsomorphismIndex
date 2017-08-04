package org.aksw.commons.graph.index.jena;

import java.util.Comparator;
import java.util.function.Function;

import org.aksw.combinatorics.solvers.ProblemNeighborhoodAware;
import org.aksw.commons.graph.index.core.IsoMatcher;
import org.aksw.commons.graph.index.jgrapht.ProblemNodeMappingGraph;
import org.jgrapht.Graph;

import com.google.common.collect.BiMap;

class IsoMatcherImpl<V, E, G extends Graph<V, E>>
implements IsoMatcher<G, V>
{
protected Function<BiMap<V, V>, Comparator<V>> createVertexComparator;
protected Function<BiMap<V, V>, Comparator<E>> createEdgeComparator;

public IsoMatcherImpl(
        Function<BiMap<V, V>, Comparator<V>> createVertexComparator,
        Function<BiMap<V, V>, Comparator<E>> createEdgeComparator) {
    super();
    this.createVertexComparator = createVertexComparator;
    this.createEdgeComparator = createEdgeComparator;
}

public ProblemNeighborhoodAware<BiMap<V, V>, V> toProblem(BiMap<V, V> baseIso, G viewGraph, G insertGraph) {
    ProblemNeighborhoodAware<BiMap<V, V>, V> result = new ProblemNodeMappingGraph<V, E, G, V>(
            baseIso, viewGraph, insertGraph,
            createVertexComparator, createEdgeComparator);

    //Stream<BiMap<V, V>> result = tmp.generateSolutions();

    return result;
}


@Override
public Iterable<BiMap<V, V>> match(BiMap<V, V> baseIso, G viewGraph, G insertGraph) {
    ProblemNeighborhoodAware<BiMap<V, V>, V> problem = toProblem(baseIso, viewGraph, insertGraph);
    Iterable<BiMap<V, V>> result = () -> problem.generateSolutions().iterator();

    return result;
}

}