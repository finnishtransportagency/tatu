package fi.liikennevirasto.tatu;

import java.io.File;
import java.util.List;

public interface UserFiles {

	public void setZipFile(File zipFile);
	public File getZipFile();
	
	public void setKuvLines(List<KuvDefinition> kuvLines);
	public List<KuvDefinition> getKuvLines();

}
