package com.test.csv.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Service
public class ContactService {

    @Value("${sping.external.service.base-url}")
    private String targetUrl;
    RestTemplate restTemplate;
    ObjectMapper mapper;

    public String jsonToCvsConverter() {
        String jsonData = restTemplate.getForObject(targetUrl, String.class);
        JSONParser jsonParser = new JSONParser();
        try {
            Object obj = jsonParser.parse(jsonData);
            JSONArray contacts = (JSONArray) obj;
            try (PrintWriter writer = new PrintWriter(new FileWriter("data.csv"))) {

                for (Object contact : contacts) {
                    JSONObject object = (JSONObject) contact;
                    String name = object.get("name").toString();
                    String formattedPhone = formatPhoneNumberToCsv(object.get("phone").toString());
                    String email = object.get("email").toString();

                    writer.println(name + "," + formattedPhone + "," + email);
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return "Conversion complete";
    }

    private static String formatPhoneNumberToCsv(String phoneNumber) {
        return phoneNumber.replaceFirst("-", "");
    }

    public void uploadContactToUrl(String url, Map<String, String> contactData) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(contactData, headers);

        restTemplate.postForObject(url, request, String.class);
    }

    public String CsvToJsonConverter(MultipartFile file, String uploadUrl) {
        try {
            List<Map<String, String>> jsonDataList = new ArrayList<>();

//            String cvsFile = String.valueOf(file);
            List<String[]> csvData = Files.lines(Paths.get(file.getOriginalFilename()))
                    .map(line -> line.split(","))
                    .toList();

            for (String[] entry : csvData) {
                String name = entry[0];
                String phone = entry[1];
                String email = entry[2];

                String formattedPhone = formatPhoneNumberToJson(phone);

                Map<String, String> jsonMap = new HashMap<>();
                jsonMap.put("name", name);
                jsonMap.put("phone", formattedPhone);
                jsonMap.put("email", email);

                jsonDataList.add(jsonMap);

                uploadContactToUrl(targetUrl, jsonMap);
            }

            String jsonData = mapper.writeValueAsString(jsonDataList);
            System.out.println(jsonData);

            try (Writer writer = new FileWriter("\\src\\main\\resources\\Files\\data.json")) {
                mapper.writeValue(writer, jsonDataList);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return " File successfully uploaded";
    }

    private static String formatPhoneNumberToJson(String phoneNumber) {
        String pattern;
        String replacement = "$1-$2-$3";
        if (phoneNumber.length() >= 10) {
            pattern = "(\\d{3})(\\d{3})(\\d{4})";
        } else {
            pattern = "(\\d{2})(\\d{3})(\\d{4})";
        }
        return phoneNumber.replaceFirst(pattern, replacement);
    }
}