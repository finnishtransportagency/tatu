package fi.liikennevirasto.tatu;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.geotools.feature.SchemaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.Util;

import fi.liikennevirasto.tatu.KuvDefinition.Tyyppi;
import fi.liikennevirasto.tatu.service.OsoiteverkkoService;
import fi.liikennevirasto.tatu.service.OsoiteverkkoServiceImpl;
import fi.liikennevirasto.tatu.service.SettingsService;


public class CsvReader extends TatuFileReader implements TatuReader {

	private static final Logger logger = Logger.getLogger(CsvReader.class);
	public static Boolean boolMArvotMukaan = false;
	
	public ReaderResponse readFile(OsoiteverkkoService osoiteverkkoService, File csvFile, 
			String shapeFilename, String zipFilename, String originalFilename) throws IOException, SchemaException, TatuException {

		ICsvListReader mapReader = new CsvListReader(new FileReader(csvFile), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
		
		final String[] header = mapReader.getHeader(true);
		header[0] = removeBOM(header[0]);
		List<KuvDefinition> kuvDefs = getKuvDefinitions(header, csvFile);
		
		ShapeWriter shapeWriter = new ShapeWriter(shapeFilename, originalFilename);
		shapeWriter.boolMArvotMukaan = boolMArvotMukaan;

		List<String> row = null;
		while ((row = mapReader.read()) != null) {
			
			if (mapReader.length() != header.length) {
				logger.debug("Virheellinen rivi: "+row);
				throw new TatuException("Seuraava rivi on virheellinen: " +row);
			}
			
			Map<String, String> rowMap = new HashMap<String, String>();
			Util.filterListToMap(rowMap, header, row);
			
			TatuRow tatuRow = new TatuRow();
			try{
				for (KuvDefinition kuvDef : kuvDefs) {
					String value = rowMap.get(kuvDef.getLyhenne());
					if(KuvDefinition.Tyyppi.Double == kuvDef.getTyyppi()){
						Double doubleValue = null;
						if(value != null) {
							value = value.trim();
							if(!value.isEmpty()){					
								value = value.replaceAll(",",".");
								doubleValue = Double.parseDouble(value);
							}
						}
						tatuRow.addValue(doubleValue, kuvDef);
					}
					else {
						//Tarkistus ett‰ kokonaislukukentt‰‰n p‰‰tyv‰ arvo 0 tarkoittaa nollaa varmasti
						if (value!=null){
							if (kuvDef.getTyyppi() == Tyyppi.J || kuvDef.getTyyppi() == Tyyppi.K){
								Double doubleValue = null;
								String valueCopy = value.replaceAll(",",".");
								
								try{
									doubleValue=Double.parseDouble(valueCopy);
									if(doubleValue-(double)doubleValue.intValue()!=new Double("0")){
										//Ei ole int vaikka numero onkin. On double.
										throw new TatuException("Virheellinen kent‰n arvo. Ei ole kokonaisluku. | " + kuvDef.getLyhenne() +  " | " + value.toString());
									}
								}catch(Exception e){
									//Ei ole numero lainkaan vaikka pit‰isi olla kokonaisluku
									throw new TatuException("Virheellinen kokonaislukukent‰n arvo: " + kuvDef.getLyhenne() +  " | " + value.toString() +  " | tyyppi: " + kuvDef.getTyyppi() );
								}
							}
						}
						// Jos p‰‰stiin edellisest‰ if:st‰ yli, kaikki on hyvin
						tatuRow.addValue(value, kuvDef);
					}
				}
				// Store to Shape | hakee samalla sijainnin eli geometrian kohteelle
				shapeWriter.writeLine(osoiteverkkoService, kuvDefs, tatuRow);
			}catch(Exception e){
				//Jotain meni pieleen, esim tietotyyppi ei kelpaa
				logger.debug("Virheellinen rivi: "+ row + " | " + e.getMessage());
				mapReader.close();
				throw new TatuException("Seuraava rivi on virheellinen: " + row +  " | " + e.getMessage());
			}
		}
		mapReader.close();

		return shapeWriter.createZipFile(zipFilename);
	}

	
	private static String removeBOM(String firstHeader) {
        final int charCode;
        final char firstChar = (char)(charCode = firstHeader.charAt(0));
        final int bomCode = 65279;
        if (charCode == bomCode) {
            firstHeader = firstHeader.substring(1);
        }
        if (firstHeader.substring(0, 3).equals("\u00efªø")) {
            firstHeader = firstHeader.substring(3);
        }
        return firstHeader;
    }
	
	
	/**
	 * Create KuvDefinitions and check that header contains tieosoite fields.
	 * 
	 * @param header header
	 * @param csvFile CSV file
	 * @throws TatuException 
	 * @throws IOException 
	 */
	private static List<KuvDefinition> getKuvDefinitions(String[] header, File csvFile) throws TatuException {
		boolean tie = false, aosa = false, ajr = false, aet = false;

		List<KuvDefinition> kuvDefs = new ArrayList<KuvDefinition>();
		
		for (int i = 0; i < header.length; i++) {
			String name = header[i];
			if (name == null)
				throw new TatuException("CSV-tiedoston otsikossa tyhj‰ tieto");
			// Tarkistetaaan, onko tiedostossa sama sarakkeen nimi useaan kertaan.
			int nameCount = 0;
			for (int j = 0; j < header.length; j++) {
				String name2 = header[j];
				if (name.equals(name2))
					nameCount += 1;
			}
			if (nameCount > 1)
				throw new TatuException("CSV-tiedostossa samoja sarakkeiden nimi‰: sarake "+ name +" "+ nameCount+ " kertaa.");
			if (name.compareToIgnoreCase("tie") == 0 || name.compareToIgnoreCase("tienro") == 0) {
				tie = true;
				kuvDefs.add(new KuvDefinition(name, "K"));
			}
			else if (name.compareToIgnoreCase("aosa") == 0 || name.compareToIgnoreCase("osa") == 0 || name.compareToIgnoreCase("alkuosa") == 0) {
				aosa = true;
				kuvDefs.add(new KuvDefinition(name, "K"));
			}
			else if (name.compareToIgnoreCase("ajr") == 0 || name.compareToIgnoreCase("ajorata") == 0) {
				ajr = true;
				kuvDefs.add(new KuvDefinition(name, "K"));
			}
			else if (name.compareToIgnoreCase("aet") == 0 || name.compareToIgnoreCase("alkuet") == 0 || name.compareToIgnoreCase("et") == 0 || name.compareToIgnoreCase("etaisyys") == 0) {
				aet = true;
				kuvDefs.add(new KuvDefinition(name, "K"));
			}
			else if (name.compareToIgnoreCase("losa") == 0 || name.compareToIgnoreCase("loppuosa") == 0)
				kuvDefs.add(new KuvDefinition(name, "K"));
			else if (name.compareToIgnoreCase("let") == 0 || name.compareToIgnoreCase("loppuet") == 0)
				kuvDefs.add(new KuvDefinition(name, "K"));
			else if (name.compareToIgnoreCase("offset") == 0)
				kuvDefs.add(new KuvDefinition(name, "K"));
			else {
				// Add rest of the data
				kuvDefs.add(new KuvDefinition(header[i], getColType(header, header[i], csvFile)));
			}
			
		}
		if(boolMArvotMukaan){
			kuvDefs.add(new KuvDefinition("ID", "M"));
		}
		checkForError("csv", tie, aosa, ajr, aet);
		
		return kuvDefs;
	}
	
	/***
	 * Get column type.
	 * @param header csv header
	 * @param columnName column name
	 * @param csvFile CSV file
	 * @return type of column
	 */
	private static String getColType(String[] header, String columnName, File csvFile){
		String type = null;		
		ICsvMapReader mapReader = null;
		Map<String, String> rowMap = null;
		try{
			mapReader = new CsvMapReader(new FileReader(csvFile), CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE);
			final String[] headerTemp = mapReader.getHeader(true);
			while ((rowMap = mapReader.read(headerTemp)) != null) {
				String value = rowMap.get(columnName); 
				if(value!=null){
					try{
						value = value.replaceAll(",",".");
						Double dd = Double.parseDouble(value);
						//type = KuvDefinition.Tyyppi.Double.toString();
						if(dd-(double)dd.intValue()!=new Double("0")){
							if(type!=KuvDefinition.Tyyppi.M.toString()){
								type = KuvDefinition.Tyyppi.Double.toString();
							}
						} else {
							if(type==KuvDefinition.Tyyppi.K.toString()||type==null){
								type = KuvDefinition.Tyyppi.K.toString();
							}
						}
					} catch(NumberFormatException nfe){
						type = KuvDefinition.Tyyppi.M.toString();
						break;
					}
					
					//break; // Ei keskeytet‰ vaan k‰yd‰‰n kaikki rivit l‰pi, koska ensimm‰inen rivi voi antaa v‰‰r‰n tuloksen (esim merkkijono-kentt‰ jossa sattuu olemaan pelkk‰ numero ekalla rivill‰).
				}
			}
		} catch(Exception e){
			
		} finally {
			if(mapReader!= null)
				try {
					mapReader.close();
				} catch (IOException e) {}
		}
		
		if(type == null)
			type = KuvDefinition.Tyyppi.M.toString();
		
		return type;
	}
	
	public void setMArvotMukaan(boolean mukaan){
		boolMArvotMukaan=mukaan;
	}
	public boolean getMArvotMukaan(){
		return boolMArvotMukaan;
	}
	
}
