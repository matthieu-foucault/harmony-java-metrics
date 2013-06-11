package fr.labri.harmony.analysis.metrics.compute;

import java.util.Hashtable;
import java.util.Map;

import fr.labri.harmony.analysis.metrics.ComputeMetrics;
import fr.labri.harmony.analysis.metrics.graph.EdgeKind;
import fr.labri.harmony.analysis.metrics.graph.Graph;
import fr.labri.harmony.analysis.metrics.graph.Vertex;

public abstract class BuildDependenciesGraph extends ComputeMetrics {

	protected Graph graph;
	protected Map<String, Vertex> elementsHashTable;
	protected String lastElement;

	public BuildDependenciesGraph() {
		graph = new Graph();
		elementsHashTable = new Hashtable<>();
		lastElement = "";
	}
	
	public Graph getGraph() {
		return graph;
	}

	@Override
	public void prepareMetrics() {
		Graph intenalElements = graph.internalElements();

		metrics.addMetric(getDependencyKind() + "DepNum", Long.toString(intenalElements.getEdgeCount()));
/*
		e.getData().add(graph);
		graph.setHarmonyElement(e);

		SearchSCCs sccfinder = new SearchSCCs(intenalElements);
		Set<Graph> allSCC = sccfinder.extractSCCs();

		Metadata md = new Metadata();
		md.getMetadata().put("Detailed Size Filtered", sccfinder.detailedSizeFiltered());
		e.getData().add(md);
		md.setHarmonyElement(e);

		long mfasNumber = 0;
		for (Graph jp : allSCC) {
			SearchMFAS mfas = new SearchMFAS(jp);
			mfasNumber += mfas.extractEdges().size();
		}

		metrics.addMetric(getDependencyKind() + "MFAS", Long.toString(mfasNumber));
		*/
	}

	public abstract DependencyKind getDependencyKind();

	protected void addEdge(String source, String target, EdgeKind kind) {
		
		if (!source.equals(target)) {
			graph.addEdge(getOrCreateVertex(source), getOrCreateVertex(target), kind);
		}
		
	}

	protected Vertex getOrCreateVertex(String id, boolean inModel) {
		Vertex jp = getOrCreateVertex(id);
		jp.setInModel(inModel);
		return jp;
	}

	protected Vertex getOrCreateVertex(String name) {
		Vertex vertex = elementsHashTable.get(name);

		if (vertex == null) {
			vertex = new Vertex(name, false);
			elementsHashTable.put(name, vertex);
			graph.addVertex(vertex);
		}
		
		return vertex;
	}

}
