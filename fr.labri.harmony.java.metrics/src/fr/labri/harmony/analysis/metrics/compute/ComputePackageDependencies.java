package fr.labri.harmony.analysis.metrics.compute;

import org.eclipse.jdt.core.dom.*;

import fr.labri.harmony.analysis.metrics.ComputeMetricsScope;

import fr.labri.harmony.analysis.metrics.graph.EdgeKind;
import fr.labri.harmony.analysis.metrics.graph.Graph;
import fr.labri.harmony.core.model.SourceElement;

public class ComputePackageDependencies extends BuildDependenciesGraph {

	public ComputePackageDependencies() {
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

	public Graph getJPackageGroup() {
		return graph;
	}

	public boolean visit(TypeDeclaration td) {

		ITypeBinding b = td.resolveBinding();
		if (b == null) return false;
		if (b.isAnonymous()) return false;
		if (b.getDeclaringMethod() != null) return false;

		String id = b.getPackage().getName();
		getOrCreateVertex(id, true);

		if (b.getSuperclass() != null) addEdge(id, b.getSuperclass().getPackage().getName(), EdgeKind.INH);

		if (b.isNested()) {
			addEdge(b.getDeclaringClass().getPackage().getName(), id, EdgeKind.INN);
		}

		for (ITypeBinding itfb : b.getInterfaces())
			addEdge(id, itfb.getPackage().getName(), EdgeKind.IMP);

		return true;
	}

	public boolean visit(EnumDeclaration ed) {
		ITypeBinding b = ed.resolveBinding();
		if (b != null) {
			String id = b.getPackage().getName();
			getOrCreateVertex(id, true);
		}
		return true;
	}

	public boolean visit(ClassInstanceCreation cic) {
		IMethodBinding mb = cic.resolveConstructorBinding();

		if (lastElement != null && mb != null && !lastElement.equals("")) {
			String callerId = lastElement;
			String id = mb.getDeclaringClass().getPackage().getName();
			addEdge(callerId, id, EdgeKind.REF);
		}
		return true;
	}

	public boolean visit(MethodDeclaration md) {
		IMethodBinding b = md.resolveBinding();

		ITypeBinding lastVisitedClass = null;
		IPackageBinding lastVisitedPack = null;
		if (b != null) lastVisitedClass = b.getDeclaringClass();

		if (lastVisitedClass != null) lastVisitedPack = lastVisitedClass.getPackage();

		if (lastVisitedPack != null)
			lastElement = lastVisitedPack.getName();
		else
			lastElement = "";

		// Test required in case of default constructors that have null bindings
		if (b != null) {
			if (b.getDeclaringClass().isAnonymous()) return false;

			String id = b.getDeclaringClass().getPackage().getName();

			if (!b.isConstructor()) addEdge(id, extractPackageName(b.getReturnType()), EdgeKind.REF);

			for (ITypeBinding parb : b.getParameterTypes())
				addEdge(id, extractPackageName(parb.getTypeDeclaration()), EdgeKind.REF);

			for (ITypeBinding exb : b.getExceptionTypes())
				addEdge(id, extractPackageName(exb), EdgeKind.REF);
		}

		return true;
	}

	public boolean visit(FieldDeclaration fd) {
		VariableDeclarationFragment f = (VariableDeclarationFragment) fd.fragments().get(0);
		IVariableBinding b = f.resolveBinding();
		if (b != null) {
			if (b.getDeclaringClass().isAnonymous()) return false;
			String id = b.getDeclaringClass().getPackage().getName();

			if (b.getType() != null) addEdge(id, extractPackageName(b.getType()), EdgeKind.REF);
		}
		return true;
	}

	public boolean visit(MethodInvocation mi) {
		IMethodBinding mb = mi.resolveMethodBinding();

		if (lastElement != null && mb != null && !lastElement.equals("")) {
			String callerId = lastElement;
			String calleeId = extractPackageName(mb.getDeclaringClass());
			addEdge(callerId, calleeId, EdgeKind.REF);
		}
		return true;
	}

	public boolean visit(SuperMethodInvocation mi) {
		IMethodBinding mb = mi.resolveMethodBinding();
		// System.out.println(mi.toString());
		if (lastElement != null && mb != null && !lastElement.equals("")) {
			String callerId = lastElement;
			String calleeId = extractPackageName(mb.getDeclaringClass());
			addEdge(callerId, calleeId, EdgeKind.REF);
		}

		return true;
	}

	public boolean visit(ConstructorInvocation ci) {
		IMethodBinding mb = ci.resolveConstructorBinding();

		if (lastElement != null && mb != null && !lastElement.equals("")) {
			String callerId = lastElement;
			String calleeId = extractPackageName(mb.getDeclaringClass());
			addEdge(callerId, calleeId, EdgeKind.REF);
		}

		return true;
	}

	public boolean visit(SuperConstructorInvocation ci) {
		IMethodBinding mb = ci.resolveConstructorBinding();
		if (lastElement != null && mb != null && !lastElement.equals("")) {
			String callerId = lastElement;
			String calleeId = extractPackageName(mb.getDeclaringClass());
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
					String fieldId = extractPackageName(b.getDeclaringClass());
					addEdge(callerId, fieldId, EdgeKind.REF);
				} else {
					if (((Assignment) a.getParent()).getLeftHandSide() instanceof FieldAccess) {
						if (((Assignment) a.getParent()).getLeftHandSide() instanceof FieldAccess) {
							if (((FieldAccess) ((Assignment) a.getParent()).getLeftHandSide()).resolveFieldBinding() == null) return false;
						}
						if (!((FieldAccess) ((Assignment) a.getParent()).getLeftHandSide()).resolveFieldBinding().getKey().equals(b.getKey())) {
							String callerId = lastElement;
							String fieldId = extractPackageName(b.getDeclaringClass());
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
					String fieldId = extractPackageName(b.getDeclaringClass());
					addEdge(callerId, fieldId, EdgeKind.REF);
				}
			}
		} else if (node.getLeftHandSide() instanceof SuperFieldAccess) {
			IVariableBinding b = ((SuperFieldAccess) node.getLeftHandSide()).resolveFieldBinding();

			if (b != null && lastElement != null && !lastElement.equals("")) {
				if (b.getDeclaringClass() != null) {
					String callerId = lastElement;
					String fieldId = extractPackageName(b.getDeclaringClass());
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
								String fieldId = extractPackageName(b.getDeclaringClass());
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
					String fieldId = extractPackageName(b.getDeclaringClass());
					addEdge(callerId, fieldId, EdgeKind.REF);
				} else {
					if (((Assignment) a.getParent()).getLeftHandSide() instanceof SuperFieldAccess) {
						if (!((SuperFieldAccess) ((Assignment) a.getParent()).getLeftHandSide()).resolveFieldBinding().getKey().equals(b.getKey())) {
							String callerId = lastElement;
							String fieldId = extractPackageName(b.getDeclaringClass());
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
			if (ex != null) addEdge(lastElement, extractPackageName(ex), EdgeKind.REF);
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
								String fieldId = extractPackageName(b.getDeclaringClass());
								addEdge(callerId, fieldId, EdgeKind.REF);
							} else {
								if (n.getParent().getParent() instanceof Assignment) {
									if (((Assignment) n.getParent().getParent()).getLeftHandSide() instanceof SimpleName) if (!(((SimpleName) ((Assignment) n
											.getParent().getParent()).getLeftHandSide())).resolveBinding().getKey().equals(b.getKey())) {
										String callerId = lastElement;
										String fieldId = extractPackageName(b.getDeclaringClass());
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

	public String extractPackageName(ITypeBinding cls) {

		if (cls.getPackage() != null) {
			String pckName = cls.getPackage().getName();
			if (pckName != null)
				return pckName;
			else
				return "#root";
		} else
			return "#root";
	}

	@Override
	public DependencyKind getDependencyKind() {
		return DependencyKind.Package;
	}

}
