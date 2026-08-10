package to.shipproof.model;

import javax.validation.constraints.NotBlank;

public class SubmissionRequest {
    @NotBlank
    private String projectName;
    @NotBlank
    private String repositoryUrl;
    @NotBlank
    private String demoUrl;
    @NotBlank
    private String pitch;
    private String socialUrl;
    private String track;

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getRepositoryUrl() { return repositoryUrl; }
    public void setRepositoryUrl(String repositoryUrl) { this.repositoryUrl = repositoryUrl; }
    public String getDemoUrl() { return demoUrl; }
    public void setDemoUrl(String demoUrl) { this.demoUrl = demoUrl; }
    public String getPitch() { return pitch; }
    public void setPitch(String pitch) { this.pitch = pitch; }
    public String getSocialUrl() { return socialUrl; }
    public void setSocialUrl(String socialUrl) { this.socialUrl = socialUrl; }
    public String getTrack() { return track; }
    public void setTrack(String track) { this.track = track; }
}
