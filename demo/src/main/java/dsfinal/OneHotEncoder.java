package dsfinal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OneHotEncoder {
    
   
    public List<String> encode(String query) {
        if (query == null || query.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 去除前後空白並以空白切割
        String[] tokens = query.trim().split("\\s+");
        
        List<String> keywords = new ArrayList<>();
        Collections.addAll(keywords, tokens);
        
        return keywords;
    }
}