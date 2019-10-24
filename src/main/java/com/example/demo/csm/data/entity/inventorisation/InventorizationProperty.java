package com.example.demo.csm.data.entity.inventorisation;

import lombok.Data;

import java.util.Objects;

/**
 * Отображение одной KV проперти
 */
@Data
public class InventorizationProperty {
	private String name;
	private String value;


	public InventorizationProperty() {

	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		InventorizationProperty that = (InventorizationProperty) o;
		return name.equals(that.name) &&
				value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, value);
	}

	public InventorizationProperty copy() {
		InventorizationProperty clone = new InventorizationProperty();
		clone.setName(getName());
		clone.setValue(getValue());
		return clone;
	}
}
