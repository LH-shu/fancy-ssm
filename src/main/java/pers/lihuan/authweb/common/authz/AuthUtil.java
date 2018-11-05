package pers.lihuan.authweb.common.authz;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import pers.lihuan.authweb.common.authz.annotation.Logical;
import pers.lihuan.authweb.common.authz.exception.ErrorTokenException;
import pers.lihuan.authweb.common.authz.exception.ExpiredTokenException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;


/**
 * 权限检查工具类
 * @author Fancy
 */
public class AuthUtil {

	// 权限缓存的key前缀
	private static final String KEY_PRE_PS = "etpps-";
	// 角色缓存的key前缀
	private static final String KEY_PRE_RS = "etprs-";
	// token缓存的key前缀
	private static final String KEY_PRE_TOKEN = "etp-";
	private static volatile AuthUtil instance;
	private static IUserRealm userRealm;
	private static IEtpCache cache;
	private static String tokenKey = "e-t-p";

	private AuthUtil() {
	}

	public static AuthUtil getInstance() {
		if (instance == null) {
			synchronized (AuthUtil.class) {
				if (instance == null) {
					instance = new AuthUtil();
				}
			}
		}
		return instance;
	}

	protected void setUserRealm(IUserRealm userRealm) {
		AuthUtil.userRealm = userRealm;
	}

	public IUserRealm getUserRealm() {
		return AuthUtil.userRealm;
	}

	protected void setTokenKey(String tokenKey) {
		AuthUtil.tokenKey = tokenKey;
	}

	public String getTokenKey() {
		return AuthUtil.tokenKey;
	}

	protected void setCache(IEtpCache cache) {
		AuthUtil.cache = cache;
	}

	public IEtpCache getCache() {
		return AuthUtil.cache;
	}

	/**
	 * 检查是否有指定角色
	 * 
	 * @param roles
	 * @param logical
	 * @return
	 */
	public boolean hasRole(String userId, String[] roles, Logical logical) {
		checkUserRealm();
		boolean result = false;
		List<String> cacheRoles = getUserRoles(userId);
		for (int i = 0; i < roles.length; i++) {
			result = cacheRoles.contains(roles[i]);
			if (logical == (result ? Logical.OR : Logical.AND)) {
				break;
			}
		}
		return result;
	}

	public boolean hasRole(String userId, String roles) {
		return hasRole(userId, new String[] { roles }, Logical.OR);
	}

	/**
	 * 检查是否有指定权限
	 * 
	 * @param roles
	 * @param logical
	 * @return
	 */
	public boolean hasPermission(String userId, String[] permissions,
			Logical logical) {
		checkUserRealm();
		boolean result = false;
		List<String> cachePermissions = getUserPermissions(userId);
		for (int i = 0; i < permissions.length; i++) {
			result = cachePermissions.contains(permissions[i]);
			if (logical == (result ? Logical.OR : Logical.AND)) {
				break;
			}
		}
		return result;
	}

	public boolean hasPermission(String userId, String permissions) {
		return hasPermission(userId, new String[] { permissions }, Logical.OR);
	}
	
	/**
	 * 获取用户的角色
	 * @param userId
	 * @return
	 */
	public List<String> getUserRoles(String userId){
		List<String> cacheRoles = cache.getSet(KEY_PRE_RS + userId);
		if (cacheRoles == null || cacheRoles.size()==0) {
			cacheRoles = new ArrayList<String>();
			Set<String> userRoles = userRealm.getUserRoles(userId);
			if (userRoles != null && userRoles.size()>0) {
				cacheRoles.addAll(userRoles);
				cache.putSet(KEY_PRE_RS + userId, userRoles);
			}
		}
		return cacheRoles;
	}
	
	/**
	 * 获取用户的权限
	 * @param userId
	 * @return
	 */
	public List<String> getUserPermissions(String userId){
		List<String> cachePermissions = cache.getSet(KEY_PRE_PS + userId);
		if (cachePermissions == null || cachePermissions.size()==0) {
			cachePermissions = new ArrayList<String>();
			Set<String> userPermissions = userRealm.getUserPermissions(userId);
			if (userPermissions != null && userPermissions.size()>0) {
				cachePermissions.addAll(userPermissions);
				cache.putSet(KEY_PRE_PS + userId, userPermissions);
			}
		}
		return cachePermissions;
	}

