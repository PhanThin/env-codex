/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.com.viettel.gencode.dao;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Lop quan ly ket noi toi dataVoffice 1.0
 *
 * @author datnv5
 */
public class BaseDataDao {

    /**
     * loger for this class
     */
    private static final Logger LOGGER = LogManager.getLogger(BaseDataDao.class);
    /**
     * Ket noi den DB
     */
    protected Connection conn = null;

    /**
     * Phuong thuc khoi tao
     */
    public BaseDataDao() {
    }

    public Connection openConnection() {
        Datasource ds = Datasource.getInstance();
        conn = ds.getConnection();
        return conn;
    }

    /**
     * Close a connection avoid closing if null and
     */
    public void closeConnection() {
        if ((conn != null)) {
            try {
                conn.close();
                conn = null;
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                LOGGER.error(e);
            }
        }
    }
}
