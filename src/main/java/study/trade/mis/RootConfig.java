package study.trade.mis;

import javax.sql.DataSource;
import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@PropertySource("classpath:jdbc.properties")
@EnableTransactionManagement
@MapperScan(basePackageClasses = ProductMapper.class, annotationClass = Mapper.class)
@Import({ProductService.class, CatalogService.class, TransactionExamples.class})
public class RootConfig {
    @Bean(destroyMethod = "close")
    public DruidDataSource dataSource(Environment env) {
        DruidDataSource ds = new DruidDataSource();
        ds.setUrl(env.getRequiredProperty("trade.jdbc.url"));
        ds.setUsername(env.getProperty("trade.jdbc.user"));
        ds.setPassword(env.getProperty("trade.jdbc.password"));
        ds.setMaxActive(8);
        return ds;
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        MybatisConfiguration config = new MybatisConfiguration();
        config.setMapUnderscoreToCamelCase(true);
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setConfiguration(config);
        factory.setPlugins(interceptor);
        return factory.getObject();
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
