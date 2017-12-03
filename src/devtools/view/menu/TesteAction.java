package devtools.view.menu;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.impl.libraries.ProjectLibraryTable;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;

import java.util.Collection;

public class TesteAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {


        Project project = event.getData(PlatformDataKeys.PROJECT);
        Collection<Module> modules = ModuleUtil.getModulesOfType(project, StdModuleTypes.JAVA);
//        modules.iterator().next().get

        System.out.println(modules);


        LibraryTable libraryTable = ProjectLibraryTable.getInstance(project);
        for (Library library : libraryTable.getLibraries()) {
            System.out.println(library.getRootProvider().getFiles(OrderRootType.CLASSES)[0].getCanonicalPath());
        }

    }
}
