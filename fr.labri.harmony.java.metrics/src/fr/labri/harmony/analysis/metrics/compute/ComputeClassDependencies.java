package fr.labri.harmony.analysis.metrics.compute;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import fr.labri.harmony.analysis.metrics.graph.Edge;
import fr.labri.harmony.analysis.metrics.graph.EdgeKind;
import fr.labri.harmony.analysis.metrics.graph.Graph;
import fr.labri.harmony.analysis.metrics.graph.Vertex;

public class ComputeClassDependencies extends BuildDependenciesGraph {

	private Graph internalClasses;

	public ComputeClassDependencies() {
		super();

	}

	@Override
	public void prepareMetrics() {

		double averageDIT = 0;
		double averageNOC = 0;
		internalClasses = graph.internalElements();
/*
		for (Vertex v : internalClasses.getVertices()) {
			for (Edge edge : internalClasses.getIncidentEdges(v)) {
				if (edge.getTarget().equals(v.getName()) && edge.hasKind(EdgeKind.INH)) averageNOC++;
			}
			averageDIT += getDIT(internalClasses, v);
		}
		averageDIT /= internalClasses.getVertexCount();
		averageNOC /= internalClasses.getVertexCount();

		metrics.addMetric("AVG_DIT", Double.toString(averageDIT));
		metrics.addMetric("AVG_NOC", Double.toString(averageNOC));*/

	}

	public int getDIT(String clazz) {
		Vertex v = internalClasses.getVertex(clazz);
		System.out.println("class : " + clazz);
		if (v == null) return -1;
		return getDIT(internalClasses, internalClasses.getVertex(clazz));
	}

	private int getDIT(Graph g, Vertex v) {

		int maxDIT = 0;
		for (Edge edge : g.getOutEdges(v)) {
			if (!edge.getTarget().equals(v.getName()) && edge.hasKind(EdgeKind.INH)) {
				
				System.out.println(edge.getSource() +  " -> " + edge.getTarget());
				int dit = getDIT(g, g.getVertex(edge.getTarget()));
				if (dit > maxDIT) maxDIT = dit;
			}
		}
		return maxDIT + 1;
	}

	public boolean visit(TypeDeclaration td) {

		ITypeBinding b = td.resolveBinding();
		if (b == null) return false;
		if (b.isAnonymous()) return false;
		if (b.getDeclaringMethod() != null) return false;

		String id = b.getQualifiedName();
		getOrCreateVertex(id, true);

		if (b.getSuperclass() != null) addEdge(id, b.getSuperclass().getQualifiedName(), EdgeKind.INH);

		if (b.isNested()) {
			addEdge(b.getDeclaringClass().getQualifiedName(), id, EdgeKind.INN);
		}

		for (ITypeBinding itfb : b.getInterfaces())
			addEdge(id, itfb.getQualifiedName(), EdgeKind.IMP);

		return true;
	}

	public boolean visit(EnumDeclaration ed) {
		ITypeBinding b = ed.resolveBinding();
		if (b != null) {
			String id = b.getQualifiedName();
			getOrCreateVertex(id, true);
		}
		return true;
	}

	public boolean visit(ClassInstanceCreation cic) {
		IMethodBinding mb = cic.resolveConstructorBinding();

		if (lastElement != null && mb != null && !lastElement.equals("")) {
			String callerId = lastElement;
			String id = mb.getDeclaringClass().getQualifiedName();
			addEdge(callerId, id, EdgeKind.REF);
		}
		return true;
	}

	public boolean visit(MethodDeclaration md) {
		IMethodBinding b = md.resolveBinding();

		ITypeBinding lastVisitedClass = null;
		if (b != null) lastVisitedClass = b.getDeclaringClass();

		if (lastVisitedClass != null) lastElement = lastVisitedClass.getQualifiedName();
		else lastElement = "";

		// Test required in case of default constructors that have null bindings
		if (b != null) {
			if (b.getDeclaringClass().isAnonymous()) return false;

			String id = b.getDeclaringClass().getQualifiedName();

			if (!b.isConstructor()) addEdge(id, extractClassName(b.getReturnType()), EdgeKind.REF);

			for (ITypeBinding parb : b.getParameterTypes())
				addEdge(id, extractClassName(parb.getTypeDeclaration()), EdgeKind.REF);

			for (ITypeBinding exb : b.getExceptionTypes())
				addEdge(id, extractClassName(exb), EdgeKind.REF);
		}

		return true;
	}

