package fr.labri.harmony.analysis.metrics;

import java.util.Collection;

import org.eclipse.jdt.core.dom.ASTVisitor;


public abstract class ComputeMetrics extends ASTVisitor {
	
	protected Metrics metrics;
	protected String workspacePath;
	protected Collection<String> elementsToAnalyze;
	
	public ComputeMetrics() {
		this.metrics = new Metrics();
	}
	
	public ComputeMetrics(Collection<String> elementsToAnalyze) {
		this();
		this.elementsToAnalyze = elementsToAnalyze;
	}
	
	public abstract void prepareMetrics();
	
	public abstract boolean requiresAllFiles();
	public Metrics getMetrics() {
		return this.metrics;
	}
	
	public void analyseWorkspace(String path) {
		this.workspacePath = path;
	}
	
}
