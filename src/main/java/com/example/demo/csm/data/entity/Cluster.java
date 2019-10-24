package com.example.demo.csm.data.entity;

import com.example.demo.csm.data.entity.credentials.Administrator;
import com.example.demo.csm.data.entity.system.MetaInf;
import com.example.demo.csm.data.entity.wildfly.WildFlyGroup;
import com.example.demo.csm.data.entity.wildfly.WildFlyNode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Главный тег, содержащий в себе практически все данные + все конфигурационные данные,
 * которые читаются либо из xml, либо из бд, но в итоге все равно записываются сюда
 */
@XmlRootElement
@Data
@Slf4j
@XmlAccessorType(XmlAccessType.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Cluster {

	/**
	 * id кластера
	 */
	private Integer id;
	/**
	 * Имя кластера PK для DTO
	 */
	private String name;
	/**
	 * Сквозной список опрашиваемых узлов, без повторений
	 */
//	@JsonIgnore
	private List<WildFlyNode> nodeList;
	/**
	 * Путь, где должны лежать логи
	 */
	@JsonIgnore
	private String logsPath;
	/**
	 * Легкая информация, позволяющая получить данные о нужном кластере
	 */
	@JsonIgnore
	private MetaInf metaInf;
	/**
	 * Каскадные креденшиалы: пользователь, самый высокоуровневый
	 */
	@JsonIgnore
	private String user;
	/**
	 * Каскадные креденшиалы: пароль, самый высокоуровневый
	 */
	@JsonIgnore
	private String password;
	/**
	 * Список групп
	 */
	private List<WildFlyGroup> groups;
	/**
	 * Список енвелоп кластеров
	 */

	/**
	 * Конфигурационная информация по максимальному количеству потоков, которые можно алоцировать для wf
	 */
	@JsonIgnore
	private Integer flyThreads;
	/**
	 * Конфигурационная информация по максимальному количеству потоков, которые можно алоцировать для envelope
	 */
	@JsonIgnore
	private Integer envelopeThreads;
	/**
	 * Опрашивать данные по оверлеям? По умолчанию нет
	 */
	private Boolean withOverlays = Boolean.valueOf(false);

	/**
	 * Опрашивать флай сервера по jmx?
	 */
	private Boolean flyjmx = Boolean.valueOf(false);


	/**
	 * Имя для формирования инвентори (ansible)
	 */
	private String inventoryName;





	/**
	 * Игнорировать проблемы wf
	 */
	private boolean ignoreFly;
	/**
	 * Игнорировать проблемы envelope
	 */
	private boolean ignoreEnvelope;


	/**
	 * No using platform (false by default)
	 */
	private boolean noPlatform;

	/**
	 * Имя стенда , не используется
	 */
	private String standName;




	/**
	 * Откуда прочитан конфиг
	 */
	private boolean readedFromDB;

	/**
	 * Данные для SpringSecurity для авторизации в csm
	 */
	@JsonIgnore
	public List<Administrator> csmAdministrator;


	private List<String> initComponent;
	private List<String> coreModule;
	private boolean preferXmlThreadSettings;
	private boolean localSecurity;

	public Cluster() {
		setGroups(new ArrayList<>());
		setNodeList(new ArrayList<>());
		setMetaInf(new MetaInf());
		setCsmAdministrator(new ArrayList<>());
		setInitComponent(new ArrayList<>());
		setCoreModule(new ArrayList<>());
	}


	@XmlElementWrapper(name = "components")
	@XmlElement
	public List<String> getInitComponent() {
		return initComponent;
	}

	@XmlElementWrapper(name = "coreModules")
	@XmlElement
	public List<String> getCoreModule() {
		return coreModule;
	}


	@XmlElement
	public List<Administrator> getCsmAdministrator() {
		return csmAdministrator;
	}

	@XmlElement
	public List<WildFlyGroup> getGroups() {
		return groups;
	}


	@XmlElement
	public void setLogsPath(String logsPath) {
		this.logsPath = logsPath;
	}


	@XmlAttribute
	private boolean isLocalSecurity() {
		return localSecurity;
	}

	@XmlAttribute
	private boolean isPreferXmlThreadSettings() {
		return preferXmlThreadSettings;
	}


	@XmlAttribute
	public Boolean getWithOverlays() {
		return withOverlays;
	}


	@XmlAttribute
	public Integer getFlyThreads() {
		return flyThreads;
	}

	@XmlAttribute
	public Integer getEnvelopeThreads() {
		return envelopeThreads;
	}

	@XmlAttribute
	public Boolean getFlyjmx() {
		return flyjmx;
	}

	@XmlAttribute
	public String getInventoryName() {
		return inventoryName;
	}

	@XmlAttribute
	public boolean isIgnoreFly() {
		return ignoreFly;
	}

	@XmlAttribute
	public boolean isIgnoreEnvelope() {
		return ignoreEnvelope;
	}

	@XmlAttribute
	public boolean isNoPlatform() {
		return noPlatform;
	}

	@XmlAttribute
	public String getName() {
		return name;
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
	public String getStandName() {
		return standName;
	}




	public void setStandName(String standName) {
		this.standName = standName;
	}


	/**
	 * Поиск группы по имени
	 *
	 * @param name имя группы
	 * @return may be null
	 */
	public WildFlyGroup getGroupByName(String name) {
		for (WildFlyGroup g : groups) {
			if (g.getName().equals(name)) {
				return g;
			}
		}
		return null;
	}

	public WildFlyNode getNodeByUUID(UUID uuid) {
		for (WildFlyNode n : nodeList) {
			if (n.getUuid().toString().equals(uuid.toString())) {
				return n;
			}
		}
		return null;
	}

	public WildFlyNode getFlyNodeByIpAndPort(String Ip, int port) {
		if (port == -1) {
			for (WildFlyNode n : nodeList) {
				if (n.getIp() != null && n.getIp().equals(Ip)) {
					return n;
				}
			}
		} else {
			for (WildFlyNode n : nodeList) {
				if (n.getIp() != null && n.getPort() != null && n.getIp().equals(Ip) && n.getPort() == port) {
					return n;
				}
			}
		}
		return null;
	}


	public <T extends Node> T getNodeByDomainAndPort(String domainName, int port) {
		for (WildFlyNode n : nodeList) {
			if (n.getDomainName().equals(domainName) && n.getPort() == port) {
				return (T) n;
			}
		}
		return null;
	}

	public Cluster copyFlyCluster(Boolean withOverlays) {
		Cluster clone = new Cluster();
		clone.setName(getName());
		clone.setIgnoreFly(isIgnoreFly());
		clone.setIgnoreEnvelope(isIgnoreEnvelope());
		for (WildFlyNode node : nodeList) {
			clone.getNodeList().add(node.copy(withOverlays));
		}
		for (WildFlyGroup group : groups) {
			clone.getGroups().add(group.copy(clone));
		}
		for (Administrator administrator : csmAdministrator) {
			clone.getCsmAdministrator().add(administrator.copy());
		}
		return clone;
	}

	public Cluster copyEnvelopeClusters() {
		Cluster clone = new Cluster();
		clone.setIgnoreFly(isIgnoreFly());
		clone.setIgnoreEnvelope(isIgnoreEnvelope());
		clone.setName(getName());
		for (Administrator administrator : csmAdministrator) {
			clone.getCsmAdministrator().add(administrator.copy());
		}
		return clone;
	}

	public Cluster copy() {
		Cluster clone = new Cluster();
		clone.setId(getId());
		clone.setName(getName());
		clone.setLogsPath(getLogsPath());
		clone.setUser(getUser());
		clone.setPassword(getPassword());
		clone.setFlyThreads(getFlyThreads());
		clone.setEnvelopeThreads(getEnvelopeThreads());
		clone.setWithOverlays(getWithOverlays());
		clone.setFlyjmx(getFlyjmx());
		clone.setInventoryName(getInventoryName());
		clone.setIgnoreFly(isIgnoreFly());
		clone.setIgnoreEnvelope(isIgnoreEnvelope());
		clone.setNoPlatform(isNoPlatform());
		clone.setStandName(getStandName());
		clone.setReadedFromDB(isReadedFromDB());
		clone.setPreferXmlThreadSettings(isPreferXmlThreadSettings());
		clone.setLocalSecurity(isLocalSecurity());

		for (WildFlyNode node : nodeList) {
			clone.getNodeList().add(node.copy(true));
		}

		clone.setMetaInf(getMetaInf().copy());
		for (WildFlyGroup group : groups) {
			clone.getGroups().add(group.copy(clone));
		}
		for (Administrator administrator : csmAdministrator) {
			clone.getCsmAdministrator().add(administrator.copy());
		}
		for (String init : initComponent) {
			clone.getInitComponent().add(init);
		}
		for (String core : coreModule) {
			clone.getCoreModule().add(core);
		}

		return clone;
	}

	/**
	 * Забрать все необходимые параметры, которые есть только в xml файле
	 *
	 */

	public String toXmlString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<cluster name=\"");
		sb.append(name);
		sb.append("\" user=\"");
		sb.append(user);
		sb.append("\" password=\"");
		sb.append(password);
		sb.append("\">\n");

		for (WildFlyGroup wildFlyGroup : groups) {
			sb.append(wildFlyGroup.toXmlString());
		}


		sb.append("</cluster>\n");

		return sb.toString();

	}
}
