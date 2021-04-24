
package tyler.ecf.extensions.servicecallbackmessage;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the tyler.ecf.extensions.servicecallbackmessage package. 
 * &lt;p&gt;An ObjectFactory allows you to programatically 
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

    private final static QName _ServiceCallbackMessage_QNAME = new QName("urn:tyler:ecf:extensions:ServiceCallbackMessage", "ServiceCallbackMessage");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: tyler.ecf.extensions.servicecallbackmessage
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ServiceCallbackMessageType }
     * 
     */
    public ServiceCallbackMessageType createServiceCallbackMessageType() {
        return new ServiceCallbackMessageType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServiceCallbackMessageType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ServiceCallbackMessageType }{@code >}
     */
    @XmlElementDecl(namespace = "urn:tyler:ecf:extensions:ServiceCallbackMessage", name = "ServiceCallbackMessage")
    public JAXBElement<ServiceCallbackMessageType> createServiceCallbackMessage(ServiceCallbackMessageType value) {
        return new JAXBElement<ServiceCallbackMessageType>(_ServiceCallbackMessage_QNAME, ServiceCallbackMessageType.class, null, value);
    }

}