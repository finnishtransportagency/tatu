package fi.liikennevirasto.tatu.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.geotools.feature.SchemaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.vividsolutions.jts.io.ParseException;

import fi.liikennevirasto.tatu.CsvReader;
import fi.liikennevirasto.tatu.DatReader;
import fi.liikennevirasto.tatu.DbfReader;
import fi.liikennevirasto.tatu.KuvDefinition;
import fi.liikennevirasto.tatu.KuvReader;
import fi.liikennevirasto.tatu.ReaderResponse;
import fi.liikennevirasto.tatu.TatuException;
import fi.liikennevirasto.tatu.TatuReader;
import fi.liikennevirasto.tatu.ZipResponse;
import fi.liikennevirasto.tatu.ZipResponse.ZipStatus;
import fi.liikennevirasto.tatu.dao.ProcessDao;
import fi.liikennevirasto.tatu.model.Process;

@Service("mainControlService")
public class MainControlServiceImpl implements MainControlService {

	private static final Logger logger = Logger.getLogger(MainControlServiceImpl.class);

	private static final int REMOVE_FILES_RATE = 1;
			
	private static enum Filetype {
		KUV_DAT,
		CSV,
		DBF
	}

	private static enum Status {
		NO_FILES(0),
		PARTIAL_FILES(1),
		READY(2),
		RUNNING(3),
		ZIP_OK(4),
		ERROR(5);
		
		private int intValue;
		
		private Status(int status) {
			this.intValue = status;
		}
	}

	@Autowired
	private ProcessDao processDao;

	@Autowired
	private OsoiteverkkoService osoiteverkkoService;
	
	@Autowired
	private SettingsService settingsService;
	
	@Autowired
	private EmailService emailService;
	
	private static boolean isRunning = false; 
	private static long lastStartTime = 0;

	@Override
	public String createNewProcess(String emailAddress) {
		// Generate a new UUID
		String uuid = UUID.randomUUID().toString();
		
		// Create a new Process
		Process process = new Process();
		process.setUuid(uuid);
		process.setEmail(emailAddress);

		// store to db
		processDao.createProcess(process);

		return uuid;
	}

	@Override
	public void storeKuvFile(String sessionId, File kuvFile) throws TatuException, IOException {
		File file = storeFile(kuvFile, sessionId);
		handleKuvFile(sessionId, file);
	}

	@Override
	public void storeKuvFile(String sessionId, MultipartFile kuvFile) throws TatuException, FileNotFoundException, IOException {
		File file = storeFile(kuvFile, sessionId);
		handleKuvFile(sessionId, file);
	}
	
	@Override
	public void storeDatFile(String sessionId, MultipartFile datFile)
			throws TatuException, FileNotFoundException, IOException {
		File file = storeFile(datFile, sessionId);
		handleDatFile(sessionId, file, datFile.getOriginalFilename());
	}

	@Override
	public void storeDatFile(String sessionId, File datFile) throws TatuException, IOException {
		File file = storeFile(datFile, sessionId);
		handleDatFile(sessionId, file, datFile.getName());
	}
	
	@Override
	public void storeCsvFile(String sessionId, MultipartFile csvFile)
			throws TatuException, FileNotFoundException, IOException {
		File file = storeFile(csvFile, sessionId);
		handleCsvFile(sessionId, file, csvFile.getOriginalFilename());
	}

	@Override
	public void storeCsvFile(String sessionId, File csvFile) throws TatuException, IOException {
		File file = storeFile(csvFile, sessionId);
		handleCsvFile(sessionId, file, csvFile.getName());
	}
	
	@Override
	public void storeDbfFile(String sessionId, MultipartFile dbfFile)
			throws TatuException, FileNotFoundException, IOException {
		File file = storeFile(dbfFile, sessionId);
		handleDbfFile(sessionId, file, dbfFile.getOriginalFilename());
	}

	@Override
	public void storeDbfFile(String sessionId, File dbfFile) throws TatuException, IOException {
		File file = storeFile(dbfFile, sessionId);
		handleDbfFile(sessionId, file, dbfFile.getName());
	}
	
	private void handleKuvFile(String sessionId, File kuvFile) throws TatuException {
		Process process = processDao.getProcess(sessionId);
		if (process.getStatus() != Status.NO_FILES.intValue)
			throw new TatuException("process.getStatus() != Status.NO_FILES");
		process.setKuvfile(kuvFile.getName());
		process.setStatus(Status.PARTIAL_FILES.intValue);
		processDao.updateProcess(process);
	}

