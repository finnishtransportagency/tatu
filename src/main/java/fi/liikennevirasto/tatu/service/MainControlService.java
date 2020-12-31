package fi.liikennevirasto.tatu.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.geotools.feature.SchemaException;
import org.springframework.web.multipart.MultipartFile;

import fi.liikennevirasto.tatu.TatuException;
import fi.liikennevirasto.tatu.ZipResponse;

public interface MainControlService {

	public String createNewProcess(String emailAddress);

	public void storeKuvFile(String sessionId, File kuvFile) throws TatuException, IOException;
	public void storeKuvFile(String sessionId, MultipartFile kuvFile) throws TatuException, FileNotFoundException, IOException;
	public void storeDatFile(String sessionId, File datFile) throws TatuException, IOException;
	public void storeDatFile(String sessionId, MultipartFile datFile) throws TatuException, FileNotFoundException, IOException;
	public void storeCsvFile(String sessionId, File csvFile) throws TatuException, IOException;
	public void storeCsvFile(String sessionId, MultipartFile csvFile) throws TatuException, FileNotFoundException, IOException;
	public void storeDbfFile(String sessionId, File dbfFile) throws TatuException, IOException;
	public void storeDbfFile(String sessionId, MultipartFile dbfFile) throws TatuException, FileNotFoundException, IOException;
	
	public void processNext() throws FileNotFoundException, TatuException, IOException, SchemaException;

	public ZipResponse getZipFile(String sessionId);
	
	// Asettaa tieosoiteverkon vuoden
	public void setVuosi(Integer vuosi);
	//tuotetaanko m-arvot mukaan
	public void setMArvot(Boolean marvot);
	//Halutaanko ajorataspesifi paikannus
	public void setPaikannustapa(Boolean ajorataSpesifisyys);
}
