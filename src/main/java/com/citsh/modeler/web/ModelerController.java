package com.citsh.modeler.web;

import java.util.List;

import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("modeler")
public class ModelerController {
	 private static Logger logger = LoggerFactory.getLogger(ModelerController.class);
	@Autowired 
	private ProcessEngine processEngine;
	
    @RequestMapping("modeler-list")
    public String list(org.springframework.ui.Model model) {
        List<Model> models = processEngine.getRepositoryService()
                .createModelQuery().list();
        model.addAttribute("models", models);

        return "modeler/modeler-list";
    }
	
    /**
     * 新建模型创建模板
     * */
	@RequestMapping("modeler-open")
	public String open(@RequestParam(value = "id", required = false) String id){
		RepositoryService repositoryService=processEngine.getRepositoryService();
		Model model = null;
		if(!StringUtils.isEmpty(id)){
			  model = repositoryService.getModel(id);
		}else{
			 model = repositoryService.newModel();
	         repositoryService.saveModel(model);
	         id = model.getId();
		}
		  return "redirect:/cdn/modeler/modeler.html?modelId=" + id;		
	}
	
	/**
	 * 删除
	 * */
	 @RequestMapping("modeler-remove")
	    public String remove(@RequestParam("id") String id) {
	        processEngine.getRepositoryService().deleteModel(id);

	        return "redirect:/modeler/modeler-list.do";
	    }
	 
	    @RequestMapping("modeler-deploy")
	    public String deploy(@RequestParam("id") String id,
	            org.springframework.ui.Model theModel) throws Exception {
	    	//暂时
	        String tenantId ="1";
	        RepositoryService repositoryService = processEngine
	                .getRepositoryService();
	        Model modelData = repositoryService.getModel(id);
	        byte[] bytes = repositoryService
	                .getModelEditorSource(modelData.getId());

	        if (bytes == null) {
	            theModel.addAttribute("message", "模型数据为空，请先设计流程并成功保存，再进行发布。");

	            return "modeler/failure";
	        }

	        JsonNode modelNode = (JsonNode) new ObjectMapper().readTree(bytes);
	        byte[] bpmnBytes = null;

	        BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
	        bpmnBytes = new BpmnXMLConverter().convertToXML(model);

	        String processName = modelData.getName() + ".bpmn.xml";
	        Deployment deployment = repositoryService.createDeployment()
	                .name(modelData.getName())
	                .addString(processName, new String(bpmnBytes, "UTF-8"))
	                .tenantId(tenantId).deploy();
	        modelData.setDeploymentId(deployment.getId());
	        repositoryService.saveModel(modelData);
	        return "redirect:/modeler/modeler-list.do";
	    }

}
