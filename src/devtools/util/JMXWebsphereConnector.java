package devtools.util;

import devtools.configuration.Configuration;
import devtools.configuration.DevToolsProperties;
import devtools.exception.FileNotFoundException;
import devtools.exception.MBeanNotRegistredException;
import devtools.toolwindow.Application;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class JMXWebsphereConnector {

    /** FileNotificationMBean name */
    private final static String MBEAN_FILE_NOTIFICATION = "WebSphere:service=com.ibm.ws.kernel.filemonitor.FileNotificationMBean";
    /** FileNotificationMBean method notifyFileChanges signature */
    private final static String[] MBEAN_FILE_NOTIFICATION_NOTIFYFILECHANGES_SIGNATURE = new String[] {
            Collection.class.getName(), Collection.class.getName(), Collection.class.getName() };

    private final static String MBEAN_APPLICATION = "WebSphere:service=com.ibm.websphere.application.ApplicationMBean,name=*";
    public static final String NAME = "name";
    public static final String STATE = "State";
    public static final String PID = "Pid";

    public JMXConnector connect(String urlJndi) throws IOException {
        JMXServiceURL serviceUrl = new JMXServiceURL(urlJndi);
        return JMXConnectorFactory.connect(serviceUrl, null);
    }

    public Set<ObjectName> getApplications(JMXConnector jmxConnector) throws IOException, MalformedObjectNameException, MBeanNotRegistredException {
        MBeanServerConnection mbean = jmxConnector.getMBeanServerConnection();
        ObjectName applicationsMonitorMBeanName = new ObjectName(MBEAN_APPLICATION);
        return mbean.queryNames(applicationsMonitorMBeanName, null);
    }

    public boolean isConnected(JMXConnector jmxConnector, ObjectName appMonitorObjectName) throws IOException, MBeanNotRegistredException {
        MBeanServerConnection mbean = jmxConnector.getMBeanServerConnection();
        if (!mbean.isRegistered(appMonitorObjectName)) {
            throw new MBeanNotRegistredException("MBean invoke request failed " + appMonitorObjectName.getCanonicalName() + " is not registered.");
        }
        return true;
    }

    public Application getApplication(JMXConnector jmxConnector, ObjectName appMonitorObjectName) throws IOException, MalformedObjectNameException, MBeanNotRegistredException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        MBeanServerConnection mbean = jmxConnector.getMBeanServerConnection();
        if (!mbean.isRegistered(appMonitorObjectName)) {
            throw new MBeanNotRegistredException("MBean invoke request failed " + appMonitorObjectName.getCanonicalName() + " is not registered.");
        }
        String name = appMonitorObjectName.getKeyProperty(NAME);
        String state = mbean.getAttribute(appMonitorObjectName, STATE).toString();
        String pid = mbean.getAttribute(appMonitorObjectName, PID).toString();
        Application application = new Application(name, pid, state, appMonitorObjectName);
        return application;
    }

    public void notifyFileChange(JMXConnector jmxConnector, File application) throws IOException, MalformedObjectNameException, InstanceNotFoundException, MBeanException, ReflectionException, MBeanNotRegistredException {
        MBeanServerConnection mbean = jmxConnector.getMBeanServerConnection();
        // Invoke FileNotificationMBean
        ObjectName fileMonitorMBeanName = new ObjectName(MBEAN_FILE_NOTIFICATION);

        if (!mbean.isRegistered(fileMonitorMBeanName)) {
            throw new MBeanNotRegistredException("MBean invoke request failed " + MBEAN_FILE_NOTIFICATION + " is not registered.");
        }

        // Create a list of absolute paths of each file to be checked
        application.setLastModified(new Date().getTime());
        List<String> modifiedFilePaths = new ArrayList<>();
        modifiedFilePaths.add(application.getAbsolutePath());

        // Set MBean method notifyFileChanges parameters (createdFiles, modifiedFiles, deletedFiles)
        Object[] params = new Object[]{null, modifiedFilePaths, null};

        // Invoke FileNotificationMBean method notifyFileChanges
        mbean.invoke(fileMonitorMBeanName, "notifyFileChanges", params,
                MBEAN_FILE_NOTIFICATION_NOTIFYFILECHANGES_SIGNATURE);
    }

    public boolean invokeOperationApplication(JMXConnector jmxConnector, ObjectName appMonitorObjectName, Application.Operation operation) throws IOException, MBeanNotRegistredException, MBeanException, InstanceNotFoundException, ReflectionException {
        MBeanServerConnection mbean = jmxConnector.getMBeanServerConnection();
        if (!mbean.isRegistered(appMonitorObjectName)) {
            throw new MBeanNotRegistredException("MBean invoke request failed " + appMonitorObjectName.getCanonicalName() + " is not registered.");
        }
        mbean.invoke(appMonitorObjectName, operation.getName(), null, null);
        return true;
    }

    public static void main(String args[]) throws IOException, MBeanNotRegistredException, MalformedObjectNameException, FileNotFoundException, AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException {
        DevToolsProperties toolsProperties = new DevToolsProperties();
        Configuration configuration = toolsProperties.loadConfigurationToReload();
        JMXWebsphereConnector jmxWebsphere = new JMXWebsphereConnector();

        JMXConnector jmxConnector = jmxWebsphere.connect(DevToolsUtil.getJndiPath(configuration));

        Set<ObjectName> applications = jmxWebsphere.getApplications(jmxConnector);
        for(ObjectName objectName : applications) {
            Application application = jmxWebsphere.getApplication(jmxConnector, objectName);
            System.out.println(application);
        }
    }

}
