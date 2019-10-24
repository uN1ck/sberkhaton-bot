package com.example.demo.csm.data.entity.system;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Маленький объект, который позволяет получить метаданные коротким запросом
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MetaInf {
	/**
	 * Номер версии csm
	 */
	private String buildNumber;
	/**
	 * Имя кластера
	 */
	private String name;
	/**
	 * Какие модули были сконфигурированы
	 * Todo это не реализовано
	 */
	private Map<String, List<String>> csmServices;

	public MetaInf() {
		setCsmServices(new HashMap<>());
	}

	public MetaInf copy() {
		MetaInf clone = new MetaInf();
		clone.setBuildNumber(getBuildNumber());
		clone.setName(getName());
		for (Map.Entry<String, List<String>> entry : csmServices.entrySet()) {
			List<String> valuesNew = new ArrayList<>();
			for (String csmServicesValue : entry.getValue()) {
				valuesNew.add(csmServicesValue);
			}
			clone.getCsmServices().put(entry.getKey(), valuesNew);
		}
		return clone;
	}
}
