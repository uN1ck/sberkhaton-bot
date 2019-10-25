package com.example.demo.csm.data.management;

import lombok.Data;

import java.util.Objects;

/**
 * Команда связаная с модулем, имеет свои отличия от базовой команды
 */
@Data
public class CommandModule extends Command {

	public static final String AUDIT_EVENT_ID = "module_command";
	public static final String AUDIT_EVENT_RESULT_ID = "module_command_result";

	/**
	 * Имя деплоймента в понимании WF
	 */
	private String deploymentName;
	/**
	 * Должна ли эта команда исполняться последовательно или паралельно
	 */
	private boolean cascade;

	public CommandModule() {
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		CommandModule that = (CommandModule) o;
		return super.equals(that) && deploymentName.equals(that.deploymentName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), deploymentName);
	}


	@Override
	public String toString() {
		return String.format("[node] %s:%d [deploymentName] %s  [username] %s [operation] %s [cascade] %b",
				domainName, port, deploymentName, username, command, cascade);
	}

	@Override
	public CommandModule clone() {
		return (CommandModule) super.clone();
	}

}
