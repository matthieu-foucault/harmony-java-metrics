/*
 * Copyright 2009-2011 Jean-Rémy Falleri
 * 
 * This file is part of Popsycle.
 * Popsycle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Popsycle is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Popsycle.  If not, see <http://www.gnu.org/licenses/>.
 */

package fr.labri.harmony.analysis.metrics.graph.algorithm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import fr.labri.harmony.analysis.metrics.graph.Edge;
import fr.labri.harmony.analysis.metrics.graph.Graph;
import fr.labri.harmony.analysis.metrics.graph.Vertex;

public class SearchSCCs {

	private Graph graph;

	private Set<Graph> components;

	private int index;

	private Map<Vertex, Integer> indexMap;

	private Map<Vertex, Integer> lowLinkMap;

	// private Set<Vertex> onStack;

	private Stack<Vertex> nodeStack;

	public SearchSCCs(Graph pkgGrp) {
		this.graph = pkgGrp;
	}

	public Set<Graph> getComponents() {
		return components;
	}

	public void setComponents(Set<Graph> components) {
		this.components = components;
	}

	public Set<Graph> extractSCCs() {
		components = new HashSet<>();
		index = 0;
		nodeStack = new Stack<Vertex>();
		indexMap = new HashMap<Vertex, Integer>();
		lowLinkMap = new HashMap<Vertex, Integer>();
		// onStack = new HashSet<Vertex>();
		for (Vertex v : graph.getVertices())
			if (!indexMap.containsKey(v)) tarjan(v);

		// We redraw the edges for the components
		for (Graph component : components) {
			for (Vertex source : component.getVertices()) {
				//FIXME 
				/*
				java.util.ConcurrentModificationException
				at java.util.HashMap$HashIterator.nextEntry(Unknown Source)
				at java.util.HashMap$KeyIterator.next(Unknown Source)
				at java.util.Collections$UnmodifiableCollection$1.next(Unknown Source)
				at fr.labri.harmony.analysis.metrics.graph.algorithm.SearchSCCs.extractSCCs(SearchSCCs.java:71)
				*/
				Vertex originalVertex = graph.getVertex(source.getName());
				for (Edge edge : graph.getInEdges(originalVertex)) {
					if (component.getVertex(edge.getTarget()) != null) component.addEdge(source, graph.getDest(edge),
							edge.getKind());
				}
			}
		}

		return components;
	}

	public int getTotalPackageInScc() {
		int totalVerticesInScc = 0;
		for (Graph component : components) {
			if (component.getVertexCount() > 1) {
				totalVerticesInScc += component.getVertexCount();
			}
		}
		return totalVerticesInScc;
	}

	public String detailedSizeFiltered() {
		String s = "";
		int i = 0;
		for (Graph component : components) {
			if (component.getVertices().size() > 1) {
				i++;
				s = s + "(" + component.getVertexCount() + "," + component.getEdgeCount() + ")";
			}
		}
		s = i + " - " + s;
		return s;
	}

	private void tarjan(Vertex v) {
		indexMap.put(v, index);
		lowLinkMap.put(v, index);
		index++;
		nodeStack.push(v);

		for (Vertex target : graph.getSuccessors(v)) {
			if (!indexMap.containsKey(target)) {
				tarjan(target);
				lowLinkMap.put(v, Math.min(lowLinkMap.get(v), lowLinkMap.get(target)));
			} else if (nodeStack.contains(target)) lowLinkMap.put(v, Math.min(lowLinkMap.get(v), indexMap.get(target)));
		}

		if (lowLinkMap.get(v) == indexMap.get(v)) {
			// Handle SCC
			Graph c = new Graph();
			Vertex v_p;
			do {
				v_p = nodeStack.pop();
				c.addVertex(new Vertex(v_p.getName(), false));
			} while (!v.equals(v_p));
			components.add(c);
		}
	}
}
