package fi.liikennevirasto.tatu;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FileDataStoreFactorySpi;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.hibernate.id.GUIDGenerator;
import org.hibernate.spatial.jts.mgeom.MGeometry;
import org.hibernate.spatial.jts.mgeom.MGeometryFactory;
import org.hibernate.spatial.jts.mgeom.MLineString;
import org.hibernate.spatial.jts.mgeom.MultiMLineString;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

import org.hibernate.spatial.jts.mgeom.MCoordinate;

import fi.liikennevirasto.tatu.TatuGeometry.Error;
import fi.liikennevirasto.tatu.service.OsoiteverkkoService;

public class ShapeWriter {
	
	private static final Logger logger = Logger.getLogger(ShapeWriter.class);

	public static Boolean boolMArvotMukaan = false;
	
	private static final String VIIVA_POSTFIX = "_viiva"; 
	private static final String PISTE_POSTFIX = "_piste"; 
	private static final String VIRHE_POSTFIX = "_virheet"; 
	private static final String EUREF_FIN_TM35FIN_PRJ = "PROJCS[\"EUREF_FIN_TM35FIN\",GEOGCS[\"GCS_EUREF_FIN\",DATUM[\"D_ETRS_1989\",SPHEROID[\"GRS_1980\",6378137.0,298.257222101]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"False_Easting\",500000.0],PARAMETER[\"False_Northing\",0.0],PARAMETER[\"Central_Meridian\",27.0],PARAMETER[\"Scale_Factor\",0.9996],PARAMETER[\"Latitude_Of_Origin\",0.0],UNIT[\"Meter\",1.0]]";
	private static final String M_POSTFIX = "_marvot";
	
	private String shapeFileName;
	private String originalFileName;
	
	private FeatureWriter<SimpleFeatureType, SimpleFeature> lineFeatWriter;
	private FeatureWriter<SimpleFeatureType, SimpleFeature> pointFeatWriter;
	private FeatureWriter<SimpleFeatureType, SimpleFeature> errorFeatWriter;
	private BufferedWriter errorWriter;
	
	private FeatureWriter<SimpleFeatureType, SimpleFeature> mPointFeatWriter;
	
	private boolean linesCreated;
	private boolean pointsCreated;
	
	private ReaderResponse readerResponse = new ReaderResponse(0, 0);
	
	private enum WriterType {
		POINT,
		LINE,
		ERROR,
		M;
	}
	
	public ShapeWriter(String shapeFileName, String originalFilename) throws IOException, SchemaException {
		this.shapeFileName = shapeFileName;
		this.originalFileName = originalFilename;
	}
	
