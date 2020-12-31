package fi.liikennevirasto.tatu;

public class TatuFileReader {

	protected static void checkForError(String filetype, boolean tie, boolean aosa, boolean ajr, boolean aet) throws TatuException {
		StringBuilder sb = new StringBuilder();
	
		int fieldsAdded = 0;
		if (!tie) {
			sb.append("'tie'");
			fieldsAdded++;
		}
		if (!aosa) {
			if (fieldsAdded > 0)
				sb.append(", ");
			sb.append("'aosa'");
			fieldsAdded++;
		}
		if (!ajr) {
			if (fieldsAdded > 0)
				sb.append(", ");
			sb.append("'ajr'");
			fieldsAdded++;
		}
		if (!aet) {
			if (fieldsAdded > 0)
				sb.append(", ");
			sb.append("'aet'");
			fieldsAdded++;
		}

		if (fieldsAdded == 1)
			throw new TatuException("Pakollinen kenttä puuttuu " +filetype +"-tiedostosta: " +sb.toString());
		if (fieldsAdded > 1)
			throw new TatuException("Pakollisia kenttiä puuttuu " +filetype +"-tiedostosta: " +sb.toString());
		
	}


}
