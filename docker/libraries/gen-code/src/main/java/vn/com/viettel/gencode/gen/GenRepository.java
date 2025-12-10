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

/**
 * Gen class Repository
 */
public class GenRepository {

    private static final Logger LOGGER = LogManager.getLogger(GenRepository.class);

    public static void writeClassRepository(ObjectEntity itemObject) {
        try {
            //thuc hien gen class
            if (itemObject != null) {
                String strClassRepository = itemObject.getClassName() + "Repository";
                String pathFileRepository = FunctionCommon.getPropertiesValue("src.url.create.code")
                        + File.separator + "src"
                        + File.separator + "main"
                        + File.separator + "java"
                        + File.separator + "vn"
                        + File.separator + "com"
                        + File.separator + "viettel"
                        + File.separator + "repositories"
                        + File.separator + strClassRepository + ".java";
                File file = new File(pathFileRepository);
                if (file.exists()) {
                    //file da ton tai thi tien hanh add them variable vao cuoi file
                    StringBuilder strString = new StringBuilder();
                    strString.append(FunctionCommon.readLineByLine(pathFileRepository));
                    String strSubLast = strString.substring(0, strString.toString().trim().lastIndexOf("}"));
                    StringBuilder strFullCode = addMethodInterfaceIfNotExits(strSubLast, itemObject);
                    FileWriter fileWriterAction = new FileWriter(pathFileRepository);
                    try (PrintWriter printWriteAction = new PrintWriter(fileWriterAction)) {
                        printWriteAction.print(strFullCode);
                    }

                } else {
                    file.getParentFile().mkdirs();
                    FileWriter fileWriterAction = new FileWriter(pathFileRepository);
                    try (PrintWriter printWriteAction = new PrintWriter(fileWriterAction)) {
                        StringBuilder strContentCodeAction = generateClassRepository(itemObject);
                        printWriteAction.print(strContentCodeAction);
                    }
                }
                System.out.println("Generate File Repository = " + strClassRepository);
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
    private static StringBuilder generateClassRepository(ObjectEntity itemObject) {
        String strClassDTOEntity = itemObject.getClassName() + "DTO";
        String strClassRepository = itemObject.getClassName() + "Repository";
        String strDescClass = itemObject.getDesc();
        StringBuilder strContentCodeAction = new StringBuilder();

        // File Repository
        //==============chen header import======================================
        strContentCodeAction.append("package vn.com.viettel.repositories;").append("\r\r");
        strContentCodeAction.append("import vn.com.viettel.dto.").append(strClassDTOEntity).append(";\r");
        strContentCodeAction.append("import java.util.List;").append("\r");
        strContentCodeAction.append("import vn.com.viettel.core.dto.response.BaseResultSelect;").append("\r\r");

        //thuc hien gen comment
        strContentCodeAction.append("/**").append("\r");
        strContentCodeAction.append(" * ").append("Autogen class Repository Interface: ").append(strDescClass).append("\r");
        strContentCodeAction.append(" * ").append("\r");
        strContentCodeAction.append(" * @author toolGen").append("\r");
        strContentCodeAction.append(" * @date ").append(new Date()).append("\r");
        strContentCodeAction.append(" */").append("\r");

        //thuc hien code phan content class
        strContentCodeAction.append("public interface ").append(strClassRepository).append(" {").append("\r");

        //thuc hien gen method trong khai bao
        itemObject.getListMethod().forEach((method) -> {
            if (method.getJpa() == null || !method.getJpa()) {
                strContentCodeAction.append(generateFunctionRepository(itemObject, method));
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
    private static StringBuilder generateFunctionRepository(ObjectEntity itemObject, MethodEntity method) {
        String strClassDTO = itemObject.getClassName() + "DTO";
        String strVariableClassDTO = Character.toLowerCase(strClassDTO.charAt(0)) + FunctionCommon.camelcasify(strClassDTO.substring(1));
        //thuc hien gen cac ham trong class
        StringBuilder strContentCodeAction = new StringBuilder();
        //noi dung phuong thuc
        strContentCodeAction.append("\r");
        if (method.getCount() != null && method.getCount() == 1) {
            strContentCodeAction.append("    BaseResultSelect").append(" ").append(method.getName()).append("(").append(strClassDTO).append(" ").append(strVariableClassDTO).append(");");
        } else {
            strContentCodeAction.append("    List<").append(strClassDTO).append(">").append(" ").append(method.getName()).append("(").append(strClassDTO).append(" ").append(strVariableClassDTO).append(");");
        }
        return strContentCodeAction;
    }

    /**
     * thuc hien add them method interface neu phuong thuc do chua co
     *
     * @param strSubLast
     * @param itemObject
     * @return
     */
    private static StringBuilder addMethodInterfaceIfNotExits(String strSubLast, ObjectEntity itemObject) {
        StringBuilder strContentCodeAction = new StringBuilder(strSubLast);
        itemObject.getListMethod().forEach((method) -> {
            String strMethodName = " " + method.getName().toLowerCase() + "(";
            String strMethodName1 = " " + method.getName().toLowerCase() + " (";

            String strContenFile = strContentCodeAction.toString().replaceAll("\\s{2,}", " ").toLowerCase();

            if (!strContenFile.contains(strMethodName) && !strContenFile.contains(strMethodName1)) {
                System.out.println("method= " + method.getName());
                //Neu khong co phuong thuc trong class thi add them phuong thuc
                strContentCodeAction.append(generateFunctionRepository(itemObject, method)).append("\r");
            }
        });
        //add lai ky tu dong class
        strContentCodeAction.append("}");
        return strContentCodeAction;
    }
}
