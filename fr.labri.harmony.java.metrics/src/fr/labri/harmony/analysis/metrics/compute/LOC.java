package fr.labri.harmony.analysis.metrics.compute;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import fr.labri.harmony.analysis.metrics.ComputeMetrics;
import fr.labri.harmony.analysis.metrics.ComputeMetricsScope;


public class LOC extends ComputeMetrics {

	public LOC() {
		super();
	}

	@Override
	public void prepareMetrics() {
		 try {
             Process p = Runtime.getRuntime().exec(new String[] { "cloc", new File(workspacePath).getAbsolutePath(), "--xml", "--quiet" });
             BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
             StringBuffer b = new StringBuffer();
             String line;

             while ((line = r.readLine()) != null) {
                 if (line.trim().startsWith("<"))
                     b.append(line + "\n");
             }
             p.waitFor();
             r.close();

             DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
             DocumentBuilder db = df.newDocumentBuilder();

             Document doc = db.parse(new InputSource(new StringReader(b.toString().trim())));
             NodeList l = doc.getElementsByTagName("language");



             for (int i = 0; i < l.getLength(); i++) {
                 Node el = l.item(i);
                 final NamedNodeMap attributes = el.getAttributes();
                 String lang = attributes.getNamedItem("name").getTextContent();
                 if (lang.toLowerCase().equals("java"));
                 metrics.addMetric("LOC", attributes.getNamedItem("code").getTextContent());
             }

             p.waitFor();
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }

	@Override
	public ComputeMetricsScope getScope() {
		return ComputeMetricsScope.EVENT;
	}

}
