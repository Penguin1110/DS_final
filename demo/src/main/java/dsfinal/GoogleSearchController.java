package dsfinal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@CrossOrigin("*")
public class GoogleSearchController {

    @Value("${google.cse.apiKey}")
    private String apiKey;

    @Value("${google.cse.cx}")
    private String cx;

    @GetMapping("/search")
    public List<SearchResult> search(@RequestParam String q) {
        List<SearchResult> results = new ArrayList<>();
        System.out.println("收到搜尋字詞: " + q);

        try {
            String encoded = URLEncoder.encode(q, StandardCharsets.UTF_8);
            String url = "https://www.googleapis.com/customsearch/v1?key=" + apiKey +
                         "&cx=" + cx + "&num=10&q=" + encoded;

            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> resp = restTemplate.getForObject(url, Map.class);

            if (resp != null && resp.containsKey("items")) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) resp.get("items");
                for (Map<String, Object> item : items) {
                    String title = item.get("title").toString();
                    String link = item.get("link").toString();
                    results.add(new SearchResult(title, link));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }
}
