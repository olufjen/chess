
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
 *         &lt;element name="ICD10Description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "icd10Description"
})
@XmlRootElement(name = "GetICD10ByDescription")
public class GetICD10ByDescription {

    @XmlElement(name = "ICD10Description")
    protected String icd10Description;

    /**
     * Gets the value of the icd10Description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getICD10Description() {
        return icd10Description;
    }

    /**
     * Sets the value of the icd10Description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setICD10Description(String value) {
        this.icd10Description = value;
    }

}
