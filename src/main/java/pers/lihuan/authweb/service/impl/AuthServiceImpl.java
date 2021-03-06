package pers.lihuan.authweb.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pers.lihuan.authweb.dao.PermissionMapper;
import pers.lihuan.authweb.dao.RolePermissionMapper;
import pers.lihuan.authweb.model.MenuTree;
import pers.lihuan.authweb.model.Permission;
import pers.lihuan.authweb.model.PermissionExample;
import pers.lihuan.authweb.model.RolePermission;
import pers.lihuan.authweb.model.RolePermissionExample;
import pers.lihuan.authweb.service.AuthService;
import pers.lihuan.authweb.utils.UUIDUtil;

/**
 * 管理角色权限
 * @author Fancy
 */
@Service(value="authService")
public class AuthServiceImpl implements AuthService {
	@Autowired
	private RolePermissionMapper rolePermissionMapper;
	@Autowired
	private PermissionMapper permissionMapper;
	
	@Override
	public List<MenuTree> getPermissionTree(String roleId){
		
		List<MenuTree> listMenuTree = new ArrayList<MenuTree>();
		
		PermissionExample permissionExample = new PermissionExample();
		permissionExample.setOrderByClause("order_number ASC");
		PermissionExample.Criteria permissionCriteria = permissionExample.createCriteria();
		permissionCriteria.andIsDeleteEqualTo(0);
		List<Permission> allPermissions = permissionMapper.selectByExample(permissionExample);
		
		RolePermissionExample rolePermissionExample = new RolePermissionExample();
		RolePermissionExample.Criteria rolePermissionCriteria = rolePermissionExample.createCriteria();
		rolePermissionCriteria.andRoleIdEqualTo(roleId);
		List<RolePermission> rolePermissions = rolePermissionMapper.selectByExample(rolePermissionExample);
		
		for(Permission one : allPermissions){
			MenuTree menuTree = new MenuTree();
			menuTree.setId(one.getPermissionId());
			menuTree.setName(one.getPermissionName());
			menuTree.setpId(one.getParentId());
			menuTree.setOpen(true);
			menuTree.setChecked(false);
			for(RolePermission temp : rolePermissions){
				if(temp.getPermissionId().equals(one.getPermissionId())){
					menuTree.setChecked(true);
					break;
				}
			}
			listMenuTree.add(menuTree);
		}
		return listMenuTree;
	}

	@Override
	public boolean updateRolePermission(String roleId, List<String> authIds) {
		RolePermissionExample example = new RolePermissionExample();
		RolePermissionExample.Criteria criteria = example.createCriteria();
		criteria.andRoleIdEqualTo(roleId);
		rolePermissionMapper.deleteByExample(example);
		if(authIds != null && authIds.size()>0){
			for(String authId : authIds){
				RolePermission record = new RolePermission();
				record.setId(UUIDUtil.randomUUID8());
				record.setRoleId(roleId);
				record.setPermissionId(authId);
				rolePermissionMapper.insert(record);
			}
		}
		return true;
	}
}
