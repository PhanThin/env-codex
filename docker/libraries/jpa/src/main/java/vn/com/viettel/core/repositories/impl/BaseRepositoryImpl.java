package vn.com.viettel.core.repositories.impl;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.com.viettel.core.dto.response.BaseResultSelect;
import vn.com.viettel.core.repositories.BaseRepository;
import vn.com.viettel.core.utils.Utils;


import java.util.*;

@Repository
@Transactional
public class BaseRepositoryImpl implements BaseRepository {
    private static final Logger LOGGER = LogManager.getLogger(BaseRepositoryImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Lay list data and count
     *
     * @param queryString
     * @param hmapParams
     * @param pageIndex
     * @param pageSize
     * @param classOfT
     * @return
     */
    @Override
    public BaseResultSelect getListDataAndCount(StringBuilder queryString, HashMap<String, Object> hmapParams, Integer pageIndex, Integer pageSize, Class<?> classOfT) {
        try {
            Integer startPage = null;
            if (pageIndex != null && pageIndex > 0) startPage = (pageIndex + 1) * pageSize;
            boolean databaseIsOracle = false;
            try {
                HikariDataSource hikariDataS = (HikariDataSource) entityManager.getEntityManagerFactory().getProperties().get("hibernate.connection.datasource");
                String strDriverName = hikariDataS.getDriverClassName().toLowerCase();
                if ("oracle".contains(strDriverName)) {
                    databaseIsOracle = true;
                }
            } catch (Exception e) {
                LOGGER.error(e);
            }
            StringBuilder sqlPage = new StringBuilder();
            if (databaseIsOracle && startPage != null && pageSize != null) {
                sqlPage.append(" SELECT * FROM ( ");
                sqlPage.append(queryString.toString());
                sqlPage.append(" ) a ");
                sqlPage.append(String.format("  OFFSET %d ROWS FETCH NEXT %d ROWS ONLY ", startPage, pageSize));
            } else {
                sqlPage = queryString;
            }

            Query query = entityManager.createNativeQuery(sqlPage.toString(), Tuple.class);
            if (hmapParams != null) {
                Set<Map.Entry<String, Object>> set = hmapParams.entrySet();
                for (Object o : set) {
                    Map.Entry mentry = (Map.Entry) o;
                    Object value = mentry.getValue();
                    if (value == null) {
                        value = "";
                    }
                    query.setParameter(mentry.getKey().toString(), value);
                }
            }
            if (startPage != null && pageSize != null && !databaseIsOracle) {
                query.setFirstResult(startPage).setMaxResults(pageSize);
            }

            List objectList = query.getResultList();
            BaseResultSelect result = new BaseResultSelect();
            if (objectList != null) {
                List<Object> listResult = Utils.convertToEntity(objectList, classOfT);
                result.setListData(listResult);
            }
            result.setCount(getCountDataInSelect(queryString, hmapParams));
            return result;
        } catch (Exception e) {
            LOGGER.error(e);
            return null;
        } finally {
            if (entityManager != null) entityManager.close();
        }
    }

    /**
     * Lay list data
     *
     * @param queryString
     * @param hmapParams
     * @param pageIndex
     * @param pageSize
     * @param classOfT
     * @return
     */
    @Override
    public List<? extends Object> getListData(StringBuilder queryString, HashMap<String, Object> hmapParams, Integer pageIndex, Integer pageSize, Class<?> classOfT) {
        try {
            Integer startPage = null;
            if (pageIndex != null && pageIndex > 0) startPage = (pageIndex + 1) * pageSize;
            boolean databaseIsOracle = false;
            try {
                HikariDataSource hikariDataS = (HikariDataSource) entityManager.getEntityManagerFactory().getProperties().get("hibernate.connection.datasource");
                String strDriverName = hikariDataS.getDriverClassName().toLowerCase();
                if ("oracle".contains(strDriverName)) {
                    databaseIsOracle = true;
                }
            } catch (Exception e) {
                LOGGER.error(e);
            }
            StringBuilder sqlPage = new StringBuilder();
            if (databaseIsOracle && startPage != null && pageSize != null) {
                sqlPage.append(" SELECT * FROM ( ");
                sqlPage.append(queryString.toString());
                sqlPage.append(" ) a ");
                sqlPage.append(String.format("  OFFSET %d ROWS FETCH NEXT %d ROWS ONLY ", startPage, pageSize));
            } else {
                sqlPage = queryString;
            }
            Query query = entityManager.createNativeQuery(sqlPage.toString(), Tuple.class);
            if (hmapParams != null) {
                Set<Map.Entry<String, Object>> set = hmapParams.entrySet();
                for (Object o : set) {
                    Map.Entry mentry = (Map.Entry) o;
                    query.setParameter(mentry.getKey().toString(), mentry.getValue());
                }
            }
            if (startPage != null && pageSize != null && !databaseIsOracle) {
                query.setFirstResult(startPage).setMaxResults(pageSize);
            }
            List objectList = query.getResultList();
            return Utils.convertToEntity(objectList, classOfT);
        } catch (Exception e) {
            LOGGER.error(e);
            return null;
        } finally {
            if (entityManager != null) entityManager.close();
        }
    }

    /**
     * Lay phan tu dau tien
     *
     * @param queryString
     * @param hmapParams
     * @param classOfT
     * @return
     */
    @Override
    public Object getFirstData(StringBuilder queryString, HashMap<String, Object> hmapParams, Class<?> classOfT) {
        try {
            Query query = entityManager.createNativeQuery(queryString.toString(), Tuple.class);
            if (hmapParams != null) {
                Set<Map.Entry<String, Object>> set = hmapParams.entrySet();
                for (Object o : set) {
                    Map.Entry mentry = (Map.Entry) o;
                    query.setParameter(mentry.getKey().toString(), mentry.getValue());
                }
            }
            query.setFirstResult(0).setMaxResults(1);
            List objectList = query.getResultList();
            List listResult = Utils.convertToEntity(objectList, classOfT);
            if (listResult.size() > 0) {
                return listResult.get(0);
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.error(e);
            return null;
        } finally {
            if (entityManager != null) entityManager.close();
        }
    }

    /**
     * Dem so luong ban ghi
     *
     * @param queryString
     * @param hmapParams
     * @return
     */
    @Override
    public int getCountData(StringBuilder queryString, HashMap<String, Object> hmapParams) {
        StringBuilder strBuild = new StringBuilder();
        strBuild.append("Select count(1) as count From (");
        strBuild.append(queryString);
        strBuild.append(") tbcount");
        try {
            Query query = entityManager.createNativeQuery(strBuild.toString());
            if (hmapParams != null) {
                Set<Map.Entry<String, Object>> set = hmapParams.entrySet();
                for (Map.Entry<String, Object> entry : set) {
                    Object value = entry.getValue();
                    if (value == null) {
                        value = "";
                    }
                    query.setParameter(entry.getKey(), value);
                }
            }
            List resultQr = query.getResultList();
            if (resultQr != null && resultQr.size() > 0) {
                Object value = resultQr.get(0);
                String result = String.valueOf(value);
                return Integer.parseInt(result);
            }
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            if (entityManager != null) entityManager.close();
        }
        return 0;
    }

    /**
     * Get count data in select
     *
     * @param queryString
     * @param hmapParams
     * @return
     */
    @Override
    public int getCountDataInSelect(StringBuilder queryString, HashMap<String, Object> hmapParams) {
        StringBuilder strBuild = new StringBuilder();
        String strReplace = queryString.toString().trim().replaceAll(" +", " ");
        String strSql = removeOrderBy(strReplace);
        strBuild.append("Select count(1) as count From (");
        strBuild.append(strSql);
        strBuild.append(") tbcount");
        try {
            Query query = entityManager.createNativeQuery(strBuild.toString());
            if (hmapParams != null) {
                Set<Map.Entry<String, Object>> set = hmapParams.entrySet();
                for (Object o : set) {
                    Map.Entry entry = (Map.Entry) o;
                    Object value = entry.getValue();
                    if (value == null) {
                        value = "";
                    }
                    query.setParameter(entry.getKey().toString().toLowerCase(), value);
                }
            }
            List resultQr = query.getResultList();
            if (resultQr != null && resultQr.size() > 0) {
                Object value = resultQr.get(0);
                String result = String.valueOf(value);
                return Integer.parseInt(result);
            }
        } catch (Exception e) {
            LOGGER.error(e);
        } finally {
            if (entityManager != null) entityManager.close();
        }
        return 0;
    }


    /**
     *
     * @param queryString
     * @param hmapParams
     * @return
     */
    @Override
    public Boolean executeSqlDatabase(StringBuilder queryString, HashMap<String, Object> hmapParams) {
        boolean result = true;
        try {
            Query query = entityManager.createNativeQuery(queryString.toString());
            if (hmapParams != null) {
                Set<Map.Entry<String, Object>> set = hmapParams.entrySet();
                for (Object o : set) {
                    Map.Entry mentry = (Map.Entry) o;
                    Object value = mentry.getValue();
                    if (value == null) {
                        value = "";
                    }
                    query.setParameter(mentry.getKey().toString(), value);
                }
            }
            query.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(e);
            result = false;
        } finally {
            if (entityManager != null) entityManager.close();
        }
        return result;
    }

    /**
     * Remove order by
     *
     * @param strReplace
     * @return
     */
    private String removeOrderBy(String strReplace) {
        String strResult = strReplace.toLowerCase();
        int indexLast = strResult.lastIndexOf("order by");
        int indexLastCm = strResult.lastIndexOf(")");
        if (indexLast > 0 && indexLastCm < indexLast) {
            strResult = strResult.substring(0, indexLast);
        }
        return strResult;
    }
}
