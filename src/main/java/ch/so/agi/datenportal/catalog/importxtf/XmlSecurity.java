package ch.so.agi.datenportal.catalog.importxtf;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;

public final class XmlSecurity {

    private XmlSecurity() {}

    public static XMLInputFactory secureXmlInputFactory() {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setXMLResolver(rejectingResolver());
        return factory;
    }

    private static XMLResolver rejectingResolver() {
        return (publicId, systemId, baseUri, namespace) -> {
            throw new XMLStreamException("External entity resolution is disabled");
        };
    }
}
