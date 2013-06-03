package fr.labri.harmony.analysis.metrics.compute;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import fr.labri.harmony.analysis.metrics.ComputeMetrics;
import fr.labri.harmony.analysis.metrics.ComputeMetricsScope;

public class CountElements extends ComputeMetrics {

	private long attributesCount, methodsCount, classesCount, packagesCount, abstractClassesCount, interfacesCount;

	private Set<Integer> visitedPackages;

	public CountElements() {
		visitedPackages = new HashSet<>();
		attributesCount = 0;
		methodsCount = 0;
		classesCount = 0;
		packagesCount = 0;
		abstractClassesCount = 0;
		interfacesCount = 0;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		methodsCount++;
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		classesCount++;
		if (node.isInterface()) interfacesCount++;
		if (node.modifiers().contains(Modifier.ABSTRACT)) abstractClassesCount++;
		return super.visit(node);
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		classesCount++;
		return super.visit(node);
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		attributesCount++;
		return super.visit(node);
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		int hashCode = node.getName().getFullyQualifiedName().hashCode();
		if (hashCode != 0 && !visitedPackages.contains(hashCode)) {
			visitedPackages.add(hashCode);
			packagesCount++;
		}
		return super.visit(node);
	}

	@Override
	public void prepareMetrics() {
		metrics.addMetric("Attributes", Long.toString(attributesCount));
		metrics.addMetric("Methods", Long.toString(methodsCount));
		metrics.addMetric("Classes", Long.toString(classesCount));
		metrics.addMetric("Packages", Long.toString(packagesCount));
		metrics.addMetric("Interfaces", Long.toString(interfacesCount));
		metrics.addMetric("AbstractClasses", Long.toString(abstractClassesCount));
	}

	@Override
	public ComputeMetricsScope getScope() {
		return ComputeMetricsScope.EVENT;
	}

}
