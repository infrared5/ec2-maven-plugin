package org.kuali.maven.ec2;

import java.util.Collections;

import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.EbsBlockDevice;
import com.amazonaws.services.ec2.model.RegisterImageRequest;
import com.amazonaws.services.ec2.model.RegisterImageResult;

public class EC2UtilsTest {
	private static final Logger logger = LoggerFactory.getLogger(EC2UtilsTest.class);

	public static final String ACCESS_KEY = "AKIAIBA67LRGDPAGELNA";
	public static final String SECRET_KEY_ENCRYPTED = "nVpfVv5p0f7lheZ4Z3M+UW07tLgfL/kmAAbTqXQN4z5XPLvz5VafhQUm1TEXEthxOWzK9LXlnmI=";

	protected EC2Utils getEC2Utils() {
		BasicTextEncryptor bte = new BasicTextEncryptor();
		bte.setPassword(System.getProperty("gbs.master.password"));
		String secretKey = bte.decrypt(SECRET_KEY_ENCRYPTED);
		return EC2Utils.getInstance(ACCESS_KEY, secretKey);
	}

	protected AmazonEC2 getEC2Client() {
		BasicTextEncryptor bte = new BasicTextEncryptor();
		bte.setPassword(System.getProperty("gbs.master.password"));
		String secretKey = bte.decrypt(SECRET_KEY_ENCRYPTED);
		return EC2Utils.getEC2Client(ACCESS_KEY, secretKey);
	}

	@Test
	@Disabled
	public void testRegisterImage2() {
		AmazonEC2 client = getEC2Client();
		try {
			RegisterImageRequest request = new RegisterImageRequest();
			request.setName("ci-slave-2012-02-22-test");
			request.setDescription("ci.slave.test.image");
			request.setArchitecture("x86_64");
			request.setRootDeviceName("/dev/sda1");
			request.setKernelId("aki-825ea7eb");
			BlockDeviceMapping bdm = new BlockDeviceMapping();
			bdm.setDeviceName("/dev/sda1");
			EbsBlockDevice ebs = new EbsBlockDevice();
			ebs.setDeleteOnTermination(Boolean.valueOf(true));
			ebs.setSnapshotId("snap-a6dc42d8");
			ebs.setVolumeSize(Integer.valueOf(128));
			bdm.setEbs(ebs);
			request.setBlockDeviceMappings(Collections.singletonList(bdm));

			RegisterImageResult result = client.registerImage(request);
			String id = result.getImageId();
			System.out.println(id);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Test
	@Disabled
	public void testDescribeImages() {
		try {
			EC2Utils ec2Utils = getEC2Utils();
			String key = "Name";
			String prefix = "CI Slave";
			String device = "/dev/sda1";
			int min = 14;
			ec2Utils.cleanupSlaveImages(key, prefix, device, min);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
