package com.example.framework.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.core.env.Environment;

import java.net.InetAddress;

/**
 * @WJL
 * @date 2026/9/15 10:00
 * @description
 */
@Slf4j
@Component
public class ProjectStartupPrinter implements ApplicationRunner {

    private final Environment env;

    public ProjectStartupPrinter(Environment env) {
        this.env = env;
    }

    @Override
    public void run(ApplicationArguments args) {
        String appName = env.getProperty("spring.application.name", "infra-boot");
        String port = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "");
        if (StringUtils.hasText(contextPath) && !contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }

        String ip = "127.0.0.1";
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ignored) {
        }

        String localUrl = String.format("http://localhost:%s%s", port, contextPath);
        String networkUrl = String.format("http://%s:%s%s", ip, port, contextPath);
        String docUrl = String.format("http://localhost:%s%s/doc.html", port, contextPath);
        String[] profiles = env.getActiveProfiles();
        String profile = profiles.length > 0 ? String.join(",", profiles) : "default";

        // 亮青、亮绿、黄色等 ANSI 色彩控制
        System.out.println(String.format("""
                
                \u001B[90m----------------------------------------------------------\u001B[0m
                \u001B[92;1m(♥◠‿◠)ﾉﾞ  %s 后台服务启动成功！\u001B[0m
                \u001B[90m----------------------------------------------------------\u001B[0m
                  \u001B[96m:: 运行环境 ::\u001B[0m  \u001B[93m%s\u001B[0m
                  \u001B[96m:: 本地入口 ::\u001B[0m  \u001B[92m%s\u001B[0m
                  \u001B[96m:: 局域网络 ::\u001B[0m  \u001B[92m%s\u001B[0m
                  \u001B[96m:: 接口文档 ::\u001B[0m  \u001B[94m%s\u001B[0m
                \u001B[90m----------------------------------------------------------\u001B[0m
                """, appName, profile, localUrl, networkUrl, docUrl));
    }
}