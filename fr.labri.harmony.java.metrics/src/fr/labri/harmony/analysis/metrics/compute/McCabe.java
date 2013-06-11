package fr.labri.harmony.analysis.metrics.compute;

import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;

import fr.labri.harmony.analysis.metrics.ComputeMetrics;

/**
 * Computes average McCabe's cyclomatic complexity. Code adapted from Eclipse
 * metrics plugin : http://sourceforge.net/projects/metrics2/
 * 
 */
public class McCabe extends ComputeMetrics {

	private int cyclomatic;
	private int methodCount;
	private int totalCyclomatic;
	private int maxCyclomatic;

	public McCabe() {
		methodCount = 0;
		cyclomatic = 0;
		totalCyclomatic = 0;
		maxCyclomatic = 0;

	}

	@Override
	public void prepareMetrics() {
		double averageComplexity = totalCyclomatic;
		averageComplexity /= methodCount;
		metrics.addMetric("AVG_MCCABE", Double.toString(averageComplexity));
		metrics.addMetric("MAX_MCCABE", Integer.toString(maxCyclomatic));
	}


	@Override
	public boolean visit(MethodDeclaration node) {
		methodCount++;
		cyclomatic = 1;
		return super.visit(node);
	}

	@Override
	public boolean visit(CatchClause node) {
		cyclomatic++;
		return true;
	}

	@Override
	public boolean visit(ConditionalExpression node) {
		cyclomatic++;
		Expression ex = node.getExpression();
		if (ex != null) inspectExpression(ex.toString());
		return true;
	}

	@Override
	public boolean visit(DoStatement node) {
		cyclomatic++;
		Expression ex = node.getExpression();
		if (ex != null) inspectExpression(ex.toString());
		return true;
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		cyclomatic++;
		Expression ex = node.getExpression();
		if (ex != null) inspectExpression(ex.toString());
		return true;
	}

	@Override
	public boolean visit(ForStatement node) {
		cyclomatic++;
		Expression ex = node.getExpression();
		if (ex != null) inspectExpression(ex.toString());
		return true;
	}

	@Override
	public boolean visit(IfStatement node) {
		cyclomatic++;
		Expression ex = node.getExpression();
		if (ex != null) inspectExpression(ex.toString());
		return true;
	}

	@Override
	public boolean visit(SwitchCase node) {
		if (!node.isDefault()) {
			cyclomatic++;
		}
		return true;
	}

	@Override
	public boolean visit(WhileStatement node) {
		cyclomatic++;
		Expression ex = node.getExpression();
		if (ex != null) inspectExpression(ex.toString());
		return true;
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		Expression ex = node.getExpression();
		if (ex != null) inspectExpression(ex.toString());

		return false;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		Expression ex = node.getInitializer();
		if (ex != null) inspectExpression(ex.toString());
		return true;
	}

	@Override
	public void endVisit(MethodDeclaration node) {
		totalCyclomatic += cyclomatic;
		if (cyclomatic > maxCyclomatic) maxCyclomatic = cyclomatic;
		super.endVisit(node);
	}

	/**
	 * Count occurrences of && and || (conditional and or)
	 * 
	 * @param ex
	 */
	private void inspectExpression(String ex) {
		if (ex != null) {
			char[] chars = ex.toCharArray();
			for (int i = 0; i < chars.length - 1; i++) {
				char next = chars[i];
				if ((next == '&' || next == '|') && (next == chars[i + 1])) {
					cyclomatic++;
				}
			}
		}
	}

	@Override
	public boolean requiresAllFiles() {
		// TODO Auto-generated method stub
		return false;
	}

}
