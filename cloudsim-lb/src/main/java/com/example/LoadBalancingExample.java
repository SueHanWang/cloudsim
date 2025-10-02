package com.example;

import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;
import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.allocationpolicies.VmAllocationPolicyRoundRobin;

import java.util.ArrayList;
import java.util.List;

public class LoadBalancingExample {
    private static final int HOSTS = 3;
    private static final int VMS = 6;
    private static final int CLOUDLETS = 12;

    public static void main(String[] args) {
        CloudSimPlus simulation = new CloudSimPlus();
        
        // 建立資料中心（使用 Round Robin 負載平衡）
        Datacenter datacenter = createDatacenter(simulation);
        
        // 建立 Broker
        DatacenterBroker broker = new DatacenterBrokerSimple(simulation);
        
        // 建立虛擬機
        List<Vm> vmList = createVms();
        
        // 建立 Cloudlets（任務）
        List<Cloudlet> cloudletList = createCloudlets();
        
        broker.submitVmList(vmList);
        broker.submitCloudletList(cloudletList);
        
        // 執行模擬
        simulation.start();
        
        // 顯示結果
        List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
        printResults(finishedCloudlets);
    }
    
    private static Datacenter createDatacenter(CloudSimPlus simulation) {
        List<Host> hostList = new ArrayList<>();
        
        for (int i = 0; i < HOSTS; i++) {
            List<Pe> peList = new ArrayList<>();
            for (int j = 0; j < 4; j++) {
                peList.add(new PeSimple(1000)); // 1000 MIPS
            }
            
            Host host = new HostSimple(8192, 100000, 10000, peList);
            hostList.add(host);
        }
        
        // 使用 Round Robin 分配策略
        Datacenter dc = new DatacenterSimple(simulation, hostList, new VmAllocationPolicyRoundRobin());
        return dc;
    }
    
    private static List<Vm> createVms() {
        List<Vm> list = new ArrayList<>();
        
        for (int i = 0; i < VMS; i++) {
            Vm vm = new VmSimple(1000, 2); // 1000 MIPS, 2 PEs
            vm.setRam(512).setBw(1000).setSize(10000);
            list.add(vm);
        }
        
        return list;
    }
    
    private static List<Cloudlet> createCloudlets() {
        List<Cloudlet> list = new ArrayList<>();
        
        for (int i = 0; i < CLOUDLETS; i++) {
            Cloudlet cloudlet = new CloudletSimple(10000, 1); // 10000 MI
            cloudlet.setFileSize(300).setOutputSize(300);
            list.add(cloudlet);
        }
        
        return list;
    }
    
    private static void printResults(List<Cloudlet> cloudlets) {
        System.out.println("\n========== 負載平衡實驗結果 ==========");
        System.out.printf("%-10s %-10s %-10s %-15s %-15s%n",
            "Cloudlet", "Status", "VM", "開始時間", "完成時間");
        
        for (Cloudlet cloudlet : cloudlets) {
            System.out.printf("%-10d %-10s %-10d %-15.2f %-15.2f%n",
                cloudlet.getId(),
                cloudlet.getStatus(),
                cloudlet.getVm().getId(),
                cloudlet.getExecStartTime(),
                cloudlet.getFinishTime());
        }
        
        System.out.println("\n========== VM 使用統計 ==========");
        cloudlets.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                c -> c.getVm().getId(),
                java.util.stream.Collectors.counting()
            ))
            .forEach((vmId, count) -> 
                System.out.printf("VM %d: 執行了 %d 個任務%n", vmId, count)
            );
    }
}