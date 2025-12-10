package vn.com.viettel.gencode.gen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vn.com.viettel.gencode.entities.MethodEntity;
import vn.com.viettel.gencode.entities.ObjectEntity;
import vn.com.viettel.gencode.utils.FunctionCommon;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

/**
 * Gen class Repository Impl
 */
public class GenRepositoryImpl {

    private static final Logger LOGGER = LogManager.getLogger(GenRepositoryImpl.class);

    public static void writeClassRepositoryImpl(ObjectEntity itemObject) {
        try {
            //thuc hien gen class
            if (itemObject != null) {
                String strClassRepositoryImpl = itemObject.getClassName() + "RepositoryImpl";
                String pathFileRepository = FunctionCommon.getPropertiesValue("src.url.create.code")
                        + File.separator + "src"
                        + File.separator + "main"
                        + File.separator + "java"
                        + File.separator + "vn"
                        + File.separator + "com"
                        + File.separator + "viettel"
                        + File.separator + "repositories"
                        + File.separator + "impl"
                        + File.separator + strClassRepositoryImpl + ".java";
                File file = new File(pathFileRepository);
                if (file.exists()) {
                    //thuc hien add them code khi da ton tai file code
                    //file da ton tai thi tien hanh add them variable vao cuoi file
                    StringBuilder strString = new StringBuilder();
                    strString.append(FunctionCommon.readLineByLine(pathFileRepository));
                    String strSubLast = strString.substring(0, strString.toString().trim().lastIndexOf("}"));
                    StringBuilder strFullCode = addMethodIfNotExits(strSubLast, itemObject);
                    FileWriter fileWriterAction = new FileWriter(pathFileRepository);
                    try (PrintWriter printWriteAction = new PrintWriter(fileWriterAction)) {
                        printWriteAction.print(strFullCode);
                    }
                } else {
                    //thuc hien tao moi file code khi chua co code
                    file.getParentFile().mkdirs();
                    FileWriter fileWriterAction = new FileWriter(pathFileRepository);
                    try (PrintWriter printWriteAction = new PrintWriter(fileWriterAction)) {
                        StringBuilder strContentCodeAction = generateClassRepositoryImpl(itemObject);
                        printWriteAction.print(strContentCodeAction);
                    }
                }
                System.out.println("Generate File RepositoryImpl = " + strClassRepositoryImpl);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            LOGGER.error(e);
        }
    }

    /**
     * thuc hien autogencode dao
     *
     * @param itemObject
     * @return
     */
    private static StringBuilder generateClassRepositoryImpl(ObjectEntity itemObject) {
        String strClassDTO = itemObject.getClassName() + "DTO";
        String strClassRepository = itemObject.getClassName() + "Repository";
        String strClassRepositoryImpl = itemObject.getClassName() + "RepositoryImpl";
        String strDescClass = itemObject.getDesc();
        StringBuilder strContentCodeAction = new StringBuilder();

        // File RepositoryImpl
        //==============chen header import======================================
        strContentCodeAction.append("package vn.com.viettel.repositories.impl;").append("\r\r");
        strContentCodeAction.append("import vn.com.viettel.dto.").append(strClassDTO).append(";\r");
        strContentCodeAction.append("import vn.com.viettel.repositories.").append(strClassRepository).append(";\r");
        strContentCodeAction.append("import vn.com.viettel.core.repositories.impl.BaseRepositoryImpl;").append("\r");
        strContentCodeAction.append("import vn.com.viettel.core.dto.response.BaseResultSelect;").append("\r");
        strContentCodeAction.append("import java.util.ArrayList;").append("\r");
        strContentCodeAction.append("import org.springframework.stereotype.Repository;").append("\r");
        strContentCodeAction.append("import java.util.List;").append("\r");
        strContentCodeAction.append("import java.util.HashMap;").append("\r\r");
        strContentCodeAction.append("import lombok.AccessLevel;").append("\r");
        strContentCodeAction.append("import lombok.experimental.FieldDefaults;").append("\r\r");

        //thuc hien gen comment
        strContentCodeAction.append("/**").append("\r");
        strContentCodeAction.append(" * ").append("Autogen class Repository Impl: ").append(strDescClass).append("\r");
        strContentCodeAction.append(" * ").append("\r");
        strContentCodeAction.append(" * @author ToolGen").append("\r");
        strContentCodeAction.append(" * @date ").append(new Date()).append("\r");
        strContentCodeAction.append(" */").append("\r");

        //thuc hien code phan content class
        strContentCodeAction.append("@Repository").append("\r");
        strContentCodeAction.append("@FieldDefaults(level = AccessLevel.PRIVATE)").append("\r");
        strContentCodeAction.append("public class ").append(strClassRepositoryImpl).append(" extends BaseRepositoryImpl implements ").append(strClassRepository).append(" {");

        //thuc hien gen method trong khai bao
        itemObject.getListMethod().forEach((method) -> {
            if (method.getJpa() == null || !method.getJpa()) {
                strContentCodeAction.append(generateFunctionRepositoryImpl(itemObject, method));
            }
        });
        strContentCodeAction.append("\n}");
        return strContentCodeAction;
    }

    /**
     * thuc hien gen code method in Dao
     *
     * @param itemObject
     * @param method
     * @return
     */
    private static StringBuilder generateFunctionRepositoryImpl(ObjectEntity itemObject, MethodEntity method) {
        String strClassDTO = itemObject.getClassName() + "DTO";
        String strVariableClassDTO = Character.toLowerCase(strClassDTO.charAt(0)) + FunctionCommon.camelcasify(strClassDTO.substring(1));
        //thuc hien gen cac ham trong class
        StringBuilder strContentCodeAction = new StringBuilder();
        strContentCodeAction.append("    \r\r");
        strContentCodeAction.append("    /**").append("\r");
        strContentCodeAction.append("     * ").append(method.getDesc()).append("\r");
        strContentCodeAction.append("     * ").append("\r");
        strContentCodeAction.append("     * @param ").append(strVariableClassDTO).append(":").append(" params client truyen len").append("\r");
        strContentCodeAction.append("     * @return ").append("\r");
        strContentCodeAction.append("     */").append("\r");
        //noi dung phuong thuc
        strContentCodeAction.append("    @Override").append("\r");
        if (method.getCount() != null && method.getCount() == 1) {
            strContentCodeAction.append("    public BaseResultSelect").append(" ").append(method.getName()).append("(").append(strClassDTO).append(" ").append(strVariableClassDTO).append(") {").append("\r");
        } else {
            strContentCodeAction.append("    public List<").append(strClassDTO).append(">").append(" ").append(method.getName()).append("(").append(strClassDTO).append(" ").append(strVariableClassDTO).append(") {").append("\r");
        }
        strContentCodeAction.append("        StringBuilder sql = new StringBuilder();").append("\r");
        strContentCodeAction.append("        sql.append(\"").append(method.getSql()).append("\");").append("\r");
        //kiem tra cau lenh sql xem co params khong
        List<String> listParamsSql = FunctionCommon.getListParamsSql(method.getSql());
        if (listParamsSql != null && listParamsSql.size() > 0) {
            strContentCodeAction.append("        HashMap<String, Object> hmapParams = new HashMap<>();").append("\r");
            for (String stringItemParams : listParamsSql) {
                strContentCodeAction.append("        hmapParams.put(\"");
                strContentCodeAction.append(stringItemParams);
                strContentCodeAction.append("\", ").append(strVariableClassDTO).append(".get");
                String strMethod = Character.toUpperCase(stringItemParams.charAt(0)) + FunctionCommon.camelcasify(stringItemParams.substring(1));
                strContentCodeAction.append(strMethod);
                strContentCodeAction.append("());").append("\r");
            }
        } else {
            strContentCodeAction.append("        HashMap<String, Object> hmapParams = new HashMap<>();").append("\r");
            strContentCodeAction.append("        //==========TODO: DEV Thuc hien bo sung params va edit query o day=====").append("\r");
            if (method.getSql() != null && method.getSql().trim().length() > 0) {
                String sqlCommand = method.getSql().toLowerCase().trim().replaceAll("( )+", " ");
                if (sqlCommand.startsWith("insert into ")) {
                    strContentCodeAction.append("         //Example: String sql = insert into table(column1, column2) values(?,?);").append("\r");
                } else if (sqlCommand.startsWith("update")) {
                    strContentCodeAction.append("         //Example: String sql = update table set column1 = ? where column2 = ?;").append("\r");
                } else if (sqlCommand.startsWith("delete")) {
                    strContentCodeAction.append("         //Example: String sql = delete table where column1 = ? and column2 = ?;").append("\r");
                } else {
                    strContentCodeAction.append("         //Example: String sql = select * from table where column1 = ?, column2 = ?").append("\r");
                }
            } else {
                strContentCodeAction.append("         //Example: String sql = select * from table where column1=?,column2=?").append("\r");
            }
            strContentCodeAction.append("        //         hmapParams.put(\"key1\",\"value1\");").append("\r");
            strContentCodeAction.append("        //         hmapParams.put(\"key2\",\"value2\");").append("\r");
            strContentCodeAction.append("        //==========END TODO ==================================================").append("\r");
        }
        if (method.getSql() != null && method.getSql().trim().length() > 0) {
            String sqlCommand = method.getSql().toLowerCase().trim().replaceAll("( )+", " ");
            if (sqlCommand.startsWith("insert into ") || sqlCommand.startsWith("update") || sqlCommand.startsWith("delete")) {
                //neu la cau lenh bat dau bang insert into
                if (listParamsSql != null && listParamsSql.size() > 0) {
                    strContentCodeAction.append("         Boolean").append("  result = executeSqlDatabase(sql, hmapParams);").append("\r");
                } else {
                    strContentCodeAction.append("         Boolean").append("  result = executeSqlDatabase(sql, arrParams);").append("\r");
                }
                strContentCodeAction.append("         ").append(strClassDTO).append(" itemResult=").append(" new ").append(strClassDTO).append("();").append("\r");
                strContentCodeAction.append("         itemResult.setResultSqlEx(result);").append("\r");
                strContentCodeAction.append("         List<").append(strClassDTO).append(">  listData = new ArrayList<>();").append("\r");
                strContentCodeAction.append("         listData.add(itemResult);").append("\r");
                strContentCodeAction.append("         return listData;").append("\r");
            } else {//(sqlCommand.startsWith("select "))
                //cau lenh sql bat dau bang select thi tien hanh ghep code phan trang tu dong 
                //ghep phan trang du lieu neu co
                strContentCodeAction.append("        Integer pageIndex = null;").append("\r");
                strContentCodeAction.append("        if(").append(strVariableClassDTO).append(" != null && ").append(strVariableClassDTO).append(".getPageIndex() != null) {").append("\r");
                strContentCodeAction.append("            pageIndex = ").append(strVariableClassDTO).append(".getPageIndex();").append("\r");
                strContentCodeAction.append("        }").append("\r");

                strContentCodeAction.append("        Integer pageSize = null;").append("\r");
                strContentCodeAction.append("        if(").append(strVariableClassDTO).append(" != null && ").append(strVariableClassDTO).append(".getPageSize() != null) {").append("\r");
                strContentCodeAction.append("            pageSize = ").append(strVariableClassDTO).append(".getPageSize();").append("\r");
                strContentCodeAction.append("        }").append("\r");
                if (method.getCount() != null && method.getCount() == 1) {
                    if (listParamsSql != null && listParamsSql.size() > 0) {
                        strContentCodeAction.append("        BaseResultSelect resultData = getListDataAndCount(sql, hmapParams, pageIndex, pageSize, ").append(strClassDTO).append(".class);").append("\r");
                        strContentCodeAction.append("        return resultData;").append("\r");
                    } else {
                        strContentCodeAction.append("        BaseResultSelect resultData = getListDataAndCount(sql, hmapParams, pageIndex, pageSize, ").append(strClassDTO).append(".class);").append("\r");
                        strContentCodeAction.append("        return resultData;").append("\r");
                    }
                } else {
                    if (listParamsSql != null && listParamsSql.size() > 0) {
                        strContentCodeAction.append("        List<").append(strClassDTO).append(">  listData = (List<").append(strClassDTO).append(">) getListData(sql, hmapParams, pageIndex, pageSize, ").append(strClassDTO).append(".class);").append("\r");
                        strContentCodeAction.append("        return listData;").append("\r");
                    } else {
                        strContentCodeAction.append("        List<").append(strClassDTO).append(">  listData = (List<").append(strClassDTO).append(">) getListData(sql, arrParams, pageIndex, pageSize, ").append(strClassDTO).append(".class);").append("\r");
                        strContentCodeAction.append("        return listData;").append("\r");
                    }
                }
            }
        }
        strContentCodeAction.append("    }");
        return strContentCodeAction;
    }

    /**
     * Them vao phuong thuc moi neu phuong thuc chua ton tai
     *
     * @param strSubLast
     * @param itemObject
     * @return
     */
    private static StringBuilder addMethodIfNotExits(String strSubLast, ObjectEntity itemObject) {
        StringBuilder strContentCodeAction = new StringBuilder(strSubLast);
        itemObject.getListMethod().forEach((method) -> {
            String strMethodName = " " + method.getName().toLowerCase() + "(";
            String strMethodName1 = " " + method.getName().toLowerCase() + " (";
            String strContentFile = strContentCodeAction.toString().replaceAll("\\s{2,}", " ").toLowerCase();
            if (!strContentFile.contains(strMethodName) && !strContentFile.contains(strMethodName1)) {
                System.out.println("method= " + method.getName());
                //Neu khong co phuong thuc trong class thi add them phuong thuc
                strContentCodeAction.append(generateFunctionRepositoryImpl(itemObject, method)).append("\r");
            }
        });
        //add lai ky tu dong class
        strContentCodeAction.append("}");
        return strContentCodeAction;
    }
}