	private void handleDatFile(String sessionId, File datFile, String originalFilename) throws TatuException {
		handleDataFile(sessionId, datFile.getName(), originalFilename, Status.PARTIAL_FILES, Filetype.KUV_DAT);
	}

	private void handleCsvFile(String sessionId, File csvFile, String originalFilename) throws TatuException {
		handleDataFile(sessionId, csvFile.getName(), originalFilename, Status.NO_FILES, Filetype.CSV);
	}

	private void handleDbfFile(String sessionId, File dbfFile, String originalFilename) throws TatuException {
		handleDataFile(sessionId, dbfFile.getName(), originalFilename, Status.NO_FILES, Filetype.DBF);
	}
	
	private void handleDataFile(String sessionId, String dataFileName, String originalFilename, 
			Status requiredStatus, Filetype filetype) throws TatuException {

		Process process = processDao.getProcess(sessionId);
		if (process.getStatus() != requiredStatus.intValue)
			throw new TatuException("process.getStatus() != " +requiredStatus.toString());
		process.setDatafile(dataFileName);
		process.setFilename(FilenameUtils.getBaseName(originalFilename));
		process.setFiletype(filetype.name());
		process.setStatus(Status.READY.intValue);
		processDao.updateProcess(process);
		lastStartTime = 0;	// Run on next
	}

	@Override
	public ZipResponse getZipFile(String sessionId) {
		Process process = processDao.getProcess(sessionId);
		if (process == null)
			return new ZipResponse(ZipStatus.NOT_FOUND, null, 0, 0);
		if (process.getStatus() == Status.ZIP_OK.intValue) {
			String filePath = settingsService.getFilePath();
			return new ZipResponse(ZipStatus.OK, new File(filePath + process.getUuid() +".zip"), 
					process.getLinesTotal(), process.getLinesErrors());
		} else if (process.getStatus() == Status.ERROR.intValue) {
			return new ZipResponse(process.getError());
		} else {
			return new ZipResponse(ZipStatus.NOT_READY, null, 0, 0);
		}
	}

	@Override
	@Async
	@Scheduled(fixedRate=5000)
	public void processNext() {
		logger.debug("processNext");
		if (isRunning)
			return;
		if (lastStartTime + (REMOVE_FILES_RATE * 1000 * 60) > System.currentTimeMillis())
			return;
		
		isRunning = true;
		logger.debug("processNext ----- START");
		
		try {
			// Check if there is processes available
			List<Process> processes = processDao.getProcessesWithStatus(Status.READY.intValue);
			Process process = getFirstThatExists(processes);
			if (process != null) {
				logger.debug("processNext ----- process files");
				try {
					processFiles(process);
					lastStartTime = 0;
				} catch (TatuException e) {
					// write an error to the database.
					if (e.getMessage().length()<=250){
						process.setError(e.getMessage());
					}else{
						String message = e.getMessage().substring(0, 250);
						process.setError(message);
					}
					process.setStatus(Status.ERROR.intValue);
					processDao.updateProcess(process);
				}
				// Send email to user.
				emailService.sendMail(process);
				
			} else {
				// If nothing else to do, check if there is old files to remove
				process = processDao.getOldestProcess();
				if (process != null) {
					logger.debug("processNext ----- remove files");
					// Get TTL value from settings. Value is in minutes.
					int ttl = settingsService.getFileTtl() * 1000 * 60;
					logger.debug(new Date(System.currentTimeMillis() - ttl));
					if (process != null && process.getTimestamp().before(new Date(System.currentTimeMillis() - ttl))) {
						deleteOldFiles(process);
					}
				}
				lastStartTime = System.currentTimeMillis();
			}
	
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			// Usually database error
			// Wait for next update
			lastStartTime = System.currentTimeMillis();
		}

		isRunning = false;
		logger.debug("processNext ----- END");
	}
	
	// Asettaa tieosoiteverkon vuoden
	@Override
	public void setVuosi(Integer vuosi){
		settingsService.setVuosi(vuosi);
	}
	
	// Asettaa tiedon tuotetaanko m-arvot mukaan
	@Override
	public void setMArvot(Boolean marvot){
		settingsService.setMarvotTarvitaan(marvot);
	}
	
	// Asettaa tiedon halutaanko ajorataspesifi paikannus
	@Override
	public void setPaikannustapa(Boolean ajorataSpesifisyys){
		settingsService.setAjorataSpesifiPaikannus(ajorataSpesifisyys);
	}
	
