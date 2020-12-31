package fi.liikennevirasto.tatu.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.liikennevirasto.tatu.PropertiesReader;
import fi.liikennevirasto.tatu.PropertiesReader.Prop;
import fi.liikennevirasto.tatu.dao.SettingsDao;
import fi.liikennevirasto.tatu.model.Settings;

@Service("settingsService")
public class SettingsServiceImpl implements SettingsService {

	private static final Logger logger = Logger.getLogger(SettingsServiceImpl.class);

	/** Default TTL value = 1 day (60*60*24)	 */
	private static final int DEFAULT_TTL = 86400;
	
	private static Integer vuosi; // Tieosoiteverkon vuosi
	private static Boolean boolMArvotTarvitaan;
	private static Boolean ajorataSpesifiPaikannus;
	
	@Autowired
	SettingsDao settingsDao;

	PropertiesReader propertiesReader = new PropertiesReader();
	
	@Override
	public String getFilePath() {
		// Read from the database
//		Settings settings = getSettings();
//		String filepath = settings.getFilepath();
		
		// Read from a properties file
		String filepath = propertiesReader.getProp(Prop.FILEPATH);
		if (filepath != null && filepath.length() > 0) {
			return filepath;
		}
		logger.error("filepath arvo puuttuu asetuksista.");
		return "";
	}

	@Override
	public String getTempPath() {
		String tmpdir = System.getProperty("java.io.tmpdir");
		if ( !(tmpdir.endsWith("/") || tmpdir.endsWith("\\")) )
			tmpdir = tmpdir + System.getProperty("file.separator");
		return tmpdir;

	}

	@Override
	public int getFileTtl() {
		Settings settings = getSettings();
		if (settings != null)
			return settings.getTtl();
		return DEFAULT_TTL;
	}
	
	private Settings getSettings() {
		Settings settings = settingsDao.getSettings();
		if (settings == null) {
			logger.error("Asetusten luku tietokannasta ei onnistunut.");
		}
		return settings;
	}

	// Asettaa tieosoiteverkon vuoden	
	@Override
	public void setVuosi(Integer wuosi){
		vuosi = wuosi;
	}
	
	// Palauttaa vuoden, jonka tieosoiteverkkoa k‰ytet‰‰n
	@Override
	public int getVuosi(){
		return vuosi.intValue();
	}
	
	// Asettaa tiedon siit‰ tuotetaanko m-arvot
	@Override
	public void setMarvotTarvitaan(Boolean marvot){
		boolMArvotTarvitaan=marvot;
	}

	// Tarvitaanko m-arvot
	@Override
	public Boolean getMarvotTarvitaan(){
		return boolMArvotTarvitaan.booleanValue();
	}
	
	// Palauttaa onko ajorataspesifi paikannus tieosoitev‰leille
	public Boolean getAjorataSpesifiPaikannus(){
		return ajorataSpesifiPaikannus.booleanValue();
	}
	
	// Asettaa onko ajorataspesifi paikannus tieosoitev‰leille
	public void setAjorataSpesifiPaikannus(Boolean ajorataSpesifisyys){
		ajorataSpesifiPaikannus = ajorataSpesifisyys;
	}
}
