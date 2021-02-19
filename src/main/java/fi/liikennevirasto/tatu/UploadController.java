package fi.liikennevirasto.tatu;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.WebUtils;

import fi.liikennevirasto.tatu.ZipResponse.ZipStatus;
import fi.liikennevirasto.tatu.service.MainControlService;
import fi.liikennevirasto.tatu.service.SettingsService;

@Controller
public class UploadController {

	private static final Logger logger = Logger.getLogger(UploadController.class);

	private static final String SESSION_ID_KEY ="SessionId";

	private static final String EMAIL_HEADER_NAME = "Host";
	
	@Autowired
	private MainControlService mainControlService;
	
	@RequestMapping(value = "/", method = {RequestMethod.GET})
	public String root(HttpServletRequest request) {
		clearSessionId(request);
    	return "index";
    }
	
	@RequestMapping(value = "/index.html", method = {RequestMethod.GET})
	public String index(HttpServletRequest request) {
		clearSessionId(request);
    	return "index";
    }

	@RequestMapping(value = "/upload.html", method = {RequestMethod.GET, RequestMethod.POST})
	public String upload(Model model, HttpServletRequest request) {
		
		if (WebUtils.hasSubmitParameter(request, "kuv"))
			model.addAttribute("filetype", "KUV");
		if (WebUtils.hasSubmitParameter(request, "dbf"))
			model.addAttribute("filetype", "DBF");
		if (WebUtils.hasSubmitParameter(request, "csv"))
			model.addAttribute("filetype", "CSV");
		
    	return "upload";
    }
	
