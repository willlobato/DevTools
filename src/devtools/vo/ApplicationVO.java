package devtools.vo;

import javax.management.ObjectName;

public class ApplicationVO {

    private String application;
    private String pid;
    private String state;
    private ObjectName objectName;

    public enum Operation {
        START("start"),
        STOP("stop"),
        RESTART("restart"),
        REFRESH("refresh");

        private String name;

        Operation(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public ApplicationVO() {
    }

    public ApplicationVO(String application, String pid, String state, ObjectName objectName) {
        this.application = application;
        this.pid = pid;
        this.state = state;
        this.objectName = objectName;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public ObjectName getObjectName() {
        return objectName;
    }

    public void setObjectName(ObjectName objectName) {
        this.objectName = objectName;
    }

    @Override
    public String toString() {
        return "ApplicationVO{" +
                "application='" + application + '\'' +
                ", pid='" + pid + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}
