package fi.liikennevirasto.tatu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.log4j.Logger;
import org.geotools.feature.SchemaException;

import com.vividsolutions.jts.io.ParseException;

import fi.liikennevirasto.tatu.service.OsoiteverkkoService;

public class DatReader implements TatuReader {

	private static final Logger logger = Logger.getLogger(DatReader.class);
	private List<KuvDefinition> kuvDefs;
	public static Boolean boolMArvotMukaan = false;
	
	public DatReader(List<KuvDefinition> kuvDefs) {
		this.kuvDefs = kuvDefs;
	}
	
	public ReaderResponse readFile(OsoiteverkkoService osoiteverkkoService, File datFile, 
			String shapeFilename, String zipFilename, 
			String originalFilename) throws IOException, SchemaException, TatuException, ParseException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(datFile)));
		if(boolMArvotMukaan==true){
			kuvDefs.add(new KuvDefinition("ID", "M"));
		}

		String lineStr;
		int intRiviNro=0;

		ShapeWriter shapeWriter = new ShapeWriter(shapeFilename, originalFilename);
		shapeWriter.boolMArvotMukaan = boolMArvotMukaan;
		// Read all rows
		while ((lineStr = br.readLine()) != null) {
			TatuRow tatuRow = new TatuRow();
			intRiviNro++;
			
			for (KuvDefinition kuvDef : kuvDefs) {
				String dataStr;
				try {
					if(kuvDef.getPit()>0)
						dataStr = lineStr.substring(kuvDef.getPos()-1, kuvDef.getPos()-1 +kuvDef.getPit());
					else 
						dataStr = new String();
					tatuRow.addValue(dataStr, kuvDef);
				} catch (StringIndexOutOfBoundsException e) {
					br.close();
					throw new TatuException("Virheellinen DAT-tiedosto. Ongelman aiheuttaa rivi numero : "+ intRiviNro + " | Rivin data : " + lineStr);
				}
			}
			
			// Store to Shape
			shapeWriter.writeLine(osoiteverkkoService, kuvDefs, tatuRow);
		}
		br.close();
		
		return shapeWriter.createZipFile(zipFilename);
	}
	public void setMArvotMukaan(boolean mukaan){
		boolMArvotMukaan=mukaan;
	}
	public boolean getMArvotMukaan(){
		return boolMArvotMukaan;
	}
	
}
