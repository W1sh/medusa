package com.w1sh.medusa.configurations;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import reactor.util.annotation.NonNull;

import java.util.List;

import static io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD;
import static io.r2dbc.spi.ConnectionFactoryOptions.USER;

@Configuration
@EnableR2dbcRepositories(value = "com.w1sh.medusa")
@EnableTransactionManagement
public class R2DBCConfiguration extends AbstractR2dbcConfiguration {

    private final List<Converter<?, ?>> converters;

    @Value("${postgres.user}")
    private String user;
    @Value("${postgres.password}")
    private String password;
    @Value("${postgres.url}")
    private String url;

    public R2DBCConfiguration(List<Converter<?, ?>> converters) {
        this.converters = converters;
    }

    @Bean
    @NonNull
    @Override
    public ConnectionFactory connectionFactory() {
        ConnectionFactoryOptions options = ConnectionFactoryOptions.parse(url).mutate()
                .option(USER, user)
                .option(PASSWORD, password)
                .build();
        return ConnectionFactories.get(options);
    }

    @Bean
    public DatabaseClient databaseClient(){
        return DatabaseClient.create(connectionFactory());
    }

    @Bean
    public ReactiveTransactionManager transactionManager() {
        return new R2dbcTransactionManager(connectionFactory());
    }

    @Bean
    @NonNull
    @Override
    public R2dbcCustomConversions r2dbcCustomConversions() {
        return new R2dbcCustomConversions(converters);
    }
}
