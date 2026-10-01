package com.box.service;

import com.box.utils.QWeatherJwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final QWeatherJwtUtil jwtUtil;

    @Value("${qweather.host}")
    private String host;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode getWeather(String longitude, String latitude) {
        try {
            String jwt = jwtUtil.generateToken();

            // 1. 经纬度查城市
            String geoUrl = host + "/geo/v2/city/lookup?location=" + longitude + "," + latitude;
            JsonNode geoJson = get(geoUrl, jwt);
            JsonNode loc = geoJson.get("location").get(0);
            String locationId = loc.get("id").asText();

            // 2. 实时天气
            String nowUrl = host + "/v7/weather/now?location=" + locationId;
            JsonNode nowJson = get(nowUrl, jwt).get("now");

            // 3. 24小时预报
            String hourlyUrl = host + "/v7/weather/24h?location=" + locationId;
            JsonNode hourlyJson = get(hourlyUrl, jwt).get("hourly");

            // 4. 拼装前端要的结构
            ObjectNode result = objectMapper.createObjectNode();
            result.put("success", true);

            ObjectNode location = result.putObject("location");
            location.put("country", loc.path("country").asText(""));
            location.put("adm1", loc.path("adm1").asText(""));
            location.put("adm2", loc.path("adm2").asText(""));
            location.put("name", loc.path("name").asText(""));

            ObjectNode now = result.putObject("now");
            now.put("icon", nowJson.path("icon").asText(""));
            now.put("temp", nowJson.path("temp").asText(""));
            now.put("text", nowJson.path("text").asText(""));

            ArrayNode hourly = result.putArray("hourly");
            int count = 0;
            for (JsonNode h : hourlyJson) {
                if (count++ >= 5) break;
                ObjectNode item = hourly.addObject();
                item.put("fxTime", h.path("fxTime").asText(""));
                item.put("icon", h.path("icon").asText(""));
                item.put("temp", h.path("temp").asText(""));
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取天气失败", e);
        }
    }

    private JsonNode get(String url, String jwt) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwt);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<byte[]> resp = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
        byte[] body = resp.getBody();

        if (body != null && body.length > 2 && body[0] == (byte) 0x1F && body[1] == (byte) 0x8B) {
            try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(body))) {
                body = gzip.readAllBytes();
            }
        }

        return objectMapper.readTree(new String(body, StandardCharsets.UTF_8));
    }
}