    @RequestMapping(value = "/uploadDone.html", method = RequestMethod.POST)
    public String handleFormUpload(Model model, HttpServletRequest request,
    		@RequestParam("filetype") String filetype,
    		@RequestParam("tieosoiteverkon_tyyppi") String tieosoiteverkonTyyppi, // Ei k‰ytet‰ mihink‰‰n, mutta tarvitaan k‰yttˆliittym‰‰ varten.
    		@RequestParam(value="tieosoiteverkon_vuosi", required=false) Integer tieosoiteverkonVuosi,
    		@RequestParam(required=false, value="marvot", defaultValue="Ei") String marvot,
    		@RequestParam(required=false, value="paikannustapa", defaultValue="Ei") String paikannustapa,
    		@RequestParam("file") MultipartFile file) {
    	
		try {
	        if (!file.isEmpty()) {
        		String sessionId = getSessionId(request);
	        	
        		if (tieosoiteverkonVuosi == null) // Jos k‰ytet‰‰n viimeisint‰
        			tieosoiteverkonVuosi = new Integer(20211);
        	
        		mainControlService.setVuosi(tieosoiteverkonVuosi);
        		logger.debug("K‰ytet‰‰n vuoden " + tieosoiteverkonVuosi + " tieosoiteverkkoa.");
        		
        		if(marvot!=null){ //marvot on null, jos sit‰ ei tsekattu lomakkeella
        			if(!marvot.equalsIgnoreCase("Ei")){
        				mainControlService.setMArvot(Boolean.TRUE);
        				logger.debug("M-arvot tulee mukaan.");
        			}else{
        				mainControlService.setMArvot(Boolean.FALSE);
        				logger.debug("M-arvot EI tule mukaan.");
        			}
        		}else{
    				mainControlService.setMArvot(Boolean.FALSE);
    				logger.debug("M-arvot EI tule mukaan.");
    			}
        		
        		if(paikannustapa!=null){ //paikannustapa on null, jos sit‰ ei tsekattu lomakkeella
        			if(!paikannustapa.equalsIgnoreCase("Ei")){
        				mainControlService.setPaikannustapa(Boolean.TRUE);
        				logger.debug("Paikannustapa on ajoratakohtainen");
        			}else{
        				mainControlService.setPaikannustapa(Boolean.FALSE);
        				logger.debug("Paikannustapa on tiekohtainen.");
        			}
        		}else{
    				mainControlService.setPaikannustapa(Boolean.FALSE);
    				logger.debug("Paikannustapa on tiekohtainen.");
    			}
        		
	        	if (filetype.toLowerCase().compareTo("dbf") == 0) {
	        		logger.debug("This is DBF!");
	        		mainControlService.storeDbfFile(sessionId, file);
	        	} else if (filetype.toLowerCase().compareTo("kuv") == 0) {
	        		logger.debug("This is KUV!");
	        		mainControlService.storeKuvFile(sessionId, file);
	        		// Ask for a dat-file next.
	        		model.addAttribute("filetype", "DAT");
	        		return "upload";
	        	} else if (filetype.toLowerCase().compareTo("dat") == 0) {
	        		logger.debug("This is DAT!");
	        		mainControlService.storeDatFile(sessionId, file);
	        	} else if (filetype.toLowerCase().compareTo("csv") == 0) {
	        		mainControlService.storeCsvFile(sessionId, file);
	        	}
	        	model.addAttribute("id", sessionId);
	        	return "redirect:download.html";
	        }
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return returnError(model, "Virhe", "Tiedoston l&auml;hetys ei onnistunut.");
    }

    @RequestMapping(value = "/download.html", method = {RequestMethod.GET, RequestMethod.POST})
    public String download(Model model, @RequestParam("id") String id) throws Exception {
 
        ZipResponse zipResponse = mainControlService.getZipFile(id);
        if (zipResponse.getZipStatus() == ZipStatus.NOT_FOUND) {
    		return returnError(model, "Tuntematon ID", "V&auml;&auml;r&auml; tunnus, tai muunnos on jo poistunut.");
        }
        else if (zipResponse.getZipStatus() == ZipStatus.NOT_READY)
        	return "notReady";
        else if (zipResponse.getZipStatus() == ZipStatus.ERROR) {
    		return returnError(model, "Virhe muunnoksessa", zipResponse.getErrorMessage());
        }
        else if (zipResponse.getZipStatus() != ZipStatus.OK) {
    		return returnError(model, "Tuntematon ID", "V&auml;&auml;r&auml; tunnus, tai muunnos on jo poistunut.");
        }
        
    	model.addAttribute("id", id);
    	model.addAttribute("totalRows", zipResponse.getTotalRows());
    	model.addAttribute("errorRows", zipResponse.getErrorRows());
        return "download";
    }    

    @RequestMapping(value = "/downloadFile.html", method = {RequestMethod.GET, RequestMethod.POST})
    public String downloadFile(HttpServletResponse response, 
    		@RequestParam("id") String id) throws Exception {
 
        ZipResponse zipResponse = mainControlService.getZipFile(id);
        if (zipResponse.getZipStatus() == ZipStatus.NOT_FOUND)
        	return "redirect:notFound";
        else if (zipResponse.getZipStatus() == ZipStatus.NOT_READY)
        	return "notReady";
        else if (zipResponse.getZipStatus() != ZipStatus.OK)
        	return "redirect:notOK";
        
        File file = zipResponse.getZipFile(); 
        
        response.setContentType("application/zip");
//	        response.setContentLength(file.length());
        response.addHeader("Content-Disposition", "attachment; filename=" +file.getName());	 
        response.addHeader("Expires", "0");	 

        BufferedInputStream buf = null;
        ServletOutputStream outputStream = response.getOutputStream();
		try {
			FileInputStream input = new FileInputStream(file);
			buf = new BufferedInputStream(input);
			int readBytes = 0;
			//read from the file; write to the ServletOutputStream
			while ((readBytes = buf.read()) != -1)
				outputStream.write(readBytes);
			} catch (IOException ioe) {
				throw new ServletException(ioe.getMessage());
			} finally {
				if (outputStream != null)
					outputStream.close();
				if (buf != null)
				buf.close();
			}	        
		
		return null;
 
    }    

	private String getSessionId(HttpServletRequest request) {
		String sessionId = (String) request.getSession().getAttribute(SESSION_ID_KEY);
		if (sessionId == null) {
			// Create a new session to database.
			
			// Get email address from request header
			String emailAddress = request.getHeader(EMAIL_HEADER_NAME);
			
			sessionId = mainControlService.createNewProcess(emailAddress);
			request.getSession().setAttribute(SESSION_ID_KEY, sessionId);
		}
		
		return sessionId;
	}
	
	private void clearSessionId(HttpServletRequest request) {
		request.getSession().setAttribute(SESSION_ID_KEY, null);
	}
	
	private String returnError(Model model, String title, String message) {
		model.addAttribute("title", title);
		model.addAttribute("message", message);
    	return "error";
	}
	
}
