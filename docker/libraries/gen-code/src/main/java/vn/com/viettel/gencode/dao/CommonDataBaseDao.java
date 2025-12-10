/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.com.viettel.gencode.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vn.com.viettel.gencode.entities.VariableEntity;
import vn.com.viettel.gencode.utils.FunctionCommon;
import vn.com.viettel.gencode.utils.Constants;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Thuc hien thao tac du lieu tren voffice 1.0
 *
 * @author datnv5
 */
public class CommonDataBaseDao extends BaseDataDao {

    private static final Logger LOGGER = LogManager.getLogger(CommonDataBaseDao.class);

    /**
     * 1. Thuc hien query lay ra bang du lieu theo dieu kien
     *
     * @param queryString : cau lenh sql
     * @param arrParams:  hashmap dieu kien
     * @param startPage
     * @param pageLoad
     * @param classOfT
     * @return Object: trả về đối tượng theo class đẩy vào
     */
    public List<? extends Object> executeSqlGetListObjOnCondition(StringBuilder queryString,
                                                                  List<Object> arrParams, Long startPage, Long pageLoad, Class<?> classOfT) {
        List<Object> resultObj = new ArrayList<>();
        String tg = "";
        try {
            Field[] fieldList = classOfT.getDeclaredFields();
            StringBuilder sqlPage = new StringBuilder();
            if (startPage != null && pageLoad != null) {
                sqlPage.append(" SELECT * FROM ( SELECT a.*, rownum r__  FROM (");
                sqlPage.append(queryString.toString());

                sqlPage.append(" ) a ) ");
                sqlPage.append(String.format(" WHERE r__ > %d", startPage));
                sqlPage.append(String.format(" and r__ <= %d", startPage + pageLoad));
            } else {
                sqlPage = queryString;
            }
            conn = openConnection();
            try (PreparedStatement updateSqlStatement = conn.prepareStatement(sqlPage.toString())) {

                if (arrParams != null && arrParams.size() > 0) {
                    //add dieu kien vao cau lenh sql
                    for (int i = 0; i < arrParams.size(); i++) {
                        Object object = arrParams.get(i);
                        updateSqlStatement.setObject(i + 1, object);
                    }
                }
                try (ResultSet rs = updateSqlStatement.executeQuery()) {

                    while (rs.next()) {
                        JsonObject datasetItem = new JsonObject();
                        for (Field field : fieldList) {
                            //thuc hien add du lieu vao json
                            String fileName = field.getName();
                            if (FunctionCommon.hasColumn(rs, fileName)) {
                                datasetItem.addProperty(fileName, rs.getString(fileName));
                            }
                        }
                        GsonBuilder builder = new GsonBuilder();
                        Gson gson = builder.create();
                        Object item = gson.fromJson(datasetItem, classOfT);
                        resultObj.add(item);
                    }
                }
            }
        } catch (SQLException | IllegalArgumentException | SecurityException e) {
            System.out.println(e.getMessage());
            LOGGER.error(e);
            LOGGER.error("Loi! SQL: " + tg);
        }
        return resultObj;
    }

    /**
     * thuc hien chen hoa update du lieu vao data base
     *
     * @param queryString
     * @param arrParams
     * @return
     */
    public Boolean insertOrUpdateDataBase(StringBuilder queryString,
                                          List<Object> arrParams) {
        boolean valueResult = false;
        try {
            conn = openConnection();
            try (PreparedStatement updateSqlStatement = conn.prepareStatement(queryString.toString())) {
                if (arrParams != null && arrParams.size() > 0) {
                    //add dieu kien vao cau lenh sql
                    for (int i = 0; i < arrParams.size(); i++) {
                        Object object = arrParams.get(i);
                        updateSqlStatement.setObject(i + 1, object);
                    }
                }

                int resultInsert = updateSqlStatement.executeUpdate();
                valueResult = resultInsert > 0;
            }
        } catch (SQLException | IllegalArgumentException | SecurityException e) {
            System.out.println(e.getMessage());
            LOGGER.error(e);
            LOGGER.error("Loi! SQL: " + queryString);
        } finally {
            closeConnection();
        }
        return valueResult;
    }

