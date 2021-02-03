package com.example.accessingdatar2dbc;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import com.example.accessingdatar2dbc.mapper.MybatisMapper;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.connectionfactory.init.ConnectionFactoryInitializer;
import org.springframework.data.r2dbc.connectionfactory.init.ResourceDatabasePopulator;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.r2dbc.spi.ConnectionFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@SpringBootApplication
public class AccessingDataR2dbcApplication {

    private static final Logger log = LoggerFactory.getLogger(AccessingDataR2dbcApplication.class);

    @Autowired
    CustomerRepository repository;

    @Autowired
    MybatisMapper mybatisMapper;    

    public static void main(String[] args) {
        SpringApplication.run(AccessingDataR2dbcApplication.class, args);
    }

    @Bean
    ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {

        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));

        return initializer;
    }

	@Bean(name = "mainDataSource")
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource mainDataSource() {
		DataSource mainDataSource = DataSourceBuilder.create().build();
		log.debug("mainDataSource={}", mainDataSource);
		return mainDataSource;
    }
    
    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(mainDataSource());
        return sessionFactory.getObject();
    }
        
    @GetMapping("/mb/customers")
    public List<Customer> mbGetCustomers() {
        return mybatisMapper.getList();
    }

    @PostMapping("/r2/customers")
    public Mono<Customer> r2CreateCustomer(@RequestBody Customer customer) {
        log.debug("customer={}", customer);
        return repository.save(customer);
    }

    @GetMapping("/r2/customers/{id}")
    public Mono<Customer> r2GetCustomer(@PathVariable Long id) {
        return repository.findById(id);
    }

    @PutMapping("/r2/customers")
    public Mono<Customer> r2UpdateCustomer(@RequestBody Customer customer) {
        return repository.save(customer);
    }

    @DeleteMapping("/r2/customers")
    public Mono<Void> r2DeleteCustomer(@RequestBody Customer customer) {
        return repository.delete(customer);
    }

    @GetMapping("/r2/customers")
    public Flux<Customer> r2GetCustomers() {
        return repository.findAll();
    }    

    @Bean //구동시 데이터를 넣으려면 주석 해제
    public CommandLineRunner demo(CustomerRepository repository) {

        return (args) -> {
            // save a few customers
            repository.saveAll(Arrays.asList(new Customer("Jack", "Bauer"),
                new Customer("Chloe", "O'Brian"),
                new Customer("Kim", "Bauer"),
                new Customer("David", "Palmer"),
                new Customer("Michelle", "Dessler")))
                .blockLast(Duration.ofSeconds(10));

            // fetch all customers
            log.info("Customers found with findAll():");
            log.info("-------------------------------");
            repository.findAll().doOnNext(customer -> {
                log.info(customer.toString());
            }).blockLast(Duration.ofSeconds(10));

            log.info("");

            // fetch an individual customer by ID
			repository.findById(1L).doOnNext(customer -> {
				log.info("Customer found with findById(1L):");
				log.info("--------------------------------");
				log.info(customer.toString());
				log.info("");
			}).block(Duration.ofSeconds(10));


            // fetch customers by last name
            log.info("Customer found with findByLastName('Bauer'):");
            log.info("--------------------------------------------");
            repository.findByLastName("Bauer").doOnNext(bauer -> {
                log.info(bauer.toString());
            }).blockLast(Duration.ofSeconds(10));;
            log.info("");
        };
    }

}
