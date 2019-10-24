package com.example.demo.csm.data.entity.wildfly;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Data
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrintName {
	private String groupName;
	private String nodeName;


	public PrintName() {
	}

	public PrintName(String groupName, String nodeName) {
		this.groupName = groupName;
		this.nodeName = nodeName;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		PrintName other = (PrintName) obj;
		return getGroupName().equals(other.getGroupName()) && getNodeName().equals(other.getNodeName());
	}

	@Override
	public int hashCode() {
		return Objects.hash(groupName, nodeName);
	}

	public PrintName copy() {
		PrintName clone = new PrintName(getGroupName(), getNodeName());
		return clone;
	}
}
