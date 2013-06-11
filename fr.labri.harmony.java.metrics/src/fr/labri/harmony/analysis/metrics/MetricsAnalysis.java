package fr.labri.harmony.analysis.metrics;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

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
			@SuppressWarnings("unchecked")
			Collection<String> selectedUnits = (Collection<String>) src.getConfig().getOptions().get("sampled-items");
			List<String> selectedFilesPaths = new ArrayList<>();
			if (selectedUnits != null) {
				for (String selectedUnit : selectedUnits) {
					selectedFilesPaths.add(src.getWorkspace().getPath() + File.separator + selectedUnit.replace('/', File.separatorChar));
				}
				ComputeMetricsManager manager = ComputeMetricsManagerFactory.createComputeMetricsManager(config, selectedFilesPaths);
				manager.analyseWorkspace(src.getWorkspace().getPath());
				Metrics metrics = manager.getMetrics();
				metrics.setElementId(src.getId());
				saveData(metrics);
			}
		} catch (Exception e) {
			throw new WorkspaceException(e);
		}

	}
}
