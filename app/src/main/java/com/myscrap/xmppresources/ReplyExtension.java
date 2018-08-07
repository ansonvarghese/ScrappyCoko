package com.myscrap.xmppresources;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.provider.EmbeddedExtensionProvider;
import org.jivesoftware.smack.util.XmlStringBuilder;

import java.util.List;
import java.util.Map;

public class ReplyExtension implements ExtensionElement
{

    public static final String NAMESPACE = "myscrap:reply";
    public static final String ELEMENT = "data";

    String uId = null;
    String fId =  null;
    String uImage = null;
    String fImage = null;
    String uName = null;
    String fName = null;
    String uColor = null;
    String fColor = null;

    static final String USER_ID = "userId";
    static final String FROM_ID = "fromId";
    static final String USER_IMAGE = "uImage";
    static final String FROM_IMAGE = "fImage";
    static final String USER_NAME = "uName";
    static final String FROM_NAME = "fName";
    static final String USER_COLOR = "uColor";
    static final String FROM_COLOR = "fColor";



    @Override
    public String getElementName() {
        return ELEMENT;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public XmlStringBuilder toXML()
    {
        XmlStringBuilder xml = new XmlStringBuilder(this);
        xml.attribute(USER_ID, getuId());
        xml.attribute(FROM_ID,getfId());
        xml.attribute(USER_IMAGE, getuImage());
        xml.attribute(FROM_IMAGE,getfImage());
        xml.attribute(USER_NAME, getuName());
        xml.attribute(FROM_NAME,getfName());
        xml.attribute(USER_COLOR, getuColor());
        xml.attribute(FROM_COLOR,getfColor());

        xml.closeEmptyElement();
        return xml;
    }


    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getfId() {
        return fId;
    }

    public void setfId(String fId) {
        this.fId = fId;
    }

    public String getuImage() {
        return uImage;
    }

    public void setuImage(String uImage) {
        this.uImage = uImage;
    }

    public String getfImage() {
        return fImage;
    }

    public void setfImage(String fImage) {
        this.fImage = fImage;
    }

    public String getuName() {
        return uName;
    }

    public void setuName(String uName) {
        this.uName = uName;
    }

    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }

    public String getuColor() {
        return uColor;
    }

    public void setuColor(String uColor) {
        this.uColor = uColor;
    }

    public String getfColor() {
        return fColor;
    }

    public void setfColor(String fColor) {
        this.fColor = fColor;
    }



    public static class Provider extends EmbeddedExtensionProvider<ReplyExtension>
    {
        @Override
        protected ReplyExtension createReturnExtension(String   currentElement, String currentNamespace, Map<String, String> attributeMap, List<? extends ExtensionElement> content)
        {
            ReplyExtension repExt = new ReplyExtension();
            repExt.setuId(attributeMap.get(USER_ID));
            repExt.setfId(attributeMap.get(FROM_ID));
            repExt.setuImage(attributeMap.get(USER_IMAGE));
            repExt.setfImage(attributeMap.get(FROM_IMAGE));
            repExt.setuName(attributeMap.get(USER_NAME));
            repExt.setfName(attributeMap.get(FROM_NAME));
            repExt.setuColor(attributeMap.get(USER_COLOR));
            repExt.setfColor(attributeMap.get(FROM_COLOR));
            return repExt;
        }
    }


}
