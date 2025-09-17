package com.sga.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RESTClient {
    private static final String BASE = "http://localhost:8080/api";
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private String token;

    public void setToken(String t) { this.token = t; }

    // ---------------- LOGIN ----------------
    public String login(String email, String password) throws Exception {
        var reqMap = Map.of("email", email, "password", password);
        String json = mapper.writeValueAsString(reqMap);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() == 200) {
            var node = mapper.readTree(resp.body());
            this.token = node.get("token").asText();
            return this.token;
        } else {
            throw new RuntimeException("Login falló: " + resp.body());
        }
    }

    // ---------------- GENERIC HELPERS ----------------
    private HttpRequest.Builder withAuth(HttpRequest.Builder builder) {
        if (token != null) builder.header("Authorization", "Bearer " + token);
        return builder;
    }

    private <T> T parseResponse(HttpResponse<String> resp, TypeReference<T> ref, String errorMsg) throws Exception {
        if (resp.statusCode() == 200) {
            return mapper.readValue(resp.body(), ref);
        } else {
            throw new RuntimeException(errorMsg + ": " + resp.body());
        }
    }

    // ---------------- COLEGIOS ----------------
    public List<Map<String,Object>> listColegios() throws Exception {
        HttpRequest req = withAuth(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/colegios"))
                .GET()).build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        return parseResponse(resp, new TypeReference<>() {}, "Error al listar colegios");
    }

    public void crearColegio(Map<String,String> body) throws Exception {
        String json = mapper.writeValueAsString(body);
        HttpRequest req = withAuth(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/colegios"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) throw new RuntimeException(resp.body());
    }

    public void actualizarColegio(Long id, Map<String,String> body) throws Exception {
        String json = mapper.writeValueAsString(body);
        HttpRequest req = withAuth(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/colegios/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json)))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) throw new RuntimeException(resp.body());
    }

    public void eliminarColegio(Long id) throws Exception {
        HttpRequest req = withAuth(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/colegios/" + id))
                .DELETE())
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) throw new RuntimeException(resp.body());
    }

    // ---------------- TALLERES ----------------
    public List<Map<String,Object>> listTalleres() throws Exception {
        HttpRequest req = withAuth(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/talleres"))
                .GET()).build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        return parseResponse(resp, new TypeReference<>() {}, "Error al listar talleres");
    }

    public void crearTaller(Map<String,String> body) throws Exception {
        String json = mapper.writeValueAsString(body);
        HttpRequest req = withAuth(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/talleres"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) throw new RuntimeException(resp.body());
    }

    public void actualizarTaller(Long id, Map<String,String> body) throws Exception {
        String json = mapper.writeValueAsString(body);
        HttpRequest req = withAuth(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/talleres/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json)))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) throw new RuntimeException(resp.body());
    }

    public void eliminarTaller(Long id) throws Exception {
        HttpRequest req = withAuth(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/talleres/" + id))
                .DELETE())
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) throw new RuntimeException(resp.body());
    }

    // ---------------- USUARIOS ----------------
    public List<Map<String,Object>> listUsuarios() throws Exception {
        HttpRequest req = withAuth(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/usuarios"))
                .GET()).build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        return parseResponse(resp, new TypeReference<>() {}, "Error al listar usuarios");
    }

    public void crearUsuario(Map<String,String> body) throws Exception {
        String json = mapper.writeValueAsString(body);
        HttpRequest req = withAuth(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/usuarios"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) throw new RuntimeException(resp.body());
    }

    public void actualizarUsuario(Long id, Map<String,String> body) throws Exception {
        String json = mapper.writeValueAsString(body);
        HttpRequest req = withAuth(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/usuarios/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json)))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) throw new RuntimeException(resp.body());
    }

    public void eliminarUsuario(Long id) throws Exception {
        HttpRequest req = withAuth(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/usuarios/" + id))
                .DELETE())
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) throw new RuntimeException(resp.body());
    }

    public void eliminarUsuarioByEmail(String email) throws Exception {
        HttpRequest req = withAuth(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/usuarios/email/" + email))
                .DELETE())
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) throw new RuntimeException(resp.body());
    }

    // ---------------- AGENDA ----------------
    public List<Map<String,Object>> listAgenda() throws Exception {
        HttpRequest req = withAuth(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/agenda"))
                .GET()).build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        return parseResponse(resp, new TypeReference<>() {}, "Error al listar agenda");
    }

    public void crearAgenda(Map<String,String> body) throws Exception {
        String json = mapper.writeValueAsString(body);
        HttpRequest req = withAuth(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/agenda"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) throw new RuntimeException(resp.body());
    }

    public void actualizarAgenda(Long id, Map<String,String> body) throws Exception {
        String json = mapper.writeValueAsString(body);
        HttpRequest req = withAuth(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/agenda/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json)))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) throw new RuntimeException(resp.body());
    }

    public void eliminarAgenda(Long id) throws Exception {
        HttpRequest req = withAuth(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/agenda/" + id))
                .DELETE())
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) throw new RuntimeException(resp.body());
    }

    // ---------------- REPORTES ----------------
    public List<Map<String,Object>> listReportes() throws Exception {
        HttpRequest req = withAuth(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/reportes"))
                .GET()).build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        return parseResponse(resp, new TypeReference<>() {}, "Error al listar reportes");
    }

    public void crearReporte(Map<String,String> body) throws Exception {
        String json = mapper.writeValueAsString(body);
        HttpRequest req = withAuth(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/reportes"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) throw new RuntimeException(resp.body());
    }

    public void actualizarReporte(Long id, Map<String,String> body) throws Exception {
        String json = mapper.writeValueAsString(body);
        HttpRequest req = withAuth(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/reportes/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json)))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) throw new RuntimeException(resp.body());
    }

    public void eliminarReporte(Long id) throws Exception {
        HttpRequest req = withAuth(HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/reportes/" + id))
                .DELETE())
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() >= 400) throw new RuntimeException(resp.body());
    }
    
    public byte[] downloadReportesExcel() throws Exception {
    HttpRequest req = withAuth(HttpRequest.newBuilder()
            .uri(URI.create(BASE + "/reportes/export"))
            .GET())
            .build();

    HttpResponse<byte[]> resp = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
        if (resp.statusCode() == 200) {
            return resp.body();
        } else {
            throw new RuntimeException("Error al exportar Excel: " + resp.statusCode());
        }
    }
}
