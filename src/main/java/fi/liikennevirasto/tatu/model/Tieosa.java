package fi.liikennevirasto.tatu.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.spatial.jts.mgeom.MCoordinateSequence;
import org.hibernate.spatial.jts.mgeom.MGeometryException;
import org.hibernate.spatial.jts.mgeom.MGeometryFactory;
import org.hibernate.spatial.jts.mgeom.MLineString;
import org.hibernate.spatial.jts.mgeom.MultiMLineString;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.Point;

@Entity
@Table(name = "ptk.osoite3_tatu")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class Tieosa implements Serializable {

	private static final long serialVersionUID = -962858335068604880L;

	private static final Logger logger = Logger.getLogger(Tieosa.class);
	
	@Id
    @Column(name = "objectid", unique = true, nullable = false)
    private String objectId;

	@Type(type = "org.hibernate.spatial.GeometryType")
	@Column(name="SHAPE", nullable = false)
	private Geometry shape;
    
	@Column(name = "tie", unique = false, nullable = true)
    private Integer tie;

	@Column(name = "osa", unique = false, nullable = true)
    private Integer osa;

	@Column(name = "ajorata", unique = false, nullable = true)
    private Integer ajorata;

	@Column(name = "tiepiiri", unique = false, nullable = true)
    private Integer tiepiiri;

	@Column(name = "osoite3", unique = false, nullable = true)
    private Integer routeId;

    @Column(name = "tr_pituus", unique = false, nullable = true)
    private Integer trPituus;
    
    // Erottaa eri vuosien tieosoiteverkot toisistaan
    @Column(name = "vuosi", unique = false, nullable = true)
    private Integer vuosi;
	
    public String getObjectId() {
		return objectId;
	}

	public Geometry getShape() {
		return shape;
	}
	
	public List<MLineString> getLines(double fromM, double toM, MGeometryFactory gf) throws MGeometryException {

		if (fromM == toM) {
			return null;
		}
		
		Geometry geometry = (Geometry) shape.clone();
		
		ArrayList<MLineString> lineStrings = new ArrayList<MLineString>();

		if (geometry instanceof MLineString) {
			MLineString linePart = getLinePart((MLineString) geometry, fromM, toM, gf);
			if (linePart != null && !linePart.isEmpty()) {
				lineStrings.add(linePart);
			}
		} else if (geometry instanceof MultiMLineString) {
			MultiMLineString mline = (MultiMLineString) geometry;
			for (int i = 0; i < mline.getNumGeometries(); i++) {
				MLineString mLineString = (MLineString) mline.getGeometryN(i);
				MLineString linePart = getLinePart(mLineString, fromM, toM, gf);
				if (linePart != null && !linePart.isEmpty()) {
					lineStrings.add(linePart);
				}
			}
		} else {
			logger.error("unknown type: " +geometry.toString());
		}

		return lineStrings;
	}
	/**
	 * Hakee pisteen tieosalta
	 * @param m Et�isyys tieosan alusta
	 * @param gf GeometryFactory
	 * @return Point
	 * @throws MGeometryException
	 */
	public Point getPoint(double m, GeometryFactory gf) throws MGeometryException {
		return getPoint(m, gf, 0);
	}
	
	/**
	 * Hakee pisteen tieosalta
	 * @param m Et�isyys tieosan alusta
	 * @param gf GeometryFactory
	 * @param offset Sivuttaissiirtym� palautettavalle pisteelle
	 * @return Point
	 * @throws MGeometryException
	 */
	public Point getPoint(double m, GeometryFactory gf, double offset) throws MGeometryException {
		Geometry geometry = (Geometry) shape.clone();
		
		if (geometry instanceof MLineString) {
			//return  gf.createPoint(((MLineString) geometry).getCoordinateAtM(m));
			return gf.createPoint(haePisteOffset(((MLineString) geometry), m, offset));
		} else if (geometry instanceof MultiMLineString) {
			MultiMLineString mline = (MultiMLineString) geometry;
			for (int i = 0; i < mline.getNumGeometries(); i++) {
				Coordinate coordinate = ((MLineString) mline.getGeometryN(i)).getCoordinateAtM(m);
				if (coordinate != null){
					
					if (offset!=0){
						return gf.createPoint(haePisteOffset(((MLineString) mline.getGeometryN(i) /*geometry*/), m, offset));
					}else{
						return gf.createPoint(coordinate);
					}
				}
			}
		} else {
			logger.error("unknown type: " +geometry.toString());
		}
		return null;
	}

	/**
	 * Hakee pisteen MLineStringilt� m-arvon perusteella
	 * @param mLine Segmentti jolta piste haetaan
	 * @param m Mittaluku jolla haetaan
	 * @param offset Sivuttaissiirtym� palautettavalle pisteelle
	 * @return Coordinate
	 */
	public Coordinate haePisteOffset(MLineString mLine, double m, double offset){
		Coordinate[] koordinaattiLista = mLine.getCoordinates();
		
		//aloitus toisesta taitepisteest� ett� saadaan heti eka segmentti (0 ja 1)
		for(int n=1; n<koordinaattiLista.length; n++){
			Double mAtN = mLine.getMatN(n);
			Double mAtN1 = mLine.getMatN(n-1);
			LineSegment lineSegm = new LineSegment(koordinaattiLista[n-1], koordinaattiLista[n]);
			//Ohitetaan p��llek�iset pisteet, koska kahdesta samasta pisteest� ei synny kelvollista lineSegmentti�
			if (lineSegm.getLength()>0){
				
				if ( (mAtN1<=m && m<=mAtN)){
					//l�ytyi, m-arvot kasvavassa j�rjestyksess�
					double fragment = (m-mAtN1)/(mAtN-mAtN1);
					return lineSegm.pointAlongOffset(fragment, offset);
					//return pointAlongOffset(lineSegm, fragment, offset); //k�ytet��n JTS:st� t�nne kopioitua toiminnallisuutta
				}else if(mAtN<=m && m<=mAtN1){
					//l�ytyi, m-arvot pienenev�ss� j�rjestyksess�
					double fragment = 1-((m-mAtN)/(mAtN1-mAtN));
					return lineSegm.pointAlongOffset(fragment, (-1*offset)); 
					//return pointAlongOffset(lineSegm, fragment, (-1*offset)); //k�ytet��n JTS:st� t�nne kopioitua toiminnallisuutta
				}
			}
		}
		return null; //Jos ei l�ydy mit��n	
	}
	
	public Integer getRouteId() {
		return routeId;
	}

	public Integer getTrPituus() {
		return trPituus;
	}

	public Integer getTie() {
		return tie;
	}

	public Integer getOsa() {
		return osa;
	}

	public Integer getAjorata() {
		return ajorata;
	}

	public Integer getTiepiiri() {
		return tiepiiri;
	}
	
	public Integer getVuosi() {
		return vuosi;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("tie = ");
		sb.append(tie);
		sb.append(", osa = ");
		sb.append(osa);
		sb.append(", ajorata = ");
		sb.append(ajorata);
		sb.append(", tiepiiri = ");
		sb.append(tiepiiri);
		sb.append(", routeId = ");
		sb.append(routeId);
		sb.append(", tiepiiri = ");
		sb.append(tiepiiri);
		sb.append(", trPituus = ");
		sb.append(trPituus);
		sb.append(", shape.isEmpty() = ");
		sb.append(shape.isEmpty());
		return sb.toString();
	}


	private MLineString getLinePart(MLineString line, double fromM, double toM, MGeometryFactory gf) throws MGeometryException {
		MCoordinateSequence[] part = (MCoordinateSequence[]) line.getCoordinatesBetween(fromM, toM);
		if (part[0].size() == 1)
			return null;
		if (part.length > 1)
			logger.error("more than 1 part");
		return gf.createMLineString(part[0]);
		
	}

}
