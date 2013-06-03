package fr.labri.harmony.analysis.metrics.jdt;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;




public class ASTRequestor extends FileASTRequestor {
	
	private ASTVisitor metrics;
	
	public ASTRequestor(ASTVisitor metrics) {
		super();
		this.metrics = metrics;
	}
	
	@Override
	public void acceptAST(String sourceFilePath, CompilationUnit ast) {
		ast.accept(metrics);
	}

}
