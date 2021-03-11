package fi.liikennevirasto.tatu.service;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Hashtable;





import org.apache.log4j.Logger;
import org.hibernate.spatial.jts.mgeom.MCoordinate;
import org.hibernate.spatial.jts.mgeom.MGeometryException;
import org.hibernate.spatial.jts.mgeom.MGeometryFactory;
import org.hibernate.spatial.jts.mgeom.MLineString;
import org.hibernate.spatial.jts.mgeom.MultiMLineString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.util.Debug;

import fi.liikennevirasto.tatu.TatuGeometry.Error;
import fi.liikennevirasto.tatu.TatuLineGeometry;
import fi.liikennevirasto.tatu.TatuPointGeometry;
import fi.liikennevirasto.tatu.dao.OsoiteverkkoDao;
import fi.liikennevirasto.tatu.model.Tieosa;

@Service("osoiteverkkoService")
public class OsoiteverkkoServiceImpl implements OsoiteverkkoService {

	private static final Logger logger = Logger.getLogger(OsoiteverkkoServiceImpl.class);
	
	List<Tieosa> lastTieosoitevali;
	int lastTieFrom;
	int lastTieTo;
	
	@Autowired
	OsoiteverkkoDao osoiteverkkoDao;
	
	@Autowired
	SettingsService settingsService;
	
	@Override
	public Tieosa getByRouteId(int routeId) {
		return osoiteverkkoDao.getByRouteId(routeId);
	}

	@Override
	public List<Tieosa> getTieosoitevali(int tie, int tie2) {
		logger.debug("tiet " +tie +" - " +tie2);
		return osoiteverkkoDao.getTieosoitevali(tie, tie2);
	}

