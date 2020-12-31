package fi.liikennevirasto.tatu.dao;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import fi.liikennevirasto.tatu.model.Settings;

@Repository("SettingsDao")
@Transactional(readOnly=true)
public class SettingsDaoImpl extends BaseDaoImpl implements SettingsDao {

	@Override
	public Settings getSettings() {
		@SuppressWarnings("rawtypes")
		List settings = sessionFactory.getCurrentSession().createCriteria(Settings.class).list();
		if (settings == null || settings.size() == 0) {
			return null;
		}
		return (Settings) settings.get(0);
	}

}
