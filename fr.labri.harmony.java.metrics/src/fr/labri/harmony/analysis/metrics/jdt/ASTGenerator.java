package fr.labri.harmony.analysis.metrics.jdt;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;

import fr.labri.harmony.core.config.model.AnalysisConfiguration;

public class ASTGenerator {

	private ASTVisitor visitor;

	protected AnalysisConfiguration config;

	protected String localPath;

	private String[] classPath;

	private String[] sourcePath;

	public ASTGenerator(String localPath, AnalysisConfiguration config, ASTVisitor metrics) {
		this.localPath = localPath;
		this.config = config;
		this.visitor = metrics;
	}

	public ASTGenerator(String localPath) {
		this.localPath = localPath;
	}

	public ASTGenerator() {
		localPath = "";
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

	public void generate(String[] files, String dir) {
		ASTParser parser = ASTParser.newParser(AST.JLS4);

		setLocalPath(dir);

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
