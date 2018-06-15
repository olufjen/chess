
package no.chess.web.client;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the client package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _String_QNAME = new QName("http://www.webserviceX.NET", "string");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: client
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetICD10ByDescriptionResponse }
     * 
     */
    public GetICD10ByDescriptionResponse createGetICD10ByDescriptionResponse() {
        return new GetICD10ByDescriptionResponse();
    }

    /**
     * Create an instance of {@link GetICD10ByDescription }
     * 
     */
    public GetICD10ByDescription createGetICD10ByDescription() {
        return new GetICD10ByDescription();
    }

    /**
     * Create an instance of {@link GetICD10Response }
     * 
     */
    public GetICD10Response createGetICD10Response() {
        return new GetICD10Response();
    }

    /**
     * Create an instance of {@link GetICD10 }
     * 
     */
    public GetICD10 createGetICD10() {
        return new GetICD10();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.webserviceX.NET", name = "string")
    public JAXBElement<String> createString(String value) {
        return new JAXBElement<String>(_String_QNAME, String.class, null, value);
    }

}
