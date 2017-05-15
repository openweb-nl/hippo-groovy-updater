package nl.openweb.hippo.groovy;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
import com.sun.xml.bind.marshaller.MinimumEscapeHandler;

import nl.openweb.hippo.groovy.model.jaxb.Node;
import nl.openweb.hippo.groovy.model.jaxb.Property;

public class Marshal {

    public static final String CDATA_START = "<![CDATA[";

    private Marshal() {
    }

    public static Marshaller getMarshaller() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Node.class, Property.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(CharacterEscapeHandler.class.getName(), (CharacterEscapeHandler) (chars, start, length, isAttVal, writer) -> {
            boolean cdata = new String(chars).trim().startsWith(CDATA_START);
            if (cdata) {
                writer.write(chars, start, length);
            } else {
                MinimumEscapeHandler.theInstance.escape(chars, start, length, isAttVal, writer);
            }
        });
        return marshaller;
    }

}
