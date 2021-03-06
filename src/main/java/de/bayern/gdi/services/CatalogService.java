/*
 * DownloadClient Geodateninfrastruktur Bayern
 *
 * (c) 2016 GSt. GDI-BY (gdi.bayern.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.bayern.gdi.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.bayern.gdi.gui.ServiceModel;
import de.bayern.gdi.utils.NamespaceContextMap;
import de.bayern.gdi.utils.XML;

/**
 * @author Jochen Saalfeld (jochen@intevation.de)
 */
public class CatalogService {

    private static final String CSW_NAMESPACE =
            "http://www.opengis.net/cat/csw/2.0.2";
    private static final String CSW_SCHEMA =
            "http://schemas.opengis.net/csw/2.0.2/csw.xsd";
    private static final String GMD_NAMESPACE =
            "http://www.isotc211.org/2005/gmd";
    private static final String GMD_SCHEMA =
            "http://www.isotc211.org/2005/gmd/gmd.xsd";
    private static final String OWS_NAMESPACE =
            "http://www.opengis.net/ows";
    private static final String SRV_NAMESPACE =
            "http://www.isotc211.org/2005/srv";
    private static final String GCO_NAMESPACE =
            "http://www.isotc211.org/2005/gco";
    private static final String XSI_NAMESPACE =
            "http://www.w3.org/2001/XMLSchema-instance";
    private static final String OGC_NAMESPACE =
            "http://www.opengis.net/ogc";
    private static final String XML_NAMESPACE =
            "http://www.w3.org/2000/xmlns/";
    private static final String CSW_QUERY_FILEPATH =
            "csw_searchQuery.xml";
    private static final Logger log
            = Logger.getLogger(CatalogService.class.getName());
    private URL catalogURL;
    private String userName;
    private static final int MIN_SEARCHLENGTH = 2;
    private String password;
    private String providerName;
    private NamespaceContext context;

    /**
     * Constructor.
     *
     * @param url      String URL
     * @param userName Username
     * @param password Password
     * @throws MalformedURLException
     */
    public CatalogService(String url, String userName, String password) throws
            MalformedURLException {
        this(new URL(url), userName, password);
    }

    /**
     * Constructor.
     *
     * @param url Stirng URL
     * @throws MalformedURLException When URL has bad format
     */
    public CatalogService(String url) throws MalformedURLException {
        this(url, null, null);
    }

    /**
     * Constructor.
     *
     * @param url URL
     */
    public CatalogService(URL url) {
        this(url, null, null);
    }

    /**
     * Constructor.
     *
     * @param url      URL
     * @param userName Username
     * @param password Password
     */
    public CatalogService(URL url, String userName, String password) {
        this.catalogURL = url;
        this.userName = userName;
        this.password = password;
        this.context = new NamespaceContextMap(
                "csw", CSW_NAMESPACE,
                "gmd", GMD_NAMESPACE,
                "ows", OWS_NAMESPACE,
                "srv", SRV_NAMESPACE,
                "gco", GCO_NAMESPACE);
        Document xml = XML.getDocument(this.catalogURL,
                this.userName,
                this.password);
        String getProviderExpr = "//ows:ServiceIdentification/ows:Title";
        Node providerNameNode = (Node) XML.xpath(xml,
                getProviderExpr,
                XPathConstants.NODE,
                context);
        this.providerName = providerNameNode.getTextContent();
    }

    /**
     * gets the Name of the service Provider.
     *
     * @return Name of the service Provider
     */
    public String getProviderName() {
        return this.providerName;
    }

