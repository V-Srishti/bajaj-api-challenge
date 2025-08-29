package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        // Step 1: First API call - generateWebhook
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "V. Srishti");        
        requestBody.put("regNo", "22BEC1517");       
        requestBody.put("email", "vsrishti.vsr@gmail.com");  

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        // API call
        Map response = restTemplate.postForObject(url, entity, Map.class);

        System.out.println("Response from generateWebhook: " + response);

        // Extract webhook + token
        String webhookUrl = (String) response.get("webhook");
        String accessToken = (String) response.get("accessToken");

        // Step 2: Your SQL query
        String finalSqlQuery =
                "SELECT p.AMOUNT AS SALARY, " +
                "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, " +
                "TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, " +
                "d.DEPARTMENT_NAME " +
                "FROM PAYMENTS p " +
                "JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
                "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
                "WHERE DAY(p.PAYMENT_TIME) <> 1 " +
                "ORDER BY p.AMOUNT DESC LIMIT 1;";

        // Step 3: Submit SQL to webhook
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);

        Map<String, String> sqlBody = new HashMap<>();
        sqlBody.put("finalQuery", finalSqlQuery);

        HttpEntity<Map<String, String>> sqlEntity = new HttpEntity<>(sqlBody, headers);

        String result = restTemplate.postForObject(webhookUrl, sqlEntity, String.class);

        System.out.println("Submission Result: " + result);
    }
}
