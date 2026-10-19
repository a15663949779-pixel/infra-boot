package com.example.framework.file.manager;

import com.example.framework.file.config.FileProperties;
import com.example.framework.file.config.FileWebMvcConfig;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.annotation.*;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.nio.file.*;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FilePublicAccessTest {
    @TempDir Path directory;
    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;

    @BeforeEach void setup() throws Exception {
        Files.createDirectories(directory.resolve("avatar"));
        Files.createDirectories(directory.resolve("document"));
        Files.writeString(directory.resolve("avatar/test.png"), "image");
        Files.writeString(directory.resolve("document/secret.txt"), "private-attachment");
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test-path", Map.of("test.file.path", directory.toString())));
        context.register(Config.class);
        context.refresh();
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }
    @AfterEach void close() { context.close(); }

    @Test void avatarsRemainPublic() throws Exception {
        mvc.perform(get("/profile/avatar/test.png")).andExpect(status().isOk());
    }
    @Test void attachmentUrlsCannotBypassAuthenticatedDownloadEndpoint() throws Exception {
        mvc.perform(get("/profile/document/secret.txt")).andExpect(status().isNotFound());
    }
    @Test void avatarTraversalCannotExposeSiblingDirectory() throws Exception {
        mvc.perform(get("/profile/avatar/../document/secret.txt")).andExpect(status().isNotFound());
    }

    @Configuration
    @EnableWebMvc
    @Import(FileWebMvcConfig.class)
    static class Config {
        @Bean @Primary FileProperties properties(Environment environment) {
            FileProperties properties = new FileProperties();
            properties.setBasePath(environment.getRequiredProperty("test.file.path"));
            return properties;
        }
    }
}
