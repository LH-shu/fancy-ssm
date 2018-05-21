package pers.myAuthweb.common.authz;

import java.util.Set;

/**
 * User授权接口
 */
public abstract class IUserRealm {

	/**
	 * 获取用户角色
	 * 
	 * @param userId
	 * @return
	 */
	public abstract Set<String> getUserRoles(String userId);

	/**
	 * 获取用户权限
	 * 
	 * @param userId
	 * @return
	 */
	public abstract Set<String> getUserPermissions(String userId);

	/**
	 * 是否是单账号登录
	 * 
	 * @return
	 */
	public boolean isSingleUser() {
		return false;
	}
}
