package cn.captainshen.controller;

import cn.captainshen.activiti.ActivitiUtil;
import cn.captainshen.entity.Info;
import org.activiti.engine.*;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class IndexController{
    private static HistoryService historyService = ActivitiUtil.getHistoryService();
    private static RepositoryService repositoryService = ActivitiUtil.getRepositoryService();
    private static TaskService taskService = ActivitiUtil.getTaskService();

    @RequestMapping(value = {"/index"}, method = RequestMethod.GET)
    public String index(HttpSession httpSession, Model model){
        List<ProcessDefinition> processes = ActivitiUtil.queryAllDefinitions();
        model.addAttribute("processes", processes);
        return "index";
    }

    @RequestMapping(value = {"/enterProcess"}, method = RequestMethod.GET)
    public String enterProcess(@RequestParam("key") String processKey, HttpSession httpSession){
        httpSession.setAttribute("processKey", processKey);
        return "redirect:/process";
    }

    @RequestMapping(value = {"/process"}, method = RequestMethod.GET)
    public String process(HttpSession httpSession, Model model){
        String loginUser = (String)httpSession.getAttribute("loginUser");
        String processKey = (String)httpSession.getAttribute("processKey");
        String userGroup = (String) httpSession.getAttribute("userGroup");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        httpSession.setAttribute("date", sdf.format(new Date()));

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                                              .processDefinitionKey(processKey).singleResult();
        // 获取已提交任务列表
        List<Info> handledInfoList = new ArrayList<>();
        List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
                                                           .processDefinitionKey(processKey)
                                                           .taskAssignee(loginUser).list();
        for(HistoricTaskInstance historicTaskInstance : historicTaskInstances){
            HistoricProcessInstance historicProcessInstance =  historyService.createHistoricProcessInstanceQuery()
                                                               .processInstanceId(historicTaskInstance.getProcessInstanceId())
                                                               .singleResult();
            // 获取该流程实例已完成的任务
            List<HistoricActivityInstance> actInsts = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(historicProcessInstance.getId())
                    .finished()
                    .orderByHistoricActivityInstanceEndTime()
                    .desc()
                    .list();
            List<HistoricVariableInstance> varInsts = historyService.createHistoricVariableInstanceQuery()
                                                .processInstanceId(historicProcessInstance.getId()).list();
            Info info = new Info();
            info.setProcessId(historicProcessInstance.getId());
            boolean rejected = false;
            for(HistoricVariableInstance var : varInsts){
                if(var.getVariableName().equals("status")){
                    if(((String)var.getValue()).equals("驳回")){
                        rejected = true;
                        info.setCurrentStatus("已驳回");
                    }
                }
            }
            if(!rejected && actInsts.size() > 0){
                if(actInsts.get(0).getActivityName() != null)
                    info.setCurrentStatus(actInsts.get(0).getActivityName() + "已完成");
                else
                    info.setCurrentStatus("会签已完成");
            }
            // 发起人
            if(historicProcessInstance.getStartUserId() != null){
                info.setName(historicProcessInstance.getStartUserId());
                info.setDate(historicProcessInstance.getStartTime().toString());
                info.setProjectName(historicProcessInstance.getProcessDefinitionName());
                handledInfoList.add(info);
            }
        }
        model.addAttribute("handledInfoList", handledInfoList);
        // 获取等待签收列表
        List<Info> claimTaskList = new ArrayList<>();
        List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup(userGroup).taskUnassigned().list();
        for(Task task : tasks){
            Info info = new Info();
            info.setTaskId(task.getId());
            info.setDate(task.getCreateTime().toString());
            claimTaskList.add(info);
        }
        model.addAttribute("claimTaskList", claimTaskList);
        // 获取等待处理列表
        List<Info> taskList = new ArrayList<>();
        List<Task> tasks1 = taskService.createTaskQuery().taskAssignee(loginUser).list();
        for(Task task : tasks1){
            Info info = new Info();
            info.setDate(task.getCreateTime().toString());
            info.setTaskId(task.getId());
            taskList.add(info);
        }
        model.addAttribute("taskList", taskList);
        return "process";
    }
}