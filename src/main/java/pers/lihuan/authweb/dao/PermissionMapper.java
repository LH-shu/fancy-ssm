package pers.lihuan.authweb.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import pers.lihuan.authweb.model.Permission;
import pers.lihuan.authweb.model.PermissionExample;

/**
 * @author Administrator
 */
public interface PermissionMapper {

	List<Permission> selectPermissionByRoleId(String roleId);
	List<Permission> selectPermissionByUserId(String userId);
	
	List<Permission> selectPermissions(@Param("searchKey") String searchKey, @Param("searchValue") String searchValue);

	List<Permission> selectByExample(PermissionExample example);
	List<Permission> selectButtonParent();
	
	int insertSelective(Permission permission);


	/**以下是更新方法
	 *
	 * @param permission
	 * @return
	 */
	int updateByPrimaryKeySelective(Permission permission);//更新Permission

	/**
	 * 以下是删除方法
	 *
	 * @param permissionId
	 * @return
	 */
	int deleteByPrimaryKey(String permissionId);
	
}