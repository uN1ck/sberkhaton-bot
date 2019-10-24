package com.example.demo.csm.data.management;

import com.example.demo.csm.data.entity.wildfly.Module;
import lombok.Data;

import java.util.List;

/**
 * Команда связаная с целым узлом wf.
 * Все запуски или остановки в рамках данной команды будут исполняться паралельно если задан batch
 */
@Data
public class CommandNode extends Command {

	public static final String AUDIT_EVENT_ID = "node_command";
	public static final String AUDIT_EVENT_RESULT_ID = "node_command_result";
	/**
	 * Паралельное или последовательное исполнение команд
	 */
	private boolean batch;
	/**
	 * Список модулей, с которыми необходимо произвести действия
	 */
	private List<Module> modules;

	public CommandNode() {
	}

	@Override
	public String toString() {
		return String.format("[node] %s:%d [username] %s [operation] %s [batch] %b [deployments] %s "
				, domainName, port, username, command, batch, modules.toString());
	}

	@Override
	public CommandNode clone() {
		return (CommandNode) super.clone();
	}
}
