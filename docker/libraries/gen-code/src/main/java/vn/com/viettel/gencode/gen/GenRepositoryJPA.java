package vn.com.viettel.gencode.gen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vn.com.viettel.gencode.entities.ObjectEntity;
import vn.com.viettel.gencode.entities.VariableEntity;
import vn.com.viettel.gencode.utils.FunctionCommon;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import static vn.com.viettel.gencode.gen.GenEntity.getListPrivateKey;

/**
 * Gen class RepositoryJPA
 */
public class GenRepositoryJPA {
    private static final Logger LOGGER = LogManager.getLogger(GenRepositoryJPA.class);

    private static Integer numberOfTable = 0;

    public static void writeClassRepositoryJPA(ObjectEntity itemObject) {

        //thuc hien gen class
        if (itemObject != null) {
            //thuc hien kiem tra trong cau sql lay ra danh sach bang
            List<String> listTableName = FunctionCommon.getListTableFromSql(itemObject);
            if (listTableName == null || listTableName.size() == 0) {
                return;
            } else {
                numberOfTable = listTableName.size();
            }
            for (String stringTableName : listTableName) {
                List<String> listPr = getListPrivateKey(stringTableName);
                if (listPr == null || listPr.size() == 0) {
                    continue;
                }
                generateFileClassRepositoryJPA(stringTableName, itemObject);
            }

        }
    }

