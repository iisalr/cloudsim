package org.cloudbus.cloudsim.examples.network.datacenter;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.EX.DatacenterBrokerEX;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.GuestEntity;
import org.cloudbus.cloudsim.distributions.ExponentialDistr;
import org.cloudbus.cloudsim.network.datacenter.*;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class TandemAppExample2 {

	private static List<GuestEntity> guestList;

	private static List<NetworkHost> hostList = new ArrayList<>();

	private static NetworkDatacenter datacenter;

	private static DatacenterBrokerEX broker;

	private static final int numberOfHosts = 4;
	private static final int numberOfVms = 4;

	// Realistic cloudlet arrival
    private static ExponentialDistr distr;

	/**
	 * Example of tandem DAG: A ---> B
	 * with Datacenter configuration:        switch
	 * 								   		 /	 \
	 * 									 Host0    Host1
	 * 						   			VM0 VM2  VM1 VM3
	 *
	 * and realistic cloudlet arrivals, sampled from exponential distribution.
	 * Depending on the cloudlet placement, the network may be used or not.
	 * 
	 * @param args the args
	 * @author Remo Andreoli
	 */
	public static void main(String[] args) {

		Log.printLine("Starting TandemAppExample...");

		try {
			distr = new ExponentialDistr(5, 1000);

			int num_user = 1; // number of cloud users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false; // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			// Datacenters are the resource providers in CloudSim. We need at
			// list one of them to run a CloudSim simulation
			datacenter = createDatacenter("Datacenter_0");

			// Third step: Create Broker
			broker = createBroker();

			// Fifth step: Create one Cloudlet

			guestList = CreateVMs(datacenter.getId());

			for(int i = 0; i < 2; i++) {
				AppCloudlet app = new AppCloudlet(AppCloudlet.APP_Workflow, i, 2000, broker.getId());
				createTaskList(app);

				broker.submitCloudletList(app.cList, distr.sample());
			}


			// submit vm list to the broker

			broker.submitGuestList(guestList);

			// Sixth step: Starts the simulation
			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();
			printCloudletList(newList);
			System.out.println("numberofcloudlet " + newList.size() + " Data transfered "
					+ NetworkGlobals.totaldatatransfer);

			Log.printLine("DiamondAppExample finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}

	/**
	 * Creates the datacenter.
	 * 
	 * @param name
	 *            the name
	 * 
	 * @return the datacenter
	 */
	private static NetworkDatacenter createDatacenter(String name) {
		// 2. A Machine contains one or more PEs or CPUs/Cores.
		// In this example, it will have only one core.
		// List<Pe> peList = new ArrayList<Pe>();

		int mips = 10;
		int ram = 2048; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 100000;

		for (int i=0; i<numberOfHosts; i++) {
			List<Pe> peList = new ArrayList<>();
			peList.add(new Pe(0, new PeProvisionerSimple(mips)));
			peList.add(new Pe(1, new PeProvisionerSimple(mips)));
			hostList.add(new NetworkHost(
					i,
					new RamProvisionerSimple(ram),
					new BwProvisionerSimple(bw),
					storage,
					peList,
					new VmSchedulerTimeShared(peList)));
		}



		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<>(); // no SAN devices as of now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch,
				os,
				vmm,
				hostList,
				time_zone,
				cost,
				costPerMem,
				costPerStorage,
				costPerBw);

		NetworkDatacenter datacenter = null;
		try {
			datacenter = new NetworkDatacenter(
					name,
					characteristics,
					new VmAllocationPolicySimple(hostList),
					storageList,
					0);

		} catch (Exception e) {
			e.printStackTrace();
		}

		// Create Internal Datacenter network
		CreateNetwork(datacenter);
		return datacenter;
	}

	/**
	 * Creates the broker.
	 * 
	 * @return the datacenter broker
	 */
	private static DatacenterBrokerEX createBroker() {
		DatacenterBrokerEX broker = null;
		try {
			broker = new DatacenterBrokerEX("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects.
	 * 
	 * @param list
	 *            list of Cloudlets
	 * @throws IOException
	 */
	private static void printCloudletList(List<Cloudlet> list) throws IOException {
		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID"
				+ indent + "Time" + indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (Cloudlet cloudlet : list) {
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");
				Log.printLine(indent + indent + cloudlet.getResourceId() + indent + indent + indent
						+ cloudlet.getGuestId() + indent + indent + dft.format(cloudlet.getActualCPUTime())
						+ indent + indent + dft.format(cloudlet.getExecStartTime()) + indent + indent
						+ dft.format(cloudlet.getExecFinishTime()));
			}
		}

	}

	/**
	 * Creates virtual machines in a datacenter
	 * @param datacenterId The id of the datacenter where to create the VMs.
	 */
	private static ArrayList<GuestEntity> CreateVMs(int datacenterId) {
		ArrayList<GuestEntity> vmList = new ArrayList<>();

		int mips = 1;
		long size = 10000; // image size (MB)
		int ram = 512; // vm memory (MB)
		long bw = 1000;
		int pesNumber = 1;
		String vmm = "Xen";

		for (int i=0; i<numberOfVms; i++) {
			vmList.add(new Vm(
					i,
					broker.getId(),
					mips,
					pesNumber,
					ram,
					bw,
					size,
					vmm,
					new NetworkCloudletSpaceSharedScheduler()));
		}

		return vmList;
	}

	static private void createTaskList(AppCloudlet appCloudlet) {
		long fileSize = NetworkConstants.FILE_SIZE;
		long outputSize = NetworkConstants.OUTPUT_SIZE;
		UtilizationModel utilizationModel = new UtilizationModelFull();

		// Define cloudlets
		NetworkCloudlet cla = new NetworkCloudlet(
				NetworkConstants.currentCloudletId,
				0,
				1,
				fileSize,
				outputSize,
				utilizationModel,
				utilizationModel,
				utilizationModel);
		NetworkConstants.currentCloudletId++;
		cla.setUserId(broker.getId());
		//cla.setGuestId(guestList.get(0).getId());
		appCloudlet.cList.add(cla);

		NetworkCloudlet clb = new NetworkCloudlet(
				NetworkConstants.currentCloudletId,
				0,
				1,
				fileSize,
				outputSize,
				utilizationModel,
				utilizationModel,
				utilizationModel);
		NetworkConstants.currentCloudletId++;
		clb.setUserId(broker.getId());
		//clb.setGuestId(guestList.get(1).getId());
		appCloudlet.cList.add(clb);

		// Configure task stages within the cloudlets
		//
		cla.addExecutionStage(1000);
		cla.addSendStage(1000, clb);

		//
		clb.addRecvStage(cla);
		clb.addExecutionStage(1000);
	}

	private static void CreateNetwork(NetworkDatacenter dc) {
		// Create ToR switch
		Switch ToRSwitch = new Switch("Edge0", NetworkConstants.EdgeSwitchPort, Switch.SwitchLevel.EDGE_LEVEL,
					0, NetworkConstants.BandWidthEdgeHost, NetworkConstants.BandWidthEdgeAgg, dc);

		dc.registerSwitch(ToRSwitch);

		// Attach to hosts
		for (NetworkHost netHost : dc.<NetworkHost>getHostList()) {
			dc.attachSwitchToHost(ToRSwitch, netHost);
		}

	}
}