    /**
     * retrieves a Map of ServiceNames and URLs for a Filter-Word.
     *
     * @param filter the Word to filter to
     * @return Map of Service Names and URLs
     */
    public List<ServiceModel> getServicesByFilter(String filter) {
        List<ServiceModel> services = new ArrayList<ServiceModel>();
        if (filter.length() > MIN_SEARCHLENGTH) {
            //Document searchXML = createXMLFilter(filter);
            //XML.printDocument(searchXML, System.out);
            String search = loadXMLFilter(filter);
            //Document xmlRead = XML.getDocument(search);
            //System.out.println(search);
            //XML.printDocument(xmlRead, System.out);
            /*
            URL requestURL = setURLRequestAndSearch(filter);
            Document xml = XML.getDocument(requestURL,
                    this.userName,
                    this.password);
            */
            Document xml = XML.getDocument(this.catalogURL,
                    this.userName,
                    this.password,
                    search,
                    true);
            //XML.printDocument(xml, System.out);
            Node exceptionNode = (Node) XML.xpath(xml,
                    "//ows:ExceptionReport",
                    XPathConstants.NODE, this.context);
            if (exceptionNode != null) {
                String exceptionCode = (String) XML.xpath(xml,
                        "//ows:ExceptionReport/ows:Exception/@exceptionCode",
                        XPathConstants.STRING, this.context);
                String exceptionlocator = (String) XML.xpath(xml,
                        "//ows:ExceptionReport/ows:Exception/@locator",
                        XPathConstants.STRING, this.context);
                String exceptiontext = (String) XML.xpath(xml,
                        "//ows:ExceptionReport/ows:Exception/ows:ExceptionText",
                        XPathConstants.STRING, this.context);
                String excpetion = "An Excpetion was thrown by the CSW: \n";
                excpetion += "\texceptionCode: " + exceptionCode + "\n";
                excpetion += "\tlocator: " + exceptionlocator + "\n";
                excpetion += "\texceptiontext: " + exceptiontext + "\n";
                log.log(Level.SEVERE, excpetion, xml);
                return null;
            }
            String numberOfResultsExpr =
                    "//csw:SearchResults/@numberOfRecordsReturned";
            Double numberOfResults = (Double) XML.xpath(xml,
                    numberOfResultsExpr,
                    XPathConstants.NUMBER, this.context);
            String nodeListOfServicesExpr =
                    "//csw:SearchResults//gmd:MD_Metadata";
            NodeList servicesNL = (NodeList) XML.xpath(xml,
                    nodeListOfServicesExpr,
                    XPathConstants.NODESET, this.context);
            for (int i = 0; i < numberOfResults; i++) {
                Node serviceN = servicesNL.item(i);
                String nameExpr =
                        "gmd:identificationInfo"
                                + "/srv:SV_ServiceIdentification"
                                + "/gmd:citation"
                                + "/gmd:CI_Citation"
                                + "/gmd:title"
                                + "/gco:CharacterString";
                String serviceName = (String) XML.xpath(serviceN,
                        nameExpr,
                        XPathConstants.STRING, context);
                String restrictionExpr =
                        "gmd:identificationInfo"
                                + "/srv:SV_ServiceIdentification"
                                + "/gmd:resourceConstraints"
                                + "/gmd:MD_SecurityConstraints"
                                + "/gmd:classification"
                                + "/gmd:MD_ClassificationCode";
                String restriction = (String) XML.xpath(serviceN,
                        restrictionExpr,
                        XPathConstants.STRING, context);
                boolean restricted = false;
                if ("restricted".equals(restriction)) {
                    restricted = true;
                }
                String typeExpr =
                        "gmd:identificationInfo"
                                + "/srv:SV_ServiceIdentification"
                                + "/srv:serviceType"
                                + "/gco:LocalName";
                String serviceType = (String) XML.xpath(serviceN,
                        typeExpr,
                        XPathConstants.STRING, context);


                String serviceTypeVersionExpr =
                        "gmd:identificationInfo"
                                + "/srv:SV_ServiceIdentification"
                                + "/srv:serviceTypeVersion"
                                + "/gco:CharacterString";
                String serviceTypeVersion = (String) XML.xpath(serviceN,
                        serviceTypeVersionExpr,
                        XPathConstants.STRING, context);
                String onLineExpr = "gmd:distributionInfo"
                        + "/gmd:MD_Distribution"
                        + "/gmd:transferOptions"
                        + "/gmd:MD_DigitalTransferOptions"
                        + "/gmd:onLine";
                NodeList onlineNL = (NodeList) XML.xpath(serviceN,
                        onLineExpr,
                        XPathConstants.NODESET, context);
                String serviceURL = null;
                for (int j = 0; j < onlineNL.getLength(); j++) {
                    Node onlineN = onlineNL.item(j);
                    String urlExpr =
                            "gmd:CI_OnlineResource"
                                    + "/gmd:linkage"
                                    + "/gmd:URL";
                    String onLineserviceURL = (String) XML.xpath(onlineN,
                            urlExpr,
                            XPathConstants.STRING, context);
                    String applicationprofileExpr =
                            "gmd:CI_OnlineResource"
                                    + "/gmd:applicationProfile"
                                    + "/gco:CharacterString";
                    String applicationProfile = (String) XML.xpath(onlineN,
                            applicationprofileExpr,
                            XPathConstants.STRING, context);
                    applicationProfile = applicationProfile.toLowerCase();
                    if (applicationProfile.equals("wfs-url")
                            || applicationProfile.equals("feed-url")) {
                        serviceURL = onLineserviceURL;
                        break;
                    } else if (applicationProfile.equals("dienste-url")
                            || applicationProfile.equals("download")) {
                        serviceURL = onLineserviceURL;
                    }
                }

                if (serviceTypeVersion.equals("")) {
                    serviceTypeVersion = "ATOM";
                }
                    if (!serviceName.equals("") && serviceURL != null) {
                        serviceURL = makeCapabiltiesURL(serviceURL,
                                serviceTypeVersion);
                        ServiceModel service = new ServiceModel();
                        service.setName(serviceName);
                        service.setUrl(serviceURL);
                        service.setVersion(serviceTypeVersion);
                        service.setRestricted(restricted);
                        services.add(service);
                    }
                }

        }
        return services;
    }

