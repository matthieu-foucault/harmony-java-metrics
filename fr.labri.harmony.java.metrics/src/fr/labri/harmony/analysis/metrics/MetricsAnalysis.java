package fr.labri.harmony.analysis.metrics;

import java.util.Properties;

import fr.labri.harmony.analysis.metrics.util.ComputeMetricsManagerFactory;
import fr.labri.harmony.core.analysis.AbstractAnalysis;
import fr.labri.harmony.core.config.model.AnalysisConfiguration;
import fr.labri.harmony.core.dao.Dao;
import fr.labri.harmony.core.model.Source;
import fr.labri.harmony.core.source.WorkspaceException;

public class MetricsAnalysis extends AbstractAnalysis {

	public MetricsAnalysis() {
		super();
	}

	public MetricsAnalysis(AnalysisConfiguration config, Dao dao, Properties properties) {
		super(config, dao, properties);
	}

	@Override
	public void runOn(Source src) {
		try {
				ComputeMetricsManager manager = ComputeMetricsManagerFactory.createComputeMetricsManager(config, ComputeMetricsScope.EVENT);
				manager.analyseWorkspace(src.getWorkspace().getPath());
				Metrics metrics = manager.getMetrics();
				metrics.setElementId(src.getId());
				saveData(metrics);
		} catch (Exception e) {
			throw new WorkspaceException(e);
		}
		
	}
}
