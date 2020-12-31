package fi.liikennevirasto.tatu;

import java.io.File;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("userFiles")
@Scope("session")
public class UserFilesImpl implements UserFiles {

	private List<KuvDefinition> kuvLines;
	private File zipFile;
	
	@Override
	public List<KuvDefinition> getKuvLines() {
		return kuvLines;
	}

	@Override
	public void setKuvLines(List<KuvDefinition> kuvLines) {
		this.kuvLines = kuvLines;
	}

	@Override
	public File getZipFile() {
		return zipFile;
	}

	@Override
	public void setZipFile(File zipFile) {
		this.zipFile = zipFile;
	}
	
}
