# activiti-demo
赛题名称：【本科组】Activiti国产化迁移与应用实践  
团队名称：4AMNotNull  
介绍：本成果实现了在国产龙芯开发机环境下部署的由Activiti实现的科研经费申请流程。  
流程包括研究人员提出申请、组长审批、院办核准、院长审批（副院长审批）、财务处工作  
人员复核、财务处长会签、财务副处长会签、校长终审、返回申请人等过程。实现了条件审  
批(设定阈值，不同申请金额交由不同角色审批)、并行会签审批(多个并行结点进行会签审  
批，所有结点通过后申请交由下一个结点审批)、驳回审批(直接告知申请人审批不通过)、退  
回至之前的任意环节(将审批退回至之前经过的任意审批环节)、多用户抢签(同一个用户组中  
的用户均可看到申请并抢签，抢到者处理该申请)等功能，具有审批流程可视化、审批结果即  
时化等特点，并支持100以上用户并发。
