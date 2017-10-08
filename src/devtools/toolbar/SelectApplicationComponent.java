package devtools.toolbar;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import devtools.configuration.ApplicationSelected;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map;

public class SelectApplicationComponent implements ProjectComponent {

    private Map<String, File> applications;
    private ApplicationSelected applicationSelected;

    public SelectApplicationComponent() {
        this.applicationSelected = new ApplicationSelected();
    }

    public static SelectApplicationComponent getManager(@NotNull Project project) {
        return project.getComponent(SelectApplicationComponent.class);
    }

    public Map<String, File> getApplications() {
        return applications;
    }

    public void setApplications(Map<String, File> applications) {
        this.applications = applications;
    }

    public ApplicationSelected getApplicationSelected() {
        return applicationSelected;
    }

    public void setApplicationSelected(ApplicationSelected applicationSelected) {
        this.applicationSelected = applicationSelected;
    }

    public void clearApplicationSelected() {
        this.applicationSelected = new ApplicationSelected();
    }
}
