package com.w1sh.medusa.configurations;

import com.w1sh.medusa.converters.GuildUserConverter;
import com.w1sh.medusa.converters.PointDistributionConverter;
import com.w1sh.medusa.converters.UserConverter;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.core.DatabaseClient;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import reactor.util.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

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
    @NonNull
    public ConnectionFactory connectionFactory() {
        return ConnectionFactories.get(ConnectionFactoryOptions.builder()
                .option(DRIVER, driver)
                .option(HOST, host)
                .option(USER, user)
                .option(PASSWORD, password)
                .option(DATABASE, database)
                .build());
    }

    @Bean
    public DatabaseClient databaseClient(){
        return DatabaseClient.create(connectionFactory());
    }

    @Bean
    @Override
    public R2dbcCustomConversions r2dbcCustomConversions() {
        List<Converter<?, ?>> converterList = new ArrayList<>();
        converterList.add(new UserConverter.UserReadConverter());
        converterList.add(new UserConverter.UserWriteConverter());
        converterList.add(new GuildUserConverter.GuildUserReadConverter());
        converterList.add(new GuildUserConverter.GuildUserWriteConverter());
        converterList.add(new PointDistributionConverter.PointDistributionReadConverter());
        converterList.add(new PointDistributionConverter.PointDistributionWriteConverter());
        return new R2dbcCustomConversions(getStoreConversions(), converterList);
    }
}
