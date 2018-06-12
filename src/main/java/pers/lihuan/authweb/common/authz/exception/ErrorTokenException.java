package pers.lihuan.authweb.common.authz.exception;

/**
 * token验证失败
 */
public class ErrorTokenException extends EtpException {
	private static final long serialVersionUID = -2283411683871567063L;

	public ErrorTokenException() {
		super(401, "身份验证失败");
	}
}
