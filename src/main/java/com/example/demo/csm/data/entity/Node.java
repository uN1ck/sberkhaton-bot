package com.example.demo.csm.data.entity;

import com.example.demo.csm.data.entity.wildfly.PrintName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.*;

/**
 * Отображение узла внутри CSM, совмещает и конфигурационные данные и контейнер для поступающих данных
 */
@Data
@XmlAccessorType(XmlAccessType.NONE)
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class Node {

	/**
	 * Тип узла (wf,envelope,was)
	 */
	private String type;
	/**
	 * Каскадные креды, последний этам переопределения: логин
	 */
	@JsonIgnore
	private String user;
	/**
	 * Каскадные креды, последний этам переопределения: пароль
	 */
	@JsonIgnore
	private String password;
	/**
	 * Время, когда из данного сервиса последний раз получали данные
	 */
	protected Date lastFetchTime;
	/**
	 * Подвис ли?
	 */
	protected boolean pending;

	/**
	 * Строка с ошибкой по данному узлу, помогает в дебаге
	 */
	private String fails;
	/**
	 * Отображаемое имя на странице
	 */
	private String printName;

	/**
	 * Хостнейм, адрес для подключения
	 */
	protected String domainName;

	/**
	 * Доступен ли узел
	 */
	protected boolean reachable;
	/**
	 * Время получения данных
	 */
	protected long lastPing;
	/**
	 * Время жизни JVm
	 */
	protected long uptime;

	/**
	 * Порт management консоли wf
	 */
	protected Integer port;
	/**
	 * Внутренний номер
	 */
	private UUID uuid;
	/**
	 * Частота опроса
	 */
	private Integer period;
	/**
	 * Нужно ли игнорировать проблемы по данному узлу
	 */
	private Boolean ignoreIssues;

	/**
	 * Какие принтнеймы имеет данный узел для разных групп (если втречается больше чем в 1 группе
	 * todo перенести класс {@link PrintName}
	 */
	private List<PrintName> printNames;

	/**
	 * Фиксация ранабла, который извлекает данные по этому серверу
	 */
	@JsonIgnore
	private Runnable fetcher;


	/**
	 * IP адрес  узла
	 */
	private String ip;


	public Node() {
		printNames = new ArrayList();
	}


	@XmlAttribute
	public String getUser() {
		return user;
	}

	@XmlAttribute
	public Boolean getIgnoreIssues() {
		return ignoreIssues;
	}

	@XmlAttribute
	public String getPassword() {
		return password;
	}

	@XmlAttribute
	public String getPrintName() {
		return printName;
	}

	@XmlAttribute
	public String getDomainName() {
		return domainName;
	}

	@XmlAttribute
	public Integer getPort() {
		return port;
	}

	public <T extends Node> T withUser(String user) {
		setUser(user);
		return (T) this;
	}


	public <T extends Node> T withPassword(String password) {
		setPassword(password);
		return (T) this;
	}


	public <T extends  Node> T withPrintName(String printName) {
		setPrintName(printName);
		return (T) this;
	}


	public <T extends  Node> T withDomainName(String withDomainName) {
		setDomainName(withDomainName);
		return (T) this;
	}


	public <T extends Node> T withPort(Integer port) {
		setPort(port);
		return (T) this;
	}

	public <T extends Node> T withUUID(UUID uuid) {
		setUuid(uuid);
		return (T) this;
	}

	public <T extends Node> T withType(String type) {
		setType(type);
		return (T) this;
	}

	public <T extends Node> T withPeriod(Integer period) {
		setPeriod(period);
		return (T) this;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Node node = (Node) o;
		return domainName.equals(node.domainName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(domainName);
	}

	@Override
	public String toString() {
		return "DomainName: " + domainName;
	}


	public static <T extends Node> void copy(T from, T to) {
		to.setType(from.getType());
		to.setUser(from.getUser());
		to.setPassword(from.getPassword());
		if (from.getLastFetchTime() != null) {
			to.setLastFetchTime(new Date(from.getLastFetchTime().getTime()));
		} else {
			to.setLastFetchTime(new Date(0));
		}
		to.setPending(from.isPending());
		to.setReachable(from.isReachable());
		to.setFails(from.getFails());
		to.setPrintName(from.getPrintName());
		to.setDomainName(from.getDomainName());
		to.setLastPing(from.getLastPing());
		to.setUptime(from.getUptime());
		to.setPort(from.getPort());
		if (from.getUuid() != null) {
			to.setUuid(UUID.fromString(from.getUuid().toString()));
		}
		to.setPeriod(from.getPeriod());
		to.setIgnoreIssues(from.getIgnoreIssues());
		to.setIp(from.getIp());
		//не уверен что фетчер нужен, это внутренняя кухн.
		for (PrintName pn : from.getPrintNames()) {
			to.getPrintNames().add(pn.copy());
		}
	}
}
