package com.taiji.eap.common.form.formconfColumn.service;

import com.github.pagehelper.PageInfo;
import org.apache.ibatis.annotations.Param;
import com.taiji.eap.common.generator.bean.LayuiTree;
import com.taiji.eap.common.generator.bean.EasyUISubmitData;
import com.taiji.eap.common.form.formconfColumn.bean.FormconfColumn;
import java.util.List;

public interface FormconfColumnService{
     /**
     * 通过主键删除数据
     * @param primaryKey
     * @return
     */
    int deleteByPrimaryKey(Long primaryKey) throws Exception;
     /**
     * 插入数据
     * @param formconfColumn
     * @return
     */
    int insert(FormconfColumn formconfColumn) throws Exception;
     /**
     * 根据主键查询数据
     * @param primaryKey
     * @return
     */
    FormconfColumn selectByPrimaryKey(Long primaryKey) throws Exception;
     /**
     * 根据主键修改数据
     * @param formconfColumn
     * @return
     */
    int updateByPrimaryKey(FormconfColumn formconfColumn) throws Exception;
     /**
     * 无分页查询数据
     * @param searchText 搜索条件
     * @return
     */
    List<FormconfColumn> list(String searchText) throws Exception;
     /**
     * 分页查询数据
     * @param pageNum 当前页数
     * @param pageSize 每页行数
     * @param searchText 搜索条件
     * @return
     * @throws Exception
     */
    PageInfo<FormconfColumn> list(int pageNum, int pageSize, String searchText) throws Exception;

}