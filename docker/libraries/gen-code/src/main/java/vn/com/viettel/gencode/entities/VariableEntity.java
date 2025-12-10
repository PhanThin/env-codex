/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.com.viettel.gencode.entities;

/**
 * @author datnv5
 */
public class VariableEntity {

    String columnNameOrigin;
    String columnName;
    int columnType;
    String columnTypeName;
    Boolean isPrivateKey;
    int scale;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public int getColumnType() {
        return columnType;
    }

    public void setColumnType(int columnType) {
        this.columnType = columnType;
    }

    /**
     * 2,NUMBER
     * 12,VARCHAR2
     * 93,DATE
     * -1,LONG
     * 2004,BLOB
     * 2005,CLOB
     * -13,BFILE
     * 101,BINARY_DOUBLE
     * 100,BINARY_FLOAT
     * 1,CHAR
     * -104,INTERVALDS
     * -103,INTERVALYM
     * -15,NCHAR
     * -9,NVARCHAR2
     * 2011,NCLOB
     * 93,TIMESTAMP
     *
     * @return
     */
    public String getTypeVariable() {
        String strType;
        switch (columnType) {
            case 1:
            case 12:
            case -15:
            case -9:
                strType = "String";
                break;
            case 2:
            case 3:
            case -5:
            case -4:
            case -1:
                strType = "Long";
                break;
            case 4:
            case 5:
            case -6:
                strType = "Integer";
                break;
            case 6:
            case 7:
                strType = "Float";
                break;
            case 8:
                strType = "Double";
                break;
            case 91:
            case 92:
            case 93:
                strType = "Date";
                break;
            case 2004:
            case 2005:
            case -13:
            case 100:
            case 101:
            case -103:
            case -104:
            case 2011:
                strType = "byte[]";
                break;
            case 158111:
                //ap dung cho gen code dto
                strType = "Boolean";
                break;
            default:
                strType = "String";
                break;
        }
        if (columnType == 93 && columnTypeName.equals("TIMESTAMP")) {
            strType = "Long";
        }
        if (columnType == 2 && scale > 0) {
            strType = "Double";
        }

        return strType;
    }

    public Boolean getIsPrivateKey() {
        return isPrivateKey;
    }

    public void setIsPrivateKey(Boolean isPrivateKey) {
        this.isPrivateKey = isPrivateKey;
    }

    public String getColumnTypeName() {
        return columnTypeName;
    }

    public void setColumnTypeName(String columnTypeName) {
        this.columnTypeName = columnTypeName;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public String getColumnNameOrigin() {
        return columnNameOrigin;
    }

    public void setColumnNameOrigin(String columnNameOrigin) {
        this.columnNameOrigin = columnNameOrigin;
    }
}
