package com.mlyauth.security.sso;

import org.opensaml.Configuration;
import org.opensaml.common.impl.SecureRandomIdentifierGenerator;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeValue;
import org.opensaml.saml2.metadata.impl.EntityDescriptorImpl;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;

@Component
public class SAMLHelper {
    private static Logger logger = LoggerFactory.getLogger(SAMLHelper.class);


    @Autowired
    private ParserPool parserPool;

    private static SecureRandomIdentifierGenerator randomIdGenerator;


    static {
        try {
            randomIdGenerator = new SecureRandomIdentifierGenerator();
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String generateRandomId() {
        return randomIdGenerator.generateIdentifier();
    }

    public static <T> T buildSAMLObject(final Class<T> clazz) {
        try {

            XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
            QName defaultElementName = (QName) clazz.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null);
            return (T) builderFactory.getBuilder(defaultElementName).buildObject(defaultElementName);

        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalArgumentException("Could not create SAML object");
        }
    }

    public EntityDescriptorImpl toMetadata(String content) throws Exception {
        final Document doc = parserPool.parse(new ByteArrayInputStream(content.getBytes()));
        UnmarshallerFactory unmarshallerFactory = org.opensaml.xml.Configuration.getUnmarshallerFactory();
        Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(doc.getDocumentElement());
        XMLObject xmlObject = unmarshaller.unmarshall(doc.getDocumentElement());
        return (EntityDescriptorImpl) xmlObject;
    }

    public Attribute buildStringAttribute(String attName, String attValue) {
        Attribute attribute = SAMLHelper.buildSAMLObject(Attribute.class);
        attribute.setName(attName);
        XSStringBuilder stringBuilder = (XSStringBuilder) org.opensaml.xml.Configuration.getBuilderFactory().getBuilder(XSString.TYPE_NAME);
        XSString value = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        value.setValue(attValue);
        attribute.getAttributeValues().add(value);
        return attribute;
    }

    public String toString(XMLObject xmlObject) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        Marshaller out = org.opensaml.xml.Configuration.getMarshallerFactory().getMarshaller(xmlObject);
        out.marshall(xmlObject, document);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter stringWriter = new StringWriter();
        StreamResult streamResult = new StreamResult(stringWriter);
        DOMSource source = new DOMSource(document);
        transformer.transform(source, streamResult);
        stringWriter.close();
        return stringWriter.toString();
    }
}
