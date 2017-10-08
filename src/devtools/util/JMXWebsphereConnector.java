package devtools.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import devtools.configuration.ApplicationSelected;
import devtools.exception.MBeanNotRegistredException;

import javax.management.*;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class JMXWebsphereConnector {

    /** FileNotificationMBean name */
    private final static String MBEAN_FILE_NOTIFICATION = "WebSphere:service=com.ibm.ws.kernel.filemonitor.FileNotificationMBean";
    /** FileNotificationMBean method notifyFileChanges signature */
    private final static String[] MBEAN_FILE_NOTIFICATION_NOTIFYFILECHANGES_SIGNATURE = new String[] {
            Collection.class.getName(), Collection.class.getName(), Collection.class.getName() };


    public JMXConnector connect(String urlJndi) throws IOException {
        JMXServiceURL serviceUrl = new JMXServiceURL(urlJndi);
        return JMXConnectorFactory.connect(serviceUrl, null);
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

}
