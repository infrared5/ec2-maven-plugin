package org.kuali.maven.ec2;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.kuali.maven.ec2.pojo.ImageComparator;
import org.kuali.maven.ec2.state.ImageStateRetriever;
import org.kuali.maven.ec2.state.InstanceStateRetriever;
import org.kuali.maven.ec2.state.SnapshotStateRetriever;
import org.kuali.maven.ec2.state.StateRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.AmazonEC2Exception;
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.CreateSnapshotRequest;
import com.amazonaws.services.ec2.model.CreateSnapshotResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DeleteSnapshotRequest;
import com.amazonaws.services.ec2.model.DeregisterImageRequest;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.DescribeSnapshotsRequest;
import com.amazonaws.services.ec2.model.DescribeSnapshotsResult;
import com.amazonaws.services.ec2.model.EbsBlockDevice;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RegisterImageRequest;
import com.amazonaws.services.ec2.model.RegisterImageResult;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Snapshot;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

public class EC2Utils {

	private static final Logger logger = LoggerFactory.getLogger(EC2Utils.class);

	AmazonEC2 client;

	private EC2Utils(final AWSCredentials credentials) {
		//this.client = new AmazonEC2Client(credentials);
		this.client = AmazonEC2ClientBuilder.standard()
				.withRegion(Regions.US_EAST_2)
				.withCredentials(new AWSCredentialsProvider() {

					public AWSCredentials getCredentials() {
						return credentials;
					}

					public void refresh() {

					}
				})
				.build();
	}

	public static EC2Utils getInstance(String accessKey, String secretKey) {
		AWSCredentials credentials = getCredentials(accessKey, secretKey);
		return getInstance(credentials);
	}

	public static EC2Utils getInstance(AWSCredentials credentials) {
		return new EC2Utils(credentials);
	}

	public static EC2Utils getImage(String accessKey, String secretKey) {
		AWSCredentials credentials = getCredentials(accessKey, secretKey);
		return getImage(credentials);
	}

	public static EC2Utils getImage(AWSCredentials credentials) {
		return new EC2Utils(credentials);
	}

	public void cleanupSlaveImages(String key, String prefix, String device, int min) {
		// Just in case
		if (min < 1) {
			min = 1;
		}
		List<Image> images = getEC2ImagesOwnedByMe();
		Collections.sort(images, new ImageComparator());
		List<SlaveTag> slaveTags = new ArrayList<SlaveTag>();
		for (Image image : images) {
			if (containsTag(image.getTags(), key, prefix)) {
				Tag tag = getTag(image.getTags(), key, prefix);
				SlaveTag slaveTag = getSlaveTag(image, tag, device);
				slaveTags.add(slaveTag);
			}
		}
		int size = slaveTags.size();
		if (size <= min) {
			logger.info("Retaining all slave images since there are only " + size + " and " + min + " must be retained");
			return;
		}
		Collections.sort(slaveTags);
		Collections.reverse(slaveTags);
		List<SlaveTag> delete = new ArrayList<SlaveTag>();
		for (int i = min; i < size; i++) {
			delete.add(slaveTags.get(i));
		}
		logger.info("Retaining " + min + " slave images");
		logger.info("Deleting " + delete.size() + " slave images");
		for (SlaveTag st : delete) {
			logger.info("Deleting " + st.getSequence() + " - " + st.getImageId() + " - " + st.getSnapshotId());
			deRegisterImage(st.getImageId());
			deleteSnapshot(st.getSnapshotId());
		}
	}

	public boolean containsTag(List<Tag> tags, String key, String prefix) {
		for (Tag tag : tags) {
			if (matches(tag, key, prefix)) {
				return true;
			}
		}
		return false;
	}

	public String getSnapshotId(Image image, String deviceName) {
		List<BlockDeviceMapping> mappings = image.getBlockDeviceMappings();
		for (BlockDeviceMapping mapping : mappings) {
			if (deviceName.equals(mapping.getDeviceName())) {
				EbsBlockDevice ebd = mapping.getEbs();
				return ebd.getSnapshotId();
			}
		}
		return null;
	}

	public SlaveTag getSlaveTag(Image image, Tag tag, String device) {
		String[] tokens = StringUtils.splitByWholeSeparator(tag.getValue(), " - ");
		String snapshotId = getSnapshotId(image, device);

		SlaveTag slaveTag = new SlaveTag();
		slaveTag.setImageId(image.getImageId());
		slaveTag.setKey(tag.getKey());
		slaveTag.setLabel(tokens[0]);
		slaveTag.setDate(tokens[1]);
		slaveTag.setSequence(new Integer(tokens[2]));
		slaveTag.setSnapshotId(snapshotId);
		return slaveTag;
	}

