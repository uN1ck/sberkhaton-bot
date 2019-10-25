package com.example.demo.csm.data.entity.inventorisation;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Конфигурация для сброса данных о WF на диск
 */
@Data
@Slf4j
public class DumpConfig {
	/**
	 * Путь, где должен будет лежать json
	 */
	private String path;
	/**
	 * Как часто надо сбрасывать
	 */
	private Long period;
	/**
	 * Активировать фичу
	 */
	private Boolean enable;

	@XmlAttribute
	public String getPath() {
		return path;
	}

	@XmlAttribute
	public Long getPeriod() {
		return period;
	}

	@XmlAttribute
	public Boolean getEnable() {
		return enable;
	}

	public DumpConfig copy() {
		DumpConfig clone = new DumpConfig();
		if (getEnable() != null) {
			clone.setEnable(getEnable().booleanValue());
		}
		clone.setPath(getPath());
		if (getPeriod() != null) {
			clone.setPeriod(getPeriod().longValue());
		}
		return clone;
	}
}
