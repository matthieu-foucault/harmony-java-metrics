package fr.labri.harmony.analysis.metrics.graph;


public class Vertex {

	private String name;
	private boolean isInModel;

	public Vertex(String name, boolean isInModel) {
		super();
		this.name = name;
		this.isInModel = isInModel;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isInModel() {
		return isInModel;
	}

	public void setInModel(boolean isInModel) {
		this.isInModel = isInModel;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isInModel ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Vertex other = (Vertex) obj;
		if (isInModel != other.isInModel) return false;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		return true;
	}

}