	public boolean visit(FieldDeclaration fd) {
		VariableDeclarationFragment f = (VariableDeclarationFragment) fd.fragments().get(0);
		IVariableBinding b = f.resolveBinding();
		if (b != null) {
			if (b.getDeclaringClass().isAnonymous()) return false;
			String id = b.getDeclaringClass().getQualifiedName();

			if (b.getType() != null) addEdge(id, extractClassName(b.getType()), EdgeKind.REF);
		}
		return true;
	}

	public boolean visit(MethodInvocation mi) {
		IMethodBinding mb = mi.resolveMethodBinding();

		if (lastElement != null && mb != null && !lastElement.equals("")) {
			String callerId = lastElement;
			String calleeId = extractClassName(mb.getDeclaringClass());
			addEdge(callerId, calleeId, EdgeKind.REF);
		}
		return true;
	}

	public boolean visit(SuperMethodInvocation mi) {
		IMethodBinding mb = mi.resolveMethodBinding();
		// System.out.println(mi.toString());
		if (lastElement != null && mb != null && !lastElement.equals("")) {
			String callerId = lastElement;
			String calleeId = extractClassName(mb.getDeclaringClass());
			addEdge(callerId, calleeId, EdgeKind.REF);
		}

		return true;
	}

	public boolean visit(ConstructorInvocation ci) {
		IMethodBinding mb = ci.resolveConstructorBinding();

		if (lastElement != null && mb != null && !lastElement.equals("")) {
			String callerId = lastElement;
			String calleeId = extractClassName(mb.getDeclaringClass());
			addEdge(callerId, calleeId, EdgeKind.REF);
		}

		return true;
	}

	public boolean visit(SuperConstructorInvocation ci) {
		IMethodBinding mb = ci.resolveConstructorBinding();
		if (lastElement != null && mb != null && !lastElement.equals("")) {
			String callerId = lastElement;
			String calleeId = extractClassName(mb.getDeclaringClass());
			addEdge(callerId, calleeId, EdgeKind.REF);
		}

		return true;
	}

	public boolean visit(FieldAccess a) {
		IVariableBinding b = a.resolveFieldBinding();

		if (b != null && lastElement != null && !lastElement.equals("")) {
			if (b.getDeclaringClass() != null) {
				if (!(a.getParent() instanceof Assignment)) {
					String callerId = lastElement;
					String fieldId = extractClassName(b.getDeclaringClass());
					addEdge(callerId, fieldId, EdgeKind.REF);
				} else {
					if (((Assignment) a.getParent()).getLeftHandSide() instanceof FieldAccess) {
						if (((Assignment) a.getParent()).getLeftHandSide() instanceof FieldAccess) {
							if (((FieldAccess) ((Assignment) a.getParent()).getLeftHandSide()).resolveFieldBinding() == null) return false;
						}
						if (!((FieldAccess) ((Assignment) a.getParent()).getLeftHandSide()).resolveFieldBinding().getKey().equals(b.getKey())) {
							String callerId = lastElement;
							String fieldId = extractClassName(b.getDeclaringClass());
							addEdge(callerId, fieldId, EdgeKind.REF);
						}
					}
				}
			}
		}

		return true;
	}

