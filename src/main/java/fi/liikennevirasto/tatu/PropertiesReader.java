package fi.liikennevirasto.tatu;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;

public class PropertiesReader {

	Logger logger = Logger.getLogger(PropertiesReader.class);
	
	private HashMap<String, String> taulukko = new HashMap<String, String>();
	
	/**
	 * Kaikki arvot jotka voidaan lukea properties-tiedostosta.
	 * @author matikaineno
	 *
	 */
	public enum Prop {
		FILEPATH("filepath");
		
		private String name;
		private Prop(String name) {
			this.name = name;
		}
	}

    public PropertiesReader() {
    	logger.debug("Ladataan tatu.properties tiedosto contextista");
    	InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("/tatu.properties");
    	if (in == null) {
        	logger.debug("Ei löytynyt. Ladataan tatu.properties tiedosto war-paketista");
        	in = this.getClass().getResourceAsStream("/tatu.properties");
    	}
    	if (in == null) {
    		logger.error("tatu.properties tiedostoa ei löytynyt.");
    		return;
    	}
    	try {
            Properties properties = new Properties();
            properties.load(in);

            Prop[] props = Prop.values();
            for (int i = 0; i < props.length; i++) {
				Prop prop = props[i];
				taulukko.put(prop.name, properties.getProperty(prop.name));
			}
            
          logger.debug("Parametrit luettu: ");
          logger.debug(this.toString());
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public String getProp(Prop prop) {
    	return taulukko.get(prop.name);
    }
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();

    	Iterator<String> keys = taulukko.keySet().iterator();
    	while (keys.hasNext()) {
			String key = (String) keys.next();
			sb.append(key);
			sb.append("=");
			sb.append(taulukko.get(key));
			sb.append(", ");
		}

    	return sb.toString();
    }
	
}
