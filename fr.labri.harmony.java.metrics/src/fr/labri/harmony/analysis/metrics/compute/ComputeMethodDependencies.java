package fr.labri.harmony.analysis.metrics.compute;


import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

import fr.labri.harmony.analysis.metrics.ComputeMetricsScope;
import fr.labri.harmony.analysis.metrics.graph.EdgeKind;

public class ComputeMethodDependencies extends BuildDependenciesGraph {

	public ComputeMethodDependencies() {
		super();
	}

	@Override
	public void prepareMetrics() {
		super.prepareMetrics();
	}

	@Override
	public ComputeMetricsScope getScope() {
		return ComputeMetricsScope.EVENT;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		
		lastElement = getMethodSignature(node.resolveBinding());
		getOrCreateVertex(lastElement, true);

		return super.visit(node);
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		if (lastElement != "") {
			IMethodBinding method = node.resolveMethodBinding();
			if (method != null) {
				addEdge(lastElement, getMethodSignature(method), EdgeKind.REF);

			} 
			
		}
		return super.visit(node);
	}
	
	@Override
	public boolean visit(ConstructorInvocation node) {
		
		if (lastElement != "") {
			IMethodBinding method = node.resolveConstructorBinding();
			if (method != null) {
				addEdge(lastElement,getMethodSignature(method) , EdgeKind.REF);

			}
		}
		
		return super.visit(node);
	}
	
	
	@Override
	public boolean visit(MethodInvocation node) {
		if (lastElement != "") {
			
			IMethodBinding method = node.resolveMethodBinding();
			if (method != null) {
				addEdge(lastElement,getMethodSignature(method), EdgeKind.REF );

			}
				
		}
		return super.visit(node);
	}
	
	@Override
	public boolean visit(ClassInstanceCreation node) {
		if (lastElement != "") {
			IMethodBinding method = node.resolveConstructorBinding();
			if (method != null)
				addEdge(lastElement,getMethodSignature(method), EdgeKind.REF );

		}
		
		return super.visit(node);
	}

	private String getMethodSignature(IMethodBinding method) {
		if (method == null) {
			return "missing binding";
		}
		
		String signature = "";
		
		ITypeBinding declaringClass = method.getDeclaringClass();
		if (declaringClass != null) signature += declaringClass.getQualifiedName() + ".";
		
		signature += method.getName();
		signature += '(';
		ITypeBinding[] params = method.getParameterTypes();
		for (int i = 0 ; i < params.length ; ++i) {
			signature += params[i].getQualifiedName();
			if (i != params.length - 1) signature += ',';
		}
		signature += ')';
		
		return signature;
	}


	@Override
	public DependencyKind getDependencyKind() {
		return DependencyKind.Method;
	}

}
