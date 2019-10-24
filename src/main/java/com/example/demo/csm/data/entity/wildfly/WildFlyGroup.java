package com.example.demo.csm.data.entity.wildfly;

import com.example.demo.csm.data.entity.Cluster;
import com.example.demo.csm.data.entity.Group;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class WildFlyGroup extends Group {

	@JsonIgnore
	private Boolean newGroup;

	/**
	 * Список узлов, входящих в группу
	 */
	protected List<WildFlyNode> nodes;


	protected List<String> optsOfCurrentGroup;

	protected List<String> argsOfCurrentGroup;

	public WildFlyGroup() {
		super();
		this.nodes = new ArrayList<>();
		optsOfCurrentGroup = new ArrayList<>();
		argsOfCurrentGroup = new ArrayList<>();
	}

	/**
	 * Высчитывание модулей текущей группы
	 */
	public void calculateModulesOfCurrentGroup() {
		List<Module> result = new ArrayList<>();
		for (WildFlyNode n : nodes) {
			if (n != null) {
				for (Module m : n.getModules()) {
					if (!result.contains(m)) {
						result.add(m);
					}
				}
			}
		}
		modulesOfCurrentGroup = result;
	}




	@XmlElement
	public List<WildFlyNode> getNodes() {
		return nodes;
	}


	public String toXmlString() {
		Character indent = '\t';
		StringBuilder sb = new StringBuilder();
		sb.append(indent);
		sb.append("<groups name=\"");
		sb.append(getName());
		sb.append("\" ");
		if (getUser() != null) {
			sb.append("user =\"");
			sb.append(getUser());
			sb.append("\" ");
		}
		if (getPassword() != null) {
			sb.append("password =\"");
			sb.append(getPassword());
			sb.append("\" ");
		}
		sb.append(">\n");
		for (WildFlyNode node : nodes) {
			sb.append(node.toXmlString());
		}
		sb.append(indent);
		sb.append("</groups>\n");
		return sb.toString();
	}

	public WildFlyNode getNode(WildFlyNode nodeToFind) {
		if (nodeToFind.getIp() == null) {
			nodeToFind.findIp();
		}
		for (WildFlyNode node : nodes) {
			if (node.getDomainName().equals(nodeToFind.getDomainName()) || nodeToFind.getDomainName().equals(node.getIp())) {
				if (node.getPort().equals(nodeToFind.getPort()))
					return node;
			}
		}
		return null;
	}

	public WildFlyGroup copy(Cluster parent) {
		WildFlyGroup superClone = new WildFlyGroup();
		superClone.setCluster(parent);
		superClone.setUser(getUser());
		superClone.setPassword(getPassword());
		superClone.setName(getName());
		superClone.setType(getType());
		if (getShowOrder() != null) {
			superClone.setShowOrder(getShowOrder().intValue());
		}
		if (getIgnoreIssues() != null) {
			superClone.setIgnoreIssues(getIgnoreIssues().booleanValue());
		}
		superClone.setInventoryPrefix(getInventoryPrefix());
		superClone.setInventoryName(getInventoryName());
		for (Module m : modulesOfCurrentGroup) {
			superClone.getModulesOfCurrentGroup().add(m.copy(null, true));
		}
		if (getNewGroup() != null) {
			superClone.setNewGroup(getNewGroup().booleanValue());
		}
		for (String opt : optsOfCurrentGroup) {
			superClone.getOptsOfCurrentGroup().add(opt);
		}
		for (String arg : argsOfCurrentGroup) {
			superClone.getArgsOfCurrentGroup().add(arg);
		}
		//в кластере уже есть ноделист, мы просто линкуем
		for (WildFlyNode node : nodes) {
			WildFlyNode clonedNode = superClone.getCluster().getNodeByUUID(node.getUuid());
			clonedNode.getGroups().add(superClone);
			superClone.getNodes().add(clonedNode);
		}
		return superClone;
	}
}
