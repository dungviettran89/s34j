package us.cuatoi.s34jserver.core.dto;

import com.google.api.client.xml.XmlNamespaceDictionary;

public class AbstractXml extends com.google.api.client.xml.GenericXml {
    public AbstractXml() {
        super.namespaceDictionary = new XmlNamespaceDictionary();
        super.namespaceDictionary.set("s3", "http://s3.amazonaws.com/doc/2006-03-01/");
        super.namespaceDictionary.set("", "");
    }

    public XmlNamespaceDictionary getNamespaceDictionary(){
        return super.namespaceDictionary;
    }
}
