package live.itrip.agent.util;

import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.Reader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by Feng on 2017/9/8.
 */

public class Parsers {
    /**
     * Parses XML from the given reader with namespace support enabled.
     */
    public static void parse(Reader in, ContentHandler contentHandler)
            throws SAXException, IOException {
        try {
            XMLReader reader
                    = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            reader.setFeature("http://xml.org/sax/features/namespaces", true);
            reader.setContentHandler(contentHandler);
            reader.parse(new InputSource(in));
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
