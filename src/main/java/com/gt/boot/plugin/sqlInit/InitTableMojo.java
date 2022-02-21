package com.gt.boot.plugin.sqlInit;

import com.gt.boot.plugin.sqlInit.config.DataSourceConfig;
import com.gt.boot.plugin.sqlInit.dao.DBDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * InitTableMojo
 *
 * @author maxzhao
 * @date 2022-02-19 10:59
 */
@Slf4j
@Mojo(name = "initTable", threadSafe = true)
public class InitTableMojo extends AbstractMojo {
    /**
     * 数据源配置
     */
    @Parameter(required = true)
    private DataSourceConfig dataSource;
    @Parameter(required = true)
    private File sqlFile;
    @Parameter
    private String sqlFileEncoding;
    /**
     * SQL 分隔符
     */
    static final String DEFAULT_DELIMITER = ";";

    protected DBDao config;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (this.sqlFileEncoding == null) {
            this.sqlFileEncoding = StandardCharsets.UTF_8.toString();
        }
        log.info("====================== 开始 连接数据库 ======================");
        this.config = new DBDao(this.dataSource);
        log.info("====================== 开始解析SQL脚本 ======================");
        List<String> sqlList = readSQLs(this.sqlFile);
        if (sqlList.isEmpty()) {
            log.warn("====================== SQL脚本不存在 ======================");
            return;
        }
        log.info("====================== 开始执行SQL脚本 ======================");
        if (this.config.executeSqls(sqlList)) {
            log.info("====================== 执行SQL脚本成功 ======================");
        }
    }


    /**
     * 读取文本文件内容
     *
     * @param sqlFile 文本文件
     * @return 结果
     */
    public String read(File sqlFile) {
        if (sqlFile == null
                || !sqlFile.exists()
                || sqlFile.isDirectory()) {
            return null;
        }
        if (!sqlFile.getName().endsWith(".sql")
                && !sqlFile.getName().endsWith(".txt")) {
            log.warn("SQL脚本 文件只能为 .sql 或 .txt ");
            return null;
        }
        StringBuilder sql = new StringBuilder();
        InputStreamReader isr = null;
        BufferedReader reader = null;
        try {
            isr = new InputStreamReader(Files.newInputStream(sqlFile.toPath()), sqlFileEncoding);
            reader = new BufferedReader(isr);
            char[] buf = new char[50];
            int len;
            while ((len = reader.read(buf)) != -1) {
                sql.append(buf, 0, len);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return sql.toString();
    }

    /**
     * 读取文本文件内容
     *
     * @param sqlFile 文本文件
     * @return 结果
     */
    public List<String> readSQLs(File sqlFile) {
        /*待执行的SQL脚本*/
        List<String> sqlList = new ArrayList<>();
        if (sqlFile == null
                || !sqlFile.exists()
                || sqlFile.isDirectory()) {
            return sqlList;
        }
        if (!sqlFile.getName().endsWith(".sql")
                && !sqlFile.getName().endsWith(".txt")) {
            log.warn("SQL脚本 文件只能为 .sql 或 .txt ");
            return sqlList;
        }
        StringBuilder sqlTemp = new StringBuilder();
        InputStreamReader isr = null;
        BufferedReader reader = null;
        try {
            isr = new InputStreamReader(Files.newInputStream(sqlFile.toPath()), sqlFileEncoding);
            reader = new BufferedReader(isr);
            String sqlLine;
            while ((sqlLine = reader.readLine()) != null) {

                /*去除左右空格*/
                sqlLine = sqlLine.trim();
                if (sqlLine.isEmpty() || sqlLine.startsWith("--")
                        || sqlLine.startsWith("//")) {
                    /*Do nothing*/
                    continue;
                }
                /*先添加当前行数据*/
                sqlTemp.append(sqlLine)
                        .append(" ");
                if (sqlLine.endsWith(DEFAULT_DELIMITER)) {
                    /*最后是否为分号*/
                    sqlList.add(sqlTemp.substring(0, sqlTemp.length() - 1));
                    sqlTemp = new StringBuilder();
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return sqlList;
    }
}
