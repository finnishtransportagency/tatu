package fi.liikennevirasto.tatu;

import java.util.ArrayList;

import fi.liikennevirasto.tatu.KuvDefinition.Tyyppi;
import fi.liikennevirasto.tatu.TatuGeometry.Error;
import fi.liikennevirasto.tatu.service.OsoiteverkkoService;

public class TatuRow {

	private Integer tie, aosa, ajr, aet, losa, let, offset;

	private ArrayList<Object> fieldValues = new ArrayList<Object>();

	public void addValue(Object value, KuvDefinition kuvDef) {
		String stringValue = null;
		Integer intValue = null;
		Double doubleValue = null;

		if (value instanceof String) {
			stringValue = ((String) value).trim();
			try {
				intValue = Integer.parseInt(stringValue);
			} catch (Exception e) {
				// No need to log.
			}
		} else if (value instanceof Integer) {
			intValue = (Integer) value;
			stringValue = intValue.toString();
		} else if (value instanceof Double) {
			doubleValue = (Double) value;
			stringValue = doubleValue.toString();
			intValue = doubleValue.intValue();
		}
				
		if (stringValue != null && stringValue.trim().isEmpty()) {
			// empty value.
			fieldValues.add(null);
		} else {
			if (kuvDef.getTyyppi() == Tyyppi.D)
				fieldValues.add(value);
			else if (kuvDef.getTyyppi() == Tyyppi.M)
				fieldValues.add(value);
			else if (kuvDef.getTyyppi() == Tyyppi.J)
				fieldValues.add(intValue);
			else if (kuvDef.getTyyppi() == Tyyppi.K)
				fieldValues.add(intValue);
			else if (kuvDef.getTyyppi() == Tyyppi.Double){
				
				fieldValues.add(doubleValue);
			}
			else
				fieldValues.add(value);
			
			// Vaihtoehtoisia kentän nimiä lisätty csv ja dbf muotoisille tiedostoille.
			if (kuvDef.getLyhenne().toLowerCase().compareTo("tie") == 0 || kuvDef.getLyhenne().toLowerCase().compareTo("tienro") == 0)
				tie = intValue;
			else if (kuvDef.getLyhenne().toLowerCase().compareTo("ajr") == 0 || kuvDef.getLyhenne().toLowerCase().compareTo("ajorata") == 0)
				ajr = intValue;
			else if (kuvDef.getLyhenne().toLowerCase().compareTo("aosa") == 0 || kuvDef.getLyhenne().toLowerCase().compareTo("osa") == 0 || kuvDef.getLyhenne().toLowerCase().compareTo("alkuosa") == 0)
				aosa = intValue;
			else if (kuvDef.getLyhenne().toLowerCase().compareTo("aet") == 0 || kuvDef.getLyhenne().toLowerCase().compareTo("et") == 0 || kuvDef.getLyhenne().toLowerCase().compareTo("alkuet") == 0 || kuvDef.getLyhenne().toLowerCase().compareTo("etaisyys") == 0)
				aet = intValue;
			else if (kuvDef.getLyhenne().toLowerCase().compareTo("losa") == 0 || kuvDef.getLyhenne().toLowerCase().compareTo("loppuosa") == 0)
				losa = intValue;
			else if (kuvDef.getLyhenne().toLowerCase().compareTo("let") == 0 || kuvDef.getLyhenne().toLowerCase().compareTo("loppuet") == 0)
				let = intValue;
			else if (kuvDef.getLyhenne().toLowerCase().compareTo("offset") == 0){
				offset = intValue;
			}
			
		}
	}
	
	public ArrayList<Object> getFieldValues() {
		return fieldValues;
	}

	public boolean isValidAddress() {
		if (tie == null || aosa == null || aet == null || ajr == null)
			return false;
		return true;
	}
	
	public boolean isLine() {
		if (losa == null || let == null)
			return false;
		if (!isValidAddress())
			return false;
		return (!(aosa.compareTo(losa) == 0 && aet.compareTo(let) == 0));
	}

	public TatuGeometry getTatuGeometry(OsoiteverkkoService osoiteverkkoService) {
		if (!isValidAddress())
			return new TatuPointGeometry(null, Error.INVALID_ADDRESS);
		if (isLine())
			if (offset!=null){
				return osoiteverkkoService.getTieosoitevaliAsGeometry(tie, aosa, aet, losa, let, ajr, offset);
			}else{
				return osoiteverkkoService.getTieosoitevaliAsGeometry(tie, aosa, aet, losa, let, ajr);
			}
		else
			if (offset!=null){
				return osoiteverkkoService.getTieosoiteAsPoint(tie, aosa, aet, ajr, offset);
			}else{
				return osoiteverkkoService.getTieosoiteAsPoint(tie, aosa, aet, ajr);
			}
		
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (isValidAddress()) {
			sb.append("tie=");
			sb.append(tie);
			sb.append(", ajr=");
			sb.append(ajr);
			sb.append(", aosa=");
			sb.append(aosa);
			sb.append(", aet=");
			sb.append(aet);
			if (losa != null) {
				sb.append(", losa=");
				sb.append(losa);
			} if (let != null) {
				sb.append(", let=");
				sb.append(let);
			}
		} sb.append(", DATA: ");
		boolean first = true;
		for (Object object : fieldValues) {
			if (object != null) {
				if (!first)
					sb.append(", ");
				sb.append(object.toString());
				first = false;
			}
		}
		
		return sb.toString();
	}
}
