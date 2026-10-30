package com.ronreynolds.wooddb.extractor.hobbithouse;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.util.Set;
import java.util.TreeSet;

/**
 * given the main wood index page at hobbithouse.com, extract the wood-specific page URLs
 */
@Slf4j
public class URLExtractor {
    public static void main(String[] args) throws Exception {
        File input = new File("src/main/resources/source-sites/hobbit-house/hobbit-house.html");
        if (!input.exists()) {
            log.error("File not found: {}", input.getAbsolutePath());
            return;
        }
        Document doc = Jsoup.parse(input, "UTF-8");

        // the URLs have lots of dupes and other weirdness so this should generate the cleanest result
        Set<String> woodPageUrls = new TreeSet<>();

        // wood-specific pages start after the h2#letterA element (fragile but true as of 2026-10-28)
        // we're after the a.href elements after this h2
        // simpler, tho, is to find all a.href that start with "http://hobbithouseinc.com/personal/woodpics/" and then filter out
        // the bits that aren't wood. :-/

        /*
        <h2 id="letterA"></h2>
<b id="acacia"></b>
<a href="http://hobbithouseinc.com/personal/woodpics/acacia.htm" target="_blank"><img src="./HobbitHouse Wood ID
site_files/acacia thumb.jpg" border="0"></a>
<br>acacia (341/335/324)
...
<br><br>
<a href="http://hobbithouseinc.com/personal/woodpics/zz_working.htm" target="_blank"><img src="./HobbitHouse Wood ID
site_files/zz_working thumb.jpg" border="0"></a>
<br>zz_working (111/111/111)
         */
        Elements woodPicsUrls = doc.select("a[href^='http://hobbithouseinc.com/personal/woodpics/']");
        for (Element woodAnchor : woodPicsUrls) {
            String woodUrl = woodAnchor.absUrl("href");
            if (woodUrl.contains("index.htm")           // this includes internal and self-refs
                    || woodUrl.contains("woodpics/_")           // links to non-wood-specific pages
                    || woodUrl.endsWith("zz_working.htm")) {    // a work-in-progress page for as-yet-unidentified woods
                continue;
            }
            woodPageUrls.add(woodUrl);
        }

        File woodDataFile = new File("wood-urls.txt");
        try (FileWriter out = new FileWriter(woodDataFile)) {
            for (String url : woodPageUrls) {
                out.write(url + "\n");
            }
        }

        log.info("Done: {}", woodDataFile.getAbsolutePath());
    }
}
