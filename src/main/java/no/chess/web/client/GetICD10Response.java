
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
 *         &lt;element name="GetICD10Result" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "getICD10Result"
})
@XmlRootElement(name = "GetICD10Response")
public class GetICD10Response {

    @XmlElement(name = "GetICD10Result")
    protected String getICD10Result;

    /**
     * Gets the value of the getICD10Result property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGetICD10Result() {
        return getICD10Result;
    }

    /**
     * Sets the value of the getICD10Result property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGetICD10Result(String value) {
        this.getICD10Result = value;
    }

}
