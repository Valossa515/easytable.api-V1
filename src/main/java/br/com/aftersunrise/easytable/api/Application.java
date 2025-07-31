package br.com.aftersunrise.easytable.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "br.com.aftersunrise.easytable")
@EnableMongoRepositories(basePackages = "br.com.aftersunrise.easytable.repositories")
@EntityScan(basePackages = "br.com.aftersunrise.easytable.borders.entities")
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}