package com.gt.boot.plugin.sqlInit.dao;

import com.gt.boot.plugin.sqlInit.config.DataSourceConfig;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.plexus.util.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 持久层
 *
 * @author maxzhao
 * @date 2022-02-19
 */
@Slf4j
public class DBDao {

    /**
     * SQL连接
     */
    private Connection connection;
    /**
     * 数据库配置
     */
    private DataSourceConfig dataSourceConfig;

    /**
     * 在构造器中处理配置
     *
     * @param dataSourceConfig 数据源配置
     */
    public DBDao(DataSourceConfig dataSourceConfig) {
        this.dataSourceConfig = dataSourceConfig;
    }


    /**
     * 执行 SQL
     *
     * @param sql 待执行的SQL
     * @return 执行结果
     */
    public boolean executeSql(String sql) {
        Statement ps = null;
        connection = dataSourceConfig.getConn();
        try {
            ps = connection.createStatement();
            return ps.execute(sql);
        } catch (SQLException e) {
            log.error("====================== 执行SQL脚本失败 ======================");
            log.error("执行失败SQL脚本：\n{}\n", sql);
            log.error("SQL 执行失败 ", e);
        } finally {
            //释放资源
            try {
                if (ps != null) {
                    ps.close();
                }
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 执行 sqls
     *
     * @param sqlList 待执行的SQLs
     * @return 执行结果
     */
    public boolean executeSqls(List<String> sqlList) {
        Statement ps = null;
        connection = dataSourceConfig.getConn();
        try {
            /*设置手动提交事务*/
            connection.setAutoCommit(false);
            ps = connection.createStatement();
            for (String sql : sqlList) {
                if (StringUtils.isBlank(sql)) {
                    continue;
                }
                ps.addBatch(sql);
            }
            int[] ints = ps.executeBatch();
            /*提交事务*/
            connection.commit();
            log.info("执行成功数据量：{}", ints.length);
            /*设置自动提交事务*/
            connection.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            log.error("====================== 执行SQL脚本失败 ======================");
            /*回滚事务*/
            try {
                connection.rollback();
                log.error("====================== 回滚事务成功 ======================");
            } catch (SQLException ex) {
                log.error("====================== 回滚事务失败 ======================");
            }
            log.error("执行失败SQL脚本：\n{}\n", String.join(";\n", sqlList));
            log.error("SQL 执行失败 ", e);
        } finally {
            //释放资源
            try {
                if (ps != null) {
                    ps.close();
                }
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


}
