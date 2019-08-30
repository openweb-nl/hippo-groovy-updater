/*
 * Copyright 2019 Open Web IT B.V. (https://www.openweb.nl/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    public static final String INDENT_STRING = "com.sun.xml.bind.indentString";
    public static final String INDENTATION = "  ";

    private Marshal() {
    }

    public static Marshaller getMarshaller() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Node.class, Property.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(INDENT_STRING, INDENTATION);
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