	public boolean visit(final Assignment node) {
		if (node.getLeftHandSide() instanceof FieldAccess) {
			IVariableBinding b = ((FieldAccess) node.getLeftHandSide()).resolveFieldBinding();

			if (b != null && lastElement != null && !lastElement.equals("")) {
				if (b.getDeclaringClass() != null) {
					String callerId = lastElement;
					String fieldId = extractClassName(b.getDeclaringClass());
					addEdge(callerId, fieldId, EdgeKind.REF);
				}
			}
		} else if (node.getLeftHandSide() instanceof SuperFieldAccess) {
			IVariableBinding b = ((SuperFieldAccess) node.getLeftHandSide()).resolveFieldBinding();

			if (b != null && lastElement != null && !lastElement.equals("")) {
				if (b.getDeclaringClass() != null) {
					String callerId = lastElement;
					String fieldId = extractClassName(b.getDeclaringClass());
					addEdge(callerId, fieldId, EdgeKind.REF);
				}
			}
		} else if (node.getLeftHandSide() instanceof SimpleName) {
			IBinding ib = ((Name) node.getLeftHandSide()).resolveBinding();
			if (ib != null) {
				if (ib.getKind() == IBinding.VARIABLE) {

					IVariableBinding b = (IVariableBinding) ((Name) node.getLeftHandSide()).resolveBinding();
					if (b.isField()) {

						if (b != null && lastElement != null && !lastElement.equals("")) {
							if (b.getDeclaringClass() != null) {
								String callerId = lastElement;
								String fieldId = extractClassName(b.getDeclaringClass());
								addEdge(callerId, fieldId, EdgeKind.REF);
							}
						}
					}

				}
			}
		}
		return true;
	}

	public boolean visit(SuperFieldAccess a) {
		IVariableBinding b = a.resolveFieldBinding();

		if (b != null && lastElement != null && !lastElement.equals("")) {
			if (b.getDeclaringClass() != null) {
				if (!(a.getParent() instanceof Assignment)) {
					String callerId = lastElement;
					String fieldId = extractClassName(b.getDeclaringClass());
					addEdge(callerId, fieldId, EdgeKind.REF);
				} else {
					if (((Assignment) a.getParent()).getLeftHandSide() instanceof SuperFieldAccess) {
						if (!((SuperFieldAccess) ((Assignment) a.getParent()).getLeftHandSide()).resolveFieldBinding().getKey().equals(b.getKey())) {
							String callerId = lastElement;
							String fieldId = extractClassName(b.getDeclaringClass());
							addEdge(callerId, fieldId, EdgeKind.REF);
						}
					}
				}
			}
		}

		return true;
	}

	public boolean visit(CatchClause c) {
		if (lastElement != null && !lastElement.equals("")) {
			ITypeBinding ex = c.getException().getType().resolveBinding();
			if (ex != null) addEdge(lastElement, extractClassName(ex), EdgeKind.REF);
		}

		return true;
	}

	public boolean visit(SimpleName n) {
		IBinding ib = n.resolveBinding();
		if (ib != null) {
			if (ib.getKind() == IBinding.VARIABLE) {

				IVariableBinding b = (IVariableBinding) n.resolveBinding();
				if (b.isField()) {

					if (b != null && lastElement != null && !lastElement.equals("")) {
						if (b.getDeclaringClass() != null) {
							if (!(n.getParent() instanceof Assignment)) {
								String callerId = lastElement;
								String fieldId = extractClassName(b.getDeclaringClass());
								addEdge(callerId, fieldId, EdgeKind.REF);
							} else {
								if (n.getParent().getParent() instanceof Assignment) {
									if (((Assignment) n.getParent().getParent()).getLeftHandSide() instanceof SimpleName)
										if (!(((SimpleName) ((Assignment) n.getParent().getParent()).getLeftHandSide())).resolveBinding().getKey()
												.equals(b.getKey())) {
											String callerId = lastElement;
											String fieldId = extractClassName(b.getDeclaringClass());
											addEdge(callerId, fieldId, EdgeKind.REF);
										}
								}
							}
						}
					}
				}

			}
		}
		return true;
	}

	public String extractClassName(ITypeBinding cls) {

		if (cls != null) {
			String clsName = cls.getQualifiedName();
			if (clsName != null) return clsName;
			else return "";
		} else return "";
	}

	@Override
	public DependencyKind getDependencyKind() {
		return DependencyKind.Class;
	}

}
