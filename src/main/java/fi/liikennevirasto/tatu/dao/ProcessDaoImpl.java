package fi.liikennevirasto.tatu.dao;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import fi.liikennevirasto.tatu.model.Process;

@Repository("ProcessDao")
@Transactional(readOnly=false)
public class ProcessDaoImpl extends BaseDaoImpl implements ProcessDao {

	Logger logger = Logger.getLogger(ProcessDaoImpl.class);
	
	@Override
	public void createProcess(Process process) {
		process.setTimestamp(new Date(System.currentTimeMillis()));
		Session session = sessionFactory.getCurrentSession();
		session.save(process);
	}

	@Override
	public void updateProcess(Process process) {
		if (process == null || process.getUuid() == null) {
			logger.error("process == null || process.getUuid() == null");
			return;
		}
		process.setTimestamp(new Date(System.currentTimeMillis()));
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(process);
	}

	@Override
	public Process getProcess(String uuid) {
		return (Process) findByUniqueField(Process.class, "uuid", uuid);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Process> getProcessesWithStatus(int status) {
		DetachedCriteria criteria = DetachedCriteria.forClass(Process.class);
		criteria.add(Restrictions.eq("status", status));
		criteria.addOrder(Order.asc("timestamp"));
		
		Criteria ec = criteria.getExecutableCriteria(sessionFactory.getCurrentSession());
		ec.setMaxResults(1);
		return ec.list();
	}

	@Override
	public Process getOldestProcess() {
		DetachedCriteria criteria = DetachedCriteria.forClass(Process.class);
		criteria.addOrder(Order.asc("timestamp"));
		
		Criteria ec = criteria.getExecutableCriteria(sessionFactory.getCurrentSession());
		ec.setMaxResults(1);
		@SuppressWarnings("rawtypes")
		List results = ec.list();
		if (results.size() > 0) {
			return (Process) results.get(0);
		}
		return null;
	}

	@Override
	public void deleteProcess(Process process) {
		if (process != null) {
			Session session = sessionFactory.getCurrentSession();
			session.delete(process);
		}
	}
}
