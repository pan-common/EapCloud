package com.taiji.eap.common.area.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.taiji.eap.common.generator.bean.LayuiTree;
import com.taiji.eap.common.area.bean.Area;
import com.taiji.eap.common.area.dao.AreaDao;
import com.taiji.eap.common.area.service.AreaService;
import com.taiji.eap.common.redis.dao.impl.RedisFactoryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class AreaServiceImpl implements AreaService{

    @Autowired
    private AreaDao areaDao;

    @Autowired
    private RedisFactoryDao<LayuiTree> redisFactoryDao;

    @Transactional
    @Override
    public int deleteByPrimaryKey(Integer primaryKey) {
        return areaDao.deleteByPrimaryKey(primaryKey);
    }

    @Transactional
    @Override
    public int insert(Area area) {
        return areaDao.insert(area);
    }

    @Override
    public Area selectByPrimaryKey(Integer primaryKey) {
        return areaDao.selectByPrimaryKey(primaryKey);
    }

    @Transactional
    @Override
    public int updateByPrimaryKey(Area area) {
        return areaDao.updateByPrimaryKey(area);
    }

    @Override
    public List<Area> list(String searchText) {
        return areaDao.list(searchText);
    }

    @Override
    public PageInfo<Area> list(int pageNum, int pageSize, String searchText) throws Exception {
        PageHelper.startPage(pageNum,pageSize);
        List<Area> areas = areaDao.list(searchText);
        PageInfo<Area> pageInfo = new PageInfo<Area>(areas);
        return pageInfo;
    }

    @Override
    public List<Area> listByPid(Integer parentId) throws Exception {
        return areaDao.listByPid(parentId,"");
    }

    @Override
    public PageInfo<Area> listByPid(Integer parentId, int pageNum, int pageSize, String searchText) throws Exception {
        PageHelper.startPage(pageNum, pageSize);
        List<Area> list = areaDao.listByPid(parentId,searchText);
        PageInfo<Area> pageInfo = new PageInfo<Area>(list);
        return pageInfo;
    }

    @Override
    public List<Area> getPath(Integer areaId) throws Exception {
        List<Area> list = new ArrayList<Area>();
        if(areaId!=1){
            disPlay(areaId, list);
        }else {
            list.add(new Area(1,"中国"));
        }
        Collections.reverse(list);
        return list;
    }

    @Override
    public List<LayuiTree> treeView(Integer parentId) throws Exception {
        List<LayuiTree> trees = redisFactoryDao.getDatas("areatree", "", new RedisFactoryDao.OnRedisSelectListener() {
            @Override
            public List fruitless() {
                List<LayuiTree> trees = new ArrayList<>();
                List<Area> list = areaDao.selectLevel3();
                for (Area tree : list) {
                    if(parentId!=null&&tree!=null&&tree.getParentId()!=null) {
                        if (parentId.equals(tree.getParentId())) {
                            trees.add(findChildren(tree, list));
                        }
                    }
                }
                return trees;
            }
        });
        return trees;
    }

    private Area findChildren(Area tree,List<Area> list){
        for (Area area:list) {
            area.setName(area.getAreaName());
            area.setSpread(true);
            if(tree.getAreaId().equals(area.getParentId())){
                tree.getChildren().add(findChildren(area,list));
            }
        }
        return tree;
    }

    private void disPlay(Integer areaId,List<Area> list){
        Area area = areaDao.selectByPrimaryKey(areaId);
        if(area!=null){
            list.add(area);
            disPlay(area.getParentId(), list);
        }
    }
}