package cn.captainshen;
import cn.captainshen.activiti.ActivitiUtil;
import org.activiti.engine.*;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;

import javax.jws.soap.SOAPBinding;
import java.util.HashMap;
import java.util.Map;

public class Deploy {

    public static void deploy() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        // 流程部署
        if(processEngine != null){
            System.out.println("engine acquired...");
        }
        String name = "FundApply.bpmn20.xml";
        ActivitiUtil.deployProcess(name);
        // 100个研究人员
        IdentityService identityService = processEngine.getIdentityService();
        Group group = identityService.newGroup("研究人员");
        identityService.saveGroup(group);
        for(int i = 0; i < 100; ++i){
            String username = "研究人员" + i;
            User user = identityService.newUser(username);
            user.setPassword("password");
            identityService.saveUser(user);
            identityService.createMembership(user.getId(), group.getId());
        }

        // 5个小组长
        group = identityService.newGroup("组长");
        identityService.saveGroup(group);
        for(int i = 0; i < 5; ++i){
            String username = "组长" + i;
            User user = identityService.newUser(username);
            user.setPassword("password");
            identityService.saveUser(user);
            identityService.createMembership(user.getId(), group.getId());
        }

        // 5个院办公室负责人
        group = identityService.newGroup("院办公室负责人");
        identityService.saveGroup(group);
        for(int i = 0; i < 5; ++i){
            String username = "院办公室负责人" + i;
            User user = identityService.newUser(username);
            user.setPassword("password");
            identityService.saveUser(user);
            identityService.createMembership(user.getId(), group.getId());
        }

        // 1个院长
        group = identityService.newGroup("院长");
        identityService.saveGroup(group);
        for(int i = 0; i < 1; ++i){
            String username = "院长" + i;
            User user = identityService.newUser(username);
            user.setPassword("password");
            identityService.saveUser(user);
            identityService.createMembership(user.getId(), group.getId());
        }

        // 5个副院长
        group = identityService.newGroup("副院长");
        identityService.saveGroup(group);
        for(int i = 0; i < 5; ++i){
            String username = "副院长" + i;
            User user = identityService.newUser(username);
            user.setPassword("password");
            identityService.saveUser(user);
            identityService.createMembership(user.getId(), group.getId());
        }

        // 5个财务处工作人员
        group = identityService.newGroup("财务处工作人员");
        identityService.saveGroup(group);
        for(int i = 0; i < 5; ++i){
            String username = "财务处工作人员" + i;
            User user = identityService.newUser(username);
            user.setPassword("password");
            identityService.saveUser(user);
            identityService.createMembership(user.getId(), group.getId());
        }

        // 1个处长
        group = identityService.newGroup("财务处长");
        identityService.saveGroup(group);
        for(int i = 0; i < 1; ++i){
            String username = "财务处长" + i;
            User user = identityService.newUser(username);
            user.setPassword("password");
            identityService.saveUser(user);
            identityService.createMembership(user.getId(), group.getId());
        }

        // 2个副处长
        group = identityService.newGroup("财务副处长");
        identityService.saveGroup(group);
        for(int i = 0; i < 5; ++i){
            String username = "财务副处长" + i;
            User user = identityService.newUser(username);
            user.setPassword("password");
            identityService.saveUser(user);
            identityService.createMembership(user.getId(), group.getId());
        }

        // 1个校长
        group = identityService.newGroup("校长");
        identityService.saveGroup(group);
        for(int i = 0; i < 5; ++i){
            String username = "校长" + i;
            User user = identityService.newUser(username);
            user.setPassword("password");
            identityService.saveUser(user);
            identityService.createMembership(user.getId(), group.getId());
        }
    }
}
