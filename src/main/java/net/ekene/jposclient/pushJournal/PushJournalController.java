package net.ekene.jposclient.pushJournal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RequestMapping("/v1/push")
@RestController
@Slf4j
public class PushJournalController {

    @PostMapping
    public ResponseEntity<String> pushJournal(){
        return ResponseEntity.ok(newCall());
    }

    private String newCall() {
        Journal journalEntry = new Journal().returnJournal();

        HttpRequest httpRequest
                = HttpRequest.newBuilder()
                .uri(URI.create("http://52.234.156.59:31000/pushjournal/api/push-journal/"))
                .header("Content-Type", "application/json")
                .header("x-api-key", "zsLive_8748261147813940309")
                .header("accept", "application/json")
                .method("POST", HttpRequest.BodyPublishers.ofString(String.valueOf(journalEntry)))
                .build();

        try {
            HttpResponse<String> response
                    = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return "Push Journal Post Successful " + response.body();
        } catch (IOException | InterruptedException e) {
            return "Push Journal Post failed " + e.getMessage();

        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    class Journal{
        String rrn;
        String stan;
        String acquirerBank;
        int amount;
        String accountNumber;
        String pan;
        String transactionStatus;
        String currencyCode;
        String comment;
        String transactionDate;
        String transactionTime;
        String error;
        String terminalId;
        String pOSDisputeJournal;

        Journal returnJournal(){
            return new Journal(
                    "123456789012",
                    "654321",
                    "Ekene Bank",
                    5000,
                    "1234567890123456",
                    "555940******8222",
                    "APPROVED",
                    "566",
                    "Sample transaction",
                    "05/09/2024",
                    "14:35",
                    "Unprocessed error",
                    "2345TU89",
                    "Test"
            );
        }
    }

}
