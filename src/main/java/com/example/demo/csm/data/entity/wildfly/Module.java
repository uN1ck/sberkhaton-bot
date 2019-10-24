package com.example.demo.csm.data.entity.wildfly;

import com.example.demo.csm.data.entity.Node;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Отображение модуля WF
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public class Module {
	/**
	 * Паттерн для цифорок при вычислении версии
	 */
	private static final Pattern DIGIT_P = Pattern.compile("[0-9]");
	/**
	 * Паттерн для маленьких латинских букв
	 */
	private static final Pattern SMALL_LATIN_LETTER_P = Pattern.compile("[a-z]");


	/**
	 * деплоймент нейм из wf
	 */
	private String name;
	/**
	 * имя исполняемого файла из wf
	 */
	private String runtimeName;
	/**
	 * включен ли модуль
	 */
	private String enabled;
	/**
	 * В каком статусе прибывает
	 */
	private String status;
	/**
	 * Не используется
	 * Todo safe delete this field
	 */
	private boolean pending;
	/**
	 * Когда последний раз обновлялась инфа по данному модулю
	 */
	private Date updatedAt;
	/**
	 * Хеш модуля
	 */
	private String hash;


	/**
	 * Обратный линк на узел
	 */
	@JsonIgnore
	private Node node;
	/**
	 * Версия
	 */
	private String version;
	/**
	 * Нормализованая версия (отрезано все непотребство от runtimeName)
	 */
	private String normalName;
	/**
	 * Когда модуль в последний раз стартован, пока не используется
	 */
	private Date startedAt;
	/**
	 * История изменений состояний модуля
	 */
	private List<ModuleState> stateHistory;
	/**
	 * Нужно ли игнорировать проблемы по данному модулю
	 */
	private boolean ignoreIssues;

	/**
	 * ядровой ли модуль
	 */
	private boolean coreModule;
	/**
	 * Список оверлеев, в которых задействован данный модуль
	 */
	private List<String> usedOverlay;
	/**
	 * Список подмодулей, которые входят в данный модуль
	 */
	private List<String> subdeployments;

	public Module() {
		this(null, null, null, null, false, null, null, null, null, null, null);
	}



	/**
	 * Инициализация модуля из всех доступных параметров
	 */
	public Module(String name, String runtimeName, String enabled, String status, boolean pending, Date updatedAt, String hash, Node node, String version, String normalName, Date startedAt) {
		setName(name);
		setRuntimeName(runtimeName);
		setEnabled(enabled);
		setStatus(status);
		setPending(pending);
		setUpdatedAt(updatedAt);
		setHash(hash);
		setNode(node);
		setVersion(version);
		setNormalName(normalName);
		setStartedAt(startedAt);
		setStateHistory(new ArrayList<>());
		if (stateHistory != null) {
		}
		/*	setThreadPools(new ArrayList<>());*/
		setUsedOverlay(new ArrayList<>());
		setSubdeployments(new ArrayList<>());
	}



	public void setHash(String hash) {
		this.hash = hash;
	}

	/**
	 * В момент присваинвания этого поля так же происходит вычисление версии
	 *
	 * @param runtimeName исполняемый файл
	 */
	public void setRuntimeName(String runtimeName) {
		this.runtimeName = runtimeName;
		if (version == null && runtimeName != null) {
			setVersion(extraxtVersion());
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRuntimeName(String runtimeName, boolean ignoreExtractor) {
		if (ignoreExtractor) {
			this.runtimeName = runtimeName;
		}
	}


	/**
	 * Функция вычисления версии модуля из {@link #runtimeName}
	 *
	 * @return строка с версией
	 */
	private String extraxtVersion() {
		String res = "";
		int longPos = -1;
		//String findRuntimeName = runtimeName.toLowerCase();
		int lastDot = runtimeName.lastIndexOf('.');
		for (int i = 0; i < runtimeName.length() - 2; i++) {
			if (
					runtimeName.charAt(i) == '-' && (runtimeName.charAt(i + 1) == 'D' || runtimeName.charAt(i + 1) == 'P') &&
							((runtimeName.charAt(i + 2) == '-') || DIGIT_P.matcher(runtimeName.charAt(i + 2) + "").find())) {
				longPos = i;
				break;
			} else if ((runtimeName.charAt(i) == '-') && DIGIT_P.matcher(runtimeName.charAt(i + 1) + "").find() &&
					!SMALL_LATIN_LETTER_P.matcher(runtimeName.charAt(i + 2) + "").find()) {
				longPos = i;
				break;
			}
		}

		if ((longPos != -1) && (lastDot != -1)) {
			res = runtimeName.substring(longPos + 1, lastDot);
			if (res.charAt(0) == 'D') {
				res = 'D' + res.substring(1);
			}
			if (res.charAt(0) == 'P') {
				res = 'P' + res.substring(1);
			}
		}

		if (res.isEmpty()) {
			res = "N/a";
			if (lastDot != -1) {
				normalName = runtimeName.substring(0, lastDot);
			}
		} else {
			normalName = runtimeName.substring(0, longPos);
		}
		return res;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		Module other = (Module) obj;
		if (hash != null && runtimeName != null && other.hash != null && other.runtimeName != null) {
			return name.equals(other.name) && runtimeName.equals(other.runtimeName) && hash.equals(other.hash);
		} else {
			return name.equals(other.name);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, runtimeName, hash);
	}

	public Module copy(Node parent, Boolean withOverlays) {
		Module clone = new Module();
		clone.setName(getName());
		clone.setRuntimeName(getRuntimeName());
		clone.setNormalName(getNormalName());
		clone.setEnabled(getEnabled());
		clone.setStatus(getStatus());
		clone.setPending(isPending());
		if (getUpdatedAt() != null) {
			clone.setUpdatedAt(new Date(getUpdatedAt().getTime()));
		}
		clone.setHash(getHash());
		clone.setVersion(getVersion());
		if (getStartedAt() != null) {
			clone.setStartedAt(new Date(getStartedAt().getTime()));
		}
		clone.setIgnoreIssues(isIgnoreIssues());
		clone.setCoreModule(isCoreModule());
		if (withOverlays) {
			for (String usedOverlay : usedOverlay) {
				clone.getUsedOverlay().add(usedOverlay);
			}
		}
		for (String subdeployment : subdeployments) {
			clone.getSubdeployments().add(subdeployment);
		}
		//ссылка
		clone.setNode(parent);
		for (ModuleState ms : stateHistory) {
			clone.getStateHistory().add(ms.copy());
		}
		return clone;
	}

}
