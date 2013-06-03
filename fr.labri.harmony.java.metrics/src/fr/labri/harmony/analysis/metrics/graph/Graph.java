package fr.labri.harmony.analysis.metrics.graph;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;

@SuppressWarnings("serial")
public class Graph extends DirectedSparseGraph<Vertex, Edge> {

	public Graph() {
		super();
	}

	public Graph copy() {
		Graph copy = new Graph();
		for (Edge edge : getEdges())
			copy.addEdge(edge, getEndpoints(edge));

		return copy;
	}

	public Graph internalElements() {
		Graph copy = new Graph();
		for (Edge edge : getEdges()) {
			Pair<Vertex> endpoints = getEndpoints(edge);
			if (endpoints.getFirst().isInModel() && endpoints.getSecond().isInModel()) copy.addEdge(edge, endpoints);
		}
		return copy;
	}

	public Vertex getVertex(String name) {
		Vertex vertex = null;
		for (Vertex v : getVertices()) {
			if (v.getName().equals(name)) {
				vertex = v;
				break;
			}
		}
		
		return vertex;
	}

	public void addEdge(Vertex src, Vertex tgt, EdgeKind kind) {
		Edge e = findEdge(src, tgt);
		if (e == null) {
			e = new Edge(src.getName(), tgt.getName(), kind);
			addEdge(e, src, tgt);
		}
	}
}
