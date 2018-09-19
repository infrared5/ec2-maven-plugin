package org.kuali.maven.ec2;

import org.jasypt.util.text.BasicTextEncryptor;

import com.amazonaws.services.ec2.AmazonEC2;

public class Common {
	public static final String ACCESS_KEY = "AKIAIBA67LRGDPAGELNA";
	public static final String SECRET_KEY_ENCRYPTED = "nVpfVv5p0f7lheZ4Z3M+UW07tLgfL/kmAAbTqXQN4z5XPLvz5VafhQUm1TEXEthxOWzK9LXlnmI=";

	public static EC2Utils getEC2Utils() {
		BasicTextEncryptor bte = new BasicTextEncryptor();
		bte.setPassword(System.getProperty("gbs.master.password"));
		String secretKey = bte.decrypt(SECRET_KEY_ENCRYPTED);
		return EC2Utils.getInstance(ACCESS_KEY, secretKey);
	}

	public static AmazonEC2 getEC2Client() {
		BasicTextEncryptor bte = new BasicTextEncryptor();
		bte.setPassword(System.getProperty("gbs.master.password"));
		String secretKey = bte.decrypt(Common.SECRET_KEY_ENCRYPTED);
		return EC2Utils.getEC2Client(Common.ACCESS_KEY, secretKey);
	}
	
}