	@Override
	public TatuLineGeometry getTieosoitevaliAsGeometry(int tie, int aosa,
			double aet, int losa, double let, Integer ajorata) throws ParseException {
		//Alkuperäinen sisältö kommentoitu pois, kutsuu muokattua versiota offsetilla 0.
		return getTieosoitevaliAsGeometry(tie, aosa, aet, losa, let, ajorata, 0);
		
		
	}
	
	
	//Alkuperäinen funktio johon lisätty offset 
	//public TatuLineGeometry getTieosoitevaliAsGeometry(int tie, int aosa,
	//		double aet, int losa, double let, int ajorata) {
	@Override
	public TatuLineGeometry getTieosoitevaliAsGeometry(int tie, int aosa,
			double aet, int losa, double let, Integer ajorata, int offset) throws ParseException {

		boolean aosaFound = false;
		boolean losaFound = false;
		boolean fromFound = false;
		boolean toFound = false;
		boolean lineFound = false;
		
        List<Tieosa> tieosoitevali = getTieosoitevali(tie);
        if (tieosoitevali.size() == 0)
        	return new TatuLineGeometry(null, Error.TIE_NOT_FOUND);

		MGeometryFactory gf = new MGeometryFactory();
		ArrayList<MLineString> results = new ArrayList<MLineString>();
		for (Tieosa tieosa : tieosoitevali) {
			if (tieosa.getTie() == tie && tieosa.getVuosi() == settingsService.getVuosi()) {
				if (isValidAjorata(ajorata, tieosa.getAjorata())) {
					List<MLineString> line = null;
					try {
						
						if (tieosa.getOsa() == aosa && tieosa.getOsa() == losa) {
							aosaFound = true;
							losaFound = true;
							// only 1 osa. get from aet to let
							line = tieosa.getLines(aet, let, gf);
							if (line != null && line.size() > 0) {
								if (!fromFound)
									fromFound = isMeasureInLine(line, aet);
								if (!toFound)
									toFound = isMeasureInLine(line, let);
							}
						} else if (tieosa.getOsa() == aosa) {
							// this is first osa. get only line from aet to end
							aosaFound = true;
							line = tieosa.getLines(aet, Double.MAX_VALUE, gf);
							if (line != null && line.size() > 0) {
								if (!fromFound)
									fromFound = isMeasureInLine(line, aet);
							}
						} else if (tieosa.getOsa() == losa) {
							// This is last osa.
							losaFound = true;
							if (let == 0) {
								// This is last osa but user requested let 0. 
								// End geometry is in previous osa
								toFound = true;
							} else {
								// this is last osa. get only line from start to let
								line = tieosa.getLines(0, let, gf);
								if (line != null && line.size() > 0) {
									if (!toFound)
										toFound = isMeasureInLine(line, let);
								}
							}
						} else if (tieosa.getOsa() > aosa && tieosa.getOsa() < losa){
							// this osa is in the middle
							line = tieosa.getLines(0, Double.MAX_VALUE, gf);
						}
					} catch (MGeometryException e) {
						// No need to handle. Error code will tell the result.
					}
					
					// Something found. Add line to results
					if (line != null && line.size() > 0) {
						lineFound = true;
						results.addAll(line);
					}
				}
			}
		}

		// Check errors
		Error error = Error.OK;

		if (!aosaFound)
			error = Error.AOSA_NOT_FOUND;
		else if (!losaFound)
			error = Error.LOSA_NOT_FOUND;
		else if (!lineFound)
        	error = Error.NULL_GEOMETRY;
		else if (!fromFound)
			error = Error.FROM_NOT_FOUND;
		else if (!toFound)
			error = Error.TO_NOT_FOUND;
		
		MultiMLineString mls = gf.createMultiMLineString(results.toArray(new MLineString[0]));
		
		//Jos offset on määrätty, kutsutaan offset funktiota ja palautetaan sen tulos
		if (offset!=0){
			MultiMLineString mls2 = teeSivuttaissiirto(mls, offset);
			return new TatuLineGeometry(mls2, error);
		}
		
		return new TatuLineGeometry(mls, error);
	}
	
	
	/**
	 * Siirtää MultiMLineStringiä sivusuunnassa offsetin verran sivuttaissuunnassa
	 * 
	 * @param mls Siirrettävä MultiMLineString
	 * @param offset Sivuttaissiirtymän määrä. 
	 * Positiivinen offset siirtää viivan kulkusuunnassa vasemmalle, 
	 * negatiivinen offset siirtää viivaa kulkusuunnassa oikealle
	 * @return MultiMlineString mls (palauttaa saamansa viivan siirrettynä)
	 */
	public MultiMLineString teeSivuttaissiirto(MultiMLineString mls, double offset){
		for (int i = 0; i < mls.getNumGeometries(); i++) {
			Hashtable hashTableOffsetKoordinaatit = new Hashtable();
			Coordinate[] koordinaattiLista = mls.getGeometryN(i).getCoordinates();
			
			for(int n=0; n<koordinaattiLista.length; n++){
				Coordinate coordSiirrettavaPiste=null;
				Coordinate coordSiirrettyPiste=null;
				Coordinate coordSegmPisteEdellinen=null;
				Coordinate coordSegmPisteSeuraava=null;
				Coordinate coordOffsetPisteEdellinen=null;
				Coordinate coordOffsetPisteSeuraava=null;
				
				coordSiirrettavaPiste=koordinaattiLista[n];
				// Hae edeltävä piste/segmentti
				if (n>0){
					// Jos n-e == n, hae edellinen piste kunnes segment(n-e, n).length>0
					for(int e=1; e<=n; e++){
						coordSegmPisteEdellinen=koordinaattiLista[n-e];
						if(coordSegmPisteEdellinen!=null && !coordSegmPisteEdellinen.equals2D(coordSiirrettavaPiste)){
							break;
						}
					}
					// 		Jos n-e == null, kyseessä on ensimmäinen segmentti
					//			*käytetään vain seuraava segmentti
				}
				
				// Hae seuraava segmentti
				if(n<koordinaattiLista.length){
					//Jos n == n+1, hae seuraava piste kunnes segment(n, n+k).length>0
					
					// 		Jos n+k == null, kyseessä on viimeinen segmentti
					//			*käytetään vain edellinen segmentti
					int sMax = koordinaattiLista.length-1-n;
					for(int s=1; s<=sMax; s++){
						coordSegmPisteSeuraava=koordinaattiLista[n+s];
						if(coordSegmPisteSeuraava!=null && !coordSegmPisteSeuraava.equals2D(coordSiirrettavaPiste)){
							break;
						}
					}
				}
				
				// Hae offsetPisteEdellinen
				if (coordSegmPisteEdellinen!=null){
					LineSegment segmEdellinen = new LineSegment(coordSegmPisteEdellinen, coordSiirrettavaPiste);
					coordOffsetPisteEdellinen = segmEdellinen.pointAlongOffset(1, offset); //segmentin loppupää
				}
				
				// Hae offsetPisteSeuraava
				if (coordSegmPisteSeuraava!=null){
					LineSegment segmSeuraava= new LineSegment(coordSiirrettavaPiste, coordSegmPisteSeuraava);
					coordOffsetPisteSeuraava = segmSeuraava.pointAlongOffset(0, offset); //segmentin alkupää
				}
				
				// Jos offsetPisteEdellinen == null && offsetPisteSeuraava != null 
				//		eka piste, käytetään vain seuraavan segmentin kautta saatu piste
				if(coordSegmPisteEdellinen==null && coordSegmPisteSeuraava!=null){
					coordSiirrettyPiste=coordOffsetPisteSeuraava;
				}
	
				// Jos offsetPisteEdellinen != null && offsetPisteSeuraava == null 
				//		vika piste, käytetään vain edellisen segmentin kautta saatu piste
				else if(coordOffsetPisteEdellinen!=null && coordOffsetPisteSeuraava==null){
					coordSiirrettyPiste=coordOffsetPisteEdellinen;
				}
				
				// Jos offsetPisteEdellinen != null && offsetPisteSeuraava != null 
				//		lasketaan piste näiden kahden perusteella
				else if(coordSegmPisteEdellinen!=null && coordSegmPisteSeuraava!=null){
					// 		keskiarvosegmentti ( edellinenPiste, seuraavaPiste )
					LineSegment keskiarvosegm = new LineSegment(coordOffsetPisteEdellinen, coordOffsetPisteSeuraava);
					//		haetaan puoliväli keskiarvosegmentiltä 
					//			keskiarvopiste = keskiarvosegmentti.pointalong(0,5)
					Coordinate coordKeskiarvopiste = keskiarvosegm.pointAlong(0.5);
					//		Lasketaan keskiarvopisteen vektori alkuperäisestä pisteestä
					LineSegment oikeaSuunta = new LineSegment(coordSiirrettavaPiste, coordKeskiarvopiste);
					if(oikeaSuunta.getLength()>0){
						double x = coordSiirrettavaPiste.x + (coordKeskiarvopiste.x - coordSiirrettavaPiste.x) / oikeaSuunta.getLength() * Math.abs(offset);
						double y = coordSiirrettavaPiste.y + (coordKeskiarvopiste.y - coordSiirrettavaPiste.y) / oikeaSuunta.getLength() * Math.abs(offset);
						coordSiirrettyPiste=new Coordinate(x, y);
					}
				}
				else{
					coordSiirrettyPiste=coordSiirrettavaPiste;
				}
				hashTableOffsetKoordinaatit.put(n, coordSiirrettyPiste);
			}
			// Siirrä läpikäydyn geometrian taitepisteet
			for(int p=0; p<koordinaattiLista.length;p++){
				Coordinate coordOffset=(Coordinate)hashTableOffsetKoordinaatit.get(p);
				koordinaattiLista[p].x=coordOffset.x;
				koordinaattiLista[p].y=coordOffset.y;
			}
			
		}
		
		return mls;
	}
	
	
	private boolean isValidAjorata(int requestedAjorata, int ajorata) {
		
		if (settingsService.getAjorataSpesifiPaikannus()) {
			if (requestedAjorata == 0 && ajorata == 0)
				return true;
			if (requestedAjorata == 1 && ajorata == 1)
				return true;
			if (requestedAjorata == 2 && ajorata == 2)
				return true;
			return false;
		}
		else {
			// logic for requested ajorata:
			// 0 = 0 & 1
			// 1 = 0 & 1
			// 2 = 0 & 2
		
			// always return 0 ajorata
			if (ajorata == 0)
				return true;
			if (requestedAjorata == 0 && ajorata == 1)
				return true;
			if (requestedAjorata == 1 && ajorata == 1)
				return true;
			if (requestedAjorata == 2 && ajorata == 2)
				return true;
			return false;
		}
	}
	
	
	/**
	 * Searches M-value from a line.
	 * 
	 * @param line
	 * @param measure
	 * @return true if found, false if not.
	 * @throws MGeometryException
	 */
	private boolean isMeasureInLine(List<MLineString> line, double measure) throws MGeometryException {
		for (MLineString mLineString : line) {
			if (mLineString.getCoordinateAtM(measure) != null)
				return true;
		}
		return false;
	}

