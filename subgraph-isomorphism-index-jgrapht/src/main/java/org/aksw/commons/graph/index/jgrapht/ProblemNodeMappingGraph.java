package org.aksw.commons.graph.index.jgrapht;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.aksw.combinatorics.solvers.ProblemMappingKPermutationsOfN;
import org.aksw.combinatorics.solvers.ProblemNeighborhoodAware;
import org.aksw.commons.graph.index.core.MapUtils;
import org.jgrapht.Graph;
import org.jgrapht.GraphMapping;
import org.jgrapht.alg.isomorphism.IsomorphicGraphMapping;
import org.jgrapht.alg.isomorphism.VF2SubgraphIsomorphismInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

public class ProblemNodeMappingGraph<V, E, G extends Graph<V, E>, T>
    implements ProblemNeighborhoodAware<BiMap<V, V>, T>
    //extends ProblemMappingVarsBase<DirectedGraph<Node, Triple>, DirectedGraph<Node, Triple>, Var, Var>
{
    private static final Logger logger = LoggerFactory.getLogger(ProblemNodeMappingGraph.class);


    protected BiMap<? extends V, ? extends V> baseSolution;
    protected G viewGraph;
    protected G queryGraph;

    protected Function<BiMap<? extends V, ? extends V>, Comparator<V>> nodeComparatorFactory;
    protected Function<BiMap<? extends V, ? extends V>, Comparator<E>> edgeComparatorFactory;

    protected boolean skipIncompatibleMappings;

    protected transient Comparator<V> nodeComparator;
    protected transient Comparator<E> edgeComparator;
    protected transient VF2SubgraphIsomorphismInspector<V, E> inspector;

//    public ProblemNodeMappingGraph(
//            BiMap<V, V> baseSolution,
//            G viewGraph,
//            G queryGraph,
//            Comparator<V> nodeComparator,
//            Comparator<E> edgeComparator,
//            boolean skipIncompatibleMappings) {
//        super();
//        this.baseSolution = baseSolution;
//        this.viewGraph = viewGraph;
//        this.queryGraph = queryGraph;
//
//        this.nodeComparator = nodeComparator;
//        this.edgeComparator = edgeComparator;
//
//        this.skipIncompatibleMappings = skipIncompatibleMappings;
//        inspector = new VF2SubgraphIsomorphismInspector<>(queryGraph, viewGraph, nodeComparator, edgeComparator, true);
//    }

    public ProblemNodeMappingGraph(
            BiMap<? extends V, ? extends V> baseSolution,
            G viewGraph,
            G queryGraph,
            Function<BiMap<? extends V, ? extends V>, Comparator<V>> nodeComparatorFactory,
            Function<BiMap<? extends V, ? extends V>, Comparator<E>> edgeComparatorFactory)
    {
        this(
            baseSolution, viewGraph, queryGraph,
            nodeComparatorFactory, edgeComparatorFactory,
            true);
    }

    public ProblemNodeMappingGraph(
            BiMap<? extends V, ? extends V> baseSolution,
            G viewGraph,
            G queryGraph,
            Function<BiMap<? extends V, ? extends V>, Comparator<V>> nodeComparatorFactory,
            Function<BiMap<? extends V, ? extends V>, Comparator<E>> edgeComparatorFactory,
            boolean skipIncompatibleMappings) {
        super();
        this.baseSolution = baseSolution;
        this.viewGraph = viewGraph;
        this.queryGraph = queryGraph;

        this.nodeComparatorFactory = nodeComparatorFactory;
        this.edgeComparatorFactory = edgeComparatorFactory;

        this.nodeComparator = nodeComparatorFactory.apply(baseSolution);
        this.edgeComparator = edgeComparatorFactory.apply(baseSolution);

        this.skipIncompatibleMappings = skipIncompatibleMappings;
        inspector = new VF2SubgraphIsomorphismInspector<>(queryGraph, viewGraph, nodeComparator, edgeComparator, true);
//        System.out.println("New inspector for viewGraphHash: " + viewGraph.hashCode() + ", queryGraphHash: " + queryGraph.hashCode());

        //super(as, bs, baseSolution);
    }

    @Override
    public Stream<BiMap<V, V>> generateSolutions() {
//        VF2SubgraphIsomorphismInspector<V, E> inspector;
//        inspector = new VF2SubgraphIsomorphismInspector<>(queryGraph, viewGraph, nodeComparator, edgeComparator, true);

        Iterator<GraphMapping<V, E>> it = inspector.getMappings();

        // TODO WHY DOES THIS TRULY STREAMING VERSION FAIL WITH ODD DUPLICATE ITEMS AND NPE???
//      Stream<GraphMapping<V, E>> baseStream = Streams.stream(it);

        List<GraphMapping<V, E>> tmp = Lists.newArrayList(it);
        //System.out.println("GraphMappings " + tmp.size() + ": " + tmp);
        Stream<GraphMapping<V, E>> baseStream = tmp.stream();


        Stream<BiMap<V, V>> result = baseStream//Streams.stream(it)
//            .peek(x -> System.out.println("Seen: " + x))
//            .peek(x -> System.out.println("viewGraphHash: " + viewGraph.hashCode() + ", queryGraphHash: " + queryGraph.hashCode()))
//            .distinct()
            .map(m -> (IsomorphicGraphMapping<V, E>)m)
            .map(m -> {
                BiMap<V, V> nodeMap = HashBiMap.create();//new HashMap<>();

                // Add the base solution
                nodeMap.putAll(baseSolution);

                //System.out.println("Base Mappings:");
                //nodeMap.forEach((k, v) -> System.out.println("  " + k + " -> " + v));

                //System.out.println("New Mappings:");
                for(V bNode : queryGraph.vertexSet()) {
                    if(m.hasVertexCorrespondence(bNode)) {
                        V aNode = m.getVertexCorrespondence(bNode, true);
                        //System.out.println("  " + aNode + " -> " + bNode);
                        try {
                            nodeMap.put(aNode, bNode);
                        } catch(IllegalArgumentException e) {
                            if(skipIncompatibleMappings) {
                                // If the mapping is inconsistent with the baseMapping, skip it
                                // but log a warnign
                                //logger.warn("Skipping incompatible mapping");
                                nodeMap = null;
                                break;
                            } else {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
                //System.out.println("Created node map: " + nodeMap);
                return nodeMap;
            })
            .filter(x -> x != null)
			.filter(iso -> MapUtils.isCompatible(baseSolution, iso));


        return result;
    }

    @Override
    public Collection<? extends ProblemNeighborhoodAware<BiMap<V, V>, T>> refine(BiMap<V, V> partialSolution) {
        BiMap<V, V> newBaseSolution = HashBiMap.create(baseSolution.size() + partialSolution.size());
        newBaseSolution.putAll(baseSolution);
        newBaseSolution.putAll(partialSolution);

        return Collections.singleton(new ProblemNodeMappingGraph<>(newBaseSolution, viewGraph, queryGraph, nodeComparatorFactory, edgeComparatorFactory, skipIncompatibleMappings));
    }

    @Override
    public boolean isEmpty() {
//        VF2SubgraphIsomorphismInspector<V, E> inspector;
//        inspector = new VF2SubgraphIsomorphismInspector<>(queryGraph, viewGraph, nodeComparator, edgeComparator, true);

        boolean result = inspector.isomorphismExists();
        return result;
    }

    @Override
    public long getEstimatedCost() {
        // n over k based on the number of edges
        // The more the graphs differ in size, the higher the cost
        int n = queryGraph.edgeSet().size();
        int k = viewGraph.edgeSet().size();

        long result = ProblemMappingKPermutationsOfN.kCombinationCount(n, k);
//        System.out.println("estimated cost: " + result);
        return result;
    }

    @Override
    public Collection<T> getSourceNeighbourhood() {
        return null;
    }

}
