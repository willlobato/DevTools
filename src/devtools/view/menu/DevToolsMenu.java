package devtools.view.menu;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DevToolsMenu extends ActionGroup {

    private static final String LABEL = "DevTools";

    public DevToolsMenu() {
        super(LABEL, null, null);
    }

    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
        return new AnAction[]{new ConfigurationAction(), new RestartApplicationAction()};
    }
}
