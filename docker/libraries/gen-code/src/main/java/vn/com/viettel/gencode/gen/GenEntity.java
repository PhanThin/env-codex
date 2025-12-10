package vn.com.viettel.gencode.gen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vn.com.viettel.gencode.dao.CommonDataBaseDao;
import vn.com.viettel.gencode.entities.ObjectEntity;
import vn.com.viettel.gencode.entities.VariableEntity;
import vn.com.viettel.gencode.utils.FunctionCommon;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

/**
 * Gen class Entity
 */
public class GenEntity {

    private static final Logger LOGGER = LogManager.getLogger(GenEntity.class);

    public static void writeClassEntity(ObjectEntity itemObject) {

        //thuc hien gen class
        if (itemObject != null) {
            //thuc hien kiem tra trong cau sql lay ra danh sach bang
            List<String> listTableName = FunctionCommon.getListTableFromSql(itemObject);
            if (listTableName == null || listTableName.size() == 0) {
                return;
            }
            for (String stringTableName : listTableName) {
                List<String> listPr = getListPrivateKey(stringTableName);
                if (listPr == null || listPr.size() == 0) {
                    continue;
                }
                generateFileClassEntity(stringTableName);
            }

        }
    }


    public static void generateFileClassEntity(String stringTableName) {
        try {
            //thuc hien gen class
            if (stringTableName != null) {
                stringTableName = Character.toUpperCase(stringTableName.charAt(0)) + stringTableName.substring(1);
                String strClassEntity = Character.toUpperCase(stringTableName.charAt(0)) + FunctionCommon.camelcasify(stringTableName.substring(1)) + "Entity";

                String pathFileEntity = FunctionCommon.getPropertiesValue("src.url.create.code")
                        + File.separator + "src"
                        + File.separator + "main"
                        + File.separator + "java"
                        + File.separator + "vn"
                        + File.separator + "com"
                        + File.separator + "viettel"
                        + File.separator + "entities"
                        + File.separator + strClassEntity + ".java";
                File file = new File(pathFileEntity);
                if (file.exists()) {
                    //neu entity da ton tai
                    //file da ton tai thi tien hanh add them variable vao cuoi file
                    StringBuilder strString = new StringBuilder();
                    strString.append(FunctionCommon.readLineByLine(pathFileEntity));
                    String strSubLast = strString.substring(0, strString.toString().trim().lastIndexOf("}"));
                    StringBuilder strFullCode = addParamsInfileIfNotExits(strSubLast, stringTableName);

                    FileWriter fileWriterAction = new FileWriter(pathFileEntity);
                    try (PrintWriter printWriteAction = new PrintWriter(fileWriterAction)) {
                        printWriteAction.print(strFullCode);
                    }
                } else {
                    file.getParentFile().mkdirs();
                    FileWriter fileWriterAction = new FileWriter(pathFileEntity);
                    try (PrintWriter printWriteAction = new PrintWriter(fileWriterAction)) {
                        StringBuilder strContentCodeAction = generateClassEntity(stringTableName);
                        printWriteAction.print(strContentCodeAction);
                    }
                }
                System.out.println("Generate File Entity = " + strClassEntity);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            LOGGER.error(e);
        }
    }

    /**
     * @param
     * @return
     */
    private static StringBuilder generateClassEntity(String stringTableName) {
        String strDescClass = "Create Entity For Table Name " + stringTableName;
        StringBuilder strContentCodeAction = new StringBuilder();

        // File Entity
        //==============chen header import======================================
        strContentCodeAction.append("package vn.com.viettel.entities;").append("\r\r");
        strContentCodeAction.append("import java.io.Serializable;").append("\r");
        strContentCodeAction.append("import jakarta.persistence.*;").append("\r");
//        strContentCodeAction.append("import javax.validation.constraints.NotNull;").append("\r");
        strContentCodeAction.append("import com.fasterxml.jackson.annotation.JsonInclude;").append("\r");
        strContentCodeAction.append("import com.fasterxml.jackson.annotation.JsonInclude.Include;").append("\r");
        strContentCodeAction.append("import lombok.AccessLevel;").append("\r");
        strContentCodeAction.append("import lombok.Data;").append("\r");
        strContentCodeAction.append("import lombok.NoArgsConstructor;").append("\r");
        strContentCodeAction.append("import lombok.experimental.FieldDefaults;").append("\r");
        strContentCodeAction.append("import java.sql.*;").append("\r\r");

        //thuc hien gen comment
        strContentCodeAction.append("/**").append("\r");
        strContentCodeAction.append(" * ").append("Autogen class Entity: ").append(strDescClass).append("\r");
        strContentCodeAction.append(" * ").append("\r");
        strContentCodeAction.append(" * @author ToolGen").append("\r");
        strContentCodeAction.append(" * @date ").append(new Date()).append("\r");
        strContentCodeAction.append(" */").append("\r");

        //thuc hien code phan content class
        String strClassEntityCamel = FunctionCommon.camelcasify(stringTableName);
        strContentCodeAction.append("@Data").append("\r");
        strContentCodeAction.append("@NoArgsConstructor").append("\r");
        strContentCodeAction.append("@JsonInclude(Include.NON_NULL)").append("\r");
        strContentCodeAction.append("@FieldDefaults(level = AccessLevel.PRIVATE)").append("\r");
        strContentCodeAction.append("@Entity").append("\r");
        strContentCodeAction.append("@Table(name = \"").append(stringTableName.toUpperCase()).append("\")").append("\r");
        strContentCodeAction.append("public class ").append(strClassEntityCamel).append("Entity implements Serializable {").append("\r");
        strContentCodeAction.append(getParamsFromQuery(stringTableName));
        strContentCodeAction.append("\n}");
        return strContentCodeAction;
    }

    private static StringBuilder getParamsFromQuery(String stringTableName) {
        List<VariableEntity> listVariable = getListVariableFromSql(stringTableName);
        List<String> listPrivateKey = getListPrivateKey(stringTableName);
        addPrivateKeyVariable(listVariable, listPrivateKey);
        if (listVariable == null) {
            return new StringBuilder("");
        }
        StringBuilder strContentCodeAction = new StringBuilder();
        for (VariableEntity variableEntity : listVariable) {
            //thuc hien khai bao bien
            String strType = variableEntity.getTypeVariable();
            Boolean isPrimaryKey = variableEntity.getIsPrivateKey();
            strContentCodeAction.append("\r");
            if (isPrimaryKey) {
//                String strSeqName = stringTableName.toUpperCase().trim();
                strContentCodeAction.append("    @Id").append("\r");
//                strContentCodeAction.append("    @GeneratedValue(generator = \"").append(strSeqName).append("_SEQ\")").append("\r");
//                strContentCodeAction.append("    @SequenceGenerator(name = \"").append(strSeqName).append("_SEQ\", sequenceName = \"").append(strSeqName).append("_SEQ\", allocationSize = 1)").append("\r");
//                strContentCodeAction.append("    @Basic(optional = false)").append("\r");
//                strContentCodeAction.append("    @NotNull").append("\r");
            }
            strContentCodeAction.append("    @Column(name = \"").append(variableEntity.getColumnName().toUpperCase()).append("\")").append("\r");
            String strColumnName = FunctionCommon.camelcasify(variableEntity.getColumnName());
            strContentCodeAction.append("    ").append(strType).append(" ").append(strColumnName).append(";\r");
//            String strMethod = WordUtils.capitalizeFully(variableEntity.getColumnName()).trim();
            //Phuong thuc get
//            strContentCodeAction.append("   public ").append(strType).append(" get").append(strMethod).append("() {\r");
//            strContentCodeAction.append("       return ").append(variableEntity.getColumnName()).append(";\r");
//            strContentCodeAction.append("   }").append("\r");
            //phuong thuc set
//            strContentCodeAction.append("   public void").append(" set").append(strMethod).append("(").append(strType).append(" ").append(variableEntity.getColumnName()).append("){\r");
//            strContentCodeAction.append("       this.").append(variableEntity.getColumnName()).append(" = ").append(variableEntity.getColumnName()).append(";\r");
//            strContentCodeAction.append("   }").append("\r");

        }

        return strContentCodeAction;
    }


    /**
     * Thuc hien lay danh sach dto tu cac cau sql
     *
     * @param stringTableName
     * @return
     */
    public static List<VariableEntity> getListVariableFromSql(String stringTableName) {
        StringBuilder query = new StringBuilder();
        // Select clause
        query.append("SELECT * FROM ");
        query.append(stringTableName);
        CommonDataBaseDao cmd = new CommonDataBaseDao();
        return cmd.getListColumnsSql(query);
    }

    /**
     * Thuc hien lay danh sach dto tu cac cau sql
     *
     * @param stringTableName
     * @return
     */
    public static List<String> getListPrivateKey(String stringTableName) {
        // Select clause
        CommonDataBaseDao cmd = new CommonDataBaseDao();
        List<String> params = cmd.getPrimaryKey(stringTableName.toUpperCase());
        if (params == null || params.size() == 0) {
            return null;
        }
        return params;
    }

    /**
     * thuc hien set khoa vao bang
     *
     * @param listVariableTg
     * @param listPrivaryKey
     * @return
     */
    public static void addPrivateKeyVariable(List<VariableEntity> listVariableTg, List<String> listPrivaryKey) {
        if (listPrivaryKey != null && listPrivaryKey.size() > 0) {
            for (String string : listPrivaryKey) {
                for (VariableEntity variableEntity : listVariableTg) {
                    if (listPrivaryKey.size() <= 1 && string.equalsIgnoreCase(variableEntity.getColumnName())) {
                        //truong hop co 1 khoa
                        variableEntity.setIsPrivateKey(true);
                        break;
                    } else if (string.equalsIgnoreCase(variableEntity.getColumnName()) &&
                            (variableEntity.getTypeVariable().equals("Long") || variableEntity.getTypeVariable().equals("Integer") || variableEntity.getTypeVariable().equals("Double"))) {
                        //nhieu khoa
                        variableEntity.setIsPrivateKey(true);
                    }
                }
            }
        }
    }

    /**
     * thuc hien add params vao entity
     *
     * @param strSubLast
     * @param stringTableName
     * @return
     */
    private static StringBuilder addParamsInfileIfNotExits(String strSubLast, String stringTableName) {
        StringBuilder strContentCodeAction = new StringBuilder(strSubLast);
        List<VariableEntity> listVariable = getListVariableFromSql(stringTableName);
        if (listVariable != null) {
            for (VariableEntity variableEntity : listVariable) {
                String strType = variableEntity.getTypeVariable();
                //thuc hien khai bao bien
//                    String strStrVariale = strType + " " + variableEntity.getColumnName()+";";
//                    String strMethod = WordUtils.capitalizeFully(variableEntity.getColumnName()).trim();

                //ten entity khai bao
                String strStrVariale = " " + variableEntity.getColumnName().toLowerCase() + ";";
                String strStrVariale1 = " " + variableEntity.getColumnName().toLowerCase() + " ;";
                String strStrVarialeCamel = " " + FunctionCommon.camelcasify(variableEntity.getColumnName()).toLowerCase() + ";";
                String strStrVariale1Camel = " " + FunctionCommon.camelcasify(variableEntity.getColumnName()).toLowerCase() + " ;";

                String strContenFile = strContentCodeAction.toString().replaceAll("\\s{2,}", " ").toLowerCase();
                if (!strContenFile.contains(strStrVariale) && !strContenFile.contains(strStrVariale1)
                        && !strContenFile.contains(strStrVarialeCamel) && !strContenFile.contains(strStrVariale1Camel)) {
                    //chua co thi add them params
                    strContentCodeAction.append("\r\r");
                    strContentCodeAction.append("    @Column(name = \"").append(variableEntity.getColumnName().toUpperCase()).append("\")").append("\r");
                    strContentCodeAction.append("    ").append(strType).append(" ").append(variableEntity.getColumnName()).append(";\r");

                    //Phuong thuc get
//                        strContentCodeAction.append("   public ").append(strType).append(" get").append(strMethod).append("() {\r");
//                        strContentCodeAction.append("       return ").append(variableEntity.getColumnName()).append(";\r");
//                        strContentCodeAction.append("   }").append("\r");
                    //phuong thuc set
//                        strContentCodeAction.append("   public void").append(" set").append(strMethod).append("(").append(strType).append(" ").append(variableEntity.getColumnName()).append("){\r");
//                        strContentCodeAction.append("       this.").append(variableEntity.getColumnName()).append(" = ").append(variableEntity.getColumnName()).append(";\r");
//                        strContentCodeAction.append("   }");
                }
            }
        }
        strContentCodeAction.append("}");
        return strContentCodeAction;
    }
}
