package com.example.demo.csm.data.entity;

import com.example.demo.csm.data.entity.credentials.Administrator;
import com.example.demo.csm.data.entity.wildfly.Module;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Отображение группы
 */
@Data
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Group {
	/**
	 * Каскадные данные для подключения: логин
	 */
	@JsonIgnore
	private String user;
	/**
	 * Каскадные данные для подключения: пасс
	 */
	@JsonIgnore
	private String password;
	/**
	 * имя группы
	 */
	private String name;

	/**
	 * Обратный линк к кластеру
	 */
	@JsonIgnore
	private Cluster cluster;

	/**
	 * Список модулей, которые есть в данной группе
	 */
	protected List<Module> modulesOfCurrentGroup;

	/**
	 * Администраторы данной группы серверов
	 */
	private List<Administrator> admins;

	/**
	 * Вспомогательные данные, тип серверов
	 */
	private String type;

	/**
	 * Порядок отображения на странице
	 */
	private Integer showOrder;
	/**
	 * Нужно ли игнорировать проблемы по данной группе
	 */
	private Boolean ignoreIssues;


	/**
	 * Для автоматического формирования инвентори файла (ansible), префикс группы
	 */
	private String inventoryPrefix;
	/**
	 * Для автоматического формирования инвентори файла, имя группы в рамках инвентори файла (ansible)
	 */
	private String inventoryName;


	public Group() {
		this.admins = new ArrayList<>();
		setModulesOfCurrentGroup(new ArrayList<>());
	}


	@XmlElement
	public List<Administrator> getAdmins() {
		return admins;
	}

	@XmlAttribute
	public Integer getShowOrder() {
		return showOrder;
	}

	@XmlAttribute
	public Boolean getIgnoreIssues() {
		return ignoreIssues;
	}

	@XmlAttribute
	public String getUser() {
		return user;
	}

	@XmlAttribute
	public String getPassword() {
		return password;
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	@XmlAttribute
	public String getType() {
		return type;
	}


	@XmlAttribute
	public String getInventoryPrefix() {
		return inventoryPrefix;
	}

	@XmlAttribute
	public String getInventoryName() {
		return inventoryName;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Group group = (Group) o;
		return name.equals(group.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
}
