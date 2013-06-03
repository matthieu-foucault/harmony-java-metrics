package fr.labri.harmony.analysis.metrics.compute;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import fr.labri.harmony.analysis.metrics.ComputeMetrics;
import fr.labri.harmony.analysis.metrics.ComputeMetricsScope;

public class CountOverrides extends ComputeMetrics {

	private int overridesCount;
	
	public int getOverridesCount() {
		return overridesCount;
	}

	public CountOverrides() {
		super();
		
		overridesCount = 0;
	}

	@Override
	public void prepareMetrics() {
		metrics.addMetric("OverridingMeth", Long.toString(overridesCount));
	}

	@Override
	public ComputeMetricsScope getScope() {
		return ComputeMetricsScope.EVENT;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		IMethodBinding method = node.resolveBinding();

		if (method != null && !method.isConstructor()) {
			ITypeBinding type = method.getDeclaringClass();
			if (isMethodOverriding(method, type)) overridesCount++;
		}

		return super.visit(node);
	}

	public boolean isMethodOverriding(IMethodBinding method, ITypeBinding type) {
		ITypeBinding superClass = type.getSuperclass();
		if (superClass != null) {

			if (isOverridingMethodInType(method, superClass)) return true;

			if (isMethodOverriding(method, superClass)) return true;
		}

		ITypeBinding[] interfaceBinds = type.getInterfaces();
		for (ITypeBinding interfaceBind : interfaceBinds) {

			if (isOverridingMethodInType(method, interfaceBind)) return true;

			if (isMethodOverriding(method, interfaceBind)) return true;
		}

		return false;
	}

	private boolean isOverridingMethodInType(IMethodBinding method, ITypeBinding type) {

		for (IMethodBinding tMethodBind : type.getDeclaredMethods()) {
			if (method.overrides(tMethodBind)) return true;
		}
		return false;
	}

}
