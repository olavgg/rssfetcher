package com.capgemini;

import groovy.sql.GroovyRowResult;
import groovy.sql.Sql;
import org.postgresql.ds.PGPoolingDataSource;

import java.sql.SQLException;
import java.util.*;

/**
 * User: olavgjerde
 * Date: 11/08/14
 * Time: 16:07
 */
public class AppDB {

    private static PGPoolingDataSource source = new PGPoolingDataSource();

    public static void initDBPool(){
        source.setDataSourceName("SearchProto");
        source.setServerName("localhost");
        source.setDatabaseName("searchproto");
        source.setUser("cap");
        source.setPassword("capgemini");
        source.setMaxConnections(10);
    }

    public static void closeDB(){
        source.close();
    }

    public static List<Map<Object, Object>>
    rows(String sql) {
        return rows(sql, new HashMap<>());
    }

    public static Sql getSession(){
        return new Sql(source);
    }

    public static List<Map<Object, Object>>
    rows(String sql, Map<Object, Object> params){
        List<Map<Object, Object>> results = new ArrayList<>();
        Sql db = new Sql(source);
        try {
            db.rows(params, sql)
                    .parallelStream()
                    .map(GroovyRowResult::entrySet)
                    .forEach( entry -> {
                        Iterator it = entry.iterator();
                        Map<Object,Object> m = new HashMap<>();
                        while (it.hasNext()) {
                            Map.Entry pairs = (Map.Entry)it.next();
                            m.put(pairs.getKey(), pairs.getValue());
                            it.remove();
                        }
                        results.add(m);
                    });
        } catch (SQLException e){
            e.printStackTrace();
        }
        return results;
    }

    public static boolean execute(String sql){
        return execute(sql, new HashMap<>());
    }

    public static boolean execute(String sql, Map<Object, Object> params){
        Sql db = new Sql(source);
        try {
            return db.execute(params, sql);
        } catch (SQLException e) {
            System.out.println(params.entrySet());
            e.printStackTrace();
        }
        return false;
    }
}
