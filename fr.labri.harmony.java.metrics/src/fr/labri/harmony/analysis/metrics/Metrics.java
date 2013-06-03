package fr.labri.harmony.analysis.metrics;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import fr.labri.harmony.core.model.Data;

public class Metrics implements Data {
	
	private Set<Metric> metrics;
	
	public Metrics() {
		metrics = new HashSet<>();
		elementKind = Data.SOURCE;
	}

	public Set<Metric> getMetrics() {
		return metrics;
	}

	public void setMetrics(Set<Metric> metrics) {
		this.metrics = metrics;
	}
	
	public void addMetric(String name, String value) {
		metrics.add(new Metric(name, value));
	}
	
	@Id @GeneratedValue
	private int id;

	private int elementId;
	private int elementKind;
	
	@Override
	public int getElementId() {
		return elementId;
	}

	@Override
	public void setElementId(int id) {
		elementId = id;
	}

	@Override
	public int getElementKind() {
		return elementKind;
	}

	@Override
	public void setElementKind(int kind) {
		elementKind = kind;
	}

}
