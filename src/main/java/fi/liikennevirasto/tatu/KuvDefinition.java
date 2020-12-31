package fi.liikennevirasto.tatu;

public class KuvDefinition {

	public enum Tyyppi {
		D, K, J, M, Double;
	}
	
	private String lyhenne;
	private int pos;
	private int pit;
	private String selite;
	private String yksikko;
	private Tyyppi tyyppi;
	
	public KuvDefinition(String lyhenne, String tyyppi) throws TatuException {
		this.lyhenne = lyhenne;
		
		try {
			this.tyyppi = Tyyppi.valueOf(tyyppi);
		} catch (NumberFormatException e) {
			throw new TatuException("Tuntematon TYYPPI-arvo: " +tyyppi);
		}
	}
	
	public KuvDefinition(String lyhenne, String pos, String pit, String selite, 
			String yksikko, String tyyppi) throws TatuException {

		this(lyhenne, tyyppi);
		
		try {
			this.pos = Integer.parseInt(pos);
		} catch (NumberFormatException e) {
			throw new TatuException("Virheellinen POS-arvo: " +pos);
		}

		try {
			this.pit = Integer.parseInt(pit);
		} catch (NumberFormatException e) {
			throw new TatuException("Virheellinen PIT-arvo: " +pit);
		}

		this.selite = selite;
		this.yksikko = yksikko;

	}

	public String getLyhenne() {
		return lyhenne;
	}

	public int getPos() {
		return pos;
	}

	public int getPit() {
		return pit;
	}

	public String getSelite() {
		return selite;
	}

	public String getYksikko() {
		return yksikko;
	}

	public Tyyppi getTyyppi() {
		return tyyppi;
	}
}
