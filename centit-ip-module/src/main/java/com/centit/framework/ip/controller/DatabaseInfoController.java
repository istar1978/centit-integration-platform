package com.centit.framework.ip.controller;

import com.alibaba.fastjson.JSONArray;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.common.ResponseMapData;
import com.centit.framework.components.OperationLogCenter;
import com.centit.framework.core.controller.BaseController;
import com.centit.framework.ip.po.DatabaseInfo;
import com.centit.framework.ip.service.DatabaseInfoManager;
import com.centit.framework.model.basedata.OperationLog;
import com.centit.framework.security.model.CentitUserDetails;
import com.centit.support.database.utils.DataSourceDescription;
import com.centit.support.database.utils.PageDesc;
import com.centit.support.json.JsonPropertyUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Map;

@Controller
@RequestMapping("/sys/database")
public class DatabaseInfoController extends BaseController {
	
	@Resource
    private DatabaseInfoManager databaseInfoMag;

    private String optId = "DATABASE";

	@RequestMapping(method = RequestMethod.GET)
	public void list( PageDesc pageDesc, HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> searchColumn = BaseController.convertSearchColumn(request);

//        JSONArray listObjects = databaseInfoMag.queryDatabaseAsJson(
//                StringBaseOpt.objectToString(searchColumn.get("databaseName")), pageDesc);
        JSONArray listObjects = databaseInfoMag.listObjectsAsJson(searchColumn, pageDesc);

        ResponseMapData resData = new ResponseMapData();
        resData.addResponseData(BaseController.OBJLIST, listObjects);
        resData.addResponseData(BaseController.PAGE_DESC, pageDesc);

        JsonResultUtils.writeResponseDataAsJson(resData, response);
    }

	/*
	 * 新增
	 */
    @RequestMapping(method = {RequestMethod.POST})
    public void saveDatabaseInfo(@Valid DatabaseInfo databaseinfo,
                                 HttpServletRequest request, HttpServletResponse response) {
    	
    	DatabaseInfo temp = databaseInfoMag.getObjectById(databaseInfoMag.getNextKey());
    	if (temp!=null){
    		JsonResultUtils.writeErrorMessageJson("该数据库标识已存在", response);
            return;
        }
        //加密
      	databaseinfo.setClearPassword(databaseinfo.getPassword());
    	databaseinfo.setCreated(super.getLoginUserCode(request));
        databaseInfoMag.saveNewObject(databaseinfo);

        JsonResultUtils.writeBlankJson(response);

        /**********************log************************/
        OperationLogCenter.logNewObject(request, optId, databaseinfo.getDatabaseCode(), OperationLog.P_OPT_LOG_METHOD_C,
                "新增数据库", databaseinfo);
        /**********************log************************/
    }

    /**
     * 连接测试
     * @param databaseInfo 数据库信息
     * @param response 返回
     */
    @RequestMapping(value = "testConnect", method = {RequestMethod.GET})
    public void testConnect(@Valid DatabaseInfo databaseInfo, HttpServletResponse response) {


        boolean result = DataSourceDescription.testConntect(new DataSourceDescription(
                databaseInfo.getDatabaseUrl(),
                databaseInfo.getUsername(),
                databaseInfo.getPassword()));

        if (result) {
        	JsonResultUtils.writeSingleDataJson("连接测试成功",response);
        } else {
        	JsonResultUtils.writeErrorMessageJson("数据库连接测试失败！", response);
        }

    }

    /*
     * 编辑保存
     */
    @RequestMapping(value = "/{databaseCode}", method = {RequestMethod.PUT})
    public void updateDatabaseInfo(@PathVariable String databaseCode, @Valid DatabaseInfo databaseinfo,
                                   HttpServletRequest request, HttpServletResponse response) {
    	DatabaseInfo temp = databaseInfoMag.getObjectById(databaseCode);
    	if (!databaseinfo.getPassword().equals(temp.getPassword())){
    		databaseinfo.setClearPassword(databaseinfo.getPassword());
    	}

    	DatabaseInfo oldValue = new DatabaseInfo();
        BeanUtils.copyProperties(temp, oldValue);
        databaseInfoMag.mergeObject(databaseinfo);

        JsonResultUtils.writeBlankJson(response);

        /**********************log****************************/
        OperationLogCenter.logUpdateObject(request, optId, databaseCode, OperationLog.P_OPT_LOG_METHOD_U,
                "更新数据库信息", databaseinfo, oldValue);
        /**********************log****************************/
    }

    @RequestMapping(value = "/{databaseCode}", method = {RequestMethod.GET})
    public void getDatabaseInhfo(@PathVariable String databaseCode, HttpServletResponse response) {
    	DatabaseInfo databaseInfo = databaseInfoMag.getObjectById(databaseCode);

        JsonResultUtils.writeSingleDataJson(databaseInfo, response,
                JsonPropertyUtils.getExcludePropPreFilter(DatabaseInfo.class, "databaseInfo"));
    }
    @RequestMapping(value = "/{databaseCode}", method = {RequestMethod.DELETE})
    public void deleteDatabase(@PathVariable String databaseCode,
                               HttpServletRequest request, HttpServletResponse response) {
        DatabaseInfo databaseInfo = databaseInfoMag.getObjectById(databaseCode);
    	databaseInfoMag.deleteObjectById(databaseCode);

    	JsonResultUtils.writeBlankJson(response);

        /******************************log********************************/
        OperationLogCenter.logDeleteObject(request, optId, databaseCode, OperationLog.P_OPT_LOG_METHOD_D,
                "删除数据库", databaseInfo);
        /******************************log********************************/
    }

   
    
}
