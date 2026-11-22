package com.ronreynolds.util.jsoup;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.LeafNode;
import org.jsoup.nodes.Node;
import org.jspecify.annotations.Nullable;   // pulled in by the Guava library (apparently it's the new standard)

/**
 * some helper methods for working with JSoup
 */
public class JSoup {
    private JSoup() {
    }

    /**
     * extract and return the text representation of a JSoup Node; also fixes the <code>&nbsp;</code> encoding issue
     * @param node the node from which to extract text; may be null
     * @return the text "within" the node or <code>null</code> if <code>node</code> is <code>null</code>
     */
    public static @Nullable String text(@Nullable Node node) {
        if (node == null) {
            return null;
        }
        String text;
        if (node instanceof LeafNode leaf) {
            text = leaf.nodeValue();
        } else if (node instanceof Element element) {
            text = element.ownText();
        } else {
            throw new IllegalStateException("Node only has 2 child types; what is this? " + node);
        }

        // fix non-breaking space encoding issue (JSoup's text() method returns non-breaking spaces as '\u00A0')
        text = text.replace('\u00A0', ' '); // non-breaking space → space
        return text;
    }
}
