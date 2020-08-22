package ru.abramov.filemanager.netty.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
public class SpringConfig {

    @Bean
    public SqlClient sqlClient(DataSource dataSource) throws SQLException {
        return new SqlClient(dataSource);
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setUsername("root");
        ds.setPassword("Z4Vesrfd1.");
        ds.setUrl("jdbc:mysql://localhost:3306/mysql_chat?&useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC");
        return ds;
    }

}