	public boolean matches(Tag tag, String key, String prefix) {
		return key.equals(tag.getKey()) && tag.getValue().startsWith(prefix);
	}

	public Tag getTag(List<Tag> tags, String key, String prefix) {
		for (Tag tag : tags) {
			if (matches(tag, key, prefix)) {
				return tag;
			}
		}
		return null;
	}

	public Image getImage(String imageId) {
		DescribeImagesRequest request = new DescribeImagesRequest();
		request.setImageIds(Collections.singletonList(imageId));
		DescribeImagesResult result = client.describeImages(request);
		List<Image> images = result.getImages();
		if (isEmpty(images)) {
			throw new IllegalArgumentException("Unable to locate '" + imageId + "'");
		}
		if (images.size() > 1) {
			throw new IllegalArgumentException("Found " + images.size() + " matching '" + imageId + "'");
		}

		return images.get(0);
	}

	public RegisterImageResult registerImage(RegisterImageRequest request, WaitControl wc) {
		RegisterImageResult result = client.registerImage(request);
		if (wc.isWait()) {
			String id = result.getImageId();
			int timeout = wc.getTimeout();
			StateRetriever sr = new ImageStateRetriever(this, id);
			logger.info("Waiting up to " + timeout + " seconds for '" + id + "' to reach state '" + wc.getState() + "'");
			waitForState(sr, wc);
		} else {
			logger.info("Created image " + result.getImageId());
		}
		return result;
	}

	public void terminate(String instanceId, WaitControl wc) {
		TerminateInstancesRequest request = new TerminateInstancesRequest();
		request.setInstanceIds(Collections.singletonList(instanceId));
		client.terminateInstances(request);
		if (wc.isWait()) {
			StateRetriever sr = new InstanceStateRetriever(this, instanceId);
			logger.info("Waiting up to " + wc.getTimeout() + " seconds for " + instanceId + " to terminate");
			waitForState(sr, wc);
		} else {
			logger.info("Terminated " + instanceId);
		}
	}

	public Instance wait(Instance i, WaitControl wc, Properties props) {
		if (wc.isWait()) {
			StateRetriever sr = new InstanceStateRetriever(this, i.getInstanceId());
			logger.info("Waiting up to " + wc.getTimeout() + " seconds for " + i.getInstanceId() + " to start");
			waitForState(sr, wc);
			Instance running = getEC2Instance(i.getInstanceId());
			String id = i.getInstanceId();
			String dns = running.getPublicDnsName();
			String name = getTagValue(running, "Name");
			logger.info("EC2 Instance: " + name + " (" + id + ") " + dns);
			props.setProperty("ec2.instance.dns", running.getPublicDnsName());
			waitForPorts(running.getPublicDnsName(), wc);
			if(wc.getExtraWait() != 0) {
				logger.info("Extra Wait of " + wc.getExtraWait() + "s for all services to be started up");
				sleep(wc.getExtraWait() * 1000);
			}
			
			return running;
		} else {
			logger.info("Launched " + i.getInstanceId());
			return i;
		}
	}

	public void createTags(Instance instance, List<Tag> tags) {
		if (isEmpty(tags)) {
			return;
		}
		while(!createTagsInternal(instance, tags)) {
			logger.info("Instance not yet available for tagging");
			EC2Utils.sleep(500);
		}
	}

	private boolean createTagsInternal(Instance instance, List<Tag> tags) {
		try {
			CreateTagsRequest request = new CreateTagsRequest();
			request.setResources(Collections.singletonList(instance.getInstanceId()));
			request.setTags(tags);
			client.createTags(request);
			return true;
		}
		catch(AmazonEC2Exception e) {
			return false;
		}
	}

	public Instance getSingleEC2Instance(RunInstancesRequest request) {
		RunInstancesResult result = client.runInstances(request);
		Reservation r = result.getReservation();
		List<Instance> instances = r.getInstances();
		Instance instance = instances.get(0);
		
/*		// check that you can query this instance
		// this will avoid com.amazonaws.services.ec2.model.AmazonEC2Exception: The instance ID 'i-0a99681d856ab576f' does not exist (Service: AmazonEC2; Status Code: 400; Error Code: InvalidInstanceID.NotFound; Request ID: a376a3e9-0029-45ea-8381-45824d757016)
		while(!instanceIsAvailable(instance)) {
			
			
		}*/
		return instance;
	}

