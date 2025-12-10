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
 * Gen class ServiceImpl
 */
public class GenServiceImpl {

    private static final Logger LOGGER = LogManager.getLogger(GenServiceImpl.class);

    public static void writeClassServiceImpl(ObjectEntity itemObject) {
        try {
            //thuc hien gen class
            if (itemObject != null) {
                String strClassServiceImpl = itemObject.getClassName() + "ServiceImpl";
                String pathFileServiceImpl = FunctionCommon.getPropertiesValue("src.url.create.code")
                        + File.separator + "src"
                        + File.separator + "main"
                        + File.separator + "java"
                        + File.separator + "vn"
                        + File.separator + "com"
                        + File.separator + "viettel"
                        + File.separator + "services"
                        + File.separator + "impl"
                        + File.separator + strClassServiceImpl + ".java";
                File file = new File(pathFileServiceImpl);
                if (file.exists()) {
                    StringBuilder strString = new StringBuilder();
                    strString.append(FunctionCommon.readLineByLine(pathFileServiceImpl));
                    String strSubLast = strString.substring(0, strString.toString().trim().lastIndexOf("}"));
                    StringBuilder strFullCode = addMethodIfNotExits(strSubLast, itemObject);
                    FileWriter fileWriterAction = new FileWriter(pathFileServiceImpl);
                    try (PrintWriter printWriteAction = new PrintWriter(fileWriterAction)) {
                        printWriteAction.print(strFullCode);
                    }
                } else {
                    file.getParentFile().mkdirs();
                    FileWriter fileWriterAction = new FileWriter(pathFileServiceImpl);
                    try (PrintWriter printWriteAction = new PrintWriter(fileWriterAction)) {
                        StringBuilder strContentCodeAction = generateClassServiceImpl(itemObject);
                        printWriteAction.print(strContentCodeAction);
                    }
                }
                System.out.println("Generate File ServiceImpl = " + strClassServiceImpl);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            LOGGER.error(e);
        }
    }

    private static StringBuilder generateClassServiceImpl(ObjectEntity itemObject) {
        String strClassServiceImpl = itemObject.getClassName() + "ServiceImpl";
        String strClassService = itemObject.getClassName() + "Service";
        String strClassRepository = itemObject.getClassName() + "Repository";
        String strClassDTO = itemObject.getClassName() + "DTO";
        String strDescClass = itemObject.getDesc();
        StringBuilder strContentCodeAction = new StringBuilder();

        // File ServiceImpl
        //==============chen header import======================================
        strContentCodeAction.append("package vn.com.viettel.services.impl;").append("\r\r");
        strContentCodeAction.append("import vn.com.viettel.repositories.").append(strClassRepository).append(";\r");
        strContentCodeAction.append("import vn.com.viettel.dto.").append(strClassDTO).append(";\r");
        strContentCodeAction.append("import vn.com.viettel.services.").append(strClassService).append(";\r");
        strContentCodeAction.append("import lombok.AllArgsConstructor;").append("\r");
        strContentCodeAction.append("import vn.com.viettel.core.dto.response.BaseResultSelect;").append("\r");
        strContentCodeAction.append("import org.springframework.stereotype.Service;").append("\r");
        strContentCodeAction.append("import java.util.List;").append("\r\r");
        strContentCodeAction.append("import lombok.AccessLevel;").append("\r");
        strContentCodeAction.append("import lombok.experimental.FieldDefaults;").append("\r\r");

        //thuc hien gen comment
        strContentCodeAction.append("/**").append("\r");
        strContentCodeAction.append(" * ").append("Autogen class: ").append(strDescClass).append("\r");
        strContentCodeAction.append(" * ").append("\r");
        strContentCodeAction.append(" * @author ToolGen").append("\r");
        strContentCodeAction.append(" * @date ").append(new Date()).append("\r");
        strContentCodeAction.append(" */").append("\r");

        //thuc hien code phan content class
        strContentCodeAction.append("@Service").append("\r");
        strContentCodeAction.append("@AllArgsConstructor").append("\r");
        strContentCodeAction.append("@FieldDefaults(level = AccessLevel.PRIVATE)").append("\r");
        strContentCodeAction.append("public class ").append(strClassServiceImpl).append(" implements ").append(strClassService).append(" {\r\r");
        String variableRepository = Character.toLowerCase(strClassRepository.charAt(0)) + strClassRepository.substring(1);
        strContentCodeAction.append("    private final ").append(strClassRepository).append(" ").append(variableRepository).append(";\r");
        //thuc hien gen method trong khai bao
        itemObject.getListMethod().forEach((method) -> {
            if (method.getJpa() == null || !method.getJpa()) {
                strContentCodeAction.append(generateFunctionServiceImpl(itemObject, method));
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

    private static StringBuilder generateFunctionServiceImpl(ObjectEntity itemObject, MethodEntity method) {
        String strClassRepository = itemObject.getClassName() + "Repository";
        String strClassDTO = itemObject.getClassName() + "DTO";
        String strVariableClassDTO = Character.toLowerCase(strClassDTO.charAt(0)) + FunctionCommon.camelcasify(strClassDTO.substring(1));
        String variableRepository = Character.toLowerCase(strClassRepository.charAt(0)) + strClassRepository.substring(1);
        StringBuilder strContentCodeAction = new StringBuilder();
        //thuc hien gen cac ham trong class
        strContentCodeAction.append("    \r\r");
        strContentCodeAction.append("    /**").append("\r");
        strContentCodeAction.append("     * ").append(method.getDesc()).append("\r");
        strContentCodeAction.append("     * ").append("\r");
        strContentCodeAction.append("     * @param ").append(strVariableClassDTO).append(" params client").append("\r");
        strContentCodeAction.append("     * @return ").append("\r");
        strContentCodeAction.append("     */").append("\r");

        //noi dung phuong thuc
        strContentCodeAction.append("    @Override").append("\r");
        strContentCodeAction.append("    public Object ").append(method.getName()).append("(").append(strClassDTO).append(" ").append(strVariableClassDTO).append(") {").append("\r");

        //gen comment code lay params
        strContentCodeAction.append("        /*").append("\r");
        strContentCodeAction.append("        ==========================================================").append("\r");
        strContentCodeAction.append("       ").append(strVariableClassDTO).append(": params nguoi dung truyen len").append("\r");
        strContentCodeAction.append("        ==========================================================").append("\r");
        strContentCodeAction.append("        */").append("\r");
        if (method.getCount() != null && method.getCount() == 1) {
            strContentCodeAction.append("        BaseResultSelect dataResult = ").append(variableRepository).append(".").append(method.getName()).append("(").append(strVariableClassDTO).append(");").append("\r");
        } else {
            strContentCodeAction.append("        List<").append(strClassDTO).append("> dataResult = ").append(variableRepository).append(".").append(method.getName()).append("(").append(strVariableClassDTO).append(");").append("\r");
        }


        //gen comment code lay params
        strContentCodeAction.append("        /*").append("\r");
        strContentCodeAction.append("        ==========================================================").append("\r");
        strContentCodeAction.append("        TODO: (Code at here) Thuc hien luong nghiep vu chi tiet").append("\r");
        strContentCodeAction.append("        ==========================================================").append("\r");
        strContentCodeAction.append("        */").append("\r");

        strContentCodeAction.append("        return dataResult;").append("\r");
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

            String strContenFile = strContentCodeAction.toString().replaceAll("\\s{2,}", " ").toLowerCase();
            if (!strContenFile.contains(strMethodName) && !strContenFile.contains(strMethodName1)) {
                System.out.println("method= " + method.getName());
                //Neu khong co phuong thuc trong class thi add them phuong thuc
                strContentCodeAction.append(generateFunctionServiceImpl(itemObject, method)).append("\r");
            }
        });
        //add lai ky tu dong class
        strContentCodeAction.append("}");
        return strContentCodeAction;
    }
}
