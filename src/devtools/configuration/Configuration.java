package devtools.configuration;

public class Configuration {

    private String profilePath = "";
    private String profileUse = "";

    public String getProfilePath() {
        return profilePath;
    }

    public void setProfilePath(String profilePath) {
        this.profilePath = profilePath;
    }

    public String getProfileUse() {
        return profileUse;
    }

    public void setProfileUse(String profileUse) {
        this.profileUse = profileUse;
    }

    public boolean profilePathIsBlank() {
        return this.profilePath != null && this.profilePath.equals("");
    }

    public boolean profileUseIsBlank() {
        return this.profileUse != null && this.profileUse.equals("");
    }

}
