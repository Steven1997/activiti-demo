package cn.captainshen.controller;

import cn.captainshen.activiti.ActivitiUtil;
import cn.captainshen.entity.Info;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.*;

@Controller
public class ActivitiController {
    private static RuntimeService runtimeService = ActivitiUtil.getRuntimeService();
    private static RepositoryService repositoryService = ActivitiUtil.getRepositoryService();
    private static TaskService taskService = ActivitiUtil.getTaskService();
    private static HistoryService historyService = ActivitiUtil.getHistoryService();
    private static IdentityService identityService = ActivitiUtil.getIdentityService();

    // 开始流程实例
    @RequestMapping(value = "/process/startEvent", method = RequestMethod.POST)
    public String index(@RequestParam("name")String name,
                        @RequestParam("department")String department,
                        @RequestParam("position")String position,
                        @RequestParam("tel")String tel,
                        @RequestParam("project_name")String project_name,
                        @RequestParam("apply_sum")Double apply_sum,
                        HttpSession httpSession){
        try{
            String loginUser = (String)httpSession.getAttribute("loginUser");
            String processKey = (String)httpSession.getAttribute("processKey");
            Map<String, Object> map = new HashMap<>();
            map.put("name", name);
            map.put("department", department);
            map.put("position", position);
            map.put("date", httpSession.getAttribute("date"));
            map.put("projectName", project_name);
            map.put("tel", tel);
            map.put("applySum", apply_sum);
            map.put("status", "发起申请");
            Authentication.setAuthenticatedUserId(loginUser);
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processKey);
            String instanceId = processInstance.getId();
            Task task = taskService.createTaskQuery().processInstanceId(instanceId).singleResult();

            taskService.claim(task.getId(),loginUser);
            taskService.addComment(task.getId(), null, "发起申请");
            ActivitiUtil.commitProcess(task.getId(), map, null);
        }catch (Exception e){
            e.printStackTrace();
        }
        return "redirect:/process?op=0";
    }

    // 签收任务
    @RequestMapping(value = {"/process/claim"}, method = RequestMethod.GET)
    public String claim(@RequestParam("taskId") String taskId,
                        HttpSession httpSession){
        String loginUser = (String)httpSession.getAttribute("loginUser");
        String userGroup = (String)httpSession.getAttribute("userGroup");
        List<IdentityLink> identityLinks = taskService.getIdentityLinksForTask(taskId);
        boolean valid = false;
        for(IdentityLink identityLink : identityLinks){
            if(identityLink.getGroupId().equals(userGroup)){
                valid = true;
                break;
            }
        }
        if(valid){
            taskService.claim(taskId, loginUser);
        }
        return "redirect:/process?op=1";
    }

    // 执行任务
    @RequestMapping(value = {"/process/execute"}, method = RequestMethod.GET)
    public String execute(@RequestParam("taskId") String taskId,
                          HttpSession httpSession,
                          Model model){
        try{
            String loginUser = (String)httpSession.getAttribute("loginUser");
            String procId = ActivitiUtil.findProcessInstanceByTaskId(taskId).getId();
            // 申请表信息
            Info applyInfo = new Info();
            applyInfo.setTaskId(taskId);
            List<HistoricVariableInstance> vars = historyService.createHistoricVariableInstanceQuery()
                                                  .processInstanceId(procId).list();
            for(HistoricVariableInstance var : vars){
                switch (var.getVariableName()){
                    case "name":
                        applyInfo.setName((String)var.getValue());
                        break;
                    case "department":
                        applyInfo.setDepartment((String)var.getValue());
                        break;
                    case "position":
                        applyInfo.setPosition((String)var.getValue());
                        break;
                    case "tel":
                        applyInfo.setTel((String)var.getValue());
                        break;
                    case "date":
                        applyInfo.setDate((String)var.getValue());
                        break;
                    case "projectName":
                        applyInfo.setProjectName((String)var.getValue());
                        break;
                    case "applySum":
                        applyInfo.setApplySum((Double)var.getValue());
                        break;
                    default:
                        break;
                }
            }
            model.addAttribute("info", applyInfo);
            // 历史处理意见
            List<Info> historyInfo = new ArrayList<>();
            List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(procId)
                    .finished()
                    .orderByHistoricActivityInstanceEndTime().asc().list();
            for(HistoricActivityInstance instance : historicActivityInstances){
                if(instance.getAssignee() == null){
                    continue;
                }
                Info info = new Info();
                info.setName(instance.getAssignee());
                info.setDate(instance.getEndTime().toString());
                List<Comment> comments = taskService.getTaskComments(instance.getTaskId());
                info.setComment("");
                for(Comment comment : comments){
                    info.setComment(info.getComment() + " " + comment.getFullMessage());
                }
                historyInfo.add(info);
            }
            model.addAttribute("historyInfo", historyInfo);
            // 获取当前节点可驳回的任务节点
            List<ActivityImpl> backActivities = ActivitiUtil.findBackAvtivity(taskId);
            List<Info> res = new ArrayList<>();
            for(ActivityImpl activity : backActivities){
                Info info = new Info();
                info.setTaskId(activity.getId());
                res.add(info);
            }
            model.addAttribute("backActivities", res);
            return "commit";
        }catch (Exception e){
            e.printStackTrace();
            return "redirect:/process?op=1";
        }
    }

    @RequestMapping(value = {"/process/pass"}, method = RequestMethod.POST)
    public String pass(@RequestParam("pass_reason")String passReason,
                       @RequestParam("taskId")String taskId,
                       HttpSession httpSession) {
        try {
            String loginUser = (String)httpSession.getAttribute("loginUser");
            String procId = ActivitiUtil.findProcessInstanceByTaskId(taskId).getId();
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

            List<HistoricVariableInstance> vars = historyService.createHistoricVariableInstanceQuery()
                                                                .processInstanceId(procId).list();

            Map<String, Object> map = new HashMap<>();
            for (HistoricVariableInstance var : vars){
                if(var.getVariableName().equals("applySum")){
                    map.put("money", (Double)var.getValue());
                    break;
                }
            }
            Authentication.setAuthenticatedUserId(loginUser);
            taskService.addComment(taskId, null, passReason + "(通过)");
            taskService.setVariable(taskId, "status", task.getName() + "已完成");
            ActivitiUtil.commitProcess(taskId, map, null);
            return "redirect:/process?op=0";
        }catch (Exception e){
            e.printStackTrace();
            return "redirect:/process?op=0";
        }
    }

    // 直接驳回并终止此流程实例
    // TODO 回收所有并行任务
    @RequestMapping(value = {"/process/reject"}, method = RequestMethod.POST)
    public String reject(@RequestParam("taskId")String taskId,
                         @RequestParam("reject_reason") String rejectReason,
                         HttpSession httpSession){
        try{
            String loginUser = (String)httpSession.getAttribute("loginUser");
            String userGroup = (String)httpSession.getAttribute("userGroup");
            String proId = ActivitiUtil.findProcessInstanceByTaskId(taskId).getId();

            taskService.setVariable(taskId, "status", "驳回");
            ActivitiUtil.endProcess(taskId);
            return "redirect:/process?op=0";
        }catch (Exception e){
            e.printStackTrace();
            return "redirect:/process?op=0";
        }
    }

    // 退回至特定节点
    // TODO 回收所有并行任务
    @RequestMapping(value = {"/process/rollback"}, method = RequestMethod.POST)
    public String rollback(@RequestParam("taskId")String taskId,
                           @RequestParam("back_activity_id")String backActivityId,
                           @RequestParam("back_reason")String backReason,
                           HttpSession httpSession){
        try {
            String loginUser = (String) httpSession.getAttribute("loginUser");
            String procId = ActivitiUtil.findProcessInstanceByTaskId(taskId).getId();
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            List<HistoricVariableInstance> vars = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(procId).list();

            Map<String, Object> map = new HashMap<>();
            for (HistoricVariableInstance var : vars) {
                if (var.getVariableName().equals("applySum")) {
                    map.put("money", (Double) var.getValue());
                    break;
                }
            }
            Authentication.setAuthenticatedUserId(loginUser);
            taskService.addComment(taskId, null, backReason + "(回退)");
            ActivitiUtil.backProcess(taskId, backActivityId, map);
            return "redirect:/process?op=0";
        }catch (Exception e){
            e.printStackTrace();
            return "redirect:/process?op=0";
        }
    }
}
//TODO 撤回 已完成