package com.example.demo.csm.data.management;

import lombok.Data;

import java.util.Objects;

/**
 * Отдельный тип команд для рестарта/стопа jvm
 */
@Data
public class CommandRestartNode extends Command {

	public static final String AUDIT_EVENT_ID = "node_restart_wf_command";
	public static final String AUDIT_EVENT_RESULT_ID = "node_restart_wf_command_result";

	/**
	 * Нужно ли рестартовать jvm wf или просто остановить
	 */
	private boolean restart;

	public CommandRestartNode() {
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
		CommandRestartNode that = (CommandRestartNode) o;
		return restart == that.restart;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), restart);
	}

	/**
	 * Кастомный иквалс для определения уникальна ли команда
	 *
	 * @param obj obj
	 * @return t/f
	 */
	/*@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CommandRestartNode)) {
			return false;
		}
		CommandRestartNode other = (CommandRestartNode) obj;

		return command.equals(other.getCommand()) && nodeUUID.equals(other.getNodeUUID()) && restart == other.isRestart();
	}*/
	@Override
	public String toString() {
		return String.format("[node] %s:%d [username] %s [operation] %s  [restart] %b",
				domainName, port, username, command, restart);
	}

	@Override
	public CommandRestartNode clone() {
		return (CommandRestartNode) super.clone();
	}
}
