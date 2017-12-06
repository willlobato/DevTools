package devtools.action.projectview;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import devtools.view.addproject.AddProjectWizard;

public class AddProjectToProfile extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
//        Collection<Module> modules = ModuleUtil.getModulesOfType(project, StdModuleTypes.JAVA);
////        modules.iterator().next().get
//
////        System.out.println(modules);
//
//
//        LibraryTable libraryTable = ProjectLibraryTable.getInstance(project);
//        for (Library library : libraryTable.getLibraries()) {
//            System.out.println(library.getRootProvider().getFiles(OrderRootType.CLASSES)[0].getCanonicalPath());
//        }




        AddProjectWizard wizard = new AddProjectWizard(project);
//        wizard.createCenterPanel();
        wizard.show();
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>");

    }
}