    /**
     * thuc hien chen du lieu theo lo vao data base
     *
     * @param sqlInsert
     * @param listArrMapParams
     * @return
     */
    public Boolean insertDataBaseBlock(StringBuilder sqlInsert, List<HashMap<String, Object>> listArrMapParams) {
        boolean valueResult = false;

        if (sqlInsert != null && sqlInsert.toString().trim().length() > 0) {
            try {
                conn = openConnection();
                String sqlExcute = sqlInsert.toString();

                //thuc hien sap xep key tu khoa theo thu tu
                if (listArrMapParams != null && listArrMapParams.size() > 0) {
                    HashMap<String, Object> arrMapParams = listArrMapParams.get(0);
                    List<String> resultKey = getOrderKeyParamsSql(sqlInsert.toString(), arrMapParams);
                    for (String s : resultKey) {
                        String keyReplace = String.format(Constants.STR_SQL_PARAM,
                                s).trim();
                        //lay gia tri can chen theo tu khoa
                        sqlExcute = sqlExcute.replace(keyReplace, Constants.STR_SQL_ASK_PARAM);
                    }
                    try (PreparedStatement excuteSqlStatement = conn.prepareStatement(sqlExcute)) {
                        for (HashMap<String, Object> itemArrMapParams : listArrMapParams) {
                            for (int i = 0; i < resultKey.size(); i++) {
                                Object object = itemArrMapParams.get(resultKey.get(i));
                                excuteSqlStatement.setObject(i + 1, object);
                            }
                            excuteSqlStatement.addBatch();
                        }

                        int[] resultInsert = excuteSqlStatement.executeBatch();
                        excuteSqlStatement.close();
                        conn.commit();
                        valueResult = true;
                        for (int j : resultInsert) {
                            if (j < 0 && j != -2) {
                                valueResult = false;
                                break;
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                try {
                    System.out.println(e.getMessage());
                    LOGGER.error(e);
                    LOGGER.error("Loi! SQL: " + sqlInsert);
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.error(ex);
                }
            } finally {
                closeConnection();
            }
        }
        return valueResult;
    }

    /**
     * Ham cu thuc hien dem count
     *
     * @param queryString
     * @param arrParams
     * @return
     */
    public Long executeSqlGetCountOnConditionListParams(StringBuilder queryString,
                                                        List<Object> arrParams) {
        Long resultValue = null;
        try {
            StringBuilder strBuild = new StringBuilder();
            strBuild.append("Select count(*) as count From (");
            strBuild.append(queryString);
            strBuild.append(")");
            conn = openConnection();
            try (PreparedStatement updateSqlStatement = conn.prepareStatement(strBuild.toString())) {
                if (arrParams != null && arrParams.size() > 0) {
                    //add dieu kien vao cau lenh sql
                    for (int i = 0; i < arrParams.size(); i++) {

                        Object object = arrParams.get(i);

                        updateSqlStatement.setObject(i + 1, object);

                    }
                }
                try (ResultSet rs = updateSqlStatement.executeQuery()) {
                    while (rs.next()) {
                        resultValue = rs.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            LOGGER.error(e);
            LOGGER.error("Loi! SQL: " + queryString);
        } finally {
            closeConnection();
        }
        return resultValue;
    }

    /**
     * Ham cu thuc hien lay gia tri trong Db
     *
     * @param queryString
     * @param arrParams
     * @return
     */
    public Object executeSqlGetValOnConditionListParams(StringBuilder queryString,
                                                        List<Object> arrParams) {
        Object resultValue = null;
        try {

            conn = openConnection();
            try (PreparedStatement updateSqlStatement = conn.prepareStatement(queryString.toString())) {
                if (arrParams != null && arrParams.size() > 0) {
                    //add dieu kien vao cau lenh sql
                    for (int i = 0; i < arrParams.size(); i++) {
                        Object object = arrParams.get(i);
                        updateSqlStatement.setObject(i + 1, object);
                    }
                }
                try (ResultSet rs = updateSqlStatement.executeQuery()) {
                    while (rs.next()) {
                        resultValue = rs.getObject(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            LOGGER.error(e);
            LOGGER.error("Loi! SQL: " + queryString);
        } finally {
            closeConnection();
        }
        return resultValue;
    }

    /**
     * Thuc thi procedure trong database
     *
     * @param sqlInsert
     * @param arrMapParams
     * @return
     */
    public Boolean callProcedureDataBase(StringBuilder sqlInsert, HashMap<String, Object> arrMapParams) {
        boolean valueResult = false;
        if (sqlInsert != null && sqlInsert.toString().trim().length() > 0) {
            try {
                conn = openConnection();
                String sqlExcute = sqlInsert.toString();

                //thuc hien sap xep key tu khoa theo thu tu
                if (arrMapParams != null && arrMapParams.size() > 0) {
                    List<String> resultKey = getOrderKeyParamsSql(sqlInsert.toString(), arrMapParams);
                    for (String s : resultKey) {
                        String keyReplace = String.format(Constants.STR_SQL_PARAM,
                                s).trim();
                        //lay gia tri can chen theo tu khoa
                        sqlExcute = sqlExcute.replace(keyReplace, Constants.STR_SQL_ASK_PARAM);
                    }
                    try (PreparedStatement excuteSqlStatement = conn.prepareCall(sqlExcute)) {
                        for (int i = 0; i < resultKey.size(); i++) {
                            Object object = arrMapParams.get(resultKey.get(i));
                            excuteSqlStatement.setObject(i + 1, object);
                        }
                        int resultInsert = excuteSqlStatement.executeUpdate();
                        valueResult = resultInsert >= 0;
                    }
                }
            } catch (SQLException e) {
                System.out.println(e.getMessage());
                LOGGER.error(e);
                LOGGER.error("Loi! SQL: " + sqlInsert);
                valueResult = false;
            } finally {
                closeConnection();
            }
        }
        return valueResult;
    }

    /**
     * thuc hien chen du lieu theo lo vao data base
     *
     * @param sqlInsert
     * @param listArrMapParams
     * @return
     */
    public Boolean insertDataBaseBloc(StringBuilder sqlInsert,
                                      List<HashMap<String, Object>> listArrMapParams) {
        boolean valueResult = false;

        if (sqlInsert != null && sqlInsert.toString().trim().length() > 0) {
            try {
                conn = openConnection();
                String sql = sqlInsert.toString();

                //thuc hien sap xep key tu khoa theo thu tu
                if (listArrMapParams != null && listArrMapParams.size() > 0) {
                    HashMap<String, Object> arrMapParams = listArrMapParams.get(0);
                    List<String> resultKey = getOrderKeyParamsSql(sqlInsert.toString(), arrMapParams);
                    for (String s : resultKey) {
                        String keyReplace = String.format(Constants.STR_SQL_PARAM,
                                s).trim();
                        //lay gia tri can chen theo tu khoa
                        sql = sql.replace(keyReplace, Constants.STR_SQL_ASK_PARAM);
                    }
                    int[] resultInsert;
                    try (PreparedStatement excuteSqlStatement = conn.prepareStatement(sql)) {
                        for (HashMap<String, Object> itemArrMapParams : listArrMapParams) {
                            for (int i = 0; i < resultKey.size(); i++) {
                                Object object = itemArrMapParams.get(resultKey.get(i));
                                excuteSqlStatement.setObject(i + 1, object);
                            }
                            excuteSqlStatement.addBatch();
                        }
                        resultInsert = excuteSqlStatement.executeBatch();
                    }
                    conn.commit();
                    valueResult = true;
                    for (int j : resultInsert) {
                        if (j < 0 && j != -2) {
                            valueResult = false;
                            break;
                        }
                    }
                }
            } catch (SQLException e) {
                try {
                    System.out.println(e.getMessage());
                    LOGGER.error(e);
                    conn.rollback();
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                    LOGGER.error(ex);
                }
            } finally {
                closeConnection();
            }
        }
        return valueResult;
    }


    /**
     * <b>Chen hoac cap nhat du lieu theo bloc</b><br>
     *
     * @param sqlInsert
     * @param listParams
     * @return
     */
    public synchronized Boolean insertDataByBloc(StringBuilder sqlInsert,
                                                 List<List<Object>> listParams) {
        boolean valueResult = false;
        if (sqlInsert != null && sqlInsert.toString().trim().length() > 0) {
            try {
                conn = openConnection();
                String sqlExcute = sqlInsert.toString();
                if (listParams != null && listParams.size() > 0) {
                    int[] resultInsert;
                    try (PreparedStatement excuteSqlStatement = conn.prepareStatement(sqlExcute)) {
                        for (List<Object> listChidlParams : listParams) {
                            for (int i = 0; i < listChidlParams.size(); i++) {
                                excuteSqlStatement.setObject(i + 1, listChidlParams.get(i));
                            }
                            excuteSqlStatement.addBatch();
                        }
                        excuteSqlStatement.setQueryTimeout(360);
                        resultInsert = excuteSqlStatement.executeBatch();
                    }
                    conn.commit();
                    valueResult = true;
                    for (int j : resultInsert) {
                        if (j < 0 && j != -2) {
                            valueResult = false;
                            break;
                        }
                    }
                }
            } catch (SQLException e) {
                try {
                    System.out.println(e.getMessage());
                    LOGGER.error(e);
                    conn.rollback();
                } catch (SQLException ex) {
                    System.out.println(ex.getMessage());
                    LOGGER.error(ex);
                }
            } finally {
                closeConnection();
            }
        }
        return valueResult;
    }

    /**
     * Thực hiện lấy ra thứ tự các key trong chuỗi sql để thay thế biến
     *
     * @param sqlQuery
     * @param arrMapParams
     * @return
     */
    private List<String> getOrderKeyParamsSql(String sqlQuery,
                                              HashMap<String, Object> arrMapParams) {
        List<String> result = new ArrayList<>();
        HashMap<Integer, Object> resultHmap = new HashMap<>();
        if (arrMapParams != null) {
            for (Entry<String, Object> entry : arrMapParams.entrySet()) {
                String key = String.format(Constants.STR_SQL_PARAM, entry.getKey().trim());
                int indexOfParams = sqlQuery.indexOf(key);
                resultHmap.put(indexOfParams, entry.getKey().trim());
            }
            result = FunctionCommon.sortHmap(resultHmap);
        }
        return result;
    }

    //========================================================================

    /**
     * thuc hien lay du lieu theo query
     *
     * @param queryString : cau lenh sql
     * @param arrParams:  hashmap dieu kien
     * @param startPage
     * @param pageLoad
     * @return Object: trả về đối tượng theo class đẩy vào
     */
    public List<List<Object>> executeSqlGetDataFromDatabase(StringBuilder queryString,
                                                            List<Object> arrParams, Long startPage, Long pageLoad) {
        List<List<Object>> resultObj = new ArrayList<>();
        try {
            StringBuilder sqlPage = new StringBuilder();
            if (startPage != null && pageLoad != null) {
                sqlPage.append(" SELECT * FROM ( SELECT a.*, rownum r__  FROM (");
                sqlPage.append(queryString.toString());
                sqlPage.append(String.format(" ) a WHERE rownum <= %d) ", startPage + pageLoad));
                sqlPage.append(String.format(" WHERE r__ > %d", startPage));
            } else {
                sqlPage = queryString;
            }

            conn = openConnection();
            try (PreparedStatement updateSqlStatement = conn.prepareStatement(sqlPage.toString())) {

                if (arrParams != null && arrParams.size() > 0) {
                    //add dieu kien vao cau lenh sql
                    for (int i = 0; i < arrParams.size(); i++) {
                        Object object = arrParams.get(i);
                        updateSqlStatement.setObject(i + 1, object);
                    }
                }
                try (ResultSet rs = updateSqlStatement.executeQuery()) {
                    boolean first = true;
                    List<Object> rowfield;
                    while (rs.next()) {
                        ResultSetMetaData rsmd = rs.getMetaData();
                        int columns = rsmd.getColumnCount();
                        List<Object> rowValue = new ArrayList<>();
                        rowfield = new ArrayList<>();
                        for (int x = 1; x <= columns; x++) {
                            //ten cot du lieu
                            String cl = rsmd.getColumnName(x);
                            if (first) {
                                rowfield.add(cl);
                            }
                            rowValue.add(rs.getString(cl));
                        }
                        if (first) {
                            resultObj.add(rowfield);
                        }
                        resultObj.add(rowValue);
                        first = false;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            LOGGER.error(e);
            return null;
        } finally {
            closeConnection();
        }
        return resultObj;
    }

    /**
     * @param queryString
     * @return
     */
    public String executeSql(StringBuilder queryString) {
        String valueResult = "";
        try {
            conn = openConnection();
            try (PreparedStatement updateSqlStatement = conn.prepareStatement(queryString.toString())) {
                valueResult = String.valueOf(updateSqlStatement.execute());
            }
        } catch (SQLException | IllegalArgumentException | SecurityException e) {
            System.out.println(e.getMessage());
            LOGGER.error(e);
            return null;
        } finally {
            closeConnection();
        }
        return valueResult;
    }

    /**
     * querydatabase
     *
     * @param sqlQuery
     * @param start
     * @param size
     * @return
     */
    public List<List<Object>> getDataQuery(String sqlQuery, Long start, Long size) {
        StringBuilder sql = new StringBuilder();
        sql.append(sqlQuery);
        List<Object> params = new ArrayList<>();
        CommonDataBaseDao cmd = new CommonDataBaseDao();
        return cmd.executeSqlGetDataFromDatabase(sql, params, start, size);
    }

    /**
     * querydatabase
     *
     * @param sqlQuery
     * @return
     */
    public String updateDataQuery(String sqlQuery) {
        StringBuilder sql = new StringBuilder();
        sql.append(sqlQuery);
        CommonDataBaseDao cmd = new CommonDataBaseDao();
        return cmd.executeSql(sql);
    }

    /**
     * thuc hien lay danh sach ten cac cot
     *
     * @param queryString
     * @return
     */
    public List<VariableEntity> getListColumnsSql(StringBuilder queryString) {
        try {
            List<VariableEntity> listColumns = new ArrayList<>();
            conn = openConnection();
            try (PreparedStatement updateSqlStatement = conn.prepareStatement(queryString.toString())) {
                try (ResultSet rs = updateSqlStatement.executeQuery()) {
                    ResultSetMetaData rsMetaData = rs.getMetaData();
                    int columnCount = rsMetaData.getColumnCount();
                    // The column count starts from 1
                    for (int i = 1; i <= columnCount; i++) {
                        String name = rsMetaData.getColumnName(i).toLowerCase();
                        int type = rsMetaData.getColumnType(i);
                        String columnTypeName = rsMetaData.getColumnTypeName(i);
                        int scale = rsMetaData.getScale(i);
                        // Do stuff with name
                        if (!name.equalsIgnoreCase("r_")) {
                            VariableEntity item = new VariableEntity();
                            String strQuery = queryString.toString().replace(name.toUpperCase(), "").replace(name.toLowerCase(), "");
                            int index = strQuery.toLowerCase().indexOf(name.toLowerCase());
                            String columnName = name;
                            if (index > 0) {
                                columnName = strQuery.substring(index, index + name.length());
                            }
                            item.setColumnName(columnName);
                            item.setColumnType(type);
                            item.setColumnTypeName(columnTypeName);
                            item.setScale(scale);
                            item.setIsPrivateKey(false);
                            item.setColumnNameOrigin(rsMetaData.getColumnName(i));
                            listColumns.add(item);
                        }
                    }
                }
            }
            return listColumns;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            LOGGER.error(e);
            LOGGER.error("Loi! SQL: " + queryString);
        }
        return null;
    }


    /**
     * thuc hien lay danh sach bang co trong cau sql
     *
     * @return
     */
    public List<String> getListTableAll() {
        List<String> listTable = new ArrayList<>();
        if (getDatabaseName().contains("mariadb")) {
            try {
                this.conn = openConnection();
                DatabaseMetaData dbmd = this.conn.getMetaData();
                String[] types = {"TABLE"};
                ResultSet rs = dbmd.getTables(null, null, "%", types);
                while (rs.next()) {
                    listTable.add(rs.getString("TABLE_NAME"));
                }
            } catch (SQLException e) {
                LOGGER.error(e);
            }
        } else {
            CommonDataBaseDao cdbd = new CommonDataBaseDao();
            StringBuilder sql = new StringBuilder();
            sql.append("select TABLE_NAME columnName from user_tables");
            List<VariableEntity> listTables =
                    (List<VariableEntity>) cdbd.executeSqlGetListObjOnCondition(sql, null, null, null, VariableEntity.class);
            for (VariableEntity tableName : listTables) {
                listTable.add(tableName.getColumnName());
            }
        }

        return listTable;
    }

    /**
     * thuc hien lay danh sach bang co trong cau sql
     *
     * @param tables
     * @param queryString
     * @return
     */
    public List<String> getListTableNameSql(List<String> tables, StringBuilder queryString) {
        if (queryString == null || queryString.toString().trim().length() == 0 || tables == null || tables.size() == 0) {
            return null;
        }
        List<String> listTableName = new ArrayList<>();
        String sql = queryString.toString().toLowerCase().replace(" ,", ",").replace(", ", ",");
        for (String tableNameItem : tables) {
            String tableName = tableNameItem.trim().toLowerCase();
            if (tableName.trim().length() > 0) {
                //check sql neu co ten bang thi lay ra
                if (sql.trim().endsWith(" " + tableName) || sql.contains(" " + tableName + " ") || sql.contains(" " + tableName + ",")
                        || sql.contains("," + tableName + " ") || sql.contains("," + tableName + ",") || sql.trim().endsWith("," + tableName)) {
                    listTableName.add(tableName.trim().toLowerCase());
                }
            }
        }

        return listTableName;
    }


    /**
     * lay danh sach truong la khoa chinh cua bang
     *
     * @param tableName
     * @return
     */
    public List<String> getPrimaryKey(String tableName) {
        List<String> listResult = new ArrayList<>();
        try {
            conn = openConnection();
            DatabaseMetaData metaData = conn.getMetaData();

            ResultSet rs = metaData.getPrimaryKeys(null, null, tableName);
            //Printing the column name and size
            while (rs.next()) {
//                LOGGER.info("Table name: " + rs.getString("TABLE_NAME"));
//                LOGGER.info("Column name: " + rs.getString("COLUMN_NAME"));
                listResult.add(rs.getString("COLUMN_NAME").trim().toLowerCase());
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            LOGGER.error(e);
        }
        return listResult;
    }

    public String getDatabaseName() {
        Properties props = FunctionCommon.readFileProperties("config.properties");
        if (props != null && !props.isEmpty()) {
            String driverName = props.getProperty("spring.datasource.driver-class-name");
            if (driverName.contains("mariadb")) {
                return "mariadb";
            }
        }
        return "oracle";
    }
}
