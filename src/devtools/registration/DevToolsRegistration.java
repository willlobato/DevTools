package devtools.registration;

import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;

public class DevToolsRegistration implements ApplicationComponent {

    @Override
    public void initComponent() {
//        ActionManager am = ActionManager.getInstance();
//        am.get
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "DevTools";
    }
}