	public void writeLine(OsoiteverkkoService osoiteverkkoService, List<KuvDefinition> kuvDefs, 
			TatuRow tatuRow) throws IOException, SchemaException, TatuException {
		
        TatuGeometry tatuGeometry = tatuRow.getTatuGeometry(osoiteverkkoService);
        
        WriterType writerType;
        if (tatuGeometry.getError() == Error.OK) {
        	if (tatuRow.isLine())
        		writerType = WriterType.LINE;
        	else
        		writerType = WriterType.POINT;
        } else {
        	writerType = WriterType.ERROR;
        }


    	String id = UUID.randomUUID().toString();

        FeatureWriter<SimpleFeatureType, SimpleFeature> fw = getWriter(kuvDefs, writerType);
        SimpleFeature sf = (SimpleFeature) fw.next();
    	sf.setAttribute(/*"the_geom"*/ "SHAPE", tatuGeometry.getGeometry());

    	ArrayList<Object> fieldValues = tatuRow.getFieldValues();
        for (int i = 0; i < fieldValues.size(); i++) {
        	//logger.debug(kuvDefs.get(i).getLyhenne()+" - "+fieldValues.get(i));
			sf.setAttribute(kuvDefs.get(i).getLyhenne(), fieldValues.get(i));
		}
      //ID-kenttä joka yhdistää m-arvopisteet ja viivat
        if(boolMArvotMukaan){
        	sf.setAttribute("ID", id);
        }
        try{
        	fw.write();
        }catch(Exception e){ //ajo ei kaadu, vaan virhe kulkeutuu lokiin.
        	logger.error("Tapahtui virhe rivin tallennuksessa, " + e.getMessage());
        	throw new TatuException("Rivin tallennus ei onnistunut. | " +  e.getMessage() + " | " + e.getCause());
        }
        // **************************************************************************************************
        // M-arvot piste-shapena, jokainen taitepiste shapeen ja attribuutiksi m-arvo (muiden viivan attribuuttien kanssa)
        // Jos writertype == error, ohitetaan koska geometriaakaan ei kirjoiteta shape-tiedostoon
        //
        if(boolMArvotMukaan && writerType != WriterType.ERROR){
	        Geometry geometry = (Geometry) tatuGeometry.getGeometry().clone();			
			ArrayList<MLineString> lineStrings = new ArrayList<MLineString>();
			if (geometry instanceof MLineString) {
				MLineString linePart = (MLineString) geometry;
				if (linePart != null && !linePart.isEmpty())
					lineStrings.add(linePart);
			} else if (geometry instanceof MultiMLineString) {
				MultiMLineString mline = (MultiMLineString) geometry;
				for (int i = 0; i < mline.getNumGeometries(); i++) {
					MLineString mLineString = (MLineString) mline.getGeometryN(i);
					MLineString linePart = mLineString;
					if (linePart != null && !linePart.isEmpty())
						lineStrings.add(linePart);
				}
			}
			List<KuvDefinition> kuvDefs2 = new ArrayList<KuvDefinition>();
	     	for(int q=0; q<kuvDefs.size();q++){
	     		kuvDefs2.add(kuvDefs.get(q));
	     	}
			try{
	     		kuvDefs2.add(new KuvDefinition("marvo", KuvDefinition.Tyyppi.Double.toString()));
	     	}catch(Exception e){
	     		String a = "virhe tapahtui"; 
	     	}
	        FeatureWriter<SimpleFeatureType, SimpleFeature> fm = getWriter(kuvDefs2, writerType.M);
	        
	        MLineString mline;
	        Coordinate[] coordColl;
	        for(int d=0; d<lineStrings.size();d++){
	        	mline= lineStrings.get(d);
	        	coordColl=mline.getCoordinates();
	        	
	        	for (int a=0; a<coordColl.length; a++){
	                SimpleFeature pmsf = (SimpleFeature) fm.next();
	              	
	                pmsf.setAttribute(/*"the_geom"*/ "SHAPE", mline.getPointN(a));
	             	
	             	for (int i = 0; i < fieldValues.size(); i++) {
	             		pmsf.setAttribute(kuvDefs.get(i).getLyhenne(), fieldValues.get(i));
	         		}
	             	pmsf.setAttribute("ID", id);
	             	pmsf.setAttribute("marvo", mline.getMatN(a));
	             	fm.write();
	             }
	        	
	        }
        }
        // M-arvot
        //
        // **************************************************************************************************
        
        
        readerResponse.addToTotalCount();

        if (tatuGeometry.getError() != Error.OK) {
        	writeErrorLine(tatuRow, tatuGeometry);
            readerResponse.addToErrorCount();
        }
	}

