/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.com.viettel.gencode.entities;

import lombok.Data;
import vn.com.viettel.gencode.utils.FunctionCommon;

import java.util.List;

/**
 * @author datnv5
 */
@Data
public class ObjectEntity {

    String className;

    List<MethodEntity> listMethod;

    String desc;

    public String toString() {
        String result = "className = " + className + ",desc = " + desc;
        if (listMethod != null) {
            result += ",listMethod= = " + listMethod;
        }
        return result;
    }
}