    public static void generateFileClassRepositoryJPA(String stringTableName, ObjectEntity itemObject) {
        try {
            //thuc hien gen class
            if (stringTableName != null) {
                String stringTableNameStr = Character.toUpperCase(stringTableName.charAt(0)) + FunctionCommon.camelcasify(stringTableName.substring(1));
                String strClassRepositoryJPA = stringTableNameStr + "RepositoryJPA";
                String pathRepositoryJPA= FunctionCommon.getPropertiesValue("src.url.create.code")
                        + File.separator + "src"
                        + File.separator + "main"
                        + File.separator + "java"
                        + File.separator + "vn"
                        + File.separator + "com"
                        + File.separator + "viettel"
                        + File.separator + "repositories"
                        + File.separator + "jpa"
                        + File.separator + strClassRepositoryJPA + ".java";
                File file = new File(pathRepositoryJPA);
                if (file.exists()) {
                    return;
                } else {
                    file.getParentFile().mkdirs();
                    FileWriter fileWriterAction = new FileWriter(pathRepositoryJPA);
                    try (PrintWriter printWriteAction = new PrintWriter(fileWriterAction)) {
                        StringBuilder strContentCodeAction = generateClassRepositoryJPA(stringTableName, itemObject);
                        printWriteAction.print(strContentCodeAction);
                    }
                }
                System.out.println("Generate File RepositoryJPA = " + strClassRepositoryJPA);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            LOGGER.error(e);
        }
    }

    /**
     * thuc hien autogencode dao
     * @param stringTableName
     * @return
     */
    private static StringBuilder generateClassRepositoryJPA(String stringTableName, ObjectEntity itemObject) {
        String finalStringTableName = stringTableName.toLowerCase();
        List<VariableEntity> variableTables = GenEntity.getListVariableFromSql(stringTableName);
        List<VariableEntity> listVariable = GenEntity.getListVariableFromSql(stringTableName);
        List<String> listPrivateKey = getListPrivateKey(stringTableName);
        GenEntity.addPrivateKeyVariable(listVariable, listPrivateKey);
        String strTypeId = "Long";
        for (VariableEntity variableEntity : listVariable) {
            //thuc hien khai bao bien
            Boolean isPrimaryKey = variableEntity.getIsPrivateKey();
            if (isPrimaryKey) {
                strTypeId = variableEntity.getTypeVariable();
                break;
            }
        }
        stringTableName = Character.toUpperCase(stringTableName.charAt(0)) + FunctionCommon.camelcasify(stringTableName.substring(1));
        String strClassEntity = FunctionCommon.camelcasify(stringTableName) + "Entity";
        String strClassDTO = itemObject.getClassName() + "DTO";
        String strClassRepository = stringTableName + "RepositoryJPA";
        String varClassDTO = Character.toLowerCase(itemObject.getClassName().charAt(0)) + FunctionCommon.camelcasify(itemObject.getClassName().substring(1));
        String strClassRepositoryCamel = FunctionCommon.camelcasify(strClassRepository);
        String strDescClass = "Create Repository For Table Name " + stringTableName;
        StringBuilder strContentCodeAction = new StringBuilder();

        // File RepositoryJPA
        //==============chen header import======================================
        strContentCodeAction.append("package vn.com.viettel.repositories.jpa;").append("\r\r");
        strContentCodeAction.append("import vn.com.viettel.entities.").append(strClassEntity).append(";\r");
        strContentCodeAction.append("import vn.com.viettel.dto.").append(strClassDTO).append(";\r");
        strContentCodeAction.append("import org.springframework.stereotype.Repository;").append("\r");
        strContentCodeAction.append("import org.springframework.data.domain.Page;").append("\r");
        strContentCodeAction.append("import org.springframework.data.domain.Pageable;").append("\r");
        strContentCodeAction.append("import org.springframework.data.jpa.repository.JpaRepository;").append("\r");
        strContentCodeAction.append("import org.springframework.data.jpa.repository.Query;").append("\r");
        strContentCodeAction.append("import org.springframework.data.jpa.repository.Modifying;").append("\r");
        strContentCodeAction.append("import org.springframework.data.repository.query.Param;").append("\r");
        strContentCodeAction.append("import org.springframework.transaction.annotation.Transactional;").append("\r");

        //thuc hien gen comment
        strContentCodeAction.append("/**").append("\r");
        strContentCodeAction.append(" * ").append("Autogen class Repository Interface: ").append(strDescClass).append("\r");
        strContentCodeAction.append(" * ").append("\r");
        strContentCodeAction.append(" * @author ToolGen").append("\r");
        strContentCodeAction.append(" * @date ").append(new Date()).append("\r");
        strContentCodeAction.append(" */").append("\r");

        //thuc hien code phan content class
        strContentCodeAction.append("@Repository").append("\r");
        strContentCodeAction.append("public interface ").append(strClassRepositoryCamel).append(" extends JpaRepository<").append(strClassEntity).append(", ").append(strTypeId).append("> {").append("\r");

        // Gen method Repository JPA
        List<VariableEntity> variableEntities = GenDTO.getListVariableFrom(itemObject, true);
        itemObject.getListMethod().forEach((method) -> {
            StringBuilder strParams = new StringBuilder();
            if (method.getJpa() != null && method.getJpa()) {
                String prefixFirstEntity = String.valueOf(Character.toLowerCase(strClassEntity.charAt(0)));
                String strSQLMapping = method.getSql();
                String strSqlWhere = "", prefix = "";
                String sql = method.getSql().toLowerCase();
                if (strSQLMapping.toLowerCase().contains("where")) {
                    strSqlWhere = strSQLMapping.substring(strSQLMapping.toLowerCase().lastIndexOf("where"));
                    prefix = sql.substring(sql.lastIndexOf(finalStringTableName), sql.lastIndexOf("where") - 1).replace(finalStringTableName, "").trim();
                } else {
                    prefix = sql.substring(sql.lastIndexOf(finalStringTableName)).replace(finalStringTableName, "").trim();
                }
                if (!prefix.isEmpty() && strSqlWhere.contains(prefix + ".")) prefixFirstEntity = prefix;
                if (method.getParams() != null) {
                    // Replace param and column
                    strSQLMapping = strSQLMapping.toLowerCase();
                    for (String param : method.getParams()) {
                        VariableEntity variableEntity = variableEntities.stream().filter(variable -> param.equalsIgnoreCase(variable.getColumnName())).findAny().orElse(null);
                        if (variableEntity != null) {
                            strSQLMapping = strSQLMapping.replace(":" + param.toLowerCase(), ":#{#" + varClassDTO + "." + variableEntity.getColumnName() + "}");
                        }
                    }

                    if (method.getValue() != null && method.getValue().trim().length() > 0) {
                        List<String> listParams = FunctionCommon.getListParamsFromUrl(method.getValue());
                        for (String param : listParams) {
                            if (param != null && param.trim().length() > 0) {
                                VariableEntity variableEntity = variableEntities.stream().filter(variable -> param.equalsIgnoreCase(variable.getColumnName())).findAny().orElse(null);
                                if (variableEntity != null) {
                                    strSQLMapping = strSQLMapping.replace(":" + param.toLowerCase(), ":#{#" + varClassDTO + "." + variableEntity.getColumnName() + "}");
                                }
                            }
                        }
                    }
                    for (VariableEntity variableEntity : variableTables) {
                        if (variableEntity.getColumnNameOrigin() != null && strSQLMapping.contains(variableEntity.getColumnNameOrigin().toLowerCase())) {
                            String variableJPa = Character.toLowerCase(variableEntity.getColumnName().charAt(0)) + FunctionCommon.camelcasify(variableEntity.getColumnName().substring(1));
                            strSQLMapping = strSQLMapping.replace(variableEntity.getColumnNameOrigin().toLowerCase(), variableJPa);
                        }
                    }

                    strParams.append("@Param(\"").append(varClassDTO).append("\") ").append(strClassDTO).append(" ").append(varClassDTO).append(", ");

                }
                if ( numberOfTable > 1) {

                } else {
                    strSqlWhere = "";
                    if (strSQLMapping.toLowerCase().contains("where")) {
                        strSqlWhere = strSQLMapping.substring(strSQLMapping.toLowerCase().lastIndexOf("where"));
                    }
                    if (method.getSql() != null && method.getSql().trim().length() > 0) {
                        String sqlCommand = method.getSql().toLowerCase().trim().replaceAll("( )+", " ");
                        if (sqlCommand.startsWith("insert into") || sqlCommand.startsWith("update") || sqlCommand.startsWith("delete")) {
                            strContentCodeAction.append("    @Transactional\r");
                            strContentCodeAction.append("    @Modifying\r");
                            if (sqlCommand.startsWith("insert into")) {
                                strSQLMapping = strSQLMapping.substring(strSQLMapping.toLowerCase().lastIndexOf("values"));
                                strContentCodeAction.append("    @Query(\"INSERT INTO ").append(strClassEntity).append(" ").append(prefixFirstEntity).append(" ").append(strSQLMapping).append("\")\r");
                            } else if (sqlCommand.startsWith("update")) {
                                strSQLMapping = strSQLMapping.substring(strSQLMapping.toLowerCase().lastIndexOf("set"));
                                strContentCodeAction.append("    @Query(\"UPDATE ").append(strClassEntity).append(" ").append(prefixFirstEntity).append(" ").append(strSQLMapping).append("\")\r");
                            } else if (sqlCommand.startsWith("delete")) {
                                strContentCodeAction.append("    @Query(\"DELETE FROM ").append(strClassEntity).append(" ").append(prefixFirstEntity).append(" ").append(strSqlWhere).append("\")\r");
                            }
                            strContentCodeAction.append("    int ").append(method.getName()).append("(").append(strParams.substring(0, strParams.length() - 2)).append(");\r");
                        } else {
                            strContentCodeAction.append("    @Query(\"FROM ").append(strClassEntity).append(" ").append(prefixFirstEntity).append(" ").append(strSqlWhere).append("\")\r");
                            strContentCodeAction.append("    Page<").append(strClassEntity).append("> ").append(method.getName()).append("(").append(strParams).append("Pageable pageable);\r");
                        }
                    }
                }
            }
        });

        strContentCodeAction.append("\n}");
        return strContentCodeAction;
    }
}
