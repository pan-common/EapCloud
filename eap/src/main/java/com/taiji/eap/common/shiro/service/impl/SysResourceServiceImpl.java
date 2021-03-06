package com.taiji.eap.common.shiro.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.taiji.eap.common.base.BaseServiceImpl;
import com.taiji.eap.common.generator.bean.EasyUISubmitData;
import com.taiji.eap.common.generator.bean.LayuiTree;
import com.taiji.eap.common.redis.dao.impl.RedisFactoryDao;
import com.taiji.eap.common.shiro.bean.*;
import com.taiji.eap.common.shiro.dao.*;
import com.taiji.eap.common.shiro.service.SysResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SysResourceServiceImpl extends BaseServiceImpl implements SysResourceService{

    private static final String REDIS_KEY_RESOURCE = "user:resource";

    private static final String REDIS_KEY_PURIEW = "user:puriew";

    @Autowired
    private SysResourceDao sysResourceDao;
    @Autowired
    private SysUserRoleDao sysUserRoleDao;
    @Autowired
    private SysUserOrganDao sysUserOrganDao;
    @Autowired
    private SysRoleResourceDao sysRoleResourceDao;//角色资源关系
    @Autowired
    private SysOrganResourceDao sysOrganResourceDao;//部门资源关系
    @Autowired
    private SysPuriewResourceDao sysPuriewResourceDao;//
    @Autowired
    private SysPuriewDao sysPuriewDao;
    @Autowired
    private RedisFactoryDao<LayuiTree> redisFactoryDao;

    @Transactional
    @Override
    public int deleteByPrimaryKey(Long primaryKey) {
        int k = 0;
        k+=sysResourceDao.deleteByPrimaryKey(primaryKey);
        k+=sysPuriewResourceDao.deleteByResourceId(primaryKey);
        recursiveDelete(primaryKey);
        if(k>0)
            redisFactoryDao.deleteByPattern(REDIS_KEY_RESOURCE+"*");
        redisFactoryDao.deleteByPattern(REDIS_KEY_PURIEW+"*");
        return k;
    }


    @Transactional
    @Override
    public int insert(SysResource sysResource) {
        int k = 0;
        SysPuriew sysPuriew = new SysPuriew();
        sysPuriew.setName(sysResource.getName()+"权限");
        sysPuriew.setExpression(sysResource.getLink());
        sysPuriew.setSeq(1);
        sysPuriew.setCreateTime(new Date());
        sysPuriew.setUpdateTime(new Date());
        sysPuriew.setCreater(0L);
        sysPuriew.setValid("1");
        k+=sysPuriewDao.insert(sysPuriew);
        k+=sysResourceDao.insert(sysResource);
        SysPuriewResource sysPuriewResource = new SysPuriewResource();
        sysPuriewResource.setPuriewId(sysPuriew.getPuriewId());
        sysPuriewResource.setResourceId(sysResource.getResourceId());
        k+=sysPuriewResourceDao.insert(sysPuriewResource);
        return k;
    }

    @Override
    public SysResource selectByPrimaryKey(Long primaryKey) {
        return sysResourceDao.selectByPrimaryKey(primaryKey);
    }

    @Transactional
    @Override
    public int updateByPrimaryKey(SysResource sysResource) {
        int k = sysResourceDao.updateByPrimaryKey(sysResource);
        if(k>0) {
            redisFactoryDao.deleteByPattern(REDIS_KEY_RESOURCE + "*");
            redisFactoryDao.deleteByPattern(REDIS_KEY_PURIEW + "*");
        }
        return k;
    }

    @Override
    public List<SysResource> list(String searchText) {
        return sysResourceDao.list(searchText);
    }

    @Override
    public PageInfo<SysResource> list(int pageNum, int pageSize, String searchText) throws Exception {
        PageHelper.startPage(pageNum,pageSize);
        List<SysResource> sysResources = sysResourceDao.list(searchText);
        PageInfo<SysResource> pageInfo = new PageInfo<SysResource>(sysResources);
        return pageInfo;
    }

    @Override
    public List<SysResource> listByPid(Long parentId) throws Exception {
        return sysResourceDao.listByPid(parentId,"");
    }

    @Override
    public PageInfo<SysResource> listByPid(Long parentId, int pageNum, int pageSize, String searchText) throws Exception {
        PageHelper.startPage(pageNum, pageSize);
        List<SysResource> list = sysResourceDao.listByPid(parentId,searchText);
        PageInfo<SysResource> pageInfo = new PageInfo<SysResource>(list);
        return pageInfo;
    }

    @Override
    public List<SysResource> getPath(Long resourceId) throws Exception {
        List<SysResource> list = new ArrayList<SysResource>();
        if(resourceId!=0){
            disPlay(resourceId, list);
        }
        list.add(new SysResource(0L,"根路径"));
        Collections.reverse(list);
        return list;
    }

    @Override
    public List<LayuiTree> treeView(Long parentId) throws Exception {
        List<SysResource> list = sysResourceDao.selectAll();
        List<LayuiTree> trees = new ArrayList<>();
        for (SysResource tree: list) {
            if(parentId==tree.getParentId()){
                trees.add(findChildren(tree,list));
            }
        }
        return trees;
    }

    @Override
    public List<LayuiTree> treeViewByUser(Long parentId) throws Exception {
        SysUser sysUser = getCurrentUser();
        List<LayuiTree> list = redisFactoryDao.getDatas(REDIS_KEY_RESOURCE,String.valueOf(sysUser.getUserId()),
                new RedisFactoryDao.OnRedisSelectListener<LayuiTree>() {
                    @Override
                    public List fruitless() {
                        List<Long> roleIdList = sysUserRoleDao.getRoleIdsByUserId(sysUser.getUserId());
                        List<Long> organIdList = sysUserOrganDao.getOrganIdsByUserId(sysUser.getUserId());
                        List<Long> tempResourceIds = new ArrayList<>();
                        List<Long> resourceIds = new ArrayList<>();
                        Set<Long> set = new HashSet<>();
                        tempResourceIds.addAll(sysOrganResourceDao.getResourceIdsByOrganIds(organIdList));
                        tempResourceIds.addAll(sysRoleResourceDao.getResourceIdsByRoleIds(roleIdList));
                        for (Long resourceId : tempResourceIds) {
                            if (set.add(resourceId)) {
                                resourceIds.add(resourceId);
                            }
                        }
                        List<SysResource> list = sysResourceDao.selectByIds(resourceIds);
                        List<LayuiTree> trees = new ArrayList<>();
                        for (SysResource tree: list) {
                            if(parentId==tree.getParentId()){
                                trees.add(findChildren(tree,list));
                            }
                        }
                        return trees;
                    }
                });
        return list;
    }

    private SysResource findChildren(SysResource tree,List<SysResource> list){
        for (SysResource sysResource:list) {
            sysResource.setName(sysResource.getName());
            sysResource.setSpread(false);
            if(tree.getResourceId()==sysResource.getParentId()){
                tree.getChildren().add(findChildren(sysResource,list));
            }
        }
        return tree;
    }

    @Override
    public List<Long> getResourceIdsByRoleId(Long roleId) {
        List<Long> resourceIds = sysRoleResourceDao.getResourceIdsByRoleId(roleId);
        return resourceIds;
    }

    @Override
    public List<Long> getResourceIdsByOrganId(Long organId) {
        List<Long> resourceIds = sysOrganResourceDao.getResourceIdsByOrganId(organId);
        return resourceIds;
    }

    @Override
    public List<SysResource> selectByIds(List<Long> resourceIds) {
        List<SysResource> sysResources = sysResourceDao.selectByIds(resourceIds);
        return sysResources;
    }

    @Transactional
    @Override
    public int saveRoleResource(Long roleId, List<Long> resourceIds) {
        //删除所有角色为roleId的用户的  资源和权限 缓存
        List<Long> userIds =  sysUserRoleDao.getUserIdByRoleId(roleId);
        for (int i = 0; i < userIds.size(); i++) {
            redisFactoryDao.delete(REDIS_KEY_RESOURCE+":"+userIds.get(i));
            redisFactoryDao.delete(REDIS_KEY_PURIEW+":"+userIds.get(i));
        }
        //修改角色资源
        int k = 0;
        k+=sysRoleResourceDao.deleteByRoleId(roleId);
        for (int i = 0; i < resourceIds.size(); i++) {
            SysRoleResource sysRoleResource = new SysRoleResource();
            sysRoleResource.setResourceId(resourceIds.get(i));
            sysRoleResource.setRoleId(roleId);
            k+=sysRoleResourceDao.insert(sysRoleResource);
        }
        return k;
    }

    @Transactional
    @Override
    public int saveOrganResource(Long organId, List<Long> resourceIds) {
        //删除所有部门ID为organId的用户的 资源和权限 缓存
        List<Long> userIds = sysUserOrganDao.getUserIdByOrganId(organId);
        for (int i = 0; i < userIds.size(); i++) {
            redisFactoryDao.delete(REDIS_KEY_RESOURCE+":"+userIds.get(i));
            redisFactoryDao.delete(REDIS_KEY_PURIEW+":"+userIds.get(i));
        }
        //修改部门资源
        int k=0;
        k+=sysOrganResourceDao.deleteByOrganId(organId);
        for (int i = 0; i < resourceIds.size(); i++) {
            SysOrganResource sysOrganResource = new SysOrganResource();
            sysOrganResource.setOrganId(organId);
            sysOrganResource.setResourceId(resourceIds.get(i));
            k+=sysOrganResourceDao.insert(sysOrganResource);
        }
        return k;
    }

    @Override
    public List<Long> getResourceIdsByOrganIds(List<Long> organIdList) {
        return sysOrganResourceDao.getResourceIdsByOrganIds(organIdList);
    }

    @Override
    public List<Long> getResourceIdsByRoleIds(List<Long> roleIdList) {
        return sysRoleResourceDao.getResourceIdsByRoleIds(roleIdList);
    }

    private void disPlay(Long resourceId,List<SysResource> list){
        SysResource sysResource = sysResourceDao.selectByPrimaryKey(resourceId);
        if(sysResource!=null){
            list.add(sysResource);
            disPlay(sysResource.getParentId(), list);
        }
    }

    /**
     * 递归删除
     * @param parentId
     */
    private void recursiveDelete(Long parentId){
        List<SysResource> sysResources =sysResourceDao.listByPid(parentId,"");
        if(sysResources!=null&&!sysResources.isEmpty()){
            for (SysResource sysResource: sysResources) {
                sysResourceDao.deleteByPrimaryKey(sysResource.getResourceId());
                recursiveDelete(sysResource.getResourceId());
            }
        }
    }
}