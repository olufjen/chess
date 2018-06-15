
package no.chess.web.client;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="GetICD10ByDescriptionResult" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "getICD10ByDescriptionResult"
})
@XmlRootElement(name = "GetICD10ByDescriptionResponse")
public class GetICD10ByDescriptionResponse {

    @XmlElement(name = "GetICD10ByDescriptionResult")
    protected String getICD10ByDescriptionResult;

    /**
     * Gets the value of the getICD10ByDescriptionResult property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGetICD10ByDescriptionResult() {
        return getICD10ByDescriptionResult;
    }

    /**
     * Sets the value of the getICD10ByDescriptionResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGetICD10ByDescriptionResult(String value) {
        this.getICD10ByDescriptionResult = value;
    }

}
