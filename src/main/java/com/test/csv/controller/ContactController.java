package com.test.csv.controller;

import com.test.csv.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Controller
@RequestMapping
public class ContactController {

    @Value("${sping.external.service.base-url}")
    private String targetUrl;

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<String> uploadCsvFile(@RequestParam("file") MultipartFile file,
                                                @RequestParam("uploadUrl") String uploadUrl) {
        if (file.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        String response = contactService.CsvToJsonConverter(file, uploadUrl);
        return ResponseEntity.ok("CSV file uploaded successfully");
    }

    @GetMapping
    public ResponseEntity<String> convertJsonToCsv() {
        String result = contactService.jsonToCvsConverter();
        return ResponseEntity.ok(result);
    }
}
