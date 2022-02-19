package com.gt.boot.plugin.sqlInit.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugins.annotations.Parameter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 数据库配置
 *
 * @author maxzhao
 * @date 2022-02-19
 */
@Slf4j
public class DataSourceConfig {
    /**
     * 驱动连接的URL
     */
    @Parameter(required = true)
    private String url;
    /**
     * 驱动名称
     */
    @Parameter(required = true)
    private String driverClassName;
    /**
     * 数据库连接用户名
     */
    @Parameter(required = true)
    private String username;
    /**
     * 数据库连接密码
     */
    @Parameter(required = true)
    private String password;

    /**
     * 创建数据库连接对象
     *
     * @return Connection
     */
    public Connection getConn() {
        Connection conn = null;
        try {
            Class.forName(driverClassName);
            conn = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException e) {
            log.error("请添加数据库依赖:比如：\n{}\n",
                    "<dependency>\n" +
                            "    <groupId>mysql</groupId>\n" +
                            "    <artifactId>mysql-connector-java</artifactId>\n" +
                            "</dependency>",
                    e);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return conn;
    }
}
