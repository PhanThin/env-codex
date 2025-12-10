/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.com.viettel.gencode.dao;

/**
 * @author datnv5
 */

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vn.com.viettel.gencode.utils.FunctionCommon;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class Datasource {

    private static final Logger LOGGER = LogManager.getLogger(Datasource.class);
    private static Datasource instance;
    private Connection conn = null;
    /**
     * A singleton that represents a pooled datasource. It is composed of a C3PO
     * pooled datasource. Can be changed to any connect pool provider
     */
    private Properties props;

    private Datasource() {
        try {
            props = FunctionCommon.readFileProperties("config.properties");
            String datasourceUrl = "";
            String userName = "";
            String password = "";
            String driverName = "";
            if (props != null && !props.isEmpty()) {
                driverName = props.getProperty("spring.datasource.driver-class-name");
                datasourceUrl = props.getProperty("spring.datasource.url");
                userName = props.getProperty("spring.datasource.username");
                password = props.getProperty("spring.datasource.password");
            }
            Class.forName(driverName);
            conn = DriverManager.getConnection(datasourceUrl, userName, password);
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    public static synchronized Datasource getInstance() {
        if (instance != null && instance.getConnection() != null) {
            return instance;
        }
        instance = new Datasource();
        return instance;
    }

    public Connection getConnection() {
        return conn;
    }
}
