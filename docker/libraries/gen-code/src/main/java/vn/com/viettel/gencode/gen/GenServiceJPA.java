package vn.com.viettel.gencode.gen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vn.com.viettel.gencode.entities.MethodEntity;
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
 * Gen class ServiceJPA
 */
public class GenServiceJPA {

    private static final Logger LOGGER = LogManager.getLogger(GenServiceJPA.class);

    public static void writeClassServiceJPA(ObjectEntity itemObject) {

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
                writeClassServiceTable(stringTableName, itemObject);
            }

        }
    }

    private static void writeClassServiceTable(String stringTableName, ObjectEntity itemObject) {
        try {
            String classTableService = Character.toUpperCase(stringTableName.charAt(0)) + FunctionCommon.camelcasify(stringTableName.substring(1));
            String strClassServiceJPA = classTableService + "ServiceJPA";
            String pathFileServiceJPA = FunctionCommon.getPropertiesValue("src.url.create.code")
                    + File.separator + "src"
                    + File.separator + "main"
                    + File.separator + "java"
                    + File.separator + "vn"
                    + File.separator + "com"
                    + File.separator + "viettel"
                    + File.separator + "services"
                    + File.separator + "jpa"
                    + File.separator + strClassServiceJPA + ".java";
            File file = new File(pathFileServiceJPA);
            if (file.exists()) {
                return;
            } else {
                file.getParentFile().mkdirs();
                FileWriter fileWriterAction = new FileWriter(pathFileServiceJPA);
                try (PrintWriter printWriteAction = new PrintWriter(fileWriterAction)) {
                    StringBuilder strContentCodeAction = generateClassServiceJPA(stringTableName, itemObject);
                    printWriteAction.print(strContentCodeAction);
                }
            }
            System.out.println("Generate File ServiceJPA = " + strClassServiceJPA);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            LOGGER.error(e);
        }
    }

    private static StringBuilder generateClassServiceJPA(String stringTableName, ObjectEntity itemObject) {
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
        stringTableName = Character.toUpperCase(stringTableName.charAt(0)) + stringTableName.substring(1);
        String strClassService = FunctionCommon.camelcasify(stringTableName) + "ServiceJPA";
        String strClassEntity = FunctionCommon.camelcasify(stringTableName) + "Entity";
        String strClassRepository = FunctionCommon.camelcasify(stringTableName) + "RepositoryJPA";
        String strClassDTO = itemObject.getClassName() + "DTO";
        String varTableNameCamel = Character.toLowerCase(strClassEntity.charAt(0)) + FunctionCommon.camelcasify(strClassEntity.substring(1));
        String strDescClass = "Create Service For Table Name " + stringTableName;
        StringBuilder strContentCodeAction = new StringBuilder();

        // File ServiceJPA
        //==============chen header import======================================
        strContentCodeAction.append("package vn.com.viettel.services.jpa;").append("\r\r");
        strContentCodeAction.append("import vn.com.viettel.core.dto.response.BaseResultSelect;\r");
        strContentCodeAction.append("import vn.com.viettel.dto.").append(strClassDTO).append(";\r");
        strContentCodeAction.append("import vn.com.viettel.entities.").append(strClassEntity).append(";\r");
        strContentCodeAction.append("import vn.com.viettel.repositories.jpa.").append(strClassRepository).append(";\r");
        strContentCodeAction.append("import java.util.List;").append("\r");
        strContentCodeAction.append("import java.util.Optional;").append("\r");
        strContentCodeAction.append("import lombok.AllArgsConstructor;").append("\r");
        strContentCodeAction.append("import org.springframework.stereotype.Service;").append("\r");
        strContentCodeAction.append("import org.springframework.data.domain.Page;").append("\r");
        strContentCodeAction.append("import org.springframework.data.domain.PageRequest;").append("\r");
        strContentCodeAction.append("import org.springframework.data.domain.Pageable;").append("\r");

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
        strContentCodeAction.append("public class ").append(strClassService).append(" {").append("\r\r");

//        strContentCodeAction.append("    @Autowired").append("\r");
        String varStringTableNameCamel = Character.toLowerCase(stringTableName.charAt(0)) + FunctionCommon.camelcasify(stringTableName.substring(1));
        strContentCodeAction.append("    private final ").append(strClassRepository).append(" ").append(varStringTableNameCamel).append(";\r\r");

        //method get all
        strContentCodeAction.append("    public List<").append(strClassEntity).append("> findAll() {\r");
        strContentCodeAction.append("        return ").append(varStringTableNameCamel).append(".findAll();").append("\r");
        strContentCodeAction.append("    }").append("\r\r");

        //method  add
        strContentCodeAction.append("    public ").append(strClassEntity).append(" save(").append(strClassEntity).append(" ").append(varTableNameCamel).append(") {\r");
        strContentCodeAction.append("        return ").append(varStringTableNameCamel).append(".save(").append(varTableNameCamel).append(");\r");
        strContentCodeAction.append("    }").append("\r\r");

        //method  get by Id
        strContentCodeAction.append("    public Optional<").append(strClassEntity).append("> findById(").append(strTypeId).append(" id) {").append("\r");
        strContentCodeAction.append("        return ").append(varStringTableNameCamel).append(".findById(id);").append("\r");
        strContentCodeAction.append("    }").append("\r\r");

        //method  delete
        strContentCodeAction.append("    public void").append(" deleteById(").append(strTypeId).append(" id) {").append("\r");
        strContentCodeAction.append("        ").append(varStringTableNameCamel).append(".deleteById(id);").append("\r");
        strContentCodeAction.append("    }").append("\r\r");

        //method  get one
        strContentCodeAction.append("    public ").append(strClassEntity).append(" getReferenceById(").append(strTypeId).append(" id) {").append("\r");
        strContentCodeAction.append("        return ").append(varStringTableNameCamel).append(".getReferenceById(id);").append("\r");
        strContentCodeAction.append("    }").append("\r\r");

        //method  get one
        strContentCodeAction.append("    public Boolean existsById(").append(strTypeId).append(" id) {").append("\r");
        strContentCodeAction.append("        return ").append(varStringTableNameCamel).append(".existsById(id);").append("\r");
        strContentCodeAction.append("    }").append("\r\r");

        // Gen method Service JPA
        List<VariableEntity> variableEntities = GenDTO.getListVariableFrom(itemObject, true);
        itemObject.getListMethod().forEach((method) -> {
            if (method.getJpa() != null && method.getJpa()) {
                strContentCodeAction.append(generateServiceJPA(variableEntities, method, strClassDTO, strClassEntity, varStringTableNameCamel));
            }
        });

        strContentCodeAction.append("\n}");
        return strContentCodeAction;
    }


    /**
     * Gen service JPA
     * @param variableEntities
     * @param method
     * @param strClassDTO
     * @param strClassEntity
     * @param strTableNameCamel
     * @return
     */
    private static StringBuilder generateServiceJPA(List<VariableEntity> variableEntities, MethodEntity method, String strClassDTO, String strClassEntity, String strTableNameCamel) {
        StringBuilder strContentCodeAction = new StringBuilder();
        String varClassDTO = Character.toLowerCase(strClassDTO.charAt(0)) + FunctionCommon.camelcasify(strClassDTO.substring(1));
        if (method.getSql() != null && method.getSql().trim().length() > 0) {
            String sqlCommand = method.getSql().toLowerCase().trim().replaceAll("( )+", " ");
            if (sqlCommand.startsWith("insert into") || sqlCommand.startsWith("update") || sqlCommand.startsWith("delete")) {
                strContentCodeAction.append("    public int ").append(method.getName()).append("(").append(strClassDTO).append(" ").append(varClassDTO).append(") {").append("\r");
                strContentCodeAction.append("        return ").append(strTableNameCamel).append(".").append(method.getName()).append("(").append(varClassDTO).append(");\r");
                strContentCodeAction.append("    }").append("\r\r");
            } else {
                strContentCodeAction.append("    public Object ").append(method.getName()).append("(").append(strClassDTO).append(" ").append(varClassDTO).append(") {").append("\r");
                strContentCodeAction.append("        Pageable pageable;\r");
                strContentCodeAction.append("        if (").append(varClassDTO).append(".getPageIndex() != null && ").append(varClassDTO).append(".getPageSize() != null) {\r");
                strContentCodeAction.append("           pageable = PageRequest.of(").append(varClassDTO).append(".getPageIndex(), ").append(varClassDTO).append(".getPageSize());\r");
                strContentCodeAction.append("        } else {\r");
                strContentCodeAction.append("           pageable = PageRequest.of (0, 10);\r");
                strContentCodeAction.append("        }\r");
                strContentCodeAction.append("        Page<").append(strClassEntity).append("> page = ").append(strTableNameCamel).append(".").append(method.getName()).append("(").append(varClassDTO).append(", pageable);").append("\r");
                if (method.getCount() != null && method.getCount() == 1) {
                    strContentCodeAction.append("        return new BaseResultSelect(page.getContent(), page.getTotalElements());\r");
                } else {
                    strContentCodeAction.append("        return page.getContent();\r");
                }
                strContentCodeAction.append("    }").append("\r\r");
            }
        }
        return strContentCodeAction;
    }
}
