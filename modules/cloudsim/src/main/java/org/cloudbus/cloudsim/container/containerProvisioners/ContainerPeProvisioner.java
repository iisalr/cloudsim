package org.cloudbus.cloudsim.container.containerProvisioners;

import java.util.List;

import org.cloudbus.cloudsim.core.GuestEntity;


public abstract class ContainerPeProvisioner {
	/** The mips. */
	private double mips;

	/** The available mips. */
	private double availableMips;

	/**
	 * Creates the new PeProvisioner.
	 * 
	 * @param mips overall amount of MIPS available in the Pe
	 * 
	 * @pre mips>=0
	 * @post $none
	 */
	public ContainerPeProvisioner(double mips) {
		// TODO Auto-generated constructor stub
		setMips(mips);
		setAvailableMips(mips);
	}
	/**
	 * Allocates MIPS for a given Container.
	 *
	 * @param guest container for which the MIPS are being allocated
	 * @param mips  the mips
	 * @return $true if the MIPS could be allocated; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public abstract boolean allocateMipsForGuest(GuestEntity guest, double mips);

	/**
	 * Allocates MIPS for a given Container.
	 * 
	 * @param containerUid the container uid
	 * @param mips the mips
	 * 
	 * @return $true if the MIPS could be allocated; $false otherwise
	 * 
	 * @pre $none
	 * @post $none
	 */
	public abstract boolean allocateMipsForGuest(String containerUid, double mips);

	/**
	 * Allocates MIPS for a given container.
	 *
	 * @param guest container for which the MIPS are being allocated
	 * @param mips  the mips for each virtual Pe
	 * @return $true if the MIPS could be allocated; $false otherwise
	 * @pre $none
	 * @post $none
	 */
	public abstract boolean allocateMipsForGuest(GuestEntity guest, List<Double> mips);

	/**
	 * Gets allocated MIPS for a given VM.
	 *
	 * @param guest container for which the MIPS are being allocated
	 * @return array of allocated MIPS
	 * @pre $none
	 * @post $none
	 */
	public abstract List<Double> getAllocatedMipsForGuest(GuestEntity guest);

	/**
	 * Gets total allocated MIPS for a given VM for all PEs.
	 *
	 * @param guest container for which the MIPS are being allocated
	 * @return total allocated MIPS
	 * @pre $none
	 * @post $none
	 */
	public abstract double getTotalAllocatedMipsForGuest(GuestEntity guest);

	/**
	 * Gets allocated MIPS for a given container for a given virtual Pe.
	 *
	 * @param guest container for which the MIPS are being allocated
	 * @param peId  the pe id
	 * @return allocated MIPS
	 * @pre $none
	 * @post $none
	 */
	public abstract double getAllocatedMipsForGuestByVirtualPeId(GuestEntity guest, int peId);

	/**
	 * Releases MIPS used by a container.
	 *
	 * @param guest the container
	 * @pre $none
	 * @post none
	 */
	public abstract void deallocateMipsForGuest(GuestEntity guest);

	/**
	 * Releases MIPS used by all containers.
	 * 
	 * @pre $none
	 * @post none
	 */
	public void deallocateMipsForAllGuests() {
		setAvailableMips(getMips());
	}

	/**
	 * Gets the MIPS.
	 * 
	 * @return the MIPS
	 */
	public double getMips() {
		return mips;
	}

	/**
	 * Sets the MIPS.
	 * 
	 * @param mips the MIPS to set
	 */
	public void setMips(double mips) {
		this.mips = mips;
	}

	/**
	 * Gets the available MIPS in the PE.
	 * 
	 * @return available MIPS
	 * 
	 * @pre $none
	 * @post $none
	 */
	public double getAvailableMips() {
		return availableMips;
	}

	/**
	 * Sets the available MIPS.
	 * 
	 * @param availableMips the availableMips to set
	 */
	protected void setAvailableMips(double availableMips) {
		this.availableMips = availableMips;
	}

	/**
	 * Gets the total allocated MIPS.
	 * 
	 * @return the total allocated MIPS
	 */
	public double getTotalAllocatedMips() {
		double totalAllocatedMips = getMips() - getAvailableMips();
		if (totalAllocatedMips > 0) {
			return totalAllocatedMips;
		}
		return 0;
	}

	/**
	 * Gets the utilization of the Pe in percents.
	 * 
	 * @return the utilization
	 */
	public double getUtilization() {
		return getTotalAllocatedMips() / getMips();
	}


}
