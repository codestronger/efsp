
package oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_2.MiscellaneousEventTypeCodeType;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.cxf.xjc.runtime.JAXBToStringStyle;


/**
 * &lt;p&gt;Java class for MiscellaneousEventType complex type.
 * 
 * &lt;p&gt;The following schema fragment specifies the expected content contained within this class.
 * 
 * &lt;pre&gt;
 * &amp;lt;complexType name="MiscellaneousEventType"&amp;gt;
 *   &amp;lt;complexContent&amp;gt;
 *     &amp;lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&amp;gt;
 *       &amp;lt;sequence&amp;gt;
 *         &amp;lt;element ref="{urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2}MiscellaneousEventTypeCode"/&amp;gt;
 *         &amp;lt;element ref="{urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2}EventLineItem" maxOccurs="unbounded"/&amp;gt;
 *       &amp;lt;/sequence&amp;gt;
 *     &amp;lt;/restriction&amp;gt;
 *   &amp;lt;/complexContent&amp;gt;
 * &amp;lt;/complexType&amp;gt;
 * &lt;/pre&gt;
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MiscellaneousEventType", propOrder = {
    "miscellaneousEventTypeCode",
    "eventLineItem"
})
public class MiscellaneousEventType {

    @XmlElement(name = "MiscellaneousEventTypeCode", namespace = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2", required = true)
    protected MiscellaneousEventTypeCodeType miscellaneousEventTypeCode;
    @XmlElement(name = "EventLineItem", required = true)
    protected List<EventLineItemType> eventLineItem;

    /**
     * Gets the value of the miscellaneousEventTypeCode property.
     * 
     * @return
     *     possible object is
     *     {@link MiscellaneousEventTypeCodeType }
     *     
     */
    public MiscellaneousEventTypeCodeType getMiscellaneousEventTypeCode() {
        return miscellaneousEventTypeCode;
    }

    /**
     * Sets the value of the miscellaneousEventTypeCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link MiscellaneousEventTypeCodeType }
     *     
     */
    public void setMiscellaneousEventTypeCode(MiscellaneousEventTypeCodeType value) {
        this.miscellaneousEventTypeCode = value;
    }

    /**
     * Gets the value of the eventLineItem property.
     * 
     * &lt;p&gt;
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a &lt;CODE&gt;set&lt;/CODE&gt; method for the eventLineItem property.
     * 
     * &lt;p&gt;
     * For example, to add a new item, do as follows:
     * &lt;pre&gt;
     *    getEventLineItem().add(newItem);
     * &lt;/pre&gt;
     * 
     * 
     * &lt;p&gt;
     * Objects of the following type(s) are allowed in the list
     * {@link EventLineItemType }
     * 
     * 
     */
    public List<EventLineItemType> getEventLineItem() {
        if (eventLineItem == null) {
            eventLineItem = new ArrayList<EventLineItemType>();
        }
        return this.eventLineItem;
    }

    /**
     * Generates a String representation of the contents of this type.
     * This is an extension method, produced by the 'ts' xjc plugin
     * 
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, JAXBToStringStyle.DEFAULT_STYLE);
    }

}