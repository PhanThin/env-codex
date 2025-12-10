/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.com.viettel.gencode.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vn.com.viettel.gencode.dao.CommonDataBaseDao;
import vn.com.viettel.gencode.entities.MethodEntity;
import vn.com.viettel.gencode.entities.ObjectEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 *
 * @author datnv5
 */
public class FunctionCommon {
    private static final Logger LOGGER = LogManager.getLogger(FunctionCommon.class);

    private static final ResourceBundle RESOURCE_BUNDLE = getResourceBundle();

    private static ResourceBundle getResourceBundle() {
        return ResourceBundle.getBundle(Constants.CONFIG_FILE_PROPERTIES);
    }

    /**
     * doc file properties trong cau hinh thu muc default
     *
     * @param key
     * @return
     */
    public static String getPropertiesValue(String key) {
        String value = FunctionCommon.RESOURCE_BUNDLE.containsKey(key)
                ? RESOURCE_BUNDLE.getString(key)
                : Constants.STR_EMPTY;
        if (value.trim().length() == 0) {
            LOGGER.info("Not value with key:" + key + ", in file properties");
        }
        return value;
    }

    /**
     * convert json to object
     *
     * @param strJsonData
     * @param classOfT
     * @return
     */
    public static Object convertJsonToObject(Reader strJsonData, Class<?> classOfT) {
        Object result = null;
        try {
            Gson gson
                    = new GsonBuilder()
                            .registerTypeAdapter(int.class, new GsonEmptyStringToNumber.EmptyStringToNumberTypeAdapter())
                            .registerTypeAdapter(Integer.class, new GsonEmptyStringToNumber.EmptyStringToNumberTypeAdapter())
                            .registerTypeAdapter(long.class, new GsonEmptyStringToNumber.EmptyStringToLongTypeAdapter())
                            .registerTypeAdapter(Long.class, new GsonEmptyStringToNumber.EmptyStringToLongTypeAdapter())
                            .registerTypeAdapter(double.class, new GsonEmptyStringToNumber.EmptyStringToDoubleTypeAdapter())
                            .registerTypeAdapter(Double.class, new GsonEmptyStringToNumber.EmptyStringToDoubleTypeAdapter())
                            .create();
            result = gson.fromJson(strJsonData, classOfT);
        } catch (JsonIOException | JsonSyntaxException e) {
            System.out.println(e.getMessage());
            LOGGER.error(e);
        }
        return result;
    }

    /**
     * Kiem tra xem cot select trong database va class co cot tuong ung hay
     * khong
     *
     * @param rs
     * @param columnName
     * @return
     * @throws SQLException
     */
    public static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        for (int x = 1; x <= columns; x++) {
            String cl = rsmd.getColumnName(x);
            if (columnName.equalsIgnoreCase(cl)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Loai bo khoang trang du thua
     *
     * @param str
     * @return
     */
    public static String trimSpace(String str) {
        str = str.replaceAll("\\s+", " ");
        str = str.replaceAll("(^\\s+|\\s+$)", "");
        return str;
    }

    /**
     * lay ra khoang replace va ban ghi can thay the chu y: cau truc sql thay
     * the se phai thoa man dang where staffid in(:ListStaffId) and : cac ky tu
     * trong chuoi in phai sat canh nhau
     *
     * @param strSql
     * @param variale
     * @return
     */
    public static int[] getReplaceSqlInArr(String strSql, String variale) {
        int[] result = new int[4];
        int indext = strSql.indexOf(variale);
        int i = indext;
        int spase = 0;
        int end = indext + variale.length() + 1;
        //ket thuc ten cot can ghep dieu kien
        int charEnd = 0;
        int strInOrNotin = 0;
        int start = 0;
        while (true) {
            char a_char = strSql.charAt(i);

            if (a_char == ' ') {
                spase++;
            }
            if (spase == 1) {
                //gap khoang trong dau thi lay luon vi tri khoang trong
                charEnd = i;
                spase = 2;
            }
            if (spase == 3) {
                //gap khoang trong tiep theo thi danh dau vi tri dau can thay
                start = i;
                break;
            }
            i--;
            if (a_char == '(') {
                strInOrNotin = i;
            }
        }
        result[0] = start;
        result[1] = charEnd;
        result[2] = end;
        result[3] = strInOrNotin;
        return result;
    }

    /**
     * loai bo ky tu dac biet tim kiem sql
     *
     * @param input
     * @return
     */
    public static String escapeSql(String input) {
        return input.trim().replace("/", "//")
                .replace("_", "/_").replace("%", "/%");
    }

    /**
     * sap xep thu tu Hmap
     *
     * @param hmap
     * @return
     */
    public static List<String> sortHmap(HashMap<Integer, Object> hmap) {
        List<String> result = new ArrayList<>();
        if (hmap != null) {
            Map<Integer, Object> map = new TreeMap<>(hmap);
            Set set2 = map.entrySet();
            for (Object o : set2) {
                Map.Entry me2 = (Map.Entry) o;
                result.add(me2.getValue().toString());
            }

        }
        return result;
    }

    /**
     * Read a properties file from the classpath and return a Properties object
     *
     * @param filename
     * @return
     */
    static public Properties readFileProperties(String filename) {
        InputStream stream = null;
        try {
            Properties properties = new Properties();
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            stream = loader.getResourceAsStream(filename);
            properties.load(stream);
            return properties;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            LOGGER.error(e);
            return null;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    LOGGER.error(e);
                }
            }
        }
    }

