package jie.atf.core.service.tx;

import jie.atf.core.utils.exception.AtfException;

public interface IAtfTransaction {
	/**
	 * lock
	 * 
	 * @param key
	 * @param value
	 * @param seconds
	 *            expire time
	 * @throws AtfException
	 */
	void lock(String key, String value, Long seconds) throws AtfException;

	/**
	 * unlock
	 * 
	 * @param key
	 */
	void unlock(String key) throws AtfException;

}
