package me.sunmisc.btree;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public interface Navigable {

    Optional<String> get(String key);

    Optional<Map.Entry<String, String>> first();

    Optional<Map.Entry<String, String>> last();

    void forEach(BiConsumer<String, String> action);
}
