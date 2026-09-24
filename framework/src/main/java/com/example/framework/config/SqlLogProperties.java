package com.example.framework.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.sql-log")
public class SqlLogProperties {

    /**
     * SQL 监听模式：off / summary / detail
     * - off:     关闭 SQL 监听
     * - summary: 仅记录 Mapper 方法、耗时、状态（一行概要）
     * - detail:  记录完整 SQL、参数、耗时、状态（详细模式）
     */
    private String mode = "summary";
}
