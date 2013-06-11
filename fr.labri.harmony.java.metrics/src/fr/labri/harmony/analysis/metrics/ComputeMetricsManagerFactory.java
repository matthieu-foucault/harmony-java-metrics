package fr.labri.harmony.analysis.metrics;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.labri.harmony.core.config.model.AnalysisConfiguration;

public class ComputeMetricsManagerFactory {

	public static ComputeMetricsManager createComputeMetricsManager(AnalysisConfiguration config, List<String> selectedUnits) {
		Set<ComputeMetrics> computeMetrics = new HashSet<ComputeMetrics>();

		@SuppressWarnings("unchecked")
		Collection<String> metricsNames = (Collection<String>) config.getOptions().get("metrics");

		for (String metricName : metricsNames) {
			try {
				Class<? extends ComputeMetrics> c = Class.forName(metricName).asSubclass(ComputeMetrics.class);
				Constructor<? extends ComputeMetrics> cst = c.getConstructor(Collection.class);
				ComputeMetrics cm = cst.newInstance(selectedUnits);
				computeMetrics.add(cm);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		ComputeMetricsManager manager = new ComputeMetricsManager(computeMetrics, selectedUnits);
		manager.setConfig(config);

		return manager;
	}

}
