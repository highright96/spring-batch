package dev.highright96.springbatch.part3;

import org.springframework.batch.item.ItemProcessor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class DuplicateValidationProcessor<T> implements ItemProcessor<T, T> {

    private final Map<String, String> keyPool = new ConcurrentHashMap<>();
    private final boolean allowDuplicate;
    private final Function<T, String> keyExtractor;

    public DuplicateValidationProcessor(Function<T, String> keyExtractor, boolean allowDuplicate) {
        this.allowDuplicate = allowDuplicate;
        this.keyExtractor = keyExtractor;
    }

    @Override
    public T process(T item) throws Exception {
        if (allowDuplicate) return item;

        String key = keyExtractor.apply(item);

        if (keyPool.containsKey(key)) return null;

        keyPool.put(key, key);
        return item;
    }
}
