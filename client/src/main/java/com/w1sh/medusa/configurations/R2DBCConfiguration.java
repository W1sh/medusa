package com.w1sh.medusa.configurations;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

@Configuration
@EnableR2dbcRepositories(value = "com.w1sh.medusa")
public class R2DBCConfiguration extends AbstractR2dbcConfiguration{

    @Value("${postgres.driver}")
    private String driver;
    @Value("${postgres.host}")
    private String host;
    @Value("${postgres.user}")
    private String user;
    @Value("${postgres.password}")
    private String password;
    @Value("${postgres.database}")
    private String database;

    @Override
    public ConnectionFactory connectionFactory() {
        return ConnectionFactories.get(ConnectionFactoryOptions.builder()
                .option(DRIVER, driver)
                .option(HOST, host)
                .option(USER, user)
                .option(PASSWORD, password)
                .option(DATABASE, database)
                .build());
    }

    public @Bean
    DatabaseClient databaseClient(){
        return DatabaseClient.create(connectionFactory());
    }
}