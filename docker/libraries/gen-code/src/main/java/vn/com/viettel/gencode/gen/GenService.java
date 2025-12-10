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
 * Gen class Service
 */
public class GenService {

    private static final Logger LOGGER = LogManager.getLogger(GenService.class);

    public static void writeClassService(ObjectEntity itemObject) {
        try {
            //thuc hien gen class
            if (itemObject != null) {
                String strClassService = itemObject.getClassName() + "Service";
                String pathFileService = FunctionCommon.getPropertiesValue("src.url.create.code")
                        + File.separator + "src"
                        + File.separator + "main"
                        + File.separator + "java"
                        + File.separator + "vn"
                        + File.separator + "com"
                        + File.separator + "viettel"
                        + File.separator + "services"
                        + File.separator + strClassService + ".java";
                File file = new File(pathFileService);
                if (file.exists()) {
                    //thuc hien add them code khi da ton tai file code
                    //file da ton tai thi tien hanh add them variable vao cuoi file
                    StringBuilder strString = new StringBuilder();
                    strString.append(FunctionCommon.readLineByLine(pathFileService));
                    String strSubLast = strString.substring(0, strString.toString().trim().lastIndexOf("}"));
                    StringBuilder strFullCode = addMethodIfNotExits(strSubLast, itemObject);
                    FileWriter fileWriterAction = new FileWriter(pathFileService);
                    try (PrintWriter printWriteAction = new PrintWriter(fileWriterAction)) {
                        printWriteAction.print(strFullCode);
                    }
                } else {
                    file.getParentFile().mkdirs();
                    FileWriter fileWriterAction = new FileWriter(pathFileService);
                    try (PrintWriter printWriteAction = new PrintWriter(fileWriterAction)) {
                        StringBuilder strContentCodeAction = generateClassService(itemObject);
                        printWriteAction.print(strContentCodeAction);
                    }
                }
                System.out.println("Generate File Service = " + strClassService);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            LOGGER.error(e);
        }
    }

    private static StringBuilder generateClassService(ObjectEntity itemObject) {
        String strClassService = itemObject.getClassName() + "Service";
        String strClassDTO = itemObject.getClassName() + "DTO";
        String strDescClass = itemObject.getDesc();
        StringBuilder strContentCodeAction = new StringBuilder();

        // File Service
        //==============chen header import======================================
        strContentCodeAction.append("package vn.com.viettel.services;").append("\r\r");
        strContentCodeAction.append("import vn.com.viettel.dto.").append(strClassDTO).append(";\r\r");

        //thuc hien gen comment
        strContentCodeAction.append("/**").append("\r");
        strContentCodeAction.append(" * ").append("Autogen class: ").append(strDescClass).append("\r");
        strContentCodeAction.append(" * ").append("\r");
        strContentCodeAction.append(" * @author ToolGen").append("\r");
        strContentCodeAction.append(" * @date ").append(new Date()).append("\r");
        strContentCodeAction.append(" */").append("\r");

        //thuc hien code phan content class
        strContentCodeAction.append("public interface ").append(strClassService).append(" {").append("\r");

        //thuc hien gen method trong khai bao
        itemObject.getListMethod().forEach((method) -> {
            if (method.getJpa() == null || !method.getJpa()) {
                strContentCodeAction.append(generateFunctionService(itemObject, method));
            }
        });
        strContentCodeAction.append("\n}");
        return strContentCodeAction;
    }

    /**
     * thuc hien code gen function service
     * @param itemObject
     * @param method
     * @return
     */
    private static StringBuilder generateFunctionService(ObjectEntity itemObject, MethodEntity method) {
        StringBuilder strParamsMethod = new StringBuilder();
        String strClassDTO = itemObject.getClassName() + "DTO";
        String strVariableClassDTO = Character.toLowerCase(strClassDTO.charAt(0)) + FunctionCommon.camelcasify(strClassDTO.substring(1));
        StringBuilder strContentCodeAction = new StringBuilder();
        //noi dung phuong thuc
        strContentCodeAction.append("    \r");
        strContentCodeAction.append("    Object ").append(method.getName()).append("(").append(strClassDTO).append(" ").append(strVariableClassDTO).append(");");
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

            String strContentFile = strContentCodeAction.toString().replaceAll("\\s{2,}", " ").toLowerCase();
            if (!strContentFile.contains(strMethodName)) {
                System.out.println("method= " + method.getName());
                //Neu khong co phuong thuc trong class thi add them phuong thuc
                strContentCodeAction.append(generateFunctionService(itemObject, method)).append("\r");
            }
        });
        //add lai ky tu dong class
        strContentCodeAction.append("}");
        return strContentCodeAction;
    }
}
