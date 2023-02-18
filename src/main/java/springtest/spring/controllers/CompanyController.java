package springtest.spring.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api")
public class CompanyController {

    String url = "http://dev3.dansmultipro.co.id/api/recruitment/positions.json";
    RestTemplate restTemplate = new RestTemplate();

    @GetMapping(value = "/companies")
    public List<Object> getCompanies() {
        Object[] companies = restTemplate.getForObject(url, Object[].class);
        System.out.println("datatype :" + companies.getClass().getName());
        return Arrays.asList(companies);
    }

    @SneakyThrows
    @GetMapping(value = "/company/{id}")
    public String getCompanyById(@PathVariable String id) {
        // Make GET request to external API and retrieve response
        String response = restTemplate.getForObject(url, String.class, id);
        // Parse response and extract object with given ID
        JsonNode rootNode = new ObjectMapper().readTree(response);
        JsonNode objectNode = rootNode.findParent("id");

        return objectNode.toString();
    }

    @GetMapping(value = "/exportcsv")
    public ResponseEntity<byte[]> exportCsv() throws Exception {
        String response = restTemplate.getForObject(url, String.class);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(response.toString());
        List<String[]> data = new ArrayList<>();

        for (JsonNode node : rootNode) {
            String[] row = new String[9];
            row[0] = node.get("id").asText();
            row[1] = node.get("type").asText();
            row[2] = node.get("url").asText();
            row[3] = node.get("created_at").asText();
            row[4] = node.get("company").asText();
            row[5] = node.get("company_url").asText();
            row[6] = node.get("location").asText();
            row[7] = node.get("title").asText();
            row[8] = node.get("description").asText();
            data.add(row);
        }

        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer);
        csvWriter.writeAll(data);
        csvWriter.close();

        byte[] bytes = writer.toString().getBytes();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "data.csv");
        headers.setContentLength(bytes.length);

        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