    private String makeCapabiltiesURL(String url, String type) {
        if (url.endsWith("?") && type.toUpperCase().contains("WFS")) {
            return url + "service=wfs&version=" + getVersionOfType(type)
                    + "&request=GetCapabilities";
        }
        if (!url.toUpperCase().contains("GETCAPABILITIES") && type
                .toUpperCase().contains("WFS")) {
            return url + "?service=wfs&version=" + getVersionOfType(type)
                    + "&request=GetCapabilities";
        }
        return url;
    }

    private String getVersionOfType(String type) {
        String versionNumber = type.substring(type.lastIndexOf(" ") + 1);
        if ("2.0".equals(versionNumber)) {
            versionNumber = "2.0.0";
        } else if ("1.0".equals(versionNumber)) {
            versionNumber = "1.0.0";
        } else if ("1.1".equals(versionNumber)) {
            versionNumber = "1.1.0";
        }
        return versionNumber;
    }

    /**
     * http://www.weichand.de/2012/03/24/
     * grundlagen-catalogue-service-web-csw-2-0-2/ .
     */
    private URL setURLRequestAndSearch(String search) {
        URL newURL = null;
        String constraintAnyText = "csw:AnyText Like '%"
                + search + "%'";
        try {
            constraintAnyText = URLEncoder.encode(constraintAnyText, "UTF-8");
            newURL = new URL(this.catalogURL.toString().replace(
                    "GetCapabilities", "GetRecords"
                            + "&version=2.0.2"
                            + "&namespace=xmlns"
                            + "(csw=" + CSW_NAMESPACE + "),"
                            + "xmlns(gmd=" + GMD_NAMESPACE + ")"
                            + "&resultType=results"
                            + "&outputFormat=application/xml"
                            + "&outputSchema=" + GMD_NAMESPACE
                            + "&startPosition=1"
                            + "&maxRecords=20"
                            + "&typeNames=csw:Record"
                            + "&elementSetName=full"
                            + "&constraintLanguage=CQL_TEXT"
                            + "&constraint_language_version=1.1.0"
                            + "&constraint=" + constraintAnyText));
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return newURL;
    }

    private String loadXMLFilter(String search) {
        ClassLoader classLoader = CatalogService.class.getClassLoader();
        InputStream stream =
                classLoader.getResourceAsStream(CSW_QUERY_FILEPATH);
        StringWriter writer = new StringWriter();
        try {
            IOUtils.copy(stream, writer, "UTF-8");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        String xmlStr = writer.toString();
        return xmlStr.replace("{SUCHBEGRIFF}", search);
    }

    /**
     * https://github.com/gdi-by/beispiele/
     * blob/master/csw/GetRecords-wfs20-atom.xml .
     */
    private Document createXMLFilter(String search) {
        Document xml = null;
        String backslash = "\\";
        try {
            DocumentBuilderFactory docFactory =
                    DocumentBuilderFactory.newInstance();
            docFactory.isNamespaceAware();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            //DOMImplementation domImpl = docBuilder.getDOMImplementation();
            xml = docBuilder.newDocument();
            xml.setXmlVersion("1.0");
            Element root = xml.createElement("GetRecords");
            root.setAttributeNS(XML_NAMESPACE, "xmlns", CSW_NAMESPACE);
            root.setAttributeNS(XML_NAMESPACE, "xmlns:csw", CSW_NAMESPACE);
            root.setAttributeNS(XML_NAMESPACE, "xmlns:gmd", GMD_NAMESPACE);
            root.setAttributeNS(XML_NAMESPACE, "xmlns:xsi", XSI_NAMESPACE);
            //root.setAttribute("xmlns", CSW_NAMESPACE);
            //root.setAttribute("xmlns:csw", CSW_NAMESPACE);
            //root.setAttribute("xmlns:gmd", GMD_NAMESPACE);
            //root.setAttribute("xmlns:xsi", XSI_NAMESPACE);
            root.setAttribute("service", "CSW");
            root.setAttribute("version", "2.0.2");
            root.setAttribute("maxRecords", "500");
            root.setAttribute("startPosition", "1");
            root.setAttribute("resultType", "results");
            root.setAttribute("outputFormat", "application/xml");
            root.setAttribute("outputSchema",
                    "http://www.isotc211.org/2005/gmd");
            root.setAttribute("xsi:schemaLocation", CSW_NAMESPACE
                    + " http://schemas.opengis.net/"
                    + "csw/2.0.2/CSW-discovery.xsd");
            xml.appendChild(root);
            Element query = xml.createElement("Query");
            query.setAttribute("typeNames", "gmd:MD_Metadata");
            root.appendChild(query);
            Element elementSetName = xml.createElement("ElementSetName");
            elementSetName.setAttribute("typeNames", "csw:IsoRecord");
            elementSetName.setTextContent("full");
            query.appendChild(elementSetName);
            Element constraint = xml.createElement("Constraint");
            constraint.setAttribute("version", "1.1.0");
            query.appendChild(constraint);
            Element ogcFilter = xml.createElementNS(OGC_NAMESPACE,
                    "ogc:Filter");
            ogcFilter.setAttribute("xmlns:ogc", "http://www.opengis.net/ogc");
            ogcFilter.setAttribute("xmlns:gml", "http://www.opengis.net/gml");
            constraint.appendChild(ogcFilter);
            Element andAnyText = xml.createElementNS(OGC_NAMESPACE, "ogc:And");
            ogcFilter.appendChild(andAnyText);
            Element anyTextPropertyIsLike =
                    xml.createElementNS(OGC_NAMESPACE, "ogc:PropertyIsLike");
            anyTextPropertyIsLike.setAttribute("escapeChar", backslash);
            anyTextPropertyIsLike.setAttribute("singleChar", "?");
            anyTextPropertyIsLike.setAttribute("wildCard", "*");
            andAnyText.appendChild(anyTextPropertyIsLike);
            Element anyTextPropertyName = xml.createElement("ogc:PropertyName");
            anyTextPropertyName.setTextContent("gmd:anytext");
            anyTextPropertyIsLike.appendChild(anyTextPropertyName);
            Element anyTextLiteral = xml.createElement("ogc:Literal");
            anyTextLiteral.setTextContent("*" + search + "*");
            anyTextPropertyIsLike.appendChild(anyTextLiteral);
            Element orServices = xml.createElementNS(OGC_NAMESPACE, "ogc:Or");
            andAnyText.appendChild(orServices);
            Element andWFS20 = xml.createElementNS(OGC_NAMESPACE, "ogc:And");
            orServices.appendChild(andWFS20);
            Element wfs20NamePropertyIsLike =
                    xml.createElementNS(OGC_NAMESPACE, "ogc:PropertyIsLike");
            wfs20NamePropertyIsLike.setAttribute("escapeChar", backslash);
            wfs20NamePropertyIsLike.setAttribute("singleChar", "?");
            wfs20NamePropertyIsLike.setAttribute("wildCard", "*");
            andWFS20.appendChild(wfs20NamePropertyIsLike);
            Element wfs20NamePropertyName =
                    xml.createElementNS(OGC_NAMESPACE, "ogc:PropertyName");
            wfs20NamePropertyName.setTextContent("gmd:serviceTypeVersion");
            wfs20NamePropertyIsLike.appendChild(wfs20NamePropertyName);
            Element wfs20NameLiteral = xml.createElementNS(OGC_NAMESPACE,
                    "ogc:Literal");
            wfs20NameLiteral.setTextContent("*OGC:WFS*");
            wfs20NamePropertyIsLike.appendChild(wfs20NameLiteral);
            Element wfs20VersionPropertyIsLike =
                    xml.createElementNS(OGC_NAMESPACE, "ogc:PropertyIsLike");
            wfs20VersionPropertyIsLike.setAttribute("escapeChar", backslash);
            wfs20VersionPropertyIsLike.setAttribute("singleChar", "?");
            wfs20VersionPropertyIsLike.setAttribute("wildCard", "*");
            andWFS20.appendChild(wfs20VersionPropertyIsLike);
            Element wfs20VersionPropertyName =
                    xml.createElementNS(OGC_NAMESPACE, "ogc:PropertyName");
            wfs20VersionPropertyName.setTextContent("gmd:serviceTypeVersion");
            wfs20VersionPropertyIsLike.appendChild(wfs20VersionPropertyName);
            Element wfs20VersionLiteral = xml.createElementNS(OGC_NAMESPACE,
                    "ogc:Literal");
            wfs20VersionLiteral.setTextContent("*2.0*");
            wfs20VersionPropertyIsLike.appendChild(wfs20VersionLiteral);
            Element atomOr = xml.createElementNS(OGC_NAMESPACE, "ogc:Or");
            orServices.appendChild(atomOr);
            Element atomSmallPropertyIsLike =
                    xml.createElementNS(OGC_NAMESPACE, "ogc:PropertyIsLike");
            atomSmallPropertyIsLike.setAttribute("escapeChar", backslash);
            atomSmallPropertyIsLike.setAttribute("singleChar", "?");
            atomSmallPropertyIsLike.setAttribute("wildCard", "*");
            atomOr.appendChild(atomSmallPropertyIsLike);
            Element atomSmallPropertyName =
                    xml.createElementNS(OGC_NAMESPACE, "ogc:PropertyName");
            atomSmallPropertyName.setTextContent("gmd:serviceTypeVersion");
            atomSmallPropertyIsLike.appendChild(atomSmallPropertyName);
            Element atomSmallLiteral = xml.createElement("ogc:Literal");
            atomSmallLiteral.setTextContent("*atom*");
            atomSmallPropertyIsLike.appendChild(atomSmallLiteral);
            Element atomCapStartPropertyIsLike =
                    xml.createElementNS(OGC_NAMESPACE, "ogc:PropertyIsLike");
            atomCapStartPropertyIsLike.setAttribute("escapeChar", backslash);
            atomCapStartPropertyIsLike.setAttribute("singleChar", "?");
            atomCapStartPropertyIsLike.setAttribute("wildCard", "*");
            atomOr.appendChild(atomCapStartPropertyIsLike);
            Element atomCapStartPropertyName =
                    xml.createElementNS(OGC_NAMESPACE, "ogc:PropertyName");
            atomCapStartPropertyName.setTextContent("gmd:serviceTypeVersion");
            atomCapStartPropertyIsLike.appendChild(atomCapStartPropertyName);
            Element atomCapStartLiteral = xml.createElement("ogc:Literal");
            atomCapStartLiteral.setTextContent("*Atom*");
            atomCapStartPropertyIsLike.appendChild(atomCapStartLiteral);
            Element atomCapitalPropertyIsLike =
                    xml.createElementNS(OGC_NAMESPACE, "ogc:PropertyIsLike");
            atomCapitalPropertyIsLike.setAttribute("escapeChar", backslash);
            atomCapitalPropertyIsLike.setAttribute("singleChar", "?");
            atomCapitalPropertyIsLike.setAttribute("wildCard", "*");
            atomOr.appendChild(atomCapitalPropertyIsLike);
            Element atomCapitalPropertyName =
                    xml.createElementNS(OGC_NAMESPACE, "ogc:PropertyName");
            atomCapitalPropertyName.setTextContent("gmd:serviceTypeVersion");
            atomCapitalPropertyIsLike.appendChild(atomCapitalPropertyName);
            Element atomCapitalLiteral = xml.createElementNS(
                    OGC_NAMESPACE, "ogc:Literal");
            atomCapitalLiteral.setTextContent("*ATOM*");
            atomCapitalPropertyIsLike.appendChild(atomCapitalLiteral);
        } catch (ParserConfigurationException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
        return xml;
    }
}
