package fr.labri.harmony.analysis.metrics.jdt;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;

public class ASTGenerator {

	private ASTVisitor visitor;



	private String[] classPath;

	private String[] sourcePath;

	public ASTGenerator(ASTVisitor metrics) {
		this.visitor = metrics;
	}


	public void generate(String[] files) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);

		parser.setEnvironment(classPath, sourcePath, null, true);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		@SuppressWarnings("unchecked")
		Map<String, String> pOptions = JavaCore.getOptions();
		pOptions.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_7);
		pOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_7);
		pOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_7);
		parser.setCompilerOptions(pOptions);
		ASTRequestor req = new ASTRequestor(visitor);
		parser.createASTs(files, null, new String[] {}, req, null);
	}


}
