package ar.santandertecnologia.transferencias.kitkat.commons.logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.ws.client.core.WebServiceTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@RestController
public class TestApplication {

    @Autowired
    WebServiceTemplate webService;

    @PostMapping("/test")
    public ResponseEntity<Map<String, String>> test(@RequestBody String s) {
        Map<String, String> map = new HashMap<>();
        map.put("password", "asdasdasd");
        return ResponseEntity.ok(map);
    }

    @GetMapping("/empty")
    public ResponseEntity<String> testempty() {
        return ResponseEntity.ok("");
    }

    @GetMapping("/external")
    public ResponseEntity<String> testExternal() {
        webService.marshalSendAndReceive("asd");
        return ResponseEntity.ok("");
    }

    @PostMapping("/exception")
    public ResponseEntity<String> testexception() throws Exception {
        throw new Exception("message");
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("health");
    }

    @ExceptionHandler
    public ResponseEntity<String> exceptionHandler(Exception e) {
        return ResponseEntity.status(500).body("");
    }
}
