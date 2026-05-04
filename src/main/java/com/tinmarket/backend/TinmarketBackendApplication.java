package com.tinmarket.backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TinmarketBackendApplication {

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
