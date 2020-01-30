package com.epa.util;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.context.internal.ManagedSessionContext;
import org.hibernate.resource.transaction.spi.TransactionStatus;

public class SessionManager {
	public org.hibernate.Session createNewSession() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		session.setFlushMode(FlushMode.MANUAL);
		ManagedSessionContext.bind(session);
		return (org.hibernate.Session) session;
	}

	/**
	 * Start a new Transaction in the given session
	 * 
	 * @param session The session to create the transaction in
	 */
	public void startNewTransaction(Session session) {
		session.beginTransaction();
	}

	/**
	 * Shortcut method that creates a new session and begins a transaction in it
	 * 
	 * @return A new session with a transaction started
	 */
	public org.hibernate.Session createNewSessionAndTransaction() {
		Session session = createNewSession();
		startNewTransaction(session);
		return session;
	}

	/**
	 * Commit the transaction within the given session. This method unbinds the
	 * session from the session context (ManagedSessionContext), flushes the
	 * session, commmits the session and then closes the session
	 * 
	 * @param session The session with the transaction to commit
	 */
	public void commitTransaction(Session session) {
		ManagedSessionContext.unbind(HibernateUtil.getSessionFactory());
		session.flush();
		if (!session.getTransaction().getStatus().equals(TransactionStatus.ACTIVE)) {
			session.getTransaction().commit();
		}
		session.close();
	}
}
