package fr.labri.harmony.analysis.metrics.compute;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraDistance;
import fr.labri.harmony.analysis.metrics.ComputeMetrics;
import fr.labri.harmony.analysis.metrics.ComputeMetricsManager;
import fr.labri.harmony.analysis.metrics.graph.Edge;
import fr.labri.harmony.analysis.metrics.graph.Graph;
import fr.labri.harmony.analysis.metrics.graph.Vertex;

/**
 * Implementation of the network metrics set presented in : [1] T. Zimmermann
 * and N. Nagappan. Predicting defects using network analysis on dependency
 * graphs. In Proceedings of the 30th international conference on Software
 * engineering, ICSE ’08, New York, NY, USA, 2008. ACM.
 * 
 * 
 * @author Amine Raji
 * 
 */

public class Network extends ComputeMetricsManager {

	private ComputeMethodDependencies methodDependencies;
	private Graph internalMethods;
	private Graph allMethods;

	private ComputeClassDependencies classDependencies;
	private Graph allClasses;

	public Network(Collection<String> elementsToAnalyze) {
		super(new HashSet<ComputeMetrics>(), elementsToAnalyze);

		methodDependencies = new ComputeMethodDependencies();
		computeMetrics.add(methodDependencies);

		classDependencies = new ComputeClassDependencies();
		computeMetrics.add(classDependencies);

	}

	@Override
	public void prepareMetrics() {

		internalMethods = methodDependencies.getGraph().internalElements();
		allMethods = methodDependencies.getGraph().copy();

		computeClosinessDijkstra(allMethods);
		computeFanin();
		computeFanout();

		allClasses = classDependencies.getGraph().copy();
		computeDependecyMetrics(allClasses);

	}

	/**
	 * Compute class dependency metrics: number of sub-classed classes,
	 * implemented interfaces, number of dependencies (references)
	 * 
	 * @param the
	 *            global dependency graph of all classes in the project
	 */
	private void computeDependecyMetrics(Graph dg) {
		int nbExtends = 0;
		int nbImplements = 0;
		int nbDependencies = 0;
		for (Edge e : dg.getEdges()) {
			switch (e.getKind()) {
			case IMP:
				nbImplements++;
			case INH:
				nbExtends++;
			case REF:
				nbDependencies++;
			default:
				;
			}

		}
		metrics.addMetric("nbExtends", Integer.toString(nbExtends));
		metrics.addMetric("nbImplements", Integer.toString(nbImplements));
		metrics.addMetric("nbDependencies", Integer.toString(nbDependencies));

	}

	private void computeClosinessDijkstra(Graph dg) {

		DijkstraDistance<Vertex, Edge> dsp = new DijkstraDistance<>(dg);

		int sum = 0;
		for (Vertex v : dg.getVertices()) {
			Map<Vertex, Number> m = dsp.getDistanceMap(v);
			for (Number n : m.values())
				sum += n.intValue();
		}
		metrics.addMetric("TotalClosiness", Integer.toString(sum));
	}

	private void computeFanout() {
		int totalFanout = 0;
		int maxFanout = 0;
		for (Vertex method : allMethods.getVertices()) {
			totalFanout += allMethods.getSuccessorCount(method);
			// FIXME : explain why getSuccessor and then getPredecessor
			maxFanout = Math.max(maxFanout, allMethods.getPredecessorCount(method));
		}
		metrics.addMetric("TotalFanout", Integer.toString(totalFanout));
		metrics.addMetric("MaxFanout", Integer.toString(maxFanout));
	}

	private void computeFanin() {

		int totalFanin = 0;
		int maxFanin = 0;

		for (Vertex method : internalMethods.getVertices()) {

			totalFanin += internalMethods.getPredecessorCount(method);
			// FIXME : idem
			maxFanin = Math.max(maxFanin, internalMethods.getSuccessorCount(method));
		}
		metrics.addMetric("TotalFanin", Integer.toString(totalFanin));
		metrics.addMetric("MaxFanin", Integer.toString(maxFanin));
	}

}
