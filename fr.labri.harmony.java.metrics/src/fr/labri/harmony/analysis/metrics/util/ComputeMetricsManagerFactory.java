package fr.labri.harmony.analysis.metrics.util;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import fr.labri.harmony.analysis.metrics.ComputeMetrics;
import fr.labri.harmony.analysis.metrics.ComputeMetricsManager;
import fr.labri.harmony.analysis.metrics.ComputeMetricsScope;
import fr.labri.harmony.core.config.model.AnalysisConfiguration;

public class ComputeMetricsManagerFactory {

	public static ComputeMetricsManager createComputeMetricsManager(AnalysisConfiguration config, ComputeMetricsScope scope) {
		Set<ComputeMetrics> computeMetrics = new HashSet<ComputeMetrics>();

		Collection<String> metricsNames = (Collection<String>) config.getOptions().get("metrics");

		for (String metricName : metricsNames) {
			try {
				Class<? extends ComputeMetrics> c = Class.forName(metricName).asSubclass(ComputeMetrics.class);
				Constructor<? extends ComputeMetrics> cst = c.getConstructor();
				ComputeMetrics cm = cst.newInstance();
				if (cm.getScope() == scope) computeMetrics.add(cm);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		ComputeMetricsManager manager = new ComputeMetricsManager(computeMetrics);
		manager.setConfig(config);

		return manager;
	}

}
