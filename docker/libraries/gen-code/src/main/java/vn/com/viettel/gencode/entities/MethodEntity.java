/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.com.viettel.gencode.entities;

import vn.com.viettel.gencode.utils.FunctionCommon;

import java.util.List;

/**
 * @author datnv5
 */
public class MethodEntity {

    String name;
    String type;
    String value;
    String sql;
    String desc;
    List<String> params;
    Integer count;

    public Boolean getJpa() {
        return jpa;
    }

    public void setJpa(Boolean jpa) {
        this.jpa = jpa;
    }

    Boolean jpa;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSql() {
        sql = sql.replace("\n", "").replace("\\n", "");
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getDesc() {
        return FunctionCommon.removeAccent(desc);
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String toString(){
        String result = "name = " + name +
                ",type = " + type +
                ",value = " + value +
                ",desc = " + desc +
                ",count = " + count;
        if (params != null) {
            result += ",params = " + String.join(" | ", params);
        }
        return result;
    }
}