	private boolean instanceIsAvailable(Instance instance) {
		try {
			getEC2Instance(instance.getInstanceId());
			return true;
		}
		catch(AmazonEC2Exception e) {
			return false;			
		}
	}

	protected Filter getFilterFromTag(String tag, String value) {
		Filter filter = new Filter();
		filter.setName("tag:" + tag);
		filter.setValues(Collections.singletonList(value));
		return filter;
	}

	protected DescribeInstancesRequest getDescribeInstancesRequest(Tag tag) {
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		Filter filter = getFilterFromTag(tag.getKey(), tag.getValue());
		request.setFilters(Collections.singletonList(filter));
		return request;
	}

	protected int validate(List<Instance> instances, Tag tag, boolean failIfNotFound) {
		int size = instances.size();
		String msg = tag.getKey() + "=" + tag.getValue() + " matched " + size + " instances";
		if (size == 1) {
			return size;
		}
		if (size > 1) {
			throw new IllegalStateException(msg);
		}
		// size == 0
		if (failIfNotFound) {
			throw new IllegalStateException(msg);
		} else {
			logger.info(msg);
		}
		return size;
	}

	public Instance findInstanceFromTag(Tag tag, boolean failIfNotFound) {
		DescribeInstancesRequest request = getDescribeInstancesRequest(tag);
		DescribeInstancesResult result = client.describeInstances(request);
		List<Instance> instances = getAllInstances(result.getReservations());
		int size = validate(instances, tag, failIfNotFound);
		if (size == 1) {
			return instances.get(0);
		} else {
			return null;
		}
	}

	public List<Instance> getEC2Instances() {
		return getEC2Instances(Collections.<String> emptyList());
	}

	public List<Instance> getEC2Instances(List<String> instanceIds) {
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		request.setInstanceIds(instanceIds);
		DescribeInstancesResult result = client.describeInstances(request);
		return getAllInstances(result.getReservations());
	}

	public List<Image> getEC2Images(List<String> imageIds) {
		DescribeImagesRequest request = new DescribeImagesRequest();
		request.setImageIds(imageIds);
		DescribeImagesResult result = client.describeImages(request);
		return result.getImages();
	}

	public List<Image> getEC2ImagesOwnedByMe() {
		DescribeImagesRequest request = new DescribeImagesRequest();
		request.withOwners("self");
		DescribeImagesResult result = client.describeImages(request);

		return result.getImages();
	}

	public void deleteSnapshot(String snapshotId) {
		DeleteSnapshotRequest request = new DeleteSnapshotRequest();
		request.setSnapshotId(snapshotId);
		client.deleteSnapshot(request);
	}

	public void deRegisterImage(String ImageId) {
		DeregisterImageRequest request = new DeregisterImageRequest();
		request.setImageId(ImageId);
		client.deregisterImage(request);
	}

	public List<Snapshot> getEC2SnapshotsbyTag(Tag tag) {
		DescribeSnapshotsRequest request = new DescribeSnapshotsRequest();
		Filter filter = getFilterFromTag(tag.getKey(), tag.getValue());
		request.setFilters(Collections.singletonList(filter));
		DescribeSnapshotsResult result = client.describeSnapshots(request);
		return result.getSnapshots();
	}

	public Snapshot createSnapshot(String volumeId, String description, WaitControl wc) {
		CreateSnapshotRequest request = new CreateSnapshotRequest(volumeId, description);
		CreateSnapshotResult result = client.createSnapshot(request);
		String id = result.getSnapshot().getSnapshotId();
		if (wc.isWait()) {
			StateRetriever sr = new SnapshotStateRetriever(this, id);
			logger.info("Waiting up to " + wc.getTimeout() + " seconds for snapshot '" + id + "' to complete");
			waitForState(sr, wc);
		} else {
			logger.info("Completed " + id);
		}
		return result.getSnapshot();
	}

	public void tag(String id, String name, String value) {
		tag(id, new Tag(name, value));
	}

	public void tag(String id, Tag tag) {
		tag(id, Collections.singletonList(tag));
	}

