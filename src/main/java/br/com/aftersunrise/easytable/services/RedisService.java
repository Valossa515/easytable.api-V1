package br.com.aftersunrise.easytable.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void salvar(String chave, Object valor, long tempoSegundos) {
        try {
            String json = objectMapper.writeValueAsString(valor);
            redisTemplate.opsForValue().set(chave, json, Duration.ofSeconds(tempoSegundos));
        } catch (Exception e) {
            throw new RuntimeException("Erro ao serializar dado para Redis", e);
        }
    }

    public <T> T buscar(String chave, TypeReference<T> tipo) {
        Object valor = redisTemplate.opsForValue().get(chave);
        if (valor == null) {
            return null;
        }

        String json;
        if (valor instanceof String) {
            json = (String) valor;
        } else {
            json = valor.toString();  // Cuidado: garanta que toString() retorne o JSON correto
        }

        try {
            return objectMapper.readValue(json, tipo);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao desserializar dado do Redis", e);
        }
    }

    public <T> T buscar(String chave, Class<T> clazz) {
        Object valor = redisTemplate.opsForValue().get(chave);
        if (valor == null) {
            return null;
        }

        String json;
        if (valor instanceof String) {
            json = (String) valor;
        } else {
            json = valor.toString();
        }

        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao desserializar dado do Redis", e);
        }
    }

    public void deletar(String chave) {
        redisTemplate.delete(chave);
    }

}
