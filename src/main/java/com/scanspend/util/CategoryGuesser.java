package com.scanspend.util;

public class CategoryGuesser {
    public static String guess(String merchant, String text) {
        String source = (merchant + " " + (text == null ? "" : text)).toLowerCase();
        if (containsAny(source, "uber", "ola", "metro", "cab", "train", "bus")) return "Transport";
        if (containsAny(source, "zomato", "swiggy", "restaurant", "cafe", "pizza", "food")) return "Food";
        if (containsAny(source, "amazon", "flipkart", "d mart", "reliance", "bazaar", "mall", "shopping")) return "Shopping";
        if (containsAny(source, "electric", "water", "gas", "internet", "phone", "bill")) return "Bills";
        if (containsAny(source, "movie", "netflix", "prime", "entertainment", "ticket")) return "Entertainment";
        return "General";
    }

    private static boolean containsAny(String src, String... keys) {
        for (String k : keys) {
            if (src.contains(k)) return true;
        }
        return false;
    }
}
