package fi.liikennevirasto.tatu;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.geotools.feature.SchemaException;

import com.linuxense.javadbf.DBFException;
import com.linuxense.javadbf.DBFField;
import com.linuxense.javadbf.DBFReader;

import fi.liikennevirasto.tatu.service.OsoiteverkkoService;

public class DbfReader extends TatuFileReader implements TatuReader {

	private static final Logger logger = Logger.getLogger(DbfReader.class);
	public static Boolean boolMArvotMukaan = false;
	
	public ReaderResponse readFile(OsoiteverkkoService osoiteverkkoService, File dbfFile, 
			String shapeFilename, String zipFilename, String originalFilename) throws IOException, TatuException, SchemaException {

		FileInputStream inputStream = new FileInputStream(dbfFile); 
		
		DBFReader reader = new DBFReader(inputStream);
		
		// Hakee sarakkeiden nimet
		List<KuvDefinition> kuvDefs = getKuvDefinitions(reader);
		
		// Käsittele varsinainen data
		ShapeWriter shapeWriter = new ShapeWriter(shapeFilename, originalFilename);
		shapeWriter.boolMArvotMukaan = boolMArvotMukaan;
		
		Object[] rowObjects;

			while((rowObjects = reader.nextRecord()) != null) {
				TatuRow tatuRow = new TatuRow();
				
				for (int i = 0; i < kuvDefs.size(); i++) {
					KuvDefinition kuvDef = kuvDefs.get(i);
					
					if(rowObjects.length>i)
						tatuRow.addValue(rowObjects[i], kuvDef);
					else
						tatuRow.addValue(new String(), kuvDef);
				}
				// Store to Shape | hakee samalla sijainnin eli geometrian kohteelle
				shapeWriter.writeLine(osoiteverkkoService, kuvDefs, tatuRow);			
			}

		inputStream.close();
		
		return shapeWriter.createZipFile(zipFilename);
	}

	private static List<KuvDefinition> getKuvDefinitions(DBFReader reader) throws DBFException, TatuException {
		boolean tie = false, aosa = false, ajr = false, aet = false;

		List<KuvDefinition> kuvDefs = new ArrayList<KuvDefinition>();
		
		for (int i = 0; i < reader.getFieldCount(); i++) {
			DBFField field = reader.getField(i);
			String name = field.getName();
			
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
				byte[] dataTypes = new byte[1];
				dataTypes[0] = field.getDataType();
				String type = new String(dataTypes);
	
				if(field.getDecimalCount()==0){
					if (type.compareTo("C") == 0)
						type = "M";
					else if (type.compareTo("N") == 0)
						type = "K";
				} else {
					type = "Double";
				}

				kuvDefs.add(new KuvDefinition(name, type));
			}
			
		}
		if(boolMArvotMukaan==true){
			kuvDefs.add(new KuvDefinition("ID", "M"));
		}
		checkForError("dbf", tie, aosa, ajr, aet);
		
		return kuvDefs; 
	}
	public void setMArvotMukaan(boolean mukaan){
		boolMArvotMukaan=mukaan;
	}
	public boolean getMArvotMukaan(){
		return boolMArvotMukaan;
	}
}
