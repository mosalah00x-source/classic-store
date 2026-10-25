package com.classicstore.config;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class YamlConfigSanityTest {

    @Test
    void testApplicationYamlAndApplicationTestYamlParsingWithoutDuplicateKeys() {
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setAllowDuplicateKeys(false);
        Yaml yaml = new Yaml(loaderOptions);

        // Parse application.yml
        InputStream appYamlStream = getClass().getClassLoader().getResourceAsStream("application.yml");
        assertNotNull(appYamlStream, "application.yml should exist on classpath");
        Map<String, Object> appConfig = yaml.load(appYamlStream);
        assertNotNull(appConfig, "application.yml should be loaded successfully");

        // Parse application-test.yml
        InputStream testYamlStream = getClass().getClassLoader().getResourceAsStream("application-test.yml");
        assertNotNull(testYamlStream, "application-test.yml should exist on classpath");
        Map<String, Object> testConfig = yaml.load(testYamlStream);
        assertNotNull(testConfig, "application-test.yml should be loaded successfully");

        // Verify spring.jpa.hibernate.ddl-auto in application-test.yml is set to "validate"
        @SuppressWarnings("unchecked")
        Map<String, Object> springMap = (Map<String, Object>) testConfig.get("spring");
        assertNotNull(springMap, "spring key should exist in application-test.yml");

        @SuppressWarnings("unchecked")
        Map<String, Object> jpaMap = (Map<String, Object>) springMap.get("jpa");
        assertNotNull(jpaMap, "jpa key should exist under spring in application-test.yml");

        @SuppressWarnings("unchecked")
        Map<String, Object> hibernateMap = (Map<String, Object>) jpaMap.get("hibernate");
        assertNotNull(hibernateMap, "hibernate key should exist under spring.jpa in application-test.yml");

        Object ddlAuto = hibernateMap.get("ddl-auto");
        assertEquals("validate", ddlAuto, "spring.jpa.hibernate.ddl-auto should be 'validate' in application-test.yml");
    }
}
