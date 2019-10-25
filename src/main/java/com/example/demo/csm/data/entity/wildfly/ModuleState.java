package com.example.demo.csm.data.entity.wildfly;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Date;

/**
 * Изменение статуса модуля
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModuleState {
    private Integer id;
    /**
     * Статус в текстовом виде {@link Module#getEnabled()}:{@link Module#getStatus()}
     */
    private String state;
    /**
     * Время зафиксированого изменения
     */
    private Date time;

    public ModuleState() {
    }

    public ModuleState(Integer id, String state, Date time) {
        this.id = id;
        setState(state);
        setTime(time);
    }

    public ModuleState copy() {
        Integer intValue = null;
        if (getId() != null) {
            intValue = getId().intValue();
        }
        ModuleState clone = new ModuleState(intValue, getState(), new Date(getTime().getTime()));
        return clone;
    }
}
