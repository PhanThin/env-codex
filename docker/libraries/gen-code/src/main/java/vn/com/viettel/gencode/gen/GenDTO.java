package vn.com.viettel.gencode.gen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vn.com.viettel.gencode.dao.CommonDataBaseDao;
import vn.com.viettel.gencode.entities.MethodEntity;
import vn.com.viettel.gencode.entities.ObjectEntity;
import vn.com.viettel.gencode.entities.VariableEntity;
import vn.com.viettel.gencode.utils.FunctionCommon;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Gen class DTO
 */
public class GenDTO {

    private static final Logger LOGGER = LogManager.getLogger(GenDTO.class);

    public static void writeClassDTO(ObjectEntity itemObject) {
        try {
            if (itemObject != null) {
                String strClassDTO = itemObject.getClassName() + "DTO";
                String pathFileDTO = FunctionCommon.getPropertiesValue("src.url.create.code")
                        + File.separator + "src"
                        + File.separator + "main"
                        + File.separator + "java"
                        + File.separator + "vn"
                        + File.separator + "com"
                        + File.separator + "viettel"
                        + File.separator + "dto"
                        + File.separator + strClassDTO + ".java";
                File file = new File(pathFileDTO);
                if (file.exists()) {
                    // File da ton tai thi tien hanh add them variable vao cuoi file
                    StringBuilder strString = new StringBuilder();
                    strString.append(FunctionCommon.readLineByLine(pathFileDTO));
                    String strSubLast = strString.substring(0, strString.toString().trim().lastIndexOf("}"));
                    StringBuilder strFullCode = addParamsInfileIfNotExits(strSubLast, itemObject);
                    FileWriter fileWriterAction = new FileWriter(pathFileDTO);
                    try (PrintWriter printWriteAction = new PrintWriter(fileWriterAction)) {
                        printWriteAction.print(strFullCode);
                    }
                } else {
                    // File moi thi tien hanh tao moi file tu dau
                    file.getParentFile().mkdirs();
                    FileWriter fileWriterAction = new FileWriter(pathFileDTO);
                    try (PrintWriter printWriteAction = new PrintWriter(fileWriterAction)) {
                        StringBuilder strContentCodeAction = generateClassDTO(itemObject);
                        printWriteAction.print(strContentCodeAction);
                    }
                }
                System.out.println("Generate File DTO = " + strClassDTO);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            LOGGER.error(e);
        }
    }

    /**
     * @param itemObject
     * @return
     */
    private static StringBuilder generateClassDTO(ObjectEntity itemObject) {
        String strClassDTO = itemObject.getClassName() + "DTO";
        String strDescClass = itemObject.getDesc();
        StringBuilder strContentCodeAction = new StringBuilder();

        // File DTO
        //==============chen header import======================================
        strContentCodeAction.append("package vn.com.viettel.dto;").append("\r\r");
        strContentCodeAction.append("import com.fasterxml.jackson.annotation.JsonInclude;").append("\r");
        strContentCodeAction.append("import com.fasterxml.jackson.annotation.JsonInclude.Include;").append("\r");
        strContentCodeAction.append("import java.util.Date;").append("\r");
        strContentCodeAction.append("import lombok.AccessLevel;").append("\r");
        strContentCodeAction.append("import lombok.Data;").append("\r");
        strContentCodeAction.append("import lombok.NoArgsConstructor;").append("\r");
        strContentCodeAction.append("import lombok.experimental.FieldDefaults;").append("\r\r");

        //thuc hien gen comment
        strContentCodeAction.append("/**").append("\r");
        strContentCodeAction.append(" * ").append("Autogen class DTO: ").append(strDescClass).append("\r");
        strContentCodeAction.append(" * ").append("\r");
        strContentCodeAction.append(" * @author ToolGen").append("\r");
        strContentCodeAction.append(" * @date ").append(new Date()).append("\r");
        strContentCodeAction.append(" */").append("\r");

        //thuc hien code phan content class
        strContentCodeAction.append("@Data").append("\r");
        strContentCodeAction.append("@NoArgsConstructor").append("\r");
        strContentCodeAction.append("@JsonInclude(Include.NON_NULL)").append("\r");
        strContentCodeAction.append("@FieldDefaults(level = AccessLevel.PRIVATE)").append("\r");
        strContentCodeAction.append("public class ").append(strClassDTO).append(" {").append("\r");
        strContentCodeAction.append(getParamsFromQuery(itemObject));

        strContentCodeAction.append("\n}");
        return strContentCodeAction;
    }

    private static StringBuilder getParamsFromQuery(ObjectEntity itemObject) {
        List<VariableEntity> listVariable = getListVariableFrom(itemObject, true);
        StringBuilder strContentCodeAction = new StringBuilder();
        for (VariableEntity variableEntity : listVariable) {
            //thuc hien khai bao bien
            String strType = variableEntity.getTypeVariable();
            strContentCodeAction.append("\r").append("    ").append(strType).append(" ").append(variableEntity.getColumnName()).append(";\r");
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
     * lay danh sach bien tu khai bao sql va dinh nghia ben ngoai
     *
     * @param itemObject
     * @return
     */
    public static List<VariableEntity> getListVariableFrom(ObjectEntity itemObject, Boolean isCamel) {
        List<MethodEntity> listMethod = itemObject.getListMethod();
        //thuc hien lay danh sach params entity trong sql
        List<VariableEntity> listVariable = new ArrayList<>();
        for (MethodEntity methodEntity : listMethod) {
            String sql = methodEntity.getSql();
            if (sql != null && sql.trim().length() > 0) {
                String sqlCommand = sql.toLowerCase().trim().replaceAll("( )+", " ");
                if (sqlCommand.startsWith("insert into ") || sqlCommand.startsWith("update") || sqlCommand.startsWith("delete")) {
                } else {
                    List<VariableEntity> listVariableTg = getListVariableFromSql(sql);
                    if (listVariableTg != null) {
                        listVariable.addAll(listVariableTg);
                    }
                }
                List<VariableEntity> listVariableFromSql = getListVariableFromSqlPa(sql);
                if (listVariableFromSql != null) {
                    listVariable.addAll(listVariableFromSql);
                }
            }
            List<String> listParamsDefine = methodEntity.getParams();
            if (listParamsDefine != null && listParamsDefine.size() > 0) {
                for (String string : listParamsDefine) {
                    VariableEntity variableEntity = new VariableEntity();
                    String strClName = string.trim();
                    variableEntity.setColumnName(strClName);
                    variableEntity.setColumnType(10000);
                    listVariable.add(variableEntity);
                }
            }
        }


        return removeDuplicates(listVariable, isCamel);
    }

    /**
     * Thuc hien lay danh sach dto tu cac cau sql
     *
     * @param sql
     * @return
     */
    public static List<VariableEntity> getListVariableFromSql(String sql) {
        StringBuilder query = new StringBuilder();
        if (sql == null || sql.trim().length() == 0) {
            return null;
        }
        //lay danh sach params
        List<String> listParams = FunctionCommon.getListParamsSql(sql);
        if (listParams != null && listParams.size() > 0) {
            for (String listParam : listParams) {
                sql = sql.replace(":" + listParam, "null");
            }
        }
        // Select clause
        query.append(sql.replace("%", ""));
        CommonDataBaseDao cmd = new CommonDataBaseDao();
        return cmd.getListColumnsSql(query);
    }

    /**
     * thuc hien remove duplicate variable
     *
     * @param list
     * @param isCamel
     * @return
     */
    public static List<VariableEntity> removeDuplicates(List<VariableEntity> list, Boolean isCamel) {
        ArrayList<VariableEntity> newList = new ArrayList<>();


        for (VariableEntity element : list) {
            //thuc hien so sanh item trong danh sach da add vao chua
            boolean isNew = true;
            for (VariableEntity variableEntity : newList) {
                if (variableEntity.getColumnName().equalsIgnoreCase(element.getColumnName())
                        || FunctionCommon.camelcasify(variableEntity.getColumnName()).equalsIgnoreCase(element.getColumnName())) {
                    isNew = false;
                    break;
                }
            }
            if (isNew) {
                String strColumnName = element.getColumnName();
                element.setColumnName(strColumnName);
                newList.add(element);
            }
        }
        //thuc hien add them 2 bien mac dinh la pageIndex vaf pageSize
        VariableEntity variableStartRc = new VariableEntity();
        variableStartRc.setColumnName("pageIndex");
        variableStartRc.setColumnType(4);
        newList.add(variableStartRc);

        VariableEntity variableStartPs = new VariableEntity();
        variableStartPs.setColumnName("pageSize");
        variableStartPs.setColumnType(4);
        newList.add(variableStartPs);

        VariableEntity variableResutlExcuteSql = new VariableEntity();
        variableResutlExcuteSql.setColumnName("resultSqlEx");
        variableResutlExcuteSql.setColumnType(158111);
        newList.add(variableResutlExcuteSql);
        if (isCamel) {
            for (VariableEntity variableEntity : newList) {
                variableEntity.setColumnName(FunctionCommon.camelcasify(variableEntity.getColumnName()));
            }
        }
        return newList;
    }


    /**
     * thuc hien insert them params neu co them param moi
     *
     * @param strSubLast
     * @param itemObject
     * @return
     */
    private static StringBuilder addParamsInfileIfNotExits(String strSubLast, ObjectEntity itemObject) {
        StringBuilder strContentCodeAction = new StringBuilder(strSubLast);
        List<VariableEntity> listVariable = getListVariableFrom(itemObject, false);
        List<VariableEntity> listVariableCamel = getListVariableFrom(itemObject, true);
        boolean isAddNew = false;
        VariableEntity variableEntity;
        VariableEntity variableEntityCamel;
        for (int i = 0; i < listVariable.size(); i++) {
            variableEntity = listVariable.get(i);
            variableEntityCamel = listVariableCamel.get(i);
            //thuc hien khai bao bien
            String strType = variableEntity.getTypeVariable();

            //Kiem tra xem bien da duoc khai bao trong file chua
//                String strStrVariable = strType + " " + variableEntity.getColumnName()+";";
//                String strMethod = WordUtils.capitalizeFully(variableEntity.getColumnName()).trim();
            //ten bien
            String strStrVariable = " " + variableEntity.getColumnName().toLowerCase() + ";";
            String strStrVariable1 = " " + variableEntity.getColumnName().toLowerCase() + " ;";
            String strStrVariableCamel = " " + variableEntityCamel.getColumnName().toLowerCase() + ";";
            String strStrVariable1Camel = " " + variableEntityCamel.getColumnName().toLowerCase() + " ;";
            //noi dung file convert ve chuoi thuong va loai bo nhieu dau cach ve 1 dau cach
            String strContentFile = strContentCodeAction.toString().replaceAll("\\s{2,}", " ").toLowerCase();

            if (!strContentFile.contains(strStrVariable) && !strContentFile.contains(strStrVariable1)
                    && !strContentFile.contains(strStrVariableCamel) && !strContentFile.contains(strStrVariable1Camel)) {
                //neu chua khai bao thi tien hanh khai bao them
                strContentCodeAction.append("\r\r").append("    ").append(strType).append(" ").append(variableEntityCamel.getColumnName()).append(";\r");

                //Phuong thuc get
//                    strContentCodeAction.append("   public ").append(strType).append(" get").append(strMethod).append("() {\r");
//                    strContentCodeAction.append("       return ").append(variableEntity.getColumnName()).append(";\r");
//                    strContentCodeAction.append("   }").append("\r");
                //phuong thuc set
//                    strContentCodeAction.append("   public void").append(" set").append(strMethod).append("(").append(strType).append(" ").append(variableEntity.getColumnName()).append("){\r");
//                    strContentCodeAction.append("       this.").append(variableEntity.getColumnName()).append(" = ").append(variableEntity.getColumnName()).append(";\r");
//                    strContentCodeAction.append("   }");
                isAddNew = true;
            }
        }
        if (isAddNew) {
            strContentCodeAction.append("\r");
        }
        strContentCodeAction.append("}");
        return strContentCodeAction;
    }

    /**
     * thuc hien lay params duoc viet trong cau sql
     *
     * @param sql
     * @return
     */
    private static List<VariableEntity> getListVariableFromSqlPa(String sql) {
        List<VariableEntity> listResult = null;
        List<String> listParams = FunctionCommon.getListParamsSql(sql);
        if (listParams != null && listParams.size() > 0) {
            listResult = new ArrayList<>();
            for (String listParam : listParams) {
                VariableEntity itemEntity = new VariableEntity();
                itemEntity.setColumnName(listParam);
                itemEntity.setColumnType(12);
                listResult.add(itemEntity);
            }
        }
        return listResult;
    }
}