	/**
	 * 更新user的权限缓存
	 * 
	 * @param userId
	 * @return
	 */
	public boolean updateCachePermission(String userId) {
		checkUserRealm();
		return cache.delete(KEY_PRE_PS + userId);
	}
	
	public boolean updateCachePermission(){
		checkUserRealm();
		return cache.delete(cache.keys(KEY_PRE_PS+"*"));
	}

	/**
	 * 更新user的角色缓存
	 * 
	 * @param userId
	 * @return
	 */
	public boolean updateCacheRoles(String userId) {
		checkUserRealm();
		return cache.delete(KEY_PRE_RS + userId);
	}
	
	public boolean updateCacheRoles(){
		checkUserRealm();
		return cache.delete(cache.keys(KEY_PRE_RS+"*"));
	}

	/**
	 * 检查token是否有效
	 * 
	 * @param userId
	 * @param token
	 * @return
	 */
	protected boolean isValidToken(String userId, String token) {
		checkUserRealm();
		List<String> tokens = cache.getSet(KEY_PRE_TOKEN + userId);
		return tokens != null && tokens.contains(token);
	}

	/**
	 * 缓存token
	 * 
	 * @param userId
	 * @param token
	 */
	private boolean setCacheToken(String userId, String token) {
		checkUserRealm();
		if (userRealm.isSingleUser()) {
			cache.delete(KEY_PRE_TOKEN + userId);
		}
		Set<String> tokens = new HashSet<String>();
		tokens.add(token);
		return cache.putSet(KEY_PRE_TOKEN + userId, tokens);
	}

	/**
	 * 主动让user的所有token失效
	 * 
	 * @param userId
	 * @return
	 */
	public boolean expireToken(String userId) {
		return cache.delete(KEY_PRE_TOKEN + userId);
	}

	/**
	 * 移除user的某一个token
	 * 
	 * @param userId
	 * @param token
	 * @return
	 */
	public boolean expireToken(String userId, String token) {
		return cache.removeSet(KEY_PRE_TOKEN + userId, token);
	}
	
	/**
	 * 移除过期的token
	 * @param token
	 * @return
	 */
	public boolean expireBadToken(String token) {
		Set<String> keys = cache.keys(KEY_PRE_TOKEN + "*");
		for(String keyOne : keys){
			List<String> tokens = cache.getSet(keyOne);
			if(tokens.contains(token)){
				return cache.removeSet(keyOne, token);
			}
		}
		return false;
	}

	/**
	 * 检查userRealm是否注入
	 */
	private void checkUserRealm() {
		if (userRealm == null) {
			throw new NullPointerException("userRealm is null");
		}
	}

	/**
	 * 创建token
	 * 
	 * @param userId
	 * @param ttlMillis
	 * @return
	 */
	public String createToken(String userId, Date expireDate) {
		String token = TokenUtil.createToken(userId, tokenKey, expireDate);
		setCacheToken(userId, token);
		return token;
	}

	/**
	 * 解析token
	 * 
	 * @param token
	 * @return
	 * @throws Exception
	 */
	protected Claims parseToken(String token) throws Exception {
		try {
			Claims claims = TokenUtil.parseToken(token, tokenKey);
			// 校验服务器是否存在token
			if (!isValidToken(claims.getSubject(), token)) {
				throw new ExpiredTokenException();
			}
			return claims;
		} catch (ExpiredJwtException e) {  //token过期
			expireBadToken(token);
			throw new ExpiredTokenException();
		} catch (Exception e) {  //token解析失败
			e.printStackTrace();
			throw new ErrorTokenException();
		}
	}
	
	/**
	 * 解析token的载体subject
	 * @param token
	 * @return
	 */
	public String getTokenSubject(String token) {
		try {
			return parseToken(token).getSubject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 从request中获取token
	 * @param request
	 * @return
	 */
	public String getRequestToken(HttpServletRequest request){
		String token = request.getHeader("token");
		if (token == null) {
			token = request.getParameter("token");
		}
		return token;
	}
}
