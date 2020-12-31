package fi.liikennevirasto.tatu;

import com.vividsolutions.jts.geom.Geometry;

public abstract class TatuGeometry {

	private Error error;
	
	public enum Error {
		OK("ok"),
		INVALID_ADDRESS("Tieosoitetta ei saatu selville"),
		TIE_NOT_FOUND("Tiet� ei tieverkolla"),
		AOSA_NOT_FOUND("Tien alkuosaa ei tieverkolla"),
		LOSA_NOT_FOUND("Tien loppuosaa ei tieverkolla"),
		FROM_NOT_FOUND("Alkuet�isyytt� ei l�ytynyt tieverkolta"),
		TO_NOT_FOUND("Loppuet�isyytt� ei l�ytynyt tieverkolta"),
		NULL_GEOMETRY("Tyhj� geometria");

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
