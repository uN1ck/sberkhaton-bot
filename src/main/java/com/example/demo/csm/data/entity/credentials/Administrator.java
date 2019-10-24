package com.example.demo.csm.data.entity.credentials;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.util.Objects;

/**
 * Отображение в рамках csm администраторов чего бы то ни было
 */
@Data
@XmlAccessorType(XmlAccessType.NONE)
@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
public class Administrator {

	private String email;
	private String fullName;

	private boolean notificationsEnabled;
	@JsonIgnore
	private String user;

	@JsonIgnore
	private String password;

	@XmlAttribute
	public String getEmail() {
		return email;
	}

	@XmlAttribute
	public boolean isNotificationsEnabled() {
		return notificationsEnabled;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@XmlAttribute
	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	@XmlAttribute(name = "username")
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	@XmlAttribute
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return String.format("Administrator(fullname=\"%s\", email=%s, user=\"%s\")", fullName, email, user);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Administrator that = (Administrator) o;
		return email.equals(that.email) &&
				fullName.equals(that.fullName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(email, fullName);
	}

	public Administrator copy() {
		Administrator clone = new Administrator();
		clone.setEmail(getEmail());
		clone.setFullName(getFullName());
		clone.setPassword(getPassword());
		clone.setUser(getUser());
		clone.setNotificationsEnabled(isNotificationsEnabled());
		return clone;
	}
}