	@Override
	public TatuPointGeometry getTieosoiteAsPoint(int tie, int osa, double et,
			Integer ajorata) throws ParseException {
		return getTieosoiteAsPoint(tie, osa, et, ajorata, 0);

	}
	
	//Uusi versio jossa offset
	@Override
	public TatuPointGeometry getTieosoiteAsPoint(int tie, int osa, double et,
			Integer ajorata, int offset) throws ParseException {

        List<Tieosa> tieosoitevali = getTieosoitevali(tie);
        if (tieosoitevali == null || tieosoitevali.size() == 0)
        	return new TatuPointGeometry(null, Error.TIE_NOT_FOUND);
        
        GeometryFactory gf = new GeometryFactory();

        for (Tieosa tieosa : tieosoitevali) {
			if (tieosa.getTie() == tie  && tieosa.getOsa() == osa && tieosa.getAjorata() == ajorata && tieosa.getVuosi() == settingsService.getVuosi()) {

				Point point = null;
				try {
					point = tieosa.getPoint(et, gf, offset); 
				} catch (MGeometryException e) {
					logger.error("Pistettä ei löytynyt: tie=" 
							+tie +", osa=" +osa +", ajorata=" +ajorata +", et=" +et);
					return new TatuPointGeometry(null, Error.FROM_NOT_FOUND);
					
				}
				if (point != null && !point.isEmpty()) {
					return new TatuPointGeometry(point, Error.OK);
				} else {
					logger.error("Pistettä ei löytynyt: tie=" 
							+tie +", osa=" +osa +", ajorata=" +ajorata +", et=" +et);
			        return new TatuPointGeometry(null, Error.FROM_NOT_FOUND);
				}
			}
		}
        
        return new TatuPointGeometry(null, Error.AOSA_NOT_FOUND);
	}

	private List<Tieosa> getTieosoitevali(int tie) {
		List<Tieosa> tieosoitevali;
		if (tie >= lastTieFrom && tie <= lastTieTo)
        	tieosoitevali = lastTieosoitevali;
        else {
        	int numLen = Integer.toString(tie).length();
        	if (numLen == 1) {
            	lastTieFrom = tie;
        		lastTieTo = tie;
        	} else if (numLen == 2) {
            	lastTieFrom = tie/10*10;
        		lastTieTo = (tie/10*10) + 9;
        	} else if (numLen == 3) {
            	lastTieFrom = tie/100*100;
        		lastTieTo = (tie/100*100) + 99;
        	} else {
            	lastTieFrom = tie/1000*1000;
        		lastTieTo = (tie/1000*1000) + 999;
        	}
        	
        	tieosoitevali = getTieosoitevali(lastTieFrom, lastTieTo);

        	lastTieosoitevali = tieosoitevali;
        }

		return tieosoitevali;
	}
}
