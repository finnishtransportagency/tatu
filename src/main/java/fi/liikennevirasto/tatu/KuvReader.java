package fi.liikennevirasto.tatu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class KuvReader {
	
	private static final String LYHENNE_STRING = "LYHENNE";
	private static final String POS_STRING = "POS";
	private static final String PIT_STRING = "PIT";
	private static final String SELITE_STRING = "SELITE";
	private static final String YKSIKKO_STRING = "YKSIKK";
	private static final String TYYPPI_STRING = "TYYPPI";

	public static List<KuvDefinition> readFile(InputStream inputStream) throws TatuException {

		ArrayList<KuvDefinition> results = new ArrayList<KuvDefinition>();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

		// Positions
		int lyhennePos = 0, posPos = 0, pitPos = 0, selitePos = 0, yksikkoPos = 0, tyyppiPos = 0;
		
		try {
			String lineStr;
			int currentLine = 0;
			boolean headerFound = false;
			while ((lineStr = br.readLine()) != null) {
				currentLine++;
				// Find header line
				if (!headerFound) {
					if (lineStr.startsWith(LYHENNE_STRING)) {
						headerFound = true;
						// Read positions.
						// Not sure if this is needed

						// This is always 0;
						lyhennePos = 0;
						posPos = lineStr.indexOf(POS_STRING);
						pitPos = lineStr.indexOf(PIT_STRING);
						selitePos = lineStr.indexOf(SELITE_STRING);
						yksikkoPos = lineStr.indexOf(YKSIKKO_STRING);
						tyyppiPos = lineStr.indexOf(TYYPPI_STRING);
						if (posPos < 0) throw new TatuException("KUV-tiedosto: Tietoa ei löytynyt: " +POS_STRING);
						if (pitPos < 0) throw new TatuException("KUV-tiedosto: Tietoa ei löytynyt: " +PIT_STRING);
						if (selitePos < 0) throw new TatuException("KUV-tiedosto: Tietoa ei löytynyt: " +SELITE_STRING);
						if (yksikkoPos < 0) throw new TatuException("KUV-tiedosto: Tietoa ei löytynyt: " +YKSIKKO_STRING);
						if (tyyppiPos < 0) throw new TatuException("KUV-tiedosto: Tietoa ei löytynyt: " +TYYPPI_STRING);
					}
				} else {
					// Read rest of the lines
					// lyhenne
					String lyhenne = lineStr.substring(lyhennePos, posPos).trim();
					String pos = lineStr.substring(posPos, pitPos).trim();
					String pit = lineStr.substring(pitPos, selitePos).trim();
					String selite = lineStr.substring(selitePos, yksikkoPos).trim();
					String yksikko = lineStr.substring(yksikkoPos, tyyppiPos).trim();
					String tyyppi = lineStr.substring(tyyppiPos, tyyppiPos + 1).trim();
					
					// Add to results
					try {
						results.add(new KuvDefinition(lyhenne, pos, pit, selite, yksikko, tyyppi));
					} catch (TatuException e) {
						throw new TatuException("KUV-tiedoston luvussa virhe rivillä: " +currentLine + ": " +e.getMessage());
					}
				}
			}
			
		} catch (IOException e) {
			throw new TatuException("KUV-tiedoston luku ei onnistunut");
		}
		
		if (results.size() == 0)
			throw new TatuException("Virheellinen KUV-tiedosto.");
		
		return results;
	}
}
