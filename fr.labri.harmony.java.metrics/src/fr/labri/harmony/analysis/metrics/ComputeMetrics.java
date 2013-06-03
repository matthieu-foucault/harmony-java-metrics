package fr.labri.harmony.analysis.metrics;

import org.eclipse.jdt.core.dom.ASTVisitor;


public abstract class ComputeMetrics extends ASTVisitor {
	
	protected Metrics metrics;
	protected String workspacePath;
	
	public ComputeMetrics() {
		this.metrics = new Metrics();
	}
	
	public abstract void prepareMetrics();
	
	public abstract ComputeMetricsScope getScope();
	
	public Metrics getMetrics() {
		return this.metrics;
	}
	
	public void analyseWorkspace(String path) {
		this.workspacePath = path;
	}
	
}
