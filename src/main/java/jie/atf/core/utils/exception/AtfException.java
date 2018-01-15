package jie.atf.core.utils.exception;

/**
 * ATF异常
 * 
 * @author Jie
 *
 */
public class AtfException extends Exception {
	public AtfException(String msg) {
		super(msg);
	}

	public AtfException(Throwable e) {
		super(e);
	}
}
