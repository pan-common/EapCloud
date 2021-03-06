
package com.taiji.eap.common.generator.controller;

import com.github.pagehelper.PageInfo;
import com.taiji.eap.common.base.BaseController;
import com.taiji.eap.common.generator.bean.ColumnExtend;
import com.taiji.eap.common.generator.service.ColumnExtendService;
import com.taiji.eap.common.http.entity.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("columnExtend")
public class ColumnExtendController extends BaseController{

    @Autowired
    private ColumnExtendService columnExtendService;

    @GetMapping(value = "list")
    @ResponseBody
    public PageInfo<ColumnExtend> list(Integer pageNum,Integer pageSize,String searchText){
        PageInfo<ColumnExtend> pageInfo = null;
        try {
            pageInfo = columnExtendService.list(pageNum,pageSize,searchText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pageInfo;
    }

    @PostMapping(value = "add")
    @ResponseBody
    public Response<String> add(ColumnExtend columnExtend){
        try {
            int k = columnExtendService.insert(columnExtend);
            if(k>0){
                return renderSuccess("添加成功");
            }else {
                return renderError("失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return renderError(e.getMessage());
        }
    }

    @PostMapping(value = "edit")
    @ResponseBody
    public Response<String> edit(ColumnExtend columnExtend){
        try {
            int k = columnExtendService.updateByPrimaryKey(columnExtend);
            if(k>0){
                return renderSuccess("修改成功");
            }else {
                return renderError("失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return renderError(e.getMessage());
        }
    }

    @PostMapping(value = "delete")
    @ResponseBody
    public Response<String> delete(Long id){
        try {
            int k = columnExtendService.deleteByPrimaryKey(id);
            if(k>0){
                return renderSuccess("删除成功");
            }else {
                return renderError("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return renderError(e.getMessage());
        }
    }

    @GetMapping(value = "selectOne")
    @ResponseBody
    public Response<ColumnExtend> selectOne(Long id){
         try {
            return renderSuccess(columnExtendService.selectByPrimaryKey(id));
         } catch (Exception e) {
             e.printStackTrace();
             return renderError(e.getMessage());
          }
    }
}