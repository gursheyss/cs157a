package edu.sjsu.cs157a.sjsu_event_manager.config;

import javax.sql.DataSource;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulator;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        DataSource dataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .build();
        
        DatabasePopulator populator = new ResourceDatabasePopulator(
            new ClassPathResource("schema.sql")
        );
        
        try {
            populator.populate(dataSource.getConnection());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize test database", e);
        }
        
        return dataSource;
    }
} 