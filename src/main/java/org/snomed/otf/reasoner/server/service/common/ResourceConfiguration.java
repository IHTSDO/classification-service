package org.snomed.otf.reasoner.server.service.common;

public class ResourceConfiguration {

	private boolean readonly;
	private boolean useCloud;

	private Local local;
	private Cloud cloud;

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public boolean isUseCloud() {
		return useCloud;
	}

	public void setUseCloud(boolean useCloud) {
		this.useCloud = useCloud;
	}

	public Local getLocal() {
		return local;
	}

	public void setLocal(Local local) {
		this.local = local;
	}

	public Cloud getCloud() {
		return cloud;
	}

	public void setCloud(Cloud cloud) {
		this.cloud = cloud;
	}

	@Override
	public String toString() {
		return "ResourceConfiguration{" +
				"readonly=" + readonly +
				", useCloud=" + useCloud +
				", local=" + local +
				", cloud=" + cloud +
				'}';
	}

	public static class Local {
		private String path;

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = normalisePath(path);
		}

		@Override
		public String toString() {
			return "Local{" +
					"path='" + path + '\'' +
					'}';
		}
	}

	public static class Cloud {
		private String bucketName;
		private String path;

		public String getBucketName() {
			return bucketName;
		}

		public void setBucketName(String bucketName) {
			this.bucketName = bucketName;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = normalisePath(path);
		}

		@Override
		public String toString() {
			return "Cloud{" +
					"bucketName='" + bucketName + '\'' +
					", path='" + path + '\'' +
					'}';
		}
	}

	static String normalisePath(String path) {
		if (path == null || path.isEmpty()) {
			return "";
		}
		if (path.substring(0, 1).equals("/")) {
			path = path.substring(1, path.length());
		}
		if (!path.isEmpty() && path.lastIndexOf("/") != path.length() - 1) {
			path = path + "/";
		}
		return path;
	}
}
