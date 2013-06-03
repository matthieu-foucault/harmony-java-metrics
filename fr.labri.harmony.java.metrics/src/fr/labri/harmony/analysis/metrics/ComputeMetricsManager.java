package fr.labri.harmony.analysis.metrics;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

import fr.labri.harmony.analysis.metrics.jdt.ASTGenerator;
import fr.labri.harmony.core.config.model.AnalysisConfiguration;

/**
 * This visitor takes as input a collection of visitors (one per metric) that
 * will be called for each node of the AST
 * 
 * @author Matthieu Foucault
 * 
 */
public class ComputeMetricsManager extends ComputeMetrics {

	protected Set<ComputeMetrics> computeMetrics;
	protected AnalysisConfiguration config;

	
	public AnalysisConfiguration getConfig() {
		return config;
	}

	
	public void setConfig(AnalysisConfiguration config) {
		this.config = config;
	}
	

	public ComputeMetricsScope getScope() {
		return null;
	}

	@Override
	public void prepareMetrics() {
		for (ComputeMetrics c : computeMetrics) {
			c.prepareMetrics();
			metrics.getMetrics().addAll(c.getMetrics().getMetrics());
		}
	}
	
	public void analyseWorkspace(String path, int elementId, int elementKind) {
		this.workspacePath = path;
		for (ComputeMetrics metrics : computeMetrics)
			metrics.analyseWorkspace(path);

		// get all java files
		Collection<File> allFiles = FileUtils.listFiles(new File(path), new String[]{"java"}, true);

		// Parse allFiles with JDT
		ASTGenerator gen = new ASTGenerator(path, config, this);
		gen.generate(allFiles.toArray(new String[allFiles.size()]), path);
		
		prepareMetrics();
		
		/*
		 * If we are on windows, we might have an exception when trying to
		 * update the workspace. This occurs when the AST parser of the
		 * previous iterations hasn't released some files. Running GC solves
		 * this issue.
		 */
		if (System.getProperty("os.name").toLowerCase().contains("win")) Runtime.getRuntime().gc();

	}

	public ComputeMetricsManager(Set<ComputeMetrics> computeMetrics) {
		this.computeMetrics = computeMetrics;
	}

	@Override
	public void endVisit(MethodDeclaration node) {
		for (ASTVisitor metric : computeMetrics)
			metric.endVisit(node);
		super.endVisit(node);
	}

	@Override
	public void endVisit(TypeDeclaration node) {
		for (ASTVisitor metric : computeMetrics)
			metric.endVisit(node);
		super.endVisit(node);
	}

	// TODO : implement endVisit for other types.

	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayAccess node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayCreation node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayInitializer node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayType node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(AssertStatement node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(Assignment node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(Block node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(BlockComment node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(BooleanLiteral node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(BreakStatement node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(CastExpression node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(CatchClause node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(CharacterLiteral node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(CompilationUnit node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ConditionalExpression node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ConstructorInvocation node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ContinueStatement node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(DoStatement node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(EmptyStatement node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(FieldAccess node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ForStatement node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(IfStatement node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(InfixExpression node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(InstanceofExpression node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(Initializer node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(Javadoc node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(LabeledStatement node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(LineComment node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(MemberRef node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(MemberValuePair node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodRef node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodRefParameter node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(Modifier node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(NullLiteral node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(NumberLiteral node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ParameterizedType node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ParenthesizedExpression node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(PostfixExpression node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(PrefixExpression node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(PrimitiveType node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(QualifiedName node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(QualifiedType node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ReturnStatement node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(SimpleName node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(SimpleType node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(StringLiteral node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperConstructorInvocation node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(SwitchCase node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(SwitchStatement node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(SynchronizedStatement node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(TagElement node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(TextElement node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ThisExpression node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(ThrowStatement node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(TryStatement node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclarationStatement node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeLiteral node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeParameter node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(UnionType node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationExpression node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(WhileStatement node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(WildcardType node) {
		for (ASTVisitor metric : computeMetrics)
			metric.visit(node);
		return super.visit(node);
	}

}
