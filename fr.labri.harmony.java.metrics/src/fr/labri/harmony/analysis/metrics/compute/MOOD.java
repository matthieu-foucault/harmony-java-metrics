package fr.labri.harmony.analysis.metrics.compute;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import fr.labri.harmony.analysis.metrics.ComputeMetrics;
import fr.labri.harmony.analysis.metrics.ComputeMetricsManager;
import fr.labri.harmony.analysis.metrics.ComputeMetricsScope;
import fr.labri.harmony.analysis.metrics.graph.Edge;
import fr.labri.harmony.analysis.metrics.graph.EdgeKind;
import fr.labri.harmony.analysis.metrics.graph.Graph;
import fr.labri.harmony.analysis.metrics.graph.Vertex;

/**
 * Implementation of the MOOD metrics set: [1] F. Brito e Abreu and W. Melo,
 * “Evaluating the impact of object-oriented design on software quality, in
 * Proceedings of the 3rd International Software Metrics Symposium, 1996, pp. 90-99.
 * 
 * In order to reuse existing metrics class, this class the
 * {@link ComputeMetricsManager} composite
 * 
 * @author Matthieu Foucault
 * 
 */
public class MOOD extends ComputeMetricsManager {

	protected CountOverrides countOverrides;
	protected ComputeClassDependencies classDependencies;

	/**
	 * Stores for each class the number of new methods (i.e. that do not
	 * override inherited ones)
	 */
	private Map<String, Integer> newMethods;
	private Map<String, Integer> definedMethods;
	private Map<String, Integer> definedAttributes;
	private Map<String, Integer> packagesSizes;

	/**
	 * we have to keep typeBindings for MHF and AHF
	 */
	private Map<String, ITypeBinding> typeBindings;
	private Graph internalClasses;

	public MOOD() {
		super(new HashSet<ComputeMetrics>());

		countOverrides = new CountOverrides();
		computeMetrics.add(countOverrides);

		classDependencies = new ComputeClassDependencies();
		computeMetrics.add(classDependencies);

		newMethods = new HashMap<>();
		definedMethods = new HashMap<>();
		definedAttributes = new HashMap<>();
		packagesSizes = new HashMap<>();
		typeBindings = new HashMap<>();

	}

	@Override
	public void prepareMetrics() {

		internalClasses = classDependencies.getGraph().internalElements();

		computePolymorphismFactor();
		computeCouplingFactor();
		computeMethodInheritanceFactor();
		computeAttributeInheritanceFactor();
		computeMethodHidingFactor();
		computeAttributeHidingFactor();
	}

	private void computeAttributeInheritanceFactor() {

		int denominator = 0;
		double attributeInheritanceFactor = 0;
		denominator = 0;
		for (Vertex clazz : internalClasses.getVertices()) {
			attributeInheritanceFactor += getInheritedAttributesCount(clazz);
			denominator += getAvailableAttributesCount(clazz);
		}
		if (denominator == 0) attributeInheritanceFactor = 0;
		else attributeInheritanceFactor /= denominator;
		metrics.addMetric("MOOD_AIF", Double.toString(attributeInheritanceFactor));
	}

	private void computeMethodInheritanceFactor() {

		int denominator = 0;
		double methodInheritanceFactor = 0;
		denominator = 0;

		for (Vertex clazz : internalClasses.getVertices()) {
			methodInheritanceFactor += getInheritedMethodsCount(clazz);
			denominator += getAvailableMethodsCount(clazz);
		}
		if (denominator == 0) methodInheritanceFactor = 0;
		else methodInheritanceFactor /= denominator;
		metrics.addMetric("MOOD_MIF", Double.toString(methodInheritanceFactor));
	}

	private void computeCouplingFactor() {

		double couplingFactor = 0;
		if (internalClasses.getVertexCount() > 1) {
			for (Vertex vI : internalClasses.getVertices()) {
				for (Vertex vJ : internalClasses.getVertices()) {
					Edge edge = internalClasses.findEdge(vI, vJ);
					if (edge != null && edge.hasKind(EdgeKind.REF) && !vI.equals(vJ)) couplingFactor++;
				}
			}
			couplingFactor /= (Math.pow(internalClasses.getVertexCount(), 2) - internalClasses.getVertexCount());
		}
		metrics.addMetric("MOOD_COF", Double.toString(couplingFactor));
	}

	private void computePolymorphismFactor() {

		double polymorphismFactor = countOverrides.getOverridesCount();
		if (polymorphismFactor > 0) {
			int denominator = 0;
			for (String clazz : newMethods.keySet()) {
				denominator += newMethods.get(clazz) * getDescendantsCount(internalClasses.getVertex(clazz));
			}
			if (denominator == 0) polymorphismFactor = 0;
			else polymorphismFactor /= denominator;
		}
		metrics.addMetric("MOOD_POF", Double.toString(polymorphismFactor));
	}

