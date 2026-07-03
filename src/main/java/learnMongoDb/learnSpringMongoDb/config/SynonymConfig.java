package learnMongoDb.learnSpringMongoDb.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class SynonymConfig {

    // Define each group once. Order doesn't matter within a group.
    private static final List<Set<String>> SYNONYM_GROUPS = List.of(
            Set.of("phone", "mobile", "smartphone", "cell phone", "handset"),
            Set.of("tv", "television"),
            Set.of("laptop", "notebook"),
            Set.of("earphone", "earphones", "earbud", "earbuds"),
            Set.of("headphone", "headphones", "headset")
    );

    private final Map<String, Set<String>> lookup = new HashMap<>();

    @PostConstruct
    void buildLookup() {
        for (Set<String> group : SYNONYM_GROUPS) {
            for (String word : group) {
                lookup.put(word.toLowerCase(), group);
            }
        }
    }

    /** * Returns the word itself plus all synonyms, deduplicated.
     * Never returns empty.
     */
    public Set<String> expand(String token) {
        String key = token.toLowerCase();
        Set<String> group = lookup.get(key);
        if (group == null) {
            return Set.of(token);
        }
        return group; // already includes the word itself
    }
}