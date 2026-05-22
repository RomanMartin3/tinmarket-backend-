package com.tinmarket.backend;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class TinmarketBackendApplication {
	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Buenos_Aires"));
	}

	public static void main(String[] args) {



		try {
			Dotenv dotenv = Dotenv.configure()
					.ignoreIfMissing()
					.load();

			// Verificamos si logró leer alguna variable
			if (dotenv.entries().isEmpty()) {
				System.err.println("\n 🚨 ATENCIÓN: No se encontró el archivo .env o está vacío en la raíz del proyecto.");
				System.err.println(" 🚨 Si estás en local, Spring Boot va a fallar a continuación.\n");
			} else {
				System.out.println("\n ✅ Archivo .env cargado exitosamente!\n");
				dotenv.entries().forEach(entry -> {
					System.setProperty(entry.getKey(), entry.getValue());
				});
			}
		} catch (Exception e) {
			System.err.println("\n 🚨 Error leyendo el archivo .env: " + e.getMessage() + "\n");
		}

		SpringApplication.run(TinmarketBackendApplication.class, args);
	}

}