	private Process getFirstThatExists(List<Process> processes) {
		String filePath = settingsService.getFilePath();
		for (Process process : processes) {
			File dataFile = new File(filePath + process.getDatafile());
			if (dataFile.exists())
				return process;
		}
		return null;
	}

	private void processFiles(Process process) throws TatuException, ParseException {
		process.setStatus(Status.RUNNING.intValue);
		processDao.updateProcess(process);
		
		Filetype filetype = Filetype.valueOf(process.getFiletype());
		
		// File paths
		String filePath = settingsService.getFilePath();
		String tempPath = settingsService.getTempPath();
		String shapeFilename = tempPath + process.getUuid();
		String zipFilename = filePath + process.getUuid() +".zip";

		logger.debug("filetype:" +filetype);
		logger.debug("shapeFilename:" +shapeFilename);
		logger.debug("zipFilename:" +zipFilename);
		logger.info("Lahtotiedosto: " + process.getFilename() + " | tyyppi: " + process.getFiletype() + " | process id: " + process.getUuid() + " | " + process.getTimestamp());
		
		try {
			TatuReader reader = null;
			switch (filetype) {
				case KUV_DAT:
					// Read KUV-file
					File kuvFile = new File(filePath + process.getKuvfile());
					List<KuvDefinition> kuvDefs = KuvReader.readFile(new FileInputStream(kuvFile));
		    		// Read DAT-file and create SHAPE to zip
					reader = new DatReader(kuvDefs);
					reader.setMArvotMukaan(settingsService.getMarvotTarvitaan());
					break;
				case CSV:
					reader = new CsvReader();
					CsvReader c = (CsvReader) reader;
					//c.boolMArvotMukaan = settingsService.getMarvotTarvitaan();
					c.setMArvotMukaan(settingsService.getMarvotTarvitaan());
					break;
				case DBF:
					reader = new DbfReader();
					reader.setMArvotMukaan(settingsService.getMarvotTarvitaan());
					break;
				default:
					logger.error("Tuntematon tiedostomuoto: " +filetype.toString());
					process.setError("Tuntematon tiedostomuoto: " +filetype.toString());
					process.setStatus(Status.ERROR.intValue);
			}

			if (reader != null) {
				File dataFile = new File(filePath + process.getDatafile());
				ReaderResponse response = reader.readFile(osoiteverkkoService, dataFile, shapeFilename, zipFilename, process.getFilename());
				
				// Set status OK and store to DB
				process.setStatus(Status.ZIP_OK.intValue);
				process.setLinesErrors(response.getErrorRows());
				process.setLinesTotal(response.getTotalRows());
			}

		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new TatuException("Tiedostojen luku ei onnistunut.");
		} catch (SchemaException e) {
			logger.error(e.getMessage(), e);
			throw new TatuException("Tiedostojen luku ei onnistunut.");
		}
		processDao.updateProcess(process);
		
	}
	
	private void deleteOldFiles(Process process) {
		String filePath = settingsService.getFilePath();

		// Delete kuv file
		deleteFile(filePath, process.getKuvfile());
		// Delete data file (.dat, .csv, .dbf..)
		deleteFile(filePath, process.getDatafile());
		// Delete zip file
		deleteFile(filePath, process.getUuid() +".zip");
		
		// Delete row from the database
		processDao.deleteProcess(process);
	}

	private static void deleteFile(String filePath, String filename) {
		if (filename != null) {
			File file = new File(filePath + filename);
			file.delete();
		}
	}
	
	private File storeFile(File file, String sessionId) throws IOException {
		String extension = FilenameUtils.getExtension(file.getName());
		File sharedFile = new File(settingsService.getFilePath() +sessionId +"." +extension);
		FileUtils.copyFile(file, sharedFile);
		return sharedFile;
	}

	private File storeFile(MultipartFile file, String sessionId) throws IOException,
			FileNotFoundException {

		logger.debug("FILENAME = " +file.getOriginalFilename());
		logger.debug("FOLDER = "+settingsService.getFilePath());
		String extension = FilenameUtils.getExtension(file.getOriginalFilename());
		File sharedFile = new File(settingsService.getFilePath() +sessionId +"." +extension);
		sharedFile.createNewFile();
		FileOutputStream fos = new FileOutputStream(sharedFile);
		fos.write(file.getBytes());
		fos.close(); 
		return sharedFile;
	}
	
}
