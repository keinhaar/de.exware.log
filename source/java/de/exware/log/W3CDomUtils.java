package de.exware.log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.*;
import javax.xml.xpath.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class W3CDomUtils
{
    public static String getAttribute(Element el,String name)
    {
        String value = null;
        if(el.hasAttribute(name))
        {
            value = el.getAttribute(name);
        }
        return value;
    }
    
    public static Document read(URL url) throws ParserConfigurationException, IOException, SAXException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream in = url.openStream();
        Document doc = builder.parse(in);
        in.close();
        return doc;
    }
    
    public static Node selectSingleNode(Node node,String string) throws XPathExpressionException
    {
        Node retnode = null;
        List<Node> nodes = selectNodes(node,string);
        if(nodes != null && nodes.size() > 0)
        {
            retnode = nodes.get(0);
        }
        return retnode;
    }
    
    public static List<Node> selectNodes(Node node, String string) throws XPathExpressionException
    {
        XPathFactory xpfactory = XPathFactory.newInstance();
        XPath xpath = xpfactory.newXPath();
        NodeList nodeset = (NodeList) xpath.evaluate(string, node, XPathConstants.NODESET);
        List<Node> list = new ArrayList<Node>();
        for(int i=0;i<nodeset.getLength();i++)
        {
            list.add(nodeset.item(i));
        }
        return list;
    }
    
    public static List<Node> getChildsByName(Node node,String name)
    {
        List<Node> ret = new ArrayList<Node>();
        NodeList list = node.getChildNodes();
        for(int i=0;i<list.getLength();i++)
        {
            Node n = list.item(i);
            if(n.getNodeName().equals(name))
            {
                ret.add(n);
            }
        }
        return ret;
    }

}
