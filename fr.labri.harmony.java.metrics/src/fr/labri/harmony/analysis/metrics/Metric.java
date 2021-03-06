package fr.labri.harmony.analysis.metrics;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Metric {

	@Id
	@GeneratedValue
	private int id;

	private String name;

	private String value;
	
	private String elementNativeId;

	public Metric() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Metric(String name, String value) {
		this.name = name;
		this.value = value;
	}
	

	public Metric(String name, String value, String elementNativeId) {
		super();
		this.name = name;
		this.value = value;
		this.elementNativeId = elementNativeId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Metric)) return false;
		else return ((Metric) o).getName().equals(this.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	public String getElementNativeId() {
		return elementNativeId;
	}

	public void setElementNativeId(String elementNativeId) {
		this.elementNativeId = elementNativeId;
	}

}
