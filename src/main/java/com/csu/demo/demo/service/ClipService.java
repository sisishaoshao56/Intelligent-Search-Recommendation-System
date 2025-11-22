package com.csu.demo.demo.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.csu.demo.demo.domain.MultipartInputStreamFileResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ClipService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String BASE_URL = "http://127.0.0.1:8000";

    public List<Float> encodeText(String text) {
        String url = BASE_URL + "/encode_text?q=" + text;
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        return toFloatList(response.get("vector"));
    }
    public List<Float> encodeImage(MultipartFile file) throws IOException {
        String url = BASE_URL + "/encode_image";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

        return toFloatList(response.get("vector"));
    }

    private List<Float> toFloatList(Object rawVector) {
        if (rawVector == null) {
            return List.of();
        }
        List<?> rawList = (List<?>) rawVector;
        return rawList.stream()
                .map(val -> ((Number) val).floatValue())
                .toList();
    }
}
