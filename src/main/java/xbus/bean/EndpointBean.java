package xbus.bean;

public class EndpointBean {
	private String fullPath;
	private boolean nodeOriented;
	public String getFullPath() {
		return fullPath;
	}
	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}
	public boolean isNodeOriented() {
		return nodeOriented;
	}
	public void setNodeOriented(boolean nodeOriented) {
		this.nodeOriented = nodeOriented;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fullPath == null) ? 0 : fullPath.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EndpointBean other = (EndpointBean) obj;
		if (fullPath == null) {
			if (other.fullPath != null)
				return false;
		} else if (!fullPath.equals(other.fullPath))
			return false;
		return true;
	}
}
