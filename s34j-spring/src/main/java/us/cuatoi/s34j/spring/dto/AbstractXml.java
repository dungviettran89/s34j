package us.cuatoi.s34j.spring.dto;

import com.google.api.client.xml.GenericXml;
import com.google.api.client.xml.XmlNamespaceDictionary;

public class AbstractXml extends GenericXml {
    public AbstractXml() {
        super.namespaceDictionary = new XmlNamespaceDictionary();
        super.namespaceDictionary.set("s3", "http://s3.amazonaws.com/doc/2006-03-01/");
        super.namespaceDictionary.set("", "");
    }

    public XmlNamespaceDictionary getNamespaceDictionary() {
        return super.namespaceDictionary;
    }
}
