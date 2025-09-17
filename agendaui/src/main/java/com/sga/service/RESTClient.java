package com.sga.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RESTClient {
    private static final String BASE = "http://localhost:8080/api";
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private String token;

    public void setToken(String t) { this.token = t; }

    public String login(String email, String password) throws Exception {
        var reqMap = java.util.Map.of("email", email, "password", password);
        String json = mapper.writeValueAsString(reqMap);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() == 200) {
            var node = mapper.readTree(resp.body());
            this.token = node.get("token").asText(); // asignación directa al atributo
            return this.token;
        } else {
            throw new RuntimeException("Login falló: " + resp.body());
        }
    }


    public java.util.List<java.util.Map<String,Object>> listColegios() throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/colegios"))
                .GET();
        if (token != null) b.header("Authorization", "Bearer " + token);

        HttpResponse<String> resp = http.send(b.build(), HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() == 200) {
            return mapper.readValue(
                resp.body(),
                new com.fasterxml.jackson.core.type.TypeReference<
                    java.util.List<java.util.Map<String,Object>>>(){}
            );
        } else {
            throw new RuntimeException("Error al listar colegios: " + resp.body());
        }
    }


    public void crearColegio(java.util.Map<String,String> body) throws Exception {
        String json = mapper.writeValueAsString(body);
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/colegios"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));
        if (token != null) b.header("Authorization", "Bearer " + token);
        var resp = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) throw new RuntimeException(resp.body());
    }
}
