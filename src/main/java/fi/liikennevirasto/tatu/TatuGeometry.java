package fi.liikennevirasto.tatu;

import com.vividsolutions.jts.geom.Geometry;

public abstract class TatuGeometry {

	private Error error;
	
	public enum Error {
		OK("ok"),
		INVALID_ADDRESS("Tieosoitetta ei saatu selville"),
		TIE_NOT_FOUND("Tietä ei tieverkolla"),
		AOSA_NOT_FOUND("Tien alkuosaa ei tieverkolla"),
		LOSA_NOT_FOUND("Tien loppuosaa ei tieverkolla"),
		FROM_NOT_FOUND("Alkuetäisyyttä ei löytynyt tieverkolta"),
		TO_NOT_FOUND("Loppuetäisyyttä ei löytynyt tieverkolta"),
		NULL_GEOMETRY("Tyhjä geometria");

		private String text;
		
		private Error(String text) {
			this.text = text;
		}
		
		@Override
		public String toString() {
			return text;
		}
	}
	
	public TatuGeometry(Error error) {
		this.error = error;
	}

	public Error getError() {
		return error;
	}
	
	public abstract Geometry getGeometry();
	
}
