package edu.rit.se.git;

import edu.rit.se.util.ElapsedTimer;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;

/**
 * A class that, given a git repository, collects references to all tags in that repository
 * and supplies a list of those references for further processing.
 */
public class RepositoryInitializer {

    // Git command constants
    private static final String REMOTE = "remote";
    private static final String ORIGIN = "origin";
    private static final String URL = "url";
    private static final String GIT_USERNAME = "u";
    private static final String GIT_PASSWORD = "p";

    // Program constants
    private static final String REPO_OUT_DIR = "repos";

    // Constructor fields
    private String repoDir;
    private String gitURI;

    // Set after initialization
    private Git repoRef = null;

    // Timer for reporting
    private ElapsedTimer cloneTimer = null;

    // Prevents other functionality of the class from being used if the git init fails
    private Boolean gitDidInit = false;

    public RepositoryInitializer(String uri, String baseName) {
        this.repoDir = String.join(File.separator, REPO_OUT_DIR, baseName);
        this.gitURI = uri;
    }

    /**
     * Initializes the repository, which:
     * 1. Clones the repository locally (Don't forget to clean it up)
     * 2. Sets the remote reference for the repository
     * @return True if the initialization was successful, else False
     */
    public boolean initRepo() {
        final File newGitRepo = new File(this.repoDir);
        if( newGitRepo.exists() ) {
            this.cleanRepo();
        }
        newGitRepo.mkdirs();
        try {
            this.startCloneElapsedTimer();
            // Clone an instance of the repository locally
            this.repoRef = Git.cloneRepository()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(GIT_USERNAME, GIT_PASSWORD))
                    .setURI(this.gitURI)
                    .setDirectory(newGitRepo)
                    .setCloneAllBranches(false)
                    .call();
            // Add a remote instance to the repository (to be used for tag listing)
            this.repoRef.getRepository().getConfig().setString(REMOTE, ORIGIN, URL, this.gitURI);
            this.repoRef.getRepository().getConfig().save();
            this.endCloneElapsedTimer();
            this.gitDidInit = true;
        } catch (GitAPIException e) {
            System.err.println("Git API error in git init. Repository will be skipped.");
        } catch (IOException e) {
            System.err.println("IOException when setting remote in gew repo.");
        }
        return this.gitDidInit;
    }

    public RepositoryCommitReference getMostRecentCommit(String head) {
        final RevWalk revWalk = new RevWalk(this.repoRef.getRepository());
        try {
            return new RepositoryCommitReference(
                    this.repoRef,
                    GitUtil.getRepoNameFromGithubURI(this.gitURI),
                    this.gitURI,
                    revWalk.parseCommit(this.repoRef.getRepository().resolve(
                            head != null ? head : Constants.HEAD))
            );
        } catch (IOException e) {
            System.err.println("Could not parse the supplied commit for the repository: " + head);
        }
        return null;
    }

    /**
     * Attempts to delete the files generated by the initializer
     */
    public void cleanRepo() {
        if( this.repoRef != null ) {
            this.repoRef.getRepository().close();
        }
        File repo = new File(this.repoDir);
        try {
            FileUtils.deleteDirectory(repo);
        } catch (IOException e) {
            System.err.println("Error deleting git repo");
        }
    }

    public String getRepoDir() {
        return this.repoDir;
    }

    public boolean didInitialize() {
        return this.gitDidInit;
    }

    private void startCloneElapsedTimer() {
        this.cloneTimer = new ElapsedTimer();
        this.cloneTimer.start();
    }

    private void endCloneElapsedTimer() {
        this.cloneTimer.end();
//        System.out.println(String.format("Finished cloning: %s in %,dms",
//                GitUtil.getRepoNameFromGithubURI(this.gitURI), this.cloneTimer.readMS()));
    }
}
