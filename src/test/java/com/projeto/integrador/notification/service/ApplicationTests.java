package com.projeto.integrador.notification.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Teste de integração — requer infraestrutura completa (Kafka).
 * Desabilitado na suite de testes unitários.
 */
@Disabled("Teste de integração: requer Kafka rodando")
@SpringBootTest
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}
