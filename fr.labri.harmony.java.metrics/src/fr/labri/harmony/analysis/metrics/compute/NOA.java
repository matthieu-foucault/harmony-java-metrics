package fr.labri.harmony.analysis.metrics.compute;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import fr.labri.harmony.analysis.metrics.ComputeMetrics;

public class NOA extends ComputeMetrics {

	private Iterator<String> filesPathsIt;
	private String currentFilePath;

	public NOA() {
		super();
	}

	public NOA(Collection<String> selectedFilesPaths) {
		super(selectedFilesPaths);
		filesPathsIt = this.elementsToAnalyze.iterator();
	}

	@Override
	public void prepareMetrics() {

	}
	
	@Override
	public boolean visit(CompilationUnit node) {
		if (filesPathsIt.hasNext()) currentFilePath = filesPathsIt.next();
		return super.visit(node);
	}

	public boolean visit(TypeDeclaration td) {
		if (currentFilePath != null) {
			int noa = td.getFields().length;
			metrics.addMetric("NOA", Integer.toString(noa));

		}
		return true;
	}

	@Override
	public boolean requiresAllFiles() {
		return false;
	}

}
