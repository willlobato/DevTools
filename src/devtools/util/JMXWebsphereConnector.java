package devtools.util;

import devtools.configuration.Configuration;
import devtools.configuration.DevToolsProperties;
import devtools.exception.DevToolsException;
import devtools.exception.JMXWebsphereException;
import devtools.exception.MBeanNotRegistredException;
import devtools.vo.ApplicationVO;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class JMXWebsphereConnector {

    /** ApplicationMBean name */
    private final static String MBEAN_APPLICATION = "WebSphere:service=com.ibm.websphere.application.ApplicationMBean,name=*";

    /** RuntimeUpdateNotificationMBean name */
    private final static String MBEAN_RUNTIME_UPDATE_NOTIFICATION = "WebSphere:name=com.ibm.websphere.runtime.update.RuntimeUpdateNotificationMBean";

    /** FileNotificationMBean name */
    private final static String MBEAN_FILE_NOTIFICATION = "WebSphere:service=com.ibm.ws.kernel.filemonitor.FileNotificationMBean";

    /** FileNotificationMBean method notifyFileChanges signature */
    private final static String[] MBEAN_FILE_NOTIFICATION_NOTIFYFILECHANGES_SIGNATURE = new String[] {
            Collection.class.getName(), Collection.class.getName(), Collection.class.getName() };

    public static final String NAME = "name";
    public static final String STATE = "State";
    public static final String PID = "Pid";

    public JMXConnector connect(String urlJndi) throws JMXWebsphereException {
        try {
            JMXServiceURL serviceUrl = new JMXServiceURL(urlJndi);
            return JMXConnectorFactory.connect(serviceUrl, null);
        } catch (Exception e) {
            throw new JMXWebsphereException("Failed to create a connection to the connector server at the given address", e);
        }
    }

    public void disconnect(JMXConnector jmxConnector) throws JMXWebsphereException {
        try {
            if(jmxConnector != null) {
                jmxConnector.close();
            }
        } catch (Exception e) {
            throw new JMXWebsphereException("Failed to disconnect JMXConnector", e);
        }
    }

    public MBeanServerConnection getMBeanServerConnection(JMXConnector jmxConnector, ObjectName appMonitorObjectName) throws JMXWebsphereException {
        try {
            MBeanServerConnection mbean = jmxConnector.getMBeanServerConnection();
            if (!mbean.isRegistered(appMonitorObjectName)) {
                throw new JMXWebsphereException("MBean invoke request failed " + appMonitorObjectName.getCanonicalName() + " is not registered.");
            }
            return mbean;
        } catch (Exception e) {
            throw new JMXWebsphereException("Failed to Get MBeanServerConnection", e);
        }
    }

    public ObjectName getRuntimeUpdateNotification() throws JMXWebsphereException {
        try {
            ObjectName runtimeUpdateNotificationMBean = new ObjectName(MBEAN_RUNTIME_UPDATE_NOTIFICATION);
            return runtimeUpdateNotificationMBean;
        } catch (Exception e) {
            throw new JMXWebsphereException("Failed to connect on MBean "+ MBEAN_RUNTIME_UPDATE_NOTIFICATION, e);
        }
    }

    public Set<ObjectName> getApplications(JMXConnector jmxConnector) throws JMXWebsphereException {
        try {
            MBeanServerConnection mbean = jmxConnector.getMBeanServerConnection();
            ObjectName applicationsMonitorMBeanName = new ObjectName(MBEAN_APPLICATION);
            return mbean.queryNames(applicationsMonitorMBeanName, null);
        } catch (Exception e) {
            throw new JMXWebsphereException("Failed to connect on MBean "+ MBEAN_APPLICATION, e);
        }
    }

//    public boolean isApplicationsConnected(JMXConnector jmxConnector) throws IOException, MBeanNotRegistredException, MalformedObjectNameException {
//        MBeanServerConnection mbean = jmxConnector.getMBeanServerConnection();
//        ObjectName applicationsMonitorMBeanName = new ObjectName(MBEAN_APPLICATION);
//        if (!mbean.isRegistered(applicationsMonitorMBeanName)) {
//            throw new MBeanNotRegistredException("MBean invoke request failed " + applicationsMonitorMBeanName.getCanonicalName() + " is not registered.");
//        }
//        return true;
//    }
//
//    public boolean isConnected(JMXConnector jmxConnector, ObjectName appMonitorObjectName) {
//        MBeanServerConnection mbean = jmxConnector.getMBeanServerConnection();
//        if (!mbean.isRegistered(appMonitorObjectName)) {
//            throw new MBeanNotRegistredException("MBean invoke request failed " + appMonitorObjectName.getCanonicalName() + " is not registered.");
//        }
//        return true;
//    }

    public ApplicationVO getApplication(JMXConnector jmxConnector, ObjectName appMonitorObjectName) throws JMXWebsphereException {
        try {
            MBeanServerConnection mbean = jmxConnector.getMBeanServerConnection();
            if (!mbean.isRegistered(appMonitorObjectName)) {
                throw new JMXWebsphereException("MBean invoke request failed " + appMonitorObjectName.getCanonicalName() + " is not registered.");
            }
            String name = appMonitorObjectName.getKeyProperty(NAME);
            String state = mbean.getAttribute(appMonitorObjectName, STATE).toString();
            String pid = mbean.getAttribute(appMonitorObjectName, PID).toString();
            ApplicationVO applicationVO = new ApplicationVO(name, pid, state, appMonitorObjectName);
            return applicationVO;
        } catch (Exception e) {
            throw new JMXWebsphereException("Failed to connect on MBean "+ appMonitorObjectName.getCanonicalName(), e);
        }
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

    public boolean invokeOperationApplication(MBeanServerConnection mbean, ObjectName appMonitorObjectName, ApplicationVO.Operation operation) throws JMXWebsphereException {
        try {
            mbean.invoke(appMonitorObjectName, operation.getName(), null, null);
        } catch (Exception e) {
            throw new JMXWebsphereException("Failed to invoke operation " + operation.getName() +" on MBean "+ appMonitorObjectName.getCanonicalName(), e);
        }
        return true;
    }

    public static void main(String args[]) throws DevToolsException, JMXWebsphereException, IOException {
        DevToolsProperties toolsProperties = new DevToolsProperties();
        Configuration configuration = toolsProperties.loadConfigurationToReload();
        JMXWebsphereConnector jmxWebsphere = new JMXWebsphereConnector();

        JMXConnector jmxConnector = jmxWebsphere.connect(DevToolsUtil.getJndiPath(configuration));

        Set<ObjectName> applications = jmxWebsphere.getApplications(jmxConnector);
        for(ObjectName objectName : applications) {
            ApplicationVO applicationVO = jmxWebsphere.getApplication(jmxConnector, objectName);
            System.out.println(applicationVO);
        }
    }

}