	/**
	 * Adds or overwrites tags for the specified resource. <code>id</code> can be an EC2 instance id, snapshot id, volume id, etc. Each
	 * resource can have a maximum of 10 tags. Each tag consists of a key-value pair. Tag keys must be unique per resource.
	 */
	public void tag(String id, List<Tag> tags) {
		if (isEmpty(tags)) {
			return;
		}
		CreateTagsRequest request = new CreateTagsRequest();
		request.setResources(Collections.singletonList(id));
		request.setTags(tags);
		client.createTags(request);
		logger.info("Tagged '" + id + "' with " + tags.size() + " tags");
	}

	public static AWSCredentials getCredentials(String accessKey, String secretKey) {
		return new BasicAWSCredentials(accessKey, secretKey);
	}

	public static AmazonEC2 getEC2Client(String accessKey, String secretKey) {
		final AWSCredentials credentials = getCredentials(accessKey, secretKey);
		//return new AmazonEC2Client(credentials);
		return AmazonEC2ClientBuilder.standard()
				.withRegion(Regions.US_EAST_2)
				.withCredentials(new AWSCredentialsProvider() {

					public AWSCredentials getCredentials() {
						return credentials;
					}

					public void refresh() {

					}
				})
				.build();
	}

	public String getTagValue(Instance i, String tag) {
		return getTagValue(i.getTags(), tag);
	}

	public String getTagValue(Image i, String tag) {
		return getTagValue(i.getTags(), tag);
	}

	public String getTagValue(List<Tag> tags, String tag) {
		for (Tag t : tags) {
			if (t.getKey().equals(tag)) {
				return t.getValue();
			}
		}
		return "";
	}

	public void waitForState(StateRetriever retriever, WaitControl wc) {
		long now = System.currentTimeMillis();
		long timeout = now + wc.getTimeout() * 1000;
		// Wait a little bit before we query AWS for state information
		// If you query immediately it can sometimes flake out
		sleep(wc.getInitialPause());
		while (true) {
			now = System.currentTimeMillis();
			if (now > timeout) {
				throw new IllegalStateException("Timed out waiting for state '" + wc.getState() + "'");
			}
			long remaining = (timeout - now) / 1000;
			String newState = retriever.getState();
			if (newState.equals(wc.getState())) {
				logger.info("Success!!! state=" + newState);
				// set remaining as following timeout
				wc.setTimeout((int)remaining);
				break;
			}
			else {
				logger.info(newState + " - " + remaining + "s");
				sleep(wc.getSleep());
			}
		}
	}
	
	public void waitForPorts(String dns, WaitControl wc) {
		if(wc.getWaitPorts() != null && wc.getWaitPorts().size()>0) {
			long now = System.currentTimeMillis();
			long timeout = now + wc.getTimeout() * 1000;
			for (Integer port : wc.getWaitPorts()) {
				while (true) {
					now = System.currentTimeMillis();
					if (now > timeout) {
						throw new IllegalStateException("Timed out waiting for port '" + port + "'");
					}
					long remaining = (timeout - now) / 1000;
					if (checkOpenPort(dns, port)) {
						logger.info("Success!!! Port open=" + port);
						break;
					}
					else {
						logger.info("port " + port + " not open yet - " + remaining + "s");
						sleep(wc.getSleep());
					}
				}		
			}
			
		}
	}

	private boolean checkOpenPort(String host, int waitPort) {
		Socket s = null;
        try {
            s = new Socket(host, waitPort);
            return true;
        }
        catch (Exception e) {
            return false;
        }
        finally {
            if(s != null)
                try {s.close();}
            catch(Exception e){}
        }
	}

	public static final void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

	public List<Instance> getAllInstances(List<Reservation> reservations) {
		List<Instance> instances = new ArrayList<Instance>();
		for (Reservation r : reservations) {
			instances.addAll(r.getInstances());
		}
		return instances;
	}

	public Snapshot getSnapshot(String snapshotId) {
		DescribeSnapshotsRequest request = new DescribeSnapshotsRequest();
		request.setSnapshotIds(Collections.singletonList(snapshotId));
		DescribeSnapshotsResult result = client.describeSnapshots(request);
		List<Snapshot> snapshots = result.getSnapshots();
		return snapshots.get(0);
	}

	public Instance getEC2Instance(String instanceId) {
		DescribeInstancesRequest request = new DescribeInstancesRequest();
		request.setInstanceIds(Collections.singletonList(instanceId));
		DescribeInstancesResult result = client.describeInstances(request);
		List<Reservation> reservations = result.getReservations();
		Reservation r = reservations.get(0);
		List<Instance> instances = r.getInstances();
		return instances.get(0);
	}

	public static final boolean isEmpty(Collection<?> c) {
		return c == null || c.size() == 0;
	}

}
