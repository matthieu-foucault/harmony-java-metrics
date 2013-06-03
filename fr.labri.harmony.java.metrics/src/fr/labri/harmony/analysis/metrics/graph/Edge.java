package fr.labri.harmony.analysis.metrics.graph;


public class Edge {

	private String source, target;
	private EdgeKind kind;

	public Edge(String source, String target, EdgeKind kind) {
		super();
		this.source = source;
		this.target = target;
		this.kind = kind;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public EdgeKind getKind() {
		return kind;
	}

	public void setKind(EdgeKind kind) {
		this.kind = kind;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Edge other = (Edge) obj;
		if (source == null) {
			if (other.source != null) return false;
		} else if (!source.equals(other.source)) return false;
		if (target == null) {
			if (other.target != null) return false;
		} else if (!target.equals(other.target)) return false;
		return true;
	}

	public boolean hasKind(EdgeKind kind) {
		return this.kind.equals(kind);
	}

}
