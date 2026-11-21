package com.ronreynolds.util.jsoup;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.LeafNode;
import org.jsoup.nodes.Node;

public class JSoup {
    private JSoup() {
    }

    public static String text(Node node) {
        if (node == null) {
            return null;
        }
        if (node instanceof LeafNode leaf) {
            return leaf.nodeValue();
        }
        if (node instanceof Element element) {
            return element.ownText();
        }
        // return something (tho it's probably wrong)
        return node.toString();
    }
}
