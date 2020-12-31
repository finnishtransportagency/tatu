package fi.liikennevirasto.tatu.service;


public interface SettingsService {

	public String getFilePath();
	public String getTempPath();
	public int getFileTtl();
	
	// Asettaa tieosoiteverkon vuoden
	public void setVuosi(Integer vuosi);

	// Palauttaa vuoden, jonka tieosoiteverkkoa k‰ytet‰‰n
	public int getVuosi();
	
	// Asettaa tiedon siit‰ tuotetaanko m-arvot
	public void setMarvotTarvitaan(Boolean marvot);

	// Tarvitaanko m-arvot
	public Boolean getMarvotTarvitaan();
	
	// Palauttaa tiedon halutaanko ajorataspesifi paikannus
	public Boolean getAjorataSpesifiPaikannus();
	
	// Asettaa tiedon halutaanko ajorataspesifi paikannus
	public void setAjorataSpesifiPaikannus(Boolean ajorataSpesifisyys);

}
