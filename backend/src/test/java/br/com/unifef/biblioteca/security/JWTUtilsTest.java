package br.com.unifef.biblioteca.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link JWTUtils}: geração, validação e expiração de tokens JWT
 * usados na autenticação da API.
 */
class JWTUtilsTest {

    // HS512 exige uma chave com pelo menos 512 bits (64 bytes)
    private static final String SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    private JWTUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JWTUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtils, "expiration", 3600000L); // 1 hora
    }

    @Test
    void generateToken_geraTokenNaoNuloComTresPartes() {
        String token = jwtUtils.generateToken("usuario@teste.com");

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void isTokenValid_tokenReceGeradoValido_retornaTrue() {
        String token = jwtUtils.generateToken("usuario@teste.com");

        assertThat(jwtUtils.isTokenValid(token)).isTrue();
    }

    @Test
    void getUsername_extraiSubjectCorretamente() {
        String token = jwtUtils.generateToken("usuario@teste.com");

        assertThat(jwtUtils.getUsername(token)).isEqualTo("usuario@teste.com");
    }

    @Test
    void isTokenValid_tokenExpirado_retornaFalse() {
        ReflectionTestUtils.setField(jwtUtils, "expiration", -1000L); // já expirado ao gerar
        String tokenExpirado = jwtUtils.generateToken("usuario@teste.com");

        assertThat(jwtUtils.isTokenValid(tokenExpirado)).isFalse();
    }

    @Test
    void isTokenValid_tokenMalformado_retornaFalse() {
        assertThat(jwtUtils.isTokenValid("token.invalido.xyz")).isFalse();
    }

    @Test
    void isTokenValid_tokenAssinadoComOutraChave_retornaFalse() {
        String token = jwtUtils.generateToken("usuario@teste.com");

        JWTUtils outroUtils = new JWTUtils();
        ReflectionTestUtils.setField(outroUtils, "secret", "fedcba9876543210fedcba9876543210fedcba9876543210fedcba9876543210");
        ReflectionTestUtils.setField(outroUtils, "expiration", 3600000L);

        assertThat(outroUtils.isTokenValid(token)).isFalse();
    }

    @Test
    void getUsername_tokenInvalido_retornaNull() {
        assertThat(jwtUtils.getUsername("token-completamente-invalido")).isNull();
    }
}
