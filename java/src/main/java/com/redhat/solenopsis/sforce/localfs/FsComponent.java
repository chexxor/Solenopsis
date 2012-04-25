package com.redhat.solenopsis.sforce.localfs;

import com.redhat.solenopsis.properties.impl.FileMonitorPropertiesMgr;
import com.redhat.solenopsis.sforce.Component;
import com.redhat.solenopsis.sforce.ComponentMember;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author alex
 */
public class FsComponent extends File implements Component {

    public FsComponent(String componentFilePath) {
        super(componentFilePath);
    }
    public FsComponent(File file) {
        super(file.getAbsolutePath());
    }

    public String getDir() {
        return super.getParentFile().getName();
    }
    
    @Override
    public String getFullName() {
        return super.getName();
    }

    @Override
    public String getType() {
        return super.getParentFile().getName();
    }

    @Override
    public String getFileName() {
        return super.getParentFile().getName() + "/" + super.getName();
    }

    @Override
    public List<ComponentMember> getMembers() {
        //Find props for this component file, otherwise, make it.
        List<ComponentMember> members = new ArrayList<ComponentMember>();
        if (!"objects".equals(super.getParentFile().getName())) {
            return members;
        }
        
        File propsFile = this.createPropsFile();
        
        FileMonitorPropertiesMgr propsMgr = new FileMonitorPropertiesMgr(propsFile);
        Properties properties = propsMgr.getProperties();
        Properties props = propsMgr.getProperties();
        Enumeration<?> propertyNames = props.propertyNames();
        
        while (propertyNames.hasMoreElements()) {
            String propName = (String) propertyNames.nextElement();
            String propValue = props.getProperty(propName);
            members.add(new FsComponentMember(propName, propValue));
        }
        
        return members;
    }

    private File createPropsFile() {
        
        //Assume that the props file is in same directory as component file.
        //Assume props file name is same as component, but with .props extension.
        String componentPath = super.getPath();
        String componentFileName = componentPath.substring(0, componentPath.lastIndexOf('.'));
        String propsFilePath = componentFileName.concat(".props");
        
        File propsFile = new File(propsFilePath);
        if (!propsFile.exists()) {
            try {
                propsFile = this.createPropsFromComponentFile();
            } catch (TransformerConfigurationException ex) {
                Logger.getLogger(FsComponent.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerException ex) {
                Logger.getLogger(FsComponent.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return propsFile;
    }

    //TODO: Fix this method
    // I don't know the best way to get resources in a Java project,
    // and I just know that I'm butchering this.
    private File createPropsFromComponentFile()
            throws TransformerConfigurationException, TransformerException {
        
        String fsType = super.getParentFile().getName();
//        URL xslUrl = FsComponent.class.getResource("../../../../../resources/xsl/object.xsl");
//        String xsltPath = xslUrl.getFile();
//        String xsltPath = super.getParentFile().getParentFile().getPath() + "/" + fsType + ".xsl";
        String xsltPath = "/home/alex/dev/git/Solenopsis/Solenopsis/java/target/classes/xsl/24.0/object.xsl";
        String outputPath = super.getPath().substring(0, super.getPath().lastIndexOf('.')) + ".props";
        
        //TODO: Find the Xslt file in a better manner. Can we do better with these paths?
        File xmlFile = super.getAbsoluteFile();
        System.out.println("xsltPath: " + xsltPath);
        File xsltFile = new File(xsltPath);

        // JAXP reads data using the Source interface
        Source xmlSource = new StreamSource(xmlFile);
        Source xsltSource = new StreamSource(xsltFile);

        // The factory pattern supports different XSLT processors
        TransformerFactory transFact = TransformerFactory.newInstance();
        Transformer trans = transFact.newTransformer(xsltSource);

        trans.transform(xmlSource, new StreamResult(outputPath));
        
        File propsFile = new File(outputPath);
        return propsFile;
    }
    
}
