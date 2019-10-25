package com.example.demo.csm.data.management;

import lombok.Data;

import java.util.Date;

/**
 * Результат выполнения команды, содержит в себе саму команду
 */
@Data
public class CommandResult implements Comparable<CommandResult> {

	/**
	 * Команда
	 */
	private Command command;
	/**
	 * Статус исполнения команды
	 */
	private String status;
	/**
	 * Затраченое время
	 */
	private long time;
	/**
	 * Время начала исполнения
	 */
	private Date startTime;

	public CommandResult() {
	}

	public CommandResult(Command command) {
		this.command = command;
	}

	/**
	 * Кастомный compareTo для сортировки, используется для правильного отображения в jsp
	 *
	 * @param o other
	 * @return на сколько больше или меньше
	 */
	@Override
	public int compareTo(CommandResult o) {
		int compare;
		if (o == null) {
			return -1;
		}
		if ("pending".equals(status) && !"pending".equals(o.status)) {
			compare = -1;
		} else if (!"pending".equals(status) && "pending".equals(o.status)) {
			compare = 1;
		} else {
			compare = 0;
		}
		if (compare == 0) {
			if (startTime.getTime() > o.startTime.getTime()) {
				compare = -1;
			} else if (startTime.getTime() < o.startTime.getTime()) {
				compare = 1;
			}
		}
		return compare;
	}

	@Override
	public String toString() {
		return String.format("[status] %s [time] %d [command] [ %s ]", status, time, command.toString());
	}
}