	private void writeErrorLine(TatuRow tatuRow, TatuGeometry tatuGeometry)
			throws IOException {

		if (errorWriter == null) {
			errorWriter = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(shapeFileName +"_virheet.txt")));
		}
		errorWriter.write(tatuGeometry.getError().toString());
		errorWriter.write(": ");
		errorWriter.write(tatuRow.toString());
		errorWriter.write("\r\n");
		// This doesn't work in windows systems!
		//errorWriter.newLine();
	}

	private void createPrjFile(String prjFileName) throws IOException {
		// Create a prj-file
		File prjFile = new File(prjFileName);
		BufferedWriter out = new BufferedWriter(new FileWriter(prjFile));
		out.write(EUREF_FIN_TM35FIN_PRJ);
		out.close();
	}
	
	public ReaderResponse createZipFile(String zipFileName) {
        try {
			if (lineFeatWriter != null) {
				lineFeatWriter.close();
				createPrjFile(shapeFileName +VIIVA_POSTFIX +".prj");
				lineFeatWriter = null;
			}
			if (pointFeatWriter != null) {
				pointFeatWriter.close();
				createPrjFile(shapeFileName +PISTE_POSTFIX +".prj");
				pointFeatWriter = null;
			}
			if (mPointFeatWriter != null) {
				mPointFeatWriter.close();
				createPrjFile(shapeFileName +M_POSTFIX +".prj");
				mPointFeatWriter = null;
			}
			if (errorFeatWriter != null) {
				errorFeatWriter.close();
//				createPrjFile(shapeFileName +VIRHE_POSTFIX +".prj");
				errorFeatWriter = null;
			}
			if (errorWriter != null) {
				errorWriter.close();
			}

    		// ZIP shape
    		File zipFile = new File(zipFileName);
    		List<File> files = new ArrayList<File>();
    		List<String> originalFilenames = new ArrayList<String>();
    		List<File> unusedFiles = new ArrayList<File>();
    		if (linesCreated) {
    			// Files to add
    			files.add(new File(shapeFileName +VIIVA_POSTFIX +".dbf"));
        		files.add(new File(shapeFileName +VIIVA_POSTFIX +".fix"));
        		files.add(new File(shapeFileName +VIIVA_POSTFIX +".prj"));
        		//files.add(new File(shapeFileName +VIIVA_POSTFIX +".qix"));
        		files.add(new File(shapeFileName +VIIVA_POSTFIX +".shp"));
        		files.add(new File(shapeFileName +VIIVA_POSTFIX +".shx"));
        		// Name of files in zip
        		originalFilenames.add(originalFileName +VIIVA_POSTFIX +".dbf");
        		originalFilenames.add(originalFileName +VIIVA_POSTFIX +".fix");
        		originalFilenames.add(originalFileName +VIIVA_POSTFIX +".prj");
        		//originalFilenames.add(originalFileName +VIIVA_POSTFIX +".qix");
        		originalFilenames.add(originalFileName +VIIVA_POSTFIX +".shp");
        		originalFilenames.add(originalFileName +VIIVA_POSTFIX +".shx");
    		}
    		if (pointsCreated) {
    			// Files to add
        		files.add(new File(shapeFileName +PISTE_POSTFIX +".dbf"));
        		files.add(new File(shapeFileName +PISTE_POSTFIX +".fix"));
        		files.add(new File(shapeFileName +PISTE_POSTFIX +".prj"));
        		//files.add(new File(shapeFileName +PISTE_POSTFIX +".qix"));
        		files.add(new File(shapeFileName +PISTE_POSTFIX +".shp"));
        		files.add(new File(shapeFileName +PISTE_POSTFIX +".shx"));
        		// Name of files in zip
        		originalFilenames.add(originalFileName +PISTE_POSTFIX +".dbf");
        		originalFilenames.add(originalFileName +PISTE_POSTFIX +".fix");
        		originalFilenames.add(originalFileName +PISTE_POSTFIX +".prj");
        		//originalFilenames.add(originalFileName +PISTE_POSTFIX +".qix");
        		originalFilenames.add(originalFileName +PISTE_POSTFIX +".shp");
        		originalFilenames.add(originalFileName +PISTE_POSTFIX +".shx");
    		}
    		//Tiedostot m-arvot sisältäville pisteille
    		if((boolMArvotMukaan && linesCreated) || (boolMArvotMukaan && pointsCreated)){
    			// Files to add
        		files.add(new File(shapeFileName +M_POSTFIX +".dbf"));
        		files.add(new File(shapeFileName +M_POSTFIX +".fix"));
        		files.add(new File(shapeFileName +M_POSTFIX +".prj"));
        		//files.add(new File(shapeFileName +PISTE_POSTFIX +".qix"));
        		files.add(new File(shapeFileName +M_POSTFIX +".shp"));
        		files.add(new File(shapeFileName +M_POSTFIX +".shx"));
        		// Name of files in zip
        		originalFilenames.add(originalFileName +M_POSTFIX +".dbf");
        		originalFilenames.add(originalFileName +M_POSTFIX +".fix");
        		originalFilenames.add(originalFileName +M_POSTFIX +".prj");
        		//originalFilenames.add(originalFileName +PISTE_POSTFIX +".qix");
        		originalFilenames.add(originalFileName +M_POSTFIX +".shp");
        		originalFilenames.add(originalFileName +M_POSTFIX +".shx");
    		}
    		if (readerResponse.getErrorRows() > 0) {
    			// virheet.txt
    			files.add(new File(shapeFileName +"_virheet.txt"));
        		originalFilenames.add(originalFileName +"_virheet.txt");
        		// virheelliset.dbf
        		files.add(new File(shapeFileName +VIRHE_POSTFIX +".dbf"));
        		originalFilenames.add(originalFileName +VIRHE_POSTFIX +".dbf");
        		// remove all other files
        		unusedFiles.add(new File(shapeFileName +VIRHE_POSTFIX +".dbf"));
        		unusedFiles.add(new File(shapeFileName +VIRHE_POSTFIX +".fix"));
        		unusedFiles.add(new File(shapeFileName +VIRHE_POSTFIX +".prj"));
        		//unusedFiles.add(new File(shapeFileName +VIRHE_POSTFIX +".qix"));
        		unusedFiles.add(new File(shapeFileName +VIRHE_POSTFIX +".shp"));
        		unusedFiles.add(new File(shapeFileName +VIRHE_POSTFIX +".shx"));
    		}
    		ZipWriter.createZipFile(zipFile, files, originalFilenames, unusedFiles);
    		
    		return readerResponse;
        
        } catch (IOException e) {
			logger.error(e.getMessage(), e);
	        return null;
		}
        
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (lineFeatWriter != null)
			lineFeatWriter.close();
		if (pointFeatWriter != null)
			pointFeatWriter.close();
		if (errorWriter != null)
			errorWriter.close();
	}
	
	private FeatureWriter<SimpleFeatureType, SimpleFeature> getWriter(List<KuvDefinition> kuvDefs, 
			WriterType writerType) throws IOException, SchemaException {

		switch (writerType) {
		case LINE:
			if (lineFeatWriter == null)
				lineFeatWriter = createWriter(kuvDefs, writerType);
			return lineFeatWriter;
		case POINT:
			if (pointFeatWriter == null)
				pointFeatWriter = createWriter(kuvDefs, writerType);
			return pointFeatWriter;
		case M:
			if (mPointFeatWriter == null)
				mPointFeatWriter = createWriter(kuvDefs, writerType);
			return mPointFeatWriter;
		case ERROR:
			if (errorFeatWriter == null)
				errorFeatWriter = createWriter(kuvDefs, writerType);
			return errorFeatWriter;
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private FeatureWriter<SimpleFeatureType, SimpleFeature> createWriter(List<KuvDefinition> kuvDefs, WriterType writerType) throws IOException, SchemaException {

		@SuppressWarnings("rawtypes")
		Map map = Collections.singletonMap("url", createUrl(writerType));

		FileDataStoreFactorySpi factory = new ShapefileDataStoreFactory();
		DataStore myData = factory.createNewDataStore(map);
		
		SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
		b.setName("test");
		
		switch (writerType) {
			case LINE:
				b.add("SHAPE", MultiLineString.class);
				linesCreated = true;
				break;
			case POINT:
				b.add("SHAPE", MultiPoint.class);
				pointsCreated = true;
				break;
			case M:
//				b.add("SHAPE", MultiPoint.class);
				b.add("SHAPE", Point.class);
				//pointsCreated = true;
				break;
			case ERROR:
				b.add("SHAPE", MultiPoint.class);
				break;
		}
		
		for (int i = 0; i < kuvDefs.size(); i++) {
			if (kuvDefs.get(i).getTyyppi() == KuvDefinition.Tyyppi.M || 
					kuvDefs.get(i).getTyyppi() == KuvDefinition.Tyyppi.D) {
				b.add(kuvDefs.get(i).getLyhenne(),String.class);
			} else if (kuvDefs.get(i).getTyyppi() == KuvDefinition.Tyyppi.J || 
					kuvDefs.get(i).getTyyppi() == KuvDefinition.Tyyppi.K) {
				b.add(kuvDefs.get(i).getLyhenne(),Integer.class);
			} else if (kuvDefs.get(i).getTyyppi() == KuvDefinition.Tyyppi.Double ) {
				AttributeTypeBuilder atb=new AttributeTypeBuilder();
				atb.setBinding(Double.class);
				AttributeDescriptor doubleDesc = atb.buildDescriptor(kuvDefs.get(i).getLyhenne());
				b.add(doubleDesc);
			}
		}
		
		SimpleFeatureType featureType = b.buildFeatureType();
		
		myData.createSchema(featureType);

		FeatureWriter<SimpleFeatureType, SimpleFeature> writer = myData.getFeatureWriterAppend(myData.getTypeNames()[0]/* "test"*/, Transaction.AUTO_COMMIT);
		return writer;
	}
	
	private URL createUrl(WriterType writerType) throws MalformedURLException {
		String url;
		if (writerType == WriterType.LINE)
			url = shapeFileName +VIIVA_POSTFIX +".shp";
		else if (writerType == WriterType.POINT)
			url = shapeFileName +PISTE_POSTFIX +".shp";
		else if (writerType == WriterType.M)
			url = shapeFileName +M_POSTFIX +".shp";
		else 
			url = shapeFileName +VIRHE_POSTFIX +".shp";
		return new File(url).toURI().toURL();
	}
	
}
