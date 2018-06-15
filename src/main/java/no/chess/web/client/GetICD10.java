
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
 *         &lt;element name="ICD10Code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "icd10Code"
})
@XmlRootElement(name = "GetICD10")
public class GetICD10 {

    @XmlElement(name = "ICD10Code")
    protected String icd10Code;

    /**
     * Gets the value of the icd10Code property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getICD10Code() {
        return icd10Code;
    }

    /**
     * Sets the value of the icd10Code property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setICD10Code(String value) {
        this.icd10Code = value;
    }

}