    /**
     * Go bo dau tieng viet
     *
     * @param s
     * @return
     */
    public static String removeAccent(String s) {
        if (s == null) {
            return "";
        }
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace("đ", "d").replace("Đ", "D");
    }

    /**
     * Thuc hien doc file java
     *
     * @param filePath
     * @return
     */
    public static String readLineByLine(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            LOGGER.error(e);
        }
        return contentBuilder.toString();
    }

    /**
     * lay danh sach ten bien tu cau lenh sql
     *
     * @param strSql
     * @return
     */
    public static List<String> getListParamsSql(String strSql) {
        if (strSql == null || strSql.trim().length() <= 0) {
            return null;
        }
        List<String> listParameter = new ArrayList<>();
        String strSqlSub = strSql.replaceAll("\\s+"," ");
        while (true) {            
            int iFirstParam = strSqlSub.indexOf(':');
            if(iFirstParam <= 0){
                break;
            }
            int iLast = -1;
            String strVariable = null;
            int length = strSqlSub.length();
            for (int i = iFirstParam; i < length; i++) {
                if (' ' == strSqlSub.charAt(i)|| ',' == strSqlSub.charAt(i)|| ')' == strSqlSub.charAt(i) || '%' == strSqlSub.charAt(i)) {
                    strVariable = strSqlSub.substring(iFirstParam,i);
                    String strParams = strVariable.replace(":", "").trim();
                    listParameter.add(strParams);
                    iLast = i;
                    break;
                }else if (i == (strSqlSub.length()-1)) {
                    strVariable = strSqlSub.substring(iFirstParam);
                    String strParams = strVariable.replace(":", "").trim();
                    listParameter.add(strParams);
                    iLast = strSqlSub.length();
                    break;
                }
            }
            if (iLast > 0) {
                strSqlSub = strSqlSub.replace(strVariable, "");
            }
        }
        return listParameter;
    }
    
    public static String camelcasify(String in) {
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = false;
        for (char c : in.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    sb.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }
    
    /**
     * lay danh sach params tu url
     * @param urlParams
     * @return 
     */
    public static List<String> getListParamsFromUrl(String urlParams) {
        String s = urlParams;
        List<String> list = new ArrayList<>();
        String strTg = s;
        while (true) {
            int index = s.indexOf("{");
            if (index < 0) {
                break;
            }
            s = s.substring(index + 1);
            int indexEnd = s.indexOf("}");
            if (indexEnd < 0) {
                break;
            }
            s = s.substring(0, indexEnd);
            list.add(s);
            s = strTg.replace("{" + s + "}", "");
            strTg = s;
        }
        LOGGER.info(strTg);
        return list;
    }

    /**
     * lay danh sach bang tu danh sach sql trong 1 class
     *
     * @param itemObject
     * @return
     */
    public static List<String> getListTableFromSql(ObjectEntity itemObject) {
        List<MethodEntity> listMethod = itemObject.getListMethod();
        CommonDataBaseDao commonDataBaseDao = new CommonDataBaseDao();
        List<String> listTable = commonDataBaseDao.getListTableAll();
        if (listTable == null || listTable.size() == 0) {
            return null;
        }
        List<String> strTable = new ArrayList<>();
        for (MethodEntity methodEntity : listMethod) {
            StringBuilder sql = new StringBuilder(methodEntity.getSql());
            List<String> resultTables = commonDataBaseDao.getListTableNameSql(listTable, sql);
            if (resultTables != null && resultTables.size() > 0) {
                strTable.addAll(resultTables);
            }
        }
        List<String> strTableRemoveDup = new ArrayList<>();
        for (String string : strTable) {
            boolean isDup = false;
            if (strTableRemoveDup.size() > 0) {
                for (String string1 : strTableRemoveDup) {
                    if (string.trim().equals(string1.trim())) {
                        isDup = true;
                        break;
                    }
                }
            }
            if (!isDup) {
                strTableRemoveDup.add(string);
            }
        }
        return strTableRemoveDup;
    }
}
