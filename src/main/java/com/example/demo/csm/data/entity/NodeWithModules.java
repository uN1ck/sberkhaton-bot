package com.example.demo.csm.data.entity;

import com.example.demo.csm.data.entity.wildfly.Module;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeWithModules extends Node {

	/**
	 * Список модулей этого узла
	 */
	protected List<Module> modules;

	public NodeWithModules() {
		super();
		setModules(new ArrayList<>());
	}

	@Override
	public String toString() {
		return "DomainName: " + domainName;
	}


	public static <T extends  NodeWithModules> void copy(T from, T to, Boolean withOverlays) {
		Node.copy(from, to);
		for (Module m : from.getModules()) {
			to.getModules().add(m.copy(to, withOverlays));
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode());
	}
}
