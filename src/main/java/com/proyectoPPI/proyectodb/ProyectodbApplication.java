package com.proyectoPPI.proyectodb;

import com.proyectoPPI.proyectodb.controllers.UsuariosController;
import com.proyectoPPI.proyectodb.db.Db;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProyectodbApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProyectodbApplication.class, args);
	}

	@Bean
	public CommandLineRunner onStartup(Db db, UsuariosController usuariosController,
										@Value("${server.port:4000}") String port) {
		return args -> {
			if (db.isAvailable()) {
				System.out.println("Conectados a la Base de datos");
			}
			usuariosController.seedAdminUser();
			System.out.println("Conectado al puerto " + port);
		};
	}

}
