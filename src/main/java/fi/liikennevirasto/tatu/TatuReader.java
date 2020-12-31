package fi.liikennevirasto.tatu;

import java.io.File;
import java.io.IOException;

import org.geotools.feature.SchemaException;

import fi.liikennevirasto.tatu.service.OsoiteverkkoService;

public interface TatuReader {

	public ReaderResponse readFile(OsoiteverkkoService osoiteverkkoService, File dataFile, 
			String shapeFilename, String zipFilename, String originalFilename) 
					throws IOException, TatuException, SchemaException;

	public void setMArvotMukaan(boolean mukaan);
	public boolean getMArvotMukaan();
}
