package ru.examples;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Сергей on 23.04.2018.
 */
public class XMLExample {

    static String xmlFile = "XML_Test.xml";

    public static void main(String[] args) {
        List<XML_element> xml_element_list = new ArrayList<>();
        for (int a = 0; a < 3; a++){
            for (int b = 0; b < 3; b++){
                xml_element_list.add(new XML_element(a, b, new int[] {0, 1, 2, 3}));
//                XMLElementList.add(new XML_element(x, y));
            }
        }

//        XML_elements xml_elements = new XML_elements();
//        xml_elements.setXML_elements(xml_element_list);

        objectToXML(xml_element_list.get(1));
        xmlToObject();
    }

    static void objectToXML(XML_element xml_element){
        try {
            File file = new File(xmlFile);
            JAXBContext jaxbContext = JAXBContext.newInstance(XML_element.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(xml_element, file);
            jaxbMarshaller.marshal(xml_element, System.out);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    static void xmlToObject(){
        try {

            File file = new File(xmlFile );
            JAXBContext jaxbContext = JAXBContext.newInstance(XML_element.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            XML_element xml_element = (XML_element) jaxbUnmarshaller.unmarshal(file);
            System.out.println(xml_element);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }



    @XmlRootElement(name = "XML_element")
//    @XmlAccessorType(XmlAccessType.FIELD)
    static class XML_element {
        int x;
        int y;
        int[] z;

        public XML_element() {
        }

        public XML_element(int x, int y, int[] z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public int getX() {
            return x;
        }

        @XmlElement
        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        @XmlElement
        public void setY(int y) {
            this.y = y;
        }

        public int[] getZ() {
            return z;
        }

        @XmlElement
        public void setZ(int[] z) {
            this.z = z;
        }
    }

    @XmlRootElement(name = "XML_elements")
    @XmlAccessorType(XmlAccessType.FIELD)
    static class XML_elements {
        @XmlElement(name = "XML_element")
        private List<XML_element> XML_elements = null;

        public List<XML_element> getXML_elements() {
            return XML_elements;
        }

        public void setXML_elements(List<XML_element> employees) {
            this.XML_elements = employees;
        }
    }

}
