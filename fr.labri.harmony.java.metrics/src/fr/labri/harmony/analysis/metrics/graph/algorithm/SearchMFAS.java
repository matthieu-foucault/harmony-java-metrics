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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import fr.labri.harmony.analysis.metrics.graph.Edge;
import fr.labri.harmony.analysis.metrics.graph.Graph;
import fr.labri.harmony.analysis.metrics.graph.Vertex;


public class SearchMFAS {

	private Graph graph;
	
	public SearchMFAS(Graph graph) {
		this.graph = graph;
	}

	
	public Set<Edge> extractEdges() {
		Graph g = graph.copy();
		
		List<Vertex> s1 = new LinkedList<Vertex>();
		List<Vertex> s2 = new LinkedList<Vertex>();

		while ( g.getVertexCount() > 0 ) {
			while ( nextSink(g) != null ) {
				Vertex u = nextSink(g); 
				s2.add(0,u);
				g.removeVertex(u);
			}
			while ( nextSource(g) != null ) {
				Vertex u = nextSource(g); 
				s1.add(u);
				g.removeVertex(u);
			}
			
			if ( g.getVertexCount() > 0 ){
				int max = Integer.MIN_VALUE;
				Vertex best = null;
				for(Vertex p: g.getVertices()) {
					int cur = g.outDegree(p) - g.inDegree(p);
					if ( cur > max ) {
						max = cur;
						best = p;
					}
				}
				s1.add(best);
				g.removeVertex(best);
			}
		}
		s1.addAll(s2);

		Set<Edge> mfas = new HashSet<Edge>();

		for (int j = s1.size() - 1 ; j >= 1 ; j--) {
			for( int i = j - 1 ; i >= 0 ; i-- ) {
				Vertex vj = graph.getVertex(s1.get(j).getName());
				Vertex vi = graph.getVertex(s1.get(i).getName());
				if (graph.isSuccessor(vi, vj))
					mfas.add(graph.findEdge(vj, vi));
			}

		}

		return mfas;
	}
	
	private Vertex nextSink(Graph pkgGroup) {
		for(Vertex p: pkgGroup.getVertices())
			if ( pkgGroup.outDegree(p) == 0 )
				return p;

		return null;
	}

	private Vertex nextSource(Graph pkgGroup) {
		for(Vertex p: pkgGroup.getVertices())
			if ( pkgGroup.inDegree(p) == 0 )
				return p;

		return null;
	}

}
