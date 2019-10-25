package com.example.demo.csm.data.entity.wildfly;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;
import java.util.Objects;

/**
 * Данные по ММТ тредпулам
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ThreadPoolMetric {
	/**
	 * Имя тредпула
	 */
	private String name;
	/**
	 * Задач выполняется
	 */
	private Integer taskInProgress;
	/**
	 * Доступно потоков
	 */
	private Integer poolSize;
	/**
	 * Когда обновлено
	 */
	private Date updatedAt;


	public ThreadPoolMetric() {

	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		ThreadPoolMetric other = (ThreadPoolMetric) obj;
		return getName().equals(other.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	public ThreadPoolMetric copy() {
		ThreadPoolMetric clone = new ThreadPoolMetric();
		if (getPoolSize() != null) {
			clone.setPoolSize(getPoolSize().intValue());
		}
		if (getTaskInProgress() != null) {
			clone.setTaskInProgress(getTaskInProgress().intValue());
		}
		if (getUpdatedAt() != null) {
			clone.setUpdatedAt(new Date(getUpdatedAt().getTime()));
		}
		clone.setName(getName());
		return clone;
	}
}