	private void computeAttributeHidingFactor() {

		double attibuteHidingFactor = 0;
		double denominator = 0;
		for (ITypeBinding clazz : typeBindings.values()) {
			for (IVariableBinding field : clazz.getDeclaredFields()) {
				attibuteHidingFactor += 1 - getBindingVisibility(field, clazz);
				denominator++;
			}
		}
		if (denominator == 0) attibuteHidingFactor = 0;
		else attibuteHidingFactor /= denominator;
		metrics.addMetric("MOOD_AHF", Double.toString(attibuteHidingFactor));
	}

	private void computeMethodHidingFactor() {

		double methodHidingFactor = 0;
		double denominator = 0;
		for (ITypeBinding clazz : typeBindings.values()) {
			for (IMethodBinding method : clazz.getDeclaredMethods()) {
				methodHidingFactor += 1 - getBindingVisibility(method, clazz);
				denominator++;
			}
		}
		if (denominator == 0) methodHidingFactor = 0;
		else methodHidingFactor /= denominator;
		metrics.addMetric("MOOD_MHF", Double.toString(methodHidingFactor));
	}

	private double getBindingVisibility(IBinding binding, ITypeBinding clazz) {
		if (typeBindings.size() >= 1) return 0;

		double visibility = 0;
		switch (binding.getModifiers()) {
		case Flags.AccPrivate:
			// visibility += 0;
			break;
		case Flags.AccProtected:
			visibility += getDescendantsCount(internalClasses.getVertex(clazz.getQualifiedName()));
			break;
		case Flags.AccPublic:
			visibility += 1;
			break;
		default:
			IPackageBinding packaze = clazz.getPackage();
			if (packaze != null) {
				Integer size = packagesSizes.get(packaze.getName());
				if (size != null) visibility += size;
			}
		}
		return visibility / (typeBindings.size() - 1);
	}

	private int getAvailableAttributesCount(Vertex clazz) {
		Integer defined = definedAttributes.get(clazz.getName());
		if (defined == null) defined = 0;

		return defined + getInheritedAttributesCount(clazz);
	}

	private int getInheritedAttributesCount(Vertex clazz) {
		int count = 0;

		for (Edge edge : internalClasses.getOutEdges(clazz)) {
			if (edge.hasKind(EdgeKind.INH)) {
				Integer newAttr = definedAttributes.get(edge.getTarget());
				if (newAttr != null) count += newAttr;
			}
		}

		return count;
	}

	private int getDescendantsCount(Vertex clazz) {
		int count = 0;
		if (clazz == null) return 0;
		for (Edge edge : internalClasses.getInEdges(clazz)) {
			if (edge.hasKind(EdgeKind.INH)) {
				count++;
				count += getDescendantsCount(internalClasses.getSource(edge));
			}
		}
		return count;
	}

	private int getAvailableMethodsCount(Vertex clazz) {

		int count = 0;
		for (Edge edge : internalClasses.getOutEdges(clazz)) {
			if (edge.hasKind(EdgeKind.INH)) {
				Integer newMeth = newMethods.get(edge.getTarget());
				if (newMeth != null) count += newMeth;
			}
		}
		Integer definedMeth = definedMethods.get(clazz.getName());
		if (definedMeth != null) count += definedMeth;

		return count;
	}

	/**
	 * @param clazz
	 * @return the number of method inherited, and not overridden, in clazz
	 */
	private int getInheritedMethodsCount(Vertex clazz) {

		int count = 0;
		for (Edge edge : internalClasses.getOutEdges(clazz)) {
			if (edge.hasKind(EdgeKind.INH)) {
				Integer newMeth = newMethods.get(edge.getTarget());
				if (newMeth != null) count += newMeth;
			}
		}

		Integer defMeth = definedMethods.get(clazz.getName());
		if (defMeth == null) defMeth = 0;
		Integer newMeth = newMethods.get(clazz.getName());
		if (newMeth == null) newMeth = 0;

		return count + newMeth;
	}

	@Override
	public ComputeMetricsScope getScope() {
		return ComputeMetricsScope.EVENT;
	}

	@Override
	public boolean visit(TypeDeclaration node) {

		ITypeBinding binding = node.resolveBinding();
		if (binding != null) {
			String currentClass = binding.getQualifiedName();
			if (!currentClass.isEmpty()) typeBindings.put(currentClass, binding);
			IMethodBinding[] methods = binding.getDeclaredMethods();
			int newMethodsCount = 0;
			for (IMethodBinding method : methods) {
				if (!countOverrides.isMethodOverriding(method, binding)) newMethodsCount++;
			}
			definedMethods.put(currentClass, methods.length);
			newMethods.put(currentClass, newMethodsCount);

			IVariableBinding[] fields = binding.getDeclaredFields();
			definedAttributes.put(currentClass, fields.length);

			// Compute package size, for hiding factors
			IPackageBinding packaze = binding.getPackage();
			if (packaze != null && !packaze.getName().isEmpty()) {
				Integer size = packagesSizes.get(packaze.getName());
				if (size == null) size = 0;
				size++;
				packagesSizes.put(packaze.getName(), size);
			}

		}

		return super.visit(node);
	}

}
