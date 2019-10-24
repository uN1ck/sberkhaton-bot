package com.example.demo.csm.data.management;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Objects;
import java.util.UUID;

/**
 * От этого класса екстендятся все команды
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Command implements Cloneable {

	protected long startTime;
	/**
	 * Тип команды в строковом виде, может быть "start" или "stop"
	 */
	protected String command;
	/**
	 * Пользователь, который инициировал команду, получаем с помощью spring security
	 */
	protected String username;
	/**
	 * UUID узла в понимании CSM
	 */
	protected UUID nodeUUID;
	/**
	 * Хостнейм
	 */
	protected String domainName;
	/**
	 * Порт
	 */
	protected Integer port;
	/**
	 * Уникальный идентификатор команды
	 */
	protected UUID commandUUID;

	public Command() {
	}

	@Override
	public Command clone() {
		try {
			return (Command) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Command command1 = (Command) o;
		return command.equals(command1.command) &&
				nodeUUID.equals(command1.nodeUUID) && startTime == command1.startTime;
	}

	@Override
	public int hashCode() {
		return Objects.hash(command, nodeUUID, startTime);
	}
}
