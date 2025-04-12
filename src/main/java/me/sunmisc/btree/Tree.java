package me.sunmisc.btree;

import java.util.Map;

public interface Tree extends Iterable<Map.Entry<String, String>>, Navigable {

    Tree put(final String key, final String value);

    Tree remove(String key);
}
