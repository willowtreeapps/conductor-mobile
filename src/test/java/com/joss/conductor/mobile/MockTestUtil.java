package com.joss.conductor.mobile;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.hasEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.appium.java_client.TouchAction;
import org.hamcrest.Matcher;

public class MockTestUtil {
    /**
     * Create a hamcrest matcher that checks if all entries of a map match
     * @param map the map to check
     * @return the hamcrest matcher
     */
    public static <K, V> Matcher<Map<K, V>> matchesEntriesIn(Map<K, V> map) {
        return allOf(buildMatcherArray(map));
    }

    /**
     * Create a hamcrest matcher that checks if any entries of a map match
     * @param map the map to check
     * @return the hamcrest matcher
     */
    public static <K, V> Matcher<Map<K, V>> matchesAnyEntryIn(Map<K, V> map) {
        return anyOf(buildMatcherArray(map));
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Matcher<Map<? extends K, ? extends V>>[] buildMatcherArray(Map<K, V> map) {
        List<Matcher<Map<? extends K, ? extends V>>> entries = new ArrayList<Matcher<Map<? extends K, ? extends V>>>();
        for (K key : map.keySet()) {
            entries.add(hasEntry(key, map.get(key)));

        }
        return entries.toArray(new Matcher[entries.size()]);
    }
}
