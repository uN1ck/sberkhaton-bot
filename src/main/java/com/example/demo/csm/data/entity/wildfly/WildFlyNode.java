package com.example.demo.csm.data.entity.wildfly;

import com.example.demo.csm.data.entity.NodeWithModules;
import com.example.demo.csm.data.entity.inventorisation.InventorizationProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

@Data
@XmlAccessorType(XmlAccessType.NONE)
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class WildFlyNode extends NodeWithModules {
	private static final int DEFAULT_PORT = 9990;
	/**
	 * Статусы модулей для отправки в influx
	 */
	private static final List<String> statuses = Arrays.asList("OK", "FAILED", "STOPPED");

	/**
	 * Объект для отправки в influx статусов модулей
	 */
	@JsonIgnore
	private Map<String, Integer> statusesCount;
	/**
	 * Значение использованого хипа JVM, байт
	 */
	private long heapUsed;
	/**
	 * Значение аллоцированого хипа JVM, байт
	 */
	private long heapMax;
	/**
	 * Список групп, где встречается этот узел
	 */
	@JsonIgnore
	private List<WildFlyGroup> groups;


	/**
	 * Удаленные модули с данного узла
	 */
	private List<Module> oldModules;

	/**
	 * Java env props, нужно для получения zone
	 */
	@JsonIgnore
	private Map<String, String> envProps;

	/**
	 * MMT ZONE
	 */
	private String groupID;

	/**
	 * WF ver
	 */
	private String wildflyVersion;

	/**
	 * Как wf сервер себя самоидентифицирует
	 */
	private String jbossHostName;

	/**
	 * Только что добавили или уже была?
	 */
	@JsonIgnore
	private Boolean newNode;

	/**
	 * Данные по метрикам тредпулов
	 */
	private List<ThreadPoolMetric> threadPoolMetrics;

	/**
	 * System props
	 */
	@JsonIgnore
	private List<InventorizationProperty> systemProps;

	private Map<String, String> processedProps;

	private Map<String, String> inputArgs;
	/**
	 * Дебаг информация, показывает зависшие management задачи на флае
	 */
	private String badOpsJson;
	/**
	 * Дебаг информация, показывает активные management задачи на флае
	 */
	private String activeOpsJson;

	private String vmName;

	public WildFlyNode() {
		super();
		setGroups(new ArrayList<>());
		setSystemProps(new ArrayList<>());
		setOldModules(new ArrayList<>());
		setEnvProps(new HashMap<>());
		setThreadPoolMetrics(new ArrayList<>());
		setProcessedProps(new HashMap<>());
		setInputArgs(new HashMap<>());
		setStatusesCount(new HashMap<>());
		setOldModules(new ArrayList<>());
		setPeriod(30);
	}


	public void setThreadPoolMetrics(List<ThreadPoolMetric> serverThreadPoolMetrics) {
		this.threadPoolMetrics = serverThreadPoolMetrics;
	}

	private void setJbossHostNameFromEnv() {
		if (envProps != null && envProps.get("jboss.qualified.host.name") != null) {
			setJbossHostName(envProps.get("jboss.qualified.host.name"));
		}
	}

	/**
	 * Поиск модуля, который может находится в {@link #modules}
	 *
	 * @param name        deploymentName
	 * @param runtimeName имя исполняемого файла
	 * @return null если модуля, соответстующего данным критериям нет
	 */
	public Module getModule(String name, String runtimeName) {
		if (name != null && runtimeName != null) {
			for (Module m : modules) {
				if (m.getName().equals(name) && m.getRuntimeName().equals(runtimeName)) {
					return m;
				}
			}
		}
		return null;
	}

	/**
	 * Поиск модуля, который может находится в {@link #modules}
	 *
	 * @param name        deploymentName
	 * @param runtimeName имя исполняемого файла
	 * @param hash        хеш сумма модуля
	 * @return null если модуля, соответстующего данным критериям нет
	 */
	public Module getModule(String name, String runtimeName, String hash) {
		if (hash != null && name != null && runtimeName != null) {
			for (Module m : modules) {
				if (m.getName().equals(name) && m.getRuntimeName().equals(runtimeName) && m.getHash().equals(hash)) {
					return m;
				}
			}
		}
		return null;
	}

	/**
	 * Кастомный сеттер, если задать {@link #envProps}, автоматически задаются {@link #jbossHostName} и {@link #groupID}
	 *
	 * @param envPropsMap полученные java-env-props
	 */
	public void setEnvProps(Map<String, String> envPropsMap) {
		if (this.envProps != null) {
			if (this.envProps.size() > 0 && envPropsMap == null) {
				return;
			}
		}
		this.envProps = envPropsMap;
		setJbossHostNameFromEnv();
		setGroupIdFromEnv();
		setVmNameFromEnv();
	}

	/**
	 * Сетер для {@link #vmName}, поиск данных по {@link #envProps}
	 */
	private void setVmNameFromEnv() {
		if (envProps != null && envProps.get("java.vm.name") != null) {
			setVmName(envProps.get("java.vm.name"));
		}
	}

	/**
	 * Сетер для {@link #groupID}, поиск данных по {@link #envProps}
	 */
	private void setGroupIdFromEnv() {
		if (getEnvProps() != null) {
			if (getEnvProps().get("group.id") != null) {
				if ("".equals(getEnvProps().get("group.id"))) {
					setGroupID("&lt;empty&gt;");
				} else {
					setGroupID(getEnvProps().get("group.id"));
				}
			} else {
				setGroupID("&lt;not set&gt;");
			}
		} else {
			setGroupID("&lt;not set&gt;");
		}
	}


	/**
	 * Вычисление статусов работы модулей, для отправки в influx
	 *
	 * @return мапа key - имя статуса (OFF, OK, FAILED), value- количество модулей
	 */
	public Map<String, Integer> calculateStatuses() {
		if (statusesCount == null) {
			statusesCount = new HashMap<>();
		}
		for (String status : statuses) {
			statusesCount.put(status, 0);
		}
		for (Module m : modules) {
			for (String status : statuses) {
				if (m.getStatus().equals(status)) {
					statusesCount.replace(status, statusesCount.get(status) + 1);
				}
			}
		}
		int all = 0;
		statusesCount.replace("ALL", 0);
		for (Integer value : statusesCount.values()) {
			all += value;
		}
		statusesCount.put("ALL", all);
		return statusesCount;
	}




	public  WildFlyNode connectInformation() {
		 WildFlyNode res = new  WildFlyNode();
		res.setDomainName(getDomainName());
		res.setPort(getPort());
		res.setIp(getIp());
		return res;
	}


	public void findIp() {
		if (domainName != null) {
			String result;
			try {
				String ip = InetAddress.getByName(domainName).getHostAddress();
				setIp(ip);
			} catch (UnknownHostException e) {
				log.error("Cant get IP", e);
			}
		}
	}

	@Override
	public String toString() {
		return "DomainName: " + domainName;
	}


	public  WildFlyNode copy(Boolean withOverlays) {
		 WildFlyNode clone = new  WildFlyNode();
		NodeWithModules.copy(this, clone, withOverlays);
		clone.setHeapUsed(getHeapUsed());
		clone.setHeapMax(getHeapMax());
		clone.setGroupID(getGroupID());
		clone.setWildflyVersion(getWildflyVersion());
		clone.setJbossHostName(getJbossHostName());
		clone.setNewNode(getNewNode());
		clone.setBadOpsJson(getBadOpsJson());
		clone.setActiveOpsJson(getActiveOpsJson());
		clone.setVmName(getVmName());
		if (statusesCount.size() > 0) {
			for (Map.Entry<String, Integer> entry : statusesCount.entrySet()) {
				Integer intValue = null;
				if (entry.getValue() != null) {
					intValue = entry.getValue();
				}
				clone.getStatusesCount().put(entry.getKey(), intValue);
			}
		}
		/*for (Module prev : previousModules) {
			clone.getPreviousModules().add(prev.copy(clone));
		}*/
		for (Module old : oldModules) {
			clone.getOldModules().add(old.copy(clone, withOverlays));
		}
		//не уверен что нужно
		//clone.setMetadata(getMetadata());

		if (envProps.size() > 0) {
			for (Map.Entry<String, String> entry : envProps.entrySet()) {
				clone.getEnvProps().put(entry.getKey(), entry.getValue());
			}
		}

		for (InventorizationProperty inventorizationProperty : systemProps) {
			clone.getSystemProps().add(inventorizationProperty.copy());
		}

		if (processedProps.size() > 0) {
			for (Map.Entry<String, String> entry : processedProps.entrySet()) {
				clone.getProcessedProps().put(entry.getKey(), entry.getValue());
			}
		}
		if (inputArgs.size() > 0) {
			for (Map.Entry<String, String> entry : inputArgs.entrySet()) {
				clone.getInputArgs().put(entry.getKey(), entry.getValue());
			}
		}

		return clone;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		WildFlyNode other = (WildFlyNode) obj;
		if (this.port == null) {
			this.port = DEFAULT_PORT;
		}
		if (other.port == null) {
			other.setPort(DEFAULT_PORT);
		}
		return domainName.equals(other.domainName) && port.equals(other.getPort());
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), port);
	}

	public String toXmlString() {
		String indent = "\t\t";
		StringBuilder sb = new StringBuilder();
		sb.append(indent);
		sb.append("<nodes domainName=\"");
		sb.append(domainName);
		sb.append("\" port=\"");
		sb.append(port);
		sb.append("\" ");
		if (getUser() != null) {
			sb.append("user=\"");
			sb.append(getUser());
			sb.append("\" ");
		}
		if (getPassword() != null) {
			sb.append("password=\"");
			sb.append(getPassword());
			sb.append("\" ");
		}
		sb.append("/>\n");
		return sb.toString();
	}
}
